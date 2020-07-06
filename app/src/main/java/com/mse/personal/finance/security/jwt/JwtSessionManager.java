package com.mse.personal.finance.security.jwt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Session manager for JWTs.
 *
 * @author T. Dossev
 */
@Component
public class JwtSessionManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Value("${security.jwt.expiration:#{30*60}}")
	private int sessionExpirationTime;

	private final Map<String, LocalDateTime> sessionMap = new ConcurrentHashMap<>();

	/**
	 * Checks if given JWT has expired.
	 *
	 * @param tokenString the JWT string
	 * @return true if expired, otherwise false
	 */
	public boolean isExpired(String tokenString) {
		LocalDateTime lastAccessTime = sessionMap.get(tokenString);
		
		if (lastAccessTime == null) {
			return true;
		}
		
		if (isExpired(lastAccessTime)) {
			invalidateSession(tokenString);
			return true;
		}
		
		return false;
	}

	private boolean isExpired(LocalDateTime lastAccessTime) {
		return lastAccessTime.plusSeconds(sessionExpirationTime).isBefore( LocalDateTime.now() );
	}

	/**
	 * Adds new session or updates existing session's last accessed time.
	 *
	 * @param tokenString the JWT string
	 */
	public void updateSession(String tokenString) {
		sessionMap.put( tokenString, LocalDateTime.now() );
	}

	/**
	 * Invalidates the session with given JWT.
	 *
	 * @param tokenString the JWT string
	 */
	public void invalidateSession(String tokenString) {
		sessionMap.remove(tokenString);
	}

	/**
	 * Schedules function which runs on specified interval and prunes expired sessions.
	 */
	@Scheduled(fixedDelay = 600000)
	public void pruneExpiredSessions() {
		LOGGER.debug("Prune expired sessions");
		sessionMap.entrySet().removeIf( sessionEntry -> isExpired( sessionEntry.getValue() ) );
	}

}