package com.example.BankingProject;

import com.example.BankingProject.controller.BankingController;
import com.example.BankingProject.dto.CreateAccountRequest;
import com.example.BankingProject.dto.DepositRequest;
import com.example.BankingProject.dto.TransferRequest;
import com.example.BankingProject.dto.WithdrawRequest;
import com.example.BankingProject.model.BankAccount;
import com.example.BankingProject.service.BankingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class BankingControllerTest {

    @Mock
    private BankingService bankingService;

    @InjectMocks
    private BankingController bankingController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bankingController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void getAccounts_WhenUserAuthenticated_ShouldReturnUserAccounts() throws Exception {
        // Arrange
        Long userId = 1L;
        BankAccount account1 = new BankAccount("John Doe", BigDecimal.valueOf(1000), userId);
        account1.setId(1L);
        account1.setAccountNumber("ACC1001");

        BankAccount account2 = new BankAccount("John Doe", BigDecimal.valueOf(2000), userId);
        account2.setId(2L);
        account2.setAccountNumber("ACC1002");

        List<BankAccount> userAccounts = Arrays.asList(account1, account2);

        when(bankingService.getAccountsByUserId(userId)).thenReturn(userAccounts);

        // Act & Assert
        mockMvc.perform(get("/accounts")
                        .requestAttr("userId", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].accountNumber").value("ACC1001"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].accountNumber").value("ACC1002"));

        verify(bankingService).getAccountsByUserId(userId);
        verify(bankingService, never()).getAllAccounts();
    }

    @Test
    void getAccounts_WhenUserNotAuthenticated_ShouldReturnAllAccounts() throws Exception {
        // Arrange
        BankAccount account1 = new BankAccount("John Doe", BigDecimal.valueOf(1000), 1L);
        account1.setId(1L);
        account1.setAccountNumber("ACC1001");

        BankAccount account2 = new BankAccount("Jane Smith", BigDecimal.valueOf(2000), 2L);
        account2.setId(2L);
        account2.setAccountNumber("ACC1002");

        List<BankAccount> allAccounts = Arrays.asList(account1, account2);

        when(bankingService.getAllAccounts()).thenReturn(allAccounts);

        // Act & Assert
        mockMvc.perform(get("/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(bankingService).getAllAccounts();
        verify(bankingService, never()).getAccountsByUserId(any());
    }

    @Test
    void transfer_WhenUserAuthenticated_ShouldTransferMoney() throws Exception {
        // Arrange
        Long userId = 1L;
        TransferRequest request = new TransferRequest();
        request.setSenderAccountNumber("ACC1001");
        request.setReceiverAccountNumber("ACC1002");
        request.setAmount(BigDecimal.valueOf(500));

        doNothing().when(bankingService).transferMoneyForUserByAccountNumber(
                userId, "ACC1001", "ACC1002", BigDecimal.valueOf(500));

        // Act & Assert
        mockMvc.perform(post("/transfer")
                        .requestAttr("userId", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Transfer successful from ACC1001 to ACC1002"));

        verify(bankingService).transferMoneyForUserByAccountNumber(
                userId, "ACC1001", "ACC1002", BigDecimal.valueOf(500));
    }

    @Test
    void transfer_WhenUserNotAuthenticated_ShouldReturnUnauthorized() throws Exception {
        // Arrange
        TransferRequest request = new TransferRequest();
        request.setReceiverAccountNumber("ACC1002");
        request.setAmount(BigDecimal.valueOf(500));

        // Act & Assert
        mockMvc.perform(post("/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Authentication required"));

        verify(bankingService, never()).transferMoneyForUserByAccountNumber(any(), any(), any(), any());
    }

    @Test
    void transfer_WhenReceiverAccountNumberEmpty_ShouldReturnBadRequest() throws Exception {
        // Arrange
        Long userId = 1L;
        TransferRequest request = new TransferRequest();
        request.setReceiverAccountNumber("");
        request.setAmount(BigDecimal.valueOf(500));

        // Act & Assert
        mockMvc.perform(post("/transfer")
                        .requestAttr("userId", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Receiver account number is required"));

        verify(bankingService, never()).transferMoneyForUserByAccountNumber(any(), any(), any(), any());
    }

    @Test
    void deposit_WhenUserAuthenticatedWithAccountId_ShouldDepositMoney() throws Exception {
        // Arrange
        Long userId = 1L;
        Long accountId = 1L;
        DepositRequest request = new DepositRequest();
        request.setAccountId(accountId);
        request.setAmount(BigDecimal.valueOf(500));

        BankAccount account = new BankAccount("John Doe", BigDecimal.valueOf(1000), userId);
        account.setId(accountId);
        account.setAccountNumber("ACC1001");

        when(bankingService.getAccountByUserIdAndAccountId(userId, accountId)).thenReturn(account);
        doNothing().when(bankingService).depositForUser(userId, accountId, BigDecimal.valueOf(500));

        // Act & Assert
        mockMvc.perform(post("/deposit")
                        .requestAttr("userId", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Deposit successful to account ACC1001"));

        verify(bankingService).getAccountByUserIdAndAccountId(userId, accountId);
        verify(bankingService).depositForUser(userId, accountId, BigDecimal.valueOf(500));
    }

    @Test
    void deposit_WhenUserAuthenticatedWithoutAccountId_ShouldDepositToPrimaryAccount() throws Exception {
        // Arrange
        Long userId = 1L;
        DepositRequest request = new DepositRequest();
        request.setAmount(BigDecimal.valueOf(500));

        BankAccount primaryAccount = new BankAccount("John Doe", BigDecimal.valueOf(1000), userId);
        primaryAccount.setId(1L);
        primaryAccount.setAccountNumber("ACC1001");

        when(bankingService.getPrimaryAccountForUser(userId)).thenReturn(primaryAccount);
        doNothing().when(bankingService).depositForUser(userId, 1L, BigDecimal.valueOf(500));

        // Act & Assert
        mockMvc.perform(post("/deposit")
                        .requestAttr("userId", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(bankingService).getPrimaryAccountForUser(userId);
        verify(bankingService).depositForUser(userId, 1L, BigDecimal.valueOf(500));
    }

    @Test
    void deposit_WhenUserNotAuthenticated_ShouldReturnUnauthorized() throws Exception {
        // Arrange
        DepositRequest request = new DepositRequest();
        request.setAmount(BigDecimal.valueOf(500));

        // Act & Assert
        mockMvc.perform(post("/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Authentication required"));

        verify(bankingService, never()).depositForUser(any(), any(), any());
    }

    @Test
    void withdraw_WhenUserAuthenticatedWithAccountId_ShouldWithdrawMoney() throws Exception {
        // Arrange
        Long userId = 1L;
        Long accountId = 1L;
        WithdrawRequest request = new WithdrawRequest();
        request.setAccountId(accountId);
        request.setAmount(BigDecimal.valueOf(500));

        BankAccount account = new BankAccount("John Doe", BigDecimal.valueOf(1000), userId);
        account.setId(accountId);
        account.setAccountNumber("ACC1001");

        when(bankingService.getAccountByUserIdAndAccountId(userId, accountId)).thenReturn(account);
        doNothing().when(bankingService).withdrawForUser(userId, accountId, BigDecimal.valueOf(500));

        // Act & Assert
        mockMvc.perform(post("/withdraw")
                        .requestAttr("userId", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Withdrawal successful from account ACC1001"));

        verify(bankingService).getAccountByUserIdAndAccountId(userId, accountId);
        verify(bankingService).withdrawForUser(userId, accountId, BigDecimal.valueOf(500));
    }

    @Test
    void withdraw_WhenUserAuthenticatedWithoutAccountId_ShouldWithdrawFromPrimaryAccount() throws Exception {
        // Arrange
        Long userId = 1L;
        WithdrawRequest request = new WithdrawRequest();
        request.setAmount(BigDecimal.valueOf(500));

        BankAccount primaryAccount = new BankAccount("John Doe", BigDecimal.valueOf(1000), userId);
        primaryAccount.setId(1L);
        primaryAccount.setAccountNumber("ACC1001");

        when(bankingService.getPrimaryAccountForUser(userId)).thenReturn(primaryAccount);
        doNothing().when(bankingService).withdrawForUser(userId, 1L, BigDecimal.valueOf(500));

        // Act & Assert
        mockMvc.perform(post("/withdraw")
                        .requestAttr("userId", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Withdrawal successful from account ACC1001"));

        verify(bankingService).getPrimaryAccountForUser(userId);
        verify(bankingService).withdrawForUser(userId, 1L, BigDecimal.valueOf(500));
    }

    @Test
    void withdraw_WhenUserNotAuthenticated_ShouldReturnUnauthorized() throws Exception {
        // Arrange
        WithdrawRequest request = new WithdrawRequest();
        request.setAmount(BigDecimal.valueOf(500));

        // Act & Assert
        mockMvc.perform(post("/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Authentication required"));

        verify(bankingService, never()).withdrawForUser(any(), any(), any());
    }

    @Test
    void getPrimaryAccount_WhenUserAuthenticated_ShouldReturnPrimaryAccount() throws Exception {
        // Arrange
        Long userId = 1L;
        BankAccount primaryAccount = new BankAccount("John Doe", BigDecimal.valueOf(1000), userId);
        primaryAccount.setId(1L);
        primaryAccount.setAccountNumber("ACC1001");

        when(bankingService.getPrimaryAccountForUser(userId)).thenReturn(primaryAccount);

        // Act & Assert
        mockMvc.perform(get("/accounts/primary")
                        .requestAttr("userId", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.accountNumber").value("ACC1001"))
                .andExpect(jsonPath("$.accountHolderName").value("John Doe"));

        verify(bankingService).getPrimaryAccountForUser(userId);
    }

    @Test
    void getPrimaryAccount_WhenUserNotAuthenticated_ShouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/accounts/primary"))
                .andExpect(status().isUnauthorized());

        verify(bankingService, never()).getPrimaryAccountForUser(any());
    }

    @Test
    void getCurrentUserAccounts_WhenUserAuthenticated_ShouldReturnUserAccounts() throws Exception {
        // Arrange
        Long userId = 1L;
        BankAccount account1 = new BankAccount("John Doe", BigDecimal.valueOf(1000), userId);
        account1.setId(1L);
        account1.setAccountNumber("ACC1001");

        List<BankAccount> userAccounts = Arrays.asList(account1);

        when(bankingService.getAccountsByUserId(userId)).thenReturn(userAccounts);

        // Act & Assert
        mockMvc.perform(get("/me")
                        .requestAttr("userId", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].accountNumber").value("ACC1001"));

        verify(bankingService).getAccountsByUserId(userId);
    }

    @Test
    void getCurrentUserAccounts_WhenUserNotAuthenticated_ShouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/me"))
                .andExpect(status().isUnauthorized());

        verify(bankingService, never()).getAccountsByUserId(any());
    }

    @Test
    void createAccount_WhenUserAuthenticated_ShouldCreateAccount() throws Exception {
        // Arrange
        Long userId = 1L;
        CreateAccountRequest request = new CreateAccountRequest();
        request.setAccountHolderName("John Doe");

        BankAccount newAccount = new BankAccount("John Doe", BigDecimal.ZERO, userId);
        newAccount.setId(1L);
        newAccount.setAccountNumber("ACC1001");

        when(bankingService.createAccount("John Doe", BigDecimal.ZERO, userId)).thenReturn(newAccount);

        // Act & Assert
        mockMvc.perform(post("/accounts")
                        .requestAttr("userId", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.accountNumber").value("ACC1001"))
                .andExpect(jsonPath("$.accountHolderName").value("John Doe"));

        verify(bankingService).createAccount("John Doe", BigDecimal.ZERO, userId);
    }

    @Test
    void createAccount_WhenUserNotAuthenticated_ShouldReturnUnauthorized() throws Exception {
        // Arrange
        CreateAccountRequest request = new CreateAccountRequest();
        request.setAccountHolderName("John Doe");

        // Act & Assert
        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(bankingService, never()).createAccount(any(), any(), any());
    }
}