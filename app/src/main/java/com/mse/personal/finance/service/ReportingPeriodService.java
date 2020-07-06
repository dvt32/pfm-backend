package com.mse.personal.finance.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mse.personal.finance.db.entity.ReportingPeriodEntity;
import com.mse.personal.finance.db.entity.UserEntity;
import com.mse.personal.finance.db.repository.ReportingPeriodRepository;
import com.mse.personal.finance.model.ReportingPeriod;
import com.mse.personal.finance.model.request.ReportingPeriodRequest;
import com.mse.personal.finance.rest.exception.ReportingPeriodNotFoundException;
import com.mse.personal.finance.rest.exception.UserDoesNotOwnResourceException;
import com.mse.personal.finance.service.mapper.ReportingPeriodMapper;

/**
 * Service for managing {@link ReportingPeriod} reporting periods
 * of the currently logged-in user.
 * 
 * The entities are retrieved from the database
 * and then mapped to DTOs, which will be used by the REST controller.
 * 
 * Likewise, DTOs sent from the REST controller 
 * are mapped to entities and then added to the database.
 * 
 * @author dvt32
 */
@Service
public class ReportingPeriodService {

	private final ReportingPeriodRepository reportingPeriodRepository;
	private final ReportingPeriodMapper reportingPeriodMapper;
	private final ServiceUtils serviceUtils;

	@Autowired
	public ReportingPeriodService(
		ReportingPeriodRepository reportingPeriodRepository, 
		ReportingPeriodMapper reportingPeriodMapper,
		ServiceUtils serviceUtils) 
	{
		this.reportingPeriodRepository = reportingPeriodRepository;
		this.reportingPeriodMapper = reportingPeriodMapper;
		this.serviceUtils = serviceUtils;
	}
	
	/**
	 * Returns all existing reporting periods' data as a list.
	 */
	public List<ReportingPeriod> getAllReportingPeriods() {
		UserEntity currentlyLoggedInUserEntity = serviceUtils.getCurrentlyLoggedInUserEntity();
		
		List<ReportingPeriodEntity> allReportingPeriodEntities = reportingPeriodRepository.findAllByUser(currentlyLoggedInUserEntity);
		
		List<ReportingPeriod> allReportingPeriodDTOs = allReportingPeriodEntities.stream()
			.map( reportingPeriodEntity -> reportingPeriodMapper.fromEntity(reportingPeriodEntity) )
			.collect( Collectors.toList() );
		
		return allReportingPeriodDTOs; 
	}
	
	/**
	 * Returns an existing reporting period's data.
	 */
	public ReportingPeriod getReportingPeriodById(Long id) 
		throws ReportingPeriodNotFoundException, UserDoesNotOwnResourceException
	{
		boolean existsById = reportingPeriodRepository.existsById(id);
		if (!existsById) {
			throw new ReportingPeriodNotFoundException(id);
		}
		
		ReportingPeriodEntity reportingPeriodEntity = reportingPeriodRepository.findById(id).get();
		boolean belongsToCurrentlyLoggedInUser = serviceUtils.belongsToCurrentlyLoggedInUser(reportingPeriodEntity);
		if (!belongsToCurrentlyLoggedInUser) {
			throw new UserDoesNotOwnResourceException();
		}
		
		ReportingPeriod reportingPeriodDTO = reportingPeriodMapper.fromEntity(reportingPeriodEntity);
		
		return reportingPeriodDTO;
	}
	
	/**
	 * Creates a new reporting period from a DTO, 
	 * which contains the reporting period's data
	 * and returns the newly created reporting period's data.
	 */
	public ReportingPeriod createNewReportingPeriod(ReportingPeriodRequest reportingPeriod) {
		ReportingPeriodEntity reportingPeriodEntity = reportingPeriodMapper.toEntity(reportingPeriod);
		// Owner is not included in the request DTO and must be set manually.
		// It is retrieved from the currently logged-in user's details.
		UserEntity owner = serviceUtils.getCurrentlyLoggedInUserEntity();
		reportingPeriodEntity.setUser(owner);
		reportingPeriodEntity = reportingPeriodRepository.save(reportingPeriodEntity);
		
		ReportingPeriod reportingPeriodDTO = reportingPeriodMapper.fromEntity(reportingPeriodEntity);
		
		return reportingPeriodDTO;
	}
	
	/**
	 * Updates an existing reporting period with a specified ID
	 * and returns the updated reporting period's data.
	 */
	public ReportingPeriod updateReportingPeriodById(Long id, ReportingPeriodRequest reportingPeriod) 
		throws ReportingPeriodNotFoundException, UserDoesNotOwnResourceException
	{
		boolean existsById = reportingPeriodRepository.existsById(id);
		if (!existsById) {
			throw new ReportingPeriodNotFoundException(id);
		}
		
		ReportingPeriodEntity reportingPeriodEntity = reportingPeriodRepository.findById(id).get();
		boolean belongsToCurrentlyLoggedInUser = serviceUtils.belongsToCurrentlyLoggedInUser(reportingPeriodEntity);
		if (!belongsToCurrentlyLoggedInUser) {
			throw new UserDoesNotOwnResourceException();
		}
		
		ReportingPeriodEntity updatedReportingPeriodEntity = reportingPeriodMapper.toEntity(reportingPeriod);
		// ID and owner are not included in the request DTO and must be set manually.
		// ID is retrieved from the HTTP request's passed URL.
		// Owner is retrieved from the already existing entity.
		updatedReportingPeriodEntity.setId(id);
		UserEntity reportingPeriodEntityOwner = reportingPeriodEntity.getUser();
		updatedReportingPeriodEntity.setUser(reportingPeriodEntityOwner);
		updatedReportingPeriodEntity = reportingPeriodRepository.save(updatedReportingPeriodEntity);
		
		ReportingPeriod reportingPeriodDTO = reportingPeriodMapper.fromEntity(updatedReportingPeriodEntity);
		
		return reportingPeriodDTO;
	}
	
	/**
	 * Deletes an existing reporting period with a specified ID
	 * and returns the deleted reporting period's data.
	 */
	public ReportingPeriod deleteReportingPeriodById(Long id) 
		throws ReportingPeriodNotFoundException, UserDoesNotOwnResourceException
	{
		boolean existsById = reportingPeriodRepository.existsById(id);
		if (!existsById) {
			throw new ReportingPeriodNotFoundException("Reporting period with this ID does not exist!");
		}
		
		ReportingPeriodEntity reportingPeriodEntity = reportingPeriodRepository.findById(id).get();
		boolean belongsToCurrentlyLoggedInUser = serviceUtils.belongsToCurrentlyLoggedInUser(reportingPeriodEntity);
		if (!belongsToCurrentlyLoggedInUser) {
			throw new UserDoesNotOwnResourceException();
		}
		
		ReportingPeriod reportingPeriodDTO = reportingPeriodMapper.fromEntity(reportingPeriodEntity);
		
		reportingPeriodRepository.deleteById(id);
		
		return reportingPeriodDTO;
	}
	
}