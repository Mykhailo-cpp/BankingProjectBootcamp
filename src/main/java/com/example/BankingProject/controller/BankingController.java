package com.example.BankingProject.controller;

import com.example.BankingProject.model.*;
import com.example.BankingProject.service.BankingService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
public class BankingController {

    private static final Logger logger = LogManager.getLogger(BankingController.class);

    private final BankingService bankingService;

    public BankingController(BankingService bankingService) {

        this.bankingService = bankingService;
        logger.info("BankingController initialized successfully");

    }

    @PostMapping("/accounts")
    public ResponseEntity<BankAccount> createAccount(@RequestBody CreateAccountRequest request) {
        logger.info("POST /accounts - Creating account for: {}", request.getAccountHolderName());

        try{
            BankAccount account = bankingService.createAccount(
                    request.getAccountHolderName(),
                    request.getInitialBalance()
            );

            logger.info("Account created successfully - ID: {}, Account Number: {}",
                    account.getId(), account.getAccountNumber());

            return ResponseEntity.status(HttpStatus.CREATED).body(account);
        }catch (Exception e){
            logger.error("Error creating account for: {}", request.getAccountHolderName(), e);
            throw e;
        }


    }

    @PostMapping("/deposit")
    public ResponseEntity<String> deposit(@RequestBody DepositRequest request) {
        logger.info("POST /deposit - Account ID: {}, Amount: {}", request.getAccountId(), request.getAmount());

        try{
            bankingService.deposit(request.getAccountId(), request.getAmount());

            logger.info("Deposit completed successfully for Account ID: {}", request.getAccountId());

            return ResponseEntity.ok("Deposit successful");
        }catch (Exception e){
            logger.error("Error processing deposit for Account ID: {}", request.getAccountId(), e);
            throw e;
        }

    }

    @PostMapping("/withdraw")
    public ResponseEntity<String> withdraw(@RequestBody WithdrawRequest request) {
        logger.info("POST /withdraw - Account ID: {}, Amount: {}", request.getAccountId(), request.getAmount());

        try{
            bankingService.withdraw(request.getAccountId(), request.getAmount());

            logger.info("Withdrawal completed successfully for Account ID: {}", request.getAccountId());

            return ResponseEntity.ok("Withdrawal successful");
        }catch (Exception e){
            logger.error("Error processing withdrawal for Account ID: {}", request.getAccountId(), e);
            throw e;
        }


    }

    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(@RequestBody TransferRequest request) {
        logger.info("POST /transfer - Sender ID: {}, Receiver ID: {}, Amount: {}",
                request.getSenderAccountId(), request.getReceiverAccountId(), request.getAmount());

        try{
            bankingService.transferMoney(
                    request.getSenderAccountId(),
                    request.getReceiverAccountId(),
                    request.getAmount()
            );

            logger.info("Transfer completed successfully - Sender ID: {}, Receiver ID: {}",
                    request.getSenderAccountId(), request.getReceiverAccountId());

            return ResponseEntity.ok("Transfer successful");
        }catch (Exception e){
            logger.error("Error processing transfer - Sender ID: {}, Receiver ID: {}",
                    request.getSenderAccountId(), request.getReceiverAccountId(), e);
            throw e;
        }


    }

    @GetMapping("/accounts")
    public ResponseEntity<List<BankAccount>> getAllAccounts() {
            logger.info("GET /accounts - Retrieving all accounts");

        try{
            List<BankAccount> accounts;
            accounts = bankingService.getAllAccounts();
            logger.info("Retrieved {} total accounts", accounts.size());
            return ResponseEntity.ok(accounts);
        }catch (Exception e){
            logger.error("Error retrieving accounts", e);
            throw e;
        }


    }

    @GetMapping("/accounts/{id}")
    public ResponseEntity<BankAccount> getAccountById(@PathVariable Long id) {
        logger.info("GET /accounts/{} - Retrieving account", id);

        try{
            BankAccount account = bankingService.getAccountById(id);
            logger.info("Account retrieved successfully - ID: {}, Account Number: {}",
                    account.getId(), account.getAccountNumber());
            return ResponseEntity.ok(account);
        }catch (Exception e){
            logger.error("Error retrieving account by ID: {}", id, e);
            throw e;
        }


    }
}