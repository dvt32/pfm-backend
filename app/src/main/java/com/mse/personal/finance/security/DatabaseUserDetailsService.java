package com.mse.personal.finance.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.mse.personal.finance.db.entity.UserEntity;
import com.mse.personal.finance.db.repository.UserRepository;
import com.mse.personal.finance.model.UserAuthenticationDetails;

/**
 * This class implements a custom
 * database-backed UserDetailsService.
 * 
 * It retrieves a user stored in the database 
 * and gives this user authorities
 * to access the system's resources.
 * 
 * @author dvt32
 */
@Service
public class DatabaseUserDetailsService 
	implements UserDetailsService 
{

	private final UserRepository userRepository;
	
	@Autowired
	public DatabaseUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String email) {
		boolean existsByEmail = userRepository.existsByEmail(email);
		if (!existsByEmail) {
			throw new UsernameNotFoundException(email);
		}
		
		UserEntity userEntity = userRepository.findByEmail(email).get();
		String username = userEntity.getEmail();
		String password = userEntity.getPassword();
		String displayName = userEntity.getName();
		
		UserAuthenticationDetails userAuthenticationDetails = new UserAuthenticationDetails(username, password, displayName);
		
		return userAuthenticationDetails;
	}
	
}