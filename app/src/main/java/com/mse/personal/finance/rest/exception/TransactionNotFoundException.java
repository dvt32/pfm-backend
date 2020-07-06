package com.mse.personal.finance.rest.exception;

import java.io.Serializable;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This exception is thrown whenever someone 
 * attempts to manipulate a transaction
 * that does not exist in the database.
 * 
 * @author dvt32
 */
@SuppressWarnings("serial")
@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Transaction with this ID does not exist!")
public class TransactionNotFoundException 
	extends ResourceNotFoundException 
{

	public TransactionNotFoundException(Serializable resourceId) {
		super(resourceId);
	}

}