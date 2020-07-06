package com.mse.personal.finance.db.entity;

import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.mse.personal.finance.db.validation.ValidEmail;
import com.mse.personal.finance.model.FamilyStatusType;
import com.mse.personal.finance.model.GenderType;

/**
 * Persistence entity for storing a user in the system.
 *
 * @author T. Dossev
 * @author dvt32
 */
@Entity
@Table(name = "users")
public class UserEntity 
	extends BaseEntity 
{

	@Column(name = "name", length = 255, nullable = false)
	@NotNull(message = "User name must not be null!")
	private String name;

	@Column(name = "password")
	@NotBlank(message = "User password must not be null or blank!")
	@Size(min = 6, message = "User password must be at least 6 characters!")
	private String password;

	@Column(name = "email", length = 100, unique = true)
	@NotBlank(message = "User email must not be null or blank!")
	@ValidEmail
	private String email;

	@Column(name = "gender")
	private GenderType gender;

	@Column(name = "family_status")
	@Enumerated(EnumType.STRING)
	private FamilyStatusType familyStatus;

	@Column(name = "age")
	private Integer age;

	@Column(name = "education")
	private String education;

	/*
	 * NOTE: Cascade is set to "CascadeType.ALL", so that
	 * when a user gets deleted, all of his
	 * accounts, categories, reporting periods, transactions & settings
	 * will be deleted as well.
	 */

	@OneToMany(mappedBy = "owner", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<AccountEntity> accounts;

	@OneToMany(mappedBy = "owner", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<CategoryEntity> categories;

	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<ReportingPeriodEntity> reportingPeriods;

	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<TransactionEntity> transactions;

	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<UserSettingEntity> settings;

	@JoinTable(
		name = "accounts_users",
		joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
		inverseJoinColumns = @JoinColumn(name = "account_id", referencedColumnName = "id")
	)
	@ElementCollection
	private List<AccountEntity> sharedAccounts;
	
	/*
	 * Constructors
	 */
	
	public UserEntity() {}
	
	public UserEntity(
		String name,
		String password,
		String email, 
		GenderType gender,
		FamilyStatusType familyStatus, 
		Integer age, 
		String education) 
	{
		this.name = name;
		this.password = password;
		this.email = email;
		this.gender = gender;
		this.familyStatus = familyStatus;
		this.age = age;
		this.education = education;
	}

	/*
	 * Getters & setters
	 */

	public String getName() {
		return this.name;
	}

	public String getPassword() {
		return this.password;
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

	public List<AccountEntity> getAccounts() {
		return this.accounts;
	}

	public List<CategoryEntity> getCategories() {
		return this.categories;
	}

	public List<ReportingPeriodEntity> getReportingPeriods() {
		return this.reportingPeriods;
	}

	public List<TransactionEntity> getTransactions() {
		return this.transactions;
	}

	public List<UserSettingEntity> getSettings() {
		return this.settings;
	}

	public List<AccountEntity> getSharedAccounts() {
		return this.sharedAccounts;
	}

	public UserEntity setName(String name) {
		this.name = name;
		return this;
	}

	public UserEntity setPassword(String password) {
		this.password = password;
		return this;
	}

	public UserEntity setEmail(String email) {
		this.email = email;
		return this;
	}

	public UserEntity setGender(GenderType gender) {
		this.gender = gender;
		return this;
	}

	public UserEntity setFamilyStatus(FamilyStatusType familyStatus) {
		this.familyStatus = familyStatus;
		return this;
	}

	public UserEntity setAge(Integer age) {
		this.age = age;
		return this;
	}

	public UserEntity setEducation(String education) {
		this.education = education;
		return this;
	}

	public UserEntity setAccounts(List<AccountEntity> accounts) {
		this.accounts = accounts;
		return this;
	}

	public UserEntity setCategories(List<CategoryEntity> categories) {
		this.categories = categories;
		return this;
	}

	public UserEntity setReportingPeriods(List<ReportingPeriodEntity> reportingPeriods) {
		this.reportingPeriods = reportingPeriods;
		return this;
	}

	public UserEntity setTransactions(List<TransactionEntity> transactions) {
		this.transactions = transactions;
		return this;
	}

	public UserEntity setSettings(List<UserSettingEntity> settings) {
		this.settings = settings;
		return this;
	}

	public UserEntity setSharedAccounts(List<AccountEntity> sharedAccounts) {
		this.sharedAccounts = sharedAccounts;
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
		UserEntity that = (UserEntity) o;
		return Objects.equals(name, that.name) &&
				Objects.equals(email, that.email) &&
				Objects.equals(password, that.password) &&
				Objects.equals(gender, that.gender) &&
				Objects.equals(familyStatus, that.familyStatus) &&
				Objects.equals(age, that.age) &&
				Objects.equals(education, that.education);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), name, email, password, gender, familyStatus, age, education);
	}

	public String toString() {
		return "UserEntity(name=" + this.getName() + ", password=" + this.getPassword() + ", email=" + this.getEmail() + ", gender=" + this.getGender() + ", familyStatus=" + this.getFamilyStatus() + ", age=" + this.getAge() + ", education=" + this.getEducation() + ", accounts=" + this.getAccounts() + ", categories=" + this.getCategories() + ", reportingPeriods=" + this.getReportingPeriods() + ", transactions=" + this.getTransactions() + ", settings=" + this.getSettings() + ")";
	}
	
}