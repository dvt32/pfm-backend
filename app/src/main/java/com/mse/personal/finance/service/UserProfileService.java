package com.mse.personal.finance.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mse.personal.finance.db.entity.UserEntity;
import com.mse.personal.finance.db.entity.UserSettingEntity;
import com.mse.personal.finance.db.repository.UserRepository;
import com.mse.personal.finance.db.repository.UserSettingRepository;
import com.mse.personal.finance.model.User;
import com.mse.personal.finance.model.UserSetting;
import com.mse.personal.finance.model.UserSettingKey;
import com.mse.personal.finance.model.request.UserPasswordUpdateRequest;
import com.mse.personal.finance.model.request.UserSettingRequest;
import com.mse.personal.finance.model.request.UserUpdateRequest;
import com.mse.personal.finance.rest.exception.InvalidDataException;
import com.mse.personal.finance.rest.exception.UserSettingNotFoundException;
import com.mse.personal.finance.service.mapper.UserMapper;
import com.mse.personal.finance.service.mapper.UserSettingMapper;

/**
 * Service for managing {@link User} user operations
 * for the currently logged-in user.
 * 
 * The entities are retrieved from the database
 * and then mapped to DTOs, which will be used by the REST controller.
 * 
 * Likewise, DTOs sent from the REST controller 
 * are mapped to entities and then added to the database.
 *
 * @author dvt32
 */
@Service
public class UserProfileService {

	private final UserRepository userRepository;
	private final UserMapper userMapper;
	private final UserService userService;
	private final PasswordEncoder passwordEncoder;
	private final ServiceUtils serviceUtils;
	private final UserSettingRepository userSettingRepository;
	private final UserSettingMapper userSettingMapper;
	
	@Autowired
	public UserProfileService(
		UserRepository userRepository, 
		UserMapper userMapper,
		UserService userService,
		PasswordEncoder passwordEncoder,
		ServiceUtils serviceUtils,
		UserSettingRepository userSettingRepository,
		UserSettingMapper userSettingMapper) 
	{
		this.userRepository = userRepository;
		this.userMapper = userMapper;
		this.userService = userService;
		this.passwordEncoder = passwordEncoder;
		this.serviceUtils = serviceUtils;
		this.userSettingRepository = userSettingRepository;
		this.userSettingMapper = userSettingMapper;
	}
	
	/**
	 * Returns the currently logged-in user's data.
	 */
	public User getCurrentUser() {
		UserEntity currentlyLoggedInUserEntity = serviceUtils.getCurrentlyLoggedInUserEntity();
		User currentlyLoggedInUserDTO = userMapper.fromEntity(currentlyLoggedInUserEntity);
		return currentlyLoggedInUserDTO;
	}
	
	/**
	 * Updates the currently logged-in user
	 * and returns his data.
	 */
	public User updateCurrentUser(UserUpdateRequest user) {
		UserEntity updatedUserEntity = userMapper.toEntity(user);
		
		// Email, ID & password are not included in the request DTO and must be set manually.
		// All are retrieved via the currently logged-in user's authentication data.
		UserEntity currentlyLoggedInUserEntity = serviceUtils.getCurrentlyLoggedInUserEntity();
		String email = currentlyLoggedInUserEntity.getEmail();
		Long id = currentlyLoggedInUserEntity.getId();
		String password = currentlyLoggedInUserEntity.getPassword();
		updatedUserEntity.setEmail(email);
		updatedUserEntity.setId(id);
		updatedUserEntity.setPassword(password);
		
		updatedUserEntity = userRepository.save(updatedUserEntity);
		
		User updatedUserDTO = userMapper.fromEntity(updatedUserEntity);
		
		return updatedUserDTO;
	}
	
	/**
	 * Updates the currently logged-in user's password
	 * and returns that user's data
	 * (without the password, for security reasons).
	 */
	public User updatePasswordOfCurrentUser(UserPasswordUpdateRequest passwordUpdateRequest) {
		String oldPassword = passwordUpdateRequest.getOldPassword();
		String newPassword = passwordUpdateRequest.getNewPassword();
		String matchingNewPassword = passwordUpdateRequest.getMatchingNewPassword();
		
		UserEntity currentlyLoggedInUserEntity = serviceUtils.getCurrentlyLoggedInUserEntity();
		boolean isCorrectPasswordForCurrentUser = userService.isCorrectPasswordForUser(currentlyLoggedInUserEntity, oldPassword);
		if (!isCorrectPasswordForCurrentUser) {
			throw new InvalidDataException("Incorrect old password!");
		}
		boolean newPasswordsMatch = newPassword.equals(matchingNewPassword);
		if (!newPasswordsMatch) {
			throw new InvalidDataException("New passwords don't match!");
		}
		
		String newPasswordEncoded = passwordEncoder.encode(newPassword);
		currentlyLoggedInUserEntity.setPassword(newPasswordEncoded);
		currentlyLoggedInUserEntity = userRepository.save(currentlyLoggedInUserEntity);
		
		User currentlyLoggedInUserDTO = userMapper.fromEntity(currentlyLoggedInUserEntity);
		
		return currentlyLoggedInUserDTO;
	}
	
	/**
	 * Deletes the currently logged-in user
	 * and returns his data.
	 */
	public User deleteCurrentUser() {
		String currentlyLoggedInUserEmail = serviceUtils.getUsernameOfCurrentlyLoggedInUser();
		UserEntity currentlyLoggedInUserEntity = serviceUtils.getCurrentlyLoggedInUserEntity();
		User deletedUserDTO = userMapper.fromEntity(currentlyLoggedInUserEntity);
		userRepository.deleteByEmail(currentlyLoggedInUserEmail);
		return deletedUserDTO;
	}
	
	/**
	 * Returns all of the currently logged-in user's settings data (as a list).
	 */
	public List<UserSetting> getCurrentUserSettings() {
		UserEntity currentlyLoggedInUserEntity = serviceUtils.getCurrentlyLoggedInUserEntity();
		
		List<UserSettingEntity> allUserSettingEntities = userSettingRepository.findAllByUser(currentlyLoggedInUserEntity);
		
		List<UserSetting> allUserSettingDTOs = allUserSettingEntities.stream()
			.map( userSettingEntity -> userSettingMapper.fromEntity(userSettingEntity) )
			.collect( Collectors.toList() );
		
		return allUserSettingDTOs;
	}
	
	/**
	 * Returns a current user's setting's data
	 * by passing the setting's key.
	 */
	public UserSetting getSettingOfCurrentUserByKey(UserSettingKey key) 
		throws UserSettingNotFoundException
	{
		UserEntity currentlyLoggedInUserEntity = serviceUtils.getCurrentlyLoggedInUserEntity();
		boolean existsByKeyAndUser = userSettingRepository.existsByKeyAndUser(key, currentlyLoggedInUserEntity);
		if (!existsByKeyAndUser) {
			throw new UserSettingNotFoundException(key);
		}
		
		UserSettingEntity userSettingEntity = userSettingRepository.findByKeyAndUser(key, currentlyLoggedInUserEntity);
		
		UserSetting userSettingDTO = userSettingMapper.fromEntity(userSettingEntity);
		
		return userSettingDTO;
	}
	
	/**
	 * Sets a current user's setting's data
	 * by passing the setting's key and value.
	 */
	public UserSetting setSettingOfCurrentUser(UserSettingRequest userSettingRequest) 
		throws UserSettingNotFoundException
	{
		UserEntity currentlyLoggedInUserEntity = serviceUtils.getCurrentlyLoggedInUserEntity();
		UserSettingKey key = userSettingRequest.getKey();
		String value = userSettingRequest.getValue();
		
		boolean existsByKeyAndUser = userSettingRepository.existsByKeyAndUser(key, currentlyLoggedInUserEntity);
		
		UserSettingEntity userSettingEntity;
		if (!existsByKeyAndUser) {
			userSettingEntity = new UserSettingEntity(key, value, currentlyLoggedInUserEntity);
		}
		else {
			userSettingEntity = userSettingRepository.findByKeyAndUser(key, currentlyLoggedInUserEntity);
			userSettingEntity.setValue(value);
		}
		
		userSettingEntity = userSettingRepository.save(userSettingEntity);
		
		UserSetting userSettingDTO = userSettingMapper.fromEntity(userSettingEntity);
		
		return userSettingDTO;
	}
	
}