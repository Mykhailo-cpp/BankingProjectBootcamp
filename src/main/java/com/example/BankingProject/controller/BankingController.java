package com.example.BankingProject.controller;

import com.example.BankingProject.dto.CreateAccountRequest;
import com.example.BankingProject.dto.DepositRequest;
import com.example.BankingProject.dto.TransferRequest;
import com.example.BankingProject.dto.WithdrawRequest;
import com.example.BankingProject.model.*;
import com.example.BankingProject.service.BankingService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.util.List;

@RestController
public class BankingController {

    private static final Logger logger = LogManager.getLogger(BankingController.class);
    private final BankingService bankingService;

    public BankingController(BankingService bankingService) {
        this.bankingService = bankingService;
        logger.info("BankingController initialized successfully");
    }

    // Get accounts - accessible without authentication (returns all accounts)
    // If user is authenticated, returns only their accounts
    @GetMapping("/accounts")
    public ResponseEntity<List<BankAccount>> getAccounts(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        if (userId != null) {
            // User is authenticated, return only their accounts
            logger.info("GET /accounts - Authenticated user {} retrieving their accounts", userId);
            try {
                List<BankAccount> accounts = bankingService.getAccountsByUserId(userId);
                logger.info("Retrieved {} accounts for user ID: {}", accounts.size(), userId);
                return ResponseEntity.ok(accounts);
            } catch (Exception e) {
                logger.error("Error retrieving accounts for user ID: {}", userId, e);
                throw e;
            }
        } else {
            // User is not authenticated, return all accounts
            logger.info("GET /accounts - Unauthenticated access, retrieving all accounts");
            try {
                List<BankAccount> accounts = bankingService.getAllAccounts();
                logger.info("Retrieved {} total accounts", accounts.size());
                return ResponseEntity.ok(accounts);
            } catch (Exception e) {
                logger.error("Error retrieving all accounts", e);
                throw e;
            }
        }
    }
    //Transfer money - requires authentication
    //Requires receiver account number and amount
    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(@RequestBody TransferRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");

        if (userId == null) {
            logger.warn("POST /transfer - Unauthorized access attempt");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
        }

        if (request.getReceiverAccountNumber() == null || request.getReceiverAccountNumber().trim().isEmpty()) {
            logger.error("Transfer failed - Receiver account number is required");
            return ResponseEntity.badRequest().body("Receiver account number is required");
        }

        logger.info("POST /transfer - User {} transferring to account {}, amount: {}",
                userId, request.getReceiverAccountNumber(), request.getAmount());

        try {
            bankingService.transferMoneyForUserByAccountNumber(
                    userId,
                    request.getSenderAccountNumber(),
                    request.getReceiverAccountNumber(),
                    request.getAmount()
            );

            String senderAccountInfo = request.getSenderAccountNumber() != null ?
                    request.getSenderAccountNumber() : "primary account";

            logger.info("Transfer completed successfully - User ID: {}, From: {}, To: {}",
                    userId, senderAccountInfo, request.getReceiverAccountNumber());

            return ResponseEntity.ok("Transfer successful from " + senderAccountInfo + " to " + request.getReceiverAccountNumber());
        } catch (Exception e) {
            logger.error("Error processing transfer - User ID: {}, Receiver Account: {}",
                    userId, request.getReceiverAccountNumber(), e);
            throw e;
        }
    }

    // Deposit money - requires authentication
    // AccountId is optional, if not provided, uses user's primary account
    @PostMapping("/deposit")
    public ResponseEntity<String> deposit(@RequestBody DepositRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");

        if (userId == null) {
            logger.warn("POST /deposit - Unauthorized access attempt");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
        }

        logger.info("POST /deposit - User {} depositing amount: {}", userId, request.getAmount());

        try {
            // Determine target account
            BankAccount targetAccount;
            if (request.getAccountId() != null) {
                // User specified account, verify it belongs to them
                targetAccount = bankingService.getAccountByUserIdAndAccountId(userId, request.getAccountId());
                logger.info("Deposit to specified account ID: {}", request.getAccountId());
            } else {
                // Use the primary account
                targetAccount = bankingService.getPrimaryAccountForUser(userId);
                logger.info("Deposit to primary account ID: {}", targetAccount.getId());
            }

            bankingService.depositForUser(userId, targetAccount.getId(), request.getAmount());
            logger.info("Deposit completed successfully for User ID: {}, Account ID: {}", userId, targetAccount.getId());
            return ResponseEntity.ok("Deposit successful to account " + targetAccount.getAccountNumber());
        } catch (Exception e) {
            logger.error("Error processing deposit for User ID: {}", userId, e);
            throw e;
        }
    }

    // Withdraw money - requires authentication
    // AccountId is optional, if not provided, uses user's primary account
    @PostMapping("/withdraw")
    public ResponseEntity<String> withdraw(@RequestBody WithdrawRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");

        if (userId == null) {
            logger.warn("POST /withdraw - Unauthorized access attempt");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
        }

        logger.info("POST /withdraw - User {} withdrawing amount: {}", userId, request.getAmount());

        try {
            // Determine source account
            BankAccount sourceAccount;
            if (request.getAccountId() != null) {
                // User specified account, verify it belongs to them
                sourceAccount = bankingService.getAccountByUserIdAndAccountId(userId, request.getAccountId());
                logger.info("Withdrawal from specified account ID: {}", request.getAccountId());
            } else {
                // Use the primary account
                sourceAccount = bankingService.getPrimaryAccountForUser(userId);
                logger.info("Withdrawal from primary account ID: {}", sourceAccount.getId());
            }

            bankingService.withdrawForUser(userId, sourceAccount.getId(), request.getAmount());
            logger.info("Withdrawal completed successfully for User ID: {}, Account ID: {}", userId, sourceAccount.getId());
            return ResponseEntity.ok("Withdrawal successful from account " + sourceAccount.getAccountNumber());
        } catch (Exception e) {
            logger.error("Error processing withdrawal for User ID: {}", userId, e);
            throw e;
        }
    }

    // Get user's primary account - requires authentication
    @GetMapping("/accounts/primary")
    public ResponseEntity<BankAccount> getPrimaryAccount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        if (userId == null) {
            logger.warn("GET /accounts/primary - Unauthorized access attempt");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        logger.info("GET /accounts/primary - User {} retrieving primary account", userId);

        try {
            BankAccount account = bankingService.getPrimaryAccountForUser(userId);
            logger.info("Primary account retrieved successfully - ID: {}, Account Number: {}, User ID: {}",
                    account.getId(), account.getAccountNumber(), account.getUserId());
            return ResponseEntity.ok(account);
        } catch (Exception e) {
            logger.error("Error retrieving primary account for user ID: {}", userId, e);
            throw e;
        }
    }
    // Get current user's account information - requires authentication
    @GetMapping("/me")
    public ResponseEntity<List<BankAccount>> getCurrentUserAccounts(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        if (userId == null) {
            logger.warn("GET /me - Unauthorized access attempt");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        logger.info("GET /me - User {} retrieving their account information", userId);

        try {
            List<BankAccount> accounts = bankingService.getAccountsByUserId(userId);
            logger.info("Retrieved {} accounts for user ID: {} via /me endpoint", accounts.size(), userId);
            return ResponseEntity.ok(accounts);
        } catch (Exception e) {
            logger.error("Error retrieving account information for user ID: {} via /me endpoint", userId, e);
            throw e;
        }
    }

    // Create account for authenticated user
    @PostMapping("/accounts")
    public ResponseEntity<BankAccount> createAccount(@RequestBody CreateAccountRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");

        if (userId == null) {
            logger.warn("POST /accounts - Unauthorized access attempt");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        logger.info("POST /accounts - User {} creating new account with holder name: {}",
                userId, request.getAccountHolderName());

        try {
            // Force the userId to be the authenticated user (security measure)
            // Set initial balance to 0 for new accounts
            BankAccount account = bankingService.createAccount(
                    request.getAccountHolderName(),
                    BigDecimal.ZERO,
                    userId
            );

            logger.info("Account created successfully for user {} - Account ID: {}, Account Number: {} with zero balance",
                    userId, account.getId(), account.getAccountNumber());

            return ResponseEntity.status(HttpStatus.CREATED).body(account);
        } catch (Exception e) {
            logger.error("Error creating account for user ID: {}", userId, e);
            throw e;
        }
    }
}