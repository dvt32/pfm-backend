package com.mse.personal.finance.security;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import com.mse.personal.finance.rest.exception.IpAddressBlockedException;
import com.mse.personal.finance.service.LoginLimitService;

/**
 * This class listens for a login success
 * and informs the login limit service about the event.
 * 
 * @author dvt32
 */
@Component
public class AuthenticationSuccessListener 
  implements ApplicationListener<AuthenticationSuccessEvent> 
{

    private final LoginLimitService loginLimitService;
	private final HttpServletRequest request;
    
    @Autowired
	public AuthenticationSuccessListener(
		LoginLimitService loginLimitService, 
		HttpServletRequest request) 
    {
		this.loginLimitService = loginLimitService;
		this.request = request;
	}
	
    /**
     * This method is called when a user tries 
     * to login with valid credentials.
     * 
     * If the user's IP is blocked,
     * his login attempt is ignored
     * and an exception is thrown.
     * 
     * Otherwise the login limit service
     * resets the consecutive failed login attempt counter
     * for the current IP address.
     */
	public void onApplicationEvent(AuthenticationSuccessEvent e) {
		String clientIpAddress = loginLimitService.getClientIpAddressFromRequest(request);
		boolean isBlockedIpAddress = loginLimitService.isBlockedIpAddress(clientIpAddress);
		int blockTimeInMinutes = loginLimitService.getBlockTimeInMinutes();
		
		if (isBlockedIpAddress) {
			throw new IpAddressBlockedException(
				"IP blocked for " + blockTimeInMinutes + " minutes (max consecutive invalid login attempts reached)!"
			);
		}
		
        loginLimitService.loginSucceeded(clientIpAddress);      
    }
	
}