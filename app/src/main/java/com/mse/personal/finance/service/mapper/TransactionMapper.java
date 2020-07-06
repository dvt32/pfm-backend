package com.mse.personal.finance.service.mapper;

import com.mse.personal.finance.db.entity.TransactionEntity;
import com.mse.personal.finance.model.Transaction;
import com.mse.personal.finance.model.request.TransactionRequest;
import org.mapstruct.Mapper;

/**
 * Mapper between {@link Transaction} and its persistence representation {@link TransactionEntity}.
 *
 * @author D. Dimitrov
 */
@Mapper(componentModel = "spring")
public interface TransactionMapper {

	Transaction fromEntity(TransactionEntity transactionEntity);

	TransactionEntity toEntity(TransactionRequest createRequest);

}