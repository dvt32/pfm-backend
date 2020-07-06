package com.mse.personal.finance.rest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This exception is thrown whenever someone 
 * attempts to update an account's type in the database
 * when the account has been deleted.
 * 
 * @author dvt32
 */
@SuppressWarnings("serial")
@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class AccountTypeUpdateException 
	extends RuntimeException 
{
	
	public AccountTypeUpdateException(String message) {
		super(message);
	}
	
}