package com.mse.personal.finance.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
import com.mse.personal.finance.model.AccountType;
import com.mse.personal.finance.model.Category;
import com.mse.personal.finance.model.CategoryType;
import com.mse.personal.finance.model.FamilyStatusType;
import com.mse.personal.finance.model.GenderType;
import com.mse.personal.finance.model.TransactionFromType;
import com.mse.personal.finance.model.TransactionToType;
import com.mse.personal.finance.model.request.CategoryLimitUpdateRequest;
import com.mse.personal.finance.model.request.CategoryRequest;
import com.mse.personal.finance.rest.exception.CategoryNotFoundException;
import com.mse.personal.finance.rest.exception.InvalidDataException;
import com.mse.personal.finance.rest.exception.NameAlreadyExistsException;
import com.mse.personal.finance.rest.exception.UserDoesNotOwnResourceException;

/**
 * This class implements unit tests for the CategoryService class.
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
public class CategoryServiceTests {
	
	private static final String TEST_USER_EMAIL = "test@pfm.com";
	private static final String TEST_USER_PASSWORD = "123456";
	
	@Autowired
	private CategoryService categoryService;
	
	@Autowired
	private CategoryRepository categoryRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private AccountRepository accountRepository;
	
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
	 * Create and return a test category request
	 */
	public CategoryRequest getTestCategoryRequest() {
		CategoryRequest testCategoryRequest = new CategoryRequest();
		
		testCategoryRequest.setName("Category 1");
		testCategoryRequest.setType(CategoryType.EXPENSES);
		testCategoryRequest.setCurrentPeriodSum(200.00d);
		testCategoryRequest.setLimit("SOME-LIMIT");
    	
		return testCategoryRequest;
	}
	
	/**
	 * Create and return a test category limit update request
	 */
	public CategoryLimitUpdateRequest getTestCategoryLimitUpdateRequest() {
		CategoryLimitUpdateRequest testCategoryLimitUpdateRequest = new CategoryLimitUpdateRequest();
		
		testCategoryLimitUpdateRequest.setLimit("SOME-LIMIT-UPDATED");
    	
		return testCategoryLimitUpdateRequest;
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
	 * getAllNonSystemCategories() tests
	 */
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getAllNonSystemCategoriesMethodShouldReturnAllNonSystemCatogires() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		CategoryEntity firstTestCategory = insertTestCategoryForUser(testUser, CategoryType.EXPENSES, "My Expenses Category");
		CategoryEntity secondTestCategory = insertTestCategoryForUser(testUser, CategoryType.INCOME, "My Income Category");
		insertTestCategoryForUser(testUser, CategoryType.EXPENSES, CategoryService.SYSTEM_EXPENSES_CATEGORY_NAME);
		insertTestCategoryForUser(testUser, CategoryType.INCOME, CategoryService.SYSTEM_INCOME_CATEGORY_NAME);
		
		assertTrue( categoryRepository.count() == 4 );
		
		List<Category> nonSystemCategories = categoryService.getAllNonSystemCategories();
		assertTrue( nonSystemCategories.size() == 2 );
		assertTrue( nonSystemCategories.get(0).getName().equals( firstTestCategory.getName() ) );
		assertTrue( nonSystemCategories.get(1).getName().equals( secondTestCategory.getName() ) );
	}
	
	@Test(expected = NoSuchElementException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getAllNonSystemCategoriesMethodShouldThrowExceptionBecauseOfMissingUser() {
		categoryService.getAllNonSystemCategories();
	}
	
	/**
	 * getAllNonSystemCategoriesByType() tests
	 */
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getAllNonSystemCategoriesByTypeMethodShouldReturnAllNonSystemExpensesCategories() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		CategoryEntity expenseCategory = insertTestCategoryForUser(testUser, CategoryType.EXPENSES, "My Expenses Category");
		insertTestCategoryForUser(testUser, CategoryType.INCOME, "My Income Category");
		insertTestCategoryForUser(testUser, CategoryType.EXPENSES, CategoryService.SYSTEM_EXPENSES_CATEGORY_NAME);
		insertTestCategoryForUser(testUser, CategoryType.INCOME, CategoryService.SYSTEM_INCOME_CATEGORY_NAME);
		
		assertTrue( categoryRepository.count() == 4 );
		
		List<Category> expenseCategories = categoryService.getAllNonSystemCategoriesByType(CategoryType.EXPENSES);
		assertTrue( expenseCategories.size() == 1 );
		assertTrue( expenseCategories.get(0).getName().equals( expenseCategory.getName() ) );
	}
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getAllNonSystemCategoriesByTypeMethodShouldReturnAllNonSystemIncomeCategories() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		insertTestCategoryForUser(testUser, CategoryType.EXPENSES, "My Expenses Category");
		CategoryEntity incomeCategory = insertTestCategoryForUser(testUser, CategoryType.INCOME, "My Income Category");
		insertTestCategoryForUser(testUser, CategoryType.EXPENSES, CategoryService.SYSTEM_EXPENSES_CATEGORY_NAME);
		insertTestCategoryForUser(testUser, CategoryType.INCOME, CategoryService.SYSTEM_INCOME_CATEGORY_NAME);
		
		assertTrue( categoryRepository.count() == 4 );
		
		List<Category> expenseCategories = categoryService.getAllNonSystemCategoriesByType(CategoryType.INCOME);
		assertTrue( expenseCategories.size() == 1 );
		assertTrue( expenseCategories.get(0).getName().equals( incomeCategory.getName() ) );
	}
	
	@Test(expected = NoSuchElementException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getAllNonSystemCategoriesByTypeMethodShouldThrowExceptionBecauseOfMissingUser() {
		categoryService.getAllNonSystemCategoriesByType(CategoryType.EXPENSES);
	}
	
	/**
	 * getTotalCurrentPeriodSumOfCategoriesByType() tests
	 */
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void  getTotalCurrentPeriodSumOfCategoriesByTypeMethodShouldReturnExpenseCategoriesTotalSum() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		CategoryEntity firstTestCategory = insertTestCategoryForUser(testUser, CategoryType.EXPENSES, "My First Expenses Category");
		CategoryEntity secondTestCategory = insertTestCategoryForUser(testUser, CategoryType.EXPENSES, "My Second Expenses Category");
		
		insertTestCategoryForUser(testUser, CategoryType.INCOME, "My Income Category");
		insertTestCategoryForUser(testUser, CategoryType.EXPENSES, CategoryService.SYSTEM_EXPENSES_CATEGORY_NAME);
		insertTestCategoryForUser(testUser, CategoryType.INCOME, CategoryService.SYSTEM_INCOME_CATEGORY_NAME);
		
		Double firstTestCategorySum = firstTestCategory.getCurrentPeriodSum();
		Double secondTestCategorySum = secondTestCategory.getCurrentPeriodSum();
		
		Double expectedTotalSum = firstTestCategorySum + secondTestCategorySum;
		Double actualTotalSum = categoryService.getTotalCurrentPeriodSumOfCategoriesByType(CategoryType.EXPENSES);
		assertEquals(expectedTotalSum, actualTotalSum);
	}
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void  getTotalCurrentPeriodSumOfCategoriesByTypeMethodShouldReturnIncomeCategoriesTotalSum() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		CategoryEntity firstTestCategory = insertTestCategoryForUser(testUser, CategoryType.INCOME, "My First Income Category");
		CategoryEntity secondTestCategory = insertTestCategoryForUser(testUser, CategoryType.INCOME, "My Second Income Category");
		
		insertTestCategoryForUser(testUser, CategoryType.EXPENSES, "My Expenses Category");
		insertTestCategoryForUser(testUser, CategoryType.EXPENSES, CategoryService.SYSTEM_EXPENSES_CATEGORY_NAME);
		insertTestCategoryForUser(testUser, CategoryType.INCOME, CategoryService.SYSTEM_INCOME_CATEGORY_NAME);
		
		Double firstTestCategorySum = firstTestCategory.getCurrentPeriodSum();
		Double secondTestCategorySum = secondTestCategory.getCurrentPeriodSum();
		
		Double expectedTotalSum = firstTestCategorySum + secondTestCategorySum;
		Double actualTotalSum = categoryService.getTotalCurrentPeriodSumOfCategoriesByType(CategoryType.INCOME);
		assertEquals(expectedTotalSum, actualTotalSum);
	}
	
	@Test(expected = NoSuchElementException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTotalCurrentPeriodSumOfCategoriesByTypeMethodShouldThrowExceptionBecauseOfMissingUser() {
		categoryService.getTotalCurrentPeriodSumOfCategoriesByType(CategoryType.EXPENSES);
	}
	
	/**
	 * getCategoryById() tests
	 */
	
	@Test(expected = CategoryNotFoundException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getCategoryByIdMethodShouldThrowNotFoundException() {
		insertTestUser(TEST_USER_EMAIL);
		categoryService.getCategoryById(1L);
	}
	
	@Test(expected = UserDoesNotOwnResourceException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getCategoryByIdMethodShouldThrowUserDoesNotOwnResourceException() {
		insertTestUser(TEST_USER_EMAIL);
		UserEntity anotherTestUser = insertTestUser("another_email@pfm.com");
		CategoryEntity testCategory = insertTestCategoryForUser(anotherTestUser, CategoryType.EXPENSES, "My Category");
		Long testCategoryId = testCategory.getId();
		categoryService.getCategoryById(testCategoryId);
	}
	
	@Test(expected = NullPointerException.class)
	public void getCategoryByIdMethodShouldThrowExceptionBecauseOfMissingAuthentication() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		CategoryEntity testCategory = insertTestCategoryForUser(testUser, CategoryType.EXPENSES, "My Category");
		Long testCategoryId = testCategory.getId();
		categoryService.getCategoryById(testCategoryId);
	}
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getCategoryByIdMethodShouldReturnInsertedCategory() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		CategoryEntity testCategory = insertTestCategoryForUser(testUser, CategoryType.EXPENSES, "My Category");
		Long testCategoryId = testCategory.getId();
		
		Category retrievedCategory = categoryService.getCategoryById(testCategoryId);
		
		assertTrue( categoryRepository.count() == 1 );
		assertTrue( retrievedCategory.getName().equals(testCategory.getName()) );
		assertTrue( retrievedCategory.getId() == testCategory.getId() );
	}
	
	/**
	 * getTotalAddedSumBetweenDatesById() tests
	 */
	
	@Test(expected = CategoryNotFoundException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTotalAddedSumBetweenDatesByIdMethodShouldThrowNotFoundException() {
		insertTestUser(TEST_USER_EMAIL);
		categoryService.getTotalAddedSumBetweenDatesById(1L, new Date(), new Date());
	}
	
	@Test(expected = UserDoesNotOwnResourceException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTotalAddedSumBetweenDatesByIdMethodShouldThrowUserDoesNotOwnResourceException() {
		insertTestUser(TEST_USER_EMAIL);
		UserEntity anotherTestUser = insertTestUser("another_email@pfm.com");
		CategoryEntity testCategory = insertTestCategoryForUser(anotherTestUser, CategoryType.EXPENSES, "My Category");
		Long testCategoryId = testCategory.getId();
		categoryService.getTotalAddedSumBetweenDatesById(testCategoryId, new Date(), new Date());
	}
	
	@Test(expected = NullPointerException.class)
	public void getTotalAddedSumBetweenDatesByIdMethodShouldThrowExceptionBecauseOfMissingAuthentication() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		CategoryEntity testCategory = insertTestCategoryForUser(testUser, CategoryType.EXPENSES, "My Category");
		Long testCategoryId = testCategory.getId();
		categoryService.getTotalAddedSumBetweenDatesById(testCategoryId, new Date(), new Date());
	}
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTotalAddedSumBetweenDatesByIdMethod_ShouldReturnTotalAddedSumBetweenDatesForIncomeCategories() throws Exception {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		CategoryEntity incomeCategory = insertTestCategoryForUser(testUser, CategoryType.INCOME, "Income Category");
    	AccountEntity testAccount = insertTestAccountForUser(testUser, AccountType.ACTIVATED, "Account 1");
    	
    	SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
    	insertTestTransactionForUser(testUser, "INCOME", dateFormatter.parse("24.12.2019"), "My first income transaction", incomeCategory.getId(), testAccount.getId());
    	insertTestTransactionForUser(testUser, "INCOME", dateFormatter.parse("19.06.2020"), "My second income transaction", incomeCategory.getId(), testAccount.getId());
    	insertTestTransactionForUser(testUser, "INCOME", dateFormatter.parse("17.06.2020"), "My third income transaction", incomeCategory.getId(), testAccount.getId());
    	
    	Double result = categoryService.getTotalAddedSumBetweenDatesById( incomeCategory.getId(), dateFormatter.parse("24.12.2019"), dateFormatter.parse("17.06.2020") );
    	assertEquals( Double.valueOf(200.00d), result );
	}
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTotalAddedSumBetweenDatesByIdMethod_ShouldReturnTotalAddedSumBetweenDatesForExpenseCategories() throws Exception {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		CategoryEntity expenseCategory = insertTestCategoryForUser(testUser, CategoryType.EXPENSES, "Expense Category");
    	AccountEntity testAccount = insertTestAccountForUser(testUser, AccountType.ACTIVATED, "Account 1");
    	
    	SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
    	insertTestTransactionForUser(testUser, "EXPENSE", dateFormatter.parse("24.12.2019"), "My first expense transaction", testAccount.getId(), expenseCategory.getId());
    	insertTestTransactionForUser(testUser, "EXPENSE", dateFormatter.parse("19.06.2020"), "My second expense transaction", testAccount.getId(), expenseCategory.getId());
    	insertTestTransactionForUser(testUser, "EXPENSE", dateFormatter.parse("17.06.2020"), "My third expense transaction", testAccount.getId(), expenseCategory.getId());
    	
    	Double result = categoryService.getTotalAddedSumBetweenDatesById( expenseCategory.getId(), dateFormatter.parse("24.12.2019"), dateFormatter.parse("17.06.2020") );
    	assertEquals( Double.valueOf(200.00d), result );
	}
	
	/**
	 * createNewCategory() tests
	 */
	
	@Test(expected = NullPointerException.class)
	public void createNewCategoryMethodShouldThrowExceptionBecauseOfMissingAuthentication() {
		CategoryRequest testCategoryRequest = getTestCategoryRequest();
		categoryService.createNewCategory(testCategoryRequest);
	}
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void createNewCategoryMethodShouldCreateCategory() {
		insertTestUser(TEST_USER_EMAIL);
		
		CategoryRequest testCategoryRequest = getTestCategoryRequest();
		Category createdCategory = categoryService.createNewCategory(testCategoryRequest);
		Long createdCategoryId = createdCategory.getId();
		
		assertTrue( categoryRepository.count() == 1 );
		assertTrue( categoryRepository.existsById(createdCategoryId) );
		CategoryEntity categoryEntity = categoryRepository.findById(createdCategoryId).get();
		assertTrue( createdCategory.getName().equals(categoryEntity.getName()) );
		assertTrue( createdCategoryId == categoryEntity.getId() );
	}
	
	@Test(expected = NameAlreadyExistsException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void createNewCategoryMethodShouldThrowNameAlreadyExistsException() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		CategoryRequest testCategoryRequest = getTestCategoryRequest();
		insertTestCategoryForUser(testUser, CategoryType.EXPENSES, testCategoryRequest.getName());
		categoryService.createNewCategory(testCategoryRequest);
	}
	
	/**
	 * updateCategoryById() tests
	 */
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void updateCategoryByIdMethodShouldUpdateCategory() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		CategoryEntity testCategory = insertTestCategoryForUser(testUser, CategoryType.EXPENSES, "My Category");
		Long testCategoryId = testCategory.getId();
		
		CategoryRequest categoryUpdateRequest = getTestCategoryRequest();
		categoryUpdateRequest.setName("My Updated Category");
		
		Category updatedCategory = categoryService.updateCategoryById(testCategoryId, categoryUpdateRequest);
		
		assertTrue( categoryRepository.count() == 1 );
		assertTrue( categoryRepository.existsById(testCategoryId) );
		assertTrue( categoryRepository.findById(testCategoryId).get().getName().equals("My Updated Category") );
		assertTrue( updatedCategory.getName().equals("My Updated Category") );
	}
	
	@Test(expected = CategoryNotFoundException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void updateCategoryByIdMethodShouldThrowNotFoundException() {
		insertTestUser(TEST_USER_EMAIL);		
		CategoryRequest categoryUpdateRequest = getTestCategoryRequest();
		categoryService.updateCategoryById(1L, categoryUpdateRequest);
	}

	@Test(expected = UserDoesNotOwnResourceException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void updateCategoryByIdMethodShouldThrowUserDoesNotOwnResourceException() {
		insertTestUser(TEST_USER_EMAIL);
		UserEntity anotherTestUser = insertTestUser("another_email@pfm.com");
		
		CategoryEntity testCategory = insertTestCategoryForUser(anotherTestUser, CategoryType.EXPENSES, "My Category");
		Long testCategoryId = testCategory.getId();
		
		CategoryRequest categoryUpdateRequest = getTestCategoryRequest();
		categoryUpdateRequest.setName("My Updated Category");
		
		categoryService.updateCategoryById(testCategoryId, categoryUpdateRequest);
	}
	
	@Test(expected = NameAlreadyExistsException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void updateCategoryByIdMethodShouldThrowNameAlreadyExistsException() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		CategoryEntity testCategory = insertTestCategoryForUser(testUser, CategoryType.EXPENSES, "My Category");
		Long testCategoryId = testCategory.getId();
		
		CategoryRequest categoryUpdateRequest = getTestCategoryRequest();
		categoryUpdateRequest.setName("My Updated Category");
		insertTestCategoryForUser(testUser, CategoryType.EXPENSES, categoryUpdateRequest.getName());
		
		categoryService.updateCategoryById(testCategoryId, categoryUpdateRequest);
	}
	
	@Test(expected = NullPointerException.class)
	public void updateCategoryByIdMethodShouldThrowExceptionBecauseOfMissingAuthentication() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		CategoryEntity testCategory = insertTestCategoryForUser(testUser, CategoryType.EXPENSES, "My Category");
		Long testCategoryId = testCategory.getId();
		
		CategoryRequest categoryUpdateRequest = getTestCategoryRequest();
		categoryUpdateRequest.setName("My Updated Category");
		
		categoryService.updateCategoryById(testCategoryId, categoryUpdateRequest);;
	}
	
	/**
	 * setCategoryLimitById() tests
	 */
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void setCategoryLimitByIdMethodShouldSetCategoryLimit() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		CategoryEntity testCategory = insertTestCategoryForUser(testUser, CategoryType.EXPENSES, "My Category");
		Long testCategoryId = testCategory.getId();
		CategoryLimitUpdateRequest testCategoryLimitUpdateRequest = getTestCategoryLimitUpdateRequest();
		
		categoryService.setCategoryLimitById(testCategoryId, testCategoryLimitUpdateRequest);
		
		assertTrue( categoryRepository.count() == 1 );
		assertTrue( categoryRepository.existsById(testCategoryId) );
		assertTrue( 
				categoryRepository.findById(testCategoryId).get().getLimit()
					.equals( testCategoryLimitUpdateRequest.getLimit() )
		);
	}
	
	@Test(expected = CategoryNotFoundException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void setCategoryLimitByIdMethodShouldThrowNotFoundException() {
		insertTestUser(TEST_USER_EMAIL);
		CategoryLimitUpdateRequest testCategoryLimitUpdateRequest = getTestCategoryLimitUpdateRequest();
		categoryService.setCategoryLimitById(1L, testCategoryLimitUpdateRequest);
	}
	
	@Test(expected = UserDoesNotOwnResourceException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void setCategoryLimitByIdMethodShouldThrowUserDoesNotOwnResourceException() {
		insertTestUser(TEST_USER_EMAIL);
		UserEntity anotherTestUser = insertTestUser("another_email@pfm.com");
		
		CategoryEntity testCategory = insertTestCategoryForUser(anotherTestUser, CategoryType.EXPENSES, "My Category");
		Long testCategoryId = testCategory.getId();
		CategoryLimitUpdateRequest testCategoryLimitUpdateRequest = getTestCategoryLimitUpdateRequest();
		
		categoryService.setCategoryLimitById(testCategoryId, testCategoryLimitUpdateRequest);
	}
	
	/**
	 * deleteCategoryById() tests
	 */
	
	@Test
    @WithMockUser(username = TEST_USER_EMAIL)
	public void deleteCategoryByIdMethodShouldDeleteAccount() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		CategoryEntity testCategory = insertTestCategoryForUser(testUser, CategoryType.EXPENSES, "My Category");
		Long testCategoryId = testCategory.getId();
		
		assertTrue( categoryRepository.count() == 1 );
		assertTrue( categoryRepository.existsById(testCategoryId) );
		
		categoryService.deleteCategoryById(testCategoryId);
		
		assertTrue( categoryRepository.count() == 0 );
		assertFalse( categoryRepository.existsById(testCategoryId) );
	}
	
	@Test(expected = CategoryNotFoundException.class)
    @WithMockUser(username = TEST_USER_EMAIL)
	public void deleteCategoryByIdMethodShouldThrowNotFoundException() {
		insertTestUser(TEST_USER_EMAIL);
		categoryService.deleteCategoryById(1L);
	}

	@Test(expected = UserDoesNotOwnResourceException.class)
    @WithMockUser(username = TEST_USER_EMAIL)
	public void deleteCategoryByIdMethodShouldThrowUserDoesNotOwnResourceException() {
		insertTestUser(TEST_USER_EMAIL);
		UserEntity anotherTestUser = insertTestUser("another_email@pfm.com");
		
		CategoryEntity testCategory = insertTestCategoryForUser(anotherTestUser, CategoryType.EXPENSES, "My Category");
		Long testCategoryId = testCategory.getId();
		
		categoryService.deleteCategoryById(testCategoryId);
	}
	
	/**
	 * createSystemCategoriesForUser() tests
	 */
	
	@Test
    @WithMockUser(username = TEST_USER_EMAIL)
	public void createSystemCategoriesForUserMethodShouldCreateSystemCategories() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		assertTrue( categoryRepository.count() == 0 );
		
		categoryService.createSystemCategoriesForUser(testUser);
		
		assertTrue( categoryRepository.count() == 2 );
		assertTrue( categoryRepository.existsByNameAndOwner(CategoryService.SYSTEM_INCOME_CATEGORY_NAME, testUser) );
		assertTrue( categoryRepository.existsByNameAndOwner(CategoryService.SYSTEM_EXPENSES_CATEGORY_NAME, testUser) );
	}
	
	/**
	 * createExampleCategoriesForCurrentlyLoggedInUser() tests
	 */
	
	@Test
    @WithMockUser(username = TEST_USER_EMAIL)
	public void createExampleCategoriesForCurrentlyLoggedInUserMethodShouldCreateExampleCategories() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		
		assertTrue( categoryRepository.count() == 0 );
		
		List<Category> exampleCategories = categoryService.createExampleCategoriesForCurrentlyLoggedInUser();
		
		assertTrue( categoryRepository.count() == categoryService.getNumberOfExampleCategories() );
		for (Category category : exampleCategories) {
			String name = category.getName();
			assertTrue( categoryRepository.existsByNameAndOwner(name, testUser) );
		}
	}
	
	@Test(expected = NoSuchElementException.class)
    @WithMockUser(username = TEST_USER_EMAIL)
	public void createExampleCategoriesForCurrentlyLoggedInUserMethodShouldThrowExceptionBecauseOfMissingAuthentication() {
		categoryService.createExampleCategoriesForCurrentlyLoggedInUser();
	}
	
	/**
	 * performCategorySumOperationById() tests
	 */
	
	@Test
    @WithMockUser(username = TEST_USER_EMAIL)
	public void performCategorySumOperationByIdMethodShouldIncreaseCategorySum() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		CategoryEntity testCategory = insertTestCategoryForUser(testUser, CategoryType.EXPENSES, "My Category");
		Long testCategoryId = testCategory.getId();
		Double testCategorySum = testCategory.getCurrentPeriodSum();
		
		categoryService.performCategorySumOperationById(testCategoryId, '+', 100.00d);
		
		assertTrue( categoryRepository.count() == 1 );
		assertTrue( categoryRepository.existsById(testCategoryId) );
		assertTrue( 
			categoryRepository.findById(testCategoryId).get().getCurrentPeriodSum() == (testCategorySum + 100.00d)
		);
	}
	
	@Test
    @WithMockUser(username = TEST_USER_EMAIL)
	public void performCategorySumOperationByIdMethodShouldDecreaseCategorySum() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		CategoryEntity testCategory = insertTestCategoryForUser(testUser, CategoryType.EXPENSES, "My Category");
		Long testCategoryId = testCategory.getId();
		Double testCategorySum = testCategory.getCurrentPeriodSum();
		
		categoryService.performCategorySumOperationById(testCategoryId, '-', 100.00d);
		
		assertTrue( categoryRepository.count() == 1 );
		assertTrue( categoryRepository.existsById(testCategoryId) );
		assertTrue( 
			categoryRepository.findById(testCategoryId).get().getCurrentPeriodSum() == (testCategorySum - 100.00d)
		);
	}
	
	@Test
    @WithMockUser(username = TEST_USER_EMAIL)
	public void performCategorySumOperationByIdMethodShouldNotChangeCategorySum() {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		CategoryEntity testCategory = insertTestCategoryForUser(testUser, CategoryType.EXPENSES, "My Category");
		Long testCategoryId = testCategory.getId();
		Double testCategorySum = testCategory.getCurrentPeriodSum();
		
		categoryService.performCategorySumOperationById(testCategoryId, 'X', 100.00d);
		
		assertTrue( categoryRepository.count() == 1 );
		assertTrue( categoryRepository.existsById(testCategoryId) );
		assertTrue( 
			categoryRepository.findById(testCategoryId).get().getCurrentPeriodSum() == testCategorySum
		);
	}
	
}