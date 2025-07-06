package com.example.BankingProject;

import com.example.BankingProject.exception.AccountNotFoundException;
import com.example.BankingProject.exception.InsufficientBalanceException;
import com.example.BankingProject.exception.InvalidAmountException;
import com.example.BankingProject.model.BankAccount;
import com.example.BankingProject.repository.BankAccountRepository;
import com.example.BankingProject.service.BankingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankingServiceTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @InjectMocks
    private BankingService bankingService;

    private BankAccount testAccount;
    private BankAccount senderAccount;
    private BankAccount receiverAccount;

    @BeforeEach
    void setUp() {
        testAccount = new BankAccount("John Doe", new BigDecimal("1000.00"));
        testAccount.setId(1L);
        testAccount.setAccountNumber("ACC1001");

        senderAccount = new BankAccount("Alice Smith", new BigDecimal("500.00"));
        senderAccount.setId(2L);
        senderAccount.setAccountNumber("ACC1002");

        receiverAccount = new BankAccount("Bob Johnson", new BigDecimal("200.00"));
        receiverAccount.setId(3L);
        receiverAccount.setAccountNumber("ACC1003");
    }

    @Test
    void createAccount_WithValidData_ShouldReturnAccount() {
        // Arrange
        when(bankAccountRepository.findMaxAccountNumber()).thenReturn(1001);
        when(bankAccountRepository.save(any(BankAccount.class))).thenReturn(testAccount);

        // Act
        BankAccount result = bankingService.createAccount("John Doe", new BigDecimal("1000.00"));

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getAccountHolderName());
        assertEquals(new BigDecimal("1000.00"), result.getBalance());
        verify(bankAccountRepository).save(any(BankAccount.class));
    }

    @Test
    void createAccount_WithNegativeBalance_ShouldThrowException() {
        // Act & Assert
        assertThrows(InvalidAmountException.class, () ->
                bankingService.createAccount("John Doe", new BigDecimal("-100.00")));

        verify(bankAccountRepository, never()).save(any(BankAccount.class));
    }

    @Test
    void createAccount_WithNullBalance_ShouldThrowException() {
        // Act & Assert
        assertThrows(InvalidAmountException.class, () ->
                bankingService.createAccount("John Doe", null));

        verify(bankAccountRepository, never()).save(any(BankAccount.class));
    }

    @Test
    void createAccount_WithNoExistingAccounts_ShouldGenerateFirstAccountNumber() {
        // Arrange
        when(bankAccountRepository.findMaxAccountNumber()).thenReturn(null);
        when(bankAccountRepository.save(any(BankAccount.class))).thenReturn(testAccount);

        // Act
        bankingService.createAccount("John Doe", new BigDecimal("1000.00"));

        // Assert
        verify(bankAccountRepository).save(argThat(account ->
                account.getAccountNumber().equals("ACC1001")));
    }

    @Test
    void deposit_WithValidData_ShouldUpdateBalance() {
        // Arrange
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        // Act
        bankingService.deposit(1L, new BigDecimal("500.00"));

        // Assert
        verify(bankAccountRepository).updateBalance(1L, new BigDecimal("1500.00"));
    }

    @Test
    void deposit_WithNonExistentAccount_ShouldThrowException() {
        // Arrange
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AccountNotFoundException.class, () ->
                bankingService.deposit(1L, new BigDecimal("500.00")));

        verify(bankAccountRepository, never()).updateBalance(anyLong(), any(BigDecimal.class));
    }

    @Test
    void deposit_WithNegativeAmount_ShouldThrowException() {
        // Act & Assert
        assertThrows(InvalidAmountException.class, () ->
                bankingService.deposit(1L, new BigDecimal("-100.00")));

        verify(bankAccountRepository, never()).updateBalance(anyLong(), any(BigDecimal.class));
    }

    @Test
    void deposit_WithZeroAmount_ShouldThrowException() {
        // Act & Assert
        assertThrows(InvalidAmountException.class, () ->
                bankingService.deposit(1L, BigDecimal.ZERO));

        verify(bankAccountRepository, never()).updateBalance(anyLong(), any(BigDecimal.class));
    }

    @Test
    void withdraw_WithValidData_ShouldUpdateBalance() {
        // Arrange
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        // Act
        bankingService.withdraw(1L, new BigDecimal("300.00"));

        // Assert
        verify(bankAccountRepository).updateBalance(1L, new BigDecimal("700.00"));
    }

    @Test
    void withdraw_WithInsufficientBalance_ShouldThrowException() {
        // Arrange
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        // Act & Assert
        assertThrows(InsufficientBalanceException.class, () ->
                bankingService.withdraw(1L, new BigDecimal("1500.00")));

        verify(bankAccountRepository, never()).updateBalance(anyLong(), any(BigDecimal.class));
    }

    @Test
    void withdraw_WithNonExistentAccount_ShouldThrowException() {
        // Arrange
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AccountNotFoundException.class, () ->
                bankingService.withdraw(1L, new BigDecimal("100.00")));

        verify(bankAccountRepository, never()).updateBalance(anyLong(), any(BigDecimal.class));
    }

    @Test
    void transferMoney_WithValidData_ShouldUpdateBothBalances() {
        // Arrange
        when(bankAccountRepository.findById(2L)).thenReturn(Optional.of(senderAccount));
        when(bankAccountRepository.findById(3L)).thenReturn(Optional.of(receiverAccount));

        // Act
        bankingService.transferMoney(2L, 3L, new BigDecimal("200.00"));

        // Assert
        verify(bankAccountRepository).updateBalance(2L, new BigDecimal("300.00"));
        verify(bankAccountRepository).updateBalance(3L, new BigDecimal("400.00"));
    }

    @Test
    void transferMoney_WithInsufficientBalance_ShouldThrowException() {
        // Arrange
        when(bankAccountRepository.findById(2L)).thenReturn(Optional.of(senderAccount));
        when(bankAccountRepository.findById(3L)).thenReturn(Optional.of(receiverAccount));

        // Act & Assert
        assertThrows(InsufficientBalanceException.class, () ->
                bankingService.transferMoney(2L, 3L, new BigDecimal("600.00")));

        verify(bankAccountRepository, never()).updateBalance(anyLong(), any(BigDecimal.class));
    }

    @Test
    void transferMoney_WithNonExistentSenderAccount_ShouldThrowException() {
        // Arrange
        when(bankAccountRepository.findById(2L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AccountNotFoundException.class, () ->
                bankingService.transferMoney(2L, 3L, new BigDecimal("100.00")));

        verify(bankAccountRepository, never()).updateBalance(anyLong(), any(BigDecimal.class));
    }

    @Test
    void transferMoney_WithNonExistentReceiverAccount_ShouldThrowException() {
        // Arrange
        when(bankAccountRepository.findById(2L)).thenReturn(Optional.of(senderAccount));
        when(bankAccountRepository.findById(3L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AccountNotFoundException.class, () ->
                bankingService.transferMoney(2L, 3L, new BigDecimal("100.00")));

        verify(bankAccountRepository, never()).updateBalance(anyLong(), any(BigDecimal.class));
    }

    @Test
    void getAccountById_WithExistingAccount_ShouldReturnAccount() {
        // Arrange
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        // Act
        BankAccount result = bankingService.getAccountById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testAccount.getId(), result.getId());
        assertEquals(testAccount.getAccountHolderName(), result.getAccountHolderName());
    }

    @Test
    void getAccountById_WithNonExistentAccount_ShouldThrowException() {
        // Arrange
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AccountNotFoundException.class, () ->
                bankingService.getAccountById(1L));
    }

    @Test
    void getAllAccounts_ShouldReturnAllAccounts() {
        // Arrange
        List<BankAccount> accounts = Arrays.asList(testAccount, senderAccount, receiverAccount);
        when(bankAccountRepository.findAll()).thenReturn(accounts);

        // Act
        List<BankAccount> result = bankingService.getAllAccounts();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(bankAccountRepository).findAll();
    }

    @Test
    void findAccountsByName_WithExistingName_ShouldReturnMatchingAccounts() {
        // Arrange
        List<BankAccount> accounts = Arrays.asList(testAccount);
        when(bankAccountRepository.findByAccountHolderNameContaining("John")).thenReturn(accounts);

        // Act
        List<BankAccount> result = bankingService.findAccountsByName("John");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getAccountHolderName());
    }

    @Test
    void findAccountsByName_WithNonExistentName_ShouldReturnEmptyList() {
        // Arrange
        when(bankAccountRepository.findByAccountHolderNameContaining("NonExistent"))
                .thenReturn(Arrays.asList());

        // Act
        List<BankAccount> result = bankingService.findAccountsByName("NonExistent");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
