package com.example.BankingProject;

import com.example.BankingProject.controller.GlobalExceptionHandler;
import com.example.BankingProject.exception.*;
import com.example.BankingProject.dto.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private WebRequest webRequest;

    private static final String REQUEST_DESCRIPTION = "uri=/api/test";

    @BeforeEach
    void setUp() {
        when(webRequest.getDescription(false)).thenReturn(REQUEST_DESCRIPTION);
    }

    @Test
    void handleAccountNotFound_ShouldReturnNotFoundStatus() {
        // Given
        String errorMessage = "Account with ID 123 not found";
        AccountNotFoundException exception = new AccountNotFoundException(errorMessage);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleAccountNotFound(exception, webRequest);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(HttpStatus.NOT_FOUND.value(), errorResponse.getStatus());
        assertEquals("Account Not Found", errorResponse.getError());
        assertEquals(errorMessage, errorResponse.getMessage());
        assertEquals(REQUEST_DESCRIPTION, errorResponse.getPath());
        assertNotNull(errorResponse.getTimestamp());
        assertTrue(errorResponse.getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void handleInsufficientBalance_ShouldReturnBadRequestStatus() {
        // Given
        String errorMessage = "Insufficient balance. Available: $100, Required: $150";
        InsufficientBalanceException exception = new InsufficientBalanceException(errorMessage);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleInsufficientBalance(exception, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.getStatus());
        assertEquals("Insufficient Balance", errorResponse.getError());
        assertEquals(errorMessage, errorResponse.getMessage());
        assertEquals(REQUEST_DESCRIPTION, errorResponse.getPath());
        assertNotNull(errorResponse.getTimestamp());
    }

    @Test
    void handleInvalidAmount_ShouldReturnBadRequestStatus() {
        // Given
        String errorMessage = "Amount must be greater than zero";
        InvalidAmountException exception = new InvalidAmountException(errorMessage);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleInvalidAmount(exception, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.getStatus());
        assertEquals("Invalid Amount", errorResponse.getError());
        assertEquals(errorMessage, errorResponse.getMessage());
        assertEquals(REQUEST_DESCRIPTION, errorResponse.getPath());
        assertNotNull(errorResponse.getTimestamp());
    }

    @Test
    void handleUserAlreadyExists_ShouldReturnConflictStatus() {
        // Given
        String errorMessage = "User with email test@example.com already exists";
        UserAlreadyExistsException exception = new UserAlreadyExistsException(errorMessage);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleUserAlreadyExists(exception, webRequest);

        // Then
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());

        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(HttpStatus.CONFLICT.value(), errorResponse.getStatus());
        assertEquals("User Already Exists", errorResponse.getError());
        assertEquals(errorMessage, errorResponse.getMessage());
        assertEquals(REQUEST_DESCRIPTION, errorResponse.getPath());
        assertNotNull(errorResponse.getTimestamp());
    }

    @Test
    void handleInvalidCredentials_ShouldReturnUnauthorizedStatus() {
        // Given
        String errorMessage = "Invalid username or password";
        InvalidCredentialsException exception = new InvalidCredentialsException(errorMessage);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleInvalidCredentials(exception, webRequest);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), errorResponse.getStatus());
        assertEquals("Invalid Credentials", errorResponse.getError());
        assertEquals(errorMessage, errorResponse.getMessage());
        assertEquals(REQUEST_DESCRIPTION, errorResponse.getPath());
        assertNotNull(errorResponse.getTimestamp());
    }

    @Test
    void handleUserNotFound_ShouldReturnNotFoundStatus() {
        // Given
        String errorMessage = "User with ID 456 not found";
        UserNotFoundException exception = new UserNotFoundException(errorMessage);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleUserNotFound(exception, webRequest);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(HttpStatus.NOT_FOUND.value(), errorResponse.getStatus());
        assertEquals("User Not Found", errorResponse.getError());
        assertEquals(errorMessage, errorResponse.getMessage());
        assertEquals(REQUEST_DESCRIPTION, errorResponse.getPath());
        assertNotNull(errorResponse.getTimestamp());
    }

    @Test
    void handleIllegalArgument_ShouldReturnBadRequestStatus() {
        // Given
        String errorMessage = "Invalid input parameter";
        IllegalArgumentException exception = new IllegalArgumentException(errorMessage);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleIllegalArgument(exception, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.getStatus());
        assertEquals("Invalid Input", errorResponse.getError());
        assertEquals(errorMessage, errorResponse.getMessage());
        assertEquals(REQUEST_DESCRIPTION, errorResponse.getPath());
        assertNotNull(errorResponse.getTimestamp());
    }

    @Test
    void handleGenericException_ShouldReturnInternalServerErrorStatus() {
        // Given
        String errorMessage = "Database connection failed";
        Exception exception = new RuntimeException(errorMessage);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorResponse.getStatus());
        assertEquals("Internal Server Error", errorResponse.getError());
        assertEquals("An unexpected error occurred. Please try again later.", errorResponse.getMessage());
        assertEquals(REQUEST_DESCRIPTION, errorResponse.getPath());
        assertNotNull(errorResponse.getTimestamp());
    }

    @Test
    void handleGenericException_ShouldNotExposeInternalErrorMessage() {
        // Given
        String internalErrorMessage = "SQL constraint violation: foreign key constraint failed";
        Exception exception = new RuntimeException(internalErrorMessage);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(exception, webRequest);

        // Then
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        // Should not expose the internal error message to client
        assertNotEquals(internalErrorMessage, errorResponse.getMessage());
        assertEquals("An unexpected error occurred. Please try again later.", errorResponse.getMessage());
    }

    @Test
    void errorResponseTimestamp_ShouldBeRecent() {
        // Given
        LocalDateTime beforeExecution = LocalDateTime.now().minusSeconds(1);
        AccountNotFoundException exception = new AccountNotFoundException("Test error");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleAccountNotFound(exception, webRequest);
        LocalDateTime afterExecution = LocalDateTime.now().plusSeconds(1);

        // Then
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertTrue(errorResponse.getTimestamp().isAfter(beforeExecution));
        assertTrue(errorResponse.getTimestamp().isBefore(afterExecution));
    }

    @Test
    void allExceptionHandlers_ShouldSetCorrectPath() {
        // Given
        String customPath = "uri=/api/custom/endpoint";
        when(webRequest.getDescription(false)).thenReturn(customPath);

        // When & Then - Test a few different handlers
        ResponseEntity<ErrorResponse> response1 = globalExceptionHandler.handleAccountNotFound(
                new AccountNotFoundException("Test"), webRequest);
        assertEquals(customPath, response1.getBody().getPath());

        ResponseEntity<ErrorResponse> response2 = globalExceptionHandler.handleInvalidCredentials(
                new InvalidCredentialsException("Test"), webRequest);
        assertEquals(customPath, response2.getBody().getPath());

        ResponseEntity<ErrorResponse> response3 = globalExceptionHandler.handleGenericException(
                new RuntimeException("Test"), webRequest);
        assertEquals(customPath, response3.getBody().getPath());
    }
}