package com.mse.personal.finance.model.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.mse.personal.finance.model.User;

/**
 * Request DTO for updating a {@link User} user's password in the system.
 *
 * @author dvt32
 */
public class UserPasswordUpdateRequest {
	
	@NotBlank(message = "Old user password must not be null or blank!")
	private String oldPassword;
	
	@NotBlank(message = "New user password must not be null or blank!")
	@Size(min = 6, message = "New user password must be at least 6 characters!")
	private String newPassword;
	
	@NotBlank(message = "Matching new user password must not be null or blank!")
	@Size(min = 6, message = "Matching new user password must be at least 6 characters!")
	private String matchingNewPassword;
	
	/*
	 * Getters & setters
	 */
	
	public String getOldPassword() {
		return oldPassword;
	}
	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}
	public String getNewPassword() {
		return newPassword;
	}
	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
	public String getMatchingNewPassword() {
		return matchingNewPassword;
	}
	public void setMatchingNewPassword(String matchingNewPassword) {
		this.matchingNewPassword = matchingNewPassword;
	}
	
}