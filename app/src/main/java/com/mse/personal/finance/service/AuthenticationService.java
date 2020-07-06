package com.mse.personal.finance.service;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.mse.personal.finance.model.UserAuthenticationDetails;
import com.mse.personal.finance.model.UserAuthenticationToken;
import com.mse.personal.finance.model.request.UserLoginRequest;
import com.mse.personal.finance.security.jwt.JwtSessionManager;
import com.mse.personal.finance.security.jwt.JwtTokenProvider;

/**
 * Service for authentication 
 * backed by {@link JwtTokenProvider} 
 * and {@link JwtSessionManager}.
 *
 * @author T. Dossev
 * @author dvt32
 */
@Service
public class AuthenticationService {

	private final JwtTokenProvider jwtTokenProvider;
	private final JwtSessionManager jwtSessionManager;
	private final AuthenticationManager authenticationManager;

	@Autowired
	public AuthenticationService(
		JwtTokenProvider jwtTokenProvider, 
		JwtSessionManager jwtSessionManager,
		AuthenticationManager authenticationManager) 
	{
		this.jwtTokenProvider = jwtTokenProvider;
		this.jwtSessionManager = jwtSessionManager;
		this.authenticationManager = authenticationManager;
	}

	public UserAuthenticationToken getUserAuthenticationTokenForUser(UserLoginRequest user) {
		Authentication authentication = authenticateInternally(user);
		UserAuthenticationDetails userAuthenticationDetails = (UserAuthenticationDetails) authentication.getPrincipal();
		UserAuthenticationToken userAuthenticationToken = buildTokenFromDetails(userAuthenticationDetails);
		String tokenString = userAuthenticationToken.getTokenString();
		jwtSessionManager.updateSession(tokenString);
		return userAuthenticationToken;
	}
	
	private Authentication authenticateInternally(UserLoginRequest user) {
		String username = user.getUsername();
		String password = user.getPassword();
		UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(username, password);
		Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
		return authentication;
	}
	
	private UserAuthenticationToken buildTokenFromDetails(UserAuthenticationDetails userAuthenticationDetails) {
		String tokenUsername = userAuthenticationDetails.getUsername();
		String tokenDisplayName = userAuthenticationDetails.getDisplayName();
		String tokenString = jwtTokenProvider.generateTokenStringFromDetails(userAuthenticationDetails);
		UserAuthenticationToken userAuthenticationToken = new UserAuthenticationToken(tokenUsername, tokenDisplayName, tokenString);
		return userAuthenticationToken;
	}
	
	/**
	 * Checks the HTTP request's "Authorization" header for a valid JWT token and removes it.
	 */
	public void removeUserAuthenticationTokenFromRequest(HttpServletRequest request) {
		jwtTokenProvider.resolveToken(request).ifPresent(jwtSessionManager::invalidateSession);
	}

}