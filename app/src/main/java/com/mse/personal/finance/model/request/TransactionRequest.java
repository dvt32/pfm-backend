package com.mse.personal.finance.model.request;

import java.util.Date;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.mse.personal.finance.model.Transaction;
import com.mse.personal.finance.model.TransactionFromType;
import com.mse.personal.finance.model.TransactionToType;

/**
 * Request DTO for a {@link Transaction} transaction in the system.
 * 
 * Note about transaction types:
 * - From Category to Account = Income
 * - From Account to Category = Expense
 * - From Account to Account = Transfer
 *
 * @author D. Dimitrov
 * @author dvt32
 */
public class TransactionRequest {

	@Temporal(TemporalType.DATE)
	@NotNull(message = "Transaction date of completion must not be null!")
	private Date dateOfCompletion;

	@NotNull(message = "Transaction from-ID must not be null!")
	private Long fromId;

	@NotNull(message = "Transaction to-ID must not be null!")
	private Long toId;

	@NotNull(message = "Transaction from-type must not be null!")
	@Enumerated(EnumType.STRING)
	private TransactionFromType fromType;

	@NotNull(message = "Transaction to-type must not be null")
	@Enumerated(EnumType.STRING)
	private TransactionToType toType;

	@NotNull(message = "Transaction sum must not be null!")
	private Double sum;

	@Size(max=65532)
	private String recurring;

	private String description;

	@NotNull(message = "Must specify if transaction should be automatically executed!")
	private Boolean shouldBeAutomaticallyExecuted;

	/*
	 * Getters & setters
	 */
	
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

	public void setDateOfCompletion(Date dateOfCompletion) {
		this.dateOfCompletion = dateOfCompletion;
	}

	public void setFromId(Long fromId) {
		this.fromId = fromId;
	}

	public void setToId(Long toId) {
		this.toId = toId;
	}

	public void setFromType(TransactionFromType fromType) {
		this.fromType = fromType;
	}

	public void setToType(TransactionToType toType) {
		this.toType = toType;
	}

	public void setSum(Double sum) {
		this.sum = sum;
	}

	public void setRecurring(String recurring) {
		this.recurring = recurring;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setShouldBeAutomaticallyExecuted(Boolean shouldBeAutomaticallyExecuted) {
		this.shouldBeAutomaticallyExecuted = shouldBeAutomaticallyExecuted;
	}
	
}