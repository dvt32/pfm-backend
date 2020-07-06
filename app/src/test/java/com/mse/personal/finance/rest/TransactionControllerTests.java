package com.mse.personal.finance.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

/**
 * This class implements integration tests for the TransactionController class.
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
public class TransactionControllerTests {
	
	private static final String TEST_USER_EMAIL = "test@pfm.com";
	private static final String TEST_USER_PASSWORD = "123456";
	
	@Autowired
    private TransactionController transactionController;
	
	@Autowired
	private TransactionRepository transactionRepository;
	
	@Autowired
	private AccountRepository accountRepository;
	
	@Autowired
	private CategoryRepository categoryRepository;
	
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
	public JSONObject getTestTransactionJsonRequestBody(String type, String description, Long fromId, Long toId) throws JSONException {
		JSONObject requestBody = new JSONObject();
		
		requestBody.put("dateOfCompletion", "2020-03-03");
		switch (type) {
		case "INCOME":
			requestBody.put("fromType", "CATEGORY");
			requestBody.put("toType", "ACCOUNT");
			break;
		case "EXPENSE": 
			requestBody.put("fromType", "ACCOUNT");
			requestBody.put("toType", "CATEGORY");
			break;
		case "TRANSFER": 
			requestBody.put("fromType", "ACCOUNT");
			requestBody.put("toType", "ACCOUNT");
			break;
		default: 
			break;
		}
		requestBody.put("fromId", fromId);
		requestBody.put("toId", toId);
    	requestBody.put("sum", 100.00d);
    	requestBody.put("shouldBeAutomaticallyExecuted", "false");
    	requestBody.put("description", description);
		
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
     * Smoke test (tests if the controller loads properly)
     */
	
    @Test
    public void controllerShouldNotBeNull() {
        assertThat(transactionController).isNotNull();
    }
    
    /**
     * getTransactionsByPageable() tests
     * (GET "/transactions")
     */
    
    @Test
	public void getTransactionsByPageableMethod_ShouldReturnUnauthorizedStatusCode() throws Exception {
	    this.mockMvc.perform( get("/transactions") ).andExpect( status().isUnauthorized() );
	}
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void getTransactionsByPageableMethod_ShouldReturnOKStatusCodeAndReturnUserTransactions() throws Exception {
    	UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
    	insertTestTransactionForUser(testUser, "EXPENSE", new Date(), "My expense transaction");
    	insertTestTransactionForUser(testUser, "INCOME", new Date(), "My income transaction");
    	
        MvcResult result = this.mockMvc
        	.perform( get("/transactions") )
        	.andExpect( status().isOk() )
        	.andReturn();
        
        assertTrue( transactionRepository.count() == 2 );
        
        String responseString = result.getResponse().getContentAsString();
        JSONObject responseJsonObject = new JSONObject(responseString);
        String contentString = responseJsonObject.getString("content");
        JSONArray contentJsonArray = new JSONArray(contentString);
       
        assertTrue( contentJsonArray.length() == 2 );
        assertTrue( contentJsonArray.getJSONObject(0).get("description").equals("My expense transaction") );
        assertTrue( contentJsonArray.getJSONObject(1).get("description").equals("My income transaction") );
    }
    
    @Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTransactionsByPageableMethod_ShouldReturnOKStatusCodeAndReturnOneUserTransaction() throws Exception {
		UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
		insertTestTransactionForUser(testUser, "EXPENSE", new Date(), "My expense transaction");
		insertTestTransactionForUser(testUser, "INCOME", new Date(), "My income transaction");
		
	    MvcResult result = this.mockMvc
	    	.perform( get("/transactions?page=0&size=1") )
	    	.andExpect( status().isOk() )
	    	.andReturn();
	    
	    assertTrue( transactionRepository.count() == 2 );
	    
	    String responseString = result.getResponse().getContentAsString();
	    JSONObject responseJsonObject = new JSONObject(responseString);
	    String contentString = responseJsonObject.getString("content");
	    JSONArray contentJsonArray = new JSONArray(contentString);
	
	    assertTrue( contentJsonArray.length() == 1 );
	    assertTrue( contentJsonArray.getJSONObject(0).get("description").equals("My expense transaction") );
	}

	/**
     * getTransactionsByTypeAndPageable() tests
     * (GET "/transactions?type=")
     */
    
    @Test
	public void getTransactionsByTypeAndPageableMethod_ShouldReturnUnauthorizedStatusCode() throws Exception {
	    this.mockMvc.perform( get("/transactions?type=INCOME") ).andExpect( status().isUnauthorized() );
	}
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTransactionsByTypeAndPageableMethod_ShouldReturnBadRequestStatusCodeBecauseOfInvalidType() throws Exception {
	    insertTestUser(TEST_USER_EMAIL);
		this.mockMvc.perform( get("/transactions?type=IVALID_TRANSACTION_TYPE") ).andExpect( status().isBadRequest() );
	}
    
	@Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void getTransactionsByTypeAndPageableMethod_ShouldReturnOKStatusCodeAndReturnIncomeTransactions() throws Exception {
    	UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
    	
    	insertTestTransactionForUser(testUser, "INCOME", new Date(), "My first income transaction");
    	insertTestTransactionForUser(testUser, "INCOME", new Date(), "My second income transaction");
    	insertTestTransactionForUser(testUser, "EXPENSE", new Date(), "My expense transaction");
    	insertTestTransactionForUser(testUser, "TRANSFER", new Date(), "My transfer transaction");
    	
        MvcResult result = this.mockMvc
        	.perform( get("/transactions?type=INCOME") )
        	.andExpect( status().isOk() )
        	.andReturn();
        
        assertTrue( transactionRepository.count() == 4 );
        
        String responseString = result.getResponse().getContentAsString();
        JSONObject responseJsonObject = new JSONObject(responseString);
        String contentString = responseJsonObject.getString("content");
        JSONArray contentJsonArray = new JSONArray(contentString);
       
        assertTrue( contentJsonArray.length() == 2 );
        assertTrue( contentJsonArray.getJSONObject(0).get("description").equals("My first income transaction") );
        assertTrue( contentJsonArray.getJSONObject(1).get("description").equals("My second income transaction") );
    }
	
	@Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void getTransactionsByTypeAndPageableMethod_ShouldReturnOKStatusCodeAndReturnExpenseTransactions() throws Exception {
    	UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
    	
    	insertTestTransactionForUser(testUser, "EXPENSE", new Date(), "My first expense transaction");
    	insertTestTransactionForUser(testUser, "EXPENSE", new Date(), "My second expense transaction");
    	insertTestTransactionForUser(testUser, "INCOME", new Date(), "My income transaction");
    	insertTestTransactionForUser(testUser, "TRANSFER", new Date(), "My transfer transaction");
    	
        MvcResult result = this.mockMvc
        	.perform( get("/transactions?type=EXPENSE") )
        	.andExpect( status().isOk() )
        	.andReturn();
        
        assertTrue( transactionRepository.count() == 4 );
        
        String responseString = result.getResponse().getContentAsString();
        JSONObject responseJsonObject = new JSONObject(responseString);
        String contentString = responseJsonObject.getString("content");
        JSONArray contentJsonArray = new JSONArray(contentString);
       
        assertTrue( contentJsonArray.length() == 2 );
        assertTrue( contentJsonArray.getJSONObject(0).get("description").equals("My first expense transaction") );
        assertTrue( contentJsonArray.getJSONObject(1).get("description").equals("My second expense transaction") );
    }
	
	@Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void getTransactionsByTypeAndPageableMethod_ShouldReturnOKStatusCodeAndReturnTransferTransactions() throws Exception {
    	UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
    	
    	insertTestTransactionForUser(testUser, "TRANSFER", new Date(), "My first transfer transaction");
    	insertTestTransactionForUser(testUser, "TRANSFER", new Date(), "My second transfer transaction");
    	insertTestTransactionForUser(testUser, "INCOME", new Date(), "My income transaction");
    	insertTestTransactionForUser(testUser, "EXPENSE", new Date(), "My expense transaction");
    	
        MvcResult result = this.mockMvc
        	.perform( get("/transactions?type=TRANSFER") )
        	.andExpect( status().isOk() )
        	.andReturn();
        
        assertTrue( transactionRepository.count() == 4 );
        
        String responseString = result.getResponse().getContentAsString();
        JSONObject responseJsonObject = new JSONObject(responseString);
        String contentString = responseJsonObject.getString("content");
        JSONArray contentJsonArray = new JSONArray(contentString);
       
        assertTrue( contentJsonArray.length() == 2 );
        assertTrue( contentJsonArray.getJSONObject(0).get("description").equals("My first transfer transaction") );
        assertTrue( contentJsonArray.getJSONObject(1).get("description").equals("My second transfer transaction") );
    }
	
	@Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void getTransactionsByTypeAndPageableMethod_ShouldReturnOKStatusCodeAndReturnOneUserTransaction() throws Exception {
    	UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
    	insertTestTransactionForUser(testUser, "EXPENSE", new Date(), "My first expense transaction");
    	insertTestTransactionForUser(testUser, "EXPENSE", new Date(), "My second expense transaction");
    	insertTestTransactionForUser(testUser, "INCOME", new Date(), "My income transaction");
    	
        MvcResult result = this.mockMvc
        	.perform( get("/transactions?type=EXPENSE&page=0&size=1") )
        	.andExpect( status().isOk() )
        	.andReturn();
        
        assertTrue( transactionRepository.count() == 3 );
        
        String responseString = result.getResponse().getContentAsString();
        JSONObject responseJsonObject = new JSONObject(responseString);
        String contentString = responseJsonObject.getString("content");
        JSONArray contentJsonArray = new JSONArray(contentString);
       
        assertTrue( contentJsonArray.length() == 1 );
        assertTrue( contentJsonArray.getJSONObject(0).get("description").equals("My first expense transaction") );
    }
    
    /**
     * getTotalTransactionsSumByType() tests
     * (GET "/transactions/total-sum?type=")
     */
    
	@Test
	public void getTotalTransactionsSumByTypeMethod_ShouldReturnUnauthorizedStatusCode() throws Exception {
	    this.mockMvc.perform( get("/transactions/total-sum?type=EXPENSE") ).andExpect( status().isUnauthorized() );
	}
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTotalTransactionsSumByTypeMethod_ShouldReturnBadRequestStatusCodeBecauseOfInvalidType() throws Exception {
	    insertTestUser(TEST_USER_EMAIL);
		this.mockMvc.perform( get("/transactions/total-sum?type=IVALID_TRANSACTION_TYPE") ).andExpect( status().isBadRequest() );
	}
	
	@Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void getTotalTransactionsSumByTypeMethod_ShouldReturnOKStatusCodeAndReturnIncomeTransactionsSum() throws Exception {
    	UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
    	
    	insertTestTransactionForUser(testUser, "INCOME", new Date(), "My first income transaction");
    	insertTestTransactionForUser(testUser, "INCOME", new Date(), "My second income transaction");
    	insertTestTransactionForUser(testUser, "EXPENSE", new Date(), "My expense transaction");
    	insertTestTransactionForUser(testUser, "TRANSFER", new Date(), "My transfer transaction");
    	
        MvcResult result = this.mockMvc
        	.perform( get("/transactions/total-sum?type=INCOME") )
        	.andExpect( status().isOk() )
        	.andReturn();
        
        assertTrue( transactionRepository.count() == 4 );
        
        String responseString = result.getResponse().getContentAsString();
        assertTrue( Double.valueOf(responseString) == 200.00d );
    }
	
	@Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void getTotalTransactionsSumByTypeMethod_ShouldReturnOKStatusCodeAndReturnExpenseTransactionsSum() throws Exception {
    	UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
    	
    	insertTestTransactionForUser(testUser, "INCOME", new Date(), "My first income transaction");
    	insertTestTransactionForUser(testUser, "INCOME", new Date(), "My second income transaction");
    	insertTestTransactionForUser(testUser, "EXPENSE", new Date(), "My expense transaction");
    	insertTestTransactionForUser(testUser, "TRANSFER", new Date(), "My transfer transaction");
    	
        MvcResult result = this.mockMvc
        	.perform( get("/transactions/total-sum?type=EXPENSE") )
        	.andExpect( status().isOk() )
        	.andReturn();
        
        assertTrue( transactionRepository.count() == 4 );
        
        String responseString = result.getResponse().getContentAsString();
        assertTrue( Double.valueOf(responseString) == 100.00d );
    }
	
	@Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void getTotalTransactionsSumByTypeMethod_ShouldReturnOKStatusCodeAndReturnTransferTransactionsSum() throws Exception {
    	UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
    	
    	insertTestTransactionForUser(testUser, "TRANSFER", new Date(), "My first transfer transaction");
    	insertTestTransactionForUser(testUser, "TRANSFER", new Date(), "My second transfer transaction");
    	insertTestTransactionForUser(testUser, "TRANSFER", new Date(), "My third transfer transaction");
    	
        MvcResult result = this.mockMvc
        	.perform( get("/transactions/total-sum?type=TRANSFER") )
        	.andExpect( status().isOk() )
        	.andReturn();
        
        assertTrue( transactionRepository.count() == 3 );
        
        String responseString = result.getResponse().getContentAsString();
        assertTrue( Double.valueOf(responseString) == 300.00d );
    }
    
    /**
     * getTransactionsBetweenDatesByTypeAndPageable() tests
     * (GET "/transactions/between-dates" 
     *  with request params "type", "startDate" & "endDate")
     */
    
	@Test
	public void getTransactionsBetweenDatesByTypeAndPageableMethod_ShouldReturnUnauthorizedStatusCode() throws Exception {
	    this.mockMvc.perform( get("/transactions/between-dates?type=INCOME&startDate=24.12.2019&endDate=17.06.2020") ).andExpect( status().isUnauthorized() );
	}
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTransactionsBetweenDatesByTypeAndPageableMethod_ShouldReturnBadRequestStatusCodeBecauseOfMissingParams() throws Exception {
	    insertTestUser(TEST_USER_EMAIL);
		this.mockMvc.perform( get("/transactions/between-dates?type=INCOME") ).andExpect( status().isBadRequest() );
	}
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTransactionsBetweenDatesByTypeAndPageableMethod_ShouldReturnBadRequestStatusCodeBecauseOfInvalidType() throws Exception {
	    insertTestUser(TEST_USER_EMAIL);
		this.mockMvc.perform( get("/transactions/between-dates?type=INVALID_TRANSACTION_TYPE&startDate=24.12.2019&endDate=17.06.2020") ).andExpect( status().isBadRequest() );
	}
    
	@Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void getTransactionsBetweenDatesByTypeAndPageableMethod_ShouldReturnOKStatusCodeAndReturnIncomeTransactionsBetweenDates() throws Exception {
    	UserEntity testUser = insertTestUser(TEST_USER_EMAIL);

    	SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
    	
    	// Should be returned
    	insertTestTransactionForUser(testUser, "INCOME", dateFormatter.parse("24.12.2019"), "My first income transaction");
    	insertTestTransactionForUser(testUser, "INCOME", dateFormatter.parse("12.05.2020"), "My second income transaction");
    	insertTestTransactionForUser(testUser, "INCOME", dateFormatter.parse("17.06.2020"), "My third income transaction");
    	
    	// Should not be returned (not of proper type or in date range)
    	insertTestTransactionForUser(testUser, "INCOME", dateFormatter.parse("23.12.2019"), "My fourth income transaction");
    	insertTestTransactionForUser(testUser, "INCOME", dateFormatter.parse("19.06.2020"), "My fifth income transaction");
    	insertTestTransactionForUser(testUser, "EXPENSE", new Date(), "My expense transaction");
    	insertTestTransactionForUser(testUser, "TRANSFER", new Date(), "My transfer transaction");
    	
        MvcResult result = this.mockMvc
        	.perform( get("/transactions/between-dates?type=INCOME&startDate=24.12.2019&endDate=17.06.2020") )
        	.andExpect( status().isOk() )
        	.andReturn();
        
        assertTrue( transactionRepository.count() == 7 );
        
        String responseString = result.getResponse().getContentAsString();
        JSONObject responseJsonObject = new JSONObject(responseString);
        String contentString = responseJsonObject.getString("content");
        JSONArray contentJsonArray = new JSONArray(contentString);
       
        assertTrue( contentJsonArray.length() == 3 );
        assertTrue( contentJsonArray.getJSONObject(0).get("description").equals("My first income transaction") );
        assertTrue( contentJsonArray.getJSONObject(1).get("description").equals("My second income transaction") );
        assertTrue( contentJsonArray.getJSONObject(2).get("description").equals("My third income transaction") );
    }
	
	@Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void getTransactionsBetweenDatesByTypeAndPageableMethod_ShouldReturnOKStatusCodeAndReturnExpenseTransactionsBetweenDates() throws Exception {
    	UserEntity testUser = insertTestUser(TEST_USER_EMAIL);

    	SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
    	
    	// Should be returned
    	insertTestTransactionForUser(testUser, "EXPENSE", dateFormatter.parse("24.12.2019"), "My first expense transaction");
    	insertTestTransactionForUser(testUser, "EXPENSE", dateFormatter.parse("12.05.2020"), "My second expense transaction");
    	insertTestTransactionForUser(testUser, "EXPENSE", dateFormatter.parse("17.06.2020"), "My third expense transaction");
    	
    	// Should not be returned (not of proper type or in date range)
    	insertTestTransactionForUser(testUser, "EXPENSE", dateFormatter.parse("23.12.2019"), "My fourth expense transaction");
    	insertTestTransactionForUser(testUser, "EXPENSE", dateFormatter.parse("19.06.2020"), "My fifth expense transaction");
    	insertTestTransactionForUser(testUser, "INCOME", new Date(), "My income transaction");
    	insertTestTransactionForUser(testUser, "TRANSFER", new Date(), "My transfer transaction");
    	
        MvcResult result = this.mockMvc
        	.perform( get("/transactions/between-dates?type=EXPENSE&startDate=24.12.2019&endDate=17.06.2020") )
        	.andExpect( status().isOk() )
        	.andReturn();
        
        assertTrue( transactionRepository.count() == 7 );
        
        String responseString = result.getResponse().getContentAsString();
        JSONObject responseJsonObject = new JSONObject(responseString);
        String contentString = responseJsonObject.getString("content");
        JSONArray contentJsonArray = new JSONArray(contentString);
       
        assertTrue( contentJsonArray.length() == 3 );
        assertTrue( contentJsonArray.getJSONObject(0).get("description").equals("My first expense transaction") );
        assertTrue( contentJsonArray.getJSONObject(1).get("description").equals("My second expense transaction") );
        assertTrue( contentJsonArray.getJSONObject(2).get("description").equals("My third expense transaction") );
    }
	
	@Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void getTransactionsBetweenDatesByTypeAndPageableMethod_ShouldReturnOKStatusCodeAndReturnTransferTransactionsBetweenDates() throws Exception {
    	UserEntity testUser = insertTestUser(TEST_USER_EMAIL);

    	SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
    	
    	// Should be returned
    	insertTestTransactionForUser(testUser, "TRANSFER", dateFormatter.parse("24.12.2019"), "My first transfer transaction");
    	insertTestTransactionForUser(testUser, "TRANSFER", dateFormatter.parse("12.05.2020"), "My second transfer transaction");
    	insertTestTransactionForUser(testUser, "TRANSFER", dateFormatter.parse("17.06.2020"), "My third transfer transaction");
    	
    	// Should not be returned (not of proper type or in date range)
    	insertTestTransactionForUser(testUser, "TRANSFER", dateFormatter.parse("23.12.2019"), "My fourth transfer transaction");
    	insertTestTransactionForUser(testUser, "TRANSFER", dateFormatter.parse("19.06.2020"), "My fifth transfer transaction");
    	insertTestTransactionForUser(testUser, "INCOME", new Date(), "My income transaction");
    	insertTestTransactionForUser(testUser, "EXPENSE", new Date(), "My expense transaction");
    	
        MvcResult result = this.mockMvc
        	.perform( get("/transactions/between-dates?type=TRANSFER&startDate=24.12.2019&endDate=17.06.2020") )
        	.andExpect( status().isOk() )
        	.andReturn();
        
        assertTrue( transactionRepository.count() == 7 );
        
        String responseString = result.getResponse().getContentAsString();
        JSONObject responseJsonObject = new JSONObject(responseString);
        String contentString = responseJsonObject.getString("content");
        JSONArray contentJsonArray = new JSONArray(contentString);
       
        assertTrue( contentJsonArray.length() == 3 );
        assertTrue( contentJsonArray.getJSONObject(0).get("description").equals("My first transfer transaction") );
        assertTrue( contentJsonArray.getJSONObject(1).get("description").equals("My second transfer transaction") );
        assertTrue( contentJsonArray.getJSONObject(2).get("description").equals("My third transfer transaction") );
    }
	
	@Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void getTransactionsBetweenDatesByTypeAndPageableMethod_ShouldReturnOKStatusCodeAndReturnOneTransactionBetweenDates() throws Exception {
    	UserEntity testUser = insertTestUser(TEST_USER_EMAIL);

    	SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
    	
    	insertTestTransactionForUser(testUser, "EXPENSE", dateFormatter.parse("24.12.2019"), "My first expense transaction");
    	insertTestTransactionForUser(testUser, "EXPENSE", dateFormatter.parse("12.05.2020"), "My second expense transaction");
    	insertTestTransactionForUser(testUser, "EXPENSE", dateFormatter.parse("17.06.2020"), "My third expense transaction");
    	
    	insertTestTransactionForUser(testUser, "EXPENSE", dateFormatter.parse("23.12.2019"), "My fourth expense transaction");
    	insertTestTransactionForUser(testUser, "EXPENSE", dateFormatter.parse("19.06.2020"), "My fifth expense transaction");
    	insertTestTransactionForUser(testUser, "INCOME", new Date(), "My income transaction");
    	insertTestTransactionForUser(testUser, "TRANSFER", new Date(), "My transfer transaction");
    	
        MvcResult result = this.mockMvc
        	.perform( get("/transactions/between-dates?type=EXPENSE&startDate=24.12.2019&endDate=17.06.2020&page=0&size=1") )
        	.andExpect( status().isOk() )
        	.andReturn();
        
        assertTrue( transactionRepository.count() == 7 );
        
        String responseString = result.getResponse().getContentAsString();
        JSONObject responseJsonObject = new JSONObject(responseString);
        String contentString = responseJsonObject.getString("content");
        JSONArray contentJsonArray = new JSONArray(contentString);
       
        assertTrue( contentJsonArray.length() == 1 );
        assertTrue( contentJsonArray.getJSONObject(0).get("description").equals("My first expense transaction") );
    }
    
    /**
     * getTotalTransactionsSumBetweenDatesByType() tests
     * (GET "/transactions/total-sum-between-dates" 
     *  with request params "type", "startDate" & "endDate")
     */
    
	@Test
	public void getTotalTransactionsSumBetweenDatesByTypeMethod_ShouldReturnUnauthorizedStatusCode() throws Exception {
	    this.mockMvc.perform( get("/transactions/total-sum-between-dates?type=EXPENSE") ).andExpect( status().isUnauthorized() );
	}
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTotalTransactionsSumBetweenDatesByTypeMethod_ShouldReturnBadRequestStatusCodeBecauseOfMissingParams() throws Exception {
	    insertTestUser(TEST_USER_EMAIL);
		this.mockMvc.perform( get("/transactions/total-sum-between-dates?type=INCOME") ).andExpect( status().isBadRequest() );
	}
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTotalTransactionsSumBetweenDatesByTypeMethod_ShouldReturnBadRequestStatusCodeBecauseOfInvalidType() throws Exception {
	    insertTestUser(TEST_USER_EMAIL);
		this.mockMvc.perform( get("/transactions/total-sum-between-dates?type=IVALID_TRANSACTION_TYPE&startDate=24.12.2019&endDate=17.06.2020") ).andExpect( status().isBadRequest() );
	}
	
	@Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void getTotalTransactionsSumBetweenDatesByTypeMethod_ShouldReturnOKStatusCodeAndReturnIncomeTransactionsBetweenDatesSum() throws Exception {
    	UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
    	
    	SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
    	
    	// Should be used for sum calculation
    	insertTestTransactionForUser(testUser, "INCOME", dateFormatter.parse("24.12.2019"), "My first income transaction");
    	insertTestTransactionForUser(testUser, "INCOME", dateFormatter.parse("12.05.2020"), "My second income transaction");
    	insertTestTransactionForUser(testUser, "INCOME", dateFormatter.parse("17.06.2020"), "My third income transaction");
    	
    	// Should not be used for sum calculation (not of proper type or in date range)
    	insertTestTransactionForUser(testUser, "INCOME", dateFormatter.parse("23.12.2019"), "My fourth income transaction");
    	insertTestTransactionForUser(testUser, "INCOME", dateFormatter.parse("19.06.2020"), "My fifth income transaction");
    	insertTestTransactionForUser(testUser, "EXPENSE", new Date(), "My expense transaction");
    	insertTestTransactionForUser(testUser, "TRANSFER", new Date(), "My transfer transaction");
    	
        MvcResult result = this.mockMvc
        	.perform( get("/transactions/total-sum-between-dates?type=INCOME&startDate=24.12.2019&endDate=17.06.2020") )
        	.andExpect( status().isOk() )
        	.andReturn();
        
        String responseString = result.getResponse().getContentAsString();
        assertTrue( Double.valueOf(responseString) == 300.00d );
    }
	
	@Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void getTotalTransactionsSumBetweenDatesByTypeMethod_ShouldReturnOKStatusCodeAndReturnExpenseTransactionsBetweenDatesSum() throws Exception {
    	UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
    	
    	SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
    	
    	// Should be used for sum calculation
    	insertTestTransactionForUser(testUser, "EXPENSE", dateFormatter.parse("24.12.2019"), "My first expense transaction");
    	insertTestTransactionForUser(testUser, "EXPENSE", dateFormatter.parse("12.05.2020"), "My second expense transaction");
    	insertTestTransactionForUser(testUser, "EXPENSE", dateFormatter.parse("17.06.2020"), "My third expense transaction");
    	
    	// Should not be used for sum calculation (not of proper type or in date range)
    	insertTestTransactionForUser(testUser, "EXPENSE", dateFormatter.parse("23.12.2019"), "My fourth expense transaction");
    	insertTestTransactionForUser(testUser, "EXPENSE", dateFormatter.parse("19.06.2020"), "My fifth expense transaction");
    	insertTestTransactionForUser(testUser, "INCOME", new Date(), "My income transaction");
    	insertTestTransactionForUser(testUser, "TRANSFER", new Date(), "My transfer transaction");
    	
        MvcResult result = this.mockMvc
        	.perform( get("/transactions/total-sum-between-dates?type=EXPENSE&startDate=24.12.2019&endDate=17.06.2020") )
        	.andExpect( status().isOk() )
        	.andReturn();
        
        String responseString = result.getResponse().getContentAsString();
        assertTrue( Double.valueOf(responseString) == 300.00d );
    }
	
	@Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void getTotalTransactionsSumBetweenDatesByTypeMethod_ShouldReturnOKStatusCodeAndReturnTransferTransactionsBetweenDatesSum() throws Exception {
    	UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
    	
    	SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
    	
    	// Should be used for sum calculation
    	insertTestTransactionForUser(testUser, "TRANSFER", dateFormatter.parse("24.12.2019"), "My first transfer transaction");
    	insertTestTransactionForUser(testUser, "TRANSFER", dateFormatter.parse("12.05.2020"), "My second transfer transaction");
    	insertTestTransactionForUser(testUser, "TRANSFER", dateFormatter.parse("17.06.2020"), "My third transfer transaction");
    	
    	// Should not be used for sum calculation (not of proper type or in date range)
    	insertTestTransactionForUser(testUser, "TRANSFER", dateFormatter.parse("23.12.2019"), "My fourth transfer transaction");
    	insertTestTransactionForUser(testUser, "TRANSFER", dateFormatter.parse("19.06.2020"), "My fifth transfer transaction");
    	insertTestTransactionForUser(testUser, "INCOME", new Date(), "My income transaction");
    	insertTestTransactionForUser(testUser, "EXPENSE", new Date(), "My expense transaction");
    	
        MvcResult result = this.mockMvc
        	.perform( get("/transactions/total-sum-between-dates?type=TRANSFER&startDate=24.12.2019&endDate=17.06.2020") )
        	.andExpect( status().isOk() )
        	.andReturn();
        
        String responseString = result.getResponse().getContentAsString();
        assertTrue( Double.valueOf(responseString) == 300.00d );
    }
    
    /**
     * getTransactionsByFromDataAndPageable() tests
     * (GET "/transactions/by-from-data" 
     *  with request params "fromType" & "fromId")
     */
    
	@Test
	public void getTransactionsByFromDataAndPageableMethod_ShouldReturnUnauthorizedStatusCode() throws Exception {
	    this.mockMvc.perform( get("/transactions/by-from-data?fromType=ACCOUNT&fromId=1") ).andExpect( status().isUnauthorized() );
	}
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTransactionsByFromDataAndPageableMethod_ShouldReturnBadRequestStatusCodeBecauseOfMissingParams() throws Exception {
	    insertTestUser(TEST_USER_EMAIL);
		this.mockMvc.perform( get("/transactions/by-from-data") ).andExpect( status().isBadRequest() );
	}
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTransactionsByFromDataAndPageableMethod_ShouldReturnBadRequestStatusCodeBecauseOfInvalidType() throws Exception {
	    insertTestUser(TEST_USER_EMAIL);
		this.mockMvc.perform( get("/transactions/by-from-data?fromType=INVALID_FROM_TYPE&fromId=1") ).andExpect( status().isBadRequest() );
	}
	
	@Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void getTransactionsByFromDataAndPageableMethod_ShouldReturnOKStatusCodeAndReturnTransactionsWithAccountFromTypeAndId() throws Exception {
    	UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
    	
    	// Note: only EXPENSE & TRANSFER type transactions have an ACCOUNT fromType.
    	TransactionEntity firstExpenseTransaction = insertTestTransactionForUser(testUser, "EXPENSE", new Date(), "My first expense transaction");
    	TransactionEntity secondExpenseTransaction = insertTestTransactionForUser(testUser, "EXPENSE", new Date(), "My second expense transaction");
    	TransactionEntity transferTransaction = insertTestTransactionForUser(testUser, "TRANSFER", new Date(), "My transfer transaction");
    	insertTestTransactionForUser(testUser, "INCOME", new Date(), "My income transaction");
    	
    	Long firstExpenseTransactionFromId = firstExpenseTransaction.getFromId();
    	Long secondExpenseTransactionFromId = secondExpenseTransaction.getFromId();
    	Long transferTransactionFromId = transferTransaction.getFromId();
    	assertTrue( firstExpenseTransactionFromId == secondExpenseTransactionFromId );
    	assertTrue( secondExpenseTransactionFromId == transferTransactionFromId );
    	assertTrue( firstExpenseTransactionFromId == transferTransactionFromId );
    	
        MvcResult result = this.mockMvc
        	.perform( get("/transactions/by-from-data?fromType=ACCOUNT&fromId=" + firstExpenseTransactionFromId) )
        	.andExpect( status().isOk() )
        	.andReturn();
        
        assertTrue( transactionRepository.count() == 4 );
        
        String responseString = result.getResponse().getContentAsString();
        JSONObject responseJsonObject = new JSONObject(responseString);
        String contentString = responseJsonObject.getString("content");
        JSONArray contentJsonArray = new JSONArray(contentString);
       
        assertTrue( contentJsonArray.length() == 3 );
        assertTrue( contentJsonArray.getJSONObject(0).get("description").equals("My first expense transaction") );
        assertTrue( contentJsonArray.getJSONObject(1).get("description").equals("My second expense transaction") );
        assertTrue( contentJsonArray.getJSONObject(2).get("description").equals("My transfer transaction") );
    }
	
	@Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void getTransactionsByFromDataAndPageableMethod_ShouldReturnOKStatusCodeAndReturnTransactionsWithCategoryFromTypeAndId() throws Exception {
    	UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
    	
    	// Note: only INCOME type transactions have a CATEGORY fromType.
    	TransactionEntity firstIncomeTransaction = insertTestTransactionForUser(testUser, "INCOME", new Date(), "My first income transaction");
    	TransactionEntity secondIncomeTransaction = insertTestTransactionForUser(testUser, "INCOME", new Date(), "My second income transaction");
    	insertTestTransactionForUser(testUser, "TRANSFER", new Date(), "My transfer transaction");
    	insertTestTransactionForUser(testUser, "EXPENSE", new Date(), "My expense transaction");
    	
    	Long firstIncomeTransactionFromId = firstIncomeTransaction.getFromId();
    	Long secondIncomeTransactionFromId = secondIncomeTransaction.getFromId();
    	assertTrue( firstIncomeTransactionFromId == secondIncomeTransactionFromId );
    	
        MvcResult result = this.mockMvc
        	.perform( get("/transactions/by-from-data?fromType=CATEGORY&fromId=" + firstIncomeTransactionFromId) )
        	.andExpect( status().isOk() )
        	.andReturn();
        
        assertTrue( transactionRepository.count() == 4 );
        
        String responseString = result.getResponse().getContentAsString();
        JSONObject responseJsonObject = new JSONObject(responseString);
        String contentString = responseJsonObject.getString("content");
        JSONArray contentJsonArray = new JSONArray(contentString);
       
        assertTrue( contentJsonArray.length() == 2 );
        assertTrue( contentJsonArray.getJSONObject(0).get("description").equals("My first income transaction") );
        assertTrue( contentJsonArray.getJSONObject(1).get("description").equals("My second income transaction") );
    }
	
	@Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void getTransactionsByFromDataAndPageableMethod_ShouldReturnOKStatusCodeAndReturnOneTransactionWithCategoryFromTypeAndId() throws Exception {
    	UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
    	
    	// Note: only INCOME type transactions have a CATEGORY fromType.
    	TransactionEntity firstIncomeTransaction = insertTestTransactionForUser(testUser, "INCOME", new Date(), "My first income transaction");
    	TransactionEntity secondIncomeTransaction = insertTestTransactionForUser(testUser, "INCOME", new Date(), "My second income transaction");
    	insertTestTransactionForUser(testUser, "TRANSFER", new Date(), "My transfer transaction");
    	insertTestTransactionForUser(testUser, "EXPENSE", new Date(), "My expense transaction");
    	
    	Long firstIncomeTransactionFromId = firstIncomeTransaction.getFromId();
    	Long secondIncomeTransactionFromId = secondIncomeTransaction.getFromId();
    	assertTrue( firstIncomeTransactionFromId == secondIncomeTransactionFromId );
    	
        MvcResult result = this.mockMvc
        	.perform( get("/transactions/by-from-data?fromType=CATEGORY&fromId=" + firstIncomeTransactionFromId + "&page=1&size=1") )
        	.andExpect( status().isOk() )
        	.andReturn();
        
        assertTrue( transactionRepository.count() == 4 );
        
        String responseString = result.getResponse().getContentAsString();
        JSONObject responseJsonObject = new JSONObject(responseString);
        String contentString = responseJsonObject.getString("content");
        JSONArray contentJsonArray = new JSONArray(contentString);
       
        assertTrue( contentJsonArray.length() == 1 );
        assertTrue( contentJsonArray.getJSONObject(0).get("description").equals("My second income transaction") );
    }
    
    /**
     * getTransactionsByToDataAndPageable() tests
     * (GET "/transactions/by-to-data" 
     *  with request params "toType" & "toId")
     */
    
	@Test
	public void getTransactionsByToDataAndPageableMethod_ShouldReturnUnauthorizedStatusCode() throws Exception {
	    this.mockMvc.perform( get("/transactions/by-to-data?toType=ACCOUNT&toId=1") ).andExpect( status().isUnauthorized() );
	}
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTransactionsByToDataAndPageableMethod_ShouldReturnBadRequestStatusCodeBecauseOfMissingParams() throws Exception {
	    insertTestUser(TEST_USER_EMAIL);
		this.mockMvc.perform( get("/transactions/by-to-data") ).andExpect( status().isBadRequest() );
	}
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getTransactionsByToDataAndPageableMethod_ShouldReturnBadRequestStatusCodeBecauseOfInvalidType() throws Exception {
	    insertTestUser(TEST_USER_EMAIL);
		this.mockMvc.perform( get("/transactions/by-to-data?toType=INVALID_TO_TYPE&toId=1") ).andExpect( status().isBadRequest() );
	}
	
	@Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void getTransactionsByToDataAndPageableMethod_ShouldReturnOKStatusCodeAndReturnTransactionsWithAccountToTypeAndId() throws Exception {
    	UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
    	
    	// Note: only INCOME & TRANSFER type transactions have an ACCOUNT toType.
    	TransactionEntity firstIncomeTransaction = insertTestTransactionForUser(testUser, "INCOME", new Date(), "My first income transaction");
    	TransactionEntity secondIncomeTransaction = insertTestTransactionForUser(testUser, "INCOME", new Date(), "My second income transaction");
    	TransactionEntity transferTransaction = insertTestTransactionForUser(testUser, "TRANSFER", new Date(), "My transfer transaction");
    	insertTestTransactionForUser(testUser, "EXPENSE", new Date(), "My expense transaction");
    	
    	Long firstIncomeTransactionToId = firstIncomeTransaction.getToId();
    	Long secondIncomeTransactionToId = secondIncomeTransaction.getToId();
    	Long transferTransactionToId = transferTransaction.getToId();
    	assertTrue( firstIncomeTransactionToId == secondIncomeTransactionToId );
    	assertTrue( secondIncomeTransactionToId == transferTransactionToId );
    	assertTrue( firstIncomeTransactionToId == transferTransactionToId );
    	
        MvcResult result = this.mockMvc
        	.perform( get("/transactions/by-to-data?toType=ACCOUNT&toId=" + firstIncomeTransactionToId) )
        	.andExpect( status().isOk() )
        	.andReturn();
        
        assertTrue( transactionRepository.count() == 4 );
        
        String responseString = result.getResponse().getContentAsString();
        JSONObject responseJsonObject = new JSONObject(responseString);
        String contentString = responseJsonObject.getString("content");
        JSONArray contentJsonArray = new JSONArray(contentString);
       
        assertTrue( contentJsonArray.length() == 3 );
        assertTrue( contentJsonArray.getJSONObject(0).get("description").equals("My first income transaction") );
        assertTrue( contentJsonArray.getJSONObject(1).get("description").equals("My second income transaction") );
        assertTrue( contentJsonArray.getJSONObject(2).get("description").equals("My transfer transaction") );
    }
	
	@Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void getTransactionsByToDataAndPageableMethod_ShouldReturnOKStatusCodeAndReturnTransactionsWithCategoryToTypeAndId() throws Exception {
    	UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
    	
    	// Note: only EXPENSE type transactions have a CATEGORY toType.
    	TransactionEntity firstExpenseTransaction = insertTestTransactionForUser(testUser, "EXPENSE", new Date(), "My first expense transaction");
    	TransactionEntity secondExpenseTransaction = insertTestTransactionForUser(testUser, "EXPENSE", new Date(), "My second expense transaction");
    	insertTestTransactionForUser(testUser, "TRANSFER", new Date(), "My transfer transaction");
    	insertTestTransactionForUser(testUser, "INCOME", new Date(), "My income transaction");
    	
    	Long firstExpenseTransactionToId = firstExpenseTransaction.getToId();
    	Long secondExpenseTransactionToId = secondExpenseTransaction.getToId();
    	assertTrue( firstExpenseTransactionToId == secondExpenseTransactionToId );
    	
        MvcResult result = this.mockMvc
        	.perform( get("/transactions/by-to-data?toType=CATEGORY&toId=" + firstExpenseTransactionToId) )
        	.andExpect( status().isOk() )
        	.andReturn();
        
        assertTrue( transactionRepository.count() == 4 );
        
        String responseString = result.getResponse().getContentAsString();
        JSONObject responseJsonObject = new JSONObject(responseString);
        String contentString = responseJsonObject.getString("content");
        JSONArray contentJsonArray = new JSONArray(contentString);
       
        assertTrue( contentJsonArray.length() == 2 );
        assertTrue( contentJsonArray.getJSONObject(0).get("description").equals("My first expense transaction") );
        assertTrue( contentJsonArray.getJSONObject(1).get("description").equals("My second expense transaction") );
    }
	
	@Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void getTransactionsByToDataAndPageableMethod_ShouldReturnOKStatusCodeAndReturnOneTransactionWithCategoryToTypeAndId() throws Exception {
    	UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
    	
    	// Note: only EXPENSE type transactions have a CATEGORY toType.
    	TransactionEntity firstExpenseTransaction = insertTestTransactionForUser(testUser, "EXPENSE", new Date(), "My first expense transaction");
    	TransactionEntity secondExpenseTransaction = insertTestTransactionForUser(testUser, "EXPENSE", new Date(), "My second expense transaction");
    	insertTestTransactionForUser(testUser, "TRANSFER", new Date(), "My transfer transaction");
    	insertTestTransactionForUser(testUser, "INCOME", new Date(), "My income transaction");
    	
    	Long firstExpenseTransactionToId = firstExpenseTransaction.getToId();
    	Long secondExpenseTransactionToId = secondExpenseTransaction.getToId();
    	assertTrue( firstExpenseTransactionToId == secondExpenseTransactionToId );
    	
        MvcResult result = this.mockMvc
        	.perform( get("/transactions/by-to-data?toType=CATEGORY&toId=" + firstExpenseTransactionToId + "&page=0&size=1") )
        	.andExpect( status().isOk() )
        	.andReturn();
        
        assertTrue( transactionRepository.count() == 4 );
        
        String responseString = result.getResponse().getContentAsString();
        JSONObject responseJsonObject = new JSONObject(responseString);
        String contentString = responseJsonObject.getString("content");
        JSONArray contentJsonArray = new JSONArray(contentString);
       
        assertTrue( contentJsonArray.length() == 1 );
        assertTrue( contentJsonArray.getJSONObject(0).get("description").equals("My first expense transaction") );
    }
    
    /**
     * getTransactionById() tests
     * (GET "/transactions/{id}")
     */
    
	@Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void getTransactionByIdMethodShouldReturnNotFoundStatusCode() throws Exception {
    	insertTestUser(TEST_USER_EMAIL);
        this.mockMvc.perform( get("/transactions/1") ).andExpect( status().isNotFound() );
    }
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void getTransactionByIdMethodShouldReturnForbiddenStatusCode() throws Exception {
    	insertTestUser(TEST_USER_EMAIL);
    	UserEntity anotherTestUser = insertTestUser("another_email@pfm.com");
    	
    	TransactionEntity testTransaction = insertTestTransactionForUser(anotherTestUser, "TRANSFER", new Date(), "My first transfer transaction");
    	
        this.mockMvc.perform( get("/transactions/" + testTransaction.getId()) ).andExpect( status().isForbidden() );
    }
    
    @Test
    public void getTransactionByIdMethodShouldReturnUnauthorizedStatusCode() throws Exception {
        this.mockMvc.perform( get("/transactions/1") ).andExpect( status().isUnauthorized() );
    }
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void getTransactionByIdMethodShouldReturnOKStatusCodeAndCorrectTransaction() throws Exception {
    	UserEntity anotherTestUser = insertTestUser(TEST_USER_EMAIL);
    	TransactionEntity testTransaction = insertTestTransactionForUser(anotherTestUser, "EXPENSE", new Date(), "My first expense transaction");
    	
    	// Test response code
    	Long testTransactionId = testTransaction.getId();
    	MvcResult result = this.mockMvc
    		.perform( get("/transactions/" + testTransactionId) )
    		.andExpect( status().isOk() )
    		.andReturn();
		
    	// Test returned object(s)
        String responseString = result.getResponse().getContentAsString();
        JSONObject jsonObject = new JSONObject(responseString);
        assertTrue( jsonObject.get("description").equals( testTransaction.getDescription() ) );
    }
    
    /**
     * createTransaction() tests
     * (POST "/transactions")
     */
    
    @Test
	public void createTransactionMethodShouldReturnUnauthorizedStatusCodeAndNoTransactionShouldBeInserted() throws Exception {
    	JSONObject requestBody = getTestTransactionJsonRequestBody("INCOME", "My income transaction", 1L, 2L);
    	
	    this.mockMvc
	    	.perform( 
	    			post("/transactions").contentType(MediaType.APPLICATION_JSON).content( requestBody.toString() )
	    	)
	    	.andExpect( status().isUnauthorized() );
	    
	    assertTrue( transactionRepository.count() == 0 );
	}
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void createTransactionMethodShouldReturnCreatedStatusCodeAndCreateIncomeTransaction() throws Exception {
    	UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
    	
    	AccountEntity testAccount = insertTestAccountForUser(testUser, AccountType.ACTIVATED, "Account 1");
    	CategoryEntity testIncomeCategory = insertTestCategoryForUser(testUser, CategoryType.INCOME, "Category 1");
    	Long testAccountId = testAccount.getId();
    	Long testIncomeCategoryId = testIncomeCategory.getId();
    	Double testAccountBalance = testAccount.getBalance();
    	Double testIncomeCategorySum = testIncomeCategory.getCurrentPeriodSum();
    	
    	JSONObject requestBody = getTestTransactionJsonRequestBody("INCOME", "My income transaction", testIncomeCategoryId, testAccountId);
 
    	MvcResult result = this.mockMvc
    		.perform( 
    			post("/transactions").contentType(MediaType.APPLICATION_JSON).content( requestBody.toString() )
    		)
    		.andExpect( status().isCreated() )
    		.andReturn();
    	
    	assertTrue( transactionRepository.count() == 1 );
    	Long createdTransactionId = transactionRepository.findAll().get(0).getId();
    	String jsonResponseString = result.getResponse().getContentAsString();
    	assertThat(jsonResponseString).contains("\"id\":" + createdTransactionId);
    	
    	Double transactionSum = requestBody.getDouble("sum");
    	assertTrue( 
    		accountRepository.findById(testAccountId).get().getBalance() == (testAccountBalance + transactionSum) 
    	);
    	assertTrue( 
    		categoryRepository.findById(testIncomeCategoryId).get().getCurrentPeriodSum() == (testIncomeCategorySum + transactionSum) 
    	);
    }
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void createTransactionMethodShouldThrowInvalidDataExceptionBecauseOfInvalidRequestBody() throws Exception {
    	UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
    	
    	AccountEntity testAccount = insertTestAccountForUser(testUser, AccountType.ACTIVATED, "Account 1");
    	CategoryEntity testIncomeCategory = insertTestCategoryForUser(testUser, CategoryType.INCOME, "Category 1");
    	Long testAccountId = testAccount.getId();
    	Long testIncomeCategoryId = testIncomeCategory.getId();
    	Double testAccountBalance = testAccount.getBalance();
    	Double testIncomeCategorySum = testIncomeCategory.getCurrentPeriodSum();
    	
    	JSONObject requestBody = getTestTransactionJsonRequestBody("INCOME", "My income transaction", testIncomeCategoryId, testAccountId);
    	requestBody.put("sum", null);
 
    	MvcResult result = this.mockMvc
    		.perform( 
    			post("/transactions").contentType(MediaType.APPLICATION_JSON).content( requestBody.toString() )
    		)
    		.andExpect( status().isBadRequest() )
    		.andReturn();
    	
    	assertTrue( result.getResolvedException() instanceof InvalidDataException );
    	assertTrue( result.getResolvedException().getMessage().equals("Transaction sum must not be null!") );
    	assertTrue( transactionRepository.count() == 0 );
    	assertTrue( accountRepository.findById(testAccountId).get().getBalance() == testAccountBalance );
    	assertTrue( categoryRepository.findById(testIncomeCategoryId).get().getCurrentPeriodSum() == testIncomeCategorySum );
    }
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void createTransactionMethodShouldThrowInvalidDataExceptionBecauseOfInvalidFromToData() throws Exception {
    	insertTestUser(TEST_USER_EMAIL);
    	
    	JSONObject requestBody = getTestTransactionJsonRequestBody("INCOME", "My income transaction", 1L, 2L);
 
    	MvcResult result = this.mockMvc
    		.perform( 
    			post("/transactions").contentType(MediaType.APPLICATION_JSON).content( requestBody.toString() )
    		)
    		.andExpect( status().isBadRequest() )
    		.andReturn();
    	
    	assertTrue( result.getResolvedException() instanceof InvalidDataException );
    	assertTrue( result.getResolvedException().getMessage().equals("Transaction contains invalid from-to data!") );
    	assertTrue( transactionRepository.count() == 0 );
    }
    
    /**
     * updateTransactionById() tests
     * (PUT "/transactions/{id}")
     */
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void updateTransactionByIdMethodShouldReturnNotFoundStatusCode() throws Exception {
    	insertTestUser(TEST_USER_EMAIL);
    	
    	JSONObject requestBody = this.getTestTransactionJsonRequestBody("EXPENSE", "My updated transaction", 1L, 2L);
    	
        this.mockMvc.perform(
        	put("/transactions/1").contentType(MediaType.APPLICATION_JSON).content( requestBody.toString()) 
        ).andExpect( status().isNotFound() );
    }
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void updateTransactionByIdMethodShouldReturnBadRequestStatusCodeBecauseOfNoBody() throws Exception {
    	UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
    	
    	TransactionEntity testTransaction = insertTestTransactionForUser(testUser, "TRANSFER", new Date(), "My first transfer transaction");
    	Long testTransactionId = testTransaction.getId();
    	
        this.mockMvc.perform( put("/transactions/" + testTransactionId) ).andExpect( status().isBadRequest() );
    }
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void updateTransactionByIdMethodShouldReturnBadRequestStatusCodeBecauseOfNullSum() throws Exception {
    	UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
    	TransactionEntity testTransaction = insertTestTransactionForUser(testUser, "TRANSFER", new Date(), "My first transfer transaction");
    	
    	JSONObject requestBody = this.getTestTransactionJsonRequestBody("EXPENSE", "My updated transaction", 1L, 2L);
    	requestBody.put("sum", null);
    	
    	// Test response code
    	Long testTransactionId = testTransaction.getId();
        MvcResult result = this.mockMvc.perform( 
        	put("/transactions/" + testTransactionId).contentType(MediaType.APPLICATION_JSON).content( requestBody.toString() ) 
        ).andExpect( status().isBadRequest() ).andReturn();
        
        // Test if the exception message contains the correct information
    	String exceptionMessage = result.getResolvedException().getMessage();
    	assertThat(exceptionMessage).contains("Transaction sum must not be null!");
    }
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void updateTransactionByIdMethodShouldReturnForbiddenStatusCode() throws Exception {
    	insertTestUser(TEST_USER_EMAIL);
    	UserEntity anotherTestUser = insertTestUser("another_email@pfm.com");
    	
    	TransactionEntity testTransaction = insertTestTransactionForUser(anotherTestUser, "TRANSFER", new Date(), "My first transfer transaction");
  
    	JSONObject requestBody = this.getTestTransactionJsonRequestBody("EXPENSE", "My updated transaction", 1L, 2L);
    	
    	Long testTransactionId = testTransaction.getId();
        this.mockMvc.perform( 
        	put("/transactions/" + testTransactionId)
        		.contentType(MediaType.APPLICATION_JSON).content( requestBody.toString() ) 
        )
        .andExpect( status().isForbidden() );
        
        assertTrue( transactionRepository.count() == 1 );
        assertTrue( transactionRepository.findById(testTransactionId).get().getDescription().equals("My first transfer transaction") );
    }
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void updateTransactionByIdMethodShouldReturnOkStatusCode() throws Exception {
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
    	 * Send transaction update request & test returned code/object
    	 */
    	JSONObject requestBody = this.getTestTransactionJsonRequestBody("EXPENSE", "My updated transaction", testAccountId, testExpenseCategoryId);
    	Double updatedTransactionSum = 1337.37d;
    	requestBody.put("sum", updatedTransactionSum);
    	
        MvcResult result = this.mockMvc.perform( 
        	put("/transactions/" + testTransactionId).contentType(MediaType.APPLICATION_JSON).content( requestBody.toString()) 
        ).andExpect( status().isOk() ).andReturn();
        
        String jsonResponseString = result.getResponse().getContentAsString();
        JSONObject jsonResponseObject = new JSONObject(jsonResponseString);
        assertTrue( jsonResponseObject.get("description").equals("My updated transaction") );
        assertTrue( ((double) jsonResponseObject.get("sum")) == 1337.37d );
        
        /*
         *  Test if transaction & from-to entity data has been updated
         */
		assertTrue( transactionRepository.count() == 1 );
		TransactionEntity updatedTransaction = transactionRepository.findById(testTransactionId).get();
        assertTrue( updatedTransaction.getDescription().equals("My updated transaction") );
        assertTrue( updatedTransaction.getFromType() == TransactionFromType.ACCOUNT );
        assertTrue( updatedTransaction.getToType() == TransactionToType.CATEGORY );
        assertTrue( updatedTransaction.getFromId() == testAccountId );
        assertTrue( updatedTransaction.getToId() == testExpenseCategoryId );
        assertEquals( updatedTransactionSum, updatedTransaction.getSum() );
        
        assertTrue( accountRepository.count() == 1 );
        AccountEntity updatedAccount = accountRepository.findById(testAccountId).get();
        assertTrue( updatedAccount.getBalance() == (initialTestAccountBalance - updatedTransactionSum) );
        
        assertTrue( categoryRepository.count() == 1 );
        CategoryEntity updatedExpenseCategory = categoryRepository.findById(testExpenseCategoryId).get();
        assertTrue( updatedExpenseCategory.getCurrentPeriodSum() == (initialTestExpenseCategorySum + updatedTransactionSum) );
    }
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void updateTransactionByIdMethodShouldReturnBadRequestStatusCodeBecauseOfInvalidFromToData() throws Exception {
    	UserEntity testUser = insertTestUser(TEST_USER_EMAIL);
    	
    	TransactionEntity testTransaction = insertTestTransactionForUser(testUser, "EXPENSE", new Date(), "My first transaction");
    	Long testTransactionId = testTransaction.getId();
 
    	JSONObject requestBody = this.getTestTransactionJsonRequestBody("EXPENSE", "My updated transaction", 1L, 2L);
    	Double updatedTransactionSum = 1337.37d;
    	requestBody.put("sum", updatedTransactionSum);
    	
        MvcResult result = this.mockMvc.perform( 
        	put("/transactions/" + testTransactionId).contentType(MediaType.APPLICATION_JSON).content( requestBody.toString()) 
        ).andExpect( status().isBadRequest() ).andReturn();
        
        assertTrue( result.getResolvedException() instanceof InvalidDataException );
        assertTrue( result.getResolvedException().getMessage().equals("Transaction contains invalid from-to data!") );
        
		assertTrue( transactionRepository.count() == 1 );
		TransactionEntity retrievedTransaction = transactionRepository.findById(testTransactionId).get();
        assertTrue( retrievedTransaction.getDescription().equals("My first transaction") );
    }
    
    /**
     * deleteTransactionById() tests
     * (DELETE "/transactions/{id}")
     */
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void deleteTransactionByIdMethodShouldReturnNotFoundStatusCode() throws Exception {
    	insertTestUser(TEST_USER_EMAIL);
        this.mockMvc.perform( delete("/transactions/1") ).andExpect( status().isNotFound() );
    }
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void deleteTransactionByIdMethodShouldReturnForbiddenStatusCode() throws Exception {
    	insertTestUser(TEST_USER_EMAIL);
    	UserEntity anotherTestUser = insertTestUser("another_email@pfm.com");
    	
    	TransactionEntity testTransaction = insertTestTransactionForUser(anotherTestUser, "TRANSFER", new Date(), "My first transfer transaction");
  
    	Long testTransactionId = testTransaction.getId();
        this.mockMvc.perform( delete("/transactions/" + testTransactionId) ).andExpect( status().isForbidden() );
        
        assertTrue( transactionRepository.count() == 1 );
        assertTrue( transactionRepository.findById(testTransactionId).get().getDescription().equals("My first transfer transaction") );
    }
    
    @Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void deleteTransactionByIdMethodShouldReturnOkStatusCode() throws Exception {
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
    	
        this.mockMvc.perform( delete("/transactions/" + testTransactionId) ).andExpect( status().isOk() );
   
        assertTrue( transactionRepository.count() == 0 );
        // Account balance should increase & category sum should decrease, because the EXPENSE transaction is undone by the deletion operation
        assertTrue( accountRepository.findById(testAccountId).get().getBalance() == (testAccountBalance+testTransactionSum) );
        assertTrue( categoryRepository.findById(testExpenseCategoryId).get().getCurrentPeriodSum() == (testExpenseCategorySum-testTransactionSum) ); 
    }
    
}