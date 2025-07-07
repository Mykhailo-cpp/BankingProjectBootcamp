package com.example.BankingProject;

import com.example.BankingProject.controller.GlobalExceptionHandler;
import com.example.BankingProject.exception.AccountNotFoundException;
import com.example.BankingProject.exception.InsufficientBalanceException;
import com.example.BankingProject.exception.InvalidAmountException;
import com.example.BankingProject.model.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        globalExceptionHandler = new GlobalExceptionHandler();
        when(webRequest.getDescription(false)).thenReturn("uri=/api/test");
    }

    @Test
    void testHandleAccountNotFound() {
        // Given
        String errorMessage = "Account not found with ID: 123";
        AccountNotFoundException exception = new AccountNotFoundException(errorMessage);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleAccountNotFound(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getBody().getError()).isEqualTo("Account Not Found");
        assertThat(response.getBody().getMessage()).isEqualTo(errorMessage);
        assertThat(response.getBody().getTimestamp()).isNotNull();
        assertThat(response.getBody().getPath()).isEqualTo("uri=/api/test");
    }

    @Test
    void testHandleInsufficientBalance() {
        // Given
        String errorMessage = "Insufficient balance for withdrawal";
        InsufficientBalanceException exception = new InsufficientBalanceException(errorMessage);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleInsufficientBalance(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getError()).isEqualTo("Insufficient Balance");
        assertThat(response.getBody().getMessage()).isEqualTo(errorMessage);
        assertThat(response.getBody().getTimestamp()).isNotNull();
        assertThat(response.getBody().getPath()).isEqualTo("uri=/api/test");
    }

    @Test
    void testHandleInvalidAmount() {
        // Given
        String errorMessage = "Amount must be positive";
        InvalidAmountException exception = new InvalidAmountException(errorMessage);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleInvalidAmount(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getError()).isEqualTo("Invalid Amount");
        assertThat(response.getBody().getMessage()).isEqualTo(errorMessage);
        assertThat(response.getBody().getTimestamp()).isNotNull();
        assertThat(response.getBody().getPath()).isEqualTo("uri=/api/test");
    }

    @Test
    void testHandleGenericException() {
        // Given
        String errorMessage = "Something went wrong";
        Exception exception = new Exception(errorMessage);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(response.getBody().getError()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred. Please try again later.");
        assertThat(response.getBody().getTimestamp()).isNotNull();
        assertThat(response.getBody().getPath()).isEqualTo("uri=/api/test");
    }

    @Test
    void testHandleGenericExceptionWithNullMessage() {
        // Given
        Exception exception = new Exception((String) null);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(response.getBody().getError()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred. Please try again later.");
        assertThat(response.getBody().getTimestamp()).isNotNull();
        assertThat(response.getBody().getPath()).isEqualTo("uri=/api/test");
    }

    @Test
    void testHandleAccountNotFoundWithNullMessage() {
        // Given
        AccountNotFoundException exception = new AccountNotFoundException(null);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleAccountNotFound(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getBody().getError()).isEqualTo("Account Not Found");
        assertThat(response.getBody().getMessage()).isNull();
        assertThat(response.getBody().getTimestamp()).isNotNull();
        assertThat(response.getBody().getPath()).isEqualTo("uri=/api/test");
    }

    @Test
    void testHandleInsufficientBalanceWithEmptyMessage() {
        // Given
        String errorMessage = "";
        InsufficientBalanceException exception = new InsufficientBalanceException(errorMessage);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleInsufficientBalance(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getError()).isEqualTo("Insufficient Balance");
        assertThat(response.getBody().getMessage()).isEqualTo(errorMessage);
        assertThat(response.getBody().getTimestamp()).isNotNull();
        assertThat(response.getBody().getPath()).isEqualTo("uri=/api/test");
    }

    @Test
    void testHandleInvalidAmountWithLongMessage() {
        // Given
        String errorMessage = "This is a very long error message that describes in detail why the amount is invalid and what the user should do to fix it";
        InvalidAmountException exception = new InvalidAmountException(errorMessage);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleInvalidAmount(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getError()).isEqualTo("Invalid Amount");
        assertThat(response.getBody().getMessage()).isEqualTo(errorMessage);
        assertThat(response.getBody().getTimestamp()).isNotNull();
        assertThat(response.getBody().getPath()).isEqualTo("uri=/api/test");
    }

    @Test
    void testTimestampIsRecentForAccountNotFoundException() {
        // Given
        AccountNotFoundException exception = new AccountNotFoundException("Test message");
        LocalDateTime beforeCall = LocalDateTime.now().minusSeconds(1);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleAccountNotFound(exception, webRequest);
        LocalDateTime afterCall = LocalDateTime.now().plusSeconds(1);

        // Then
        assertThat(response.getBody().getTimestamp()).isAfter(beforeCall);
        assertThat(response.getBody().getTimestamp()).isBefore(afterCall);
    }

    @Test
    void testWebRequestPathHandling() {
        // Given
        String customPath = "uri=/api/accounts/123";
        when(webRequest.getDescription(false)).thenReturn(customPath);
        AccountNotFoundException exception = new AccountNotFoundException("Account not found");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleAccountNotFound(exception, webRequest);

        // Then
        assertThat(response.getBody().getPath()).isEqualTo(customPath);
    }
}