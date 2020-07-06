package com.mse.personal.finance.rest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This exception is thrown whenever 
 * a user attempts to access a resource
 * (account, category, reporting period, transaction etc.)
 * which does not belong to him.
 * 
 * @author dvt32
 */
@SuppressWarnings("serial")
@ResponseStatus(code = HttpStatus.FORBIDDEN, reason = "User does not own this resource!")
public class UserDoesNotOwnResourceException 
	extends RuntimeException 
{
	
}