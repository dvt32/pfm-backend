package com.mse.personal.finance.security.jwt;

import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configures {@link JwtTokenFilter} in Spring Security.
 *
 * @author T. Dossev
 */
public class JwtTokenFilterConfigurer 
	extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> 
{

	private final JwtTokenProvider jwtTokenProvider;
	private final JwtSessionManager jwtSessionManager;

	public JwtTokenFilterConfigurer(JwtTokenProvider jwtTokenProvider, JwtSessionManager jwtSessionManager) {
		this.jwtTokenProvider = jwtTokenProvider;
		this.jwtSessionManager = jwtSessionManager;
	}

	@Override
	public void configure(HttpSecurity http) {
		JwtTokenFilter jwtTokenFilter = new JwtTokenFilter(jwtTokenProvider, jwtSessionManager);
		http.addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);
	}
	
}