package com.mse.personal.finance.security.jwt;

import java.util.Date;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.mse.personal.finance.model.UserAuthenticationDetails;
import com.mse.personal.finance.security.DatabaseUserDetailsService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

/**
 * Provides means to work with JWTs (JSON Web Tokens).
 * 
 * JWTs are signed with a secret key.
 * The secret key is generated on startup 
 * using the HMAC SHA-256 algorithm.
 *
 * @author T. Dossev
 * @author dvt32
 */
@Component
public class JwtTokenProvider {

	private static final String AUTHORIZATION_TYPE = "Bearer ";
	private static final String DISPLAY_NAME_CLAIM = "displayName";
	
	private final DatabaseUserDetailsService databaseUserDetailsService;
	
	@Value("${security.jwt.issuer:personal}")
	
	private String issuer;
	
	private SecretKey secretKey;
	
	@Autowired
	public JwtTokenProvider(DatabaseUserDetailsService databaseUserDetailsService) {
		this.databaseUserDetailsService = databaseUserDetailsService;
	}
	
	@PostConstruct
	protected void init() {
		secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
	}
	
	private String getUsername(String tokenString) {
		return parseTokenString(tokenString).getSubject();
	}
	
	private Claims parseTokenString(String tokenString) {
		Claims claims = Jwts.parser()
			.setSigningKey(secretKey)
			.requireIssuer(issuer)
			.parseClaimsJws(tokenString)
			.getBody();
		
		return claims;
	}

	/**
	 * Resolves token from given {@link HttpServletRequest}. 
	 * 
	 * It looks at the "Authorization" HTTP header
	 * with authorization type "Bearer".
	 *
	 * @param request the HTTP request
	 * @return optional containing either the token or empty
	 */
	public Optional<String> resolveToken(HttpServletRequest request) {
		String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
		
		if ( !StringUtils.isEmpty(authorization) && authorization.startsWith(AUTHORIZATION_TYPE) ) {
			return Optional.of( authorization.substring(AUTHORIZATION_TYPE.length()) );
		}
		
		return Optional.empty();
	}
	
	/**
	 * Generates a JWT string based on given {@link UserAuthenticationDetails}.
	 *
	 * @param userAuthenticationDetails the user authentication details
	 * @return a JWT string
	 */
	public String generateTokenStringFromDetails(UserAuthenticationDetails userAuthenticationDetails) {
		Claims claims = buildClaimsFromDetails(userAuthenticationDetails);
		
		String tokenString = Jwts.builder()
			.setClaims(claims)
			.setIssuedAt(new Date())
			.setIssuer(issuer)
			.signWith(secretKey)
			.compact();
		
		return tokenString;
	}
	
	private Claims buildClaimsFromDetails(UserAuthenticationDetails userAuthenticationDetails) {
		String username = userAuthenticationDetails.getUsername();
		Claims claims = Jwts.claims().setSubject(username);
		String displayName = userAuthenticationDetails.getDisplayName();
		claims.put(DISPLAY_NAME_CLAIM, displayName);
		return claims;
	}
	
	/**
	 * Resolves the user contained in the given JWT string.
	 *
	 * @param tokenString the JWT string
	 * @return the authentication
	 */
	public Authentication resolveAuthenticationFromTokenString(String tokenString) {
		String username = getUsername(tokenString);
		
		UserDetails userDetails = databaseUserDetailsService.loadUserByUsername(username);

		return 
			new UsernamePasswordAuthenticationToken(
				userDetails, 
				userDetails.getPassword(),
				userDetails.getAuthorities()
			);
	}

}