package com.mse.personal.finance.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

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
import com.mse.personal.finance.model.TransactionFromType;
import com.mse.personal.finance.model.TransactionToType;
import com.mse.personal.finance.rest.exception.InvalidDataException;
import com.mse.personal.finance.rest.exception.UserDoesNotOwnResourceException;
import com.mse.personal.finance.service.CategoryService;

/**
 * This class implements integration tests for the CategoryController class.
 * 
 * The @Transactional annotation is used to rollback 
 * database changes (made via a MockMvc request)
 * after the annotated test has finished executing.
 * 
 * @author dvt32
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Transactional
@ActiveProfiles("test")
public class CategoryControllerTests {
	
	private static final String TEST_USER_EMAIL = "test@pfm.com";
	private static final String TEST_USER_PASSWORD = "123456";
	
	@Autowired
    private CategoryController categoryController;
	
	@Autowired
	private CategoryRepository categoryRepository;
	
	@Autowired
	private CategoryService categoryService;
	
	@Autowired
	private AccountRepository accountRepository;
	
	@Autowired
	private TransactionRepository transactionRepository;
	
	@Autowired
	private WebApplicationContext context;
	
	private MockMvc mockMvc;
	
	@Autowired
    private UserRepository userRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	/**
	 * Set context & configure Spring Security 
	 * for testing secured methods / REST endpoints.
	 */
	@Before
    public void setup() {
        this.mockMvc = MockMvcBuilders
          .webAppContextSetup(context)
          .apply(springSecurity())
          .build();
    }
	
	/**
	 * Inserts test user and returns his data.
	 */
	public UserEntity insertTestUser() {
    	UserEntity testUser = new UserEntity(
			"John Doe",
			passwordEncoder.encode(TEST_USER_PASSWORD), 
			TEST_USER_EMAIL,  
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
	public JSONObject getTestCategoryJsonRequestBody() throws JSONException {
		JSONObject requestBody = new JSONObject();
		
		requestBody.put("name", "Test Account");
		requestBody.put("type", "EXPENSES");
		requestBody.put("currentPeriodSum", "200.00");
		requestBody.put("limit", "SOME-LIMIT");
    	
    	return requestBody;
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
     * Smoke test (tests if the controller loads properly)
     */
    @Test
    public void controllerShouldNotBeNull() {
        assertThat(categoryController).isNotNull();
    }
    
    /**
     * getAllCategories() tests
     * (GET "/categories")
     */
	
	@Test
	public void getAllCategoriesMethodShouldReturnUnauthorizedStatusCode() throws Exception {
	    this.mockMvc.perform( get("/categories") ).andExpect( status().isUnauthorized() );
	}
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void getAllCategoriesMethodShouldReturnOKStatusCodeAndReturnUserCategories() throws Exception {
    	UserEntity testUser = insertTestUser();
    	insertTestCategoryForUser(testUser, CategoryType.EXPENSES, "Category 1");
    	
        MvcResult result = this.mockMvc
        	.perform( get("/categories") )
        	.andExpect( status().isOk() )
        	.andReturn();
        
        assertTrue( categoryRepository.count() == 1 );
        String responseString = result.getResponse().getContentAsString();
        JSONArray responseJsonArray = new JSONArray(responseString);
        assertTrue( responseJsonArray.length() == 1 );
        JSONObject jsonObject = responseJsonArray.getJSONObject(0);
        assertTrue( jsonObject.get("name").equals("Category 1") );
    }
    
    /**
     * getAllCategoriesByType() tests
     * (GET "/categories?type=")
     */
	
	@Test
	public void getAllCategoriesByTypeMethodShouldReturnUnauthorizedStatusCode() throws Exception {
	    this.mockMvc.perform( get("/categories?type=EXPENSES") ).andExpect( status().isUnauthorized() );
	}
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getAllCategoriesByTypeMethodShouldReturnBadRequestStatusCodeBecauseOfInvalidCategoryType() throws Exception {
	    insertTestUser();
		this.mockMvc.perform( get("/categories?type=INVALID_CATEGORY_TYPE") ).andExpect( status().isBadRequest() );
	}
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void getAllCategoriesByTypeMethodShouldReturnOKStatusCodeAndReturnIncomeUserCategories() throws Exception {
    	UserEntity testUser = insertTestUser();
    	insertTestCategoryForUser(testUser, CategoryType.INCOME, "Income Category");
    	insertTestCategoryForUser(testUser, CategoryType.EXPENSES, "Expenses Category");
    				
    	// Test status code
        MvcResult result = this.mockMvc
        	.perform( get("/categories?type=INCOME") )
        	.andExpect( status().isOk() )
        	.andReturn();
        
        // Test returned object(s)
        String responseString = result.getResponse().getContentAsString();
        JSONArray responseJsonArray = new JSONArray(responseString);
        assertTrue( responseJsonArray.length() == 1 );
        JSONObject jsonObject = responseJsonArray.getJSONObject(0);
        assertTrue( jsonObject.get("name").equals("Income Category") );
    }
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void getAllCategoriesByTypeMethodShouldReturnOKStatusCodeAndReturnExpenseUserCategories() throws Exception {
    	UserEntity testUser = insertTestUser();
    	insertTestCategoryForUser(testUser, CategoryType.INCOME, "Income Category");
    	insertTestCategoryForUser(testUser, CategoryType.EXPENSES, "Expenses Category");
    				
    	// Test status code
        MvcResult result = this.mockMvc
        	.perform( get("/categories?type=EXPENSES") )
        	.andExpect( status().isOk() )
        	.andReturn();
        
        // Test returned object(s)
        String responseString = result.getResponse().getContentAsString();
        JSONArray responseJsonArray = new JSONArray(responseString);
        assertTrue( responseJsonArray.length() == 1 );
        JSONObject jsonObject = responseJsonArray.getJSONObject(0);
        assertTrue( jsonObject.get("name").equals("Expenses Category") );
    }
    
    /**
     * getTotalCurrentPeriodSumOfCategoriesByType() tests
     * (GET "/categories/total-current-period-sum?type=")
     */
    
    @Test
	public void getTotalCurrentPeriodSumOfCategoriesByTypeMethodShouldReturnUnauthorizedStatusCode() throws Exception {
	    this.mockMvc.perform( get("/categories/total-current-period-sum?type=EXPENSES") ).andExpect( status().isUnauthorized() );
	}
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void getTotalCurrentPeriodSumOfCategoriesByTypeMethodShouldReturnOKStatusCodeAndReturnIncomeCategoriesSum() throws Exception {
    	UserEntity testUser = insertTestUser();
    	
    	CategoryEntity firstTestIncomeCategory = insertTestCategoryForUser(testUser, CategoryType.INCOME, "Income Category 1");
    	CategoryEntity secondTestIncomeCategory = insertTestCategoryForUser(testUser, CategoryType.INCOME, "Income Category 2");
    	insertTestCategoryForUser(testUser, CategoryType.EXPENSES, "Expenses Category");
    	
    	// Test status code
        MvcResult result = this.mockMvc
        	.perform( get("/categories/total-current-period-sum?type=INCOME") )
        	.andExpect( status().isOk() )
        	.andReturn();
        
        // Test returned value
        String responseString = result.getResponse().getContentAsString();
        String expectedTotalSum = firstTestIncomeCategory.getCurrentPeriodSum() + secondTestIncomeCategory.getCurrentPeriodSum() + "";
        assertEquals(expectedTotalSum, responseString);
    }
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void getTotalCurrentPeriodSumOfCategoriesByTypeMethodShouldReturnOKStatusCodeAndReturnExpenseCategoriesSum() throws Exception {
    	UserEntity testUser = insertTestUser();
    	
    	CategoryEntity firstTestExpensesCategory = insertTestCategoryForUser(testUser, CategoryType.EXPENSES, "Expenses Category 1");
    	CategoryEntity secondTestExpensesCategory = insertTestCategoryForUser(testUser, CategoryType.EXPENSES, "Expenses Category 2");
    	insertTestCategoryForUser(testUser, CategoryType.INCOME, "Income Category");
    	
    	// Test status code
        MvcResult result = this.mockMvc
        	.perform( get("/categories/total-current-period-sum?type=EXPENSES") )
        	.andExpect( status().isOk() )
        	.andReturn();
        
        // Test returned value
        String responseString = result.getResponse().getContentAsString();
        String expectedTotalSum = firstTestExpensesCategory.getCurrentPeriodSum() + secondTestExpensesCategory.getCurrentPeriodSum() + "";
        assertEquals(expectedTotalSum, responseString);
    }
    
    /**
     * getCategoryById() tests
     * (GET "/categories/{id}")
     */
   
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void getCategoryByIdMethodShouldReturnNotFoundStatusCode() throws Exception {
    	insertTestUser();
        this.mockMvc.perform( get("/categories/1") ).andExpect( status().isNotFound() );
    }
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void getCategoryByIdMethodShouldReturnForbiddenStatusCode() throws Exception {
    	insertTestUser();
    	
    	UserEntity anotherTestUser = userRepository.save(
    			new UserEntity(
    			"Jane Doe",
    			passwordEncoder.encode(TEST_USER_PASSWORD), 
    			"another_email@pfm.com",  
    			GenderType.FEMALE,
    			FamilyStatusType.SINGLE,
    			24,
    			"Master's Degree"
    		)
    	);
    	CategoryEntity testCategory = insertTestCategoryForUser(anotherTestUser, CategoryType.EXPENSES, "My Category");
    	
        this.mockMvc.perform( get("/categories/" + testCategory.getId()) ).andExpect( status().isForbidden() );
    }
    
    @Test
    public void getCategoryByIdMethodShouldReturnUnauthorizedStatusCode() throws Exception {
        this.mockMvc.perform( get("/categories/1") ).andExpect( status().isUnauthorized() );
    }
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void getCategoryByIdMethodShouldReturnOKStatusCodeAndCorrectCategory() throws Exception {
    	UserEntity testUser = insertTestUser();
    	CategoryEntity testCategory = insertTestCategoryForUser(testUser, CategoryType.EXPENSES, "Category 1");
    	
    	// Test response code
    	Long testCategoryId = testCategory.getId();
    	MvcResult result = this.mockMvc
    		.perform( get("/categories/" + testCategoryId) )
    		.andExpect( status().isOk() )
    		.andReturn();
		
    	// Test returned object(s)
        String responseString = result.getResponse().getContentAsString();
        JSONObject jsonObject = new JSONObject(responseString);
        assertTrue( jsonObject.get("name").equals( testCategory.getName() ) );
    }
    
    /**
     * getTotalAddedSumBetweenDatesById() tests
     * (GET "/categories/{id}/added-sum" 
     * with request params startDate, endDate)
     */
    
    @Test
	public void getTotalAddedSumBetweenDatesByIdMethodShouldReturnUnauthorizedStatusCode() throws Exception {
	    this.mockMvc.perform( get("/categories/1/added-sum") ).andExpect( status().isUnauthorized() );
	}
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
	public void getTotalAddedSumBetweenDatesByIdMethodShouldReturnBadRequestStatusCodeBecauseOfMissingParams() throws Exception {
    	insertTestUser();
	    this.mockMvc.perform( get("/categories/1/added-sum") ).andExpect( status().isBadRequest() );
	}
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
	public void getTotalAddedSumBetweenDatesByIdMethodShouldReturnNotFoundStatusCodeBecauseOfMissingCategory() throws Exception {
    	insertTestUser();
	    this.mockMvc.perform( get("/categories/1/added-sum?startDate=24.12.2019&endDate=17.06.2020") ).andExpect( status().isNotFound() );
	}
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
	public void getTotalAddedSumBetweenDatesByIdMethodShouldReturnForbiddenStatusCode() throws Exception {
    	insertTestUser();
    	
    	UserEntity anotherTestUser = userRepository.save(
    			new UserEntity(
    			"Jane Doe",
    			passwordEncoder.encode(TEST_USER_PASSWORD), 
    			"another_email@pfm.com",  
    			GenderType.FEMALE,
    			FamilyStatusType.SINGLE,
    			24,
    			"Master's Degree"
    		)
    	);
    	CategoryEntity testCategory = insertTestCategoryForUser(anotherTestUser, CategoryType.EXPENSES, "My Category");
    	
	    this.mockMvc.perform( get("/categories/" + testCategory.getId() + "/added-sum?startDate=24.12.2019&endDate=17.06.2020") ).andExpect( status().isForbidden() );
	}
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
	public void getTotalAddedSumBetweenDatesByIdMethodShouldReturnOkStatusCodeAndAddedSumBetweenDatesForIncomeCategory() throws Exception {
    	UserEntity testUser = insertTestUser();
    	
    	CategoryEntity incomeCategory = insertTestCategoryForUser(testUser, CategoryType.INCOME, "Income Category");
    	AccountEntity testAccount = insertTestAccountForUser(testUser, AccountType.ACTIVATED, "Account 1");
    	
    	SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
    	insertTestTransactionForUser(testUser, "INCOME", dateFormatter.parse("24.12.2019"), "My first income transaction", incomeCategory.getId(), testAccount.getId());
    	insertTestTransactionForUser(testUser, "INCOME", dateFormatter.parse("19.06.2020"), "My second income transaction", incomeCategory.getId(), testAccount.getId());
    	insertTestTransactionForUser(testUser, "INCOME", dateFormatter.parse("17.06.2020"), "My third income transaction", incomeCategory.getId(), testAccount.getId());
    	
	    MvcResult result = this.mockMvc
	    	.perform( 
	    			get("/categories/" + incomeCategory.getId() + "/added-sum?startDate=24.12.2019&endDate=17.06.2020") )
	    	.andExpect( status().isOk() )
	    	.andReturn();
	    
	    String responseString = result.getResponse().getContentAsString();
	    assertTrue( Double.valueOf(responseString) == 200.00d );
	}
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
	public void getTotalAddedSumBetweenDatesByIdMethodShouldReturnOkStatusCodeAndAddedSumBetweenDatesForExpenseCategory() throws Exception {
    	UserEntity testUser = insertTestUser();
    	
    	CategoryEntity expenseCategory = insertTestCategoryForUser(testUser, CategoryType.EXPENSES, "Expense Category");
    	AccountEntity testAccount = insertTestAccountForUser(testUser, AccountType.ACTIVATED, "Account 1");
    	
    	SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
    	insertTestTransactionForUser(testUser, "EXPENSE", dateFormatter.parse("24.12.2019"), "My first expense transaction", testAccount.getId(), expenseCategory.getId());
    	insertTestTransactionForUser(testUser, "EXPENSE", dateFormatter.parse("19.06.2020"), "My second expense transaction", testAccount.getId(), expenseCategory.getId());
    	insertTestTransactionForUser(testUser, "EXPENSE", dateFormatter.parse("17.06.2020"), "My third expense transaction", testAccount.getId(), expenseCategory.getId());
    	
	    MvcResult result = this.mockMvc
	    	.perform( 
	    			get("/categories/" + expenseCategory.getId() + "/added-sum?startDate=24.12.2019&endDate=17.06.2020") )
	    	.andExpect( status().isOk() )
	    	.andReturn();
	    
	    String responseString = result.getResponse().getContentAsString();
	    assertTrue( Double.valueOf(responseString) == 200.00d );
	}
    
    /**
     * createCategory() tests
     * (POST "/categories")
     */
    
    @Test
	public void createCategoryMethodShouldReturnUnauthorizedStatusCodeAndNoCategoryShouldBeInserted() throws Exception {
    	JSONObject requestBody = getTestCategoryJsonRequestBody();
    	
	    this.mockMvc
	    	.perform( 
	    			post("/categories").contentType(MediaType.APPLICATION_JSON).content( requestBody.toString() )
	    	)
	    	.andExpect( status().isUnauthorized() );
	    
	    assertTrue( categoryRepository.count() == 0 );
	}
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void createCategoryMethodShouldReturnCreatedStatusCode() throws Exception {
    	insertTestUser();
    	
    	JSONObject requestBody = getTestCategoryJsonRequestBody();
 
    	// Test if the response code is 201 CREATED
    	MvcResult result = this.mockMvc
    		.perform( 
    			post("/categories").contentType(MediaType.APPLICATION_JSON).content( requestBody.toString() )
    		)
    		.andExpect( status().isCreated() )
    		.andReturn();
    	
    	// Test if the response object contains the created category's ID
    	assertTrue( categoryRepository.count() == 1 );
    	Long createdCategoryId = categoryRepository.findAll().get(0).getId();
    	String jsonResponseString = result.getResponse().getContentAsString();
    	assertThat(jsonResponseString).contains("\"id\":" + createdCategoryId);
    }
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void createCategoryMethodShouldReturnBadRequestStatusCodeBecauseOfEmptyCategoryName() throws Exception {
    	insertTestUser();
    	
    	JSONObject requestBody = getTestCategoryJsonRequestBody();
    	requestBody.put("name", "");
    	
    	MvcResult result = this.mockMvc
    		.perform( 
    			post("/categories").contentType(MediaType.APPLICATION_JSON).content( requestBody.toString() )
    		)
    		.andExpect( status().isBadRequest() )
    		.andReturn();
    	
    	// Test if the exception message contains the correct information
    	String exceptionMessage = result.getResolvedException().getMessage();
    	assertThat(exceptionMessage).contains("Category name must not be null or blank!");    	
    }
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void createCategoryMethodShouldReturnBadRequestStatusCodeBecauseOfExistingCategoryName() throws Exception {
    	UserEntity testUser = insertTestUser();
    	
    	JSONObject requestBody = getTestCategoryJsonRequestBody();
    	
    	insertTestCategoryForUser(testUser, CategoryType.EXPENSES, requestBody.get("name").toString());
    	
    	// Test if the response code is 400 BAD REQUEST
    	MvcResult result = this.mockMvc
    		.perform( 
    			post("/categories").contentType(MediaType.APPLICATION_JSON).content( requestBody.toString() )
    		)
    		.andExpect( status().isBadRequest() )
    		.andReturn();
    	
    	// Test if the exception message contains the correct information
    	String exceptionMessage = result.getResolvedException().getMessage();
    	assertThat(exceptionMessage).contains("Category with this name already exists for the current user!");    
    }
    
    /**
     * createExampleCategoriesForCurrentlyLoggedInUser() tests
     * (POST "/categories/create-example-categories")
     */
    
    @Test
	public void createExampleCategoriesMethodShouldReturnUnauthorizedStatusCodeAndNoCategoriesShouldBeInserted() throws Exception {
    	this.mockMvc
    	.perform( post("/categories/create-example-categories") )
    	.andExpect( status().isUnauthorized() );
    
    	assertTrue( categoryRepository.count() == 0 );
	}
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void createExampleCategoriesMethodShouldReturnCreatedStatusCode() throws Exception {
    	insertTestUser();
    	
    	assertTrue( categoryRepository.count() == 0 );
    	
    	MvcResult result = this.mockMvc
    		.perform( post("/categories/create-example-categories") )
    		.andExpect( status().isCreated() )
    		.andReturn();
    	
    	assertTrue( categoryRepository.count() == categoryService.getNumberOfExampleCategories() );
    	
    	String jsonResponseString = result.getResponse().getContentAsString();
    	JSONArray jsonResponseArray = new JSONArray(jsonResponseString);
    	assertTrue( jsonResponseArray.length() == categoryService.getNumberOfExampleCategories() );
    }
    
    /**
     * updateCategoryById() tests
     * (PUT "/categories/{id})
     */
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void updateCategoryByIdMethodShouldReturnNotFoundStatusCode() throws Exception {
    	insertTestUser();
    	
    	JSONObject requestBody = getTestCategoryJsonRequestBody();
    	
        this.mockMvc.perform(
        	put("/categories/1").contentType(MediaType.APPLICATION_JSON).content( requestBody.toString()) 
        ).andExpect( status().isNotFound() );
    }
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void updateCategoryByIdMethodShouldReturnOkStatusCode() throws Exception {
    	UserEntity testUser = insertTestUser();
    	CategoryEntity testCategory = insertTestCategoryForUser(testUser, CategoryType.EXPENSES, "Category 1");
    	
    	JSONObject requestBody = getTestCategoryJsonRequestBody();
    	requestBody.put("name", "My Updated Category");
    	
    	Long testCategoryId = testCategory.getId();
        MvcResult result = this.mockMvc.perform( 
        	put("/categories/" + testCategoryId).contentType(MediaType.APPLICATION_JSON).content( requestBody.toString()) 
        ).andExpect( status().isOk() ).andReturn();
        
        String jsonResponseString = result.getResponse().getContentAsString();
        JSONObject jsonObject = new JSONObject(jsonResponseString);
        assertTrue( jsonObject.get("name").equals("My Updated Category") );
        		
		assertTrue( categoryRepository.count() == 1 );
        assertTrue( categoryRepository.findById(testCategoryId).get().getName().equals("My Updated Category") );
    }
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void updateCategoryByIdMethodShouldReturnBadRequestStatusCodeBecauseOfNoBody() throws Exception {
    	UserEntity testUser = insertTestUser();
    	CategoryEntity testCategory = insertTestCategoryForUser(testUser, CategoryType.EXPENSES, "Category 1");
    	Long testCategoryId = testCategory.getId();
    	
        this.mockMvc.perform( put("/categories/" + testCategoryId) ).andExpect( status().isBadRequest() );
    }
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void updateCategoryByIdMethodShouldReturnBadRequestStatusCodeBecauseOfNullName() throws Exception {
    	UserEntity testUser = insertTestUser();
    	CategoryEntity testCategory = insertTestCategoryForUser(testUser, CategoryType.EXPENSES, "Category 1");
    	
    	JSONObject requestBody = getTestCategoryJsonRequestBody();
    	requestBody.put("name", null);
    	
    	// Test response code
    	Long testCategoryId = testCategory.getId();
        MvcResult result = this.mockMvc.perform( 
        	put("/categories/" + testCategoryId).contentType(MediaType.APPLICATION_JSON).content( requestBody.toString() ) 
        ).andExpect( status().isBadRequest() ).andReturn();
        
        // Test if the exception message contains the correct information
    	String exceptionMessage = result.getResolvedException().getMessage();
    	assertThat(exceptionMessage).contains("Category name must not be null or blank!");
    }

    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void updateCategoryByIdMethodShouldReturnForbiddenStatusCode() throws Exception {
    	insertTestUser();
    	
    	UserEntity anotherTestUser = userRepository.save(
			new UserEntity(
				"Jane Doe",
				passwordEncoder.encode(TEST_USER_PASSWORD), 
				"another_email@pfm.com",  
				GenderType.FEMALE,
				FamilyStatusType.SINGLE,
				24,
				"Master's Degree"
			)
    	);
    	CategoryEntity testCategory = insertTestCategoryForUser(anotherTestUser, CategoryType.EXPENSES, "Category 1");
    	
    	JSONObject requestBody = getTestCategoryJsonRequestBody();
    	requestBody.put("name", "Some New Category Name");
    	
    	Long testCategoryId = testCategory.getId();
        this.mockMvc.perform( 
        	put("/categories/" + testCategoryId)
        		.contentType(MediaType.APPLICATION_JSON).content( requestBody.toString() ) 
        )
        .andExpect( status().isForbidden() );
        
        assertTrue( categoryRepository.count() == 1 );
        assertFalse( categoryRepository.findById(testCategoryId).get().getName().equals("Some New Category Name") );
    }
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void updateCategoryByIdMethodShouldReturnBadRequestStatusCodeBecauseOfExistingCategoryName() throws Exception {
    	UserEntity testUser = insertTestUser();
    	
    	CategoryEntity firstTestCategory = insertTestCategoryForUser(testUser, CategoryType.EXPENSES, "My Account 1");
    	CategoryEntity secondTestCategory = insertTestCategoryForUser(testUser, CategoryType.EXPENSES, "My Account 2");
    	String seceondTestCategoryName = secondTestCategory.getName();
    	Long firstTestCategoryId = firstTestCategory.getId();
    	
    	JSONObject requestBody = getTestCategoryJsonRequestBody();
    	requestBody.put("name", seceondTestCategoryName);
    	MvcResult result = this.mockMvc
    		.perform( 
    			put("/categories/" + firstTestCategoryId).contentType(MediaType.APPLICATION_JSON).content( requestBody.toString() )
    		)
    		.andExpect( status().isBadRequest() )
    		.andReturn();
    	
    	// Test if the exception message contains the correct information
    	String exceptionMessage = result.getResolvedException().getMessage();
    	assertThat(exceptionMessage).contains("Category with this name already exists for the current user!");    
    }
    
    /**
     * setCategoryLimitById() tests
     * (PATCH "/categories/{id}/limit")
     */
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void setCategoryLimitByIdMethodShouldReturnNotFoundStatusCode() throws Exception {
    	insertTestUser();
    	
    	JSONObject requestBody = new JSONObject();
    	requestBody.put("limit", "SOME LIMIT VALUE");
    	
        this.mockMvc.perform( 
        	patch("/categories/1/limit")
        		.contentType(MediaType.APPLICATION_JSON).content( requestBody.toString() )
        ).andExpect( status().isNotFound() );
    }
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void setCategoryLimitByIdMethodShouldReturnOkStatusCode() throws Exception {
    	UserEntity testUser = insertTestUser();
    	CategoryEntity testCategory = insertTestCategoryForUser(testUser, CategoryType.EXPENSES, "Category 1");
    	
    	JSONObject requestBody = new JSONObject();
    	requestBody.put("limit", "SOME LIMIT VALUE");
    	
    	Long testCategoryId = testCategory.getId();
        this.mockMvc.perform( 
        	patch("/categories/" + testCategoryId + "/limit")
        		.contentType(MediaType.APPLICATION_JSON).content( requestBody.toString() )
        ).andExpect( status().isOk() );
        		
		assertTrue( categoryRepository.count() == 1 );
        assertTrue( categoryRepository.findById(testCategoryId).get().getLimit().equals("SOME LIMIT VALUE") );
    }

    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void setCategoryLimitByIdMethodShouldReturnForbiddenStatusCode() throws Exception {
    	insertTestUser();
    	
    	UserEntity anotherTestUser = userRepository.save(
			new UserEntity(
				"Jane Doe",
				passwordEncoder.encode(TEST_USER_PASSWORD), 
				"another_email@pfm.com",  
				GenderType.FEMALE,
				FamilyStatusType.SINGLE,
				24,
				"Master's Degree"
			)
    	);
    	CategoryEntity testCategory = insertTestCategoryForUser(anotherTestUser, CategoryType.EXPENSES, "Category 1");
    	
    	JSONObject requestBody = new JSONObject();
    	requestBody.put("limit", "SOME LIMIT VALUE");
    	Long testCategoryId = testCategory.getId();
        this.mockMvc.perform( 
        	patch("/categories/" + testCategoryId + "/limit")
        		.contentType(MediaType.APPLICATION_JSON).content( requestBody.toString() )
        ).andExpect( status().isForbidden() );
        
        assertTrue( categoryRepository.count() == 1 );
        assertFalse( categoryRepository.findById(testCategoryId).get().getLimit().equals("SOME LIMIT VALUE") );
    }
    
    /**
     * deleteCategoryById() tests
     * (DELETE "/categories/{id}")
     */
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void deleteCategoryByIdMethodShouldReturnOKStatusCode() throws Exception {
    	UserEntity testUser = insertTestUser();
    	CategoryEntity testCategory = insertTestCategoryForUser(testUser, CategoryType.EXPENSES, "Category 1");
    	Long testCategoryId = testCategory.getId();
    	
    	assertTrue( categoryRepository.existsById(testCategoryId) );
    	
        MvcResult result = this.mockMvc
        	.perform( delete("/categories/" + testCategoryId) )
        	.andExpect( status().isOk() )
        	.andReturn();
        
 		String jsonResponseString = result.getResponse().getContentAsString();
 		assertThat(jsonResponseString).contains("Category 1");
    	assertFalse( categoryRepository.existsById(testCategoryId) );
    }
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void deleteCategoryByIdMethodShouldReturnNotFoundStatusCode() throws Exception {
    	insertTestUser();
        this.mockMvc.perform( delete("/categories/1") ).andExpect( status().isNotFound() );
    }
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void deleteCategoryByIdMethodShouldReturnForbiddenStatusCode() throws Exception {
    	insertTestUser();

    	UserEntity anotherTestUser = userRepository.save(
			new UserEntity(
				"Jane Doe",
				passwordEncoder.encode(TEST_USER_PASSWORD), 
				"another_email@pfm.com",  
				GenderType.FEMALE,
				FamilyStatusType.SINGLE,
				24,
				"Master's Degree"
			)
    	);
    	CategoryEntity testCategory = insertTestCategoryForUser(anotherTestUser, CategoryType.EXPENSES, "Category 1");
    	
    	Long testCategoryId = testCategory.getId();
        MvcResult result = this.mockMvc.perform( 
        	delete("/categories/" + testCategoryId)
        )
        .andExpect( status().isForbidden() ).andReturn();
        
        assertTrue( result.getResolvedException() instanceof UserDoesNotOwnResourceException );
        assertTrue( categoryRepository.count() == 1 );
        assertTrue( categoryRepository.existsById(testCategoryId) );
    }
    
}