package com.mse.personal.finance.model;

/**
 * DTO for a category.
 *
 * @author D. Dimitrov
 * @author dvt32
 */

public class Category {

	private Long id;
	private String name;
	private CategoryType type;
	private Double currentPeriodSum;
	private String limit;

	/*
	 * Getters & setters
	 */
	
	public Long getId() {
		return this.id;
	}

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

	public Category setId(Long id) {
		this.id = id;
		return this;
	}

	public Category setName(String name) {
		this.name = name;
		return this;
	}

	public Category setType(CategoryType type) {
		this.type = type;
		return this;
	}

	public Category setCurrentPeriodSum(Double currentPeriodSum) {
		this.currentPeriodSum = currentPeriodSum;
		return this;
	}

	public Category setLimit(String limit) {
		this.limit = limit;
		return this;
	}
	
}