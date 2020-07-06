package com.mse.personal.finance.model;

/**
 * An enum for valid user family status types in the system.
 */
public enum FamilyStatusType {
	
	SINGLE("SINGLE"),
	MARRIED("MARRIED"),
	COHABITATION("COHABITATION"),
	WIDOW_OR_WIDOWER("WIDOW_OR_WIDOWER"),
	OTHER("OTHER");

	private String label;

	FamilyStatusType(String label) {
		this.label = label;
	}
	
	@Override
	public String toString() {
		return this.label;
	}
	
}