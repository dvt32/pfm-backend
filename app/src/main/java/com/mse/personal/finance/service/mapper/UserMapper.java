package com.mse.personal.finance.service.mapper;

import org.mapstruct.Mapper;

import com.mse.personal.finance.db.entity.UserEntity;
import com.mse.personal.finance.model.User;
import com.mse.personal.finance.model.request.UserCreateRequest;
import com.mse.personal.finance.model.request.UserUpdateRequest;

/**
 * Mapper between {@link User} and its persistence representation {@link UserEntity}.
 *
 * @author D. Dimitrov
 * @author dvt32
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

	User fromEntity(UserEntity userEntity);

	UserEntity toEntity(UserCreateRequest createRequest);
	
	UserEntity toEntity(UserUpdateRequest updateRequest);

}