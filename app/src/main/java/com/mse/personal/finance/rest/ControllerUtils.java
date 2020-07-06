package com.mse.personal.finance.rest;

import java.util.List;
import java.util.StringJoiner;

import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

/**
 * This class provides helper methods 
 * used by the REST controller classes.
 * 
 * @author dvt32
 */
public class ControllerUtils {
	
	/**
	 * This method extracts a list of error messages from a BindingResult object (in the form of a string)
	 * 
	 * @param bindingResult The source BindingRresult object
	 * @return A string containing all the error messages (separated by a space)
	 */
	public static String getErrorMessageFromBindingResult(BindingResult bindingResult) {
		List<ObjectError> errors = bindingResult.getAllErrors();
		
		StringJoiner errorMessageJoiner = new StringJoiner(" ");
		for (ObjectError error : errors) {
			String errorMessage = error.getDefaultMessage();
			errorMessageJoiner.add(errorMessage);
		}
		
		String errorMessage = errorMessageJoiner.toString();
		
		return errorMessage;
	}
	
}