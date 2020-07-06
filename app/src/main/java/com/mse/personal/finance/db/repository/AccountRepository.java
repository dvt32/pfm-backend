package com.mse.personal.finance.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mse.personal.finance.db.entity.AccountEntity;
import com.mse.personal.finance.db.entity.UserEntity;
import com.mse.personal.finance.model.AccountType;

/**
 * Persistence DAO for performing CRUD operations upon {@link AccountEntity}.
 *
 * @author D. Dimitrov
 * @author dvt32
 */
@Repository
public interface AccountRepository 
	extends JpaRepository<AccountEntity, Long> 
{
	
	@Query("SELECT a FROM AccountEntity a WHERE a.type != 'DELETED' AND owner_id = :ownerId")
	List<AccountEntity> findAllNonDeletedAccountsByOwner(@Param("ownerId") Long ownerId);
	
	List<AccountEntity> findAllByTypeAndOwner(AccountType type, UserEntity owner);
	
	boolean existsByNameAndOwner(String name, UserEntity owner);
	
	@Query(value = "SELECT sum(balance) FROM AccountEntity WHERE type = 'ACTIVATED' AND owner_id = :ownerId")
    Double getTotalBalanceOfActivatedAccounts(@Param("ownerId") Long ownerId);
	
}