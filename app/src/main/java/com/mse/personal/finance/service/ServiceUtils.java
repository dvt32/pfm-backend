package com.mse.personal.finance.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.mse.personal.finance.db.entity.AccountEntity;
import com.mse.personal.finance.db.entity.BaseEntity;
import com.mse.personal.finance.db.entity.CategoryEntity;
import com.mse.personal.finance.db.entity.ReportingPeriodEntity;
import com.mse.personal.finance.db.entity.TransactionEntity;
import com.mse.personal.finance.db.entity.UserEntity;
import com.mse.personal.finance.db.repository.UserRepository;

/**
 * This class provides helper methods 
 * used by the service classes.
 * 
 * @author dvt32
 */
@Component
public class ServiceUtils {
	
	private final UserRepository userRepository;
	
	@Autowired
	public ServiceUtils(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	/**
	 * Returns the username of the currently logged-in user (as a String).
	 */
	public String getUsernameOfCurrentlyLoggedInUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String usernameOfCurrentlyLoggedInUser = authentication.getName();
		return usernameOfCurrentlyLoggedInUser;
	}
	
	/**
	 * Returns the UserEntity object corresponding to the currently logged-in user.
	 */
	public UserEntity getCurrentlyLoggedInUserEntity() {
		String currentlyLoggedInUserEmail = getUsernameOfCurrentlyLoggedInUser();
		UserEntity currentlyLoggedInUserEntity = userRepository.findByEmail(currentlyLoggedInUserEmail).get();
		return currentlyLoggedInUserEntity;
	}
	
	/**
	 * Checks if a passed entity belongs to the currently logged-in user.
	 * 
	 * @return true if the entity belongs to the currently logged-in user, 
	 * 		   false if it doesn't or if the passed entity does not have an owner 
	 */
	public boolean belongsToCurrentlyLoggedInUser(BaseEntity entity) {
		boolean belongsToCurrentlyLoggedInUser = false;
		
		UserEntity currentlyLoggedInUserEntity = getCurrentlyLoggedInUserEntity();
		
		UserEntity entityOwner = null;
		if (entity instanceof AccountEntity) {
			entityOwner = ((AccountEntity) entity).getOwner();
		}
		else if (entity instanceof CategoryEntity) {
			entityOwner = ((CategoryEntity) entity).getOwner();
		}
		else if (entity instanceof ReportingPeriodEntity) {
			entityOwner = ((ReportingPeriodEntity) entity).getUser();
		}
		else if (entity instanceof TransactionEntity) {
			entityOwner = ((TransactionEntity) entity).getUser();
		}
		else {
			// If the passed entity is not an instance of one of the entity types above, 
			// then it does not have an owner and therefore the method returns false.
			return false;
		}
		
		belongsToCurrentlyLoggedInUser = entityOwner.equals(currentlyLoggedInUserEntity);
		
		return belongsToCurrentlyLoggedInUser;
	}

}