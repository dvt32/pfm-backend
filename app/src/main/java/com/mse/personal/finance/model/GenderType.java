package com.mse.personal.finance.model;

/**
 * An enum for valid user gender types in the system.
 */
public enum GenderType {
	
	MALE("MALE"),
	FEMALE("FEMALE");

	private String label;

	GenderType(String label) {
		this.label = label;
	}
	
	@Override
	public String toString() {
		return this.label;
	}
	
}