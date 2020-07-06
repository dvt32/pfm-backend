package com.mse.personal.finance.rest;

import java.util.Date;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mse.personal.finance.model.Account;
import com.mse.personal.finance.model.AccountType;
import com.mse.personal.finance.model.request.AccountRequest;
import com.mse.personal.finance.rest.exception.AccountDeleteException;
import com.mse.personal.finance.rest.exception.AccountNotFoundException;
import com.mse.personal.finance.rest.exception.AccountTypeUpdateException;
import com.mse.personal.finance.rest.exception.InvalidDataException;
import com.mse.personal.finance.rest.exception.NameAlreadyExistsException;
import com.mse.personal.finance.rest.exception.UserDoesNotOwnResourceException;
import com.mse.personal.finance.service.AccountService;

/**
 * REST controller for CRUD operations 
 * upon {@link Account} accounts
 * for the currently logged-in user.
 * 
 * @author dvt32
 */
@RestController
@RequestMapping(path = "/accounts")
public class AccountController {
	
	private final AccountService accountService;

	@Autowired
	public AccountController(AccountService accountService) {
		this.accountService = accountService;
	}
	
	/**
	 * Returns all non-deleted accounts' data as a JSON array.
	 * 
	 * @return the list of accounts (empty if no accounts available)
	 */
	@GetMapping
	public List<Account> getAllNonDeletedAccounts() {
		List<Account> accounts = accountService.getAllNonDeletedAccounts();
		return accounts;
	}
	
	/**
	 * Returns all accounts of the specified type's data as a JSON array.
	 * 
	 * @return the list of accounts (empty if no accounts available)
	 */
	@GetMapping(params = "type")
	public List<Account> getAllAccountsByType(@RequestParam AccountType type) {
		List<Account> allAccountsByType = accountService.getAllAccountsByType(type);
		return allAccountsByType;
	}
	
	/**
	 * Returns the current sum of balances for all activated accounts.
	 * 
	 * @return the current sum of balances
	 */
	@GetMapping("/balance-sum")
	public Double getTotalBalanceOfActivatedAccounts() {
		Double totalBalance = accountService.getTotalBalanceOfActivatedAccounts();
		return totalBalance;
	}
	
	/**
	 * Returns a specific account's data as a JSON object
	 * 
	 * @param id The id of the account to be retrieved
	 * @return the account's data
	 */
	@GetMapping("/{id}")
	public Account getAccountById(@PathVariable Long id) {
		Account account = null;
		
		try {
			account = accountService.getAccountById(id);
		} 
		catch (AccountNotFoundException e) {
			throw new AccountNotFoundException(id);
		} 
		catch (UserDoesNotOwnResourceException e) {
			throw new UserDoesNotOwnResourceException();
		}
		
		return account;
	}
	
	/**
	 * Returns the total income sum 
	 * (between two dates - inclusive)
	 * for a specified account.
	 * 
	 * The sum is formed from both income transactions for the account
	 * and transfer transactions where the account is the recipient.
	 * 
	 * @return the total income sum
	 */
	@GetMapping("/{id}/income-sum")
	public Double getTotalIncomeSumBetweenDatesById(
		@PathVariable Long id,
		@RequestParam @DateTimeFormat(pattern = "dd.MM.yyyy") Date startDate,
		@RequestParam @DateTimeFormat(pattern = "dd.MM.yyyy") Date endDate)
	{
		Double totalIncomeSum = null;
		
		try {
			totalIncomeSum = accountService.getTotalIncomeSumBetweenDatesById(id, startDate, endDate);
		}
		catch (AccountNotFoundException e) {
			throw new AccountNotFoundException(id);
		} 
		catch (UserDoesNotOwnResourceException e) {
			throw new UserDoesNotOwnResourceException();
		}
		
		return totalIncomeSum;
	}
	
	/**
	 * Returns the total expense sum 
	 * (between two dates - inclusive)
	 * for a specified account.
	 * 
	 * The sum is formed from both expense transactions for the account
	 * and transfer transactions where the account is the sender.
	 * 
	 * @return the total expense sum
	 */
	@GetMapping("/{id}/expense-sum")
	public Double getTotalExpenseSumBetweenDatesById(
		@PathVariable Long id,
		@RequestParam @DateTimeFormat(pattern = "dd.MM.yyyy") Date startDate,
		@RequestParam @DateTimeFormat(pattern = "dd.MM.yyyy") Date endDate)
	{
		Double totalExpenseSum = null;
		
		try {
			totalExpenseSum = accountService.getTotalExpenseSumBetweenDatesById(id, startDate, endDate);
		}
		catch (AccountNotFoundException e) {
			throw new AccountNotFoundException(id);
		} 
		catch (UserDoesNotOwnResourceException e) {
			throw new UserDoesNotOwnResourceException();
		}
		
		return totalExpenseSum;
	}
	
	/**
	 * This method creates an account by passing the account's data in a POST request's body 
	 * and returns the newly created account's data as a JSON object.
	 * 
	 * The data is validated before the account is stored in the database.
	 * 
	 * @param account An object containing the account-to-be-created's data
	 * @param bindingResult The validator of the passed data
	 * 
	 * @return the created account's data
	 */
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Account createAccount(
		@RequestBody @Valid AccountRequest account, 
		BindingResult bindingResult) 
	{
		if (bindingResult.hasErrors()) {
			String errorMessageFromBindingResult = ControllerUtils.getErrorMessageFromBindingResult(bindingResult);
			throw new InvalidDataException(errorMessageFromBindingResult);
		}
		
		Account createdAccount;
		try {
			createdAccount = accountService.createNewAccount(account);
		} 
		catch (NameAlreadyExistsException e) {
			String exceptionMessage = e.getMessage();
			throw new NameAlreadyExistsException(exceptionMessage);
		}
		
		return createdAccount;
	}
	
	/**
	 * This method creates example accounts for the currently logged-in user
	 * and returns the created accounts' data as a JSON array.
	 */
	@PostMapping("/create-example-accounts")
	@ResponseStatus(HttpStatus.CREATED)
	public List<Account> createExampleAccountsForCurrentlyLoggedInUser() {
		List<Account> createdAccounts = accountService.createExampleAccountsForCurrentlyLoggedInUser();
		return createdAccounts;
	}
	
	/**
	 * This method updates an existing account in the database 
	 * by passing the updated account's data in a PUT request's body
	 * and returns the updated account's data as a JSON object.
	 * 
	 * The data is validated before the account is updated in the database.
	 * 
	 * @param id The account-to-be-updated's ID
	 * @param account An object containing the account-to-be-updated's data
	 * @param bindingResult The validator of the passed data
	 * 
	 * @return the updated account's data
	 */
	@PutMapping("/{id}")
	public Account updateAccountById(
		@PathVariable Long id, 
		@RequestBody @Valid AccountRequest account, 
		BindingResult bindingResult) 
	{
		if (bindingResult.hasErrors()) {
			String errorMessageFromBindingResult = ControllerUtils.getErrorMessageFromBindingResult(bindingResult);
			throw new InvalidDataException(errorMessageFromBindingResult);
		}
		
		Account updatedAccount = null;
		try {
			updatedAccount = accountService.updateAccountById(id, account);
		} 
		catch (AccountNotFoundException e) {
			throw new AccountNotFoundException(id);
		}
		catch (UserDoesNotOwnResourceException e) {
			throw new UserDoesNotOwnResourceException();
		}
		catch (NameAlreadyExistsException e) {
			String exceptionMessage = e.getMessage();
			throw new NameAlreadyExistsException(exceptionMessage);
		}
		
		return updatedAccount;
	}
	
	/**
	 * This method deactivates an existing account in the database by passing an ID
	 * and returns the deactivated account's data as a JSON object.
	 * 
	 * If the account does not exist, an exception is thrown.
	 * 
	 * @param id The ID of the account
	 * @return the deactivated account's data
	 */
	@PatchMapping("/{id}/deactivate")
	public Account deactivateAccountById(@PathVariable Long id) {
		Account deactivatedAccount = null;
		
		try {
			deactivatedAccount = accountService.deactivateAccountById(id);
		} 
		catch (AccountNotFoundException e) {
			throw new AccountNotFoundException(id);
		}
		catch (UserDoesNotOwnResourceException e) {
			throw new UserDoesNotOwnResourceException();
		}
		catch (AccountTypeUpdateException e) {
			String exceptionMessage = e.getMessage();
			throw new AccountTypeUpdateException(exceptionMessage);
		}
		
		return deactivatedAccount;
	}
	
	/**
	 * This method activates an existing account in the database by passing an ID
	 * and returns the activated account's data as a JSON object.
	 * 
	 * If the account does not exist, an exception is thrown.
	 * 
	 * @param id The ID of the account
	 * @return the activated account's data
	 */
	@PatchMapping("/{id}/activate")
	public Account activateAccountById(@PathVariable Long id) {
		Account activatedAccount = null;
		
		try {
			activatedAccount = accountService.activateAccountById(id);
		} 
		catch (AccountNotFoundException e) {
			throw new AccountNotFoundException(id);
		}
		catch (UserDoesNotOwnResourceException e) {
			throw new UserDoesNotOwnResourceException();
		}
		catch (AccountTypeUpdateException e) {
			String exceptionMessage = e.getMessage();
			throw new AccountTypeUpdateException(exceptionMessage);
		}
		
		return activatedAccount;
	}
	
	/**
	 * This method sets an existing account's goal in the database 
	 * by passing an ID and the goal amount (as a request param)
	 * and returns the updated account's data as a JSON object.
	 * 
	 * @param id The ID of the account
	 * @param goal the goal amount
	 * @return the updated account's data
	 */
	@PatchMapping(value = "/{id}", params = "goal")
	public Account setAccountGoalById(
		@PathVariable Long id, 
		@RequestParam Double goal) 
	{
		Account accountWithSetGoal = null;
		
		try {
			accountWithSetGoal = accountService.setAccountGoalById(id, goal);
		} 
		catch (AccountNotFoundException e) {
			throw new AccountNotFoundException(id);
		}
		catch (UserDoesNotOwnResourceException e) {
			throw new UserDoesNotOwnResourceException();
		}
		
		return accountWithSetGoal;
	}
	
	/**
	 * This method sets an existing account's balance in the database 
	 * by passing an ID and the balance (as a request param)
	 * and returns the updated account's data as a JSON object.
	 * 
	 * @param id The ID of the account
	 * @param balance the balance
	 * @return the updated account's data
	 */
	@PatchMapping(value = "/{id}", params = "balance")
	public Account setAccountBalanceById(
		@PathVariable Long id, 
		@RequestParam Double balance) 
	{
		Account accountWithUpdatedBalance = null;
		
		try {
			accountWithUpdatedBalance = accountService.setAccountBalanceById(id, balance);
		} 
		catch (AccountNotFoundException e) {
			throw new AccountNotFoundException(id);
		}
		catch (UserDoesNotOwnResourceException e) {
			throw new UserDoesNotOwnResourceException();
		}
		
		return accountWithUpdatedBalance;
	}
	
	/**
	 * This method deletes an existing account in the database by passing an ID
	 * and returns the deleted account's data as a JSON object.
	 * 
	 * If the account does not exist, an exception is thrown.
	 * 
	 * @param id The ID of the account
	 * @return the deleted account's data
	 */
	@DeleteMapping("/{id}")
	public Account deleteAccountById(@PathVariable Long id) {
		Account deletedAccount = null;
		
		try {
			deletedAccount = accountService.deleteAccountById(id);
		} 
		catch (AccountNotFoundException e) {
			throw new AccountNotFoundException(id);
		}
		catch (UserDoesNotOwnResourceException e) {
			throw new UserDoesNotOwnResourceException();
		}
		catch (AccountDeleteException e) {
			String exceptionMessage = e.getMessage();
			throw new AccountDeleteException(exceptionMessage);
		}
		
		return deletedAccount;
	}

}