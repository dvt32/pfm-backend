package com.mse.personal.finance.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mse.personal.finance.db.entity.ReportingPeriodEntity;
import com.mse.personal.finance.db.entity.UserEntity;

/**
 * Persistence DAO for performing CRUD operations upon {@link ReportingPeriodEntity}.
 *
 * @author D. Dimitrov
 * @author dvt32
 */
@Repository
public interface ReportingPeriodRepository 
	extends JpaRepository<ReportingPeriodEntity, Long> 
{
	
	List<ReportingPeriodEntity> findAllByUser(UserEntity user);
	
}