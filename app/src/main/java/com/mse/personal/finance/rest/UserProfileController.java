package com.mse.personal.finance.rest;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mse.personal.finance.model.User;
import com.mse.personal.finance.model.UserSetting;
import com.mse.personal.finance.model.UserSettingKey;
import com.mse.personal.finance.model.request.UserPasswordUpdateRequest;
import com.mse.personal.finance.model.request.UserSettingRequest;
import com.mse.personal.finance.model.request.UserUpdateRequest;
import com.mse.personal.finance.rest.exception.InvalidDataException;
import com.mse.personal.finance.rest.exception.UserSettingNotFoundException;
import com.mse.personal.finance.service.UserProfileService;

/**
 * REST controller for user operations
 * after the user has logged in to his account 
 * (e.g. changing his password, his preferences etc.)
 * 
 * @author dvt32
 */
@RestController
@RequestMapping(path = "/profile")
public class UserProfileController {

	private final UserProfileService userProfileService;

	@Autowired
	public UserProfileController(UserProfileService userProfileService) {
		this.userProfileService = userProfileService;
	}
	
	/**
	 * Returns the currently logged-in user's data from the database
	 * (excluding his password, for security reasons) as a JSON object.
	 * 
	 * @return the user's data as a JSON object
	 */
	@GetMapping
	public User getCurrentUser() {
		User currentUser = userProfileService.getCurrentUser();
		return currentUser;
	}
	
	/**
	 * Updates the currently logged-in user's data in the database 
	 * by passing the updated user's data in a PUT request's body
	 * and returns the updated user's data as a JSON object.
	 * 
	 * The data is validated before the user is updated in the database.
	 * 
	 * @param userUpdateRequest the user-to-be-updated's data
	 * @param bindingResult the validator of the passed data
	 * @return the updated user's data
	 */
	@PutMapping
	public User updateCurrentUser(
		@RequestBody @Valid UserUpdateRequest userUpdateRequest, 
		BindingResult bindingResult) 
	{
		if (bindingResult.hasErrors()) {
			String errorMessageFromBindingResult = ControllerUtils.getErrorMessageFromBindingResult(bindingResult);
			throw new InvalidDataException(errorMessageFromBindingResult);
		}
		
		User updatedUser = userProfileService.updateCurrentUser(userUpdateRequest);
		
		return updatedUser;
	}
	
	/**
	 * Changes a user's password in the system by passing in 
	 * the user's old password once and his new password twice
	 * in a PUT request's body, and returns the user's data as a JSON object
	 * (excluding his password, for security reasons).
	 * 
	 * The data is validated before the user is updated in the database.
	 * 
	 * @param userPasswordUpdateRequest a request consisting of the user's old password, his new password and his new password again
	 * @param bindingResult the validator of the passed data
	 * @return the user's data
	 */
	@PutMapping("/password")
	public User updatePasswordOfCurrentUser(
		@RequestBody @Valid UserPasswordUpdateRequest userPasswordUpdateRequest,
		BindingResult bindingResult) 
	{
		if (bindingResult.hasErrors()) {
			String errorMessageFromBindingResult = ControllerUtils.getErrorMessageFromBindingResult(bindingResult);
			throw new InvalidDataException(errorMessageFromBindingResult);
		}
		
		User user = null;
		try {
			user = userProfileService.updatePasswordOfCurrentUser(userPasswordUpdateRequest);
		}
		catch (InvalidDataException e) {
			String exceptionMessage = e.getMessage();
			throw new InvalidDataException(exceptionMessage);
		}
		
		return user;
	}
	
	/**
	 * Deletes the currently logged-in user from the database.
	 * 
	 * The @Transactional annotation is used to prevent a 
	 * TransactionRequiredException exception.
	 * 
	 * @return the deleted user's data
	 */
	@DeleteMapping
	@Transactional
	public User deleteCurrentUser() {
		User deletedUser = userProfileService.deleteCurrentUser();
		return deletedUser;
	}
	
	/**
	 * Returns the currently logged-in user's 
	 * settings data from the database
	 * as a JSON list.
	 * 
	 * @return the user's settings data as a JSON list
	 */
	@GetMapping("/settings")
	public List<UserSetting> getCurrentUserSettings() {
		List<UserSetting> currentUserSettings = userProfileService.getCurrentUserSettings();
		return currentUserSettings;
	}
	
	/**
	 * Returns a current user's setting's data
	 * as a JSON object
	 * by passing the setting's key as a request param.
	 * 
	 * @param key The key of the setting-to-be-retrieved
	 * @return the user setting's data as a JSON object
	 */
	@GetMapping(value = "/setting-by-key")
	public UserSetting getUserSettingByKey(@RequestParam UserSettingKey key) { 
		UserSetting setting = null;
		
		try {
			setting = userProfileService.getSettingOfCurrentUserByKey(key);
		}
		catch (UserSettingNotFoundException e) {
			throw new UserSettingNotFoundException(key);
		}
		
		return setting;
	}
	
	/**
	 * Sets a current user's setting's data
	 * by passing the setting's key and value.
	 * 
	 * It then returns the setting's data as a JSON object.
	 * 
	 * @param userSettingRequest An object containing the setting's key and value
	 * @param bindingResult the validator of the passed data
	 * @return the user setting's data as a JSON object
	 */
	@PostMapping(value = "/settings")
	public UserSetting setUserSetting(
		@RequestBody @Valid UserSettingRequest userSettingRequest,
		BindingResult bindingResult) 
	{ 
		if (bindingResult.hasErrors()) {
			String errorMessageFromBindingResult = ControllerUtils.getErrorMessageFromBindingResult(bindingResult);
			throw new InvalidDataException(errorMessageFromBindingResult);
		}
		
		UserSetting setting = userProfileService.setSettingOfCurrentUser(userSettingRequest);
		
		return setting;
	}
	
}