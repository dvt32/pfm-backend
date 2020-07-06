package com.mse.personal.finance.model.request;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.mse.personal.finance.model.Category;
import com.mse.personal.finance.model.CategoryType;

/**
 * Request DTO for a {@link Category} category in the system.
 *
 * @author D. Dimitrov
 * @author dvt32
 */
public class CategoryRequest {

	@NotBlank(message = "Category name must not be null or blank!")
	@Size(max = 65536)
	private String name;

	@NotNull(message = "Category type must not be null!")
	@Enumerated(EnumType.STRING)
	private CategoryType type;

	@NotNull(message = "Category current period sum must not be null!")
	private Double currentPeriodSum;

	private String limit;

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

	public void setName(String name) {
		this.name = name;
	}

	public void setType(CategoryType type) {
		this.type = type;
	}

	public void setCurrentPeriodSum(Double currentPeriodSum) {
		this.currentPeriodSum = currentPeriodSum;
	}

	public void setLimit(String limit) {
		this.limit = limit;
	}
	
}