package com.mse.personal.finance.security.jwt;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;

/**
 * Filter that uses {@link JwtTokenProvider} and {@link JwtSessionManager} to validate JWTs 
 * and sets proper {@link org.springframework.security.core.context.SecurityContext} for requests.
 *
 * @author T. Dossev
 */
public class JwtTokenFilter 
	extends OncePerRequestFilter 
{

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private final JwtTokenProvider jwtTokenProvider;
	private final JwtSessionManager jwtSessionManager;

	public JwtTokenFilter(JwtTokenProvider jwtTokenProvider, JwtSessionManager jwtSessionManager) {
		this.jwtTokenProvider = jwtTokenProvider;
		this.jwtSessionManager = jwtSessionManager;
	}

	@Override
	protected void doFilterInternal(
		HttpServletRequest request, 
		HttpServletResponse response, 
		FilterChain filterChain
	)
		throws ServletException, IOException 
	{
		jwtTokenProvider.resolveToken(request).ifPresent(this::validateToken);
		filterChain.doFilter(request, response);
	}

	private void validateToken(String tokenString) {
		try {
			if (jwtSessionManager.isExpired(tokenString)) {
				jwtSessionManager.invalidateSession(tokenString);
			} 
			else {
				Authentication authentication = jwtTokenProvider.resolveAuthenticationFromTokenString(tokenString);
				jwtSessionManager.updateSession(tokenString);
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		} 
		catch (JwtException e) {
			SecurityContextHolder.clearContext();
			LOGGER.error("Invalid token provided", e);
		}
	}

}