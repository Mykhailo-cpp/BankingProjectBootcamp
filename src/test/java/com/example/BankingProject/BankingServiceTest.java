package com.example.BankingProject;

import com.example.BankingProject.exception.AccountNotFoundException;
import com.example.BankingProject.exception.InsufficientBalanceException;
import com.example.BankingProject.exception.InvalidAmountException;
import com.example.BankingProject.exception.UserNotFoundException;
import com.example.BankingProject.model.BankAccount;
import com.example.BankingProject.model.User;
import com.example.BankingProject.repository.BankAccountRepository;
import com.example.BankingProject.repository.UserRepository;
import com.example.BankingProject.service.BankingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankingServiceTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BankingService bankingService;

    private User testUser;
    private BankAccount testAccount;
    private BankAccount senderAccount;
    private BankAccount receiverAccount;

    @BeforeEach
    void setUp() {
        testUser = new User("TestUser", "123");
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testAccount = new BankAccount();
        testAccount.setId(1L);
        testAccount.setAccountNumber("ACC1001");
        testAccount.setAccountHolderName("John Doe");
        testAccount.setBalance(new BigDecimal("1000.00"));
        testAccount.setUserId(1L);

        senderAccount = new BankAccount();
        senderAccount.setId(1L);
        senderAccount.setAccountNumber("ACC1001");
        senderAccount.setAccountHolderName("John Doe");
        senderAccount.setBalance(new BigDecimal("1000.00"));
        senderAccount.setUserId(1L);

        receiverAccount = new BankAccount();
        receiverAccount.setId(2L);
        receiverAccount.setAccountNumber("ACC1002");
        receiverAccount.setAccountHolderName("Jane Smith");
        receiverAccount.setBalance(new BigDecimal("500.00"));
        receiverAccount.setUserId(2L);
    }

    // ==================== CREATE ACCOUNT TESTS ====================

    @Test
    void createAccount_Success_WithValidData() {
        // Arrange
        String holderName = "John Doe";
        BigDecimal initialBalance = new BigDecimal("1000.00");
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(bankAccountRepository.findMaxAccountNumber()).thenReturn(1000);
        when(bankAccountRepository.save(any(BankAccount.class))).thenReturn(testAccount);

        // Act
        BankAccount result = bankingService.createAccount(holderName, initialBalance, userId);

        // Assert
        assertNotNull(result);
        assertEquals(testAccount.getId(), result.getId());
        assertEquals(testAccount.getAccountNumber(), result.getAccountNumber());
        verify(userRepository).findById(userId);
        verify(bankAccountRepository).findMaxAccountNumber();
        verify(bankAccountRepository).save(any(BankAccount.class));
    }

    @Test
    void createAccount_Success_WithoutUserId() {
        // Arrange
        String holderName = "John Doe";
        BigDecimal initialBalance = new BigDecimal("1000.00");

        when(bankAccountRepository.findMaxAccountNumber()).thenReturn(1000);
        when(bankAccountRepository.save(any(BankAccount.class))).thenReturn(testAccount);

        // Act
        BankAccount result = bankingService.createAccount(holderName, initialBalance, null);

        // Assert
        assertNotNull(result);
        verify(userRepository, never()).findById(anyLong());
        verify(bankAccountRepository).save(any(BankAccount.class));
    }

    @Test
    void createAccount_Success_WithNullMaxAccountNumber() {
        // Arrange
        String holderName = "John Doe";
        BigDecimal initialBalance = new BigDecimal("1000.00");
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(bankAccountRepository.findMaxAccountNumber()).thenReturn(null);
        when(bankAccountRepository.save(any(BankAccount.class))).thenReturn(testAccount);

        // Act
        BankAccount result = bankingService.createAccount(holderName, initialBalance, userId);

        // Assert
        assertNotNull(result);
        verify(bankAccountRepository).save(any(BankAccount.class));
    }

    @Test
    void createAccount_ThrowsException_WithNegativeInitialBalance() {
        // Arrange
        String holderName = "John Doe";
        BigDecimal initialBalance = new BigDecimal("-100.00");
        Long userId = 1L;

        // Act & Assert
        InvalidAmountException exception = assertThrows(InvalidAmountException.class, () ->
                bankingService.createAccount(holderName, initialBalance, userId));
        assertEquals("Initial balance cannot be negative", exception.getMessage());
        verify(bankAccountRepository, never()).save(any(BankAccount.class));
    }

    @Test
    void createAccount_ThrowsException_WithNullInitialBalance() {
        // Arrange
        String holderName = "John Doe";
        Long userId = 1L;

        // Act & Assert
        InvalidAmountException exception = assertThrows(InvalidAmountException.class, () ->
                bankingService.createAccount(holderName, null, userId));
        assertEquals("Initial balance cannot be negative", exception.getMessage());
    }

    @Test
    void createAccount_ThrowsException_WithNonExistentUser() {
        // Arrange
        String holderName = "John Doe";
        BigDecimal initialBalance = new BigDecimal("1000.00");
        Long userId = 999L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () ->
                bankingService.createAccount(holderName, initialBalance, userId));
        assertEquals("User not found with ID: " + userId, exception.getMessage());
        verify(bankAccountRepository, never()).save(any(BankAccount.class));
    }

    // ==================== TRANSFER MONEY TESTS ====================

    @Test
    void transferMoney_Success_WithValidData() {
        // Arrange
        Long senderAccountId = 1L;
        Long receiverAccountId = 2L;
        BigDecimal amount = new BigDecimal("100.00");

        when(bankAccountRepository.findById(senderAccountId)).thenReturn(Optional.of(senderAccount));
        when(bankAccountRepository.findById(receiverAccountId)).thenReturn(Optional.of(receiverAccount));

        // Act
        bankingService.transferMoney(senderAccountId, receiverAccountId, amount);

        // Assert
        verify(bankAccountRepository).updateBalance(senderAccountId, new BigDecimal("900.00"));
        verify(bankAccountRepository).updateBalance(receiverAccountId, new BigDecimal("600.00"));
    }

    @Test
    void transferMoney_ThrowsException_WithInsufficientBalance() {
        // Arrange
        Long senderAccountId = 1L;
        Long receiverAccountId = 2L;
        BigDecimal amount = new BigDecimal("1500.00"); // More than sender's balance

        when(bankAccountRepository.findById(senderAccountId)).thenReturn(Optional.of(senderAccount));
        when(bankAccountRepository.findById(receiverAccountId)).thenReturn(Optional.of(receiverAccount));

        // Act & Assert
        InsufficientBalanceException exception = assertThrows(InsufficientBalanceException.class, () ->
                bankingService.transferMoney(senderAccountId, receiverAccountId, amount));
        assertEquals("Insufficient balance for transfer", exception.getMessage());
        verify(bankAccountRepository, never()).updateBalance(anyLong(), any(BigDecimal.class));
    }

    @Test
    void transferMoney_ThrowsException_WithNonExistentSenderAccount() {
        // Arrange
        Long senderAccountId = 999L;
        Long receiverAccountId = 2L;
        BigDecimal amount = new BigDecimal("100.00");

        when(bankAccountRepository.findById(senderAccountId)).thenReturn(Optional.empty());

        // Act & Assert
        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, () ->
                bankingService.transferMoney(senderAccountId, receiverAccountId, amount));
        assertEquals("Sender account not found with ID: " + senderAccountId, exception.getMessage());
    }

    @Test
    void transferMoney_ThrowsException_WithNonExistentReceiverAccount() {
        // Arrange
        Long senderAccountId = 1L;
        Long receiverAccountId = 999L;
        BigDecimal amount = new BigDecimal("100.00");

        when(bankAccountRepository.findById(senderAccountId)).thenReturn(Optional.of(senderAccount));
        when(bankAccountRepository.findById(receiverAccountId)).thenReturn(Optional.empty());

        // Act & Assert
        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, () ->
                bankingService.transferMoney(senderAccountId, receiverAccountId, amount));
        assertEquals("Receiver account not found with ID: " + receiverAccountId, exception.getMessage());
    }

    @Test
    void transferMoney_ThrowsException_WithInvalidAmount() {
        // Arrange
        Long senderAccountId = 1L;
        Long receiverAccountId = 2L;
        BigDecimal amount = new BigDecimal("-100.00");

        // Act & Assert
        InvalidAmountException exception = assertThrows(InvalidAmountException.class, () ->
                bankingService.transferMoney(senderAccountId, receiverAccountId, amount));
        assertEquals("Amount must be positive", exception.getMessage());
    }

    // ==================== TRANSFER BY ACCOUNT NUMBER TESTS ====================

    @Test
    void transferMoneyForUserByAccountNumber_Success_WithSenderAccountNumber() {
        // Arrange
        Long userId = 1L;
        String senderAccountNumber = "ACC1001";
        String receiverAccountNumber = "ACC1002";
        BigDecimal amount = new BigDecimal("100.00");

        when(bankAccountRepository.findByAccountNumber(senderAccountNumber)).thenReturn(Optional.of(senderAccount));
        when(bankAccountRepository.findByAccountNumber(receiverAccountNumber)).thenReturn(Optional.of(receiverAccount));
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(senderAccount));
        when(bankAccountRepository.findById(2L)).thenReturn(Optional.of(receiverAccount));

        // Act
        bankingService.transferMoneyForUserByAccountNumber(userId, senderAccountNumber, receiverAccountNumber, amount);

        // Assert
        verify(bankAccountRepository).updateBalance(1L, new BigDecimal("900.00"));
        verify(bankAccountRepository).updateBalance(2L, new BigDecimal("600.00"));
    }

    @Test
    void transferMoneyForUserByAccountNumber_Success_WithoutSenderAccountNumber() {
        // Arrange
        Long userId = 1L;
        String receiverAccountNumber = "ACC1002";
        BigDecimal amount = new BigDecimal("100.00");

        when(bankAccountRepository.findByAccountNumber(receiverAccountNumber)).thenReturn(Optional.of(receiverAccount));
        when(bankAccountRepository.findByUserId(userId)).thenReturn(Arrays.asList(senderAccount));
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(senderAccount));
        when(bankAccountRepository.findById(2L)).thenReturn(Optional.of(receiverAccount));

        // Act
        bankingService.transferMoneyForUserByAccountNumber(userId, null, receiverAccountNumber, amount);

        // Assert
        verify(bankAccountRepository).updateBalance(1L, new BigDecimal("900.00"));
        verify(bankAccountRepository).updateBalance(2L, new BigDecimal("600.00"));
    }

    @Test
    void transferMoneyForUserByAccountNumber_ThrowsException_WithUnauthorizedSenderAccount() {
        // Arrange
        Long userId = 1L;
        String senderAccountNumber = "ACC1001";
        String receiverAccountNumber = "ACC1002";
        BigDecimal amount = new BigDecimal("100.00");

        BankAccount unauthorizedSenderAccount = new BankAccount();
        unauthorizedSenderAccount.setId(1L);
        unauthorizedSenderAccount.setAccountNumber("ACC1001");
        unauthorizedSenderAccount.setUserId(999L); // Different user

        when(bankAccountRepository.findByAccountNumber(senderAccountNumber)).thenReturn(Optional.of(unauthorizedSenderAccount));

        // Act & Assert
        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, () ->
                bankingService.transferMoneyForUserByAccountNumber(userId, senderAccountNumber, receiverAccountNumber, amount));
        assertEquals("Sender account does not belong to the authenticated user", exception.getMessage());
    }

    @Test
    void transferMoneyForUserByAccountNumber_ThrowsException_WithNonExistentReceiverAccount() {
        // Arrange
        Long userId = 1L;
        String senderAccountNumber = "ACC1001";
        String receiverAccountNumber = "ACC9999";
        BigDecimal amount = new BigDecimal("100.00");

        when(bankAccountRepository.findByAccountNumber(senderAccountNumber)).thenReturn(Optional.of(senderAccount));
        when(bankAccountRepository.findByAccountNumber(receiverAccountNumber)).thenReturn(Optional.empty());

        // Act & Assert
        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, () ->
                bankingService.transferMoneyForUserByAccountNumber(userId, senderAccountNumber, receiverAccountNumber, amount));
        assertEquals("Receiver account not found with account number: " + receiverAccountNumber, exception.getMessage());
    }

    // ==================== GET ACCOUNTS TESTS ====================

    @Test
    void getAllAccounts_Success() {
        // Arrange
        List<BankAccount> accounts = Arrays.asList(senderAccount, receiverAccount);
        when(bankAccountRepository.findAll()).thenReturn(accounts);

        // Act
        List<BankAccount> result = bankingService.getAllAccounts();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(bankAccountRepository).findAll();
    }

    @Test
    void getAccountsByUserId_Success() {
        // Arrange
        Long userId = 1L;
        List<BankAccount> accounts = Arrays.asList(senderAccount);
        when(bankAccountRepository.findByUserId(userId)).thenReturn(accounts);

        // Act
        List<BankAccount> result = bankingService.getAccountsByUserId(userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(senderAccount.getId(), result.get(0).getId());
        verify(bankAccountRepository).findByUserId(userId);
    }

    @Test
    void getAccountByUserIdAndAccountId_Success() {
        // Arrange
        Long userId = 1L;
        Long accountId = 1L;
        when(bankAccountRepository.findByUserIdAndId(userId, accountId)).thenReturn(Optional.of(senderAccount));

        // Act
        BankAccount result = bankingService.getAccountByUserIdAndAccountId(userId, accountId);

        // Assert
        assertNotNull(result);
        assertEquals(senderAccount.getId(), result.getId());
        verify(bankAccountRepository).findByUserIdAndId(userId, accountId);
    }

    @Test
    void getAccountByUserIdAndAccountId_ThrowsException_WithNonExistentAccount() {
        // Arrange
        Long userId = 1L;
        Long accountId = 999L;
        when(bankAccountRepository.findByUserIdAndId(userId, accountId)).thenReturn(Optional.empty());

        // Act & Assert
        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, () ->
                bankingService.getAccountByUserIdAndAccountId(userId, accountId));
        assertEquals("Account not found with ID: " + accountId + " for user: " + userId, exception.getMessage());
    }

    // ==================== DEPOSIT TESTS ====================

    @Test
    void depositForUser_Success() {
        // Arrange
        Long userId = 1L;
        Long accountId = 1L;
        BigDecimal amount = new BigDecimal("100.00");
        when(bankAccountRepository.findByUserIdAndId(userId, accountId)).thenReturn(Optional.of(senderAccount));

        // Act
        bankingService.depositForUser(userId, accountId, amount);

        // Assert
        verify(bankAccountRepository).updateBalance(accountId, new BigDecimal("1100.00"));
    }

    @Test
    void depositForUser_ThrowsException_WithNonExistentAccount() {
        // Arrange
        Long userId = 1L;
        Long accountId = 999L;
        BigDecimal amount = new BigDecimal("100.00");
        when(bankAccountRepository.findByUserIdAndId(userId, accountId)).thenReturn(Optional.empty());

        // Act & Assert
        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, () ->
                bankingService.depositForUser(userId, accountId, amount));
        assertEquals("Account not found with ID: " + accountId + " for user: " + userId, exception.getMessage());
    }

    @Test
    void depositForUser_ThrowsException_WithInvalidAmount() {
        // Arrange
        Long userId = 1L;
        Long accountId = 1L;
        BigDecimal amount = BigDecimal.ZERO;

        // Act & Assert
        InvalidAmountException exception = assertThrows(InvalidAmountException.class, () ->
                bankingService.depositForUser(userId, accountId, amount));
        assertEquals("Amount must be positive", exception.getMessage());
    }

    // ==================== WITHDRAW TESTS ====================

    @Test
    void withdrawForUser_Success() {
        // Arrange
        Long userId = 1L;
        Long accountId = 1L;
        BigDecimal amount = new BigDecimal("100.00");
        when(bankAccountRepository.findByUserIdAndId(userId, accountId)).thenReturn(Optional.of(senderAccount));

        // Act
        bankingService.withdrawForUser(userId, accountId, amount);

        // Assert
        verify(bankAccountRepository).updateBalance(accountId, new BigDecimal("900.00"));
    }

    @Test
    void withdrawForUser_ThrowsException_WithInsufficientBalance() {
        // Arrange
        Long userId = 1L;
        Long accountId = 1L;
        BigDecimal amount = new BigDecimal("1500.00");
        when(bankAccountRepository.findByUserIdAndId(userId, accountId)).thenReturn(Optional.of(senderAccount));

        // Act & Assert
        InsufficientBalanceException exception = assertThrows(InsufficientBalanceException.class, () ->
                bankingService.withdrawForUser(userId, accountId, amount));
        assertEquals("Insufficient balance for withdrawal", exception.getMessage());
        verify(bankAccountRepository, never()).updateBalance(anyLong(), any(BigDecimal.class));
    }

    @Test
    void withdrawForUser_ThrowsException_WithNonExistentAccount() {
        // Arrange
        Long userId = 1L;
        Long accountId = 999L;
        BigDecimal amount = new BigDecimal("100.00");
        when(bankAccountRepository.findByUserIdAndId(userId, accountId)).thenReturn(Optional.empty());

        // Act & Assert
        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, () ->
                bankingService.withdrawForUser(userId, accountId, amount));
        assertEquals("Account not found with ID: " + accountId + " for user: " + userId, exception.getMessage());
    }

    // ==================== PRIMARY ACCOUNT TESTS ====================

    @Test
    void getPrimaryAccountForUser_Success() {
        // Arrange
        Long userId = 1L;
        List<BankAccount> accounts = Arrays.asList(senderAccount, receiverAccount);
        when(bankAccountRepository.findByUserId(userId)).thenReturn(accounts);

        // Act
        BankAccount result = bankingService.getPrimaryAccountForUser(userId);

        // Assert
        assertNotNull(result);
        assertEquals(senderAccount.getId(), result.getId());
        verify(bankAccountRepository).findByUserId(userId);
    }

    @Test
    void getPrimaryAccountForUser_ThrowsException_WithNoAccounts() {
        // Arrange
        Long userId = 1L;
        when(bankAccountRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        // Act & Assert
        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, () ->
                bankingService.getPrimaryAccountForUser(userId));
        assertEquals("No accounts found for user ID: " + userId, exception.getMessage());
    }

    // ==================== VALIDATE AMOUNT TESTS ====================

    @Test
    void validateAmount_ThrowsException_WithNullAmount() {
        // Act & Assert
        InvalidAmountException exception = assertThrows(InvalidAmountException.class, () ->
                bankingService.depositForUser(1L, 1L, null));
        assertEquals("Amount must be positive", exception.getMessage());
    }

    @Test
    void validateAmount_ThrowsException_WithZeroAmount() {
        // Act & Assert
        InvalidAmountException exception = assertThrows(InvalidAmountException.class, () ->
                bankingService.depositForUser(1L, 1L, BigDecimal.ZERO));
        assertEquals("Amount must be positive", exception.getMessage());
    }

    @Test
    void validateAmount_ThrowsException_WithNegativeAmount() {
        // Act & Assert
        InvalidAmountException exception = assertThrows(InvalidAmountException.class, () ->
                bankingService.depositForUser(1L, 1L, new BigDecimal("-100.00")));
        assertEquals("Amount must be positive", exception.getMessage());
    }
}