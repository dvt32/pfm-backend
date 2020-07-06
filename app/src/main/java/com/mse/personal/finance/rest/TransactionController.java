package com.mse.personal.finance.rest;

import java.util.Date;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mse.personal.finance.model.Transaction;
import com.mse.personal.finance.model.TransactionFromType;
import com.mse.personal.finance.model.TransactionToType;
import com.mse.personal.finance.model.request.TransactionRequest;
import com.mse.personal.finance.rest.exception.InvalidDataException;
import com.mse.personal.finance.rest.exception.TransactionNotFoundException;
import com.mse.personal.finance.rest.exception.UserDoesNotOwnResourceException;
import com.mse.personal.finance.service.TransactionService;

/**
 * REST controller for CRUD operations 
 * upon {@link Transaction} transactions
 * for the currently logged-in user.
 * 
 * NOTE: 
 * 	Methods with a Pageable param allow applying a paging restriction. 
 * 	The restriction can be applied by passing request parameters in the GET request:
 * 	- "page" (the number of the page to be returned, numbering is zero-based)
 * 	- "size" (number of elements on the page)
 * 	- "sort" (the order of the returned elements).
 * 
 * @author dvt32
 */
@RestController
@RequestMapping(path = "/transactions")
public class TransactionController {
	
	private final TransactionService transactionService;

	@Autowired
	public TransactionController(TransactionService transactionService) {
		this.transactionService = transactionService;
	}
	
	/**
	 * Returns a page of transactions data as a JSON object
	 * by passing a paging restriction in a GET request.
	 * 
	 * @return a page of transactions' data
	 */
	@GetMapping
	public Page<Transaction> getTransactionsByPageable(Pageable pageable) {
		Page<Transaction> pageOfTransactions = transactionService.getTransactionsByPageable(pageable);
		return pageOfTransactions;
	}
	
	/**
	 * Returns a page of transactions of a specified type's data as a JSON object
	 * by passing a paging restriction in a GET request.
	 * 
	 * Allowed transaction types (case-insensitive):
	 * - "INCOME"
	 * - "EXPENSE"
	 * - "TRANSFER"
	 * 
	 * @return a page of transactions' data
	 */
	@GetMapping(params = "type")
	public Page<Transaction> getTransactionsByTypeAndPageable(
		@RequestParam String type, 
		Pageable pageable) 
	{
		Page<Transaction> pageOfTransactions = null;
		
		try {
			pageOfTransactions = transactionService.getTransactionsByTypeAndPageable(type, pageable);
		}
		catch (InvalidDataException e) {
			String exceptionMessage = e.getMessage();
			throw new InvalidDataException(exceptionMessage);
		}
		
		return pageOfTransactions;
	}
	
	/**
	 * Returns the total sum for transactions of a specified type.
	 * 
	 * Allowed transaction types (case-insensitive):
	 * - "INCOME"
	 * - "EXPENSE"
	 * - "TRANSFER"
	 * 
	 * @return the transactions' total sum
	 */
	@GetMapping("/total-sum")
	public Double getTotalTransactionsSumByType(@RequestParam String type) {
		Double totalSum = null;
		
		try {
			totalSum = transactionService.getTotalTransactionsSumByType(type);
		}
		catch (InvalidDataException e) {
			String exceptionMessage = e.getMessage();
			throw new InvalidDataException(exceptionMessage);
		}
		
		return totalSum;
	}
	
	/**
	 * Returns a page of transactions of a specified type's data 
	 * (which were made between two dates - INCLUSIVE), as a JSON object
	 * by passing a paging restriction in a GET request.
	 * 
	 * Allowed transaction types (case-insensitive):
	 * - "INCOME"
	 * - "EXPENSE"
	 * - "TRANSFER"
	 * 
	 * @return a page of transactions' data
	 */
	@GetMapping("/between-dates")
	public Page<Transaction> getTransactionsBetweenDatesByTypeAndPageable(
		@RequestParam String type,
		@RequestParam @DateTimeFormat(pattern = "dd.MM.yyyy") Date startDate,
		@RequestParam @DateTimeFormat(pattern = "dd.MM.yyyy") Date endDate,
		Pageable pageable) 
	{
		Page<Transaction> pageOfTransactions = null;
		
		try {
			pageOfTransactions = transactionService.getTransactionsBetweenDatesByTypeAndPageable(type, startDate, endDate, pageable);
		}
		catch (InvalidDataException e) {
			String exceptionMessage = e.getMessage();
			throw new InvalidDataException(exceptionMessage);
		}
		
		return pageOfTransactions;
	}
	
	/**
	 * Returns the total sum for transactions of a specified type,
	 * which were made between two dates (inclusive).
	 * 
	 * Allowed transaction types (case-insensitive):
	 * - "INCOME"
	 * - "EXPENSE"
	 * - "TRANSFER"
	 * 
	 * @return the transactions' total sum
	 */
	@GetMapping("/total-sum-between-dates")
	public Double getTotalTransactionsSumBetweenDatesByType(
		@RequestParam String type,
		@RequestParam @DateTimeFormat(pattern = "dd.MM.yyyy") Date startDate,
		@RequestParam @DateTimeFormat(pattern = "dd.MM.yyyy") Date endDate) 
	{
		Double totalSum = null;
		
		try {
			totalSum = transactionService.getTotalTransactionsSumBetweenDatesByType(type, startDate, endDate);
		}
		catch (InvalidDataException e) {
			String exceptionMessage = e.getMessage();
			throw new InvalidDataException(exceptionMessage);
		}
		
		return totalSum;
	}
	
	/**
	 * Returns a page of transactions with the passed from-type & from-ID.
	 * 
	 * @param fromType the transactions-to-be-retrieved's from-type
	 * @param fromId the transaction-to-be-retrieved's from-ID
	 * 
	 * @return a page of transactions' data
	 */
	@GetMapping("/by-from-data")
	public Page<Transaction> getTransactionsByFromDataAndPageable(
		@RequestParam TransactionFromType fromType,
		@RequestParam Long fromId,
		Pageable pageable) 
	{
		Page<Transaction> pageOfTransactions = transactionService.getTransactionsByFromDataAndPageable(fromType, fromId, pageable);
		return pageOfTransactions;
	}
	
	/**
	 * Returns a page of transactions with the passed to-type & to-ID.
	 * 
	 * @param toType the transactions-to-be-retrieved's to-type
	 * @param toId the transaction-to-be-retrieved's to-ID
	 * 
	 * @return a page of transactions' data
	 */
	@GetMapping("/by-to-data")
	public Page<Transaction> getTransactionsByToDataAndPageable(
		@RequestParam TransactionToType toType,
		@RequestParam Long toId,
		Pageable pageable) 
	{
		Page<Transaction> pageOfTransactions = transactionService.getTransactionsByToDataAndPageable(toType, toId, pageable);	
		return pageOfTransactions;
	}
	
	/**
	 * Returns a specific transaction's data as a JSON object
	 * 
	 * @param id The id of the transaction to be retrieved
	 * @return the transaction's data
	 */
	@GetMapping("/{id}")
	public Transaction getTransactionById(@PathVariable Long id) {
		Transaction transaction = null;
		
		try {
			transaction = transactionService.getTransactionById(id);
		}
		catch (TransactionNotFoundException e) {
			throw new TransactionNotFoundException(id);
		}
		catch (UserDoesNotOwnResourceException e) {
			throw new UserDoesNotOwnResourceException();
		}
		
		return transaction;
	}
	
	/**
	 * This method creates a transaction by passing the transaction's data in a POST request's body
	 * and returns the newly created transaction's data as a JSON object.
	 * 
	 * The data is validated before the transaction is stored in the database.
	 * 
	 * @param transaction An object containing the transaction-to-be-created's data
	 * @param bindingResult The validator of the passed data
	 * 
	 * @return the created transaction's data
	 */
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Transaction createTransaction(
		@RequestBody @Valid TransactionRequest transaction, 
		BindingResult bindingResult)
	{
		if (bindingResult.hasErrors()) {
			String errorMessageFromBindingResult = ControllerUtils.getErrorMessageFromBindingResult(bindingResult);
			throw new InvalidDataException(errorMessageFromBindingResult);
		}
		
		Transaction createdTransaction = null;
		try {
			createdTransaction = transactionService.createNewTransaction(transaction);
		}
		catch (InvalidDataException e) {
			String exceptionMessage = e.getMessage();
			throw new InvalidDataException(exceptionMessage);
		}
		
		return createdTransaction;
	}
	
	/**
	 * This method updates an existing transaction in the database 
	 * by passing the updated transaction's data in a POST request's body
	 * and returns the updated transaction's data as a JSON object.
	 * 
	 * The data is validated before the transaction is updated in the database.
	 * 
	 * @param id The transaction-to-be-updated's ID
	 * @param transaction An object containing the transaction-to-be-updated's data
	 * @param bindingResult The validator of the passed data
	 * 
	 * @return the updated transaction's data
	 */
	@PutMapping("/{id}")
	public Transaction updateTransactionById(
		@PathVariable Long id, 
		@RequestBody @Valid TransactionRequest transaction, 
		BindingResult bindingResult) 
	{
		if (bindingResult.hasErrors()) {
			String errorMessageFromBindingResult = ControllerUtils.getErrorMessageFromBindingResult(bindingResult);
			throw new InvalidDataException(errorMessageFromBindingResult);
		}
		
		Transaction updatedTransaction = null;
		try {
			updatedTransaction = transactionService.updateTransactionById(id, transaction);
		} 
		catch (TransactionNotFoundException e) {
			throw new TransactionNotFoundException(id);
		}
		catch (UserDoesNotOwnResourceException e) {
			throw new UserDoesNotOwnResourceException();
		}
		catch (InvalidDataException e) {
			String exceptionMessage = e.getMessage();
			throw new InvalidDataException(exceptionMessage);
		}
		
		return updatedTransaction;
	}
	
	/**
	 * This method deletes an existing transaction in the database by passing an ID
	 * and returns the deleted transaction's data as a JSON object.
	 * 
	 * If the transaction does not exist, an exception is thrown.
	 * 
	 * @param id The ID of the transaction
	 * @return the deleted transaction's data
	 */
	@DeleteMapping("/{id}")
	public Transaction deleteTransactionById(@PathVariable Long id) {
		Transaction deletedTransaction = null;
		
		try {
			deletedTransaction = transactionService.deleteTransactionById(id);
		} 
		catch (TransactionNotFoundException e) {
			throw new TransactionNotFoundException(id);
		}
		catch (UserDoesNotOwnResourceException e) {
			throw new UserDoesNotOwnResourceException();
		}
		
		return deletedTransaction;
	}

}