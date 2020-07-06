package com.mse.personal.finance.model.request;

import com.mse.personal.finance.model.Category;

/**
 * Request DTO for updating a {@link Category} category's limit in the system.
 *
 * @author dvt32
 */
public class CategoryLimitUpdateRequest {

	private String limit;

	/*
	 * Getters & setters
	 */

	public String getLimit() {
		return this.limit;
	}
	
	public void setLimit(String limit) {
		this.limit = limit;
	}
	
}