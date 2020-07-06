package com.mse.personal.finance.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mse.personal.finance.security.jwt.JwtSessionManager;
import com.mse.personal.finance.security.jwt.JwtTokenFilterConfigurer;
import com.mse.personal.finance.security.jwt.JwtTokenProvider;

/**
 * This class defines Spring Security configuration
 * like what users have what privileges,
 * which pages are protected, how is login/logout handled etc.
 *
 * @author T. Dossev
 * @author dvt32
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
	prePostEnabled = true, // enables Spring Security pre/post annotations
	securedEnabled = true, // determines if the @Secured annotation should be enabled
	jsr250Enabled = true // allows us to use the @RoleAllowed annotation
)
public class SecurityConfiguration 
	extends WebSecurityConfigurerAdapter 
{
	
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final JwtSessionManager jwtSessionManager;
	private final DatabaseUserDetailsService databaseUserDetailsService;
	
	@Autowired
	public SecurityConfiguration(
		DatabaseUserDetailsService databaseUserDetailsService,
		PasswordEncoder passwordEncoder,
		JwtTokenProvider jwtTokenProvider, 
		JwtSessionManager jwtSessionManager) 
	{
		this.databaseUserDetailsService = databaseUserDetailsService;
		this.passwordEncoder = passwordEncoder;
		this.jwtTokenProvider = jwtTokenProvider;
		this.jwtSessionManager = jwtSessionManager;
	}

	@Override
	protected void configure(HttpSecurity http) 
		throws Exception
	{
		// Configure CORS via the CorsConfiguration class
		http.cors();

		// Disable CSRF support
		http.csrf().disable();
		
		// Enable HTTP Basic Auth (for testing purposes)
		http.httpBasic();

		// Disable HTTP sessions because a custom JWT authentication is used
		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

		// Set request access restriction settings
		http.authorizeRequests()
			// Allow user login to all
			.antMatchers( HttpMethod.POST, "/auth/login" )
				.permitAll()
			// Allow user registration (user creation) to all
			.antMatchers( HttpMethod.POST, "/users" )
				.permitAll()
			// Allow access to Swagger UI to all (for testing purposes)
			.antMatchers(
				"/v2/api-docs", 
				"/swagger-resources/configuration/ui", 
				"/swagger-resources", 
				"/swagger-resources/configuration/security", 
				"/swagger-ui.html",
				"/webjars/**"
			)
				.permitAll()
			// Require authentication for any other request
			.anyRequest()
				.authenticated();

		// Configure custom filter for JWT authentication
		JwtTokenFilterConfigurer jwtTokenFilterConfigurer = new JwtTokenFilterConfigurer(jwtTokenProvider, jwtSessionManager);
		http.apply(jwtTokenFilterConfigurer);
	}
	
	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() 
		throws Exception 
	{
		return super.authenticationManagerBean();
	}
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) 
		throws Exception 
	{
		auth
			.userDetailsService(databaseUserDetailsService)
			.passwordEncoder(passwordEncoder);
	}
	
}