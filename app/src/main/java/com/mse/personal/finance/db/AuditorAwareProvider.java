package com.mse.personal.finance.db;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Custom implementation for decorating JPA {@link javax.persistence.Column} annotated with
 * {@link org.springframework.data.annotation.CreatedBy} by providing the currently authenticated user.
 *
 * @author T. Dossev
 */
@Component
public class AuditorAwareProvider 
	implements AuditorAware<String> 
{

	private static final String SYSTEM = "system";

	@Override
	public Optional<String> getCurrentAuditor() {
		/*
		 * TODO: 
		 * 	- Add logic when Spring Security is introduced
		 */
		
		return Optional.of(SYSTEM);
	}
	
}