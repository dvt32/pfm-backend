package com.mse.personal.finance.db.validation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Custom email validator used when persisting users to the database.
 * It is used as a replacement to Hibernate's @Email, which is more limited.
 * 
 * Implementation provided by Baeldung:
 * 	- https://github.com/Baeldung/spring-security-registration/blob/master/src/main/java/org/baeldung/validation/EmailValidator.java
 */
public class EmailValidator 
  implements ConstraintValidator<ValidEmail, String> 
{
	
    private Pattern pattern;
    private Matcher matcher;
    private static final String EMAIL_PATTERN = 
    	"^[_A-Za-z0-9-+]+(.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(.[A-Za-z0-9]+)*(.[A-Za-z]{2,})$"; 
    
    @Override
    public void initialize(ValidEmail constraintAnnotation) {}
    
    @Override
    public boolean isValid(String email, ConstraintValidatorContext context){   
        return (validateEmail(email));
    } 
    
    private boolean validateEmail(String email) {
        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(email);
        return matcher.matches();
    }
    
}