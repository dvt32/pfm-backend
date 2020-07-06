package com.mse.personal.finance.db.entity;

import java.util.Date;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;

/**
 * Persistence entity for users' reporting periods in the finance system.
 * Contains the needed information for user reporting periods.
 *
 * @author D. Dimitrov
 * @author dvt32
 */
@Entity
@Table(name = "reporting_periods")
public class ReportingPeriodEntity 
	extends BaseEntity 
{

	@Column(name = "end_date", nullable = false)
	@Temporal(TemporalType.DATE)
	@FutureOrPresent(message = "Reporting period end date must be in the present or the future!")
	@NotNull(message = "Reporting period end date must not be null!")
	private Date endDate;

	@Column(name = "end_sum", nullable = false)
	@NotNull(message = "Reporting period end sum must not be null!")
	private Double endSum;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private UserEntity user;

	/*
	 * Constructors
	 */
	
	public ReportingPeriodEntity() {}
	
	/*
	 * Getters & setters
	 */
	
	public Date getEndDate() {
		return this.endDate;
	}

	public Double getEndSum() {
		return this.endSum;
	}

	public UserEntity getUser() {
		return this.user;
	}

	public ReportingPeriodEntity setEndDate(Date endDate) {
		this.endDate = endDate;
		return this;
	}

	public ReportingPeriodEntity setEndSum(Double endSum) {
		this.endSum = endSum;
		return this;
	}

	public ReportingPeriodEntity setUser(UserEntity user) {
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
		ReportingPeriodEntity that = (ReportingPeriodEntity) o;
		return Objects.equals(user, that.user) &&
				Objects.equals(endDate, that.endDate) &&
				Objects.equals(endSum, that.endSum);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), user, endDate, endSum);
	}

	public String toString() {
		return "ReportingPeriodEntity(endDate=" + this.getEndDate() + ", endSum=" + this.getEndSum() + ", user=" + this.getUser() + ")";
	}
	
}