package com.example.BankingProject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BankingProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(BankingProjectApplication.class, args);
	}

}
/*
POST /accounts - Create new account
POST /deposit - Deposit money
POST /withdraw - Withdraw money
POST /transfer - Transfer money between accounts
GET /accounts - Get all accounts (with optional name filter)
GET /accounts/{id} - Get account by ID
*/
