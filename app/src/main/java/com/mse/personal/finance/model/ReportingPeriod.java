package com.mse.personal.finance.model;

import java.util.Date;

/**
 * DTO for a reporting period.
 *
 * @author D. Dimitrov
 */

public class ReportingPeriod {

	private Long id;
	private Date endDate;
	private Double endSum;
	
	/*
	 * Getters & setters
	 */
	
	public Long getId() {
		return this.id;
	}

	public Date getEndDate() {
		return this.endDate;
	}

	public Double getEndSum() {
		return this.endSum;
	}

	public ReportingPeriod setId(Long id) {
		this.id = id;
		return this;
	}

	public ReportingPeriod setEndDate(Date endDate) {
		this.endDate = endDate;
		return this;
	}

	public ReportingPeriod setEndSum(Double endSum) {
		this.endSum = endSum;
		return this;
	}
	
}