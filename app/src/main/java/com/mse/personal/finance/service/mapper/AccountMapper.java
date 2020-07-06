package com.mse.personal.finance.service.mapper;

import com.mse.personal.finance.db.entity.AccountEntity;
import com.mse.personal.finance.model.Account;
import com.mse.personal.finance.model.request.AccountRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

/**
 * Mapper between {@link Account} and its persistence representation {@link AccountEntity}.
 *
 * @author D. Dimitrov
 */
@Mapper(componentModel = "spring")
public interface AccountMapper {

	Account fromEntity(AccountEntity accountEntity);

	AccountEntity toEntity(AccountRequest createRequest);

}