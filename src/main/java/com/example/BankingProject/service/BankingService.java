
package com.example.BankingProject.service;

import com.example.BankingProject.exception.AccountNotFoundException;
import com.example.BankingProject.exception.InsufficientBalanceException;
import com.example.BankingProject.exception.InvalidAmountException;
import com.example.BankingProject.exception.UserNotFoundException;
import com.example.BankingProject.model.BankAccount;
import com.example.BankingProject.model.User;
import com.example.BankingProject.repository.BankAccountRepository;
import com.example.BankingProject.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BankingService {

    private static final Logger logger = LogManager.getLogger(BankingService.class);

    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;
    private static final int ACCOUNT_NUMBER_START = 1000;

    public BankingService(BankAccountRepository bankAccountRepository, UserRepository userRepository) {
        this.bankAccountRepository = bankAccountRepository;
        this.userRepository = userRepository;
        logger.info("BankingService initialized successfully");
    }

    @Transactional
    public BankAccount createAccount(String accountHolderName, BigDecimal initialBalance, Long userId) {
        logger.info("Creating account for holder: {} with initial balance: {}, userId: {}",
                accountHolderName, initialBalance, userId);

        if (initialBalance == null || initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            logger.error("Account creation failed - invalid initial balance: {}", initialBalance);
            throw new InvalidAmountException("Initial balance cannot be negative");
        }

        // Validate user exists if userId is provided
        if (userId != null) {
            userRepository.findById(userId)
                    .orElseThrow(() -> {
                        logger.error("Account creation failed - User not found: {}", userId);
                        return new UserNotFoundException("User not found with ID: " + userId);
                    });
        }

        try {
            // Generate account number
            Integer maxAccountNumber = bankAccountRepository.findMaxAccountNumber();
            int nextAccountNumber = (maxAccountNumber != null) ? maxAccountNumber + 1 : ACCOUNT_NUMBER_START + 1;
            String accountNumber = "ACC" + nextAccountNumber;

            logger.debug("Generated account number: {}", accountNumber);

            BankAccount account = new BankAccount(accountHolderName, initialBalance, userId);
            account.setAccountNumber(accountNumber);

            BankAccount savedAccount = bankAccountRepository.save(account);
            logger.info("Account created successfully - ID: {}, Account Number: {}, Holder: {}, User ID: {}",
                    savedAccount.getId(), savedAccount.getAccountNumber(),
                    savedAccount.getAccountHolderName(), savedAccount.getUserId());

            return savedAccount;
        } catch (Exception e) {
            logger.error("Error creating account for holder: {}", accountHolderName, e);
            throw e;
        }
    }

    @Transactional
    public void transferMoney(Long senderAccountId, Long receiverAccountId, BigDecimal amount) {
        logger.info("Processing transfer - Sender ID: {}, Receiver ID: {}, Amount: {}",
                senderAccountId, receiverAccountId, amount);

        validateAmount(amount);

        try {
            BankAccount senderAccount = bankAccountRepository.findById(senderAccountId)
                    .orElseThrow(() -> {
                        logger.error("Transfer failed - Sender account not found: {}", senderAccountId);
                        return new AccountNotFoundException("Sender account not found with ID: " + senderAccountId);
                    });

            BankAccount receiverAccount = bankAccountRepository.findById(receiverAccountId)
                    .orElseThrow(() -> {
                        logger.error("Transfer failed - Receiver account not found: {}", receiverAccountId);
                        return new AccountNotFoundException("Receiver account not found with ID: " + receiverAccountId);
                    });

            BigDecimal senderCurrentBalance = senderAccount.getBalance();
            if (senderCurrentBalance.compareTo(amount) < 0) {
                logger.warn("Transfer failed - Insufficient balance. Sender ID: {}, Current Balance: {}, Transfer Amount: {}",
                        senderAccountId, senderCurrentBalance, amount);
                throw new InsufficientBalanceException("Insufficient balance for transfer");
            }

            BigDecimal senderNewBalance = senderAccount.getBalance().subtract(amount);
            BigDecimal receiverCurrentBalance = receiverAccount.getBalance();
            BigDecimal receiverNewBalance = receiverAccount.getBalance().add(amount);

            bankAccountRepository.updateBalance(senderAccountId, senderNewBalance);
            bankAccountRepository.updateBalance(receiverAccountId, receiverNewBalance);

            logger.info("Transfer successful - Sender ID: {} (Balance: {} -> {}), Receiver ID: {} (Balance: {} -> {})",
                    senderAccountId, senderCurrentBalance, senderNewBalance,
                    receiverAccountId, receiverCurrentBalance, receiverNewBalance);

        } catch (Exception e) {
            logger.error("Error processing transfer - Sender ID: {}, Receiver ID: {}, Amount: {}",
                    senderAccountId, receiverAccountId, amount, e);
            throw e;
        }
    }

    @Transactional
    public void transferMoneyForUserByAccountNumber(Long userId, String senderAccountNumber, String receiverAccountNumber, BigDecimal amount) {
        logger.info("Processing transfer for user by account number - User ID: {}, Sender: {}, Receiver: {}, Amount: {}",
                userId, senderAccountNumber, receiverAccountNumber, amount);

        validateAmount(amount);

        try {
            BankAccount senderAccount;

            if (senderAccountNumber != null) {
                // Verify sender account belongs to the user
                senderAccount = bankAccountRepository.findByAccountNumber(senderAccountNumber)
                        .orElseThrow(() -> {
                            logger.error("Transfer failed - Sender account not found: {}", senderAccountNumber);
                            return new AccountNotFoundException("Sender account not found with account number: " + senderAccountNumber);
                        });

                // Verify the sender account belongs to the authenticated user
                if (!senderAccount.getUserId().equals(userId)) {
                    logger.error("Transfer failed - Sender account {} does not belong to user {}", senderAccountNumber, userId);
                    throw new AccountNotFoundException("Sender account does not belong to the authenticated user");
                }
            } else {
                // Use primary account if no sender account specified
                senderAccount = getPrimaryAccountForUser(userId);
            }

            BankAccount receiverAccount = bankAccountRepository.findByAccountNumber(receiverAccountNumber)
                    .orElseThrow(() -> {
                        logger.error("Transfer failed - Receiver account not found: {}", receiverAccountNumber);
                        return new AccountNotFoundException("Receiver account not found with account number: " + receiverAccountNumber);
                    });

            // Perform the transfer
            transferMoney(senderAccount.getId(), receiverAccount.getId(), amount);

            logger.info("Transfer by account number successful for user - User ID: {}, Sender: {} (ID: {}), Receiver: {} (ID: {})",
                    userId, senderAccount.getAccountNumber(), senderAccount.getId(), receiverAccountNumber, receiverAccount.getId());

        } catch (Exception e) {
            logger.error("Error processing transfer for user by account number - User ID: {}, Sender: {}, Receiver: {}, Amount: {}",
                    userId, senderAccountNumber, receiverAccountNumber, amount, e);
            throw e;
        }
    }

    public List<BankAccount> getAllAccounts() {
        logger.debug("Retrieving all accounts");

        try {
            List<BankAccount> accounts = (List<BankAccount>) bankAccountRepository.findAll();
            logger.info("Retrieved {} accounts", accounts.size());
            return accounts;
        } catch (Exception e) {
            logger.error("Error retrieving all accounts", e);
            throw e;
        }
    }

    //Methods for user-specific operations
    public List<BankAccount> getAccountsByUserId(Long userId) {
        logger.debug("Retrieving accounts for user ID: {}", userId);

        try {
            List<BankAccount> accounts = bankAccountRepository.findByUserId(userId);
            logger.info("Found {} accounts for user ID: {}", accounts.size(), userId);
            return accounts;
        } catch (Exception e) {
            logger.error("Error retrieving accounts for user ID: {}", userId, e);
            throw e;
        }
    }

    public BankAccount getAccountByUserIdAndAccountId(Long userId, Long accountId) {
        logger.debug("Retrieving account ID: {} for user ID: {}", accountId, userId);

        try {
            BankAccount account = bankAccountRepository.findByUserIdAndId(userId, accountId)
                    .orElseThrow(() -> {
                        logger.error("Account not found: {} for user: {}", accountId, userId);
                        return new AccountNotFoundException("Account not found with ID: " + accountId + " for user: " + userId);
                    });
            logger.debug("Account retrieved successfully - ID: {}, Account Number: {}, User ID: {}",
                    account.getId(), account.getAccountNumber(), account.getUserId());
            return account;
        } catch (Exception e) {
            logger.error("Error retrieving account ID: {} for user ID: {}", accountId, userId, e);
            throw e;
        }
    }

    // User-specific transaction methods
    @Transactional
    public void depositForUser(Long userId, Long accountId, BigDecimal amount) {
        logger.info("Processing deposit for user - User ID: {}, Account ID: {}, Amount: {}", userId, accountId, amount);

        validateAmount(amount);

        try {
            BankAccount account = bankAccountRepository.findByUserIdAndId(userId, accountId)
                    .orElseThrow(() -> {
                        logger.error("Deposit failed - Account not found: {} for user: {}", accountId, userId);
                        return new AccountNotFoundException("Account not found with ID: " + accountId + " for user: " + userId);
                    });

            BigDecimal oldBalance = account.getBalance();
            BigDecimal newBalance = oldBalance.add(amount);
            bankAccountRepository.updateBalance(accountId, newBalance);
            logger.info("Deposit successful for user - User ID: {}, Account ID: {}, Previous Balance: {}, New Balance: {}",
                    userId, accountId, oldBalance, newBalance);

        } catch (Exception e) {
            logger.error("Error processing deposit for user - User ID: {}, Account ID: {}, Amount: {}", userId, accountId, amount, e);
            throw e;
        }
    }

    @Transactional
    public void withdrawForUser(Long userId, Long accountId, BigDecimal amount) {
        logger.info("Processing withdrawal for user - User ID: {}, Account ID: {}, Amount: {}", userId, accountId, amount);

        validateAmount(amount);

        try {
            BankAccount account = bankAccountRepository.findByUserIdAndId(userId, accountId)
                    .orElseThrow(() -> {
                        logger.error("Withdrawal failed - Account not found: {} for user: {}", accountId, userId);
                        return new AccountNotFoundException("Account not found with ID: " + accountId + " for user: " + userId);
                    });

            BigDecimal currentBalance = account.getBalance();
            if (currentBalance.compareTo(amount) < 0) {
                logger.warn("Withdrawal failed - Insufficient balance. User ID: {}, Account ID: {}, Current Balance: {}, Withdrawal Amount: {}",
                        userId, accountId, currentBalance, amount);
                throw new InsufficientBalanceException("Insufficient balance for withdrawal");
            }

            BigDecimal newBalance = currentBalance.subtract(amount);
            bankAccountRepository.updateBalance(accountId, newBalance);

            logger.info("Withdrawal successful for user - User ID: {}, Account ID: {}, Previous Balance: {}, New Balance: {}",
                    userId, accountId, currentBalance, newBalance);
        } catch (Exception e) {
            logger.error("Error processing withdrawal for user - User ID: {}, Account ID: {}, Amount: {}", userId, accountId, amount, e);
            throw e;
        }
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.error("Invalid amount provided: {}", amount);
            throw new InvalidAmountException("Amount must be positive");
        }
        logger.debug("Amount validation passed: {}", amount);
    }

    //Gets the user's primary account. Currently returns the first account,
    //but can be enhanced to support primary account designation.
    public BankAccount getPrimaryAccountForUser(Long userId) {
        logger.debug("Getting primary account for user ID: {}", userId);

        try {
            List<BankAccount> accounts = getAccountsByUserId(userId);

            if (accounts.isEmpty()) {
                logger.error("No accounts found for user ID: {}", userId);
                throw new AccountNotFoundException("No accounts found for user ID: " + userId);
            }

            // For now, return the first account (oldest)
            // In the future, you could add a 'primary' flag to BankAccount model
            BankAccount primaryAccount = accounts.get(0);

            logger.debug("Primary account retrieved for user ID: {} - Account ID: {}, Account Number: {}",
                    userId, primaryAccount.getId(), primaryAccount.getAccountNumber());

            return primaryAccount;
        } catch (Exception e) {
            logger.error("Error getting primary account for user ID: {}", userId, e);
            throw e;
        }
    }

}