package com.mse.personal.finance.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mse.personal.finance.db.entity.UserEntity;
import com.mse.personal.finance.db.entity.UserSettingEntity;
import com.mse.personal.finance.model.UserSettingKey;

/**
 * Persistence DAO for performing CRUD operations upon {@link UserSettingEntity}.
 *
 * @author D. Dimitrov
 * @author dvt32
 */
@Repository
public interface UserSettingRepository 
	extends JpaRepository<UserSettingEntity, Long> 
{
	
	List<UserSettingEntity> findAllByUser(UserEntity user);
	
	UserSettingEntity findByKeyAndUser(UserSettingKey key, UserEntity user);
	
	boolean existsByKeyAndUser(UserSettingKey key, UserEntity user);
	
}