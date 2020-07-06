package com.mse.personal.finance.db.entity;

import java.util.Date;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import com.mse.personal.finance.model.TransactionFromType;
import com.mse.personal.finance.model.TransactionToType;

/**
 * Persistence entity for user transactions in the finance system.
 * Contains the needed information for user transactions.
 *
 * @author D. Dimitrov
 * @author dvt32
 */
@Entity
@Table(name = "transactions")
public class TransactionEntity 
	extends BaseEntity 
{

	@Column(name = "date_of_completion", nullable = false)
	@NotNull(message = "Transaction date of completion must not be null!")
	@Temporal(TemporalType.DATE)
	private Date dateOfCompletion;

	@Column(name = "from_id", nullable = false)
	@NotNull(message = "Transaction from-ID must not be null!")
	private Long fromId;

	@Column(name = "to_id", nullable = false)
	@NotNull(message = "Transaction to-ID must not be null!")
	private Long toId;

	@Column(name = "from_type")
	@Enumerated(EnumType.STRING)
	@NotNull(message = "Transaction from-type must not be null!")
	private TransactionFromType fromType;

	@Column(name = "to_type")
	@Enumerated(EnumType.STRING)
	@NotNull(message = "Transaction to-type must not be null")
	private TransactionToType toType;

	@Column(name = "sum", nullable = false)
	@NotNull(message = "Transaction sum must not be null!")
	private Double sum;

	@Column(name = "recurring")
	private String recurring;

	@Column(name = "description")
	private String description;

	@Column(name = "should_be_automatically_executed")
	@NotNull(message = "Must specify if transaction should be automatically executed!")
	private Boolean shouldBeAutomaticallyExecuted;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private UserEntity user;

	/*
	 * Constructors
	 */
	
	public TransactionEntity() {}
	
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

	public UserEntity getUser() {
		return this.user;
	}

	public TransactionEntity setDateOfCompletion(Date dateOfCompletion) {
		this.dateOfCompletion = dateOfCompletion;
		return this;
	}

	public TransactionEntity setFromId(Long fromId) {
		this.fromId = fromId;
		return this;
	}

	public TransactionEntity setToId(Long toId) {
		this.toId = toId;
		return this;
	}

	public TransactionEntity setFromType(TransactionFromType fromType) {
		this.fromType = fromType;
		return this;
	}

	public TransactionEntity setToType(TransactionToType toType) {
		this.toType = toType;
		return this;
	}

	public TransactionEntity setSum(Double sum) {
		this.sum = sum;
		return this;
	}

	public TransactionEntity setRecurring(String recurring) {
		this.recurring = recurring;
		return this;
	}

	public TransactionEntity setDescription(String description) {
		this.description = description;
		return this;
	}

	public TransactionEntity setShouldBeAutomaticallyExecuted(Boolean shouldBeAutomaticallyExecuted) {
		this.shouldBeAutomaticallyExecuted = shouldBeAutomaticallyExecuted;
		return this;
	}

	public TransactionEntity setUser(UserEntity user) {
		this.user = user;
		return this;
	}
	
	/*
	 * Other methods
	 */
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		TransactionEntity that = (TransactionEntity) o;
		return Objects.equals(user, that.user) &&
				Objects.equals(dateOfCompletion, that.dateOfCompletion) &&
				Objects.equals(fromId, that.fromId) &&
				Objects.equals(toId, that.toId) &&
				Objects.equals(fromType, that.fromType) &&
				Objects.equals(toType, that.toType) &&
				Objects.equals(sum, that.sum) &&
				Objects.equals(recurring, that.recurring) &&
				Objects.equals(description, that.description) &&
				Objects.equals(shouldBeAutomaticallyExecuted, that.shouldBeAutomaticallyExecuted);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), user, fromId, toId, fromType, toType, sum, recurring, description, shouldBeAutomaticallyExecuted);
	}

	public String toString() {
		return "TransactionEntity(dateOfCompletion=" + this.getDateOfCompletion() + ", fromId=" + this.getFromId() + ", toId=" + this.getToId() + ", fromType=" + this.getFromType() + ", toType=" + this.getToType() + ", sum=" + this.getSum() + ", recurring=" + this.getRecurring() + ", description=" + this.getDescription() + ", shouldBeAutomaticallyExecuted=" + this.getShouldBeAutomaticallyExecuted() + ", user=" + this.getUser() + ")";
	}
	
}