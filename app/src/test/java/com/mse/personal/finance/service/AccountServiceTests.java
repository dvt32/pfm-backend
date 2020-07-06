package com.mse.personal.finance.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.mse.personal.finance.db.entity.AccountEntity;
import com.mse.personal.finance.db.entity.CategoryEntity;
import com.mse.personal.finance.db.entity.TransactionEntity;
import com.mse.personal.finance.db.entity.UserEntity;
import com.mse.personal.finance.db.repository.AccountRepository;
import com.mse.personal.finance.db.repository.CategoryRepository;
import com.mse.personal.finance.db.repository.TransactionRepository;
import com.mse.personal.finance.db.repository.UserRepository;
import com.mse.personal.finance.model.Account;
import com.mse.personal.finance.model.AccountType;
import com.mse.personal.finance.model.CategoryType;
import com.mse.personal.finance.model.FamilyStatusType;
import com.mse.personal.finance.model.GenderType;
import com.mse.personal.finance.model.TransactionFromType;
import com.mse.personal.finance.model.TransactionToType;
import com.mse.personal.finance.model.request.AccountRequest;
import com.mse.personal.finance.rest.exception.AccountDeleteException;
import com.mse.personal.finance.rest.exception.AccountNotFoundException;
import com.mse.personal.finance.rest.exception.AccountTypeUpdateException;
import com.mse.personal.finance.rest.exception.InvalidDataException;
import com.mse.personal.finance.rest.exception.NameAlreadyExistsException;
import com.mse.personal.finance.rest.exception.UserDoesNotOwnResourceException;

/**
 * This class implements unit tests for the AccountService class.
 * 
 * The @Transactional annotation is used to rollback 
 * database changes after a test has finished executing.
 * 
 * @author dvt32
 */
@RunWith(SpringRunner.class)
@Transactional
@SpringBootTest
@ActiveProfiles("test")
public class AccountServiceTests {
	
	private static final String TEST_USER_EMAIL = "test@pfm.com";
	private static final String TEST_USER_PASSWORD = "123456";
	
	@Autowired
	private AccountService accountService;
	
	@Autowired
	private AccountRepository accountRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private CategoryRepository categoryRepository;
	
	@Autowired
	private TransactionRepository transactionRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	/**
	 * Insert test user and return his data.
	 */
	public UserEntity insertTestUser(String email) {
    	UserEntity testUser = new UserEntity(
			"John Doe",
			passwordEncoder.encode(TEST_USER_PASSWORD), 
			email,  
			GenderType.MALE,
			FamilyStatusType.SINGLE,
			24,
			"Master's Degree"
		);
    	
    	testUser = userRepository.save(testUser);
    	
    	return testUser;
	}
	
	/**
	 * Inserts a test account for the specified user and returns that account's data
	 */
	public AccountEntity insertTestAccountForUser(UserEntity user, AccountType accountType, String accountName) {
		AccountEntity testAccountEntity = new AccountEntity(
			accountName,
			1337.37d,
			2000.00d,
			accountType,
			user,
			null
		);
		
		testAccountEntity = accountRepository.save(testAccountEntity);
		
		return testAccountEntity;
	}
	
	/**
	 * Create and return a test account request
	 */
	public AccountRequest getTestAccountRequest() {
		AccountRequest testAccountRequest = new AccountRequest();
		
		testAccountRequest.setName("Account 1");
		testAccountRequest.setBalance(1337.37d);
		testAccountRequest.setGoal(2000.00d);
		testAccountRequest.setType(AccountType.ACTIVATED);
    	
		return testAccountRequest;
	}
	
	/**
	 * Inserts a test category for the specified user and returns that category's data
	 */
	public CategoryEntity insertTestCategoryForUser(UserEntity user, CategoryType categoryType, String categoryName) {
		CategoryEntity testCategoryEntity = new CategoryEntity(
			categoryName,
			categoryType,
			150.00d,
			"limit",
			user
		);
		
		testCategoryEntity = categoryRepository.save(testCategoryEntity);
		
		return testCategoryEntity;
	}
	
	/**
	 * Inserts a test transaction for the specified user and returns that transaction's data
	 */
	public TransactionEntity insertTestTransactionForUser(
		UserEntity owner, 
		String type, 
		Date date,
		String description,
		Long fromId,
		Long toId) 
	{
		TransactionEntity testTransactionEntity = new TransactionEntity();
		
		testTransactionEntity.setDateOfCompletion(date);
		switch (type) {
		case "INCOME":
			testTransactionEntity.setFromType(TransactionFromType.CATEGORY);
			testTransactionEntity.setToType(TransactionToType.ACCOUNT);
			break;
		case "EXPENSE":
			testTransactionEntity.setFromType(TransactionFromType.ACCOUNT);
			testTransactionEntity.setToType(TransactionToType.CATEGORY);
			break;
		case "TRANSFER":
			testTransactionEntity.setFromType(TransactionFromType.ACCOUNT);
			testTransactionEntity.setToType(TransactionToType.ACCOUNT);
			break;
		default:
			throw new InvalidDataException("Invalid transaction type!");
		}
		testTransactionEntity.setFromId(fromId);
		testTransactionEntity.setToId(toId);
		testTransactionEntity.setSum(100.00d);
		testTransactionEntity.setShouldBeAutomaticallyExecuted(false);
		testTransactionEntity.setUser(owner);
		testTransactionEntity.setDescription(description);
		
		testTransactionEntity = transactionRepository.save(testTransactionEntity);
		
		return testTransactionEntity;
	}
	
	/**
	 * Inserts system categories for a specified user.
	 */
	public void insertTestSystemCategoriesForUser(UserEntity user) {
		CategoryEntity systemIncomeCategory = new CategoryEntity();
		systemIncomeCategory.setName("SYS_INCOME");
		systemIncomeCategory.setType(CategoryType.INCOME);
		systemIncomeCategory.setCurrentPeriodSum(0.0d);
		systemIncomeCategory.setOwner(user);
		categoryRepository.save(systemIncomeCategory);
		
		CategoryEntity systemExpensesCategory = new CategoryEntity();
		systemExpensesCategory.setName("SYS_EXPENSES");
		systemExpensesCategory.setType(CategoryType.EXPENSES);
		systemExpensesCategory.setCurrentPeriodSum(0.0d);
		systemExpensesCategory.setOwner(user);
		categoryRepository.save(systemExpensesCategory);
	}

	/**
	 * getAllNonDeletedAccounts() tests
	 */
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getAllNonDeletedAccountsMethodShouldReturnAllNonDeletedAccounts() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		AccountEntity firstTestAccount = insertTestAccountForUser(testUser, AccountType.ACTIVATED, "My Activated Account");
		AccountEntity secondTestAccount = insertTestAccountForUser(testUser, AccountType.DEACTIVATED, "My Deactivated Account");
		insertTestAccountForUser(testUser, AccountType.DELETED, "My Deleted Account");
		
		assertTrue( accountRepository.count() == 3 );
		
		List<Account> nonDeletedAccounts = accountService.getAllNonDeletedAccounts();
		assertTrue( nonDeletedAccounts.size() == 2 );
		assertTrue( nonDeletedAccounts.get(0).getName().equals( firstTestAccount.getName() ) );
		assertTrue( nonDeletedAccounts.get(1).getName().equals( secondTestAccount.getName() ) );
	}
	
	@Test(expected = NoSuchElementException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getAllNonDeletedAccountsMethodShouldThrowExceptionBecauseOfMissingUser() {
		accountService.getAllNonDeletedAccounts();
	}
	
	/**
	 * getAllAccountsByType() tests
	 */
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getAllAccountsByTypeMethodShouldReturnAllActivatedAccounts() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		AccountEntity firstTestAccount = insertTestAccountForUser(testUser, AccountType.ACTIVATED, "My First Activated Account");
		AccountEntity secondTestAccount = insertTestAccountForUser(testUser, AccountType.ACTIVATED, "My Second Activated Account");
		insertTestAccountForUser(testUser, AccountType.DELETED, "My Deleted Account");
		
		assertTrue( accountRepository.count() == 3 );
		
		List<Account> activatedAccounts = accountService.getAllAccountsByType(AccountType.ACTIVATED);
		assertTrue( activatedAccounts.size() == 2 );
		assertTrue( activatedAccounts.get(0).getName().equals( firstTestAccount.getName() ) );
		assertTrue( activatedAccounts.get(1).getName().equals( secondTestAccount.getName() ) );	
	}
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getAllAccountsByTypeMethodShouldReturnAllDeactivatedAccounts() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		AccountEntity firstTestAccount = insertTestAccountForUser(testUser, AccountType.DEACTIVATED, "My First Deactivated Account");
		AccountEntity secondTestAccount = insertTestAccountForUser(testUser, AccountType.DEACTIVATED, "My Second Deactivated Account");
		insertTestAccountForUser(testUser, AccountType.DELETED, "My Deleted Account");
		
		assertTrue( accountRepository.count() == 3 );
		
		List<Account> activatedAccounts = accountService.getAllAccountsByType(AccountType.DEACTIVATED);
		assertTrue( activatedAccounts.size() == 2 );
		assertTrue( activatedAccounts.get(0).getName().equals( firstTestAccount.getName() ) );
		assertTrue( activatedAccounts.get(1).getName().equals( secondTestAccount.getName() ) );	
	}
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getAllAccountsByTypeMethodShouldReturnAllDeletedAccounts() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		AccountEntity firstTestAccount = insertTestAccountForUser(testUser, AccountType.DELETED, "My First Deleted Account");
		AccountEntity secondTestAccount = insertTestAccountForUser(testUser, AccountType.DELETED, "My Second Deleted Account");
		insertTestAccountForUser(testUser, AccountType.ACTIVATED, "My Activated Account");
		
		assertTrue( accountRepository.count() == 3 );
		
		List<Account> activatedAccounts = accountService.getAllAccountsByType(AccountType.DELETED);
		assertTrue( activatedAccounts.size() == 2 );
		assertTrue( activatedAccounts.get(0).getName().equals( firstTestAccount.getName() ) );
		assertTrue( activatedAccounts.get(1).getName().equals( secondTestAccount.getName() ) );	
	}
	
	@Test(expected = NoSuchElementException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getAllAccountsByTypeMethodShouldThrowExceptionBecauseOfMissingUser() {
		accountService.getAllAccountsByType(AccountType.ACTIVATED);
	}
	
	/**
	 * getTotalBalanceOfActivatedAccounts() tests
	 */
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTotalBalanceOfActivatedAccountsMethodShouldReturnActivatedAccountsTotalBalance() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		AccountEntity firstActivatedAccount = insertTestAccountForUser(testUser, AccountType.ACTIVATED, "My First Activated Account");
		AccountEntity secondActivatedAccount = insertTestAccountForUser(testUser, AccountType.ACTIVATED, "My Second Activated Account");
		insertTestAccountForUser(testUser, AccountType.DEACTIVATED, "My Deactivated Account");
		insertTestAccountForUser(testUser, AccountType.DELETED, "My Deleted Account");
		
		Double firstTestAccountBalance = firstActivatedAccount.getBalance();
		Double secondTestAccountBalance = secondActivatedAccount.getBalance();
		
		Double expectedTotalBalance = firstTestAccountBalance + secondTestAccountBalance;
		Double actualTotalBalance = accountService.getTotalBalanceOfActivatedAccounts();
		assertEquals(expectedTotalBalance, actualTotalBalance);
	}
	
	@Test(expected = NoSuchElementException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTotalBalanceOfActivatedAccountsMethodShouldThrowExceptionBecauseOfMissingUser() {
		accountService.getTotalBalanceOfActivatedAccounts();
	}
	
	/**
	 * getAccountById() tests
	 */
	
	@Test(expected = AccountNotFoundException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getAccountByIdMethodShouldThrowAccountNotFoundException() {
		insertTestUser(TEST_USER_EMAIL);
		accountService.getAccountById(1L);
	}
	
	@Test(expected = UserDoesNotOwnResourceException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getAccountByIdMethodShouldThrowUserDoesNotOwnResourceException() {
		insertTestUser(TEST_USER_EMAIL);
		UserEntity anotherTestUser = insertTestUser("another_email@pfm.com");
		AccountEntity testAccount = insertTestAccountForUser(anotherTestUser, AccountType.ACTIVATED, "My Account");
		Long testAccountId = testAccount.getId();
		accountService.getAccountById(testAccountId);
	}
	
	@Test(expected = NullPointerException.class)
	public void getAccountByIdMethodShouldThrowExceptionBecauseOfMissingAuthentication() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		AccountEntity testAccount = insertTestAccountForUser(testUser, AccountType.ACTIVATED, "My Account");
		Long testAccountId = testAccount.getId();
		accountService.getAccountById(testAccountId);
	}
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getAccountByIdMethodShouldReturnInsertedAccount() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		AccountEntity testAccount = insertTestAccountForUser(testUser, AccountType.ACTIVATED, "My Account");
		Long testAccountId = testAccount.getId();
		Account retrievedAccount = accountService.getAccountById(testAccountId);
		assertTrue( accountRepository.count() == 1 );
		assertTrue( retrievedAccount.getName().equals(testAccount.getName()) );
		assertTrue( retrievedAccount.getId() == testAccount.getId() );
	}
	
	/**
	 * getTotalIncomeSumBetweenDatesById() tests
	 */

	@Test(expected = AccountNotFoundException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTotalIncomeSumBetweenDatesByIdMethodShouldThrowAccountNotFoundException() {
		insertTestUser(TEST_USER_EMAIL);
		accountService.getTotalIncomeSumBetweenDatesById(1L, new Date(), new Date());
	}
	
	@Test(expected = UserDoesNotOwnResourceException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTotalIncomeSumBetweenDatesByIdMethodShouldThrowUserDoesNotOwnResourceException() {
		insertTestUser(TEST_USER_EMAIL);
		UserEntity anotherTestUser = insertTestUser("another_email@pfm.com");
		AccountEntity testAccount = insertTestAccountForUser(anotherTestUser, AccountType.ACTIVATED, "My Account");
		Long testAccountId = testAccount.getId();
		accountService.getTotalIncomeSumBetweenDatesById(testAccountId, new Date(), new Date());
	}
	
	@Test(expected = NullPointerException.class)
	public void getTotalIncomeSumBetweenDatesByIdMethodShouldThrowExceptionBecauseOfMissingAuthentication() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		AccountEntity testAccount = insertTestAccountForUser(testUser, AccountType.ACTIVATED, "My Account");
		Long testAccountId = testAccount.getId();
		accountService.getTotalIncomeSumBetweenDatesById(testAccountId, new Date(), new Date());
	}
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTotalIncomeSumBetweenDatesByIdMethod_ShouldReturnTotalIncomeSumBetweenDates() throws Exception {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		AccountEntity testAccount = insertTestAccountForUser(testUser, AccountType.ACTIVATED, "Account 1");
    	AccountEntity anotherTestAccount = insertTestAccountForUser(testUser, AccountType.ACTIVATED, "Account 2");
    	CategoryEntity incomeCategory = insertTestCategoryForUser(testUser, CategoryType.INCOME, "Income Category");
    	
    	SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
    	insertTestTransactionForUser(testUser, "TRANSFER", dateFormatter.parse("24.12.2019"), "My first transfer transaction", anotherTestAccount.getId(), testAccount.getId());
    	insertTestTransactionForUser(testUser, "TRANSFER", dateFormatter.parse("19.06.2020"), "My second transfer transaction", anotherTestAccount.getId(), testAccount.getId());
    	insertTestTransactionForUser(testUser, "INCOME", dateFormatter.parse("17.06.2020"), "My income transaction", incomeCategory.getId(), testAccount.getId());
    	
    	Double result = accountService.getTotalIncomeSumBetweenDatesById( testAccount.getId(), dateFormatter.parse("24.12.2019"), dateFormatter.parse("17.06.2020") );
    	assertEquals( Double.valueOf(200.00d), result );
	}
	
	/**
	 * getTotalExpenseSumBetweenDatesById() tests
	 */

	@Test(expected = AccountNotFoundException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTotalExpenseSumBetweenDatesByIdMethodShouldThrowAccountNotFoundException() {
		insertTestUser(TEST_USER_EMAIL);
		accountService.getTotalExpenseSumBetweenDatesById(1L, new Date(), new Date());
	}
	
	@Test(expected = UserDoesNotOwnResourceException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTotalExpenseSumBetweenDatesByIdMethodShouldThrowUserDoesNotOwnResourceException() {
		insertTestUser(TEST_USER_EMAIL);
		UserEntity anotherTestUser = insertTestUser("another_email@pfm.com");
		AccountEntity testAccount = insertTestAccountForUser(anotherTestUser, AccountType.ACTIVATED, "My Account");
		Long testAccountId = testAccount.getId();
		accountService.getTotalExpenseSumBetweenDatesById(testAccountId, new Date(), new Date());
	}
	
	@Test(expected = NullPointerException.class)
	public void getTotalExpenseSumBetweenDatesByIdMethodShouldThrowExceptionBecauseOfMissingAuthentication() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		AccountEntity testAccount = insertTestAccountForUser(testUser, AccountType.ACTIVATED, "My Account");
		Long testAccountId = testAccount.getId();
		accountService.getTotalExpenseSumBetweenDatesById(testAccountId, new Date(), new Date());
	}
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTotalExpenseSumBetweenDatesByIdMethod_ShouldReturnTotalExpenseSumBetweenDates() throws Exception {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		AccountEntity testAccount = insertTestAccountForUser(testUser, AccountType.ACTIVATED, "Account 1");
    	AccountEntity anotherTestAccount = insertTestAccountForUser(testUser, AccountType.ACTIVATED, "Account 2");
    	CategoryEntity expenseCategory = insertTestCategoryForUser(testUser, CategoryType.EXPENSES, "Expense Category");
    	
    	SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
    	insertTestTransactionForUser(testUser, "TRANSFER", dateFormatter.parse("24.12.2019"), "My first transfer transaction", testAccount.getId(), anotherTestAccount.getId());
    	insertTestTransactionForUser(testUser, "TRANSFER", dateFormatter.parse("19.06.2020"), "My second transfer transaction", testAccount.getId(), anotherTestAccount.getId());
    	insertTestTransactionForUser(testUser, "EXPENSE", dateFormatter.parse("17.06.2020"), "My expense transaction", testAccount.getId(), expenseCategory.getId());
    	
    	Double result = accountService.getTotalExpenseSumBetweenDatesById( testAccount.getId(), dateFormatter.parse("24.12.2019"), dateFormatter.parse("17.06.2020") );
    	assertEquals( Double.valueOf(200.00d), result );
	}
	
	/**
	 * createNewAccount() tests
	 */
	
	@Test(expected = NullPointerException.class)
	public void createNewAccountMethodShouldThrowExceptionBecauseOfMissingAuthentication() {
		AccountRequest testAccountRequest = getTestAccountRequest();
		accountService.createNewAccount(testAccountRequest);
	}
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void createNewAccountMethodShouldCreateAccount() {
		insertTestUser(TEST_USER_EMAIL);
		
		AccountRequest testAccountRequest = getTestAccountRequest();
		Account createdAccount = accountService.createNewAccount(testAccountRequest);
		Long createdAccountId = createdAccount.getId();
		
		assertTrue( accountRepository.count() == 1 );
		assertTrue( accountRepository.existsById(createdAccountId) );
		AccountEntity accountEntity = accountRepository.findById(createdAccountId).get();
		assertTrue( createdAccount.getName().equals(accountEntity.getName()) );
		assertTrue( createdAccountId == accountEntity.getId() );
	}
	
	@Test(expected = NameAlreadyExistsException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void createNewAccountMethodShouldThrowNameAlreadyExistsException() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		AccountRequest testAccountRequest = getTestAccountRequest();
		insertTestAccountForUser(testUser, AccountType.ACTIVATED, testAccountRequest.getName());
		accountService.createNewAccount(testAccountRequest);
	}
	
	/**
	 * updateAccountById() tests
	 */
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void updateAccountByIdMethodShouldUpdateAccount() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		AccountEntity testAccount = insertTestAccountForUser(testUser, AccountType.ACTIVATED, "My Account");
		Long testAccountId = testAccount.getId();
		
		AccountRequest accountUpdateRequest = getTestAccountRequest();
		accountUpdateRequest.setName("My Updated Account");
		
		Account updatedAccount = accountService.updateAccountById(testAccountId, accountUpdateRequest);
		
		assertTrue( accountRepository.count() == 1 );
		assertTrue( accountRepository.existsById(testAccountId) );
		assertTrue( accountRepository.findById(testAccountId).get().getName().equals("My Updated Account") );
		assertTrue( updatedAccount.getName().equals("My Updated Account") );
	}
	
	@Test(expected = AccountNotFoundException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void updateAccountByIdMethodShouldThrowAccountNotFoundException() {
		insertTestUser(TEST_USER_EMAIL);		
		AccountRequest accountUpdateRequest = getTestAccountRequest();
		accountService.updateAccountById(1L, accountUpdateRequest);
	}
	
	@Test(expected = UserDoesNotOwnResourceException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void updateAccountByIdMethodShouldThrowUserDoesNotOwnResourceException() {
		insertTestUser(TEST_USER_EMAIL);
		UserEntity anotherTestUser = insertTestUser("another_email@pfm.com");
		
		AccountEntity testAccount = insertTestAccountForUser(anotherTestUser, AccountType.ACTIVATED, "My Account");
		Long testAccountId = testAccount.getId();
		
		AccountRequest accountUpdateRequest = getTestAccountRequest();
		accountUpdateRequest.setName("My Updated Account");
		
		accountService.updateAccountById(testAccountId, accountUpdateRequest);
	}
	
	@Test(expected = NameAlreadyExistsException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void updateAccountByIdMethodShouldThrowNameAlreadyExistsException() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		AccountEntity testAccount = insertTestAccountForUser(testUser, AccountType.ACTIVATED, "My Account");
		Long testAccountId = testAccount.getId();
		
		AccountRequest accountUpdateRequest = getTestAccountRequest();
		accountUpdateRequest.setName("My Updated Account");
		insertTestAccountForUser(testUser, AccountType.ACTIVATED, accountUpdateRequest.getName());
		
		accountService.updateAccountById(testAccountId, accountUpdateRequest);
	}
	
	@Test(expected = NullPointerException.class)
	public void updateAccountByIdMethodShouldThrowExceptionBecauseOfMissingAuthentication() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		AccountEntity testAccount = insertTestAccountForUser(testUser, AccountType.ACTIVATED, "My Account");
		Long testAccountId = testAccount.getId();
		
		AccountRequest accountUpdateRequest = getTestAccountRequest();
		accountUpdateRequest.setName("My Updated Account");
		
		accountService.updateAccountById(testAccountId, accountUpdateRequest);
	}
	
	/**
	 * deactivateAccountById() tests
	 */
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void deactivateAccountByIdMethodShouldDeactivateAccount() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		AccountEntity testAccount = insertTestAccountForUser(testUser, AccountType.ACTIVATED, "My Account");
		Long testAccountId = testAccount.getId();
		
		accountService.deactivateAccountById(testAccountId);
		
		assertTrue( accountRepository.count() == 1 );
		assertTrue( accountRepository.existsById(testAccountId) );
		assertTrue( accountRepository.findById(testAccountId).get().getType() == AccountType.DEACTIVATED );
	}
	
	@Test(expected = AccountNotFoundException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void deactivateAccountByIdMethodShouldThrowAccountNotFoundException() {
		insertTestUser(TEST_USER_EMAIL);		
		accountService.deactivateAccountById(1L);
	}
	
	@Test(expected = UserDoesNotOwnResourceException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void deactivateAccountByIdMethodShouldThrowUserDoesNotOwnResourceException() {
		insertTestUser(TEST_USER_EMAIL);
		UserEntity anotherTestUser = insertTestUser("another_email@pfm.com");
		
		AccountEntity testAccount = insertTestAccountForUser(anotherTestUser, AccountType.ACTIVATED, "My Account");
		Long testAccountId = testAccount.getId();
		
		accountService.deactivateAccountById(testAccountId);
	}
	
	@Test(expected = AccountTypeUpdateException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void deactivateAccountByIdMethodShouldThrowAccountTypeUpdateException() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		AccountEntity testAccount = insertTestAccountForUser(testUser, AccountType.DELETED, "My Account");
		Long testAccountId = testAccount.getId();
		
		accountService.deactivateAccountById(testAccountId);
	}
	
	/**
	 * activateAccountById() tests
	 */
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void activateAccountByIdMethodShouldActivateAccount() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		AccountEntity testAccount = insertTestAccountForUser(testUser, AccountType.DEACTIVATED, "My Account");
		Long testAccountId = testAccount.getId();
		
		accountService.activateAccountById(testAccountId);
		
		assertTrue( accountRepository.count() == 1 );
		assertTrue( accountRepository.existsById(testAccountId) );
		assertTrue( accountRepository.findById(testAccountId).get().getType() == AccountType.ACTIVATED );
	}
	
	@Test(expected = AccountNotFoundException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void activateAccountByIdMethodShouldThrowAccountNotFoundException() {
		insertTestUser(TEST_USER_EMAIL);		
		accountService.activateAccountById(1L);
	}
	
	@Test(expected = UserDoesNotOwnResourceException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void activateAccountByIdMethodShouldThrowUserDoesNotOwnResourceException() {
		insertTestUser(TEST_USER_EMAIL);
		UserEntity anotherTestUser = insertTestUser("another_email@pfm.com");
		
		AccountEntity testAccount = insertTestAccountForUser(anotherTestUser, AccountType.DEACTIVATED, "My Account");
		Long testAccountId = testAccount.getId();
		
		accountService.activateAccountById(testAccountId);
	}
	
	@Test(expected = AccountTypeUpdateException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void activateAccountByIdMethodShouldThrowAccountTypeUpdateException() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		AccountEntity testAccount = insertTestAccountForUser(testUser, AccountType.DELETED, "My Account");
		Long testAccountId = testAccount.getId();
		
		accountService.activateAccountById(testAccountId);
	}
	
	/**
	 * setAccountGoalById() tests
	 */
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void setAccountGoalByIdMethodShouldSetAccountGoal() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		AccountEntity testAccount = insertTestAccountForUser(testUser, AccountType.ACTIVATED, "My Account");
		Long testAccountId = testAccount.getId();
		
		accountService.setAccountGoalById(testAccountId, 1234.56d);
		
		assertTrue( accountRepository.count() == 1 );
		assertTrue( accountRepository.existsById(testAccountId) );
		assertTrue( accountRepository.findById(testAccountId).get().getGoal() == 1234.56d );
	}
	
	@Test(expected = AccountNotFoundException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void setAccountGoalByIdMethodShouldThrowAccountNotFoundException() {
		insertTestUser(TEST_USER_EMAIL);		
		accountService.setAccountGoalById(1L, 1234.56d);
	}
	
	@Test(expected = UserDoesNotOwnResourceException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void setAccountGoalByIdMethodShouldThrowUserDoesNotOwnResourceException() {
		insertTestUser(TEST_USER_EMAIL);
		UserEntity anotherTestUser = insertTestUser("another_email@pfm.com");
		
		AccountEntity testAccount = insertTestAccountForUser(anotherTestUser, AccountType.ACTIVATED, "My Account");
		Long testAccountId = testAccount.getId();
		
		accountService.setAccountGoalById(testAccountId, 1234.56d);
	}
	
	/**
	 * setAccountBalanceById() tests
	 */
	
	@Test(expected = AccountNotFoundException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void setAccountBalanceByIdMethodShouldThrowAccountNotFoundException() {
		insertTestUser(TEST_USER_EMAIL);		
		accountService.setAccountBalanceById(1L, 3456.78d);
	}
	
	@Test(expected = UserDoesNotOwnResourceException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void setAccountBalanceByIdMethodShouldThrowUserDoesNotOwnResourceException() {
		insertTestUser(TEST_USER_EMAIL);
		UserEntity anotherTestUser = insertTestUser("another_email@pfm.com");
		
		AccountEntity testAccount = insertTestAccountForUser(anotherTestUser, AccountType.ACTIVATED, "My Account");
		Long testAccountId = testAccount.getId();
		
		accountService.setAccountBalanceById(testAccountId, 3456.78d);
	}
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void setAccountBalanceByIdMethodShouldSetAccountBalanceAndCreateIncomeTransaction() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		insertTestSystemCategoriesForUser(testUser);
		
		AccountEntity testAccount = insertTestAccountForUser(testUser, AccountType.ACTIVATED, "My Account");
		Double testAccountBalance = testAccount.getBalance();
		Long testAccountId = testAccount.getId();
		
		accountService.setAccountBalanceById(testAccountId, 3456.78d);
		
		assertTrue( accountRepository.count() == 1 );
		assertTrue( accountRepository.existsById(testAccountId) );
		assertTrue( accountRepository.findById(testAccountId).get().getBalance() == 3456.78d );
		
		assertTrue( transactionRepository.count() == 1 );
		TransactionEntity createdTransaction = transactionRepository.findAll().get(0);
		assertTrue( createdTransaction.getFromType() == TransactionFromType.CATEGORY );
		assertTrue( createdTransaction.getToType() == TransactionToType.ACCOUNT );
		assertTrue( createdTransaction.getToId() == testAccountId );
		assertTrue ( createdTransaction.getSum() == (3456.78d - testAccountBalance) );
	}
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void setAccountBalanceByIdMethodShouldSetAccountBalanceAndCreateExpenseTransaction() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		insertTestSystemCategoriesForUser(testUser);
		
		AccountEntity testAccount = insertTestAccountForUser(testUser, AccountType.ACTIVATED, "My Account");
		Double testAccountBalance = testAccount.getBalance();
		Long testAccountId = testAccount.getId();
		
		accountService.setAccountBalanceById(testAccountId, 100.00d);
		
		assertTrue( accountRepository.count() == 1 );
		assertTrue( accountRepository.existsById(testAccountId) );
		assertTrue( accountRepository.findById(testAccountId).get().getBalance() == 100.00d );
		
		assertTrue( transactionRepository.count() == 1 );
		TransactionEntity createdTransaction = transactionRepository.findAll().get(0);
		assertTrue( createdTransaction.getFromType() == TransactionFromType.ACCOUNT );
		assertTrue( createdTransaction.getToType() == TransactionToType.CATEGORY );
		assertTrue( createdTransaction.getFromId() == testAccountId );
		assertTrue ( createdTransaction.getSum() == (testAccountBalance - 100.00d) );
	}
	
	/**
	 * deleteAccountById() tests
	 */
	
	@Test
    @WithMockUser(username = TEST_USER_EMAIL)
	public void deleteAccountByIdMethodShouldDeleteAccount() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		AccountEntity testAccount = insertTestAccountForUser(testUser, AccountType.ACTIVATED, "My Account");
		testAccount.setBalance(0.0d);
		testAccount = accountRepository.save(testAccount);
		
		Long testAccountId = testAccount.getId();
		
		accountService.deleteAccountById(testAccountId);
		
		assertTrue( accountRepository.count() == 1 );
		assertTrue( accountRepository.existsById(testAccountId) );
		assertTrue( accountRepository.findById(testAccountId).get().getType() == AccountType.DELETED );
	}
	
	@Test(expected = AccountNotFoundException.class)
    @WithMockUser(username = TEST_USER_EMAIL)
	public void deleteAccountByIdMethodShouldThrowAccountNotFoundException() {
		insertTestUser(TEST_USER_EMAIL);
		accountService.deleteAccountById(1L);
	}
	
	@Test(expected = UserDoesNotOwnResourceException.class)
    @WithMockUser(username = TEST_USER_EMAIL)
	public void deleteAccountByIdMethodShouldThrowUserDoesNotOwnResourceException() {
		insertTestUser(TEST_USER_EMAIL);
		UserEntity anotherTestUser = insertTestUser("another_email@pfm.com");
		
		AccountEntity testAccount = insertTestAccountForUser(anotherTestUser, AccountType.ACTIVATED, "My Account");
		Long testAccountId = testAccount.getId();
		
		accountService.deleteAccountById(testAccountId);
	}
	
	@Test(expected = AccountDeleteException.class)
    @WithMockUser(username = TEST_USER_EMAIL)
	public void deleteAccountByIdMethodShouldThrowAccountDeleteExceptionBecauseOfAlreadyDeletedAccount() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		AccountEntity testAccount = insertTestAccountForUser(testUser, AccountType.DELETED, "My Account");
		testAccount.setBalance(0.0d);
		testAccount = accountRepository.save(testAccount);
		
		Long testAccountId = testAccount.getId();
		accountService.deleteAccountById(testAccountId);
	}
	
	@Test(expected = AccountDeleteException.class)
    @WithMockUser(username = TEST_USER_EMAIL)
	public void deleteAccountByIdMethodShouldThrowAccountDeleteExceptionBecauseOfNonZeroBalance() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		AccountEntity testAccount = insertTestAccountForUser(testUser, AccountType.ACTIVATED, "My Account");
		
		Long testAccountId = testAccount.getId();
		accountService.deleteAccountById(testAccountId);
	}
	
	/**
	 * createExampleAccountsForCurrentlyLoggedInUser() tests
	 */
	
	@Test
    @WithMockUser(username = TEST_USER_EMAIL)
	public void createExampleAccountsForCurrentlyLoggedInUserMethodShouldCreateExampleAccounts() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		assertTrue( accountRepository.count() == 0 );
		
		List<Account> exampleAccounts = accountService.createExampleAccountsForCurrentlyLoggedInUser();
		
		assertTrue( accountRepository.count() == accountService.getNumberOfExampleAccounts() );
		for (Account account : exampleAccounts) {
			String name = account.getName();
			assertTrue( accountRepository.existsByNameAndOwner(name, testUser) );
		}
	}
	
	@Test(expected = NoSuchElementException.class)
    @WithMockUser(username = TEST_USER_EMAIL)
	public void createExampleAccountsForCurrentlyLoggedInUserMethodShouldThrowExceptionBecauseOfMissingAuthentication() {
		accountService.createExampleAccountsForCurrentlyLoggedInUser();
	}
	
	/**
	 * performAccountBalanceOperationById() tests
	 */
	
	@Test
    @WithMockUser(username = TEST_USER_EMAIL)
	public void performAccountBalanceOperationByIdMethodShouldIncreaseAccountBalance() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		AccountEntity testAccount = insertTestAccountForUser(testUser, AccountType.ACTIVATED, "My Account");
		Long testAccountId = testAccount.getId();
		Double testAccountBalance = testAccount.getBalance();
		
		accountService.performAccountBalanceOperationById(testAccountId, '+', 100.00d);
		
		assertTrue( accountRepository.count() == 1 );
		assertTrue( accountRepository.existsById(testAccountId) );
		assertTrue( 
			accountRepository.findById(testAccountId).get().getBalance() == (testAccountBalance + 100.00d)
		);
	}
	
	@Test
    @WithMockUser(username = TEST_USER_EMAIL)
	public void performAccountBalanceOperationByIdMethodShouldDecreaseAccountBalance() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		AccountEntity testAccount = insertTestAccountForUser(testUser, AccountType.ACTIVATED, "My Account");
		Long testAccountId = testAccount.getId();
		Double testAccountBalance = testAccount.getBalance();
		
		accountService.performAccountBalanceOperationById(testAccountId, '-', 100.00d);
		
		assertTrue( accountRepository.count() == 1 );
		assertTrue( accountRepository.existsById(testAccountId) );
		assertTrue( 
			accountRepository.findById(testAccountId).get().getBalance() == (testAccountBalance - 100.00d)
		);
	}
	
	@Test
    @WithMockUser(username = TEST_USER_EMAIL)
	public void performAccountBalanceOperationByIdMethodShouldNotChangeAccountBalance() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		AccountEntity testAccount = insertTestAccountForUser(testUser, AccountType.ACTIVATED, "My Account");
		Long testAccountId = testAccount.getId();
		Double testAccountBalance = testAccount.getBalance();
		
		accountService.performAccountBalanceOperationById(testAccountId, 'X', 100.00d);
		
		assertTrue( accountRepository.count() == 1 );
		assertTrue( accountRepository.existsById(testAccountId) );
		assertTrue( 
			accountRepository.findById(testAccountId).get().getBalance() == testAccountBalance
		);
	}
	
}