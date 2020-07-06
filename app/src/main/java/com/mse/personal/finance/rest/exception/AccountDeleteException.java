package com.mse.personal.finance.rest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This exception is thrown whenever someone 
 * attempts to delete an account in the database,
 * which does not meet the deletion criteria.
 * 
 * @author dvt32
 */
@SuppressWarnings("serial")
@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class AccountDeleteException 
	extends RuntimeException 
{
	
	public AccountDeleteException(String message) {
		super(message);
	}
	
}