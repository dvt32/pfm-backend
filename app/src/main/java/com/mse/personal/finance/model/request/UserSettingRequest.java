package com.mse.personal.finance.model.request;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.mse.personal.finance.model.UserSettingKey;

/**
 * Request DTO for creating a {@link com.mse.personal.finance.model.UserSetting} user setting in the system.
 *
 * @author D. Dimitrov
 * @author dvt32
 */
public class UserSettingRequest {

	@Enumerated(EnumType.STRING)
	@NotNull(message = "User setting key must not be null!")
	private UserSettingKey key;

	@NotBlank(message = "User setting value must not be null or blank!")
	private String value;

	/*
	 * Getters & setters
	 */
	
	public UserSettingKey getKey() {
		return this.key;
	}

	public String getValue() {
		return this.value;
	}

	public void setKey(UserSettingKey key) {
		this.key = key;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
}