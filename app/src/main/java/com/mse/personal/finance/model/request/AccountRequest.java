package com.mse.personal.finance.model.request;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.mse.personal.finance.model.Account;
import com.mse.personal.finance.model.AccountType;

/**
 * Request DTO for an {@link Account} account in the system.
 *
 * @author D. Dimitrov
 * @author dvt32
 */
public class AccountRequest {

	@NotBlank(message = "Account name must not be null or blank!")
	@Size(max = 255)
	private String name;

	@NotNull(message = "Account balance must not be null!")
	private Double balance;

	private Double goal;

	@NotNull(message = "Account type must not be null!")
	@Enumerated(EnumType.STRING)
	private AccountType type;

	/*
	 * Getters & setters
	 */
	
	public String getName() {
		return this.name;
	}

	public Double getBalance() {
		return this.balance;
	}

	public Double getGoal() {
		return this.goal;
	}

	public AccountType getType() {
		return this.type;
	}

	public AccountRequest setName(String name) {
		this.name = name;
		return this;
	}

	public AccountRequest setBalance(Double balance) {
		this.balance = balance;
		return this;
	}

	public AccountRequest setGoal(Double goal) {
		this.goal = goal;
		return this;
	}

	public AccountRequest setType(AccountType type) {
		this.type = type;
		return this;
	}
	
}