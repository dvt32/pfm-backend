package com.mse.personal.finance.service;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.validation.ConstraintViolationException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.mse.personal.finance.db.entity.UserEntity;
import com.mse.personal.finance.db.entity.UserSettingEntity;
import com.mse.personal.finance.db.repository.UserRepository;
import com.mse.personal.finance.db.repository.UserSettingRepository;
import com.mse.personal.finance.model.FamilyStatusType;
import com.mse.personal.finance.model.GenderType;
import com.mse.personal.finance.model.User;
import com.mse.personal.finance.model.UserSetting;
import com.mse.personal.finance.model.UserSettingKey;
import com.mse.personal.finance.model.request.UserPasswordUpdateRequest;
import com.mse.personal.finance.model.request.UserSettingRequest;
import com.mse.personal.finance.model.request.UserUpdateRequest;
import com.mse.personal.finance.rest.exception.InvalidDataException;
import com.mse.personal.finance.rest.exception.UserSettingNotFoundException;

/**
 * This class implements unit tests for the UserProfileService class.
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
public class UserProfileServiceTests {
	
	private static final String TEST_USER_EMAIL = "test@pfm.com";
	private static final String TEST_USER_PASSWORD = "123456";
	private static final UserSettingKey TEST_USER_SETTING_KEY = UserSettingKey.HAS_LOGGED_IN_BEFORE;
	
	@Autowired
	private UserProfileService userProfileService;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private UserSettingRepository userSettingRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
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
	 * Create and return a test user password update request
	 */
	public UserPasswordUpdateRequest getTestUserPasswordUpdateRequest() {
		UserPasswordUpdateRequest testUserPasswordUpdateRequest = new UserPasswordUpdateRequest();
		
		testUserPasswordUpdateRequest.setOldPassword(TEST_USER_PASSWORD);
		testUserPasswordUpdateRequest.setNewPassword("qwerty");
		testUserPasswordUpdateRequest.setMatchingNewPassword("qwerty");
    	
		return testUserPasswordUpdateRequest;
	}
	
	/**
	 * Inserts a test user setting for the passed user
	 * and sets that user's settings to a list containing only the inserted setting.
	 */
	public void insertAndSetTestUserSettingForUser(UserEntity user) {
		UserSettingEntity testUserSetting = userSettingRepository.save(
    		new UserSettingEntity(
    			TEST_USER_SETTING_KEY,
    			"false",
    			user
    		)
    	);
		List<UserSettingEntity> testUserSettings = new ArrayList<>();
    	testUserSettings.add(testUserSetting);
    	user.setSettings(testUserSettings);
    	user = userRepository.save(user);
	}
	
	/**
	 * Creates and returns a test user setting request
	 */
	public UserSettingRequest getTestUserSettingRequest() {
		UserSettingRequest testUserSettingRequest = new UserSettingRequest();
		
		testUserSettingRequest.setKey(TEST_USER_SETTING_KEY);
		testUserSettingRequest.setValue("true");
		
		return testUserSettingRequest;
	}

	/**
	 * getCurrentUser() tests
	 */
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getCurrentUserMethodShouldReturnUserWithTestUserEmail() {
		insertTestUser();
		User user = userProfileService.getCurrentUser();
		assertTrue( user.getEmail().equals(TEST_USER_EMAIL) );
	}
	
	@Test(expected = NoSuchElementException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void getCurrentUserMethodShouldThrowExceptionBecauseOfMissingUser() {
		userProfileService.getCurrentUser();
	}
	
	/**
	 * updateCurrentUser() tests
	 */
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void updateUserByIdMethodShouldUpdateCurrentUserData() {
    	UserEntity testUser = insertTestUser();
    	
    	Long testUserId = testUser.getId();
		UserUpdateRequest testUserUpdateRequest = getTestUserUpdateRequest();
		userProfileService.updateCurrentUser(testUserUpdateRequest);
		
		// Test if the user's data has been updated
		UserEntity retrievedUser = userRepository.findById(testUserId).get();
		assertTrue( retrievedUser.getName().equals("Jane Doe") );
		assertTrue( retrievedUser.getGender() == GenderType.FEMALE);
	}
	
	@Test(expected = NoSuchElementException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void updateUserByIdMethodShouldThrowExceptionBecauseOfMissingUser(){
		UserUpdateRequest testUserUpdateRequest = getTestUserUpdateRequest();
		userProfileService.updateCurrentUser(testUserUpdateRequest);
	}
	
	/**
	 * updatePasswordOfCurrentUser() tests
	 */
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void updatePasswordOfCurrentUserMethodShouldUpdateCurrentUserPassword() {
		UserEntity testUser = insertTestUser();
    	UserPasswordUpdateRequest testUserPasswordUpdateRequest = getTestUserPasswordUpdateRequest();
    	
    	userProfileService.updatePasswordOfCurrentUser(testUserPasswordUpdateRequest);
    	
    	Long testUserId = testUser.getId();
    	UserEntity retrievedUser = userRepository.findById(testUserId).get();
    	
    	assertTrue( 
    		passwordEncoder.matches(
    			testUserPasswordUpdateRequest.getNewPassword(), 
    			retrievedUser.getPassword()
    		) 
    	);
	}
	
	@Test(expected = InvalidDataException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void updatePasswordOfCurrentUserMethodShouldThrowInvalidDataExceptionBecauseOfInvalidOldPassword() {
		insertTestUser();
    	UserPasswordUpdateRequest testUserPasswordUpdateRequest = getTestUserPasswordUpdateRequest();
    	testUserPasswordUpdateRequest.setOldPassword("invalid-old-password");
    	userProfileService.updatePasswordOfCurrentUser(testUserPasswordUpdateRequest);
	}
	
	@Test(expected = InvalidDataException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void updatePasswordOfCurrentUserMethodShouldThrowInvalidDataExceptionBecauseOfMismatchingPassword() {
		insertTestUser();
    	UserPasswordUpdateRequest testUserPasswordUpdateRequest = getTestUserPasswordUpdateRequest();
    	testUserPasswordUpdateRequest.setMatchingNewPassword("mismatching-new-password");
    	userProfileService.updatePasswordOfCurrentUser(testUserPasswordUpdateRequest);
	}
	
	/**
	 * deleteCurrentUser() tests
	 */
	
	@Test
	@WithMockUser(username = TEST_USER_EMAIL)
	public void deleteCurrentUserMethodShouldDeleteCurrentUser() {
		UserEntity testUser = insertTestUser();
		
		userProfileService.deleteCurrentUser();
		
		// Test if the user has been deleted
    	Long testUserId = testUser.getId();
		assertTrue( !userRepository.existsById(testUserId) );
	}
	
	@Test(expected = NoSuchElementException.class)
	@WithMockUser(username = TEST_USER_EMAIL)
	public void deleteCurrentUserMethodShouldThrowException() {
		userProfileService.deleteCurrentUser();
	}
	
	/**
	 * getCurrentUserSettings() tests
	 */
	
	@Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void getCurrentUserSettingsMethodShouldReturnCurrentUserSettings() {
    	UserEntity testUser = insertTestUser();
    	insertAndSetTestUserSettingForUser(testUser);
    
        List<UserSetting> retrievedUserSettings = userProfileService.getCurrentUserSettings();
        assertTrue( retrievedUserSettings.size() == 1 );
        assertTrue( retrievedUserSettings.get(0).getKey() == TEST_USER_SETTING_KEY );
    }

	@Test(expected = NoSuchElementException.class)
    @WithMockUser(username = TEST_USER_EMAIL)
    public void getCurrentUserSettingsMethodShouldThrowExceptionBecauseOfMissingUser() {
    	userProfileService.getCurrentUserSettings();
	}
	
	/**
	 * getSettingOfCurrentUserByKey() tests
	 */
	
	@Test(expected = UserSettingNotFoundException.class)
    @WithMockUser(username = TEST_USER_EMAIL)
    public void getCurrentUserSettingByKeyMethodShouldThrowException() throws Exception {
		insertTestUser();
    	userProfileService.getSettingOfCurrentUserByKey(TEST_USER_SETTING_KEY);
    }
    
	@Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void getCurrentUserSettingByKeyMethodShouldReturnOKStatusCode() throws Exception {
    	UserEntity testUser = insertTestUser();
    	insertAndSetTestUserSettingForUser(testUser);
    
    	UserSetting userSetting = userProfileService.getSettingOfCurrentUserByKey(TEST_USER_SETTING_KEY);
        assertTrue( userSetting.getKey() == TEST_USER_SETTING_KEY );
    }
	
	/**
	 * setSettingOfCurrentUserByKey() tests
	 */
	
	@Test(expected = NoSuchElementException.class)
    @WithMockUser(username = TEST_USER_EMAIL)
    public void setSettingOfCurrentUserByKeyMethodShouldThrowExceptionBecauseOfMissingUser() throws Exception {
		UserSettingRequest testUserSettingRequest = getTestUserSettingRequest();
		userProfileService.setSettingOfCurrentUser(testUserSettingRequest);
    }
    
	@Test
    @WithMockUser(username = TEST_USER_EMAIL)
    public void setSettingOfCurrentUserByKeyMethodShouldUpdateExistingUserSetting() throws Exception {
    	UserEntity testUser = insertTestUser();

    	UserSettingRequest testUserSettingRequest = getTestUserSettingRequest();
		userProfileService.setSettingOfCurrentUser(testUserSettingRequest);
    	
    	assertTrue( userSettingRepository.existsByKeyAndUser(UserSettingKey.HAS_LOGGED_IN_BEFORE, testUser) );
    }
    
	@Test(expected = ConstraintViolationException.class)
    @WithMockUser(username = TEST_USER_EMAIL)
    public void setSettingOfCurrentUserByKeyMethodShouldThrowExceptionBecauseOfNullSettingKey() throws Exception {
    	insertTestUser();

    	UserSettingRequest testUserSettingRequest = getTestUserSettingRequest();
    	testUserSettingRequest.setKey(null);
    	
		userProfileService.setSettingOfCurrentUser(testUserSettingRequest);
    }
	
}