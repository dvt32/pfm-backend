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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
import com.mse.personal.finance.model.AccountType;
import com.mse.personal.finance.model.CategoryType;
import com.mse.personal.finance.model.FamilyStatusType;
import com.mse.personal.finance.model.GenderType;
import com.mse.personal.finance.model.Transaction;
import com.mse.personal.finance.model.TransactionFromType;
import com.mse.personal.finance.model.TransactionToType;
import com.mse.personal.finance.model.request.TransactionRequest;
import com.mse.personal.finance.rest.exception.InvalidDataException;
import com.mse.personal.finance.rest.exception.TransactionNotFoundException;
import com.mse.personal.finance.rest.exception.UserDoesNotOwnResourceException;

/**
 * This class implements unit tests for the TransactionService class.
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
public class TransactionServiceTests {
	
	private static final String TEST_USER_EMAIL = "test@pfm.com";
	private static final String TEST_USER_PASSWORD = "123456";
	
	@Autowired
	private TransactionRepository transactionRepository;
	
	@Autowired
	private TransactionService transactionService;
	
	@Autowired
	private AccountRepository accountRepository;
	
	@Autowired
	private CategoryRepository categoryRepository;
	
	@Autowired
	private UserRepository userRepository;
	
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
	 * Inserts a test transaction for the specified user and returns that transaction's data
	 */
	public TransactionEntity insertTestTransactionForUser(
		UserEntity userEntity, 
		String transactionType, 
		Date transactionDate,
		String transactionDescription) 
	{
		TransactionEntity testTransactionEntity = new TransactionEntity();
		
		testTransactionEntity.setDateOfCompletion(transactionDate);
		switch (transactionType) {
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
		testTransactionEntity.setFromId(1L);
		testTransactionEntity.setToId(2L);
		testTransactionEntity.setSum(100.00d);
		testTransactionEntity.setShouldBeAutomaticallyExecuted(false);
		testTransactionEntity.setUser(userEntity);
		testTransactionEntity.setDescription(transactionDescription);
		
		testTransactionEntity = transactionRepository.save(testTransactionEntity);
		
		return testTransactionEntity;
	}
	
	/**
	 * Create and return a test transaction request
	 */
	public TransactionRequest getTestTransactionRequest(String type, String description, Long fromId, Long toId) throws Exception {
		TransactionRequest testTransactionRequest = new TransactionRequest();
		
		testTransactionRequest.setDateOfCompletion( new SimpleDateFormat("dd.MM.yyyy").parse("03.03.2020") );
		switch (type) {
		case "INCOME":
			testTransactionRequest.setFromType(TransactionFromType.CATEGORY);
			testTransactionRequest.setToType(TransactionToType.ACCOUNT);
			break;
		case "EXPENSE": 
			testTransactionRequest.setFromType(TransactionFromType.ACCOUNT);
			testTransactionRequest.setToType(TransactionToType.CATEGORY);
			break;
		case "TRANSFER": 
			testTransactionRequest.setFromType(TransactionFromType.ACCOUNT);
			testTransactionRequest.setToType(TransactionToType.ACCOUNT);
			break;
		default: 
			break;
		}
		testTransactionRequest.setFromId(fromId);
		testTransactionRequest.setToId(toId);
    	testTransactionRequest.setSum(100.00d);
    	testTransactionRequest.setShouldBeAutomaticallyExecuted(false);
    	testTransactionRequest.setDescription(description);
		
    	return testTransactionRequest;
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
	 * getTransactionsByPageable() tests
	 */
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTransactionsByPageableMethod_ShouldReturnTransactions() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		TransactionEntity firstTransaction = insertTestTransactionForUser(testUser, "EXPENSE", new Date(), "My expense transaction");
    	TransactionEntity secondTransaction = insertTestTransactionForUser(testUser, "INCOME", new Date(), "My income transaction");
    	
		assertTrue( transactionRepository.count() == 2 );
		
		Page<Transaction> pageOfTransactions = transactionService.getTransactionsByPageable( PageRequest.of(0, 5) );
		assertTrue( pageOfTransactions.getNumberOfElements() == 2 );
		
		List<Transaction> transactions = pageOfTransactions.getContent();
		assertTrue( transactions.size() == 2 );
		assertTrue( transactions.get(0).getDescription().equals( firstTransaction.getDescription() ) );
		assertTrue( transactions.get(1).getDescription().equals( secondTransaction.getDescription() ) );
	}
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTransactionsByPageableMethod_ShouldReturnTransactionsOnTwoPages() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		TransactionEntity[] insertedTransactions = new TransactionEntity[9];
		for (int i = 0; i < insertedTransactions.length; ++i) {
			insertedTransactions[i] = insertTestTransactionForUser(testUser, "EXPENSE", new Date(), "Transaction " + i);
		}
    	
		assertTrue( transactionRepository.count() == insertedTransactions.length);
		
		Page<Transaction> firstPageOfTransactions = transactionService.getTransactionsByPageable( PageRequest.of(0, 5) );
		Page<Transaction> secondPageOfTransactions = transactionService.getTransactionsByPageable( PageRequest.of(1, 5) );
		assertTrue( firstPageOfTransactions.getNumberOfElements() == 5 );
		assertTrue( secondPageOfTransactions.getNumberOfElements() == 4 );
		
		int i = 0;
		for (Transaction transaction : firstPageOfTransactions.getContent()) {
			assertEquals( insertedTransactions[i].getDescription(), transaction.getDescription() );
			i++;
		}
		for (Transaction transaction : secondPageOfTransactions.getContent()) {
			assertEquals( insertedTransactions[i].getDescription(), transaction.getDescription() );
			i++;
		}
	}
	
	@Test(expected = NoSuchElementException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTransactionsByPageableMethod_ShouldThrowExceptionBecauseOfMissingUser() {
		transactionService.getTransactionsByPageable( PageRequest.of(0, 5) );
	}
	
	/**
	 * getTransactionsByTypeAndPageable() tests
	 */
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTransactionsByTypeAndPageableMethod_ShouldReturnIncomeTransactions() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		TransactionEntity firstIncomeTransaction = insertTestTransactionForUser(testUser, "INCOME", new Date(), "My first income transaction");
    	TransactionEntity secondIncomeTransaction = insertTestTransactionForUser(testUser, "INCOME", new Date(), "My second income transaction");
    	insertTestTransactionForUser(testUser, "EXPENSE", new Date(), "My expense transaction");
    	insertTestTransactionForUser(testUser, "TRANSFER", new Date(), "My transfer transaction");
    	
		assertTrue( transactionRepository.count() == 4 );
		
		Page<Transaction> pageOfTransactions = transactionService.getTransactionsByTypeAndPageable( "INCOME", PageRequest.of(0, 5) );
		assertTrue( pageOfTransactions.getNumberOfElements() == 2 );
		
		List<Transaction> transactions = pageOfTransactions.getContent();
		assertTrue( transactions.size() == 2 );
		assertTrue( transactions.get(0).getDescription().equals( firstIncomeTransaction.getDescription() ) );
		assertTrue( transactions.get(1).getDescription().equals( secondIncomeTransaction.getDescription() ) );
	}
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTransactionsByTypeAndPageableMethod_ShouldReturnExpenseTransactions() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		TransactionEntity firstExpenseTransaction = insertTestTransactionForUser(testUser, "EXPENSE", new Date(), "My first expense transaction");
    	TransactionEntity secondExpenseTransaction = insertTestTransactionForUser(testUser, "EXPENSE", new Date(), "My second expense transaction");
    	insertTestTransactionForUser(testUser, "INCOME", new Date(), "My income transaction");
    	insertTestTransactionForUser(testUser, "TRANSFER", new Date(), "My transfer transaction");
    	
		assertTrue( transactionRepository.count() == 4 );
		
		Page<Transaction> pageOfTransactions = transactionService.getTransactionsByTypeAndPageable( "EXPENSE", PageRequest.of(0, 5) );
		assertTrue( pageOfTransactions.getNumberOfElements() == 2 );
		
		List<Transaction> transactions = pageOfTransactions.getContent();
		assertTrue( transactions.size() == 2 );
		assertTrue( transactions.get(0).getDescription().equals( firstExpenseTransaction.getDescription() ) );
		assertTrue( transactions.get(1).getDescription().equals( secondExpenseTransaction.getDescription() ) );
	}
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTransactionsByTypeAndPageableMethod_ShouldReturnTransferTransactions() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		TransactionEntity firstTransferTransaction = insertTestTransactionForUser(testUser, "TRANSFER", new Date(), "My first transfer transaction");
    	TransactionEntity secondTransferTransaction = insertTestTransactionForUser(testUser, "TRANSFER", new Date(), "My second transfer transaction");
    	insertTestTransactionForUser(testUser, "INCOME", new Date(), "My income transaction");
    	insertTestTransactionForUser(testUser, "EXPENSE", new Date(), "My expense transaction");
    	
		assertTrue( transactionRepository.count() == 4 );
		
		Page<Transaction> pageOfTransactions = transactionService.getTransactionsByTypeAndPageable( "TRANSFER", PageRequest.of(0, 5) );
		assertTrue( pageOfTransactions.getNumberOfElements() == 2 );
		
		List<Transaction> transactions = pageOfTransactions.getContent();
		assertTrue( transactions.size() == 2 );
		assertTrue( transactions.get(0).getDescription().equals( firstTransferTransaction.getDescription() ) );
		assertTrue( transactions.get(1).getDescription().equals( secondTransferTransaction.getDescription() ) );
	}
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTransactionsByTypeAndPageableMethod_ShouldReturnOneIncomeTransaction() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		TransactionEntity firstIncomeTransaction = insertTestTransactionForUser(testUser, "INCOME", new Date(), "My first income transaction");
    	insertTestTransactionForUser(testUser, "INCOME", new Date(), "My second income transaction");
    	insertTestTransactionForUser(testUser, "EXPENSE", new Date(), "My expense transaction");
    	insertTestTransactionForUser(testUser, "TRANSFER", new Date(), "My transfer transaction");
    	
		assertTrue( transactionRepository.count() == 4 );
		
		Page<Transaction> pageOfTransactions = transactionService.getTransactionsByTypeAndPageable( "INCOME", PageRequest.of(0, 1) );
		assertTrue( pageOfTransactions.getNumberOfElements() == 1 );
		
		List<Transaction> transactions = pageOfTransactions.getContent();
		assertTrue( transactions.size() == 1 );
		assertTrue( transactions.get(0).getDescription().equals( firstIncomeTransaction.getDescription() ) );
	}
	
	@Test(expected = NoSuchElementException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTransactionsByTypeAndPageableMethod_ShouldThrowExceptionBecauseOfMissingUser() {
		transactionService.getTransactionsByTypeAndPageable( "INCOME", PageRequest.of(0, 1) );
	}
	
	@Test(expected = InvalidDataException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTransactionsByTypeAndPageableMethod_ShouldThrowExceptionBecauseOfInvalidType() {
		insertTestUser(TEST_USER_EMAIL);
		transactionService.getTransactionsByTypeAndPageable( "INVALID-TRANSACTION-TYPE", PageRequest.of(0, 1) );
	}
	
	/**
	 * getTotalTransactionsSumByType() tests
	 */
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTotalTransactionsSumByTypeMethod_ShouldReturnIncomeTransactionsTotalSum() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		TransactionEntity firstIncomeTransaction = insertTestTransactionForUser(testUser, "INCOME", new Date(), "My first income transaction");
    	TransactionEntity secondIncomeTransaction = insertTestTransactionForUser(testUser, "INCOME", new Date(), "My second income transaction");
    	insertTestTransactionForUser(testUser, "EXPENSE", new Date(), "My expense transaction");
    	insertTestTransactionForUser(testUser, "TRANSFER", new Date(), "My transfer transaction");
		
		Double expectedTotalSum = firstIncomeTransaction.getSum() + secondIncomeTransaction.getSum();
		Double actualTotalSum = transactionService.getTotalTransactionsSumByType("INCOME");
		assertEquals(expectedTotalSum, actualTotalSum);
	}
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTotalTransactionsSumByTypeMethod_ShouldReturnExpenseTransactionsTotalSum() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		insertTestTransactionForUser(testUser, "INCOME", new Date(), "My first income transaction");
    	insertTestTransactionForUser(testUser, "INCOME", new Date(), "My second income transaction");
    	TransactionEntity expenseTransaction = insertTestTransactionForUser(testUser, "EXPENSE", new Date(), "My expense transaction");
    	insertTestTransactionForUser(testUser, "TRANSFER", new Date(), "My transfer transaction");
		
		Double expectedTotalSum = expenseTransaction.getSum();
		Double actualTotalSum = transactionService.getTotalTransactionsSumByType("EXPENSE");
		assertEquals(expectedTotalSum, actualTotalSum);
	}
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTotalTransactionsSumByTypeMethod_ShouldReturnTransferTransactionsTotalSum() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		TransactionEntity firstTransferTransaction = insertTestTransactionForUser(testUser, "TRANSFER", new Date(), "My first transfer transaction");
		TransactionEntity secondTransferTransaction = insertTestTransactionForUser(testUser, "TRANSFER", new Date(), "My second transfer transaction");
		TransactionEntity thirdTransferTransaction = insertTestTransactionForUser(testUser, "TRANSFER", new Date(), "My third transfer transaction");
		
		Double expectedTotalSum = firstTransferTransaction.getSum() + secondTransferTransaction.getSum() + thirdTransferTransaction.getSum();
		Double actualTotalSum = transactionService.getTotalTransactionsSumByType("TRANSFER");
		assertEquals(expectedTotalSum, actualTotalSum);
	}
	
	@Test(expected = NoSuchElementException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTotalTransactionsSumByTypeMethod_ShouldThrowExceptionBecauseOfMissingUser() {
		transactionService.getTotalTransactionsSumByType("TRANSFER");
	}
	
	@Test(expected = InvalidDataException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTotalTransactionsSumByTypeMethod_ShouldThrowExceptionBecauseOfInvalidType() {
		insertTestUser(TEST_USER_EMAIL);
		transactionService.getTotalTransactionsSumByType("INVALID-TRANSACTION-TYPE");
	}
	
	/**
	 * getTransactionsBetweenDatesByTypeAndPageable() tests
	 */
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTransactionsBetweenDatesByTypeAndPageableMethod_ShouldReturnIncomeTransactionsBetweenDates() throws Exception {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
    	
    	// Should be returned
    	TransactionEntity firstIncomeTransaction = insertTestTransactionForUser(testUser, "INCOME", dateFormatter.parse("24.12.2019"), "My first income transaction");
    	TransactionEntity secondIncomeTransaction = insertTestTransactionForUser(testUser, "INCOME", dateFormatter.parse("12.05.2020"), "My second income transaction");
    	TransactionEntity thirdIncomeTransaction = insertTestTransactionForUser(testUser, "INCOME", dateFormatter.parse("17.06.2020"), "My third income transaction");
    	
    	// Should not be returned (not of proper type or in date range)
    	insertTestTransactionForUser(testUser, "INCOME", dateFormatter.parse("23.12.2019"), "My fourth income transaction");
    	insertTestTransactionForUser(testUser, "INCOME", dateFormatter.parse("19.06.2020"), "My fifth income transaction");
    	insertTestTransactionForUser(testUser, "EXPENSE", new Date(), "My expense transaction");
    	insertTestTransactionForUser(testUser, "TRANSFER", new Date(), "My transfer transaction");
		
		Page<Transaction> pageOfTransactions = transactionService.getTransactionsBetweenDatesByTypeAndPageable("INCOME", dateFormatter.parse("24.12.2019"), dateFormatter.parse("17.06.2020"), PageRequest.of(0, 5));
		assertTrue( pageOfTransactions.getNumberOfElements() == 3 );
		
		List<Transaction> transactions = pageOfTransactions.getContent();
		assertTrue( transactions.size() == 3 );
		assertTrue( transactions.get(0).getDescription().equals(firstIncomeTransaction.getDescription()) );
		assertTrue( transactions.get(1).getDescription().equals(secondIncomeTransaction.getDescription()) );
		assertTrue( transactions.get(2).getDescription().equals(thirdIncomeTransaction.getDescription()) );
	}
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTransactionsBetweenDatesByTypeAndPageableMethod_ShouldReturnExpenseTransactionsBetweenDates() throws Exception {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
    	
    	// Should be returned
		TransactionEntity firstExpenseTransaction = insertTestTransactionForUser(testUser, "EXPENSE", dateFormatter.parse("24.12.2019"), "My first expense transaction");
    	TransactionEntity secondExpenseTransaction = insertTestTransactionForUser(testUser, "EXPENSE", dateFormatter.parse("12.05.2020"), "My second expense transaction");
    	TransactionEntity thirdExpenseTransaction = insertTestTransactionForUser(testUser, "EXPENSE", dateFormatter.parse("17.06.2020"), "My third expense transaction");
    	
    	// Should not be returned (not of proper type or in date range)
    	insertTestTransactionForUser(testUser, "EXPENSE", dateFormatter.parse("23.12.2019"), "My fourth expense transaction");
    	insertTestTransactionForUser(testUser, "EXPENSE", dateFormatter.parse("19.06.2020"), "My fifth expense transaction");
    	insertTestTransactionForUser(testUser, "INCOME", new Date(), "My income transaction");
    	insertTestTransactionForUser(testUser, "TRANSFER", new Date(), "My transfer transaction");
		
		Page<Transaction> pageOfTransactions = transactionService.getTransactionsBetweenDatesByTypeAndPageable("EXPENSE", dateFormatter.parse("24.12.2019"), dateFormatter.parse("17.06.2020"), PageRequest.of(0, 5));
		assertTrue( pageOfTransactions.getNumberOfElements() == 3 );
		
		List<Transaction> transactions = pageOfTransactions.getContent();
		assertTrue( transactions.size() == 3 );
		assertTrue( transactions.get(0).getDescription().equals(firstExpenseTransaction.getDescription()) );
		assertTrue( transactions.get(1).getDescription().equals(secondExpenseTransaction.getDescription()) );
		assertTrue( transactions.get(2).getDescription().equals(thirdExpenseTransaction.getDescription()) );
	}
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTransactionsBetweenDatesByTypeAndPageableMethod_ShouldReturnTransferTransactionsBetweenDates() throws Exception {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
    	
    	// Should be returned
		TransactionEntity firstTransferTransaction = insertTestTransactionForUser(testUser, "TRANSFER", dateFormatter.parse("24.12.2019"), "My first transfer transaction");
    	TransactionEntity secondTransferTransaction = insertTestTransactionForUser(testUser, "TRANSFER", dateFormatter.parse("12.05.2020"), "My second transfer transaction");
    	TransactionEntity thirdTransferTransaction = insertTestTransactionForUser(testUser, "TRANSFER", dateFormatter.parse("17.06.2020"), "My third transfer transaction");
    	
    	// Should not be returned (not of proper type or in date range)
    	insertTestTransactionForUser(testUser, "TRANSFER", dateFormatter.parse("23.12.2019"), "My fourth transfer transaction");
    	insertTestTransactionForUser(testUser, "TRANSFER", dateFormatter.parse("19.06.2020"), "My fifth transfer transaction");
    	insertTestTransactionForUser(testUser, "INCOME", new Date(), "My income transaction");
    	insertTestTransactionForUser(testUser, "EXPENSE", new Date(), "My expense transaction");
		
		Page<Transaction> pageOfTransactions = transactionService.getTransactionsBetweenDatesByTypeAndPageable("TRANSFER", dateFormatter.parse("24.12.2019"), dateFormatter.parse("17.06.2020"), PageRequest.of(0, 5));
		assertTrue( pageOfTransactions.getNumberOfElements() == 3 );
		
		List<Transaction> transactions = pageOfTransactions.getContent();
		assertTrue( transactions.size() == 3 );
		assertTrue( transactions.get(0).getDescription().equals(firstTransferTransaction.getDescription()) );
		assertTrue( transactions.get(1).getDescription().equals(secondTransferTransaction.getDescription()) );
		assertTrue( transactions.get(2).getDescription().equals(thirdTransferTransaction.getDescription()) );
	}
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTransactionsBetweenDatesByTypeAndPageableMethod_ShouldReturnOneTransferTransactionBetweenDates() throws Exception {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
    	
		TransactionEntity firstTransferTransaction = insertTestTransactionForUser(testUser, "TRANSFER", dateFormatter.parse("24.12.2019"), "My first transfer transaction");
		insertTestTransactionForUser(testUser, "TRANSFER", dateFormatter.parse("12.05.2020"), "My second transfer transaction");
    	insertTestTransactionForUser(testUser, "TRANSFER", dateFormatter.parse("17.06.2020"), "My third transfer transaction");

    	insertTestTransactionForUser(testUser, "TRANSFER", dateFormatter.parse("23.12.2019"), "My fourth transfer transaction");
    	insertTestTransactionForUser(testUser, "TRANSFER", dateFormatter.parse("19.06.2020"), "My fifth transfer transaction");
    	insertTestTransactionForUser(testUser, "INCOME", new Date(), "My income transaction");
    	insertTestTransactionForUser(testUser, "EXPENSE", new Date(), "My expense transaction");
		
		Page<Transaction> pageOfTransactions = transactionService.getTransactionsBetweenDatesByTypeAndPageable(
			"TRANSFER", 
			dateFormatter.parse("24.12.2019"), 
			dateFormatter.parse("17.06.2020"),
			PageRequest.of(0, 1)
		);
		assertTrue( pageOfTransactions.getNumberOfElements() == 1 );
		
		List<Transaction> transactions = pageOfTransactions.getContent();
		assertTrue( transactions.size() == 1 );
		assertTrue( transactions.get(0).getDescription().equals(firstTransferTransaction.getDescription()) );
	}
	
	@Test(expected = NoSuchElementException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTransactionsBetweenDatesByTypeAndPageableMethod_ShouldThrowExceptionBecauseOfMissingUser() {
		transactionService.getTransactionsBetweenDatesByTypeAndPageable("INCOME", new Date(), new Date(), PageRequest.of(0, 5));
	}
	
	@Test(expected = InvalidDataException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTransactionsBetweenDatesByTypeAndPageableMethod_ShouldThrowExceptionBecauseOfInvalidType() {
		insertTestUser(TEST_USER_EMAIL);
		transactionService.getTransactionsBetweenDatesByTypeAndPageable("INVALID-TRANSACTION-TYPE", new Date(), new Date(), PageRequest.of(0, 5));
	}
	
	/**
	 * getTotalTransactionsSumBetweenDatesByType() tests
	 */
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTotalTransactionsSumBetweenDatesByTypeMethod_ShouldReturnIncomeTransactionsBetweenDatesTotalSum() throws Exception {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
    	
    	// Should be used for sum calculation
    	TransactionEntity firstIncomeTransaction = insertTestTransactionForUser(testUser, "INCOME", dateFormatter.parse("24.12.2019"), "My first income transaction");
    	TransactionEntity secondIncomeTransaction = insertTestTransactionForUser(testUser, "INCOME", dateFormatter.parse("12.05.2020"), "My second income transaction");
    	TransactionEntity thirdIncomeTransaction = insertTestTransactionForUser(testUser, "INCOME", dateFormatter.parse("17.06.2020"), "My third income transaction");
    	
    	// Should not be used for sum calculation (not of proper type or in date range)
    	insertTestTransactionForUser(testUser, "INCOME", dateFormatter.parse("23.12.2019"), "My fourth income transaction");
    	insertTestTransactionForUser(testUser, "INCOME", dateFormatter.parse("19.06.2020"), "My fifth income transaction");
    	insertTestTransactionForUser(testUser, "EXPENSE", new Date(), "My expense transaction");
    	insertTestTransactionForUser(testUser, "TRANSFER", new Date(), "My transfer transaction");
		
		Double expectedTotalSum = firstIncomeTransaction.getSum() + secondIncomeTransaction.getSum() + thirdIncomeTransaction.getSum();
		Double actualTotalSum = transactionService.getTotalTransactionsSumBetweenDatesByType("INCOME", dateFormatter.parse("24.12.2019"), dateFormatter.parse("17.06.2020"));
		assertEquals(expectedTotalSum, actualTotalSum);
	}
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTotalTransactionsSumBetweenDatesByTypeMethod_ShouldReturnExpenseTransactionsBetweenDatesTotalSum() throws Exception {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
    	
    	// Should be used for sum calculation
    	TransactionEntity firstExpenseTransaction = insertTestTransactionForUser(testUser, "EXPENSE", dateFormatter.parse("24.12.2019"), "My first expense transaction");
    	TransactionEntity secondExpenseTransaction = insertTestTransactionForUser(testUser, "EXPENSE", dateFormatter.parse("12.05.2020"), "My second expense transaction");
    	TransactionEntity thirdExpenseTransaction = insertTestTransactionForUser(testUser, "EXPENSE", dateFormatter.parse("17.06.2020"), "My third expense transaction");
    	
    	// Should not be used for sum calculation (not of proper type or in date range)
    	insertTestTransactionForUser(testUser, "EXPENSE", dateFormatter.parse("23.12.2019"), "My fourth expense transaction");
    	insertTestTransactionForUser(testUser, "EXPENSE", dateFormatter.parse("19.06.2020"), "My fifth expense transaction");
    	insertTestTransactionForUser(testUser, "INCOME", new Date(), "My income transaction");
    	insertTestTransactionForUser(testUser, "TRANSFER", new Date(), "My transfer transaction");
		
		Double expectedTotalSum = firstExpenseTransaction.getSum() + secondExpenseTransaction.getSum() + thirdExpenseTransaction.getSum();
		Double actualTotalSum = transactionService.getTotalTransactionsSumBetweenDatesByType("EXPENSE", dateFormatter.parse("24.12.2019"), dateFormatter.parse("17.06.2020"));
		assertEquals(expectedTotalSum, actualTotalSum);
	}
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTotalTransactionsSumBetweenDatesByTypeMethod_ShouldReturnTransferTransactionsBetweenDatesTotalSum() throws Exception {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
    	
    	// Should be used for sum calculation
    	TransactionEntity firstTransferTransaction = insertTestTransactionForUser(testUser, "TRANSFER", dateFormatter.parse("24.12.2019"), "My first transfer transaction");
    	TransactionEntity secondTransferTransaction = insertTestTransactionForUser(testUser, "TRANSFER", dateFormatter.parse("12.05.2020"), "My second transfer transaction");
    	TransactionEntity thirdTransferTransaction = insertTestTransactionForUser(testUser, "TRANSFER", dateFormatter.parse("17.06.2020"), "My third transfer transaction");
    	
    	// Should not be used for sum calculation (not of proper type or in date range)
    	insertTestTransactionForUser(testUser, "TRANSFER", dateFormatter.parse("23.12.2019"), "My fourth transfer transaction");
    	insertTestTransactionForUser(testUser, "TRANSFER", dateFormatter.parse("19.06.2020"), "My fifth transfer transaction");
    	insertTestTransactionForUser(testUser, "INCOME", new Date(), "My income transaction");
    	insertTestTransactionForUser(testUser, "EXPENSE", new Date(), "My expense transaction");
		
		Double expectedTotalSum = firstTransferTransaction.getSum() + secondTransferTransaction.getSum() + thirdTransferTransaction.getSum();
		Double actualTotalSum = transactionService.getTotalTransactionsSumBetweenDatesByType("TRANSFER", dateFormatter.parse("24.12.2019"), dateFormatter.parse("17.06.2020"));
		assertEquals(expectedTotalSum, actualTotalSum);
	}

	@Test(expected = NoSuchElementException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTotalTransactionsSumBetweenDatesByTypeMethod_ShouldThrowExceptionBecauseOfMissingUser() {
		transactionService.getTotalTransactionsSumBetweenDatesByType("INCOME", new Date(), new Date());
	}
	
	@Test(expected = InvalidDataException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTotalTransactionsSumBetweenDatesByTypeMethod_ShouldThrowExceptionBecauseOfInvalidType() {
		insertTestUser(TEST_USER_EMAIL);
		transactionService.getTotalTransactionsSumBetweenDatesByType("INVALID-TRANSACTION-TYPE", new Date(), new Date());
	}
	
	/**
	 * getTransactionsByFromDataAndPageable() tests
	 */
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTransactionsByFromDataAndPageableMethod_ShouldReturnTransactionsWithAccountFromTypeAndId() throws Exception {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		// Note: only EXPENSE & TRANSFER type transactions have an ACCOUNT fromType.
    	TransactionEntity firstExpenseTransaction = insertTestTransactionForUser(testUser, "EXPENSE", new Date(), "My first expense transaction");
    	TransactionEntity secondExpenseTransaction = insertTestTransactionForUser(testUser, "EXPENSE", new Date(), "My second expense transaction");
    	TransactionEntity transferTransaction = insertTestTransactionForUser(testUser, "TRANSFER", new Date(), "My transfer transaction");
    	insertTestTransactionForUser(testUser, "INCOME", new Date(), "My income transaction");
    	
    	Long firstExpenseTransactionFromId = firstExpenseTransaction.getFromId();
    	Long secondExpenseTransactionFromId = secondExpenseTransaction.getFromId();
    	Long transferTransactionFromId = transferTransaction.getFromId();
    	
    	assertTrue( transactionRepository.count() == 4 );
    	assertTrue( firstExpenseTransactionFromId == secondExpenseTransactionFromId );
    	assertTrue( secondExpenseTransactionFromId == transferTransactionFromId );
    	assertTrue( firstExpenseTransactionFromId == transferTransactionFromId );
    	
    	Page<Transaction> pageOfTransactions = transactionService.getTransactionsByFromDataAndPageable(
    		TransactionFromType.ACCOUNT, 
    		firstExpenseTransactionFromId, 
    		PageRequest.of(0, 5)
    	);
		assertTrue( pageOfTransactions.getNumberOfElements() == 3 );
		
		List<Transaction> transactions = pageOfTransactions.getContent();
		assertTrue( transactions.size() == 3 );
		assertTrue( transactions.get(0).getDescription().equals(firstExpenseTransaction.getDescription()) );
		assertTrue( transactions.get(1).getDescription().equals(secondExpenseTransaction.getDescription()) );
		assertTrue( transactions.get(2).getDescription().equals(transferTransaction.getDescription()) );
	}
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTransactionsByFromDataAndPageableMethod_ShouldReturnTransactionsWithCategoryFromTypeAndId() throws Exception {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		// Note: only INCOME type transactions have a CATEGORY fromType.
    	TransactionEntity firstIncomeTransaction = insertTestTransactionForUser(testUser, "INCOME", new Date(), "My first income transaction");
    	TransactionEntity secondIncomeTransaction = insertTestTransactionForUser(testUser, "INCOME", new Date(), "My second income transaction");
    	insertTestTransactionForUser(testUser, "TRANSFER", new Date(), "My transfer transaction");
    	insertTestTransactionForUser(testUser, "EXPENSE", new Date(), "My expense transaction");
    	
    	Long firstIncomeTransactionFromId = firstIncomeTransaction.getFromId();
    	Long secondIncomeTransactionFromId = secondIncomeTransaction.getFromId();
    	
    	assertTrue( transactionRepository.count() == 4 );
    	assertTrue( firstIncomeTransactionFromId == secondIncomeTransactionFromId );
    	
    	Page<Transaction> pageOfTransactions = transactionService.getTransactionsByFromDataAndPageable(
    		TransactionFromType.CATEGORY, 
    		firstIncomeTransactionFromId, 
    		PageRequest.of(0, 5)
    	);
		assertTrue( pageOfTransactions.getNumberOfElements() == 2 );
		
		List<Transaction> transactions = pageOfTransactions.getContent();
		assertTrue( transactions.size() == 2 );
		assertTrue( transactions.get(0).getDescription().equals(firstIncomeTransaction.getDescription()) );
		assertTrue( transactions.get(1).getDescription().equals(secondIncomeTransaction.getDescription()) );
	}
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTransactionsByFromDataAndPageableMethod_ShouldReturnOneTransactionWithCategoryFromTypeAndId() throws Exception {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		// Note: only INCOME type transactions have a CATEGORY fromType.
    	TransactionEntity firstIncomeTransaction = insertTestTransactionForUser(testUser, "INCOME", new Date(), "My first income transaction");
    	TransactionEntity secondIncomeTransaction = insertTestTransactionForUser(testUser, "INCOME", new Date(), "My second income transaction");
    	insertTestTransactionForUser(testUser, "TRANSFER", new Date(), "My transfer transaction");
    	insertTestTransactionForUser(testUser, "EXPENSE", new Date(), "My expense transaction");
    	
    	Long firstIncomeTransactionFromId = firstIncomeTransaction.getFromId();
    	Long secondIncomeTransactionFromId = secondIncomeTransaction.getFromId();
    	
    	assertTrue( transactionRepository.count() == 4 );
    	assertTrue( firstIncomeTransactionFromId == secondIncomeTransactionFromId );
    	
    	Page<Transaction> pageOfTransactions = transactionService.getTransactionsByFromDataAndPageable(
    		TransactionFromType.CATEGORY, 
    		firstIncomeTransactionFromId, 
    		PageRequest.of(0, 1)
    	);
		assertTrue( pageOfTransactions.getNumberOfElements() == 1 );
		
		List<Transaction> transactions = pageOfTransactions.getContent();
		assertTrue( transactions.size() == 1 );
		assertTrue( transactions.get(0).getDescription().equals(firstIncomeTransaction.getDescription()) );
	}
	
	@Test(expected = NoSuchElementException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTransactionsByFromDataAndPageableMethod_ShouldThrowExceptionBecauseOfMissingUser() {
		transactionService.getTransactionsByFromDataAndPageable(
    		TransactionFromType.CATEGORY, 
    		1L, 
    		PageRequest.of(0, 1)
    	);
	}
	
	/**
	 * getTransactionsByToDataAndPageable() tests
	 */
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTransactionsByToDataAndPageableMethod_ShouldReturnTransactionsWithAccountToTypeAndId() throws Exception {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		// Note: only INCOME & TRANSFER type transactions have an ACCOUNT toType.
    	TransactionEntity firstIncomeTransaction = insertTestTransactionForUser(testUser, "INCOME", new Date(), "My first income transaction");
    	TransactionEntity secondIncomeTransaction = insertTestTransactionForUser(testUser, "INCOME", new Date(), "My second income transaction");
    	TransactionEntity transferTransaction = insertTestTransactionForUser(testUser, "TRANSFER", new Date(), "My transfer transaction");
    	insertTestTransactionForUser(testUser, "EXPENSE", new Date(), "My expense transaction");
    	
    	Long firstIncomeTransactionToId = firstIncomeTransaction.getToId();
    	Long secondIncomeTransactionToId = secondIncomeTransaction.getToId();
    	Long transferTransactionToId = transferTransaction.getToId();
    	
    	assertTrue( transactionRepository.count() == 4 );
    	assertTrue( firstIncomeTransactionToId == secondIncomeTransactionToId );
    	assertTrue( secondIncomeTransactionToId == transferTransactionToId );
    	assertTrue( firstIncomeTransactionToId == transferTransactionToId );
    	
    	Page<Transaction> pageOfTransactions = transactionService.getTransactionsByToDataAndPageable(
    		TransactionToType.ACCOUNT, 
    		firstIncomeTransactionToId, 
    		PageRequest.of(0, 5)
    	);
		assertTrue( pageOfTransactions.getNumberOfElements() == 3 );
		
		List<Transaction> transactions = pageOfTransactions.getContent();
		assertTrue( transactions.size() == 3 );
		assertTrue( transactions.get(0).getDescription().equals(firstIncomeTransaction.getDescription()) );
		assertTrue( transactions.get(1).getDescription().equals(secondIncomeTransaction.getDescription()) );
		assertTrue( transactions.get(2).getDescription().equals(transferTransaction.getDescription()) );
	}

	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTransactionsByToDataAndPageableMethod_ShouldReturnTransactionsWithCategoryToTypeAndId() throws Exception {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		// Note: only EXPENSE type transactions have a CATEGORY toType.
    	TransactionEntity firstExpenseTransaction = insertTestTransactionForUser(testUser, "EXPENSE", new Date(), "My first expense transaction");
    	TransactionEntity secondExpenseTransaction = insertTestTransactionForUser(testUser, "EXPENSE", new Date(), "My second expense transaction");
    	insertTestTransactionForUser(testUser, "TRANSFER", new Date(), "My transfer transaction");
    	insertTestTransactionForUser(testUser, "INCOME", new Date(), "My income transaction");
    	
    	Long firstExpenseTransactionToId = firstExpenseTransaction.getToId();
    	Long secondExpenseTransactionToId = secondExpenseTransaction.getToId();
    	
    	assertTrue( transactionRepository.count() == 4 );
    	assertTrue( firstExpenseTransactionToId == secondExpenseTransactionToId );
    	
    	Page<Transaction> pageOfTransactions = transactionService.getTransactionsByToDataAndPageable(
    		TransactionToType.CATEGORY, 
    		firstExpenseTransactionToId, 
    		PageRequest.of(0, 5)
    	);
		assertTrue( pageOfTransactions.getNumberOfElements() == 2 );
		
		List<Transaction> transactions = pageOfTransactions.getContent();
		assertTrue( transactions.size() == 2 );
		assertTrue( transactions.get(0).getDescription().equals(firstExpenseTransaction.getDescription()) );
		assertTrue( transactions.get(1).getDescription().equals(secondExpenseTransaction.getDescription()) );
	}
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTransactionsByToDataAndPageableMethod_ShouldReturnOneTransactionWithCategoryToTypeAndId() throws Exception {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		// Note: only EXPENSE type transactions have a CATEGORY toType.
    	TransactionEntity firstExpenseTransaction = insertTestTransactionForUser(testUser, "EXPENSE", new Date(), "My first expense transaction");
    	TransactionEntity secondExpenseTransaction = insertTestTransactionForUser(testUser, "EXPENSE", new Date(), "My second expense transaction");
    	insertTestTransactionForUser(testUser, "TRANSFER", new Date(), "My transfer transaction");
    	insertTestTransactionForUser(testUser, "INCOME", new Date(), "My income transaction");
    	
    	Long firstExpenseTransactionToId = firstExpenseTransaction.getToId();
    	Long secondExpenseTransactionToId = secondExpenseTransaction.getToId();
    	
    	assertTrue( transactionRepository.count() == 4 );
    	assertTrue( firstExpenseTransactionToId == secondExpenseTransactionToId );
    	
    	Page<Transaction> pageOfTransactions = transactionService.getTransactionsByToDataAndPageable(
    		TransactionToType.CATEGORY, 
    		firstExpenseTransactionToId, 
    		PageRequest.of(0, 1)
    	);
		assertTrue( pageOfTransactions.getNumberOfElements() == 1 );
		
		List<Transaction> transactions = pageOfTransactions.getContent();
		assertTrue( transactions.size() == 1 );
		assertTrue( transactions.get(0).getDescription().equals(firstExpenseTransaction.getDescription()) );
	}

	@Test(expected = NoSuchElementException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTransactionsByToDataAndPageableMethod_ShouldThrowExceptionBecauseOfMissingUser() {
		transactionService.getTransactionsByToDataAndPageable(
    		TransactionToType.CATEGORY, 
    		1L, 
    		PageRequest.of(0, 1)
    	);
	}
	
	/**
	 * getTransactionById() tests
	 */
	
	@Test(expected = TransactionNotFoundException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTransactionByIdMethod_ShouldThrowNotFoundException() {
		insertTestUser(TEST_USER_EMAIL);
		transactionService.getTransactionById(1L);
	}
	
	@Test(expected = UserDoesNotOwnResourceException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTransactionByIdMethod_ShouldThrowUserDoesNotOwnResourceException() {
		insertTestUser(TEST_USER_EMAIL);
		UserEntity anotherTestUser = insertTestUser("another_email@pfm.com");
		TransactionEntity testTransaction = insertTestTransactionForUser(anotherTestUser, "INCOME", new Date(), "My transaction");
		Long testTransactionId = testTransaction.getId();
		transactionService.getTransactionById(testTransactionId);
	}

	@Test(expected = NullPointerException.class)
	public void getTransactionByIdMethod_ShouldThrowExceptionBecauseOfMissingAuthentication() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		TransactionEntity testTransaction = insertTestTransactionForUser(testUser, "INCOME", new Date(), "My transaction");
		Long testTransactionId = testTransaction.getId();
		transactionService.getTransactionById(testTransactionId);
	}
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTransactionByIdMethodShouldReturnInsertedTransaction() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		TransactionEntity testTransaction = insertTestTransactionForUser(testUser, "INCOME", new Date(), "My transaction");
		Long testTransactionId = testTransaction.getId();
		
		Transaction retrievedTransaction = transactionService.getTransactionById(testTransactionId);
		
		assertTrue( transactionRepository.count() == 1 );
		assertTrue( retrievedTransaction.getDescription().equals(testTransaction.getDescription()) );
		assertTrue( retrievedTransaction.getId() == testTransaction.getId() );
	}
	
	/**
	 * createNewTransaction() tests
	 */
	
	@Test(expected = NullPointerException.class)
	public void createNewTransactionMethodShouldThrowExceptionBecauseOfMissingAuthentication() throws Exception {
		TransactionRequest testTransactionRequest = this.getTestTransactionRequest("INCOME", "My income transaction", 1L, 2L);
		transactionService.createNewTransaction(testTransactionRequest);
	}
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void createNewTransactionMethodShouldCreateTransaction() throws Exception {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		AccountEntity testAccount = this.insertTestAccountForUser(testUser, AccountType.ACTIVATED, "Account 1");
		Long testAccountId = testAccount.getId();
		Double testAccountBalance = testAccount.getBalance();
		
		CategoryEntity testIncomeCategory = this.insertTestCategoryForUser(testUser, CategoryType.INCOME, "Category 1");
		Long testIncomeCategoryId = testIncomeCategory.getId();
    	Double testIncomeCategorySum = testIncomeCategory.getCurrentPeriodSum();
		
    	TransactionRequest testTransactionRequest = this.getTestTransactionRequest("INCOME", "My income transaction", testIncomeCategoryId, testAccountId);
		Transaction createdTransaction = transactionService.createNewTransaction(testTransactionRequest);
		Long createdTransactionId = createdTransaction.getId();
		Double transactionSum = createdTransaction.getSum();
		
    	assertTrue( transactionRepository.count() == 1 );
		assertTrue( transactionRepository.existsById(createdTransactionId) );
		assertTrue( 
    		accountRepository.findById(testAccountId).get().getBalance() == (testAccountBalance + transactionSum) 
    	);
    	assertTrue( 
    		categoryRepository.findById(testIncomeCategoryId).get().getCurrentPeriodSum() == (testIncomeCategorySum + transactionSum) 
    	);
	}
	
	@Test(expected = InvalidDataException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void createNewTransactionMethodShouldThrowExceptionBecauseOfInvalidFromToData() throws Exception {
		insertTestUser(TEST_USER_EMAIL);
    	TransactionRequest testTransactionRequest = this.getTestTransactionRequest("INCOME", "My income transaction", 1L, 2L);
    	transactionService.createNewTransaction(testTransactionRequest);
	}
	
	/**
	 * updateTransactionById() tests
	 */
	
	@Test(expected = TransactionNotFoundException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void updateTransactionByIdMethod_ShouldThrowNotFoundException() throws Exception {
		insertTestUser(TEST_USER_EMAIL);
		TransactionRequest testTransactionRequest = this.getTestTransactionRequest("INCOME", "My income transaction", 1L, 2L);
		transactionService.updateTransactionById(1L, testTransactionRequest);
	}
	
	@Test(expected = UserDoesNotOwnResourceException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void updateTransactionByIdMethod_ShouldThrowUserDoesNotOwnResourceException() throws Exception {
		insertTestUser(TEST_USER_EMAIL);
		UserEntity anotherTestUser = insertTestUser("another_email@pfm.com");
		TransactionEntity testTransaction = insertTestTransactionForUser(anotherTestUser, "INCOME", new Date(), "My transaction");
		Long testTransactionId = testTransaction.getId();
		TransactionRequest testTransactionRequest = this.getTestTransactionRequest("INCOME", "My income transaction", 1L, 2L);
		transactionService.updateTransactionById(testTransactionId, testTransactionRequest);
	}

	@Test(expected = NullPointerException.class)
	public void updateTransactionByIdMethod_ShouldThrowExceptionBecauseOfMissingAuthentication() throws Exception {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		TransactionEntity testTransaction = insertTestTransactionForUser(testUser, "INCOME", new Date(), "My transaction");
		Long testTransactionId = testTransaction.getId();
		TransactionRequest testTransactionRequest = this.getTestTransactionRequest("INCOME", "My income transaction", 1L, 2L);
		transactionService.updateTransactionById(testTransactionId, testTransactionRequest);
	}
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void updateTransactionByIdMethodShouldUpdateTransaction() throws Exception {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);

		/*
    	 *  Insert test from-to entities and transaction & manually execute transaction 
    	 */
    	AccountEntity testAccount = insertTestAccountForUser(testUser, AccountType.ACTIVATED, "Account 1");
    	Long testAccountId = testAccount.getId();
    	Double initialTestAccountBalance = testAccount.getBalance();
    	
    	CategoryEntity testExpenseCategory = insertTestCategoryForUser(testUser, CategoryType.EXPENSES, "Category 1");
    	Long testExpenseCategoryId = testExpenseCategory.getId();
    	Double initialTestExpenseCategorySum = testExpenseCategory.getCurrentPeriodSum();
    	
    	TransactionEntity testTransaction = insertTestTransactionForUser(testUser, "EXPENSE", new Date(), "My first transaction");
    	testTransaction.setFromId(testAccountId);
    	testTransaction.setToId(testExpenseCategoryId);
    	testTransaction = transactionRepository.save(testTransaction);
    	Long testTransactionId = testTransaction.getId();
    	Double testTransactionSum = testTransaction.getSum();
    	
    	testAccount.setBalance( initialTestAccountBalance - testTransactionSum );
    	testAccount = accountRepository.save(testAccount);
    	testExpenseCategory.setCurrentPeriodSum( initialTestExpenseCategorySum + testTransactionSum );
    	testExpenseCategory = categoryRepository.save(testExpenseCategory);
    	
    	/*
    	 * Send transaction update request & test returned object
    	 */
    	TransactionRequest testTransactionRequest = this.getTestTransactionRequest("EXPENSE", "My updated transaction", testAccountId, testExpenseCategoryId);
    	Double updatedTransactionSum = 1337.37d;
    	testTransactionRequest.setSum(updatedTransactionSum);
    	
    	Transaction updatedTransaction = transactionService.updateTransactionById(testTransactionId, testTransactionRequest);
        assertTrue( updatedTransaction.getDescription().equals("My updated transaction") );
        assertTrue( updatedTransaction.getSum() == updatedTransactionSum );
		
    	/*
         *  Test if transaction & from-to entity data has been updated
         */
		assertTrue( transactionRepository.count() == 1 );
		TransactionEntity updatedTransactionEntity = transactionRepository.findById(testTransactionId).get();
        assertTrue( updatedTransactionEntity.getDescription().equals("My updated transaction") );
        assertTrue( updatedTransactionEntity.getFromType() == TransactionFromType.ACCOUNT );
        assertTrue( updatedTransactionEntity.getToType() == TransactionToType.CATEGORY );
        assertTrue( updatedTransactionEntity.getFromId() == testAccountId );
        assertTrue( updatedTransactionEntity.getToId() == testExpenseCategoryId );
        assertEquals( updatedTransactionSum, updatedTransactionEntity.getSum() );
        
        assertTrue( accountRepository.count() == 1 );
        AccountEntity updatedAccount = accountRepository.findById(testAccountId).get();
        assertTrue( updatedAccount.getBalance() == (initialTestAccountBalance - updatedTransactionSum) );
        
        assertTrue( categoryRepository.count() == 1 );
        CategoryEntity updatedExpenseCategory = categoryRepository.findById(testExpenseCategoryId).get();
        assertTrue( updatedExpenseCategory.getCurrentPeriodSum() == (initialTestExpenseCategorySum + updatedTransactionSum) );
	}
	
	@Test(expected = InvalidDataException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void updateTransactionByIdMethodShouldThrowExceptionBecauseOfInvalidFromToData() throws Exception {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);

    	TransactionEntity testTransaction = insertTestTransactionForUser(testUser, "EXPENSE", new Date(), "My first transaction");
    	Long testTransactionId = testTransaction.getId();
    	
    	TransactionRequest testTransactionRequest = this.getTestTransactionRequest("EXPENSE", "My updated transaction", 1L, 2L);
    	Double updatedTransactionSum = 1337.37d;
    	testTransactionRequest.setSum(updatedTransactionSum);
    	
    	transactionService.updateTransactionById(testTransactionId, testTransactionRequest);
	}
	
	/**
	 * deleteTransactionById() tests
	 */
	
	@Test(expected = TransactionNotFoundException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void deleteTransactionByIdMethod_ShouldThrowNotFoundException() {
		insertTestUser(TEST_USER_EMAIL);
		transactionService.deleteTransactionById(1L);
	}
	
	@Test(expected = UserDoesNotOwnResourceException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void deleteTransactionByIdMethod_ShouldThrowUserDoesNotOwnResourceException() {
		insertTestUser(TEST_USER_EMAIL);
		UserEntity anotherTestUser = insertTestUser("another_email@pfm.com");
		TransactionEntity testTransaction = insertTestTransactionForUser(anotherTestUser, "INCOME", new Date(), "My transaction");
		Long testTransactionId = testTransaction.getId();
		transactionService.deleteTransactionById(testTransactionId);
	}

	@Test(expected = NullPointerException.class)
	public void deleteTransactionByIdMethod_ShouldThrowExceptionBecauseOfMissingAuthentication() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		TransactionEntity testTransaction = insertTestTransactionForUser(testUser, "INCOME", new Date(), "My transaction");
		Long testTransactionId = testTransaction.getId();
		transactionService.deleteTransactionById(testTransactionId);
	}
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void deleteTransactionByIdMethodShouldDeleteAndUndoInsertedTransaction() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);

    	AccountEntity testAccount = insertTestAccountForUser(testUser, AccountType.ACTIVATED, "Account 1");
    	Long testAccountId = testAccount.getId();
    	Double testAccountBalance = testAccount.getBalance();
    	
    	CategoryEntity testExpenseCategory = insertTestCategoryForUser(testUser, CategoryType.EXPENSES, "Category 1");
    	Long testExpenseCategoryId = testExpenseCategory.getId();
    	Double testExpenseCategorySum = testExpenseCategory.getCurrentPeriodSum();
		
    	TransactionEntity testTransaction = insertTestTransactionForUser(testUser, "EXPENSE", new Date(), "My first transaction");
    	testTransaction.setFromId(testAccountId);
    	testTransaction.setToId(testExpenseCategoryId);
    	testTransaction = transactionRepository.save(testTransaction);
    	Long testTransactionId = testTransaction.getId();
    	Double testTransactionSum = testTransaction.getSum();
		
		Transaction deletedTransaction = transactionService.deleteTransactionById(testTransactionId);
		assertTrue( deletedTransaction.getDescription().equals("My first transaction"));
		assertTrue( transactionRepository.count() == 0 );
        // Account balance should increase & category sum should decrease, because the EXPENSE transaction is undone by the deletion operation
        assertTrue( accountRepository.findById(testAccountId).get().getBalance() == (testAccountBalance+testTransactionSum) );
        assertTrue( categoryRepository.findById(testExpenseCategoryId).get().getCurrentPeriodSum() == (testExpenseCategorySum-testTransactionSum) ); 
	}
	
}