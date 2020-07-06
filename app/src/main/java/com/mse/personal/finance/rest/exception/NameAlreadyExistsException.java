package com.mse.personal.finance.rest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This exception is thrown whenever someone 
 * attempts to insert an account/category
 * with a name that already exists in the database.
 * 
 * @author dvt32
 */
@SuppressWarnings("serial")
@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class NameAlreadyExistsException
	extends InvalidDataException
{
	
    public NameAlreadyExistsException(String message) {
        super(message);
    }
 
}