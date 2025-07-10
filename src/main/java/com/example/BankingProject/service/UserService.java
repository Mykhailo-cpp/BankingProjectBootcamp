
package com.example.BankingProject.service;

import com.example.BankingProject.exception.InvalidCredentialsException;
import com.example.BankingProject.exception.UserAlreadyExistsException;
import com.example.BankingProject.exception.UserNotFoundException;
import com.example.BankingProject.model.BankAccount;
import com.example.BankingProject.model.User;
import com.example.BankingProject.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LogManager.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final BankingService bankingService;

    public UserService(UserRepository userRepository, BankingService bankingService) {
        this.userRepository = userRepository;
        this.bankingService = bankingService;
        this.passwordEncoder = new BCryptPasswordEncoder();
        logger.info("UserService initialized successfully");
    }

    @Transactional
    public User registerUser(String username, String password) {
        logger.info("Attempting to register user: {}", username);

        // Validate input
        if (username == null || username.trim().isEmpty()) {
            logger.error("Registration failed - Username is empty");
            throw new IllegalArgumentException("Username cannot be empty");
        }

        if (password == null || password.trim().isEmpty()) {
            logger.error("Registration failed - Password is empty");
            throw new IllegalArgumentException("Password cannot be empty");
        }

        // Check if user already exists
        if (userRepository.existsByUsername(username)) {
            logger.warn("Registration failed - User already exists: {}", username);
            throw new UserAlreadyExistsException("User with username '" + username + "' already exists");
        }

        try {
            // Hash password
            String hashedPassword = passwordEncoder.encode(password);

            // Create new user
            User user = new User(username, hashedPassword);
            User savedUser = userRepository.save(user);

            logger.info("User registered successfully - ID: {}, Username: {}",
                    savedUser.getId(), savedUser.getUsername());

            // Automatically create a bank account for the new user
            try {
                BankAccount account = bankingService.createAccount(
                        username, // Use username as account holder name
                        BigDecimal.ZERO, // Start with zero balance
                        savedUser.getId() // Link to the user
                );

                logger.info("Default bank account created for user - User ID: {}, Account ID: {}, Account Number: {}",
                        savedUser.getId(), account.getId(), account.getAccountNumber());
            } catch (Exception e) {
                logger.error("Failed to create default bank account for user ID: {}", savedUser.getId(), e);
                // You might want to decide whether to rollback the user creation or continue
                // For now, we'll log the error but continue with user registration
            }

            return savedUser;
        } catch (Exception e) {
            logger.error("Error registering user: {}", username, e);
            throw new RuntimeException("Failed to register user", e);
        }
    }

    public User loginUser(String username, String password) {
        logger.info("Attempting to login user: {}", username);

        // Validate input
        if (username == null || username.trim().isEmpty()) {
            logger.error("Login failed - Username is empty");
            throw new IllegalArgumentException("Username cannot be empty");
        }

        if (password == null || password.trim().isEmpty()) {
            logger.error("Login failed - Password is empty");
            throw new IllegalArgumentException("Password cannot be empty");
        }

        try {
            // Find user by username
            Optional<User> optionalUser = userRepository.findByUsername(username);

            if (optionalUser.isEmpty()) {
                logger.warn("Login failed - User not found: {}", username);
                throw new InvalidCredentialsException("Invalid username or password");
            }

            User user = optionalUser.get();

            // Verify password
            if (!passwordEncoder.matches(password, user.getPassword())) {
                logger.warn("Login failed - Invalid password for user: {}", username);
                throw new InvalidCredentialsException("Invalid username or password");
            }

            logger.info("User logged in successfully - ID: {}, Username: {}",
                    user.getId(), user.getUsername());

            return user;
        } catch (InvalidCredentialsException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error during login for user: {}", username, e);
            throw new RuntimeException("Login failed", e);
        }
    }
}