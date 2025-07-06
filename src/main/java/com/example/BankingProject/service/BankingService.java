package com.example.BankingProject.service;

import com.example.BankingProject.exception.AccountNotFoundException;
import com.example.BankingProject.exception.InsufficientBalanceException;
import com.example.BankingProject.exception.InvalidAmountException;
import com.example.BankingProject.model.BankAccount;
import com.example.BankingProject.repository.BankAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BankingService {

    private final BankAccountRepository bankAccountRepository;
    private static final int ACCOUNT_NUMBER_START = 1000;

    public BankingService(BankAccountRepository bankAccountRepository) {
        this.bankAccountRepository = bankAccountRepository;
    }

    @Transactional
    public BankAccount createAccount(String accountHolderName, BigDecimal initialBalance) {
        if (initialBalance == null || initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidAmountException("Initial balance cannot be negative");
        }

        // Generate account number
        Integer maxAccountNumber = bankAccountRepository.findMaxAccountNumber();
        int nextAccountNumber = (maxAccountNumber != null) ? maxAccountNumber + 1 : ACCOUNT_NUMBER_START + 1;
        String accountNumber = "ACC" + nextAccountNumber;

        BankAccount account = new BankAccount(accountHolderName, initialBalance);
        account.setAccountNumber(accountNumber);

        return bankAccountRepository.save(account);
    }

    @Transactional
    public void deposit(Long accountId, BigDecimal amount) {
        validateAmount(amount);

        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with ID: " + accountId));

        BigDecimal newBalance = account.getBalance().add(amount);
        bankAccountRepository.updateBalance(accountId, newBalance);
    }

    @Transactional
    public void withdraw(Long accountId, BigDecimal amount) {
        validateAmount(amount);

        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with ID: " + accountId));

        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance for withdrawal");
        }

        BigDecimal newBalance = account.getBalance().subtract(amount);
        bankAccountRepository.updateBalance(accountId, newBalance);
    }

    @Transactional
    public void transferMoney(Long senderAccountId, Long receiverAccountId, BigDecimal amount) {
        validateAmount(amount);

        BankAccount senderAccount = bankAccountRepository.findById(senderAccountId)
                .orElseThrow(() -> new AccountNotFoundException("Sender account not found with ID: " + senderAccountId));

        BankAccount receiverAccount = bankAccountRepository.findById(receiverAccountId)
                .orElseThrow(() -> new AccountNotFoundException("Receiver account not found with ID: " + receiverAccountId));

        if (senderAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance for transfer");
        }

        BigDecimal senderNewBalance = senderAccount.getBalance().subtract(amount);
        BigDecimal receiverNewBalance = receiverAccount.getBalance().add(amount);

        bankAccountRepository.updateBalance(senderAccountId, senderNewBalance);
        bankAccountRepository.updateBalance(receiverAccountId, receiverNewBalance);
    }

    public BankAccount getAccountById(Long accountId) {
        return bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with ID: " + accountId));
    }

    public List<BankAccount> getAllAccounts() {
        return (List<BankAccount>) bankAccountRepository.findAll();
    }

    public List<BankAccount> findAccountsByName(String name) {
        return bankAccountRepository.findByAccountHolderNameContaining(name);
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Amount must be positive");
        }
    }
}