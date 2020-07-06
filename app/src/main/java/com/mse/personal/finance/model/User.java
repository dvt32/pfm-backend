package com.mse.personal.finance.model;

/**
 * DTO for a user.
 *
 * @author D. Dimitrov
 */

public class User {

	private Long id;
	private String name;
	private String email;
	private GenderType gender;
	private FamilyStatusType familyStatus;
	private Integer age;
	private String education;

	/*
	 * Getters & setters
	 */
	
	public Long getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public String getEmail() {
		return this.email;
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

	public User setId(Long id) {
		this.id = id;
		return this;
	}

	public User setName(String name) {
		this.name = name;
		return this;
	}

	public User setEmail(String email) {
		this.email = email;
		return this;
	}

	public User setGender(GenderType gender) {
		this.gender = gender;
		return this;
	}

	public User setFamilyStatus(FamilyStatusType familyStatus) {
		this.familyStatus = familyStatus;
		return this;
	}

	public User setAge(Integer age) {
		this.age = age;
		return this;
	}

	public User setEducation(String education) {
		this.education = education;
		return this;
	}
	
}