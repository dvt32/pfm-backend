package com.mse.personal.finance.model;

/**
 * An enum for valid transaction from-types in the system.
 */
public enum TransactionFromType {
	
	CATEGORY("CATEGORY"),
	ACCOUNT("ACCOUNT");
	
	private String label;

	TransactionFromType(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		return this.label;
	}
	
}