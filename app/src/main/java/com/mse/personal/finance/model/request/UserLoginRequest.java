package com.mse.personal.finance.model.request;

import javax.validation.constraints.NotBlank;

/**
 * DTO for a user login request in the system.
 *
 * @author T. Dossev
 * @author dvt32
 */
public class UserLoginRequest {

	@NotBlank(message = "User email must not be null or blank!")
	private String username;

	@NotBlank(message = "User password must not be null or blank!")
	private String password;

	/*
	 * Getters & setters
	 */
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
}