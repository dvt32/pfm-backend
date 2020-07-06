package com.mse.personal.finance.service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * This class is responsible for 
 * counting the number of logins the user made
 * from his current IP, setting the block time in minutes 
 * and also for blocking/unblocking the user's IP 
 * when the limits have been reached.
 * 
 * Based on implementation provided by Baeldung:
 * - https://www.baeldung.com/spring-security-block-brute-force-authentication-attempts
 * 
 * @author dvt32
 */
@Service
public class LoginLimitService {

	private final int MAX_NUMBER_OF_CONSECUTIVE_FAILED_LOGIN_ATTEMPTS = 3;
	private final int BLOCK_TIME_IN_MINUTES = 10;
	
	private LoadingCache<String, Integer> loginAttemptsCache;

	/**
	 * Constructor, which initializes 
	 * the login limit service's
	 * internal loading cache, which is used
	 * for blocking/unblocking IP addresses.
	 */
	public LoginLimitService() {
		super();

		loginAttemptsCache = CacheBuilder.newBuilder()
			.expireAfterWrite(BLOCK_TIME_IN_MINUTES, TimeUnit.MINUTES)
			.build(
				new CacheLoader<String, Integer>() {
					public Integer load(String key) {
						return 0;
					}
				}
			);
	}

	/**
	 * Resets the passed IP address'
	 * consecutive failed login attempt counter 
	 * (only if the IP is not currently blocked).
	 */
	public void loginSucceeded(String ipAddress) {
		if (!isBlockedIpAddress(ipAddress)) {
			loginAttemptsCache.invalidate(ipAddress);
		}
	}

	/**
	 * Increments the passed IP address'
	 * consecutive failed login attempt counter.
	 */
	public void loginFailed(String ipAddress) {
		int attempts = 0;

		try {
			attempts = loginAttemptsCache.get(ipAddress);
		} catch (ExecutionException e) {
			attempts = 0;
		}

		attempts++;

		loginAttemptsCache.put(ipAddress, attempts);
	}

	/**
	 * Checks if the passed IP address is currently blocked.
	 */
	public boolean isBlockedIpAddress(String ipAddress) {
		boolean isBlockedIpAddress = false;

		try {
			int currentNumberOfConsecutiveFailedLoginAttempts = loginAttemptsCache.get(ipAddress);
			isBlockedIpAddress = (currentNumberOfConsecutiveFailedLoginAttempts >= MAX_NUMBER_OF_CONSECUTIVE_FAILED_LOGIN_ATTEMPTS);
		}
		catch (ExecutionException e) {
			isBlockedIpAddress = false;
		}

		return isBlockedIpAddress;
	}

	/**
	 * Returns the login limit service's 
	 * block time limit (in minutes).
	 */
	public int getBlockTimeInMinutes() {
		return BLOCK_TIME_IN_MINUTES;
	}

	/**
	 * Extracts the client's IP address 
	 * from a HTTP request
	 * and returns it as a string.
	 */
	public String getClientIpAddressFromRequest(HttpServletRequest request) {
		String xfHeader = request.getHeader("X-Forwarded-For");

		if (xfHeader == null) {
			return request.getRemoteAddr();
		}

		return xfHeader.split(",")[0];
	}
	
	/**
	 * Returns the login limit service's 
	 * consecutive failed login attempt limit.
	 */
	public int getMaxNumberOfConsecutiveFailedLoginAttempts() {
		return MAX_NUMBER_OF_CONSECUTIVE_FAILED_LOGIN_ATTEMPTS;
	}
	
	/**
	 * Unblocks all IPs in the cache.
	 */
	public void unblockAllIps() {
		loginAttemptsCache.invalidateAll();
	}

}