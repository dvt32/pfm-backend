package com.mse.personal.finance.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.NestedServletException;

import com.mse.personal.finance.db.entity.UserEntity;
import com.mse.personal.finance.db.repository.UserRepository;
import com.mse.personal.finance.model.FamilyStatusType;
import com.mse.personal.finance.model.GenderType;

/**
 * This class implements integration tests for the UserController class.
 * 
 * The @Transactional annotation is used to rollback 
 * database changes (made via a MockMvc request)
 * after the annotated test has finished executing.
 * 
 * @author dvt32
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class UserControllerTests {
	
	@Autowired
    private UserController userController;
	
	@Autowired
	private WebApplicationContext context;
	
	private MockMvc mockMvc;
	
	@Autowired
    private UserRepository userRepository;
	
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
     * Smoke test (tests if the controller loads properly)
     */
    @Test
    public void controllerShouldNotBeNull() {
        assertThat(userController).isNotNull();
    }
    
    /**
     * getUsersByPageable() tests
     * (GET "/users")
     */
    
    @Test
    @WithMockUser(roles = "ADMIN")
    public void getUsersMethodShouldReturnOKStatusCode() throws Exception {
        this.mockMvc.perform( get("/users") ).andExpect( status().isOk() );
    }
    
    @Test
    @WithMockUser(roles = "USER")
    public void getUsersMethodShouldReturnForbiddenStatusCode() throws Exception {
        this.mockMvc.perform( get("/users") ).andExpect( status().isForbidden() );
    }
    
    @Test
    public void getUsersMethodShouldReturnUnauthorizedStatusCode() throws Exception {
        this.mockMvc.perform( get("/users") ).andExpect( status().isUnauthorized() );
    }
    
    /**
     * getUserById() tests
     * (GET "/users/{id}")
     */
    
    @Test
    @WithMockUser(roles = "ADMIN")
    public void getUserByIdMethodShouldReturnNotFoundStatusCode() throws Exception {
        this.mockMvc.perform( get("/users/1") ).andExpect( status().isNotFound() );
    }
    
    @Test
    @WithMockUser(roles = "USER")
    public void getUserByIdMethodShouldReturnForbiddenStatusCode() throws Exception {
        this.mockMvc.perform( get("/users/1") ).andExpect( status().isForbidden() );
    }
    
    @Test
    public void getUserByIdMethodShouldReturnUnauthorizedStatusCode() throws Exception {
        this.mockMvc.perform( get("/users/1") ).andExpect( status().isUnauthorized() );
    }
    
    @Test
    @Transactional
    @WithMockUser(roles = "ADMIN")
    public void getUserByIdMethodShouldReturnOKStatusCodeAndCorrectUserEmail() throws Exception {
    	// Insert test user
    	UserEntity testUser = userRepository.save( 
    		new UserEntity(
    			"John Doe",
    			"123456", 
    			"test@pfm.com",  
    			GenderType.MALE,
    			FamilyStatusType.SINGLE,
    			24,
    			"Master's Degree"
    		)
    	);
    	
    	// Test if the response code is 200 OK
    	Long testUserId = testUser.getId();
    	MvcResult result = this.mockMvc
    		.perform( get("/users/" + testUserId) )
    		.andExpect( status().isOk() )
    		.andReturn();
		
    	// Test if the response string contains the inserted user's email.
		String jsonResponseString = result.getResponse().getContentAsString();
		assertThat(jsonResponseString).contains("test@pfm.com");
    	
		// Delete test user so that he does not interfere with other tests
		userRepository.delete(testUser);
    }
    
    /**
     * getUserByEmail() tests
     * (GET "/users?email=")
     */
    
    @Test
    @WithMockUser(roles = "ADMIN")
    public void getUserByEmailMethodShouldReturnNotFoundStatusCode() throws Exception {
        this.mockMvc.perform( get("/users?email=" + "test@pfm.com") ).andExpect( status().isNotFound() );
    }
    
    @Test
    @WithMockUser(roles = "USER")
    public void getUserByEmailMethodShouldReturnForbiddenStatusCode() throws Exception {
        this.mockMvc.perform( get("/users?email=" + "test@pfm.com") ).andExpect( status().isForbidden() );
    }
    
    @Test
    public void getUserByEmailMethodShouldReturnUnauthorizedStatusCode() throws Exception {
        this.mockMvc.perform( get("/users?email=" + "test@pfm.com") ).andExpect( status().isUnauthorized() );
    }
    
    @Test
    @Transactional
    @WithMockUser(roles = "ADMIN")
    public void getUserByEmailMethodShouldReturnOKStatusCodeAndCorrectUserName() throws Exception {
    	// Insert test user
    	UserEntity testUser = userRepository.save( 
    		new UserEntity(
    			"John Doe",
    			"123456", 
    			"test@pfm.com",  
    			GenderType.MALE,
    			FamilyStatusType.SINGLE,
    			24,
    			"Master's Degree"
    		)
    	);
    	
    	// Test if the response code is 200 OK
    	String testUserEmail = testUser.getEmail();
    	MvcResult result = this.mockMvc
    		.perform( get("/users?email=" + testUserEmail) )
    		.andExpect( status().isOk() )
    		.andReturn();
		
    	// Test if the response string contains the inserted user's email.
		String jsonResponseString = result.getResponse().getContentAsString();
		assertThat(jsonResponseString).contains("John Doe");
    	
		// Delete test user so that he does not interfere with other tests
		userRepository.delete(testUser);
    }
    
    /**
     * createUser() tests
     * (POST "/users")
     */
    
    @Test
    @Transactional
    public void createUserMethodShouldReturnCreatedStatusCode() throws Exception {
    	JSONObject postRequestBody = new JSONObject();
    	postRequestBody.put("name", "John Doe");
    	postRequestBody.put("password", "123456");
    	postRequestBody.put("email", "test@pfm.com");
    	postRequestBody.put("gender", "MALE");
    	postRequestBody.put("familyStatus", "SINGLE");
    	postRequestBody.put("age", 24);
    	postRequestBody.put("education", "Master's Degree");
 
    	// Test if the response code is 201 CREATED
    	MvcResult result = this.mockMvc
    		.perform( 
    			post("/users").contentType(MediaType.APPLICATION_JSON).content( postRequestBody.toString() )
    		)
    		.andExpect( status().isCreated() )
    		.andReturn();
    	
    	// Test if the response object contains the created user's ID
    	assertTrue( userRepository.count() == 1 );
    	Long createdUserId = userRepository.findAll().get(0).getId();
    	String jsonResponseString = result.getResponse().getContentAsString();
    	assertThat(jsonResponseString).contains("\"id\":" + createdUserId);
    }
    
    @Test
    @Transactional
    public void createUserMethodShouldReturnBadRequestStatusCodeBecauseOfInvalidEmail() throws Exception {
    	JSONObject postRequestBody = new JSONObject();
    	postRequestBody.put("name", "John Doe");
    	postRequestBody.put("password", "123456");
    	postRequestBody.put("email", "invalid-email"); // use invalid email
    	postRequestBody.put("gender", "MALE");
    	postRequestBody.put("familyStatus", "SINGLE");
    	postRequestBody.put("age", 24);
    	postRequestBody.put("education", "Master's Degree");
    	
    	// Test if the response code is 400 BAD REQUEST
    	MvcResult result = this.mockMvc
    		.perform( 
    			post("/users").contentType(MediaType.APPLICATION_JSON).content( postRequestBody.toString() )
    		)
    		.andExpect( status().isBadRequest() )
    		.andReturn();
    	
    	// Test if the exception message contains the correct information
    	String exceptionMessage = result.getResolvedException().getMessage();
    	assertThat(exceptionMessage).contains("Invalid email!");    	
    }
    
    @Test
    @Transactional
    public void createUserMethodShouldReturnBadRequestStatusCodeBecauseOfInvalidPassword() throws Exception {
    	JSONObject postRequestBody = new JSONObject();
    	postRequestBody.put("name", "John Doe");
    	postRequestBody.put("password", "123"); // invalid password (too short)
    	postRequestBody.put("email", "test@pfm.com");
    	postRequestBody.put("gender", "MALE");
    	postRequestBody.put("familyStatus", "SINGLE");
    	postRequestBody.put("age", 24);
    	postRequestBody.put("education", "Master's Degree");
    	
    	// Test if the response code is 400 BAD REQUEST
    	MvcResult result = this.mockMvc
    		.perform( 
    			post("/users").contentType(MediaType.APPLICATION_JSON).content( postRequestBody.toString() )
    		)
    		.andExpect( status().isBadRequest() )
    		.andReturn();
    	
    	// Test if the exception message contains the correct information
    	String exceptionMessage = result.getResolvedException().getMessage();
    	assertThat(exceptionMessage).contains("User password must be at least 6 characters!");    
    }
    
    @Test
    @Transactional
    public void createUserMethodShouldReturnBadRequestStatusCodeBecauseEmailAndPasswordAreEmpty() throws Exception {
    	JSONObject postRequestBody = new JSONObject();
    	postRequestBody.put("name", "John Doe");
    	postRequestBody.put("password", ""); // empty password
    	postRequestBody.put("email", ""); // empty email
    	postRequestBody.put("gender", "MALE");
    	postRequestBody.put("familyStatus", "SINGLE");
    	postRequestBody.put("age", 24);
    	postRequestBody.put("education", "Master's Degree");
    	
    	// Test if the response code is 400 BAD REQUEST
    	MvcResult result = this.mockMvc
    		.perform( 
    			post("/users").contentType(MediaType.APPLICATION_JSON).content( postRequestBody.toString() )
    		)
    		.andExpect( status().isBadRequest() )
    		.andReturn();
    	
    	// Test if the exception message contains the correct information
    	String exceptionMessage = result.getResolvedException().getMessage();
    	assertThat(exceptionMessage).contains("User password must not be null or blank!");
    	assertThat(exceptionMessage).contains("User password must be at least 6 characters!");
    	assertThat(exceptionMessage).contains("User email must not be null or blank!");
    }
    
    @Test(expected = DataIntegrityViolationException.class)
    @Transactional
    public void createUserMethodShouldThrowExceptionBecauseOfExistingEmail() throws Exception {
    	// Insert test user
    	UserEntity testUser = userRepository.save( 
    		new UserEntity(
    			"John Doe",
    			"123456", 
    			"test@pfm.com",  
    			GenderType.MALE,
    			FamilyStatusType.SINGLE,
    			24,
    			"Master's Degree"
    		)
    	);
    	
    	JSONObject postRequestBody = new JSONObject();
    	postRequestBody.put("name", "John Doe");
    	postRequestBody.put("password", "123456");
    	postRequestBody.put("email", "test@pfm.com"); // email already exists
    	postRequestBody.put("gender", "MALE");
    	postRequestBody.put("familyStatus", "SINGLE");
    	postRequestBody.put("age", 24);
    	postRequestBody.put("education", "Master's Degree");
    	
    	// Test if the request triggers an exception and throw the cause exception
    	try {
    		this.mockMvc.perform( 
    			post("/users").contentType(MediaType.APPLICATION_JSON).content( postRequestBody.toString() )
    		);
    	}
    	catch (NestedServletException e) {
    		throw (Exception) e.getCause();
    	}
    	finally {
    		// Delete test user so that he does not interfere with other tests
    		userRepository.delete(testUser);
    	}
    }
    
    /**
     * updateUserById() tests
     * (PUT "/users/{id}")
     */
    
    @Test
    @Transactional
    @WithMockUser(roles = "ADMIN")
    public void updateUserByIdMethodShouldReturnNotFoundStatusCode() throws Exception {
    	JSONObject requestBody = new JSONObject();
    	requestBody.put("name", "John Doe");
    	requestBody.put("password", "123456");
    	requestBody.put("email", "test@pfm.com");
    	requestBody.put("gender", "MALE");
    	requestBody.put("familyStatus", "SINGLE");
    	requestBody.put("age", 24);
    	requestBody.put("education", "Master's Degree");
    	
        this.mockMvc.perform(
        	put("/users/1").contentType(MediaType.APPLICATION_JSON).content( requestBody.toString()) 
        ).andExpect( status().isNotFound() );
    }
    
    @Test
    @Transactional
    @WithMockUser(roles = "USER")
    public void updateUserByIdMethodShouldReturnForbiddenStatusCode() throws Exception {
    	JSONObject requestBody = new JSONObject();
    	requestBody.put("name", "John Doe");
    	requestBody.put("password", "123456");
    	requestBody.put("email", "test@pfm.com");
    	requestBody.put("gender", "MALE");
    	requestBody.put("familyStatus", "SINGLE");
    	requestBody.put("age", 24);
    	requestBody.put("education", "Master's Degree");
    	
        this.mockMvc.perform( 
        	put("/users/1").contentType(MediaType.APPLICATION_JSON).content( requestBody.toString()) 
        ).andExpect( status().isForbidden() );
    }

    @Test
    @Transactional
    public void updateUserByIdMethodShouldReturnUnauthorizedStatusCode() throws Exception {
    	JSONObject requestBody = new JSONObject();
    	requestBody.put("name", "John Doe");
    	requestBody.put("password", "123456");
    	requestBody.put("email", "test@pfm.com");
    	requestBody.put("gender", "MALE");
    	requestBody.put("familyStatus", "SINGLE");
    	requestBody.put("age", 24);
    	requestBody.put("education", "Master's Degree");
    	
        this.mockMvc.perform( 
        	put("/users/1").contentType(MediaType.APPLICATION_JSON).content( requestBody.toString()) 
        ).andExpect( status().isUnauthorized() );
    }
    
    @Test
    @Transactional
    @WithMockUser(roles = "ADMIN")
    public void updateUserByIdMethodShouldReturnBadRequestStatusCodeBecauseOfNoBody() throws Exception {
        this.mockMvc.perform( put("/users/1") ).andExpect( status().isBadRequest() );
    }
    
    @Test
    @Transactional
    @WithMockUser(roles = "ADMIN")
    public void updateUserByIdMethodShouldReturnOKStatusCode() throws Exception {
    	// Insert test user
    	UserEntity testUser = userRepository.save( 
    		new UserEntity(
    			"John Doe",
    			"123456", 
    			"test@pfm.com",  
    			GenderType.MALE,
    			FamilyStatusType.SINGLE,
    			24,
    			"Master's Degree"
    		)
    	);
    	
    	JSONObject requestBody = new JSONObject();
    	requestBody.put("name", "Jane Doe"); // updated
    	requestBody.put("gender", "FEMALE"); // updated
    	requestBody.put("familyStatus", "SINGLE");
    	requestBody.put("age", 24);
    	requestBody.put("education", "Master's Degree");
    	
    	// Test if the response code is 200 OK
    	Long testUserId = testUser.getId();
        MvcResult result = this.mockMvc
        	.perform( 
        		put("/users/" + testUserId).contentType(MediaType.APPLICATION_JSON).content( requestBody.toString()) 
        	)
        	.andExpect( status().isOk() )
        	.andReturn();
        
        // Test if the response string contains updated data
 		String jsonResponseString = result.getResponse().getContentAsString();
		assertThat(jsonResponseString).contains("Jane Doe");
		assertThat(jsonResponseString).contains("FEMALE");
    	
    	// Test if the user's data has really been updated
    	assertTrue( userRepository.findById(testUserId).get().getName().equals("Jane Doe") );
    	assertTrue( userRepository.findById(testUserId).get().getGender() == GenderType.FEMALE );
        
	     // Delete test user so that he does not interfere with other tests
		userRepository.delete(testUser);
    }
    
    @Test
    @Transactional
    @WithMockUser(roles = "ADMIN")
    public void updateUserByIdMethodShouldReturnBadRequestStatusCodeBecauseOfNullName() throws Exception {
    	// Insert test user
    	UserEntity testUser = userRepository.save( 
    		new UserEntity(
    			"John Doe",
    			"123456", 
    			"test@pfm.com",  
    			GenderType.MALE,
    			FamilyStatusType.SINGLE,
    			24,
    			"Master's Degree"
    		)
    	);
    	
    	JSONObject requestBody = new JSONObject();
    	requestBody.put("name", null); // updated, but not valid
    	requestBody.put("gender", "FEMALE"); // updated
    	requestBody.put("familyStatus", "SINGLE");
    	requestBody.put("age", 24);
    	requestBody.put("education", "Master's Degree");
    	
    	// Test if the response code is 400 BAD REQUEST
    	Long testUserId = testUser.getId();
        MvcResult result = this.mockMvc
        	.perform( 
        		put("/users/" + testUserId).contentType(MediaType.APPLICATION_JSON).content( requestBody.toString()) 
        	)
        	.andExpect( status().isBadRequest() )
        	.andReturn();
        
        // Test if the exception message contains the correct information
    	String exceptionMessage = result.getResolvedException().getMessage();
    	assertThat(exceptionMessage).contains("User name must not be null!");
    	
    	// Test if the user's data has really been updated
    	assertTrue( userRepository.findById(testUserId).get().getName().equals("John Doe") );
    	assertTrue( userRepository.findById(testUserId).get().getGender() == GenderType.MALE );
        
	     // Delete test user so that he does not interfere with other tests
		userRepository.delete(testUser);
    }
    
    /**
     * updateUserByEmail() tests
     * (PUT "/users?email=")
     */
    
    @Test
    @Transactional
    @WithMockUser(roles = "ADMIN")
    public void updateUserByEmailMethodShouldReturnNotFoundStatusCode() throws Exception {
    	JSONObject requestBody = new JSONObject();
    	requestBody.put("name", "John Doe");
    	requestBody.put("password", "123456");
    	requestBody.put("email", "test@pfm.com");
    	requestBody.put("gender", "MALE");
    	requestBody.put("familyStatus", "SINGLE");
    	requestBody.put("age", 24);
    	requestBody.put("education", "Master's Degree");
    	
        this.mockMvc.perform(
        	put("/users?email=test@pfm.com").contentType(MediaType.APPLICATION_JSON).content( requestBody.toString()) 
        ).andExpect( status().isNotFound() );
    }
    
    @Test
    @Transactional
    @WithMockUser(roles = "USER")
    public void updateUserByEmailMethodShouldReturnForbiddenStatusCode() throws Exception {
    	JSONObject requestBody = new JSONObject();
    	requestBody.put("name", "John Doe");
    	requestBody.put("password", "123456");
    	requestBody.put("email", "test@pfm.com");
    	requestBody.put("gender", "MALE");
    	requestBody.put("familyStatus", "SINGLE");
    	requestBody.put("age", 24);
    	requestBody.put("education", "Master's Degree");
    	
        this.mockMvc.perform( 
        	put("/users?email=test@pfm.com").contentType(MediaType.APPLICATION_JSON).content( requestBody.toString()) 
        ).andExpect( status().isForbidden() );
    }


    @Test
    @Transactional
    public void updateUserByEmailMethodShouldReturnUnauthorizedStatusCode() throws Exception {
    	JSONObject requestBody = new JSONObject();
    	requestBody.put("name", "John Doe");
    	requestBody.put("password", "123456");
    	requestBody.put("email", "test@pfm.com");
    	requestBody.put("gender", "MALE");
    	requestBody.put("familyStatus", "SINGLE");
    	requestBody.put("age", 24);
    	requestBody.put("education", "Master's Degree");
    	
        this.mockMvc.perform( 
        	put("/users?email=test@pfm.com").contentType(MediaType.APPLICATION_JSON).content( requestBody.toString()) 
        ).andExpect( status().isUnauthorized() );
    }
    
    @Test
    @Transactional
    @WithMockUser(roles = "ADMIN")
    public void updateUserByEmailMethodShouldReturnBadRequestStatusCodeBecauseOfNoBody() throws Exception {
        this.mockMvc.perform( put("/users?email=test@pfm.com") ).andExpect( status().isBadRequest() );
    }
    
    @Test
    @Transactional
    @WithMockUser(roles = "ADMIN")
    public void updateUserByEmailMethodShouldReturnOKStatusCode() throws Exception {
    	// Insert test user
    	UserEntity testUser = userRepository.save( 
    		new UserEntity(
    			"John Doe",
    			"123456", 
    			"test@pfm.com",  
    			GenderType.MALE,
    			FamilyStatusType.SINGLE,
    			24,
    			"Master's Degree"
    		)
    	);
    	
    	JSONObject requestBody = new JSONObject();
    	requestBody.put("name", "Jane Doe"); // updated
    	requestBody.put("gender", "FEMALE"); // updated
    	requestBody.put("familyStatus", "SINGLE");
    	requestBody.put("age", 24);
    	requestBody.put("education", "Master's Degree");
    	
    	// Test if the response code is 200 OK
    	String testUserEmail = testUser.getEmail();
        MvcResult result = this.mockMvc
        	.perform( 
        		put("/users?email=" + testUserEmail).contentType(MediaType.APPLICATION_JSON).content( requestBody.toString()) 
        	)
        	.andExpect( status().isOk() )
        	.andReturn();
        
        // Test if the response string contains updated data
 		String jsonResponseString = result.getResponse().getContentAsString();
		assertThat(jsonResponseString).contains("Jane Doe");
		assertThat(jsonResponseString).contains("FEMALE");
    	
    	// Test if the user's data has really been updated
    	assertTrue( userRepository.findByEmail(testUserEmail).get().getName().equals("Jane Doe") );
    	assertTrue( userRepository.findByEmail(testUserEmail).get().getGender() == GenderType.FEMALE );
        
	     // Delete test user so that he does not interfere with other tests
		userRepository.delete(testUser);
    }
    
    @Test
    @Transactional
    @WithMockUser(roles = "ADMIN")
    public void updateUserByEmailMethodShouldReturnBadRequestStatusCodeBecauseOfNullName() throws Exception {
    	// Insert test user
    	UserEntity testUser = userRepository.save( 
    		new UserEntity(
    			"John Doe",
    			"123456", 
    			"test@pfm.com",  
    			GenderType.MALE,
    			FamilyStatusType.SINGLE,
    			24,
    			"Master's Degree"
    		)
    	);
    	
    	JSONObject requestBody = new JSONObject();
    	requestBody.put("name", null); // updated, but not valid
    	requestBody.put("gender", "FEMALE"); // updated
    	requestBody.put("familyStatus", "SINGLE");
    	requestBody.put("age", 24);
    	requestBody.put("education", "Master's Degree");
    	
    	// Test if the response code is 400 BAD REQUEST
    	String testUserEmail = testUser.getEmail();
        MvcResult result = this.mockMvc
        	.perform( 
        		put("/users?email=" + testUserEmail).contentType(MediaType.APPLICATION_JSON).content( requestBody.toString()) 
        	)
        	.andExpect( status().isBadRequest() )
        	.andReturn();
        
        // Test if the exception message contains the correct information
    	String exceptionMessage = result.getResolvedException().getMessage();
    	assertThat(exceptionMessage).contains("User name must not be null!");
    	
    	// Test if the user's data has really been updated
    	assertTrue( userRepository.findByEmail(testUserEmail).get().getName().equals("John Doe") );
    	assertTrue( userRepository.findByEmail(testUserEmail).get().getGender() == GenderType.MALE );
        
	     // Delete test user so that he does not interfere with other tests
		userRepository.delete(testUser);
    }
    
    /**
	* deleteUserById() tests
	* (DELETE "/users/{id}")
	*/
    
    @Test
    @Transactional
    @WithMockUser(roles = "ADMIN")
    public void deleteUserByIdMethodShouldReturnOKStatusCode() throws Exception {
    	// Insert test user
    	UserEntity testUser = userRepository.save( 
    		new UserEntity(
    			"John Doe",
    			"123456", 
    			"test@pfm.com",  
    			GenderType.MALE,
    			FamilyStatusType.SINGLE,
    			24,
    			"Master's Degree"
    		)
    	);
    	
    	// Test if the response code is 200 OK
    	Long testUserId = testUser.getId();
        MvcResult result = this.mockMvc
        	.perform( delete("/users/" + testUserId) )
        	.andExpect( status().isOk() )
        	.andReturn();
        
        // Test if the response string contains the deleted user's email.
 		String jsonResponseString = result.getResponse().getContentAsString();
 		assertThat(jsonResponseString).contains("test@pfm.com");
    	
    	// Test if the user has been deleted
    	assertTrue( !userRepository.existsById(testUserId) );
    	
    	// Just in case the user was not deleted via the request, delete him manually via repository
    	userRepository.delete(testUser);
    }
    
    @Test
    @Transactional
    @WithMockUser(roles = "ADMIN")
    public void deleteUserByIdMethodShouldReturnNotFoundStatusCode() throws Exception {
        this.mockMvc.perform( delete("/users/1") ).andExpect( status().isNotFound() );
    }
    
    @Test
    @Transactional
    @WithMockUser(roles = "USER")
    public void deleteUserByIdMethodShouldReturnForbiddenStatusCode() throws Exception {
        this.mockMvc.perform( delete("/users/1") ).andExpect( status().isForbidden() );
    }
    
    @Test
    @Transactional
    public void deleteUserByIdMethodShouldReturnUnauthorizedStatusCode() throws Exception {
        this.mockMvc.perform( delete("/users/1") ).andExpect( status().isUnauthorized() );
    }
    
    /**
	* deleteUserByEmail() tests
	* (DELETE "/users?email=")
	*/
    
    @Test
    @Transactional
    @WithMockUser(roles = "ADMIN")
    public void deleteUserByEmailMethodShouldReturnOKStatusCode() throws Exception {
    	// Insert test user
    	UserEntity testUser = userRepository.save( 
    		new UserEntity(
    			"John Doe",
    			"123456", 
    			"test@pfm.com",  
    			GenderType.MALE,
    			FamilyStatusType.SINGLE,
    			24,
    			"Master's Degree"
    		)
    	);
    	
    	// Test if the response code is 200 OK
    	String testUserEmail = testUser.getEmail();
        MvcResult result = this.mockMvc
        	.perform( delete("/users?email=" + testUserEmail) )
        	.andExpect( status().isOk() )
        	.andReturn();
        
        // Test if the response string contains the deleted user's email.
 		String jsonResponseString = result.getResponse().getContentAsString();
 		assertThat(jsonResponseString).contains("test@pfm.com");
    	
    	// Test if the user has been deleted
    	assertTrue( !userRepository.existsByEmail(testUserEmail) );
    	
    	// Just in case the user was not deleted via the request, delete him manually via repository
    	userRepository.delete(testUser);
    }
    
    @Test
    @Transactional
    @WithMockUser(roles = "ADMIN")
    public void deleteUserByEmailMethodShouldReturnNotFoundStatusCode() throws Exception {
        this.mockMvc.perform( delete("/users?email=test@pfm.com") ).andExpect( status().isNotFound() );
    }
    
    @Test
    @Transactional
    @WithMockUser(roles = "USER")
    public void deleteUserByEmailMethodShouldReturnForbiddenStatusCode() throws Exception {
        this.mockMvc.perform( delete("/users?email=test@pfm.com") ).andExpect( status().isForbidden() );
    }
    
    @Test
    @Transactional
    public void deleteUserByEmailMethodShouldReturnUnauthorizedStatusCode() throws Exception {
        this.mockMvc.perform( delete("/users?email=test@pfm.com") ).andExpect( status().isUnauthorized() );
    }
    
}