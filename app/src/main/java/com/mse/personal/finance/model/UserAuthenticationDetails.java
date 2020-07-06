package com.mse.personal.finance.model;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * DTO for user details that can be used 
 * for authentication and authorization.
 * 
 * The class implements the UserDetails interface
 * and is intended to be used by a UserDetailsService implementation
 * to authorize an existing user in the database.
 *
 * @author dvt32
 */
@SuppressWarnings("serial")
public class UserAuthenticationDetails
	implements UserDetails
{
	private String username;
	private String password;
	private String displayName;
	
	public UserAuthenticationDetails(String username, String password, String displayName) {
		this.username = username;
		this.password = password;
		this.displayName = displayName;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public String getPassword() {
		return password;
	}
	
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.singletonList( new SimpleGrantedAuthority("ROLE_USER") );
	}
	
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

}