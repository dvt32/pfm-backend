package com.mse.personal.finance.security;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

import com.mse.personal.finance.rest.exception.IpAddressBlockedException;
import com.mse.personal.finance.service.LoginLimitService;

/**
 * This class listens for a login failure
 * and informs the login limit service about the event.
 * 
 * @author dvt32
 */
@Component
public class AuthenticationFailureListener 
  implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> 
{
	
    private final LoginLimitService loginLimitService;
    private final HttpServletRequest request;
    
    @Autowired
	public AuthenticationFailureListener(
		LoginLimitService loginLimitService, 
		HttpServletRequest request) 
    {
		this.loginLimitService = loginLimitService;
		this.request = request;
	}
	
    /**
     * This method is called when a user tries 
     * to login with invalid credentials.
     * 
     * If the max consecutive failed login attempts 
     * has been reached, an exception is thrown.
     * 
     * Otherwise the login limit service increases
     * the number of consecutive failed login attempts
     * for the current IP address.
     */
	public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent e) {
		String clientIpAddress = loginLimitService.getClientIpAddressFromRequest(request);
		boolean isBlockedIpAddress = loginLimitService.isBlockedIpAddress(clientIpAddress);
		int blockTimeInMinutes = loginLimitService.getBlockTimeInMinutes();
		
		if (isBlockedIpAddress) {
			throw new IpAddressBlockedException(
				"IP blocked for " + blockTimeInMinutes + " minutes (max consecutive invalid login attempts reached)!"
			);
		}
		
		loginLimitService.loginFailed(clientIpAddress);
    }
	
}