package com.mse.personal.finance.db.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mse.personal.finance.db.entity.UserEntity;

/**
 * Persistence DAO for performing CRUD operations upon {@link UserEntity}.
 *
 * @author D. Dimitrov
 * @author dvt32
 */
@Repository
public interface UserRepository 
	extends JpaRepository<UserEntity, Long> 
{
	
	/**
	 * Searches for an {@link UserEntity} in the repository matching the provided username.
	 *
	 * @param name the username to search with
	 * @return the found user or {@link Optional#empty()}
	 */
	Optional<UserEntity> findByName(String name);

	/**
	 * Searches for an {@link UserEntity} in the repository matching the provided username.
	 *
	 * @param email the email to search with
	 * @return the found user or {@link Optional#empty()}
	 */
	Optional<UserEntity> findByEmail(String email);

	boolean existsByEmail(String email);
	
	void deleteByEmail(String email);
	
}