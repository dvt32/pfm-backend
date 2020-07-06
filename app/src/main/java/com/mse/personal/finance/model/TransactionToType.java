package com.mse.personal.finance.model;

/**
 * An enum for valid transaction to-types in the system.
 */
public enum TransactionToType {
	
	CATEGORY("CATEGORY"),
	ACCOUNT("ACCOUNT");
	
	private String label;

	TransactionToType(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		return this.label;
	}
	
}
