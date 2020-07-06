package com.mse.personal.finance.db.entity;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.mse.personal.finance.model.UserSettingKey;

/**
 * Persistence entity for storing a user setting in the system.
 *
 * @author D. Dimitrov
 * @author dvt32
 */

@Entity
@Table(name = "user_settings")
public class UserSettingEntity 
	extends BaseEntity 
{

	@Column(name = "key", nullable = false, updatable = false)
	@Enumerated(EnumType.STRING)
	@NotNull(message = "User setting key must not be null!")
	private UserSettingKey key;

	@Column(name = "value", nullable = false)
	@NotBlank(message = "User setting value must not be null or blank!")
	private String value;
	
	@ManyToOne
	@JoinColumn(name = "user_id")
	private UserEntity user;
	
	/*
	 * Constructors
	 */
	
	public UserSettingEntity() {}
	
	public UserSettingEntity(UserSettingKey key, String value, UserEntity user) {
		this.key = key;
		this.value = value;
		this.user = user;
	}
	
	/*
	 * Getters & setters
	 */
	
	public UserSettingKey getKey() {
		return this.key;
	}

	public String getValue() {
		return this.value;
	}

	public UserEntity getUser() {
		return this.user;
	}

	public UserSettingEntity setKey(UserSettingKey key) {
		this.key = key;
		return this;
	}

	public UserSettingEntity setValue(String value) {
		this.value = value;
		return this;
	}

	public UserSettingEntity setUser(UserEntity user) {
		this.user = user;
		return this;
	}

	/*
	 * Other methods
	 */
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		UserSettingEntity that = (UserSettingEntity) o;
		return Objects.equals(key, that.key) &&
				Objects.equals(value, that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), key, value);
	}
	
	public String toString() {
		return "UserSettingEntity(key=" + this.getKey() + ", value=" + this.getValue() + ", user=" + this.getUser() + ")";
	}
	
}