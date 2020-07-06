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
import com.mse.personal.finance.rest.exception.AccountDeleteException;
import com.mse.personal.finance.rest.exception.InvalidDataException;
import com.mse.personal.finance.rest.exception.UserDoesNotOwnResourceException;
import com.mse.personal.finance.service.AccountService;

/**
 * This class implements integration tests for the AccountController class.
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
public class AccountControllerTests {
	
	private static final String TEST_USER_EMAIL = "test@pfm.com";
	private static final String TEST_USER_PASSWORD = "123456";
	
	@Autowired
    private AccountController accountController;
	
	@Autowired
	private AccountService accountService;
	
	@Autowired
	private CategoryRepository categoryRepository;
	
	@Autowired
	private TransactionRepository transactionRepository;
	
	@Autowired
	private WebApplicationContext context;
	
	private MockMvc mockMvc;
	
	@Autowired
    private UserRepository userRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private AccountRepository accountRepository;
	
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
	 * Create and return a test account request
	 */
	public JSONObject getTestAccountJsonRequestBody() throws JSONException {
		JSONObject requestBody = new JSONObject();
		
		requestBody.put("name", "Test Account");
		requestBody.put("balance", "1337.37");
		requestBody.put("goal", "2000.00");
		requestBody.put("type", "ACTIVATED");
    	
    	return requestBody;
	}
	
	/**
     * Smoke test (tests if the controller loads properly)
     */
    @Test
    public void controllerShouldNotBeNull() {
        assertThat(accountController).isNotNull();
    }
    
    /**
     * getAllNonDeletedAccounts() tests
     * (GET "/accounts")
     */
	
	@Test
	public void getAllNonDeletedAccountsMethodShouldReturnUnauthorizedStatusCode() throws Exception {
	    this.mockMvc.perform( get("/accounts") ).andExpect( status().isUnauthorized() );
	}
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void getAllNonDeletedAccountsMethodShouldReturnOKStatusCodeAndReturnNonDeletedUserAccounts() throws Exception {
    	UserEntity testUser = insertTestUser();
    	insertTestAccountForUser(testUser, AccountType.ACTIVATED, "Account 1");
    	insertTestAccountForUser(testUser, AccountType.DELETED, "Account 2");
    	
        MvcResult result = this.mockMvc
        	.perform( get("/accounts") )
        	.andExpect( status().isOk() )
        	.andReturn();
        
        assertTrue( accountRepository.count() == 2 );
        String responseString = result.getResponse().getContentAsString();
        JSONArray responseJsonArray = new JSONArray(responseString);
        assertTrue( responseJsonArray.length() == 1 );
        JSONObject jsonObject = responseJsonArray.getJSONObject(0);
        assertTrue( jsonObject.get("name").equals("Account 1") );
    }
    
    /**
     * getAllAccountsByType() tests
     * (GET "/accounts?type=")
     */
	
	@Test
	public void getAllAccountsByTypeMethodShouldReturnUnauthorizedStatusCode() throws Exception {
	    this.mockMvc.perform( get("/accounts?type=ACTIVATED") ).andExpect( status().isUnauthorized() );
	}
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getAllAccountsByTypeMethodShouldReturnBadRequestStatusCodeBecauseOfInvalidAccountType() throws Exception {
	    insertTestUser();
		this.mockMvc.perform( get("/accounts?type=INVALID_ACCOUNT_TYPE") ).andExpect( status().isBadRequest() );
	}
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void getAllAccountsByTypeMethodShouldReturnOKStatusCodeAndReturnActivatedUserAccounts() throws Exception {
    	UserEntity testUser = insertTestUser();
    	insertTestAccountForUser(testUser, AccountType.ACTIVATED, "Activated Account");
    	insertTestAccountForUser(testUser, AccountType.DEACTIVATED, "Deactivated Account");
    				
    	// Test status code
        MvcResult result = this.mockMvc
        	.perform( get("/accounts?type=ACTIVATED") )
        	.andExpect( status().isOk() )
        	.andReturn();
        
        // Test returned object(s)
        String responseString = result.getResponse().getContentAsString();
        JSONArray responseJsonArray = new JSONArray(responseString);
        assertTrue( responseJsonArray.length() == 1 );
        JSONObject jsonObject = responseJsonArray.getJSONObject(0);
        assertTrue( jsonObject.get("name").equals("Activated Account") );
    }
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void getAllAccountsByTypeMethodShouldReturnOKStatusCodeAndReturnDeactivatedUserAccounts() throws Exception {
    	UserEntity testUser = insertTestUser();
    	insertTestAccountForUser(testUser, AccountType.ACTIVATED, "Activated Account");
    	insertTestAccountForUser(testUser, AccountType.DEACTIVATED, "Deactivated Account");
    				
    	// Test status code
        MvcResult result = this.mockMvc
        	.perform( get("/accounts?type=DEACTIVATED") )
        	.andExpect( status().isOk() )
        	.andReturn();
        
        // Test returned object(s)
        String responseString = result.getResponse().getContentAsString();
        JSONArray responseJsonArray = new JSONArray(responseString);
        assertTrue( responseJsonArray.length() == 1 );
        JSONObject jsonObject = responseJsonArray.getJSONObject(0);
        assertTrue( jsonObject.get("name").equals("Deactivated Account") );
    }
    
    /**
     * getTotalBalanceOfActivatedAccounts() tests
     * (GET "/accounts/balance-sum")
     */
    
    @Test
	public void getTotalBalanceOfActivatedAccountsMethodShouldReturnUnauthorizedStatusCode() throws Exception {
	    this.mockMvc.perform( get("/accounts/balance-sum") ).andExpect( status().isUnauthorized() );
	}
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void getTotalBalanceOfActivatedAccountsMethodShouldReturnOKStatusCodeAndReturnBalance() throws Exception {
    	UserEntity testUser = insertTestUser();
    	AccountEntity firstActivatedAccount = insertTestAccountForUser(testUser, AccountType.ACTIVATED, "Account 1");
    	insertTestAccountForUser(testUser, AccountType.DEACTIVATED, "Account 2");
    	AccountEntity secondActivatedAccount = insertTestAccountForUser(testUser, AccountType.ACTIVATED, "Account 3");
    	
    	// Test status code
        MvcResult result = this.mockMvc
        	.perform( get("/accounts/balance-sum") )
        	.andExpect( status().isOk() )
        	.andReturn();
        
        // Test returned value
        String responseString = result.getResponse().getContentAsString();
        String expectedTotalBalance = firstActivatedAccount.getBalance() + secondActivatedAccount.getBalance() + "";
        assertEquals(expectedTotalBalance, responseString);
    }
    
    /**
     * getAccountById() tests
     * (GET "/accounts/{id}")
     */
   
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void getAccountByIdMethodShouldReturnNotFoundStatusCode() throws Exception {
    	insertTestUser();
        this.mockMvc.perform( get("/accounts/1") ).andExpect( status().isNotFound() );
    }
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void getAccountByIdMethodShouldReturnForbiddenStatusCode() throws Exception {
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
    	AccountEntity testAccountEntity = insertTestAccountForUser(anotherTestUser, AccountType.ACTIVATED, "Account 1");
    	
        this.mockMvc.perform( get("/accounts/" + testAccountEntity.getId()) ).andExpect( status().isForbidden() );
    }
    
    @Test
    public void getAccountByIdMethodShouldReturnUnauthorizedStatusCode() throws Exception {
        this.mockMvc.perform( get("/accounts/1") ).andExpect( status().isUnauthorized() );
    }
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void getAccountByIdMethodShouldReturnOKStatusCodeAndCorrectAccount() throws Exception {
    	UserEntity testUser = insertTestUser();
    	AccountEntity testAccount = insertTestAccountForUser(testUser, AccountType.ACTIVATED, "Account 1");
    	
    	// Test response code
    	Long testAccountId = testAccount.getId();
    	MvcResult result = this.mockMvc
    		.perform( get("/accounts/" + testAccountId) )
    		.andExpect( status().isOk() )
    		.andReturn();
		
    	// Test returned object(s)
        String responseString = result.getResponse().getContentAsString();
        JSONObject jsonObject = new JSONObject(responseString);
        assertTrue( jsonObject.get("name").equals( testAccount.getName() ) );
    }
    
    /**
     * getTotalIncomeSumBetweenDatesById() tests
     * (GET "/accounts/{id}/income-sum" 
     * with request params startDate, endDate)
     */
    
    @Test
	public void getTotalIncomeSumBetweenDatesByIdMethodShouldReturnUnauthorizedStatusCode() throws Exception {
	    this.mockMvc.perform( get("/accounts/1/income-sum") ).andExpect( status().isUnauthorized() );
	}
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
	public void getTotalIncomeSumBetweenDatesByIdMethodShouldReturnBadRequestStatusCodeBecauseOfMissingParams() throws Exception {
    	insertTestUser();
	    this.mockMvc.perform( get("/accounts/1/income-sum") ).andExpect( status().isBadRequest() );
	}
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
	public void getTotalIncomeSumBetweenDatesByIdMethodShouldReturnNotFoundStatusCodeBecauseOfMissingAccount() throws Exception {
    	insertTestUser();
	    this.mockMvc.perform( get("/accounts/1/income-sum?startDate=24.12.2019&endDate=17.06.2020") ).andExpect( status().isNotFound() );
	}
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
	public void getTotalIncomeSumBetweenDatesByIdMethodShouldReturnForbiddenStatusCode() throws Exception {
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
    	AccountEntity testAccountEntity = insertTestAccountForUser(anotherTestUser, AccountType.ACTIVATED, "Account 1");
    	
	    this.mockMvc.perform( get("/accounts/" + testAccountEntity.getId() + "/income-sum?startDate=24.12.2019&endDate=17.06.2020") ).andExpect( status().isForbidden() );
	}
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
	public void getTotalIncomeSumBetweenDatesByIdMethodShouldReturnOkStatusCode() throws Exception {
    	UserEntity testUser = insertTestUser();
    	
    	AccountEntity testAccount = insertTestAccountForUser(testUser, AccountType.ACTIVATED, "Account 1");
    	AccountEntity anotherTestAccount = insertTestAccountForUser(testUser, AccountType.ACTIVATED, "Account 2");
    	CategoryEntity incomeCategory = insertTestCategoryForUser(testUser, CategoryType.INCOME, "Income Category");
    	
    	SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
    	insertTestTransactionForUser(testUser, "TRANSFER", dateFormatter.parse("24.12.2019"), "My first transfer transaction", anotherTestAccount.getId(), testAccount.getId());
    	insertTestTransactionForUser(testUser, "TRANSFER", dateFormatter.parse("19.06.2020"), "My second transfer transaction", anotherTestAccount.getId(), testAccount.getId());
    	insertTestTransactionForUser(testUser, "INCOME", dateFormatter.parse("17.06.2020"), "My income transaction", incomeCategory.getId(), testAccount.getId());
    	
	    MvcResult result = this.mockMvc
	    	.perform( 
	    			get("/accounts/" + testAccount.getId() + "/income-sum?startDate=24.12.2019&endDate=17.06.2020") )
	    	.andExpect( status().isOk() )
	    	.andReturn();
	    
	    String responseString = result.getResponse().getContentAsString();
	    assertTrue( Double.valueOf(responseString) == 200.00d );
	}
    
    /**
     * getTotalExpenseSumBetweenDatesById() tests
     * (GET "/accounts/{id}/expense-sum" 
     * with request params startDate, endDate)
     */
    
    @Test
	public void getTotalExpenseSumBetweenDatesByIdMethodShouldReturnUnauthorizedStatusCode() throws Exception {
	    this.mockMvc.perform( get("/accounts/1/expense-sum") ).andExpect( status().isUnauthorized() );
	}
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
	public void getTotalExpenseSumBetweenDatesByIdMethodShouldReturnBadRequestStatusCodeBecauseOfMissingParams() throws Exception {
    	insertTestUser();
	    this.mockMvc.perform( get("/accounts/1/expense-sum") ).andExpect( status().isBadRequest() );
	}
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
	public void getTotalExpenseSumBetweenDatesByIdMethodShouldReturnNotFoundStatusCodeBecauseOfMissingAccount() throws Exception {
    	insertTestUser();
	    this.mockMvc.perform( get("/accounts/1/expense-sum-sum?startDate=24.12.2019&endDate=17.06.2020") ).andExpect( status().isNotFound() );
	}
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
	public void getTotalExpenseSumBetweenDatesByIdMethodShouldReturnForbiddenStatusCode() throws Exception {
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
    	AccountEntity testAccountEntity = insertTestAccountForUser(anotherTestUser, AccountType.ACTIVATED, "Account 1");
    	
	    this.mockMvc.perform( get("/accounts/" + testAccountEntity.getId() + "/expense-sum?startDate=24.12.2019&endDate=17.06.2020") ).andExpect( status().isForbidden() );
	}
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
	public void getTotalExpenseSumBetweenDatesByIdMethodShouldReturnOkStatusCode() throws Exception {
    	UserEntity testUser = insertTestUser();
    	
    	AccountEntity testAccount = insertTestAccountForUser(testUser, AccountType.ACTIVATED, "Account 1");
    	AccountEntity anotherTestAccount = insertTestAccountForUser(testUser, AccountType.ACTIVATED, "Account 2");
    	CategoryEntity expenseCategory = insertTestCategoryForUser(testUser, CategoryType.EXPENSES, "Expense Category");
    	
    	SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
    	insertTestTransactionForUser(testUser, "TRANSFER", dateFormatter.parse("24.12.2019"), "My first transfer transaction", testAccount.getId(), anotherTestAccount.getId());
    	insertTestTransactionForUser(testUser, "TRANSFER", dateFormatter.parse("19.06.2020"), "My second transfer transaction", testAccount.getId(), anotherTestAccount.getId());
    	insertTestTransactionForUser(testUser, "EXPENSE", dateFormatter.parse("17.06.2020"), "My expense transaction", testAccount.getId(), expenseCategory.getId());
    	
	    MvcResult result = this.mockMvc
	    	.perform( 
	    			get("/accounts/" + testAccount.getId() + "/expense-sum?startDate=24.12.2019&endDate=17.06.2020") )
	    	.andExpect( status().isOk() )
	    	.andReturn();
	    
	    String responseString = result.getResponse().getContentAsString();
	    assertTrue( Double.valueOf(responseString) == 200.00d );
	}
    
    /**
     * createAccount() tests
     * (POST "/accounts")
     */
    
    @Test
	public void createAccountMethodShouldReturnUnauthorizedStatusCodeAndNoAccountShouldBeInserted() throws Exception {
    	JSONObject requestBody = getTestAccountJsonRequestBody();
    	
	    this.mockMvc
	    	.perform( 
	    			post("/accounts").contentType(MediaType.APPLICATION_JSON).content( requestBody.toString() )
	    	)
	    	.andExpect( status().isUnauthorized() );
	    
	    assertTrue( accountRepository.count() == 0 );
	}
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void createAccountMethodShouldReturnCreatedStatusCode() throws Exception {
    	insertTestUser();
    	
    	JSONObject requestBody = getTestAccountJsonRequestBody();
 
    	// Test if the response code is 201 CREATED
    	MvcResult result = this.mockMvc
    		.perform( 
    			post("/accounts").contentType(MediaType.APPLICATION_JSON).content( requestBody.toString() )
    		)
    		.andExpect( status().isCreated() )
    		.andReturn();
    	
    	// Test if the response object contains the created account's ID
    	assertTrue( accountRepository.count() == 1 );
    	Long createdAccountId = accountRepository.findAll().get(0).getId();
    	String jsonResponseString = result.getResponse().getContentAsString();
    	assertThat(jsonResponseString).contains("\"id\":" + createdAccountId);
    }
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void createAccountMethodShouldReturnBadRequestStatusCodeBecauseOfEmptyAccountName() throws Exception {
    	insertTestUser();
    	
    	JSONObject requestBody = getTestAccountJsonRequestBody();
    	requestBody.put("name", "");
    	
    	// Test if the response code is 400 BAD REQUEST
    	MvcResult result = this.mockMvc
    		.perform( 
    			post("/accounts").contentType(MediaType.APPLICATION_JSON).content( requestBody.toString() )
    		)
    		.andExpect( status().isBadRequest() )
    		.andReturn();
    	
    	// Test if the exception message contains the correct information
    	String exceptionMessage = result.getResolvedException().getMessage();
    	assertThat(exceptionMessage).contains("Account name must not be null or blank!");    	
    }
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void createAccountMethodShouldReturnBadRequestStatusCodeBecauseOfExistingAccountName() throws Exception {
    	UserEntity testUser = insertTestUser();
    	
    	JSONObject requestBody = getTestAccountJsonRequestBody();
    	
    	insertTestAccountForUser(testUser, AccountType.ACTIVATED, requestBody.get("name").toString());
    	
    	// Test if the response code is 400 BAD REQUEST
    	MvcResult result = this.mockMvc
    		.perform( 
    			post("/accounts").contentType(MediaType.APPLICATION_JSON).content( requestBody.toString() )
    		)
    		.andExpect( status().isBadRequest() )
    		.andReturn();
    	
    	// Test if the exception message contains the correct information
    	String exceptionMessage = result.getResolvedException().getMessage();
    	assertThat(exceptionMessage).contains("Account with this name already exists for the current user!");    
    }
    
    /**
     * createExampleAccountsForCurrentlyLoggedInUser() tests
     * (POST "/accounts/create-example-accounts")
     */
    
    @Test
	public void createExampleAccountsMethodShouldReturnUnauthorizedStatusCodeAndNoAccountsShouldBeInserted() throws Exception {
	    this.mockMvc
	    	.perform( post("/accounts/create-example-accounts") )
	    	.andExpect( status().isUnauthorized() );
	    
	    assertTrue( accountRepository.count() == 0 );
	}
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void createExampleAccountsMethodShouldReturnCreatedStatusCode() throws Exception {
    	insertTestUser();
    	
    	assertTrue( accountRepository.count() == 0 );
    	
    	MvcResult result = this.mockMvc
    		.perform( post("/accounts/create-example-accounts") )
    		.andExpect( status().isCreated() )
    		.andReturn();
    	
    	assertTrue( accountRepository.count() == accountService.getNumberOfExampleAccounts() );
    	
    	String jsonResponseString = result.getResponse().getContentAsString();
    	JSONArray jsonResponseArray = new JSONArray(jsonResponseString);
    	assertTrue( jsonResponseArray.length() == accountService.getNumberOfExampleAccounts() );
    }
    
    /**
     * updateAccountById() tests
     * (PUT "/accounts/{id})
     */
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void updateAccountByIdMethodShouldReturnNotFoundStatusCode() throws Exception {
    	insertTestUser();
    	
    	JSONObject requestBody = getTestAccountJsonRequestBody();
    	
        this.mockMvc.perform(
        	put("/accounts/1").contentType(MediaType.APPLICATION_JSON).content( requestBody.toString()) 
        ).andExpect( status().isNotFound() );
    }
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void updateAccountByIdMethodShouldReturnOkStatusCode() throws Exception {
    	UserEntity testUser = insertTestUser();
    	AccountEntity testAccount = insertTestAccountForUser(testUser, AccountType.ACTIVATED, "My Account");
    	
    	JSONObject requestBody = getTestAccountJsonRequestBody();
    	requestBody.put("name", "My Updated Account");
    	
    	Long testAccountId = testAccount.getId();
        MvcResult result = this.mockMvc.perform( 
        	put("/accounts/" + testAccountId).contentType(MediaType.APPLICATION_JSON).content( requestBody.toString()) 
        ).andExpect( status().isOk() ).andReturn();
        
        String jsonResponseString = result.getResponse().getContentAsString();
        JSONObject jsonObject = new JSONObject(jsonResponseString);
        assertTrue( jsonObject.get("name").equals("My Updated Account") );
        		
		assertTrue( accountRepository.count() == 1 );
        assertTrue( accountRepository.findById(testAccountId).get().getName().equals("My Updated Account") );
    }
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void updateAccountByIdMethodShouldReturnBadRequestStatusCodeBecauseOfNoBody() throws Exception {
    	UserEntity testUser = insertTestUser();
    	AccountEntity testAccount = insertTestAccountForUser(testUser, AccountType.ACTIVATED, "My Account");
    	Long testAccountId = testAccount.getId();
    	
        this.mockMvc.perform( put("/accounts/" + testAccountId) ).andExpect( status().isBadRequest() );
    }
        
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void updateAccountByIdMethodShouldReturnBadRequestStatusCodeBecauseOfNullName() throws Exception {
    	UserEntity testUser = insertTestUser();
    	AccountEntity testAccount = insertTestAccountForUser(testUser, AccountType.ACTIVATED, "My Account");
    	
    	JSONObject requestBody = getTestAccountJsonRequestBody();
    	requestBody.put("name", null);
    	
    	// Test response code
    	Long testAccountId = testAccount.getId();
        MvcResult result = this.mockMvc.perform( 
        	put("/accounts/" + testAccountId).contentType(MediaType.APPLICATION_JSON).content( requestBody.toString() ) 
        ).andExpect( status().isBadRequest() ).andReturn();
        
        // Test if the exception message contains the correct information
    	String exceptionMessage = result.getResolvedException().getMessage();
    	assertThat(exceptionMessage).contains("Account name must not be null or blank!");
    }

    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void updateAccountByIdMethodShouldReturnForbiddenStatusCode() throws Exception {
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
    	AccountEntity testAccountEntity = insertTestAccountForUser(anotherTestUser, AccountType.ACTIVATED, "My Account");
    	
    	JSONObject requestBody = getTestAccountJsonRequestBody();
    	requestBody.put("name", "Some New Account Name");
    	
    	Long testAccountId = testAccountEntity.getId();
        this.mockMvc.perform( 
        	put("/accounts/" + testAccountId)
        		.contentType(MediaType.APPLICATION_JSON).content( requestBody.toString() ) 
        )
        .andExpect( status().isForbidden() );
        
        assertTrue( accountRepository.count() == 1 );
        assertFalse( accountRepository.findById(testAccountId).get().getName().equals("Some New Account Name") );
    }
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void updateAccountByIdMethodShouldReturnBadRequestStatusCodeBecauseOfExistingAccountName() throws Exception {
    	UserEntity testUser = insertTestUser();
    	
    	AccountEntity firstTestAccount = insertTestAccountForUser(testUser, AccountType.ACTIVATED, "My Account 1");
    	AccountEntity secondTestAccount = insertTestAccountForUser(testUser, AccountType.ACTIVATED, "My Account 2");
    	String secondTestAccountName = secondTestAccount.getName();
    	Long firstTestAccountId = firstTestAccount.getId();
    	
    	JSONObject requestBody = getTestAccountJsonRequestBody();
    	requestBody.put("name", secondTestAccountName);
    	MvcResult result = this.mockMvc
    		.perform( 
    			put("/accounts/" + firstTestAccountId).contentType(MediaType.APPLICATION_JSON).content( requestBody.toString() )
    		)
    		.andExpect( status().isBadRequest() )
    		.andReturn();
    	
    	// Test if the exception message contains the correct information
    	String exceptionMessage = result.getResolvedException().getMessage();
    	assertThat(exceptionMessage).contains("Account with this name already exists for the current user!");    
    }
    
    /**
     * deactivateAccountById() tests
     * (PATCH "/accounts/{id}/deactivate)
     */
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void deactivateAccountByIdMethodShouldReturnNotFoundStatusCode() throws Exception {
    	insertTestUser();
        this.mockMvc.perform( patch("/accounts/1/deactivate") ).andExpect( status().isNotFound() );
    }
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void deactivateAccountByIdMethodShouldReturnOkStatusCode() throws Exception {
    	UserEntity testUser = insertTestUser();
    	AccountEntity testAccount = insertTestAccountForUser(testUser, AccountType.ACTIVATED, "My Account");
    	
    	Long testAccountId = testAccount.getId();
        MvcResult result = this.mockMvc.perform( 
        	patch("/accounts/" + testAccountId + "/deactivate")
        ).andExpect( status().isOk() ).andReturn();
        
        String jsonResponseString = result.getResponse().getContentAsString();
        JSONObject jsonObject = new JSONObject(jsonResponseString);
        assertTrue( jsonObject.get("type").equals("DEACTIVATED") );
        		
		assertTrue( accountRepository.count() == 1 );
        assertTrue( accountRepository.findById(testAccountId).get().getType() == AccountType.DEACTIVATED );
    }
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void deactivateAccountByIdMethodShouldReturnBadRequestStatusCodeBecauseOfDeletedAccount() throws Exception {
    	UserEntity testUser = insertTestUser();
    	AccountEntity testAccount = insertTestAccountForUser(testUser, AccountType.DELETED, "My Account");
    	Long testAccountId = testAccount.getId();
        this.mockMvc.perform( patch("/accounts/" + testAccountId +"/deactivate") ).andExpect( status().isBadRequest() );
    }
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void deactivateAccountByIdMethodShouldReturnForbiddenStatusCode() throws Exception {
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
    	AccountEntity testAccountEntity = insertTestAccountForUser(anotherTestUser, AccountType.ACTIVATED, "My Account");
    	
    	Long testAccountId = testAccountEntity.getId();
        this.mockMvc.perform( 
        	patch("/accounts/" + testAccountId + "/deactivate")
        )
        .andExpect( status().isForbidden() );
        
        assertTrue( accountRepository.count() == 1 );
        assertFalse( accountRepository.findById(testAccountId).get().getType() == AccountType.DEACTIVATED );
    }
    
    /**
     * activateAccountById() tests
     * (PATCH "/accounts/{id}/activate)
     */
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void activateAccountByIdMethodShouldReturnNotFoundStatusCode() throws Exception {
    	insertTestUser();
        this.mockMvc.perform( patch("/accounts/1/activate") ).andExpect( status().isNotFound() );
    }
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void activateAccountByIdMethodShouldReturnOkStatusCode() throws Exception {
    	UserEntity testUser = insertTestUser();
    	AccountEntity testAccount = insertTestAccountForUser(testUser, AccountType.DEACTIVATED, "My Account");
    	
    	Long testAccountId = testAccount.getId();
        MvcResult result = this.mockMvc.perform( 
        	patch("/accounts/" + testAccountId + "/activate")
        ).andExpect( status().isOk() ).andReturn();
        
        String jsonResponseString = result.getResponse().getContentAsString();
        JSONObject jsonObject = new JSONObject(jsonResponseString);
        assertTrue( jsonObject.get("type").equals("ACTIVATED") );
        		
		assertTrue( accountRepository.count() == 1 );
        assertTrue( accountRepository.findById(testAccountId).get().getType() == AccountType.ACTIVATED );
    }
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void activateAccountByIdMethodShouldReturnBadRequestStatusCodeBecauseOfDeletedAccount() throws Exception {
    	UserEntity testUser = insertTestUser();
    	AccountEntity testAccount = insertTestAccountForUser(testUser, AccountType.DELETED, "My Account");
    	Long testAccountId = testAccount.getId();
        this.mockMvc.perform( patch("/accounts/" + testAccountId +"/activate") ).andExpect( status().isBadRequest() );
    }
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void activateAccountByIdMethodShouldReturnForbiddenStatusCode() throws Exception {
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
    	AccountEntity testAccountEntity = insertTestAccountForUser(anotherTestUser, AccountType.DEACTIVATED, "My Account");
    	
    	Long testAccountId = testAccountEntity.getId();
        this.mockMvc.perform( 
        	patch("/accounts/" + testAccountId + "/activate")
        )
        .andExpect( status().isForbidden() );
        
        assertTrue( accountRepository.count() == 1 );
        assertFalse( accountRepository.findById(testAccountId).get().getType() == AccountType.ACTIVATED );
    }
    
    /**
     * setAccountGoalById() tests
     * (PATCH "/accounts/{id}?goal=")
     */
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void setAccountGoalByIdMethodShouldReturnNotFoundStatusCode() throws Exception {
    	insertTestUser();
        this.mockMvc.perform( patch("/accounts/1?goal=1337.00") ).andExpect( status().isNotFound() );
    }
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void setAccountGoalByIdMethodShouldReturnOkStatusCode() throws Exception {
    	UserEntity testUser = insertTestUser();
    	AccountEntity testAccount = insertTestAccountForUser(testUser, AccountType.ACTIVATED, "My Account");
    	
    	Long testAccountId = testAccount.getId();
        this.mockMvc.perform( 
        	patch("/accounts/" + testAccountId + "?goal=1337.00")
        ).andExpect( status().isOk() );
        		
		assertTrue( accountRepository.count() == 1 );
        assertTrue( accountRepository.findById(testAccountId).get().getGoal() == 1337.00d );
    }

    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void setAccountGoalByIdMethodShouldReturnForbiddenStatusCode() throws Exception {
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
    	AccountEntity testAccountEntity = insertTestAccountForUser(anotherTestUser, AccountType.ACTIVATED, "My Account");
    	
    	Long testAccountId = testAccountEntity.getId();
        this.mockMvc.perform( 
        	patch("/accounts/" + testAccountId + "?goal=1337.00")
        )
        .andExpect( status().isForbidden() );
        
        assertTrue( accountRepository.count() == 1 );
        assertTrue( accountRepository.findById(testAccountId).get().getGoal() != 1337.00d );
    }

    /**
     * setAccountBalanceById() tests
     * (PATCH "/accounts/{id}?balance=")
     */
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void setAccountBalanceByIdMethodShouldReturnNotFoundStatusCode() throws Exception {
    	insertTestUser();
        this.mockMvc.perform( patch("/accounts/1?balance=8888.88") ).andExpect( status().isNotFound() );
    }
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void setAccountBalanceByIdMethodShouldReturnOkStatusCode() throws Exception {
    	UserEntity testUser = insertTestUser();
    	AccountEntity testAccount = insertTestAccountForUser(testUser, AccountType.ACTIVATED, "My Account");
    	insertTestSystemCategoriesForUser(testUser);
    	
    	Long testAccountId = testAccount.getId();
        this.mockMvc.perform( 
        	patch("/accounts/" + testAccountId + "?balance=8888.88")
        ).andExpect( status().isOk() );
        		
		assertTrue( accountRepository.count() == 1 );
        assertTrue( accountRepository.findById(testAccountId).get().getBalance() == 8888.88d );
    }

    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void setAccountBalanceByIdMethodShouldReturnForbiddenStatusCode() throws Exception {
    	UserEntity testUser = insertTestUser();
    	insertTestSystemCategoriesForUser(testUser);
    	
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
    	AccountEntity testAccountEntity = insertTestAccountForUser(anotherTestUser, AccountType.ACTIVATED, "My Account");
    	insertTestSystemCategoriesForUser(anotherTestUser);
    	
    	Long testAccountId = testAccountEntity.getId();
        this.mockMvc.perform( 
        	patch("/accounts/" + testAccountId + "?balance=8888.88")
        )
        .andExpect( status().isForbidden() );
        
        assertTrue( accountRepository.count() == 1 );
        assertTrue( accountRepository.findById(testAccountId).get().getBalance() != 8888.88d );
    }
    
    /**
     * deleteAccountById() tests
     * (DELETE "/accounts/{id}")
     */
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void deleteAccountByIdMethodShouldReturnOKStatusCode() throws Exception {
    	UserEntity testUser = insertTestUser();
    	AccountEntity testAccount = insertTestAccountForUser(testUser, AccountType.ACTIVATED, "My Account");
    	testAccount.setBalance(0.0d);
    	testAccount = accountRepository.save(testAccount);
    	
    	Long testAccountId = testAccount.getId();
        MvcResult result = this.mockMvc
        	.perform( delete("/accounts/" + testAccountId) )
        	.andExpect( status().isOk() )
        	.andReturn();
        
 		String jsonResponseString = result.getResponse().getContentAsString();
 		assertThat(jsonResponseString).contains("My Account");
    	assertTrue( accountRepository.findById(testAccountId).get().getType() == AccountType.DELETED );
    }
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void deleteAccountByIdMethodShouldReturnNotFoundStatusCode() throws Exception {
    	insertTestUser();
        this.mockMvc.perform( delete("/accounts/1") ).andExpect( status().isNotFound() );
    }
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void deleteAccountByIdMethodShouldReturnForbiddenStatusCode() throws Exception {
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
    	AccountEntity testAccountEntity = insertTestAccountForUser(anotherTestUser, AccountType.ACTIVATED, "My Account");
    	testAccountEntity.setBalance(0.0d);
    	testAccountEntity = accountRepository.save(testAccountEntity);
    	
    	Long testAccountId = testAccountEntity.getId();
        MvcResult result = this.mockMvc.perform( 
        	delete("/accounts/" + testAccountId)
        )
        .andExpect( status().isForbidden() ).andReturn();
        
        assertTrue( result.getResolvedException() instanceof UserDoesNotOwnResourceException );
        assertTrue( accountRepository.count() == 1 );
        assertTrue( accountRepository.findById(testAccountId).get().getType() == AccountType.ACTIVATED );
    }
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void deleteAccountByIdMethodShouldReturnBadRequestStatusCodeBecauseOfNonZeroBalance() throws Exception {
    	UserEntity testUser = insertTestUser();
    	AccountEntity testAccountEntity = insertTestAccountForUser(testUser, AccountType.ACTIVATED, "My Account");
    	
    	Long testAccountId = testAccountEntity.getId();
        MvcResult result = this.mockMvc.perform( 
        	delete("/accounts/" + testAccountId)
        )
        .andExpect( status().isBadRequest() ).andReturn();
        
        Exception resolvedException = result.getResolvedException();
        assertTrue( resolvedException instanceof AccountDeleteException );
        assertTrue( resolvedException.getMessage().equals("Account balance must be zero to delete account!") );
        assertTrue( accountRepository.count() == 1 );
        assertTrue( accountRepository.findById(testAccountId).get().getType() == AccountType.ACTIVATED );
    }
    
}