package com.mse.personal.finance.rest.exception;

import java.io.Serializable;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This exception is thrown whenever someone 
 * attempts to manipulate a user setting
 * which does not exist in the database.
 * 
 * @author dvt32
 */
@SuppressWarnings("serial")
@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "User setting with this key does not exist!")
public class UserSettingNotFoundException 
	extends ResourceNotFoundException 
{

	public UserSettingNotFoundException(Serializable resourceId) {
		super(resourceId);
	}

}