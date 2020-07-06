package com.mse.personal.finance.model;

/**
 * An enum for valid account types in the system.
 */
public enum AccountType {
	
	ACTIVATED("ACTIVATED"),
	DEACTIVATED("DEACTIVATED"),
	DELETED("DELETED");

	private String label;

	AccountType(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		return this.label;
	}
	
}