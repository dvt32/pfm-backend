package com.mse.personal.finance.model.request;

import java.util.Date;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;

import com.mse.personal.finance.model.ReportingPeriod;

/**
 * Request DTO for a {@link ReportingPeriod} reporting period in the system.
 *
 * @author D. Dimitrov
 * @author dvt32
 */
public class ReportingPeriodRequest {

	@Temporal(TemporalType.DATE)
	@FutureOrPresent(message = "Reprting period end date must be in the present or the future!")
	@NotNull(message = "Reporting period end date must not be null!")
	private Date endDate;

	@NotNull(message = "Reporting period end sum must not be null!")
	private Double endSum;

	/*
	 * Getters & setters
	 */
	
	public Date getEndDate() {
		return this.endDate;
	}

	public Double getEndSum() {
		return this.endSum;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public void setEndSum(Double endSum) {
		this.endSum = endSum;
	}
	
}