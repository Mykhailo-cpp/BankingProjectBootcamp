
package com.example.BankingProject.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public class TransferRequest {
    @JsonProperty("senderAccountNumber")
    private String senderAccountNumber; // Optional - if not provided, uses user's primary account

    @JsonProperty("receiverAccountNumber")
    private String receiverAccountNumber; // Required

    @JsonProperty("amount")
    private BigDecimal amount;

    // Constructors
    public TransferRequest() {}

    public TransferRequest(String senderAccountNumber, String receiverAccountNumber, BigDecimal amount) {
        this.senderAccountNumber = senderAccountNumber;
        this.receiverAccountNumber = receiverAccountNumber;
        this.amount = amount;
    }

    public TransferRequest(String receiverAccountNumber, BigDecimal amount) {
        this.receiverAccountNumber = receiverAccountNumber;
        this.amount = amount;
    }

    // Getters and Setters
    public String getSenderAccountNumber() {
        return senderAccountNumber;
    }

    public void setSenderAccountNumber(String senderAccountNumber) {
        this.senderAccountNumber = senderAccountNumber;
    }

    public String getReceiverAccountNumber() {
        return receiverAccountNumber;
    }

    public void setReceiverAccountNumber(String receiverAccountNumber) {
        this.receiverAccountNumber = receiverAccountNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}