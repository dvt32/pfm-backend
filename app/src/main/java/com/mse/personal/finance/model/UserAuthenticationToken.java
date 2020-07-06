package com.mse.personal.finance.model;

/**
 * DTO for a JWT user authentication token.
 *
 * @author dvt32
 */
public class UserAuthenticationToken {
	
	private String username;
	private String displayName;
	private String tokenString;
	
	/*
	 * Constructors
	 */
	
	public UserAuthenticationToken(String username, String displayName, String tokenString) {
		this.username = username;
		this.displayName = displayName;
		this.tokenString = tokenString;
	}

	/*
	 * Getters & setters
	 */
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getTokenString() {
		return tokenString;
	}

	public void setTokenString(String tokenString) {
		this.tokenString = tokenString;
	}
	
}