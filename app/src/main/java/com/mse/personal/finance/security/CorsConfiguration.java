package com.mse.personal.finance.security;

import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configures CORS filter that allows any origin and HTTP method for access.
 *
 * @author T. Dossev
 */
@Configuration
public class CorsConfiguration {

	/**
	 * Configures CORS filter that allows any origin and http method for access.
	 *
	 * @return configured {@link CorsConfigurationSource}
	 */
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
		List<String> allowedList = Collections.singletonList(org.springframework.web.cors.CorsConfiguration.ALL);
		
		configuration.applyPermitDefaultValues();
		configuration.setAllowedMethods(allowedList);
		configuration.setAllowedHeaders(allowedList);
		configuration.setAllowedOrigins(allowedList);
		
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		
		return source;
	}

}