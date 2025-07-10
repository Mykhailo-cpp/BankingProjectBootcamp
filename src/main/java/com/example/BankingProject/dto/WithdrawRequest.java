
package com.example.BankingProject.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public class WithdrawRequest {
    @JsonProperty("accountId")
    private Long accountId; // Optional - if not provided, uses user's first account

    @JsonProperty("amount")
    private BigDecimal amount;

    // Constructors
    public WithdrawRequest() {}

    public WithdrawRequest(Long accountId, BigDecimal amount) {
        this.accountId = accountId;
        this.amount = amount;
    }

    public WithdrawRequest(BigDecimal amount) {
        this.amount = amount;
    }

    // Getters and Setters
    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}