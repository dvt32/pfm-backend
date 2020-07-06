package com.mse.personal.finance.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mse.personal.finance.db.entity.CategoryEntity;
import com.mse.personal.finance.db.entity.UserEntity;
import com.mse.personal.finance.model.CategoryType;

/**
 * Persistence DAO for performing CRUD operations upon {@link CategoryEntity}.
 *
 * @author D. Dimitrov
 * @author dvt32
 */
@Repository
public interface CategoryRepository 
	extends JpaRepository<CategoryEntity, Long> 
{
	
	List<CategoryEntity> findAllByOwner(UserEntity owner);
	
	List<CategoryEntity> findAllByTypeAndOwner(CategoryType type, UserEntity owner);
	
	boolean existsByNameAndOwner(String name, UserEntity owner);
	
	CategoryEntity findByNameAndOwner(String name, UserEntity owner);
	
	@Query(
		"SELECT sum(currentPeriodSum) " + 
		"FROM CategoryEntity " +
		"WHERE " + 
			"type = 'INCOME' AND " +
			"owner_id = :ownerId AND " +
			"name != 'SYS_INCOME'"
	)
    Double getTotalCurrentPeriodSumOfIncomeCategories(@Param("ownerId") Long ownerId);
	
	@Query(
		"SELECT sum(currentPeriodSum) " + 
		"FROM CategoryEntity " +
		"WHERE " + 
			"type = 'EXPENSES' AND " +
			"owner_id = :ownerId AND " +
			"name != 'SYS_EXPENSES'"
	)
    Double getTotalCurrentPeriodSumOfExpenseCategories(@Param("ownerId") Long ownerId);
	
}