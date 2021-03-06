package com.mse.personal.finance.model.request;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.mse.personal.finance.model.FamilyStatusType;
import com.mse.personal.finance.model.GenderType;
import com.mse.personal.finance.model.User;

/**
 * Request DTO for updating a {@link User} user in the system.
 *
 * @author D. Dimitrov
 * @author dvt32
 */
public class UserUpdateRequest {
	
	@NotNull(message = "User name must not be null!")
	@Size(max = 255)
	private String name;

	@Enumerated(EnumType.STRING)
	private GenderType gender;

	@Enumerated(EnumType.STRING)
	private FamilyStatusType familyStatus;

	private Integer age;

	private String education;

	/*
	 * Getters & setters
	 */
	
	public String getName() {
		return this.name;
	}

	public GenderType getGender() {
		return this.gender;
	}

	public FamilyStatusType getFamilyStatus() {
		return this.familyStatus;
	}

	public Integer getAge() {
		return this.age;
	}

	public String getEducation() {
		return this.education;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setGender(GenderType gender) {
		this.gender = gender;
	}

	public void setFamilyStatus(FamilyStatusType familyStatus) {
		this.familyStatus = familyStatus;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public void setEducation(String education) {
		this.education = education;
	}
	
}