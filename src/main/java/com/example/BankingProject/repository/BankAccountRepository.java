
package com.example.BankingProject.repository;

import com.example.BankingProject.model.BankAccount;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BankAccountRepository extends CrudRepository<BankAccount, Long> {

    @Query("SELECT * FROM bank_accounts WHERE UPPER(account_holder_name) LIKE UPPER(CONCAT('%', :name, '%'))")
    List<BankAccount> findByAccountHolderNameContaining(@Param("name") String name);

    @Query("SELECT * FROM bank_accounts WHERE account_number = :accountNumber")
    Optional<BankAccount> findByAccountNumber(@Param("accountNumber") String accountNumber);

    @Modifying
    @Query("UPDATE bank_accounts SET balance = :balance, updated_at = CURRENT_TIMESTAMP WHERE id = :id")
    void updateBalance(@Param("id") Long id, @Param("balance") BigDecimal balance);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(account_number, 4) AS UNSIGNED)), 0) FROM bank_accounts WHERE account_number REGEXP '^ACC[0-9]+$'")
    Integer findMaxAccountNumber();

    // New methods for user-related queries
    @Query("SELECT * FROM bank_accounts WHERE user_id = :userId")
    List<BankAccount> findByUserId(@Param("userId") Long userId);

    @Query("SELECT * FROM bank_accounts WHERE user_id = :userId AND id = :accountId")
    Optional<BankAccount> findByUserIdAndId(@Param("userId") Long userId, @Param("accountId") Long accountId);

    /*@Query("SELECT COUNT(*) FROM bank_accounts WHERE user_id = :userId")
    int countByUserId(@Param("userId") Long userId);*/
}