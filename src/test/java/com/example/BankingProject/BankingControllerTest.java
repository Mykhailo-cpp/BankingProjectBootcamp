package com.example.BankingProject;

import com.example.BankingProject.controller.BankingController;
import com.example.BankingProject.exception.AccountNotFoundException;
import com.example.BankingProject.exception.InsufficientBalanceException;
import com.example.BankingProject.exception.InvalidAmountException;
import com.example.BankingProject.model.*;
import com.example.BankingProject.service.BankingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BankingController.class)
class BankingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BankingService bankingService;

    @Autowired
    private ObjectMapper objectMapper;

    private BankAccount testAccount;
    private CreateAccountRequest createAccountRequest;
    private DepositRequest depositRequest;
    private WithdrawRequest withdrawRequest;
    private TransferRequest transferRequest;

    @BeforeEach
    void setUp() {
        testAccount = new BankAccount("John Doe", new BigDecimal("1000.00"));
        testAccount.setId(1L);
        testAccount.setAccountNumber("ACC1001");

        createAccountRequest = new CreateAccountRequest();
        createAccountRequest.setAccountHolderName("John Doe");
        createAccountRequest.setInitialBalance(new BigDecimal("1000.00"));

        depositRequest = new DepositRequest();
        depositRequest.setAccountId(1L);
        depositRequest.setAmount(new BigDecimal("500.00"));

        withdrawRequest = new WithdrawRequest();
        withdrawRequest.setAccountId(1L);
        withdrawRequest.setAmount(new BigDecimal("300.00"));

        transferRequest = new TransferRequest();
        transferRequest.setSenderAccountId(1L);
        transferRequest.setReceiverAccountId(2L);
        transferRequest.setAmount(new BigDecimal("200.00"));
    }

    @Test
    void createAccount_WithValidRequest_ShouldReturnCreatedAccount() throws Exception {
        // Arrange
        when(bankingService.createAccount(anyString(), any(BigDecimal.class)))
                .thenReturn(testAccount);

        // Act & Assert
        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createAccountRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.accountHolderName").value("John Doe"))
                .andExpect(jsonPath("$.balance").value(1000.00))
                .andExpect(jsonPath("$.accountNumber").value("ACC1001"));

        verify(bankingService).createAccount("John Doe", new BigDecimal("1000.00"));
    }

    @Test
    void createAccount_WithInvalidAmount_ShouldReturnBadRequest() throws Exception {
        // Arrange
        when(bankingService.createAccount(anyString(), any(BigDecimal.class)))
                .thenThrow(new InvalidAmountException("Initial balance cannot be negative"));

        // Act & Assert
        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createAccountRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Initial balance cannot be negative"));
    }

    @Test
    void deposit_WithValidRequest_ShouldReturnSuccessMessage() throws Exception {
        // Arrange
        doNothing().when(bankingService).deposit(anyLong(), any(BigDecimal.class));

        // Act & Assert
        mockMvc.perform(post("/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depositRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Deposit successful"));

        verify(bankingService).deposit(1L, new BigDecimal("500.00"));
    }

    @Test
    void deposit_WithNonExistentAccount_ShouldReturnNotFound() throws Exception {
        // Arrange
        doThrow(new AccountNotFoundException("Account not found with ID: 1"))
                .when(bankingService).deposit(anyLong(), any(BigDecimal.class));

        // Act & Assert
        mockMvc.perform(post("/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depositRequest)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Account not found with ID: 1"));
    }

    @Test
    void deposit_WithInvalidAmount_ShouldReturnBadRequest() throws Exception {
        // Arrange
        doThrow(new InvalidAmountException("Amount must be positive"))
                .when(bankingService).deposit(anyLong(), any(BigDecimal.class));

        // Act & Assert
        mockMvc.perform(post("/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depositRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Amount must be positive"));
    }

    @Test
    void withdraw_WithValidRequest_ShouldReturnSuccessMessage() throws Exception {
        // Arrange
        doNothing().when(bankingService).withdraw(anyLong(), any(BigDecimal.class));

        // Act & Assert
        mockMvc.perform(post("/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withdrawRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Withdrawal successful"));

        verify(bankingService).withdraw(1L, new BigDecimal("300.00"));
    }

    @Test
    void withdraw_WithInsufficientBalance_ShouldReturnBadRequest() throws Exception {
        // Arrange
        doThrow(new InsufficientBalanceException("Insufficient balance for withdrawal"))
                .when(bankingService).withdraw(anyLong(), any(BigDecimal.class));

        // Act & Assert
        mockMvc.perform(post("/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withdrawRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Insufficient balance for withdrawal"));
    }

    @Test
    void transfer_WithValidRequest_ShouldReturnSuccessMessage() throws Exception {
        // Arrange
        doNothing().when(bankingService).transferMoney(anyLong(), anyLong(), any(BigDecimal.class));

        // Act & Assert
        mockMvc.perform(post("/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Transfer successful"));

        verify(bankingService).transferMoney(1L, 2L, new BigDecimal("200.00"));
    }

    @Test
    void transfer_WithInsufficientBalance_ShouldReturnBadRequest() throws Exception {
        // Arrange
        doThrow(new InsufficientBalanceException("Insufficient balance for transfer"))
                .when(bankingService).transferMoney(anyLong(), anyLong(), any(BigDecimal.class));

        // Act & Assert
        mockMvc.perform(post("/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Insufficient balance for transfer"));
    }

    @Test
    void getAllAccounts_WithoutNameFilter_ShouldReturnAllAccounts() throws Exception {
        // Arrange
        List<BankAccount> accounts = Arrays.asList(testAccount);
        when(bankingService.getAllAccounts()).thenReturn(accounts);

        // Act & Assert
        mockMvc.perform(get("/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].accountHolderName").value("John Doe"));

        verify(bankingService).getAllAccounts();
    }

    @Test
    void getAllAccounts_WithNameFilter_ShouldReturnFilteredAccounts() throws Exception {
        // Arrange
        List<BankAccount> accounts = Arrays.asList(testAccount);
        when(bankingService.findAccountsByName("John")).thenReturn(accounts);

        // Act & Assert
        mockMvc.perform(get("/accounts")
                        .param("name", "John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].accountHolderName").value("John Doe"));

        verify(bankingService).findAccountsByName("John");
    }

    @Test
    void getAllAccounts_WithEmptyNameFilter_ShouldReturnAllAccounts() throws Exception {
        // Arrange
        List<BankAccount> accounts = Arrays.asList(testAccount);
        when(bankingService.getAllAccounts()).thenReturn(accounts);

        // Act & Assert
        mockMvc.perform(get("/accounts")
                        .param("name", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(bankingService).getAllAccounts();
    }

    @Test
    void getAccountById_WithExistingAccount_ShouldReturnAccount() throws Exception {
        // Arrange
        when(bankingService.getAccountById(1L)).thenReturn(testAccount);

        // Act & Assert
        mockMvc.perform(get("/accounts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.accountHolderName").value("John Doe"));

        verify(bankingService).getAccountById(1L);
    }

    @Test
    void getAccountById_WithNonExistentAccount_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(bankingService.getAccountById(1L))
                .thenThrow(new AccountNotFoundException("Account not found with ID: 1"));

        // Act & Assert
        mockMvc.perform(get("/accounts/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Account not found with ID: 1"));
    }

    @Test
    void handleGenericException_ShouldReturnInternalServerError() throws Exception {
        // Arrange
        when(bankingService.getAccountById(1L))
                .thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        mockMvc.perform(get("/accounts/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("An error occurred: Database connection failed"));
    }
}
