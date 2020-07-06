package com.mse.personal.finance.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mse.personal.finance.db.entity.UserEntity;
import com.mse.personal.finance.db.entity.UserSettingEntity;
import com.mse.personal.finance.db.repository.UserRepository;
import com.mse.personal.finance.db.repository.UserSettingRepository;
import com.mse.personal.finance.model.User;
import com.mse.personal.finance.model.UserSettingKey;
import com.mse.personal.finance.model.request.UserCreateRequest;
import com.mse.personal.finance.model.request.UserUpdateRequest;
import com.mse.personal.finance.rest.exception.UserNotFoundException;
import com.mse.personal.finance.service.mapper.UserMapper;

/**
 * Service for managing {@link User} users.
 * 
 * The entities are retrieved from the database
 * and then mapped to DTOs, which will be used by the REST controller.
 * 
 * Likewise, DTOs sent from the REST controller 
 * are mapped to entities and then added to the database.
 * 
 * TODO:
 * - Implement "forgotten password" feature
 *
 * @author D. Dimitrov
 * @author dvt32
 */
@Service
public class UserService {

	private final UserRepository userRepository;
	private final UserMapper userMapper;
	private final PasswordEncoder passwordEncoder;
	private final CategoryService categoryService;
	private final UserSettingRepository userSettingRepository;

	@Autowired
	public UserService(
		UserRepository usersRepository, 
		UserMapper userMapper,
		PasswordEncoder passwordEncoder,
		CategoryService categoryService,
		UserSettingRepository userSettingRepository) 
	{
		this.userRepository = usersRepository;
		this.userMapper = userMapper;
		this.passwordEncoder = passwordEncoder;
		this.categoryService = categoryService;
		this.userSettingRepository = userSettingRepository;
	}
	
	/**
	 * Returns a page of users' data 
	 * that meet the paging restriction 
	 * in the passed Pageable object.
	 */
	public Page<User> getUsersByPageable(Pageable pageable) {
		Page<UserEntity> userEntities = userRepository.findAll(pageable);
		Page<User> userDTOs = userEntities.map(userMapper::fromEntity);
		return userDTOs;
	}
	
	/**
	 * Returns an existing user's data
	 * by passing the user's ID.
	 */
	public User getUserById(Long id) 
		throws UserNotFoundException
	{
		boolean existsById = userRepository.existsById(id);
		if (!existsById) {
			throw new UserNotFoundException(id);
		}
		
		UserEntity userEntity = userRepository.findById(id).get();
		
		User userDTO = userMapper.fromEntity(userEntity);
		
		return userDTO;
	}
	
	/**
	 * Returns an existing user's data
	 * by passing the user's email.
	 */
	public User getUserByEmail(String email) 
		throws UserNotFoundException
	{
		boolean existsByEmail = userRepository.existsByEmail(email);
		if (!existsByEmail) {
			throw new UserNotFoundException(email);
		}
		
		UserEntity userEntity = userRepository.findByEmail(email).get();
		
		User userDTO = userMapper.fromEntity(userEntity);
		
		return userDTO;
	}

	/**
	 * Creates a new user from a user DTO, 
	 * which contains the user's data
	 * and returns the newly created user's data.
	 * 
	 * The password is encoded 
	 * before it is stored in the database.
	 * 
	 * Additionally two system categories 
	 * are created for the user (for account balance sync).
	 */
	public User createNewUser(UserCreateRequest userCreateRequest) {
		UserEntity userEntity = userMapper.toEntity(userCreateRequest);
		
		String rawUserPassword = userEntity.getPassword();
		String encodedUserPassword = passwordEncoder.encode(rawUserPassword);
		userEntity.setPassword(encodedUserPassword);
		userEntity = userRepository.save(userEntity);
		
		categoryService.createSystemCategoriesForUser(userEntity);
		
		UserSettingEntity firstTimeLoginSetting = new UserSettingEntity(UserSettingKey.HAS_LOGGED_IN_BEFORE, "false", userEntity);
		userSettingRepository.save(firstTimeLoginSetting);
		
		User userDTO = userMapper.fromEntity(userEntity);
		
		return userDTO;
	}
	
	/**
	 * Updates an existing user with a specified ID 
	 * and returns the updated user's data.
	 */
	public User updateUserById(Long id, UserUpdateRequest userUpdateRequest) 
		throws UserNotFoundException
	{
		boolean existsById = userRepository.existsById(id);
		if (!existsById) {
			throw new UserNotFoundException(id);
		}
		
		UserEntity userEntity = userRepository.findById(id).get();
		
		UserEntity updatedUserEntity = userMapper.toEntity(userUpdateRequest);
		// Email, ID & password are not included in the request DTO and must be set manually.
		// ID is retrieved from the HTTP request's passed URL.
		// Email & password are retrieved from the existing entity in the database.
		String email = userEntity.getEmail();
		String password = userEntity.getPassword();
		updatedUserEntity.setId(id);
		updatedUserEntity.setEmail(email);
		updatedUserEntity.setPassword(password);
		updatedUserEntity = userRepository.save(updatedUserEntity);
		
		User userDTO = userMapper.fromEntity(updatedUserEntity);
		
		return userDTO;
	}
	
	/**
	 * Updates an existing user with a specified email
	 * and returns the updated user's data.
	 */
	public User updateUserByEmail(String email, UserUpdateRequest userUpdateRequest) 
		throws UserNotFoundException
	{
		boolean existsByEmail = userRepository.existsByEmail(email);
		if (!existsByEmail) {
			throw new UserNotFoundException(email);
		}
		
		UserEntity userEntity = userRepository.findByEmail(email).get();
		
		UserEntity updatedUserEntity = userMapper.toEntity(userUpdateRequest);
		// Email, ID & password are not included in the request DTO and must be set manually.
		// Email is retrieved from the HTTP request's passed URL.
		// ID & password is retrieved from the existing entity in the database.
		Long id = userEntity.getId();
		String password = userEntity.getPassword();
		updatedUserEntity.setEmail(email);
		updatedUserEntity.setId(id);
		updatedUserEntity.setPassword(password);
		updatedUserEntity = userRepository.save(updatedUserEntity);
		
		User userDTO = userMapper.fromEntity(updatedUserEntity);
		
		return userDTO;
	}
	
	/**
	 * Deletes an existing user with a specified ID
	 * and returns the deleted user's data.
	 */
	public User deleteUserById(Long id) 
		throws UserNotFoundException 
	{
		boolean existsById = userRepository.existsById(id);
		if (!existsById) {
			throw new UserNotFoundException(id);
		}
		
		UserEntity userEntity = userRepository.findById(id).get();
		
		User userDTO = userMapper.fromEntity(userEntity);
		
		userRepository.deleteById(id);
		
		return userDTO;
	}
	
	/**
	 * Deletes an existing user with a specified email
	 * and returns the deleted user's data.
	 */
	public User deleteUserByEmail(String email) 
		throws UserNotFoundException 
	{
		boolean existsByEmail = userRepository.existsByEmail(email);
		if (!existsByEmail) {
			throw new UserNotFoundException(email);
		}
		
		UserEntity userEntity = userRepository.findByEmail(email).get();
		
		User userDTO = userMapper.fromEntity(userEntity);
		
		userRepository.deleteByEmail(email);
		
		return userDTO;
	}
	
	/**
	 * Returns the total number of users in the database.
	 */
	public long getNumberOfUsers() {
		return userRepository.count();
	}
	
	/**
	 * Checks if a passed raw user password matches 
	 * a specified user's actual encoded password.
	 */
	public boolean isCorrectPasswordForUser(UserEntity user, String rawPassword) {
		String actualEncodedPassword = user.getPassword();
		boolean isCorrectPasswordForUser = passwordEncoder.matches(rawPassword, actualEncodedPassword);
		return isCorrectPasswordForUser;
	}

}