package com.mse.personal.finance.rest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This exception is thrown whenever 
 * a user attempts to login
 * while his IP is still blocked
 * due to reaching the max number of 
 * consecutive invalid login attempts.
 * 
 * @author dvt32
 */
@SuppressWarnings("serial")
@ResponseStatus(code = HttpStatus.UNAUTHORIZED)
public class IpAddressBlockedException 
	extends RuntimeException 
{
	
	public IpAddressBlockedException(String message) {
		super(message);
	}
	
}