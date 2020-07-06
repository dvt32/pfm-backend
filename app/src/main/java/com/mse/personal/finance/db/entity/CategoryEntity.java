package com.mse.personal.finance.db.entity;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.mse.personal.finance.model.CategoryType;

/**
 * Persistence entity for user categories in the finance system.
 * Contains the needed information for user categories.
 *
 * @author D. Dimitrov
 * @author dvt32
 */
@Entity
@Table(name = "categories")
public class CategoryEntity 
	extends BaseEntity 
{

	@Column(name = "name", nullable = false)
	@NotBlank(message = "Category name must not be null or blank!")
	private String name;

	@Column(name = "type", nullable = false)
	@Enumerated(EnumType.STRING)
	@NotNull(message = "Category type must not be null!")
	private CategoryType type;

	@Column(name = "current_period_sum", nullable = false)
	@NotNull(message = "Category current period sum must not be null!")
	private Double currentPeriodSum;

	@Column(name = "category_limit")
	private String limit;

	@ManyToOne
	@JoinColumn(name = "owner_id")
	private UserEntity owner;

	/*
	 * Constructors
	 */
	
	public CategoryEntity() {}

	public CategoryEntity(
		String name,
		CategoryType type,
		Double currentPeriodSum, 
		String limit,
		UserEntity owner) 
	{
		this.name = name;
		this.type = type;
		this.currentPeriodSum = currentPeriodSum;
		this.limit = limit;
		this.owner = owner;
	}

	/*
	 * Getters & setters
	 */
	
	public String getName() {
		return this.name;
	}

	public CategoryType getType() {
		return this.type;
	}

	public Double getCurrentPeriodSum() {
		return this.currentPeriodSum;
	}

	public String getLimit() {
		return this.limit;
	}

	public UserEntity getOwner() {
		return this.owner;
	}

	public CategoryEntity setName(String name) {
		this.name = name;
		return this;
	}

	public CategoryEntity setType(CategoryType type) {
		this.type = type;
		return this;
	}

	public CategoryEntity setCurrentPeriodSum(Double currentPeriodSum) {
		this.currentPeriodSum = currentPeriodSum;
		return this;
	}

	public CategoryEntity setLimit(String limit) {
		this.limit = limit;
		return this;
	}

	public CategoryEntity setOwner(UserEntity owner) {
		this.owner = owner;
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
		CategoryEntity that = (CategoryEntity) o;
		return Objects.equals(owner, that.owner) &&
				Objects.equals(name, that.name) &&
				Objects.equals(currentPeriodSum, that.currentPeriodSum) &&
				Objects.equals(limit, that.limit);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), owner, name, type, currentPeriodSum, limit);
	}
	
	public String toString() {
		return "CategoryEntity(name=" + this.getName() + ", type=" + this.getType() + ", currentPeriodSum=" + this.getCurrentPeriodSum() + ", limit=" + this.getLimit() + ", owner=" + this.getOwner() + ")";
	}
	
}