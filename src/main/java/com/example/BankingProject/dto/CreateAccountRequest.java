package com.example.BankingProject.dto;

public class CreateAccountRequest {
    private String accountHolderName;

    public CreateAccountRequest() {}

    public CreateAccountRequest(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }
}