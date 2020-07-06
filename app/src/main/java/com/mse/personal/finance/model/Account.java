package com.mse.personal.finance.model;

/**
 * DTO for an account.
 *
 * @author D. Dimitrov
 */

public class Account {

	private Long id;
	private String name;
	private Double balance;
	private Double goal;
	private AccountType type;
	
	/*
	 * Constructors
	 */
	
	public Account() {}

	/*
	 * Getters & setters
	 */
	
	public Long getId() {
		return this.id;
	}

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

	public Account setId(Long id) {
		this.id = id;
		return this;
	}

	public Account setName(String name) {
		this.name = name;
		return this;
	}

	public Account setBalance(Double balance) {
		this.balance = balance;
		return this;
	}

	public Account setGoal(Double goal) {
		this.goal = goal;
		return this;
	}

	public Account setType(AccountType type) {
		this.type = type;
		return this;
	}
	
}