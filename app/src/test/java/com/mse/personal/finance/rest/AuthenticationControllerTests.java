package com.mse.personal.finance.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.mse.personal.finance.db.entity.UserEntity;
import com.mse.personal.finance.db.repository.UserRepository;
import com.mse.personal.finance.model.FamilyStatusType;
import com.mse.personal.finance.model.GenderType;
import com.mse.personal.finance.model.UserAuthenticationDetails;
import com.mse.personal.finance.security.jwt.JwtSessionManager;
import com.mse.personal.finance.security.jwt.JwtTokenProvider;
import com.mse.personal.finance.service.LoginLimitService;

/**
 * This class implements integration tests for the AuthenticationController class.
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
public class AuthenticationControllerTests {
	
	private static final String TEST_USER_EMAIL = "test@pfm.com";
	private static final String TEST_USER_PASSWORD = "123456";
	
	@Autowired
    private AuthenticationController authenticationController;
	
	@Autowired
	private WebApplicationContext context;
	
	private MockMvc mockMvc;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private JwtTokenProvider tokenProvider;
	
	@Autowired
	private JwtSessionManager jwtSessionManager;
	
	@Autowired
	private LoginLimitService loginLimitService;
	
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
	 * Insert tests user and returns his data.
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
	 * Inserts JWT for passed user and returns generated token string.
	 */
	public String insertJwtForUser(UserEntity user) {
		UserAuthenticationDetails authDetails = new UserAuthenticationDetails(
    		user.getEmail(), 
    		user.getPassword(),
    		user.getName()
    	);
    	
    	String tokenString = tokenProvider.generateTokenStringFromDetails(authDetails);
    	
    	jwtSessionManager.updateSession(tokenString);
    	
    	return tokenString;
	}
	
	/**
     * Smoke test (tests if the controller loads properly)
     */
	
    @Test
    public void controllerShouldNotBeNull() {
        assertThat(authenticationController).isNotNull();
    }
    
    /**
     * getUserAuthenticationTokenForUser() tests
     * (POST "/auth/login")
     */
    
    @Test
    public void getUserAuthenticationTokenForUserMethodShouldReturnUnauthorizedStatusCodeBecauseOfMissingUser() throws Exception {
    	JSONObject requestBody = new JSONObject();
    	requestBody.put("username", TEST_USER_EMAIL);
    	requestBody.put("password", TEST_USER_PASSWORD);
    	
    	this.mockMvc
    		.perform( 
				post("/auth/login").contentType(MediaType.APPLICATION_JSON).content( requestBody.toString() )
    		)
    		.andExpect( status().isUnauthorized() );
    }
    
    @Test
    public void getUserAuthenticationTokenForUserMethodShouldReturnOKStatusCode() throws Exception {
    	insertTestUser();
    	
    	JSONObject requestBody = new JSONObject();
    	requestBody.put("username", TEST_USER_EMAIL);
    	requestBody.put("password", TEST_USER_PASSWORD);
    	
    	// Test if the response status is 200 OK
    	MvcResult result = this.mockMvc
    		.perform( 
    			post("/auth/login").contentType(MediaType.APPLICATION_JSON).content( requestBody.toString() )
    		)
    		.andExpect( status().isOk() )
    		.andReturn();
    	
    	// Test if token string is returned
    	String responseString = result.getResponse().getContentAsString();
    	JSONObject responseJsonObject = new JSONObject(responseString);
    	assertTrue( responseJsonObject.has("tokenString") );
    }
    
    /**
     * removeUserAuthenticationTokenFromRequest() tests
     * (GET "/auth/logout")
     */
    
    @Test
    public void removeUserAuthenticationTokenFromRequestMethodShouldReturnUnauthorizedStatusCodeBecauseOfNoTokenString() throws Exception {
    	this.mockMvc
			.perform( 
				get("/auth/logout")
			)
			.andExpect( status().isUnauthorized() );
    }
    
    @Test
    public void removeUserAuthenticationTokenFromRequestMethodShouldReturnUnauthorizedStatusCodeBecauseOfInvalidTokenString() throws Exception {
    	this.mockMvc
			.perform( 
				get("/auth/logout").header("Authorization", "Bearer invalidTokenString")
			)
			.andExpect( status().isUnauthorized() );
    }
    
    @Test
    public void removeUserAuthenticationTokenFromRequestMethodShouldReturnOKStatusCode() throws Exception {
    	UserEntity testUser = insertTestUser();
    	String tokenString = insertJwtForUser(testUser);
    	
    	this.mockMvc
			.perform( 
				get("/auth/logout").header("Authorization", "Bearer " + tokenString)
			)
			.andExpect( status().isOk() );
    }
    
    /**
	 * Anti-bruteforce tests
	 */
	
    @Test
    public void loginAttemptsShouldTriggerBruteforceProtection() throws Exception {
    	JSONObject requestBody = new JSONObject();
    	requestBody.put("username", TEST_USER_EMAIL);
    	requestBody.put("password", TEST_USER_PASSWORD);
    	
    	int maxNumberOfConsecutiveFailedLoginAttempts = loginLimitService.getMaxNumberOfConsecutiveFailedLoginAttempts();
    	MvcResult result = null;
    	for (int i = 0; i < maxNumberOfConsecutiveFailedLoginAttempts; ++i) {
    		result = this.mockMvc
				.perform( 
					post("/auth/login").contentType(MediaType.APPLICATION_JSON).content( requestBody.toString() )
				)
				.andExpect( status().isUnauthorized() )
				.andReturn();
    	}
    	
    	// Test if the exception message contains the correct information
    	String exceptionMessage = result.getResolvedException().getMessage();
    	assertThat(exceptionMessage).contains("IP blocked");
    	
    	// Logging in even with valid that should not be allowed
    	insertTestUser();
    	this.mockMvc
			.perform( 
				post("/auth/login").contentType(MediaType.APPLICATION_JSON).content( requestBody.toString() )
			)
			.andExpect( status().isUnauthorized() );
    	
    	// Remove blocked IPs so there is no interference in other tests
    	loginLimitService.unblockAllIps();
    }
    
    @Test
    public void loginAttemptsShouldNotTriggerBruteforceProtection() throws Exception {
    	JSONObject requestBody = new JSONObject();
    	requestBody.put("username", TEST_USER_EMAIL);
    	requestBody.put("password", TEST_USER_PASSWORD);
    	
    	int maxNumberOfConsecutiveFailedLoginAttempts = loginLimitService.getMaxNumberOfConsecutiveFailedLoginAttempts();
    	MvcResult result = null;
    	for (int i = 0; i < maxNumberOfConsecutiveFailedLoginAttempts-1; ++i) {
    		result = this.mockMvc
				.perform( 
					post("/auth/login").contentType(MediaType.APPLICATION_JSON).content( requestBody.toString() )
				)
				.andExpect( status().isUnauthorized() )
				.andReturn();
    	}
    	
    	/*
    	 *  Successful login should reset consecutive failed login attempt counter for IP address
    	 */
    	
    	UserEntity testUser = insertTestUser();
    	result = this.mockMvc
			.perform( 
				post("/auth/login").contentType(MediaType.APPLICATION_JSON).content( requestBody.toString() )
			)
			.andExpect( status().isOk() )
			.andReturn();
    	
    	userRepository.delete(testUser);
    	for (int i = 0; i < maxNumberOfConsecutiveFailedLoginAttempts-1; ++i) {
    		result = this.mockMvc
				.perform( 
					post("/auth/login").contentType(MediaType.APPLICATION_JSON).content( requestBody.toString() )
				)
				.andExpect( status().isUnauthorized() )
				.andReturn();
    		assertTrue( result.getResolvedException() == null );
    	}
    	
    	// Remove blocked IPs so there is no interference in other tests
    	loginLimitService.unblockAllIps();
    }
    
}