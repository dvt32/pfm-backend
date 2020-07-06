package com.mse.personal.finance.model;

import java.util.Date;

/**
 * DTO for a transaction.
 *
 * @author D. Dimitrov
 */

public class Transaction {

	private Long id;
	private Date dateOfCompletion;
	private Long fromId;
	private Long toId;
	private TransactionFromType fromType;
	private TransactionToType toType;
	private Double sum;
	private String recurring;
	private String description;
	private Boolean shouldBeAutomaticallyExecuted;

	/*
	 * Getters & setters
	 */
	
	public Long getId() {
		return this.id;
	}

	public Date getDateOfCompletion() {
		return this.dateOfCompletion;
	}

	public Long getFromId() {
		return this.fromId;
	}

	public Long getToId() {
		return this.toId;
	}

	public TransactionFromType getFromType() {
		return this.fromType;
	}

	public TransactionToType getToType() {
		return this.toType;
	}

	public Double getSum() {
		return this.sum;
	}

	public String getRecurring() {
		return this.recurring;
	}

	public String getDescription() {
		return this.description;
	}

	public Boolean getShouldBeAutomaticallyExecuted() {
		return this.shouldBeAutomaticallyExecuted;
	}

	public Transaction setId(Long id) {
		this.id = id;
		return this;
	}

	public Transaction setDateOfCompletion(Date dateOfCompletion) {
		this.dateOfCompletion = dateOfCompletion;
		return this;
	}

	public Transaction setFromId(Long fromId) {
		this.fromId = fromId;
		return this;
	}

	public Transaction setToId(Long toId) {
		this.toId = toId;
		return this;
	}

	public Transaction setFromType(TransactionFromType fromType) {
		this.fromType = fromType;
		return this;
	}

	public Transaction setToType(TransactionToType toType) {
		this.toType = toType;
		return this;
	}

	public Transaction setSum(Double sum) {
		this.sum = sum;
		return this;
	}

	public Transaction setRecurring(String recurring) {
		this.recurring = recurring;
		return this;
	}

	public Transaction setDescription(String description) {
		this.description = description;
		return this;
	}

	public Transaction setShouldBeAutomaticallyExecuted(Boolean shouldBeAutomaticallyExecuted) {
		this.shouldBeAutomaticallyExecuted = shouldBeAutomaticallyExecuted;
		return this;
	}
	
}