package com.mse.personal.finance.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mse.personal.finance.db.entity.AccountEntity;
import com.mse.personal.finance.db.entity.UserEntity;
import com.mse.personal.finance.db.repository.AccountRepository;
import com.mse.personal.finance.db.repository.TransactionRepository;
import com.mse.personal.finance.model.Account;
import com.mse.personal.finance.model.AccountType;
import com.mse.personal.finance.model.TransactionFromType;
import com.mse.personal.finance.model.TransactionToType;
import com.mse.personal.finance.model.request.AccountRequest;
import com.mse.personal.finance.model.request.TransactionRequest;
import com.mse.personal.finance.rest.exception.AccountDeleteException;
import com.mse.personal.finance.rest.exception.AccountNotFoundException;
import com.mse.personal.finance.rest.exception.AccountTypeUpdateException;
import com.mse.personal.finance.rest.exception.NameAlreadyExistsException;
import com.mse.personal.finance.rest.exception.UserDoesNotOwnResourceException;
import com.mse.personal.finance.service.mapper.AccountMapper;

/**
 * Service for managing {@link Account} accounts
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
public class AccountService {

	private final AccountRepository accountRepository;
	private final AccountMapper accountMapper;
	private final ServiceUtils serviceUtils;
	private final TransactionService transactionService;
	private final CategoryService categoryService;
	private final TransactionRepository transactionRepository;
	
	private static final AccountEntity[] EXAMPLE_ACCOUNTS = {
		new AccountEntity("Спестовна", 0.0d, null, AccountType.ACTIVATED, null, null),
		new AccountEntity("Пари в брой", 0.0d, null, AccountType.ACTIVATED, null, null),
		new AccountEntity("Банкова сметка", 0.0d, null, AccountType.ACTIVATED, null, null)
	};

	@Autowired
	public AccountService(
		AccountRepository accountRepository, 
		AccountMapper accountMapper,
		ServiceUtils serviceUtils,
		TransactionService transactionService,
		CategoryService categoryService,
		TransactionRepository transactionRepository) 
	{
		this.accountRepository = accountRepository;
		this.accountMapper = accountMapper;
		this.serviceUtils = serviceUtils;
		this.transactionService = transactionService;
		this.categoryService = categoryService;
		this.transactionRepository = transactionRepository;
	}
	
	/**
	 * Returns all accounts' data as a list of accounts.
	 */
	public List<Account> getAllNonDeletedAccounts() {
		Long loggedInUserId = serviceUtils.getCurrentlyLoggedInUserEntity().getId();
		
		List<AccountEntity> accountEntities = accountRepository.findAllNonDeletedAccountsByOwner(loggedInUserId);
		
		List<Account> accountDTOs = accountEntities.stream()
			.map( accountEntity -> accountMapper.fromEntity(accountEntity) )
			.collect( Collectors.toList() );
		
		return accountDTOs;
	}
	
	/**
	 * Returns all accounts of a certain type's data as a list of accounts.
	 */
	public List<Account> getAllAccountsByType(AccountType type) {
		UserEntity currentlyLoggedInUserEntity = serviceUtils.getCurrentlyLoggedInUserEntity();
		
		List<AccountEntity> allAccountEntitiesByType = accountRepository.findAllByTypeAndOwner(type, currentlyLoggedInUserEntity);
		
		List<Account> allAccountDTOsByType = allAccountEntitiesByType.stream()
			.map( accountEntity -> accountMapper.fromEntity(accountEntity) )
			.collect( Collectors.toList() );
		
		return allAccountDTOsByType;
	}
	
	/**
	 * Returns the current sum of balances 
	 * for all activated accounts.
	 */
	public Double getTotalBalanceOfActivatedAccounts() {
		Long ownerId = serviceUtils.getCurrentlyLoggedInUserEntity().getId();
		Double totalBalance = accountRepository.getTotalBalanceOfActivatedAccounts(ownerId);
		return totalBalance;
	}
	
	/**
	 * Returns an existing account's data.
	 */
	public Account getAccountById(Long id) 
		throws AccountNotFoundException, UserDoesNotOwnResourceException
	{
		boolean existsById = accountRepository.existsById(id);
		if (!existsById) {
			throw new AccountNotFoundException(id);
		}
		
		AccountEntity accountEntity = accountRepository.findById(id).get();
		boolean belongsToCurrentlyLoggedInUser = serviceUtils.belongsToCurrentlyLoggedInUser(accountEntity);
		if (!belongsToCurrentlyLoggedInUser) {
			throw new UserDoesNotOwnResourceException();
		}
		
		Account accountDTO = accountMapper.fromEntity(accountEntity);
		
		return accountDTO;
	}
	
	/**
	 * Returns an existing account's total income sum between two dates (inclusive).
	 * 
	 * The sum is formed from both income transactions for the account
	 * and transfer transactions where the account is the recipient.
	 */
	public Double getTotalIncomeSumBetweenDatesById(Long id, Date startDate, Date endDate)
		throws AccountNotFoundException, UserDoesNotOwnResourceException
	{
		boolean existsById = accountRepository.existsById(id);
		if (!existsById) {
			throw new AccountNotFoundException(id);
		}
		
		AccountEntity accountEntity = accountRepository.findById(id).get();
		boolean belongsToCurrentlyLoggedInUser = serviceUtils.belongsToCurrentlyLoggedInUser(accountEntity);
		if (!belongsToCurrentlyLoggedInUser) {
			throw new UserDoesNotOwnResourceException();
		}
		
		Long loggedInUserId = serviceUtils.getCurrentlyLoggedInUserEntity().getId();
		Double totalIncomeSum = transactionRepository.getTotalIncomeSumBetweenDatesForAccountById(loggedInUserId, startDate, endDate, id);
		
		return totalIncomeSum;
	}
	
	/**
	 * Returns an existing account's total expense sum between two dates (inclusive).
	 * 
	 * The sum is formed from both expense transactions for the account
	 * and transfer transactions where the account is the sender.
	 */
	public Double getTotalExpenseSumBetweenDatesById(Long id, Date startDate, Date endDate)
		throws AccountNotFoundException, UserDoesNotOwnResourceException
	{
		boolean existsById = accountRepository.existsById(id);
		if (!existsById) {
			throw new AccountNotFoundException(id);
		}
		
		AccountEntity accountEntity = accountRepository.findById(id).get();
		boolean belongsToCurrentlyLoggedInUser = serviceUtils.belongsToCurrentlyLoggedInUser(accountEntity);
		if (!belongsToCurrentlyLoggedInUser) {
			throw new UserDoesNotOwnResourceException();
		}
		
		Long loggedInUserId = serviceUtils.getCurrentlyLoggedInUserEntity().getId();
		Double totalExpenseSum = transactionRepository.getTotalExpenseSumBetweenDatesForAccountById(loggedInUserId, startDate, endDate, id);
		
		return totalExpenseSum;
	}
	
	/**
	 * Creates a new account from an account DTO, 
	 * which contains the account's data
	 * and returns the newly created account's data.
	 */
	public Account createNewAccount(AccountRequest account)
		throws NameAlreadyExistsException 
	{
		AccountEntity accountEntity = accountMapper.toEntity(account);
		
		// Owner is not included in the request DTO and must be set manually.
		// It is retrieved from the currently logged-in user's details.
		UserEntity owner = serviceUtils.getCurrentlyLoggedInUserEntity();
		accountEntity.setOwner(owner);
		
		String accountName = account.getName();
		boolean nameAlreadyExists = accountRepository.existsByNameAndOwner(accountName, owner);
		if (nameAlreadyExists) {
			throw new NameAlreadyExistsException("Account with this name already exists for the current user!");
		}
		
		accountEntity = accountRepository.save(accountEntity);
		
		Account accountDTO = accountMapper.fromEntity(accountEntity);
		
		return accountDTO;
	}
	
	/**
	 * Updates an existing account with a specified ID 
	 * and returns the updated account's data.
	 */
	public Account updateAccountById(Long id, AccountRequest account) 
		throws AccountNotFoundException, UserDoesNotOwnResourceException
	{
		boolean existsById = accountRepository.existsById(id);
		if (!existsById) {
			throw new AccountNotFoundException(id);
		}
		 
		AccountEntity accountEntity = accountRepository.findById(id).get();
		boolean belongsToCurrentlyLoggedInUser = serviceUtils.belongsToCurrentlyLoggedInUser(accountEntity);
		if (!belongsToCurrentlyLoggedInUser) {
			throw new UserDoesNotOwnResourceException();
		}
		
		AccountEntity updatedAccountEntity = accountMapper.toEntity(account);
		
		// ID and owner are not included in the request DTO and must be set manually.
		// ID is retrieved from the HTTP request's passed URL.
		// Owner is retrieved from the already existing entity.
		updatedAccountEntity.setId(id);
		UserEntity accountEntityOwner = accountEntity.getOwner();
		updatedAccountEntity.setOwner(accountEntityOwner);
		
		String oldAccountName = accountEntity.getName();
		String newAccountName = updatedAccountEntity.getName();
		boolean nameChanged = !oldAccountName.equals(newAccountName);
		boolean nameAlreadyExists = accountRepository.existsByNameAndOwner(newAccountName, accountEntityOwner);
		if (nameChanged && nameAlreadyExists) {
			throw new NameAlreadyExistsException("Account with this name already exists for the current user!");
		}
		
		updatedAccountEntity = accountRepository.save(updatedAccountEntity);
		
		Account accountDTO = accountMapper.fromEntity(updatedAccountEntity);
		
		return accountDTO;
	}
	
	/**
	 * Deactivates an existing account with a specified ID
	 * and returns the deactivated account's data.
	 */
	public Account deactivateAccountById(Long id) 
		throws AccountNotFoundException, UserDoesNotOwnResourceException, AccountTypeUpdateException
	{
		boolean existsById = accountRepository.existsById(id);
		if (!existsById) {
			throw new AccountNotFoundException(id);
		}
		
		AccountEntity accountEntity = accountRepository.findById(id).get();
		boolean belongsToCurrentlyLoggedInUser = serviceUtils.belongsToCurrentlyLoggedInUser(accountEntity);
		if (!belongsToCurrentlyLoggedInUser) {
			throw new UserDoesNotOwnResourceException();
		}
		
		boolean accountDeleted = ( accountEntity.getType() == AccountType.DELETED );
		if (accountDeleted) {
			throw new AccountTypeUpdateException("Account is deleted and cannot be deactivated!");
		}
		
		accountEntity.setType(AccountType.DEACTIVATED);
		accountEntity = accountRepository.save(accountEntity);
		
		Account accountDTO = accountMapper.fromEntity(accountEntity);
		
		return accountDTO;
	}
	
	/**
	 * Activates an existing account with a specified ID
	 * and returns the activated account's data.
	 */
	public Account activateAccountById(Long id) 
		throws AccountNotFoundException, UserDoesNotOwnResourceException, AccountTypeUpdateException
	{
		boolean existsById = accountRepository.existsById(id);
		if (!existsById) {
			throw new AccountNotFoundException(id);
		}
		
		AccountEntity accountEntity = accountRepository.findById(id).get();
		boolean belongsToCurrentlyLoggedInUser = serviceUtils.belongsToCurrentlyLoggedInUser(accountEntity);
		if (!belongsToCurrentlyLoggedInUser) {
			throw new UserDoesNotOwnResourceException();
		}
		
		boolean accountDeleted = ( accountEntity.getType() == AccountType.DELETED );
		if (accountDeleted) {
			throw new AccountTypeUpdateException("Account is deleted and cannot be activated!");
		}
		
		accountEntity.setType(AccountType.ACTIVATED);
		accountEntity = accountRepository.save(accountEntity);
		
		Account accountDTO = accountMapper.fromEntity(accountEntity);

		return accountDTO;
	}
	
	/**
	 * Sets an existing account with a specified ID's
	 * goal and returns the updated account's data.
	 */
	public Account setAccountGoalById(Long id, Double goal) 
		throws AccountNotFoundException, UserDoesNotOwnResourceException
	{
		boolean existsById = accountRepository.existsById(id);
		if (!existsById) {
			throw new AccountNotFoundException(id);
		}
		
		AccountEntity accountEntity = accountRepository.findById(id).get();
		boolean belongsToCurrentlyLoggedInUser = serviceUtils.belongsToCurrentlyLoggedInUser(accountEntity);
		if (!belongsToCurrentlyLoggedInUser) {
			throw new UserDoesNotOwnResourceException();
		}
		
		accountEntity.setGoal(goal);
		accountEntity = accountRepository.save(accountEntity);
		
		Account accountDTO = accountMapper.fromEntity(accountEntity);

		return accountDTO;
	}
	
	/**
	 * Sets an existing account with a specified ID's
	 * balance and returns the updated account's data.
	 * 
	 * The balance update is done by creating and executing
	 * an income/expense transaction, depending on 
	 * whether the new balance is greater or less than 
	 * the account's current balance.
	 */
	public Account setAccountBalanceById(Long id, Double balance) 
		throws AccountNotFoundException, UserDoesNotOwnResourceException
	{
		boolean existsById = accountRepository.existsById(id);
		if (!existsById) {
			throw new AccountNotFoundException(id);
		}
		
		AccountEntity accountEntity = accountRepository.findById(id).get();
		boolean belongsToCurrentlyLoggedInUser = serviceUtils.belongsToCurrentlyLoggedInUser(accountEntity);
		if (!belongsToCurrentlyLoggedInUser) {
			throw new UserDoesNotOwnResourceException();
		}
		
		Double currentBalance = accountEntity.getBalance();
		UserEntity currentlyLoggedInUser = serviceUtils.getCurrentlyLoggedInUserEntity();
		if (balance > currentBalance) {
			// Create & execute income transaction
			TransactionRequest transactionRequest = new TransactionRequest();
			transactionRequest.setDateOfCompletion( new Date() /* current date */);
			transactionRequest.setFromType( TransactionFromType.CATEGORY );
			transactionRequest.setFromId( categoryService.getSystemIncomeCategoryForUser(currentlyLoggedInUser).getId() );
			transactionRequest.setToType( TransactionToType.ACCOUNT );
			transactionRequest.setToId( id /* id of current account */ );
			transactionRequest.setSum( balance - currentBalance );
			transactionRequest.setRecurring("NO");
			transactionRequest.setDescription("Account balance sync");
			transactionRequest.setShouldBeAutomaticallyExecuted(false);
			transactionService.createNewTransaction(transactionRequest);
		}
		else if (balance < currentBalance) {
			// Create & execute expense transaction
			TransactionRequest transactionRequest = new TransactionRequest();
			transactionRequest.setDateOfCompletion( new Date() /* current date */);
			transactionRequest.setFromType( TransactionFromType.ACCOUNT );
			transactionRequest.setFromId( id /* id of current account */ );
			transactionRequest.setToType( TransactionToType.CATEGORY );
			transactionRequest.setToId( categoryService.getSystemExpensesCategoryForUser(currentlyLoggedInUser).getId() );
			transactionRequest.setSum( currentBalance - balance );
			transactionRequest.setRecurring("NO");
			transactionRequest.setDescription("Account balance sync");
			transactionRequest.setShouldBeAutomaticallyExecuted(false);
			transactionService.createNewTransaction(transactionRequest);
		}
		
		// Retrieve entity again after the transaction has been completed
		accountEntity = accountRepository.findById(id).get();
		
		Account accountDTO = accountMapper.fromEntity(accountEntity);

		return accountDTO;
	}
	
	/**
	 * Deletes an existing account with a specified ID
	 * and returns the deleted account's data.
	 */
	public Account deleteAccountById(Long id) 
		throws AccountNotFoundException, UserDoesNotOwnResourceException, AccountDeleteException
	{
		boolean existsById = accountRepository.existsById(id);
		if (!existsById) {
			throw new AccountNotFoundException(id);
		}
		
		AccountEntity accountEntity = accountRepository.findById(id).get();
		boolean belongsToCurrentlyLoggedInUser = serviceUtils.belongsToCurrentlyLoggedInUser(accountEntity);
		if (!belongsToCurrentlyLoggedInUser) {
			throw new UserDoesNotOwnResourceException();
		}
		
		boolean accountAlreadyDeleted = ( accountEntity.getType() == AccountType.DELETED );
		if (accountAlreadyDeleted) {
			throw new AccountDeleteException("Account has already been deleted!");
		}
		
		Double accountBalance = accountEntity.getBalance();
		if (accountBalance != 0) {
			throw new AccountDeleteException("Account balance must be zero to delete account!");
		}
		
		accountEntity.setType(AccountType.DELETED);
		accountEntity = accountRepository.save(accountEntity);
		
		Account accountDTO = accountMapper.fromEntity(accountEntity);
		
		return accountDTO;
	}
	
	/**
	 * Creates example accounts for the currently logged-in user
	 * and returns a list of the created accounts' data.
	 */
	public List<Account> createExampleAccountsForCurrentlyLoggedInUser() {
		List<Account> createdAccounts = new ArrayList<>();
		
		for (AccountEntity accountEntity : EXAMPLE_ACCOUNTS) {
			UserEntity owner = serviceUtils.getCurrentlyLoggedInUserEntity();
			String name = accountEntity.getName();
			
			boolean nameAlreadyExists = accountRepository.existsByNameAndOwner(name, owner);
			if (nameAlreadyExists) {
				continue;
			}

			accountEntity.setOwner(owner);

			accountRepository.save(accountEntity);
			
			Account accountDTO = accountMapper.fromEntity(accountEntity);
			createdAccounts.add(accountDTO);
		}
		
		return createdAccounts;
	}
	
	/**
	 * Updates an existing account's balance by either adding to it or subtracting the passed sum.
	 * The valid operation types are '+' and '-' (for adding and subtracting respectively).
	 */
	protected void performAccountBalanceOperationById(Long accountId, char operationType, Double sum) {
		AccountEntity accountEntity = accountRepository.findById(accountId).get();
		Double oldBalance = accountEntity.getBalance();
		
		Double newBalance = oldBalance;
		switch (operationType) {
		case '+':
			newBalance += sum;
			break;
		case '-':
			newBalance -= sum;
			break;
		}
		
		accountEntity.setBalance(newBalance);
		accountRepository.save(accountEntity);
	}
	
	/**
	 * Returns the number of accounts in the example accounts array.
	 */
	public int getNumberOfExampleAccounts() {
		return EXAMPLE_ACCOUNTS.length;
	}
	
	/**
	 * Returns the example accounts array.
	 */
	public AccountEntity[] getExampleAccounts() {
		return EXAMPLE_ACCOUNTS;
	}
	
}