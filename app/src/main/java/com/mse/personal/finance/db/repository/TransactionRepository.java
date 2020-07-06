package com.mse.personal.finance.db.repository;

import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mse.personal.finance.db.entity.TransactionEntity;
import com.mse.personal.finance.db.entity.UserEntity;
import com.mse.personal.finance.model.TransactionFromType;
import com.mse.personal.finance.model.TransactionToType;

/**
 * Persistence DAO for performing CRUD operations upon {@link TransactionEntity}.
 *
 * @author dvt32
 */
@Repository
public interface TransactionRepository 
	extends JpaRepository<TransactionEntity, Long> 
{
	
	Page<TransactionEntity> findAllByUser(UserEntity user, Pageable pageable);
	
	Page<TransactionEntity> findAllByUserAndFromTypeAndFromId(UserEntity user, TransactionFromType fromType, Long fromId, Pageable pageable);
	
	Page<TransactionEntity> findAllByUserAndToTypeAndToId(UserEntity user, TransactionToType toType, Long toId, Pageable pageable);
	
	/*
	 * General income transaction operations
	 */
	
	@Query(
		"SELECT t " + 
		"FROM TransactionEntity t " + 
		"WHERE fromType = 'CATEGORY' AND toType = 'ACCOUNT' AND user_id = :userId"
	)
	Page<TransactionEntity> findAllIncomeTransactions(@Param("userId") Long userId, Pageable pageable);
	
	@Query(
		"SELECT sum(t.sum) " + 
		"FROM TransactionEntity t " + 
		"WHERE fromType = 'CATEGORY' AND toType = 'ACCOUNT' AND user_id = :userId"
	)
	Double getTotalIncomeTransactionsSum(@Param("userId") Long userId);
	
	@Query(
		"SELECT t " + 
		"FROM TransactionEntity t " + 
		"WHERE fromType = 'CATEGORY' AND toType = 'ACCOUNT' AND user_id = :userId "
		+ "AND dateOfCompletion BETWEEN :startDate AND :endDate"
	)
	Page<TransactionEntity> findAllIncomeTransactionsBetweenDates(
		@Param("userId") Long userId, 
		@Param("startDate") Date startDate,
		@Param("endDate") Date endDate,
		Pageable pageable
	);
	
	@Query(
		"SELECT sum(t.sum) " + 
		"FROM TransactionEntity t " + 
		"WHERE fromType = 'CATEGORY' AND toType = 'ACCOUNT' AND user_id = :userId "
		+ "AND dateOfCompletion BETWEEN :startDate AND :endDate"
	)
	Double getTotalIncomeTransactionsSumBetweenDates(
		@Param("userId") Long userId, 
		@Param("startDate") Date startDate,
		@Param("endDate") Date endDate
	);
	
	/*
	 * General expense transaction operations
	 */
	
	@Query(
		"SELECT t " + 
		"FROM TransactionEntity t " + 
		"WHERE fromType = 'ACCOUNT' AND toType = 'CATEGORY' AND user_id = :userId"
	)
	Page<TransactionEntity> findAllExpenseTransactions(@Param("userId") Long userId, Pageable pageable);
	
	@Query(
		"SELECT sum(t.sum) " + 
		"FROM TransactionEntity t " + 
		"WHERE fromType = 'ACCOUNT' AND toType = 'CATEGORY' AND user_id = :userId"
	)
	Double getTotalExpenseTransactionsSum(@Param("userId") Long userId);
	
	@Query(
		"SELECT t " + 
		"FROM TransactionEntity t " + 
		"WHERE fromType = 'ACCOUNT' AND toType = 'CATEGORY' AND user_id = :userId "
		+ "AND dateOfCompletion BETWEEN :startDate AND :endDate"
	)
	Page<TransactionEntity> findAllExpenseTransactionsBetweenDates(
		@Param("userId") Long userId, 
		@Param("startDate") Date startDate,
		@Param("endDate") Date endDate,
		Pageable pageable
	);
	
	@Query(
		"SELECT sum(t.sum) " + 
		"FROM TransactionEntity t " + 
		"WHERE fromType = 'ACCOUNT' AND toType = 'CATEGORY' AND user_id = :userId "
		+ "AND dateOfCompletion BETWEEN :startDate AND :endDate"
	)
	Double getTotalExpenseTransactionsSumBetweenDates(
		@Param("userId") Long userId, 
		@Param("startDate") Date startDate,
		@Param("endDate") Date endDate
	);
	
	/*
	 * General transfer transaction operations
	 */
	
	@Query(
		"SELECT t " + 
		"FROM TransactionEntity t " + 
		"WHERE fromType = 'ACCOUNT' AND toType = 'ACCOUNT' AND user_id = :userId"
	)
	Page<TransactionEntity> findAllTransferTransactions(@Param("userId") Long userId, Pageable pageable);
	
	@Query(
		"SELECT sum(t.sum) " + 
		"FROM TransactionEntity t " + 
		"WHERE fromType = 'ACCOUNT' AND toType = 'ACCOUNT' AND user_id = :userId"
	)
	Double getTotalTransferTransactionsSum(@Param("userId") Long userId);
	
	@Query(
		"SELECT t " + 
		"FROM TransactionEntity t " + 
		"WHERE fromType = 'ACCOUNT' AND toType = 'ACCOUNT' AND user_id = :userId "
		+ "AND dateOfCompletion BETWEEN :startDate AND :endDate"
	)
	Page<TransactionEntity> findAllTransferTransactionsBetweenDates(
		@Param("userId") Long userId, 
		@Param("startDate") Date startDate,
		@Param("endDate") Date endDate,
		Pageable pageable
	);
	
	@Query(
		"SELECT sum(t.sum) " + 
		"FROM TransactionEntity t " + 
		"WHERE fromType = 'ACCOUNT' AND toType = 'ACCOUNT' AND user_id = :userId "
		+ "AND dateOfCompletion BETWEEN :startDate AND :endDate"
	)
	Double getTotalTransferTransactionsSumBetweenDates(
		@Param("userId") Long userId, 
		@Param("startDate") Date startDate,
		@Param("endDate") Date endDate
	);
	
	/*
	 * Account-specific operations
	 */
	
	// Note: income/expenses from transfer transactions are also included in the total sums.
	
	@Query(
		"SELECT sum(t.sum) " + 
		"FROM TransactionEntity t " + 
		"WHERE user_id = :userId AND dateOfCompletion BETWEEN :startDate AND :endDate " 
		+ "AND ("
		+ "(fromType = 'CATEGORY' AND toType = 'ACCOUNT' AND toId = :accountId) OR"
		+ "(fromType = 'ACCOUNT' AND toType = 'ACCOUNT' AND toId = :accountId)"
		+ ")"
	)
	Double getTotalIncomeSumBetweenDatesForAccountById(
		@Param("userId") Long userId, 
		@Param("startDate") Date startDate,
		@Param("endDate") Date endDate,
		@Param("accountId") Long accountId
	);
	
	@Query(
		"SELECT sum(t.sum) " + 
		"FROM TransactionEntity t " + 
		"WHERE user_id = :userId AND dateOfCompletion BETWEEN :startDate AND :endDate " 
		+ "AND ("
			+ "(fromType = 'ACCOUNT' AND toType = 'CATEGORY' AND fromId = :accountId) OR"
			+ "(fromType = 'ACCOUNT' AND toType = 'ACCOUNT' AND fromId = :accountId)"
		+ ")"
	)
	Double getTotalExpenseSumBetweenDatesForAccountById(
		@Param("userId") Long userId, 
		@Param("startDate") Date startDate,
		@Param("endDate") Date endDate,
		@Param("accountId") Long accountId
	);
	
	/*
	 * Category-specific operations
	 */
	
	@Query(
		"SELECT sum(t.sum) " + 
		"FROM TransactionEntity t " + 
		"WHERE user_id = :userId AND dateOfCompletion BETWEEN :startDate AND :endDate " 
		+ "AND fromType = 'CATEGORY' AND toType = 'ACCOUNT' AND fromId = :categoryId"
	)
	Double getTotalAddedSumBetweenDatesForIncomeCategoryById(
		@Param("userId") Long userId, 
		@Param("startDate") Date startDate,
		@Param("endDate") Date endDate,
		@Param("categoryId") Long categoryId
	);
	
	@Query(
		"SELECT sum(t.sum) " + 
		"FROM TransactionEntity t " + 
		"WHERE user_id = :userId AND dateOfCompletion BETWEEN :startDate AND :endDate " 
		+ "AND fromType = 'ACCOUNT' AND toType = 'CATEGORY' AND toId = :categoryId"
	)
	Double getTotalAddedSumBetweenDatesForExpenseCategoryById(
		@Param("userId") Long userId, 
		@Param("startDate") Date startDate,
		@Param("endDate") Date endDate,
		@Param("categoryId") Long categoryId
	);
	
}