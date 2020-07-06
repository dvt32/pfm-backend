package com.mse.personal.finance.rest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This exception is thrown whenever 
 * someone attempts to send invalid 
 * data via a REST request.
 * 
 * @author dvt32
 */
@SuppressWarnings("serial")
@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class InvalidDataException 
	extends RuntimeException 
{
	
	public InvalidDataException(String message) {
		super(message);
	}
	
}