package com.example.BankingProject;


import com.example.BankingProject.controller.GlobalExceptionHandler;
import com.example.BankingProject.exception.AccountNotFoundException;
import com.example.BankingProject.exception.InsufficientBalanceException;
import com.example.BankingProject.exception.InvalidAmountException;
import com.example.BankingProject.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleAccountNotFound() {
        // Given
        String errorMessage = "Account not found with ID: 123";
        AccountNotFoundException exception = new AccountNotFoundException(errorMessage);

        // When
        ResponseEntity<String> response = globalExceptionHandler.handleAccountNotFound(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo(errorMessage);
    }

    @Test
    void testHandleInsufficientBalance() {
        // Given
        String errorMessage = "Insufficient balance for withdrawal";
        InsufficientBalanceException exception = new InsufficientBalanceException(errorMessage);

        // When
        ResponseEntity<String> response = globalExceptionHandler.handleInsufficientBalance(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(errorMessage);
    }

    @Test
    void testHandleInvalidAmount() {
        // Given
        String errorMessage = "Amount must be positive";
        InvalidAmountException exception = new InvalidAmountException(errorMessage);

        // When
        ResponseEntity<String> response = globalExceptionHandler.handleInvalidAmount(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(errorMessage);
    }

    @Test
    void testHandleGenericException() {
        // Given
        String errorMessage = "Something went wrong";
        Exception exception = new Exception(errorMessage);

        // When
        ResponseEntity<String> response = globalExceptionHandler.handleGenericException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("An error occurred: " + errorMessage);
    }

    @Test
    void testHandleGenericExceptionWithNullMessage() {
        // Given
        Exception exception = new Exception((String) null);

        // When
        ResponseEntity<String> response = globalExceptionHandler.handleGenericException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("An error occurred: null");
    }

    @Test
    void testHandleAccountNotFoundWithNullMessage() {
        // Given
        AccountNotFoundException exception = new AccountNotFoundException(null);

        // When
        ResponseEntity<String> response = globalExceptionHandler.handleAccountNotFound(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void testHandleInsufficientBalanceWithEmptyMessage() {
        // Given
        String errorMessage = "";
        InsufficientBalanceException exception = new InsufficientBalanceException(errorMessage);

        // When
        ResponseEntity<String> response = globalExceptionHandler.handleInsufficientBalance(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(errorMessage);
    }

    @Test
    void testHandleInvalidAmountWithLongMessage() {
        // Given
        String errorMessage = "This is a very long error message that describes in detail why the amount is invalid and what the user should do to fix it";
        InvalidAmountException exception = new InvalidAmountException(errorMessage);

        // When
        ResponseEntity<String> response = globalExceptionHandler.handleInvalidAmount(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(errorMessage);
    }
}