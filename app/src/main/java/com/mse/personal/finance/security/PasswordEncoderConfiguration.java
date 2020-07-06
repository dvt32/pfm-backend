package com.mse.personal.finance.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configures {@link BCryptPasswordEncoder} that will be used for hashing user passwords.
 *
 * @author T. Dossev
 */
@Configuration
public class PasswordEncoderConfiguration {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}