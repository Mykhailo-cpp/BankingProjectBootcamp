
package com.example.BankingProject.controller;

import com.example.BankingProject.dto.LoginRequest;
import com.example.BankingProject.dto.LoginResponse;
import com.example.BankingProject.dto.RegisterRequest;
import com.example.BankingProject.dto.UserResponse;
import com.example.BankingProject.model.*;
import com.example.BankingProject.service.UserService;
import com.example.BankingProject.security.JwtUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class UserController {

    private static final Logger logger = LogManager.getLogger(UserController.class);
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        logger.info("UserController initialized successfully");
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {
        logger.info("POST /auth/register - Registering user: {} with password: {}",
                request.getUsername(), request.getPassword());

        try {
            User user;

            user = userService.registerUser(
                        request.getUsername(),
                        request.getPassword()
                );

            UserResponse userResponse = new UserResponse(user);

            logger.info("User registered successfully with auto-created account - ID: {}, Username: {}",
                    user.getId(), user.getUsername());

            return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
        } catch (Exception e) {
            logger.error("Error registering user: {}", request.getUsername(), e);
            throw e;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        logger.info("POST /auth/login - Login attempt for user: {}", request.getUsername(), request.getPassword());

        try {
            User user = userService.loginUser(request.getUsername(), request.getPassword());

            // Generate JWT token
            String token = jwtUtil.generateToken(user.getUsername(), user.getId());

            LoginResponse loginResponse = new LoginResponse(
                    token,
                    user.getUsername(),
                    user.getId(),
                    "Login successful"
            );

            logger.info("User logged in successfully - ID: {}, Username: {}, Token generated",
                    user.getId(), user.getUsername());

            return ResponseEntity.ok(loginResponse);
        } catch (Exception e) {
            logger.error("Error during login for user: {}", request.getUsername(), e);
            throw e;
        }
    }
}