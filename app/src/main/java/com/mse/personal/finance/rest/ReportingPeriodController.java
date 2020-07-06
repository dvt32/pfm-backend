package com.mse.personal.finance.rest;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mse.personal.finance.model.ReportingPeriod;
import com.mse.personal.finance.model.request.ReportingPeriodRequest;
import com.mse.personal.finance.rest.exception.InvalidDataException;
import com.mse.personal.finance.rest.exception.ReportingPeriodNotFoundException;
import com.mse.personal.finance.rest.exception.UserDoesNotOwnResourceException;
import com.mse.personal.finance.service.ReportingPeriodService;

/**
 * REST controller for CRUD operations 
 * upon {@link ReportingPeriod} reporting periods
 * for the currently logged-in user.
 * 
 * @author dvt32
 */
@RestController
@RequestMapping(path = "/reporting-periods")
public class ReportingPeriodController {
	
	private final ReportingPeriodService reportingPeriodService;

	@Autowired
	public ReportingPeriodController(ReportingPeriodService reportingPeriodService) {
		this.reportingPeriodService = reportingPeriodService;
	}
	
	/**
	 * Returns all reporting periods' data as a JSON array.
	 * 
	 * @return the list of reporting periods (empty if no reporting periods available)
	 */
	@GetMapping
	public List<ReportingPeriod> getAllReportingPeriods() {
		List<ReportingPeriod> allReportingPeriods = reportingPeriodService.getAllReportingPeriods();
		return allReportingPeriods;
	}
	
	/**
	 * Returns a specific reporting period's data as a JSON object
	 * 
	 * @param id The id of the reporting period to be retrieved
	 * @return the reporting period's data
	 */
	@GetMapping("/{id}")
	public ReportingPeriod getReportingPeriodById(@PathVariable Long id) {
		ReportingPeriod reportingPeriod = null;
		
		try {
			reportingPeriod = reportingPeriodService.getReportingPeriodById(id);
		} 
		catch (ReportingPeriodNotFoundException e) {
			throw new ReportingPeriodNotFoundException(id);
		}
		catch (UserDoesNotOwnResourceException e) {
			throw new UserDoesNotOwnResourceException();
		}
		
		return reportingPeriod;
	}
	
	/**
	 * This method creates a reporting period by passing its data in a POST request's body
	 * and returns the newly created reporting period's data as a JSON object.
	 * 
	 * The data is validated before it is stored in the database.
	 * 
	 * @param reportingPeriod An object containing the reporting-period-to-be-created's data
	 * @param bindingResult The validator of the passed data
	 * 
	 * @return the created reporting period's data
	 */
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ReportingPeriod createReportingPeriod(
		@RequestBody @Valid ReportingPeriodRequest reportingPeriod, 
		BindingResult bindingResult)
	{
		if (bindingResult.hasErrors()) {
			String errorMessageFromBindingResult = ControllerUtils.getErrorMessageFromBindingResult(bindingResult);
			throw new InvalidDataException(errorMessageFromBindingResult);
		}
		
		ReportingPeriod createdReportingPeriod = reportingPeriodService.createNewReportingPeriod(reportingPeriod);
		
		return createdReportingPeriod;
	}
	
	/**
	 * This method updates an existing reporting period in the database 
	 * by passing the updated data in a POST request's body
	 * and returns the updated reporting period's data as a JSON object.
	 * 
	 * The data is validated before the reporting period is updated in the database.
	 * 
	 * @param id The reporting-period-to-be-updated's ID
	 * @param reportingPeriod An object containing the reporting-period-to-be-updated's data
	 * @param bindingResult The validator of the passed data
	 * 
	 * @return the updated reporting period's data
	 */
	@PutMapping("/{id}")
	public ReportingPeriod updateReportingPeriodById(
		@PathVariable Long id, 
		@RequestBody @Valid ReportingPeriodRequest reportingPeriod, 
		BindingResult bindingResult) 
	{
		if (bindingResult.hasErrors()) {
			String errorMessageFromBindingResult = ControllerUtils.getErrorMessageFromBindingResult(bindingResult);
			throw new InvalidDataException(errorMessageFromBindingResult);
		}
		
		ReportingPeriod updatedReportingPeriod = null;
		try {
			updatedReportingPeriod = reportingPeriodService.updateReportingPeriodById(id, reportingPeriod);
		} 
		catch (ReportingPeriodNotFoundException e) {
			throw new ReportingPeriodNotFoundException(id);
		}
		catch (UserDoesNotOwnResourceException e) {
			throw new UserDoesNotOwnResourceException();
		}
		
		return updatedReportingPeriod;
	}
	
	/**
	 * This method deletes an existing reporting period in the database by passing an ID
	 * and returns the deleted reporting period's data as a JSON object.
	 * 
	 * If it does not exist, an exception is thrown.
	 * 
	 * @param id The ID of the reporting period
	 * @return the deleted reporting period's data
	 */
	@DeleteMapping("/{id}")
	public ReportingPeriod deleteReportingPeriodById(@PathVariable Long id) {
		ReportingPeriod deletedReportingPeriod = null;
		
		try {
			deletedReportingPeriod = reportingPeriodService.deleteReportingPeriodById(id);
		} 
		catch (ReportingPeriodNotFoundException e) {
			throw new ReportingPeriodNotFoundException(id);
		}
		catch (UserDoesNotOwnResourceException e) {
			throw new UserDoesNotOwnResourceException();
		}
		
		return deletedReportingPeriod;
	}

}