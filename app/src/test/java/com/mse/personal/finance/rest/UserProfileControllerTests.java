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

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.json.JSONArray;
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
import org.springframework.web.util.NestedServletException;

import com.mse.personal.finance.db.entity.UserEntity;
import com.mse.personal.finance.db.entity.UserSettingEntity;
import com.mse.personal.finance.db.repository.UserRepository;
import com.mse.personal.finance.db.repository.UserSettingRepository;
import com.mse.personal.finance.model.FamilyStatusType;
import com.mse.personal.finance.model.GenderType;
import com.mse.personal.finance.model.UserSettingKey;

/**
 * This class implements integration tests for the UserProfileController class.
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
public class UserProfileControllerTests {
	
	@Autowired
    private UserProfileController userProfileController;
	
	@Autowired
	private WebApplicationContext context;
	
	private MockMvc mockMvc;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
    private UserRepository userRepository;
	
	@Autowired
	private UserSettingRepository userSettingRepository;
	
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
        assertThat(userProfileController).isNotNull();
    }
    
    /**
     * getCurrentUser() tests
     * (GET "/profile")
     */
    
    @Test(expected = NoSuchElementException.class)
    @Transactional
    @WithMockUser(roles = "USER")
    public void getCurrentUserMethodShouldThrowExceptionBecauseOfMissingUser() throws Exception {
    	try {
    		this.mockMvc.perform( get("/profile") );
    	}
        catch (NestedServletException e) {
        	throw (Exception) e.getCause();
        }
    }
    
    @Test
    @Transactional
    @WithMockUser(roles = "USER", username="test@pfm.com")
    public void getCurrentUserMethodShouldReturnOKStatusCode() throws Exception {
    	// Insert test user with the logged in mock user's data
    	UserEntity testUser = userRepository.save( 
    		new UserEntity(
    			"John Doe",
    			"123456", 
    			"test@pfm.com", // must match username in @WithMockUser annotation
    			GenderType.MALE,
    			FamilyStatusType.SINGLE,
    			24,
    			"Master's Degree"
    		)
    	);
    	
    	// Test if the response code is 200 OK
        MvcResult result = this.mockMvc
        	.perform( get("/profile") )
        	.andExpect( status().isOk() )
        	.andReturn();
        
        // Test if the response string is the expected JSON object
    	String resultString = result.getResponse().getContentAsString();
    	JSONObject output = new JSONObject(resultString);
    	assertTrue( output.get("email").equals("test@pfm.com") );
    	assertTrue( output.get("name").equals("John Doe") );
    	assertTrue( output.get("education").equals("Master's Degree") );
    	
    	// Test if the user's info matches the output
    	Long testUserId = testUser.getId();
    	UserEntity retrievedTestUser = userRepository.findById(testUserId).get();
    	assertTrue( retrievedTestUser.getEmail().equals(output.get("email")) );
    	assertTrue( retrievedTestUser.getName().equals(output.get("name")) );
    	assertTrue( retrievedTestUser.getEducation().equals(output.get("education")) );
    	
    	// Delete test user so that he does not interfere with other tests
    	userRepository.delete(testUser);
    }
    
    /**
	* updateCurrentUser() tests
	* (PUT "/profile")
	*/
    
    @Test(expected = NoSuchElementException.class)
    @Transactional
    @WithMockUser(roles = "USER", username="test@pfm.com")
    public void updateCurrentUserMethodShouldThrowExceptionBecauseOfMissingUser() throws Exception {
    	JSONObject postRequestBody = new JSONObject();
    	postRequestBody.put("name", "John Doe");
    	postRequestBody.put("gender", "MALE");
    	postRequestBody.put("familyStatus", "SINGLE");
    	postRequestBody.put("age", 24);
    	postRequestBody.put("education", "Master's Degree");
    	
    	try {
    		 this.mockMvc.perform( 
         		put("/profile")
         			.contentType(MediaType.APPLICATION_JSON)
         			.content( postRequestBody.toString()
         		)
         	);
    	}
        catch (NestedServletException e) {
        	throw (Exception) e.getCause();
        }
    }
    
    @Test
    @Transactional
    @WithMockUser(roles = "USER", username="test@pfm.com")
    public void updateCurrentUserMethodShouldReturnOKStatusCode() throws Exception {
    	// Insert test user with the logged in mock user's data
    	UserEntity testUser = userRepository.save( 
    		new UserEntity(
    			"John Doe",
    			"123456", 
    			"test@pfm.com", // must match username in @WithMockUser annotation
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
    	
    	// Check if status is 200 OK
        MvcResult result = this.mockMvc
        	.perform( 
        		put("/profile")
        			.contentType(MediaType.APPLICATION_JSON)
        			.content( requestBody.toString()
        		)
        	)
        	.andExpect( status().isOk() )
        	.andReturn();
        
        // Test if the response string contains updated data
		String jsonResponseString = result.getResponse().getContentAsString();
		assertThat(jsonResponseString).contains("Jane Doe");
		assertThat(jsonResponseString).contains("FEMALE");
	 	
	 	// Test if the user's data has really been updated
		String testUserEmail = testUser.getEmail();
	 	assertTrue( userRepository.findByEmail(testUserEmail).get().getName().equals("Jane Doe") );
	 	assertTrue( userRepository.findByEmail(testUserEmail).get().getGender() == GenderType.FEMALE );
    	
    	// Delete test user so that he does not interfere with other tests
    	userRepository.delete(testUser);
    }
    
    @Test
    @Transactional
    @WithMockUser(roles = "USER", username="test@pfm.com")
    public void updateCurrentUserMethodShouldReturnBadRequestStatusCodeBecauseOfNuLLName() throws Exception {
    	// Insert test user with the logged in mock user's data
    	UserEntity testUser = userRepository.save( 
    		new UserEntity(
    			"John Doe",
    			"123456", 
    			"test@pfm.com", // must match username in @WithMockUser annotation
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
    	
    	// Check if status code is 400
        MvcResult result = this.mockMvc
        	.perform( 
        		put("/profile")
        			.contentType(MediaType.APPLICATION_JSON)
        			.content( requestBody.toString() )
        	)
        	.andExpect( status().isBadRequest() )
        	.andReturn();
        
        // Test if the exception message contains the correct information
    	String exceptionMessage = result.getResolvedException().getMessage();
    	assertThat(exceptionMessage).contains("User name must not be null!");
    	
    	// Test if the user's data has really been updated
    	String testUserEmail = testUser.getEmail();
    	assertTrue( userRepository.findByEmail(testUserEmail).get().getName().equals("John Doe") );
    	assertTrue( userRepository.findByEmail(testUserEmail).get().getGender() == GenderType.MALE );

    	// Delete test user so that he does not interfere with other tests
    	userRepository.delete(testUser);
    }
    
    /**
     * updatePasswordOfCurrentUser() tests
     * (PUT "/profile/password")
     */

    @Test
    @Transactional
    public void updatePasswordOfCurrentUserMethodShouldReturnUnauthorizedStatusCode() throws Exception {
    	JSONObject requestBody = new JSONObject();
    	requestBody.put("oldPassword", "123456");
    	requestBody.put("newPassword", "123456789");
    	requestBody.put("matchingNewPassword", "123456789");
    	
        this.mockMvc.perform( 
        	put("/profile/password")
        		.contentType(MediaType.APPLICATION_JSON)
        		.content( requestBody.toString() )
        ).andExpect( status().isUnauthorized() );
    }
    
    
    @Test(expected = NoSuchElementException.class)
    @Transactional
    @WithMockUser(roles = "USER")
    public void updatePasswordOfCurrentUserMethodShouldThrowExceptionBecauseOfMissingUser() throws Exception {
    	JSONObject requestBody = new JSONObject();
    	requestBody.put("oldPassword", "123456");
    	requestBody.put("newPassword", "123456789");
    	requestBody.put("matchingNewPassword", "123456789");
    	
        try {
        	this.mockMvc.perform( 
    			put("/profile/password")
        		.contentType(MediaType.APPLICATION_JSON)
        		.content( requestBody.toString() )
            ).andExpect( status().isUnauthorized() );
        }
        catch (NestedServletException e) {
        	throw (Exception) e.getCause();
        }
    }
    
    @Test
    @Transactional
    @WithMockUser(roles = "USER", username="test@pfm.com")
    public void updatePasswordOfCurrentUserMethodShouldReturnOKStatusCode() throws Exception {
    	// Insert test user with the logged in mock user's data
    	UserEntity testUser = userRepository.save( 
    		new UserEntity(
    			"John Doe",
    			/*
	    			NOTE: Passwords are stored encrypted in the database 
	    			and when attempting to update a user's password via the UserProfileService, 
	    			the service compares the passed old password to its encoded equivalent.
	    			That's why we need the test user's password to be encoded.
    			 */
    			passwordEncoder.encode("123456"), 
    			"test@pfm.com", // must match username in @WithMockUser annotation
    			GenderType.MALE,
    			FamilyStatusType.SINGLE,
    			24,
    			"Master's Degree"
    		)
    	);
    	
    	JSONObject requestBody = new JSONObject();
    	requestBody.put("oldPassword", "123456");
    	requestBody.put("newPassword", "123456789");
    	requestBody.put("matchingNewPassword", "123456789");
    	
    	// Test if the response code is 200 OK
        MvcResult result = this.mockMvc
        	.perform( 
	        	put("/profile/password")
	        		.contentType(MediaType.APPLICATION_JSON)
	        		.content( requestBody.toString() )
	        )
        	.andExpect( status().isOk() )
        	.andReturn();
        
        // Test if the response string contains the user's email
        // The response should be the user's details (without his password).
    	String resultString = result.getResponse().getContentAsString();
    	assertThat(resultString).contains("test@pfm.com");
    	
    	// Test if the user's info has been updated
    	Long testUserId = testUser.getId();
    	String testUserEncryptedPassword = userRepository.findById(testUserId).get().getPassword();
    	assertTrue( passwordEncoder.matches("123456789", testUserEncryptedPassword) );
    	
    	// Delete test user so that he does not interfere with other tests
    	userRepository.delete(testUser);
    }
    
    @Test
    @Transactional
    @WithMockUser(roles = "USER", username="test@pfm.com")
    public void updatePasswordOfCurrentUserMethodShouldReturnBadRequestStatusCodeBecauseOfMismatchingPassword() throws Exception {
    	// Insert test user with the logged in mock user's data
    	UserEntity testUser = userRepository.save( 
    		new UserEntity(
    			"John Doe",
    			/*
	    			NOTE: Passwords are stored encrypted in the database 
	    			and when attempting to update a user's password via the UserProfileService, 
	    			the service compares the passed old password to its encoded equivalent.
	    			That's why we need the test user's password to be encoded.
    			 */
    			passwordEncoder.encode("123456"), 
    			"test@pfm.com", // must match username in @WithMockUser annotation
    			GenderType.MALE,
    			FamilyStatusType.SINGLE,
    			24,
    			"Master's Degree"
    		)
    	);
    	
    	JSONObject requestBody = new JSONObject();
    	requestBody.put("oldPassword", "123456");
    	requestBody.put("newPassword", "123456789");
    	requestBody.put("matchingNewPassword", "12345678"); // mismatching password
    	
    	// Test if the response code is 400 BAD REQUEST
        MvcResult result = this.mockMvc
        	.perform( 
	        	put("/profile/password")
	        		.contentType(MediaType.APPLICATION_JSON)
	        		.content( requestBody.toString() )
	        )
        	.andExpect( status().isBadRequest() )
        	.andReturn();
        
        // Test if the exception message contains the correct information
    	String exceptionMessage = result.getResolvedException().getMessage();
    	assertThat(exceptionMessage).contains("New passwords don't match!");
    	
    	// Test if the user's info has been updated
    	Long testUserId = testUser.getId();
    	String testUserEncryptedPassword = userRepository.findById(testUserId).get().getPassword();
    	assertTrue( passwordEncoder.matches("123456", testUserEncryptedPassword) );
    	
    	// Delete test user so that he does not interfere with other tests
    	userRepository.delete(testUser);
    }
    
    @Test
    @Transactional
    @WithMockUser(roles = "USER", username="test@pfm.com")
    public void updatePasswordOfCurrentUserMethodShouldReturnBadRequestStatusCodeBecauseOfInvalidNewPassword() throws Exception {
    	// Insert test user with the logged in mock user's data
    	UserEntity testUser = userRepository.save( 
    		new UserEntity(
    			"John Doe",
    			/*
	    			NOTE: Passwords are stored encrypted in the database 
	    			and when attempting to update a user's password via the UserProfileService, 
	    			the service compares the passed old password to its encoded equivalent.
	    			That's why we need the test user's password to be encoded.
    			 */
    			passwordEncoder.encode("123456"), 
    			"test@pfm.com", // must match username in @WithMockUser annotation
    			GenderType.MALE,
    			FamilyStatusType.SINGLE,
    			24,
    			"Master's Degree"
    		)
    	);
    	
    	JSONObject requestBody = new JSONObject();
    	requestBody.put("oldPassword", "123456");
    	requestBody.put("newPassword", "123"); // invalid password
    	requestBody.put("matchingNewPassword", "123"); // invalid password
    	
    	// Test if the response code is 400 BAD REQUEST
        MvcResult result = this.mockMvc
        	.perform( 
	        	put("/profile/password")
	        		.contentType(MediaType.APPLICATION_JSON)
	        		.content( requestBody.toString() )
	        )
        	.andExpect( status().isBadRequest() )
        	.andReturn();
        
        // Test if the exception message contains the correct information
    	String exceptionMessage = result.getResolvedException().getMessage();
    	assertThat(exceptionMessage).contains("New user password must be at least 6 characters!");
    	
    	// Test if the user's info has been updated
    	Long testUserId = testUser.getId();
    	String testUserEncryptedPassword = userRepository.findById(testUserId).get().getPassword();
    	assertTrue( passwordEncoder.matches("123456", testUserEncryptedPassword) );
    	
    	// Delete test user so that he does not interfere with other tests
    	userRepository.delete(testUser);
    }
    
    /**
	* deleteCurrentUser() tests
	* (DELETE "/profile")
	*/
    
    @Test(expected = NoSuchElementException.class)
    @Transactional
    @WithMockUser(roles = "USER")
    public void deleteCurrentUserMethodShouldThrowExceptionBecauseOfMissingUser() throws Exception {
        try {
        	this.mockMvc.perform( delete("/profile") );
        }
        catch (NestedServletException e) {
        	throw (Exception) e.getCause();
        }
    }
    
    @Test
    @Transactional
    @WithMockUser(roles = "USER", username="test@pfm.com")
    public void deleteUserMethodShouldReturnOKStatusCode() throws Exception {
    	// Insert test user with the logged in mock user's data
    	UserEntity testUser = userRepository.save( 
    		new UserEntity(
    			"John Doe",
    			"123456", 
    			"test@pfm.com", // must match username in @WithMockUser annotation
    			GenderType.MALE,
    			FamilyStatusType.SINGLE,
    			24,
    			"Master's Degree"
    		)
    	);
    	
    	// Test if the response code is 200 OK
        MvcResult result = this.mockMvc
        	.perform( delete("/profile") )
        	.andExpect( status().isOk() )
        	.andReturn();
        
        // Test if the response string contains the user's email
        // The response should be the user's details (without his password).
    	String resultString = result.getResponse().getContentAsString();
    	assertThat(resultString).contains("test@pfm.com");
    	
    	// Test if the user has been deleted and perform delete operation just in case
    	Long testUserId = testUser.getId();
    	assertTrue( !userRepository.existsById(testUserId) );
    	userRepository.delete(testUser);
    }
    
    /**
     * getCurrentUserSettings() tests
     * (GET "/profile/settings")
     */
    
    @Test(expected = NoSuchElementException.class)
    @Transactional
    @WithMockUser(roles = "USER")
    public void getCurrentUserSettingsMethodShouldThrowExceptionBecauseOfMissingUser() throws Exception {
    	try {
    		this.mockMvc.perform( get("/profile/settings") );
    	}
        catch (NestedServletException e) {
        	throw (Exception) e.getCause();
        }
    }
    
    @Test
    @Transactional
    @WithMockUser(roles = "USER", username="test@pfm.com")
    public void getCurrentUserSettingsMethodShouldReturnOKStatusCode() throws Exception {
    	// Insert test user with the logged in mock user's data
    	UserEntity testUser = userRepository.save( 
    		new UserEntity(
    			"John Doe",
    			"123456", 
    			"test@pfm.com", // must match username in @WithMockUser annotation
    			GenderType.MALE,
    			FamilyStatusType.SINGLE,
    			24,
    			"Master's Degree"
    		)
    	);
    	
    	// Insert test user setting for this user
    	UserSettingEntity testUserSetting = userSettingRepository.save(
    		new UserSettingEntity(
    			UserSettingKey.HAS_LOGGED_IN_BEFORE,
    			"false",
    			testUser
    		)
    	);
    	List<UserSettingEntity> testUserSettings = new ArrayList<>();
    	testUserSettings.add(testUserSetting);
    	testUser.setSettings(testUserSettings);
    	testUser = userRepository.save(testUser);
    	
    	// Test if the response code is 200 OK
        MvcResult result = this.mockMvc
        	.perform( get("/profile/settings") )
        	.andExpect( status().isOk() )
        	.andReturn();
        
        // Test if the response string contains the inserted setting's key/value
    	String resultString = result.getResponse().getContentAsString();
    	JSONArray jsonArray = new JSONArray(resultString);
    	JSONObject jsonObject = jsonArray.getJSONObject(0);
    	assertEquals( "HAS_LOGGED_IN_BEFORE", jsonObject.get("key") );
    	assertEquals( "false", jsonObject.get("value") );
    	
    	// Delete test user so that he does not interfere with other tests (user setting is removed by cascade)
    	userRepository.delete(testUser);
    }
    
    /**
     * getUserSettingByKey() tests
     * (GET "/profile/setting-by-key?key=")
     */
    
    @Test
    @Transactional
    @WithMockUser(roles = "USER", username="test@pfm.com")
    public void getCurrentUserSettingByKeyMethodShouldReturnNotFoundStatusCode() throws Exception {
    	// Insert test user with the logged in mock user's data
    	UserEntity testUser = userRepository.save( 
    		new UserEntity(
    			"John Doe",
    			"123456", 
    			"test@pfm.com", // must match username in @WithMockUser annotation
    			GenderType.MALE,
    			FamilyStatusType.SINGLE,
    			24,
    			"Master's Degree"
    		)
    	);
    	
    	// Test if the response code is 404
       this.mockMvc
        	.perform( get("/profile/setting-by-key?key=HAS_LOGGED_IN_BEFORE") )
        	.andExpect( status().isNotFound() );
    	
    	// Delete test user so that he does not interfere with other tests (user setting is removed by cascade)
    	userRepository.delete(testUser);
    }
    
    @Test
    @Transactional
    @WithMockUser(roles = "USER", username="test@pfm.com")
    public void getCurrentUserSettingByKeyMethodShouldReturnOKStatusCode() throws Exception {
    	// Insert test user with the logged in mock user's data
    	UserEntity testUser = userRepository.save( 
    		new UserEntity(
    			"John Doe",
    			"123456", 
    			"test@pfm.com", // must match username in @WithMockUser annotation
    			GenderType.MALE,
    			FamilyStatusType.SINGLE,
    			24,
    			"Master's Degree"
    		)
    	);
    	
    	// Insert test user setting for this user
    	UserSettingEntity testUserSetting = userSettingRepository.save(
    		new UserSettingEntity(
    			UserSettingKey.HAS_LOGGED_IN_BEFORE,
    			"false",
    			testUser
    		)
    	);
    	List<UserSettingEntity> testUserSettings = new ArrayList<>();
    	testUserSettings.add(testUserSetting);
    	testUser.setSettings(testUserSettings);
    	testUser = userRepository.save(testUser);
    	
    	// Test if the response code is 200 OK
        MvcResult result = this.mockMvc
        	.perform( get("/profile/setting-by-key?key=HAS_LOGGED_IN_BEFORE") )
        	.andExpect( status().isOk() )
        	.andReturn();
        
        // Test if the response string contains the inserted setting's key/value
    	String resultString = result.getResponse().getContentAsString();
    	JSONObject jsonObject = new JSONObject(resultString);
    	assertEquals( "HAS_LOGGED_IN_BEFORE", jsonObject.get("key") );
    	assertEquals( "false", jsonObject.get("value") );
    	
    	// Delete test user so that he does not interfere with other tests (user setting is removed by cascade)
    	userRepository.delete(testUser);
    }
    
    /**
     * setUserSetting() tests
     * (POST "/profile/settings")
     */
    
    @Test(expected = NoSuchElementException.class)
    @Transactional
    @WithMockUser(roles = "USER")
    public void setUserSettingMethodShouldThrowExceptionBecauseOfMissingUser() throws Exception {
    	JSONObject requestBody = new JSONObject();
    	requestBody.put("key", "HAS_LOGGED_IN_BEFORE");
    	requestBody.put("value", "false");
    	
    	try {
    		this.mockMvc
    			.perform( 
    				post("/profile/settings")
    				.contentType(MediaType.APPLICATION_JSON)
    				.content( requestBody.toString() )
    			);
    	}
        catch (NestedServletException e) {
        	throw (Exception) e.getCause();
        }
    }
    
    @Test
    @Transactional
    @WithMockUser(roles = "USER", username = "test@pfm.com")
    public void setUserSettingMethodShouldReturnOkStatusCode() throws Exception {
    	// Insert test user with the logged in mock user's data
    	UserEntity testUser = userRepository.save( 
    		new UserEntity(
    			"John Doe",
    			"123456", 
    			"test@pfm.com", // must match username in @WithMockUser annotation
    			GenderType.MALE,
    			FamilyStatusType.SINGLE,
    			24,
    			"Master's Degree"
    		)
    	);
    	
    	JSONObject requestBody = new JSONObject();
    	requestBody.put("key", "HAS_LOGGED_IN_BEFORE");
    	requestBody.put("value", "false");
    	
    	// Test if response code is 200 OK
    	this.mockMvc
			.perform( 
				post("/profile/settings")
					.contentType(MediaType.APPLICATION_JSON)
					.content( requestBody.toString() )
			)
			.andExpect( status().isOk() );
    	
    	// Test to see if the user setting was really created
    	assertTrue( userSettingRepository.existsByKeyAndUser(UserSettingKey.HAS_LOGGED_IN_BEFORE, testUser) );

    	// Delete test user so that he does not interfere with other tests (user setting is removed by cascade)
    	userRepository.delete(testUser);
    }
    
    @Test
    @Transactional
    @WithMockUser(roles = "USER", username = "test@pfm.com")
    public void setUserSettingMethodShouldReturnBadRequestStatusCode() throws Exception {
    	// Insert test user with the logged in mock user's data
    	UserEntity testUser = userRepository.save( 
    		new UserEntity(
    			"John Doe",
    			"123456", 
    			"test@pfm.com", // must match username in @WithMockUser annotation
    			GenderType.MALE,
    			FamilyStatusType.SINGLE,
    			24,
    			"Master's Degree"
    		)
    	);
    	
    	JSONObject requestBody = new JSONObject();
    	requestBody.put("key", null); // invalid setting key
    	requestBody.put("value", "false");
    	
    	// Test if response code is 400 OK
    	MvcResult result = this.mockMvc
			.perform( 
				post("/profile/settings")
					.contentType(MediaType.APPLICATION_JSON)
					.content( requestBody.toString() )
			)
			.andExpect( status().isBadRequest() )
			.andReturn();
    	
    	// Test if the exception message contains the correct information
    	String exceptionMessage = result.getResolvedException().getMessage();
    	assertThat(exceptionMessage).contains("User setting key must not be null!");

    	// Delete test user so that he does not interfere with other tests (user setting is removed by cascade)
    	userRepository.delete(testUser);
    }
    
}