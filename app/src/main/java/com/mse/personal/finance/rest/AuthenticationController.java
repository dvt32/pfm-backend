package com.mse.personal.finance.rest;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mse.personal.finance.model.UserAuthenticationToken;
import com.mse.personal.finance.model.request.UserLoginRequest;
import com.mse.personal.finance.rest.exception.InvalidDataException;
import com.mse.personal.finance.service.AuthenticationService;

/**
 * REST controller for authentication operations.
 *
 * @author T. Dossev
 * @author dvt32
 */
@RestController
@RequestMapping(path = "/auth")
public class AuthenticationController {

	private final AuthenticationService authenticationService;

	@Autowired
	public AuthenticationController(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	/**
	 * Returns a JWT, 
	 * which the user can use 
	 * to access resources in the system.
	 */
	@PostMapping("/login")
	public UserAuthenticationToken getUserAuthenticationTokenForUser(
		@RequestBody @Valid UserLoginRequest user,
		BindingResult bindingResult) 
	{
		if (bindingResult.hasErrors()) {
			String errorMessageFromBindingResult = ControllerUtils.getErrorMessageFromBindingResult(bindingResult);
			throw new InvalidDataException(errorMessageFromBindingResult);
		}
		
		UserAuthenticationToken userAuthenticationToken = authenticationService.getUserAuthenticationTokenForUser(user);
		
		return userAuthenticationToken;
	}

	/**
	 * Removes an existing JWT specified in a HTTP GET request's "Authorization" header.
	 */
	@GetMapping("/logout")
	public void removeUserAuthenticationTokenFromRequest(HttpServletRequest request) {
		authenticationService.removeUserAuthenticationTokenFromRequest(request);
	}

}