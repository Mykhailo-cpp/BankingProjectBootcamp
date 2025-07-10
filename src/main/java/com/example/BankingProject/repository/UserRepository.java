package com.example.BankingProject.repository;

import com.example.BankingProject.model.User;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    @Query("SELECT * FROM users WHERE username = :username")
    Optional<User> findByUsername(@Param("username") String username);

    @Query("SELECT COUNT(*) > 0 FROM users WHERE username = :username")
    boolean existsByUsername(@Param("username") String username);
}
