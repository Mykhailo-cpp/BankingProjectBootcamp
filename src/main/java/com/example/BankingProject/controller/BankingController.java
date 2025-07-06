package com.example.BankingProject.controller;

import com.example.BankingProject.model.*;
import com.example.BankingProject.service.BankingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
public class BankingController {

    private final BankingService bankingService;

    public BankingController(BankingService bankingService) {
        this.bankingService = bankingService;
    }

    @PostMapping("/accounts")
    public ResponseEntity<BankAccount> createAccount(@RequestBody CreateAccountRequest request) {
        BankAccount account = bankingService.createAccount(
                request.getAccountHolderName(),
                request.getInitialBalance()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @PostMapping("/deposit")
    public ResponseEntity<String> deposit(@RequestBody DepositRequest request) {
        bankingService.deposit(request.getAccountId(), request.getAmount());
        return ResponseEntity.ok("Deposit successful");
    }

    @PostMapping("/withdraw")
    public ResponseEntity<String> withdraw(@RequestBody WithdrawRequest request) {
        bankingService.withdraw(request.getAccountId(), request.getAmount());
        return ResponseEntity.ok("Withdrawal successful");
    }

    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(@RequestBody TransferRequest request) {
        bankingService.transferMoney(
                request.getSenderAccountId(),
                request.getReceiverAccountId(),
                request.getAmount()
        );
        return ResponseEntity.ok("Transfer successful");
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<BankAccount>> getAllAccounts(@RequestParam(required = false) String name) {
        List<BankAccount> accounts;
        if (name != null && !name.trim().isEmpty()) {
            accounts = bankingService.findAccountsByName(name);
        } else {
            accounts = bankingService.getAllAccounts();
        }
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/accounts/{id}")
    public ResponseEntity<BankAccount> getAccountById(@PathVariable Long id) {
        BankAccount account = bankingService.getAccountById(id);
        return ResponseEntity.ok(account);
    }
}