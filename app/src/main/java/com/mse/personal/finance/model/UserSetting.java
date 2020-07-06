package com.mse.personal.finance.model;

/**
 * DTO for a user setting.
 *
 * @author D. Dimitrov
 * @author dvt32
 */

public class UserSetting {

	private Long id;
	private UserSettingKey key;
	private String value;

	/*
	 * Getters & setters
	 */
	
	public Long getId() {
		return this.id;
	}

	public UserSettingKey getKey() {
		return this.key;
	}

	public String getValue() {
		return this.value;
	}

	public UserSetting setId(Long id) {
		this.id = id;
		return this;
	}

	public UserSetting setKey(UserSettingKey key) {
		this.key = key;
		return this;
	}

	public UserSetting setValue(String value) {
		this.value = value;
		return this;
	}
	
}