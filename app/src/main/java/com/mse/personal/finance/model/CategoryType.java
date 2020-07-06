package com.mse.personal.finance.model;

/**
 * An enum for valid category types in the system.
 */
public enum CategoryType {
	
	INCOME("INCOME"),
	EXPENSES("EXPENSES");

	private String label;

	CategoryType(String label) {
		this.label = label;
	}
	
	@Override
	public String toString() {
		return this.label;
	}
	
}