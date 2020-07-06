package com.mse.personal.finance.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.mse.personal.finance.db.entity.UserEntity;
import com.mse.personal.finance.db.repository.UserRepository;
import com.mse.personal.finance.model.FamilyStatusType;
import com.mse.personal.finance.model.GenderType;
import com.mse.personal.finance.model.UserAuthenticationDetails;
import com.mse.personal.finance.model.UserAuthenticationToken;
import com.mse.personal.finance.model.request.UserLoginRequest;
import com.mse.personal.finance.security.jwt.JwtSessionManager;
import com.mse.personal.finance.security.jwt.JwtTokenProvider;

/**
 * This class implements unit tests for the AuthenticationService class.
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
public class AuthenticationServiceTests {
	
	private static final String TEST_USER_EMAIL = "test@pfm.com";
	private static final String TEST_USER_PASSWORD = "123456";
	
	@Autowired
	private AuthenticationService authenticationService;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private JwtTokenProvider tokenProvider;
	
	@Autowired
	private JwtSessionManager jwtSessionManager;
	
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
	 * Create and return a test user login request
	 */
	public UserLoginRequest getTestUserLoginRequest() {
		UserLoginRequest testUserLoginRequest = new UserLoginRequest();
		
		testUserLoginRequest.setUsername(TEST_USER_EMAIL);
		testUserLoginRequest.setPassword(TEST_USER_PASSWORD);
		
		return testUserLoginRequest;
	}

	/**
	 * getUserAuthenticationTokenForUser() tests
	 */
	
	@Test
	public void getUserAuthenticationTokenForUserMethodShouldReturnJwtTokenForUser() {
		insertTestUser();
		UserLoginRequest testUserLoginRequest = getTestUserLoginRequest();
		
		UserAuthenticationToken token = authenticationService.getUserAuthenticationTokenForUser(testUserLoginRequest);
		
		assertTrue( token.getTokenString() != null );
		assertFalse( token.getTokenString().isEmpty() );
	}
	
	@Test(expected = BadCredentialsException.class)
	public void getUserAuthenticationTokenForUserMethodShouldThrowExceptionBecauseOfMissingUser() {
		UserLoginRequest testUserLoginRequest = getTestUserLoginRequest();
		authenticationService.getUserAuthenticationTokenForUser(testUserLoginRequest);
	}
	
	/**
	 * removeUserAuthenticationTokenFromRequest() tests
	 */
	
	@Test
	public void removeUserAuthenticationTokenFromRequestMethodShouldRemoveJwt() {
		UserEntity testUser = insertTestUser();
		String tokenString = insertJwtForUser(testUser);
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", "Bearer " + tokenString);
		
		authenticationService.removeUserAuthenticationTokenFromRequest(request);
		
		assertTrue( jwtSessionManager.isExpired(tokenString) );
	}
	
	@Test
	public void removeUserAuthenticationTokenFromRequestMethodShouldNotRemoveJwtBecauseOfInvalidRequest() {
		UserEntity testUser = insertTestUser();
		String tokenString = insertJwtForUser(testUser);
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", "Bearer invalid-token-string");
		
		authenticationService.removeUserAuthenticationTokenFromRequest(request);
		
		assertFalse( jwtSessionManager.isExpired(tokenString) );
	}
	
}