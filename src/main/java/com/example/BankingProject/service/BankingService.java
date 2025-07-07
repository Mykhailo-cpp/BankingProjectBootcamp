package com.example.BankingProject.service;

import com.example.BankingProject.exception.AccountNotFoundException;
import com.example.BankingProject.exception.InsufficientBalanceException;
import com.example.BankingProject.exception.InvalidAmountException;
import com.example.BankingProject.model.BankAccount;
import com.example.BankingProject.repository.BankAccountRepository;
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
    private static final int ACCOUNT_NUMBER_START = 1000;

    public BankingService(BankAccountRepository bankAccountRepository) {
        this.bankAccountRepository = bankAccountRepository;
        logger.info("BankingService initialized successfully");
    }

    @Transactional
    public BankAccount createAccount(String accountHolderName, BigDecimal initialBalance) {

        logger.info("Creating account for holder: {} with initial balance: {}", accountHolderName, initialBalance);

        if (initialBalance == null || initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            logger.error("Account creation failed - invalid initial balance: {}", initialBalance);
            throw new InvalidAmountException("Initial balance cannot be negative");
        }
        try{
            // Generate account number
            Integer maxAccountNumber = bankAccountRepository.findMaxAccountNumber();
            int nextAccountNumber = (maxAccountNumber != null) ? maxAccountNumber + 1 : ACCOUNT_NUMBER_START + 1;
            String accountNumber = "ACC" + nextAccountNumber;

            logger.debug("Generated account number: {}", accountNumber);

            BankAccount account = new BankAccount(accountHolderName, initialBalance);
            account.setAccountNumber(accountNumber);

            BankAccount savedAccount = bankAccountRepository.save(account);
            logger.info("Account created successfully - ID: {}, Account Number: {}, Holder: {}",
                    savedAccount.getId(), savedAccount.getAccountNumber(), savedAccount.getAccountHolderName());

            return savedAccount;
        }catch(Exception e){
            logger.error("Error creating account for holder: {}", accountHolderName, e);
            throw e;
        }

    }

    @Transactional
    public void deposit(Long accountId, BigDecimal amount) {

        logger.info("Processing deposit - Account ID: {}, Amount: {}", accountId, amount);

        validateAmount(amount);

        try{
            BankAccount account = bankAccountRepository.findById(accountId)
                    .orElseThrow(() -> {
                        logger.error("Deposit failed - Account not found: {}", accountId);
                        return new AccountNotFoundException("Account not found with ID: " + accountId);
                    });

            BigDecimal oldBalance = account.getBalance();
            BigDecimal newBalance = oldBalance.add(amount);
            bankAccountRepository.updateBalance(accountId, newBalance);
            logger.info("Deposit successful - Account ID: {}, Previous Balance: {}, New Balance: {}",
                    accountId, oldBalance, newBalance);

        }catch(Exception e){
            logger.error("Error processing deposit - Account ID: {}, Amount: {}", accountId, amount, e);
            throw e;
        }


    }

    @Transactional
    public void withdraw(Long accountId, BigDecimal amount) {

        logger.info("Processing withdrawal - Account ID: {}, Amount: {}", accountId, amount);

        validateAmount(amount);

        try{

            BankAccount account = bankAccountRepository.findById(accountId)
                    .orElseThrow(() -> {
                        logger.error("Withdrawal failed - Account not found: {}", accountId);
                        return new AccountNotFoundException("Account not found with ID: " + accountId);
                    });


            BigDecimal currentBalance = account.getBalance();
            if (currentBalance.compareTo(amount) < 0) {
                logger.warn("Withdrawal failed - Insufficient balance. Account ID: {}, Current Balance: {}, Withdrawal Amount: {}",
                        accountId, currentBalance, amount);
                throw new InsufficientBalanceException("Insufficient balance for withdrawal");
            }

            BigDecimal newBalance = currentBalance.subtract(amount);
            bankAccountRepository.updateBalance(accountId, newBalance);

            logger.info("Withdrawal successful - Account ID: {}, Previous Balance: {}, New Balance: {}",
                    accountId, currentBalance, newBalance);
        }catch (Exception e){
            logger.error("Error processing withdrawal - Account ID: {}, Amount: {}", accountId, amount, e);
            throw e;
        }


    }

    @Transactional
    public void transferMoney(Long senderAccountId, Long receiverAccountId, BigDecimal amount) {

        logger.info("Processing transfer - Sender ID: {}, Receiver ID: {}, Amount: {}",
                senderAccountId, receiverAccountId, amount);

        validateAmount(amount);

        try{

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

        }catch (Exception e){
            logger.error("Error processing transfer - Sender ID: {}, Receiver ID: {}, Amount: {}",
                    senderAccountId, receiverAccountId, amount, e);
            throw e;
        }


    }

    public BankAccount getAccountById(Long accountId) {
        logger.debug("Retrieving account by ID: {}", accountId);

        try{
           BankAccount account = bankAccountRepository.findById(accountId)
                    .orElseThrow(() -> {
                        logger.error("Account not found: {}", accountId);
                        return new AccountNotFoundException("Account not found with ID: " + accountId);
                    });
            logger.debug("Account retrieved successfully - ID: {}, Account Number: {}",
                    account.getId(), account.getAccountNumber());
            return account;
        }catch (Exception e){
            logger.error("Error retrieving account by ID: {}", accountId, e);
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

    public List<BankAccount> findAccountsByName(String name) {
        logger.debug("Searching accounts by name: {}", name);

        try {
            List<BankAccount> accounts = bankAccountRepository.findByAccountHolderNameContaining(name);
            logger.info("Found {} accounts matching name: {}", accounts.size(), name);
            return accounts;
        } catch (Exception e) {
            logger.error("Error searching accounts by name: {}", name, e);
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
}