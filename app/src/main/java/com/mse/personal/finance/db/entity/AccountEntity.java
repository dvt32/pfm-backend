package com.mse.personal.finance.db.entity;

import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.mse.personal.finance.model.AccountType;

/**
 * Persistence entity for a user balance account in the finance system.
 * 
 * Contains the needed information for user balance accounts.
 *
 * @author D. Dimitrov
 * @author dvt32
 */
@Entity
@Table(name = "accounts")
public class AccountEntity 
	extends BaseEntity 
{

	@Column(name = "name", nullable = false, length = 256)
	@NotBlank(message = "Account name must not be null or blank!")
	private String name;

	@Column(name = "balance", nullable = false)
	@NotNull(message = "Account balance must not be null!")
	private Double balance;

	@Column(name = "goal")
	private Double goal;

	@Column(name = "type", nullable = false)
	@Enumerated(EnumType.STRING)
	@NotNull(message = "Account type must not be null!")
	private AccountType type;

	@ManyToOne
	@JoinColumn(name = "owner_id")
	private UserEntity owner;

	@ManyToMany(
		fetch = FetchType.LAZY,
		cascade = {
				CascadeType.PERSIST,
				CascadeType.MERGE
		},
		mappedBy = "sharedAccounts"
	)
	private List<UserEntity> sharedUsers;

	/*
	 * Constructors
	 */
	
	public AccountEntity() {}
	
	public AccountEntity(
		String name,
		Double balance, 
		Double goal,
		AccountType type, 
		UserEntity owner,
		List<UserEntity> sharedUsers) 
	{
		this.name = name;
		this.balance = balance;
		this.goal = goal;
		this.type = type;
		this.owner = owner;
		this.sharedUsers = sharedUsers;
	}
	
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

	public UserEntity getOwner() {
		return this.owner;
	}

	public List<UserEntity> getSharedUsers() {
		return this.sharedUsers;
	}

	public AccountEntity setName(String name) {
		this.name = name;
		return this;
	}

	public AccountEntity setBalance(Double balance) {
		this.balance = balance;
		return this;
	}

	public AccountEntity setGoal(Double goal) {
		this.goal = goal;
		return this;
	}

	public AccountEntity setType(AccountType type) {
		this.type = type;
		return this;
	}

	public AccountEntity setOwner(UserEntity owner) {
		this.owner = owner;
		return this;
	}

	public AccountEntity setSharedUsers(List<UserEntity> sharedUsers) {
		this.sharedUsers = sharedUsers;
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
		AccountEntity that = (AccountEntity) o;
		return Objects.equals(owner, that.owner) &&
				Objects.equals(name, that.name) &&
				Objects.equals(balance, that.balance) &&
				Objects.equals(goal, that.goal) &&
				Objects.equals(type, that.type);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), owner, name, balance, goal, type);
	}

	public String toString() {
		return "AccountEntity(name=" + this.getName() + ", balance=" + this.getBalance() + ", goal=" + this.getGoal() + ", type=" + this.getType() + ", owner=" + this.getOwner() + ")";
	}
	
}