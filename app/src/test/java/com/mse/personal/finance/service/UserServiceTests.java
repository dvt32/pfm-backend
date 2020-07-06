package com.mse.personal.finance.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.mse.personal.finance.db.entity.UserEntity;
import com.mse.personal.finance.db.repository.UserRepository;
import com.mse.personal.finance.model.FamilyStatusType;
import com.mse.personal.finance.model.GenderType;
import com.mse.personal.finance.model.User;
import com.mse.personal.finance.model.request.UserCreateRequest;
import com.mse.personal.finance.model.request.UserUpdateRequest;
import com.mse.personal.finance.rest.exception.UserNotFoundException;

/**
 * This class implements unit tests for the UserService class.
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
public class UserServiceTests {
	
	private static final String TEST_USER_EMAIL = "test@pfm.com";
	private static final String TEST_USER_PASSWORD = "123456";
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	/**
	 * Create and return a test user create request
	 */
	public UserCreateRequest getTestUserCreateRequest() {
		UserCreateRequest testUserCreateRequest = new UserCreateRequest();
		
		testUserCreateRequest.setName("John Doe");
		testUserCreateRequest.setEmail("test@pfm.com");
		testUserCreateRequest.setPassword("123456");
		testUserCreateRequest.setGender( GenderType.MALE );
		testUserCreateRequest.setFamilyStatus( FamilyStatusType.SINGLE );
		testUserCreateRequest.setAge(24);
		testUserCreateRequest.setEducation("Master's Degree");
    	
		return testUserCreateRequest;
	}
	
	/**
	 * Insert test user and return his data.
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
	 * Create and return a test user update request
	 */
	public UserUpdateRequest getTestUserUpdateRequest() {
		UserUpdateRequest testUserUpdateRequest = new UserUpdateRequest();
		
		testUserUpdateRequest.setName("Jane Doe");
		testUserUpdateRequest.setGender( GenderType.FEMALE );
		testUserUpdateRequest.setFamilyStatus( FamilyStatusType.SINGLE );
		testUserUpdateRequest.setAge(24);
		testUserUpdateRequest.setEducation("Master's Degree");
    	
		return testUserUpdateRequest;
	}
	
	/**
	 * getUserById() tests
	 */
	
	@Test
	public void getUserByIdMethodShouldReturnInsertedUser() 
		throws UserNotFoundException 
	{
		UserEntity testUser = insertTestUser();
    	Long testUserId = testUser.getId();
    	User user = userService.getUserById(testUserId);
    	assertTrue( user.getEmail().equals(TEST_USER_EMAIL) );
		assertTrue( user.getName().equals("John Doe") );
	}
	
	@Test(expected = UserNotFoundException.class)
	public void getUserByIdMethodShouldThrowUserNotFoundException() 
		throws UserNotFoundException 
	{
		userService.getUserById(1L);
	}
	
	/**
	 * getUserByEmail() tests
	 */
	
	@Test
	public void getUserByEmailMethodShouldReturnInsertedUser() 
		throws UserNotFoundException 
	{
		UserEntity testUser = insertTestUser();
    	String testUserEmail = testUser.getEmail();
    	User user = userService.getUserByEmail(testUserEmail);
    	assertTrue( user.getEmail().equals(testUserEmail) );
		assertTrue( user.getName().equals("John Doe") );
	}
	
	@Test(expected = UserNotFoundException.class)
	public void getUserByEmailMethodShouldThrowUserNotFoundException() 
		throws UserNotFoundException 
	{
		userService.getUserByEmail(TEST_USER_EMAIL);
	}
	
	/**
	 * createNewUser() tests
	 */
	
	@Test
	public void createNewUserMethodShouldCreateNewUserAndPasswordShouldBeEncoded() {
		UserCreateRequest testUserCreateRequest = getTestUserCreateRequest();
		String rawPassword = testUserCreateRequest.getPassword();
		
		// Test if user is created
		User createdUserDTO = userService.createNewUser(testUserCreateRequest);
		Long createdUserId = createdUserDTO.getId();
		assertTrue( userRepository.existsById(createdUserId) );
		
		// Test if password is stored in encoded format
		String entityPassword = userRepository.findById(createdUserId).get().getPassword();
		assertFalse( entityPassword.equals(rawPassword) );
		assertTrue( passwordEncoder.matches( rawPassword, entityPassword ) );
	}
	
	@Test(expected = DataIntegrityViolationException.class)
	public void createNewUserMethodShouldThrowExceptionBecauseOfDuplicateEmail() throws Exception {
		UserCreateRequest userCreateRequest = getTestUserCreateRequest();
		userService.createNewUser(userCreateRequest);
		UserCreateRequest anotherUserCreateRequest = getTestUserCreateRequest(); // contains duplicate email
		userService.createNewUser(anotherUserCreateRequest);
	}
	
	/**
	 * updateUserById() tests
	 */
	
	@Test
	public void updateUserByIdMethodShouldUpdateExistingUserData() 
		throws UserNotFoundException 
	{
    	UserEntity testUser = insertTestUser();
    	
    	Long testUserId = testUser.getId();
		UserUpdateRequest testUserUpdateRequest = getTestUserUpdateRequest();
		userService.updateUserById(testUserId, testUserUpdateRequest);
		
		// Test if the user's data has been updated
		UserEntity retrievedUser = userRepository.findById(testUserId).get();
		assertTrue( retrievedUser.getName().equals("Jane Doe") );
		assertTrue( retrievedUser.getGender() == GenderType.FEMALE);
	}
	
	@Test(expected = UserNotFoundException.class)
	public void updateUserByIdMethodShouldThrowUserNotFoundException() 
		throws UserNotFoundException 
	{
		UserUpdateRequest testUserUpdateRequest = getTestUserUpdateRequest();
		userService.updateUserById(1L, testUserUpdateRequest);
	}
	
	/**
	 * updateUserByEmail() tests
	 */
	
	@Test
	public void updateUserByEmailMethodShouldUpdateExistingUserData() 
		throws UserNotFoundException 
	{
		UserEntity testUser = insertTestUser();
    	
    	String testUserEmail = testUser.getEmail();
		UserUpdateRequest testUserUpdateRequest = getTestUserUpdateRequest();
		userService.updateUserByEmail(testUserEmail, testUserUpdateRequest);
		
		// Test if the user's data has been updated
		UserEntity retrievedUser = userRepository.findByEmail(testUserEmail).get();
		assertTrue( retrievedUser.getName().equals("Jane Doe") );
		assertTrue( retrievedUser.getGender() == GenderType.FEMALE);
	}
	
	@Test(expected = UserNotFoundException.class)
	public void updateUserByEmailMethodShouldThrowUserNotFoundException() 
		throws UserNotFoundException 
	{
		UserUpdateRequest testUserUpdateRequest = getTestUserUpdateRequest();
		userService.updateUserByEmail(TEST_USER_EMAIL, testUserUpdateRequest);
	}
	
	/**
	 * deleteUserById() tests
	 */
	
	@Test
	public void deleteUserByIdMethodShouldDeleteExistingUser() 
		throws UserNotFoundException 
	{
		UserEntity testUser = insertTestUser();
    	
    	Long testUserId = testUser.getId();
		userService.deleteUserById(testUserId);
		
		// Test if the user has been deleted
		assertTrue( !userRepository.existsById(testUserId) );
	}
	
	@Test(expected = UserNotFoundException.class)
	public void deleteUserByIdMethodShouldThrowUserNotFoundException() 
		throws UserNotFoundException 
	{
		userService.deleteUserById(1L);
	}
	
	/**
	 * deleteUserByEmail() tests
	 */
	
	@Test
	public void deleteUserByEmailMethodShouldDeleteExistingUser() 
		throws UserNotFoundException 
	{
		UserEntity testUser = insertTestUser();
    	
    	String testUserEmail = testUser.getEmail();
		userService.deleteUserByEmail(testUserEmail);
		
		// Test if the user has been deleted
		assertTrue( !userRepository.existsByEmail(testUserEmail) );
	}
	
	@Test(expected = UserNotFoundException.class)
	public void deleteUserByEmailMethodShouldThrowUserNotFoundException() 
		throws UserNotFoundException 
	{
		userService.deleteUserByEmail(TEST_USER_EMAIL);
	}
	
	/**
	 * isCorrectPasswordForUser() tests
	 */
	
	@Test
	public void isCorrectPasswordForUserMethodShouldReturnTrue() {
		UserEntity testUser = insertTestUser();
		assertTrue( userService.isCorrectPasswordForUser(testUser, TEST_USER_PASSWORD) );
	}
	
	@Test
	public void isCorrectPasswordForUserMethodShouldReturnFalse() {
		UserEntity testUser = insertTestUser();
		assertFalse( userService.isCorrectPasswordForUser(testUser, "some incorrect password") );
	}
	
	@Test(expected = NullPointerException.class)
	public void isCorrectPasswordForUserMethodShouldThrowNullPointerException() {
		userService.isCorrectPasswordForUser(null, TEST_USER_PASSWORD);
	}
	
}