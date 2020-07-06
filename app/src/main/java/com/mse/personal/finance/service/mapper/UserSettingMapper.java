package com.mse.personal.finance.service.mapper;

import org.mapstruct.Mapper;

import com.mse.personal.finance.db.entity.UserSettingEntity;
import com.mse.personal.finance.model.UserSetting;
import com.mse.personal.finance.model.request.UserSettingRequest;

/**
 * Mapper between {@link UserSetting} and its persistence representation {@link UserSettingEntity}.
 *
 * @author D. Dimitrov
 */
@Mapper(componentModel = "spring")
public interface UserSettingMapper {

	UserSetting fromEntity(UserSettingEntity userSettingsEntity);

	UserSettingEntity toEntity(UserSettingRequest createRequest);

}