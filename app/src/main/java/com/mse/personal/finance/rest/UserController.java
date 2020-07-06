package com.mse.personal.finance.rest;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mse.personal.finance.model.User;
import com.mse.personal.finance.model.request.UserCreateRequest;
import com.mse.personal.finance.model.request.UserUpdateRequest;
import com.mse.personal.finance.rest.exception.InvalidDataException;
import com.mse.personal.finance.rest.exception.UserNotFoundException;
import com.mse.personal.finance.service.UserService;

/**
 * REST controller for CRUD operations 
 * upon {@link User} users.
 * 
 * Most methods have a @Secured("ROLE_ADMIN") annotation,
 * because the specified endpoints 
 * should be accessible only to admins.
 * 
 * TODO:
 * - Implement "forgotten password" feature
 *
 * @author D. Dimitrov
 * @author dvt32
 */
@RestController
@RequestMapping(path = "/users")
public class UserController {

	private final UserService userService;

	@Autowired
	public UserController(UserService usersService) {
		this.userService = usersService;
	}
	
	/**
	 * Returns a page of users data as a JSON object
	 * by passing a paging restriction in a GET request.
	 * 
	 * The paging restriction can be applied 
	 * by passing request parameters in the GET request:
	 * - "page" (the number of the page to be returned, numbering is zero-based)
	 * - "size" (number of elements on the page)
	 * - "sort" (the order of the returned elements).
	 * 
	 * @return a page of users' data
	 */
	@GetMapping
	@Secured("ROLE_ADMIN")
	public Page<User> getUsersByPageable(Pageable pageable) {
		Page<User> pageOfUsers = userService.getUsersByPageable(pageable);
		return pageOfUsers;
	}
	
	/**
	 * This method retrieves a user's info in JSON format
	 * by passing in his ID as a path parameter.
	 * 
	 * @param id The id of the user to be retrieved
	 * @return the user's data
	 */
	@GetMapping("/{id}")
	@Secured("ROLE_ADMIN")
	public User getUserById(@PathVariable Long id) {
		User user = null;
		
		try {
			user = userService.getUserById(id);
		} 
		catch (UserNotFoundException e) {
			throw new UserNotFoundException(id);
		}
		
		return user;
	}
	
	/**
	 * Returns a specific user's data as a JSON object
	 * by passing in his email as a request parameter.
	 * 
	 * @param email The email of the user to be retrieved
	 * @return the user's data
	 */
	@GetMapping(params = "email")
	@Secured("ROLE_ADMIN")
	public User getUserByEmail(@RequestParam String email) {
		User user = null;
		
		try {
			user = userService.getUserByEmail(email);
		} 
		catch (UserNotFoundException e) {
			throw new UserNotFoundException(email);
		}
		
		return user;
	}

	/**
	 * This method creates a user by passing the user's data in a POST request's body 
	 * and returns the newly created user's data as a JSON object.
	 * 
	 * The data is validated before the user is stored in the database.
	 * 
	 * @param userCreateRequest An object containing the user-to-be-created's data
	 * @param bindingResult The validator of the passed data
	 * 
	 * @return the created user's data
	 */
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public User createUser(
		@RequestBody @Valid UserCreateRequest userCreateRequest, 
		BindingResult bindingResult) 
	{
		if (bindingResult.hasErrors()) {
			String errorMessageFromBindingResult = ControllerUtils.getErrorMessageFromBindingResult(bindingResult);
			throw new InvalidDataException(errorMessageFromBindingResult);
		}
		
		User createdUser = userService.createNewUser(userCreateRequest);
		
		return createdUser;
	}

	/**
	 * This method updates an existing user in the database 
	 * by passing the updated user's data in a POST request's body 
	 * (and the user's id as a path parameter)
	 * and returns the updated user's data as a JSON object.
	 * 
	 * The data is validated before the user is updated in the database.
	 * 
	 * @param id The user-to-be-updated's ID
	 * @param userUpdateRequest An object containing the user-to-be-updated's data
	 * @param bindingResult The validator of the passed data
	 * 
	 * @return the updated user's data
	 */
	@PutMapping("/{id}")
	@Secured("ROLE_ADMIN")
	public User updateUserById(
		@PathVariable Long id, 
		@RequestBody @Valid UserUpdateRequest userUpdateRequest, 
		BindingResult bindingResult) 
	{
		if (bindingResult.hasErrors()) {
			String errorMessageFromBindingResult = ControllerUtils.getErrorMessageFromBindingResult(bindingResult);
			throw new InvalidDataException(errorMessageFromBindingResult);
		}
		
		User updatedUser = null;
		try {
			updatedUser = userService.updateUserById(id, userUpdateRequest);
		} 
		catch (UserNotFoundException e) {
			throw new UserNotFoundException(id);
		}
		
		return updatedUser;
	}
	
	/**
	 * This method updates an existing user in the database 
	 * by passing the updated user's data in a POST request's body 
	 * (and the user's email address as a request parameter)
	 * and returns the updated user's data as a JSON object.
	 * 
	 * The data is validated before the user is updated in the database.
	 * 
	 * @param email The user-to-be-updated's email
	 * @param userUpdateRequest An object containing the user-to-be-updated's data
	 * @param bindingResult The validator of the passed data
	 * 
	 * @return the updated user's data
	 */
	@PutMapping(params = "email")
	@Secured("ROLE_ADMIN")
	public User updateUserByEmail(
		@RequestParam String email, 
		@RequestBody @Valid UserUpdateRequest userUpdateRequest, 
		BindingResult bindingResult) 
	{
		if (bindingResult.hasErrors()) {
			String errorMessageFromBindingResult = ControllerUtils.getErrorMessageFromBindingResult(bindingResult);
			throw new InvalidDataException(errorMessageFromBindingResult);
		}
		
		User updatedUser = null;
		try {
			updatedUser = userService.updateUserByEmail(email, userUpdateRequest);
		} 
		catch (UserNotFoundException e) {
			throw new UserNotFoundException(email);
		}
		
		return updatedUser;
	}
	
	/**
	 * This method deletes an existing user in the database by passing an ID
	 * and returns the deleted user's data as a JSON object.
	 * 
	 * If the user does not exist, an exception is thrown.
	 * 
	 * @param id The ID of the user
	 * @return the deleted user's data
	 */
	@DeleteMapping("/{id}")
	@Secured("ROLE_ADMIN")
	public User deleteUserById(@PathVariable Long id) {
		User deletedUser = null;
		
		try {
			deletedUser = userService.deleteUserById(id);
		} 
		catch (UserNotFoundException e) {
			throw new UserNotFoundException(id);
		}
		
		return deletedUser;
	}
	
	/**
	 * This method deletes an existing user in the database by passing an email
	 * and returns the deleted user's data as a JSON object.
	 * 
	 * If the user does not exist, an exception is thrown.
	 * 
	 * The @Transactional annotation is used to prevent a 
	 * TransactionRequiredException exception.
	 * 
	 * @param email The email of the user
	 * @return the deleted user's data
	 */
	@DeleteMapping(params = "email")
	@Transactional
	@Secured("ROLE_ADMIN")
	public User deleteUserByEmail(@RequestParam String email) {
		User deletedUser = null;
		
		try {
			deletedUser = userService.deleteUserByEmail(email);
		} 
		catch (UserNotFoundException e) {
			throw new UserNotFoundException(email);
		}
		
		return deletedUser;
	}

}