package com.mse.personal.finance.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.mse.personal.finance.db.entity.AccountEntity;
import com.mse.personal.finance.db.entity.CategoryEntity;
import com.mse.personal.finance.db.entity.TransactionEntity;
import com.mse.personal.finance.db.entity.UserEntity;
import com.mse.personal.finance.db.repository.AccountRepository;
import com.mse.personal.finance.db.repository.CategoryRepository;
import com.mse.personal.finance.db.repository.TransactionRepository;
import com.mse.personal.finance.model.AccountType;
import com.mse.personal.finance.model.CategoryType;
import com.mse.personal.finance.model.Transaction;
import com.mse.personal.finance.model.TransactionFromType;
import com.mse.personal.finance.model.TransactionToType;
import com.mse.personal.finance.model.request.TransactionRequest;
import com.mse.personal.finance.rest.exception.InvalidDataException;
import com.mse.personal.finance.rest.exception.TransactionNotFoundException;
import com.mse.personal.finance.rest.exception.UserDoesNotOwnResourceException;
import com.mse.personal.finance.service.mapper.TransactionMapper;

/**
 * Service for managing {@link Transaction} transactions
 * of the currently logged-in user.
 * 
 * The entities are retrieved from the database
 * and then mapped to DTOs, which will be used by the REST controller.
 * 
 * Likewise, DTOs sent from the REST controller 
 * are mapped to entities and then added to the database.
 * 
 * TODO:
 * - Fix precision issues with 'double'
 * - Implement reporting period logic
 * - Add proper exception/error messages for invalid from-to data
 * 
 * @author dvt32
 */
@Service
public class TransactionService {

	private final TransactionRepository transactionRepository;
	private final TransactionMapper transactionMapper;
	private final ServiceUtils serviceUtils;
	private final CategoryRepository categoryRepository;
	private final AccountRepository accountRepository;
	private final AccountService accountService;
	private final CategoryService categoryService;

	/**
	 * Constructor to autowire the service's fields.
	 * 
	 * Note that the @Lazy annotation is added to prevent circular bean dependency.
	 */
	@Autowired
	public TransactionService(
		TransactionRepository transactionRepository, 
		TransactionMapper transactionMapper,
		ServiceUtils serviceUtils,
		CategoryRepository categoryRepository,
		AccountRepository accountRepository,
		@Lazy AccountService accountService,
		CategoryService categoryService)
	{
		this.transactionRepository = transactionRepository;
		this.transactionMapper = transactionMapper;
		this.serviceUtils = serviceUtils;
		this.categoryRepository = categoryRepository;
		this.accountRepository = accountRepository;
		this.accountService = accountService;
		this.categoryService = categoryService;
	}
	
	/**
	 * Returns a page of transactions' data 
	 * with the paging restriction 
	 * in the passed Pageable object.
	 */
	public Page<Transaction> getTransactionsByPageable(Pageable pageable) {
		UserEntity currentlyLoggedInUserEntity = serviceUtils.getCurrentlyLoggedInUserEntity();
		
		Page<TransactionEntity> transactionEntities = transactionRepository.findAllByUser(currentlyLoggedInUserEntity, pageable);
		Page<Transaction> transactionDTOs = transactionEntities.map(transactionMapper::fromEntity);
		
		return transactionDTOs;
	}
	
	/**
	 * Returns a page of transactions of a specified type's data 
	 * with the paging restriction 
	 * in the passed Pageable object.
	 * 
	 * Allowed transaction types (case-insensitive):
	 * - "INCOME"
	 * - "EXPENSE"
	 * - "TRANSFER"
	 */
	public Page<Transaction> getTransactionsByTypeAndPageable(String type, Pageable pageable)
		throws InvalidDataException
	{
		Page<TransactionEntity> transactionEntities = null;
		
		Long loggedInUserId = serviceUtils.getCurrentlyLoggedInUserEntity().getId();
		if (type.equalsIgnoreCase("INCOME")) {
			transactionEntities = transactionRepository.findAllIncomeTransactions(loggedInUserId, pageable);
		}
		else if (type.equalsIgnoreCase("EXPENSE")) {
			transactionEntities = transactionRepository.findAllExpenseTransactions(loggedInUserId, pageable);
		}
		else if (type.equalsIgnoreCase("TRANSFER")) {
			transactionEntities = transactionRepository.findAllTransferTransactions(loggedInUserId, pageable);
		}
		else {
			throw new InvalidDataException("Transaction type is invalid!");
		}
		
		Page<Transaction> transactionDTOs = transactionEntities.map(transactionMapper::fromEntity);
		
		return transactionDTOs;
	}
	
	/**
	 * Returns the total sum of transactions of a specified type.
	 * 
	 * Allowed transaction types (case-insensitive):
	 * - "INCOME"
	 * - "EXPENSE"
	 * - "TRANSFER"
	 */
	public Double getTotalTransactionsSumByType(String type)
		throws InvalidDataException
	{
		Double totalSum = null;
		
		Long loggedInUserId = serviceUtils.getCurrentlyLoggedInUserEntity().getId();
		if (type.equalsIgnoreCase("INCOME")) {
			totalSum = transactionRepository.getTotalIncomeTransactionsSum(loggedInUserId);
		}
		else if (type.equalsIgnoreCase("EXPENSE")) {
			totalSum = transactionRepository.getTotalExpenseTransactionsSum(loggedInUserId);
		}
		else if (type.equalsIgnoreCase("TRANSFER")) {
			totalSum = transactionRepository.getTotalTransferTransactionsSum(loggedInUserId);
		}
		else {
			throw new InvalidDataException("Transaction type is invalid!");
		}
		
		return totalSum;
	}
	
	/**
	 * Returns a page of transactions of a specified type's data
	 * (which were made between two dates - INCLUSIVE) 
	 * with the paging restriction 
	 * in the passed Pageable object.
	 * 
	 * Allowed transaction types (case-insensitive):
	 * - "INCOME"
	 * - "EXPENSE"
	 * - "TRANSFER"
	 */
	public Page<Transaction> getTransactionsBetweenDatesByTypeAndPageable(
		String type, 
		Date startDate, 
		Date endDate, 
		Pageable pageable
	)
		throws InvalidDataException
	{
		Page<TransactionEntity> transactionEntities = null;
		
		Long loggedInUserId = serviceUtils.getCurrentlyLoggedInUserEntity().getId();
		if (type.equalsIgnoreCase("INCOME")) {
			transactionEntities = transactionRepository.findAllIncomeTransactionsBetweenDates(loggedInUserId, startDate, endDate, pageable);
		}
		else if (type.equalsIgnoreCase("EXPENSE")) {
			transactionEntities = transactionRepository.findAllExpenseTransactionsBetweenDates(loggedInUserId, startDate, endDate, pageable);
		}
		else if (type.equalsIgnoreCase("TRANSFER")) {
			transactionEntities = transactionRepository.findAllTransferTransactionsBetweenDates(loggedInUserId, startDate, endDate, pageable);
		}
		else {
			throw new InvalidDataException("Transaction type is invalid!");
		}
		
		Page<Transaction> transactionDTOs = transactionEntities.map(transactionMapper::fromEntity);
		
		return transactionDTOs;
	}
	
	/**
	 * Returns the total sum of transactions of a specified type,
	 * which were made between two dates (inclusive).
	 * 
	 * Allowed transaction types (case-insensitive):
	 * - "INCOME"
	 * - "EXPENSE"
	 * - "TRANSFER"
	 */
	public Double getTotalTransactionsSumBetweenDatesByType(String type, Date startDate, Date endDate)
		throws InvalidDataException
	{
		Double totalSum = null;
		
		Long loggedInUserId = serviceUtils.getCurrentlyLoggedInUserEntity().getId();
		if (type.equalsIgnoreCase("INCOME")) {
			totalSum = transactionRepository.getTotalIncomeTransactionsSumBetweenDates(loggedInUserId, startDate, endDate);
		}
		else if (type.equalsIgnoreCase("EXPENSE")) {
			totalSum = transactionRepository.getTotalExpenseTransactionsSumBetweenDates(loggedInUserId, startDate, endDate);
		}
		else if (type.equalsIgnoreCase("TRANSFER")) {
			totalSum = transactionRepository.getTotalTransferTransactionsSumBetweenDates(loggedInUserId, startDate, endDate);
		}
		else {
			throw new InvalidDataException("Transaction type is invalid!");
		}
		
		return totalSum;
	}
	
	/**
	 * Returns a page of transactions' data
	 * with the specified from-type and from-ID
	 * and the paging restriction 
	 * in the passed Pageable object.
	 */
	public Page<Transaction> getTransactionsByFromDataAndPageable(TransactionFromType fromType, Long fromId, Pageable pageable) {
		UserEntity currentlyLoggedInUserEntity = serviceUtils.getCurrentlyLoggedInUserEntity();
		
		Page<TransactionEntity> transactionEntities = transactionRepository.findAllByUserAndFromTypeAndFromId(
			currentlyLoggedInUserEntity, 
			fromType, 
			fromId, 
			pageable
		);
		
		Page<Transaction> transactionDTOs = transactionEntities.map(transactionMapper::fromEntity);
		
		return transactionDTOs;
	}
	
	/**
	 * Returns a page of transactions' data
	 * with the specified to-type and to-ID
	 * and the paging restriction 
	 * in the passed Pageable object.
	 */
	public Page<Transaction> getTransactionsByToDataAndPageable(TransactionToType toType, Long toId, Pageable pageable) {
		UserEntity currentlyLoggedInUserEntity = serviceUtils.getCurrentlyLoggedInUserEntity();

		Page<TransactionEntity> transactionEntities = transactionRepository.findAllByUserAndToTypeAndToId(
			currentlyLoggedInUserEntity, 
			toType, 
			toId, 
			pageable
		);
		
		Page<Transaction> transactionDTOs = transactionEntities.map(transactionMapper::fromEntity);
		
		return transactionDTOs;
	}
	
	/**
	 * Returns an existing transaction's data.
	 */
	public Transaction getTransactionById(Long id) 
		throws TransactionNotFoundException, UserDoesNotOwnResourceException
	{
		boolean existsById = transactionRepository.existsById(id);
		if (!existsById) {
			throw new TransactionNotFoundException(id);
		}
		
		TransactionEntity transactionEntity = transactionRepository.findById(id).get();
		boolean belongsToCurrentlyLoggedInUser = serviceUtils.belongsToCurrentlyLoggedInUser(transactionEntity);
		if (!belongsToCurrentlyLoggedInUser) {
			throw new UserDoesNotOwnResourceException();
		}
		
		Transaction transactionDTO = transactionMapper.fromEntity(transactionEntity);
		
		return transactionDTO;
	}
	
	/**
	 * Creates a new transaction from a transaction DTO, 
	 * which contains the transaction's data
	 * and returns the newly created transaction's data.
	 */
	public Transaction createNewTransaction(TransactionRequest transactionRequest) 
		throws InvalidDataException
	{
		TransactionEntity transactionEntity = transactionMapper.toEntity(transactionRequest);
		
		// Owner is not included in the request DTO and must be set manually.
		// It is retrieved from the currently logged-in user's details.
		UserEntity owner = serviceUtils.getCurrentlyLoggedInUserEntity();
		transactionEntity.setUser(owner);
		
		boolean containsValidFromToData = containsValidFromToData(transactionRequest);
		if (!containsValidFromToData) {
			throw new InvalidDataException("Transaction contains invalid from-to data!");
		}
		
		// Make appropriate changes to affected accounts/categories
		executeTransactionFromRequest(transactionRequest);
		
		transactionEntity = transactionRepository.save(transactionEntity);
		
		Transaction transactionDTO = transactionMapper.fromEntity(transactionEntity);
		
		return transactionDTO;
	}
	
	/**
	 * Updates an existing transaction with a specified ID
	 * and returns the updated transaction's data.
	 */
	public Transaction updateTransactionById(Long id, TransactionRequest transactionRequest) 
		throws TransactionNotFoundException, UserDoesNotOwnResourceException
	{
		boolean existsById = transactionRepository.existsById(id);
		if (!existsById) {
			throw new TransactionNotFoundException(id);
		}
		
		TransactionEntity transactionEntity = transactionRepository.findById(id).get();
		boolean belongsToCurrentlyLoggedInUser = serviceUtils.belongsToCurrentlyLoggedInUser(transactionEntity);
		if (!belongsToCurrentlyLoggedInUser) {
			throw new UserDoesNotOwnResourceException();
		}
		
		TransactionEntity updatedTransactionEntity = transactionMapper.toEntity(transactionRequest);
		// ID and owner are not included in the request DTO and must be set manually.
		// ID is retrieved from the HTTP request's passed URL.
		// Owner is retrieved from the already existing entity.
		updatedTransactionEntity.setId(id);
		UserEntity transactionEntityOwner = transactionEntity.getUser();
		updatedTransactionEntity.setUser(transactionEntityOwner);
		
		boolean containsValidFromToData = containsValidFromToData(transactionRequest);
		if (!containsValidFromToData) {
			throw new InvalidDataException("Transaction contains invalid from-to data!");
		}
		
		// Undo changes made to accounts/categories by transaction
		undoTransactionExecution(transactionEntity);
		
		// Make appropriate new changes to affected accounts/categories
		executeTransactionFromRequest(transactionRequest);
		
		updatedTransactionEntity = transactionRepository.save(updatedTransactionEntity);
		
		Transaction transactionDTO = transactionMapper.fromEntity(updatedTransactionEntity);
		
		return transactionDTO;
	}
	
	/**
	 * Deletes an existing transaction with a specified ID
	 * and returns the deleted transaction's data.
	 */
	public Transaction deleteTransactionById(Long id) 
		throws TransactionNotFoundException, UserDoesNotOwnResourceException
	{
		boolean existsById = transactionRepository.existsById(id);
		if (!existsById) {
			throw new TransactionNotFoundException("Transaction with this ID does not exist!");
		}
		
		TransactionEntity transactionEntity = transactionRepository.findById(id).get();
		boolean belongsToCurrentlyLoggedInUser = serviceUtils.belongsToCurrentlyLoggedInUser(transactionEntity);
		if (!belongsToCurrentlyLoggedInUser) {
			throw new UserDoesNotOwnResourceException();
		}
		
		// Undo changes made to accounts/categories by transaction
		undoTransactionExecution(transactionEntity);
		
		Transaction transactionDTO = transactionMapper.fromEntity(transactionEntity);
		
		transactionRepository.deleteById(id);
		
		return transactionDTO;
	}
	
	/**
	 * Check if the transaction request references 
	 * valid existing from/to entities 
	 * which belong to the current user
	 * and contain valid income/expense/transfer data.
	 */
	private boolean containsValidFromToData(TransactionRequest transactionRequest) {
		String fromType = transactionRequest.getFromType().toString();
		String toType = transactionRequest.getToType().toString();
		Long fromId = transactionRequest.getFromId();
		Long toId = transactionRequest.getToId();
		
		/*
		 * 'From' & 'to' entities:
		 * - 1) must exist
		 * - 2) must belong to the current user
		 * - 3) must be activated (if they are accounts)
		 * - 4) must not both be categories (invalid transaction type)
		 *  
		 *  The first three are checked by the isValidEntityData() method.
		 */
		boolean fromEntityIsValid = isValidEntityData(fromId, fromType);
		boolean toEntityIsValid = isValidEntityData(toId, toType);
		boolean bothEntitiesAreCategories = fromType.equalsIgnoreCase("CATEGORY") && toType.equalsIgnoreCase("CATEGORY");
		if (!fromEntityIsValid || !toEntityIsValid || bothEntitiesAreCategories) {
			return false;
		}
		
		/*
		 * If the transaction is of type income, 
		 * its 'from' category must be of type INCOME.
		 * Otherwise the passed from-to data is invalid.
		 */
		boolean isIncomeTransaction = ( fromType.equalsIgnoreCase("CATEGORY") && toType.equalsIgnoreCase("ACCOUNT") );
		if (isIncomeTransaction) {
			CategoryEntity fromCategory = categoryRepository.findById(fromId).get();
			boolean isIncomeCategory = ( fromCategory.getType() == CategoryType.INCOME );
			if (!isIncomeCategory) {
				return false;
			}
		}
		
		/*
		 * If the transaction is of type expense, 
		 * its 'to' category must be of type EXPENSES.
		 * Otherwise the passed from-to data is invalid.
		 */
		boolean isExpenseTransaction = ( fromType.equalsIgnoreCase("ACCOUNT") && toType.equalsIgnoreCase("CATEGORY") );
		if (isExpenseTransaction) {
			CategoryEntity toCategory = categoryRepository.findById(toId).get();
			boolean isExpensesCategory = ( toCategory.getType() == CategoryType.EXPENSES );
			if (!isExpensesCategory) {
				return false;
			}
		}
		
		/*
		 * If the transaction is of type transfer, 
		 * its 'from' account's balance must be >= the sum
		 * in the transaction request.
		 * Otherwise the passed from-to data is invalid.
		 */
		boolean isTransferTransaction = ( fromType.equalsIgnoreCase("ACCOUNT") && toType.equalsIgnoreCase("ACCOUNT") );
		if (isTransferTransaction) {
			AccountEntity fromAccount = accountRepository.findById(fromId).get();
			Double balance = fromAccount.getBalance();
			Double transactionRequestSum = transactionRequest.getSum();
			if (balance < transactionRequestSum) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Executes a transaction 
	 * (updates accounts' balance & 
	 * categories' current period sum)
	 * from a transaction request 
	 * with valid from-to data.
	 */
	private void executeTransactionFromRequest(TransactionRequest request) {
		String fromType = request.getFromType().toString();
		String toType = request.getToType().toString();
		Long fromId = request.getFromId();
		Long toId = request.getToId();
		Double sum = request.getSum();
		
		boolean isIncomeTransaction = ( fromType.equalsIgnoreCase("CATEGORY") && toType.equalsIgnoreCase("ACCOUNT") );
		boolean isExpenseTransaction = ( fromType.equalsIgnoreCase("ACCOUNT") && toType.equalsIgnoreCase("CATEGORY") );
		boolean isTransferTransaction = ( fromType.equalsIgnoreCase("ACCOUNT") && toType.equalsIgnoreCase("ACCOUNT") );
		
		if (isIncomeTransaction) {
			accountService.performAccountBalanceOperationById(toId, '+', sum);
			categoryService.performCategorySumOperationById(fromId, '+', sum);
		}
		else if (isExpenseTransaction) {
			accountService.performAccountBalanceOperationById(fromId, '-', sum);
			categoryService.performCategorySumOperationById(toId, '+', sum);
		}
		else if (isTransferTransaction) {
			accountService.performAccountBalanceOperationById(fromId, '-', sum);
			accountService.performAccountBalanceOperationById(toId, '+', sum);
		}
	}
	
	/**
	 * Undoes the changes made 
	 * to accounts' balance & 
	 * categories' current period sum
	 * by an existing transaction.
	 */
	private void undoTransactionExecution(TransactionEntity transactionEntity) {
		String fromType = transactionEntity.getFromType().toString();
		String toType = transactionEntity.getToType().toString();
		Long fromId = transactionEntity.getFromId();
		Long toId = transactionEntity.getToId();
		Double sum = transactionEntity.getSum();
		
		boolean isIncomeTransaction = ( fromType.equalsIgnoreCase("CATEGORY") && toType.equalsIgnoreCase("ACCOUNT") );
		boolean isExpenseTransaction = ( fromType.equalsIgnoreCase("ACCOUNT") && toType.equalsIgnoreCase("CATEGORY") );
		boolean isTransferTransaction = ( fromType.equalsIgnoreCase("ACCOUNT") && toType.equalsIgnoreCase("ACCOUNT") );
		
		if (isIncomeTransaction) {
			accountService.performAccountBalanceOperationById(toId, '-', sum);
			categoryService.performCategorySumOperationById(fromId, '-', sum);
		}
		else if (isExpenseTransaction) {
			accountService.performAccountBalanceOperationById(fromId, '+', sum);
			categoryService.performCategorySumOperationById(toId, '-', sum);
		}
		else if (isTransferTransaction) {
			accountService.performAccountBalanceOperationById(fromId, '+', sum);
			accountService.performAccountBalanceOperationById(toId, '-', sum);
		}
	}
	
	/**
	 * Checks if the passed transaction request entity data is valid.
	 * 
	 * For the data to be valid, it must:
	 * - 1) point to an existing entity (an account or a category entity)
	 * - 2) point to an entity, which belongs to the currently logged-in user
	 * - 3) point to an activated entity (if it is an account entity)
	 */
	private boolean isValidEntityData(Long entityId, String entityType) {
		boolean entityExists = false;
		boolean entityBelongsToCurrentUser = false;
		
		if (entityType.equalsIgnoreCase("CATEGORY")) {
			entityExists = categoryRepository.existsById(entityId);
			if (!entityExists) { 
				return false; 
			}
			
			CategoryEntity entity = categoryRepository.findById(entityId).get();
			entityBelongsToCurrentUser = serviceUtils.belongsToCurrentlyLoggedInUser(entity);
			if (!entityBelongsToCurrentUser) {
				return false;
			}
		}
		else if (entityType.equalsIgnoreCase("ACCOUNT")) {
			entityExists = accountRepository.existsById(entityId);
			if (!entityExists) { 
				return false; 
			}
			
			AccountEntity entity = accountRepository.findById(entityId).get();
			entityBelongsToCurrentUser = serviceUtils.belongsToCurrentlyLoggedInUser(entity);
			if (!entityBelongsToCurrentUser) {
				return false;
			}
			
			boolean isActivatedAccount = ( entity.getType() == AccountType.ACTIVATED );
			if (!isActivatedAccount) { 
				return false;
			}
		}
		
		return true;
	}
	
}