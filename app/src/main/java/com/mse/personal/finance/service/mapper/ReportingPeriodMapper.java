package com.mse.personal.finance.service.mapper;

import com.mse.personal.finance.db.entity.ReportingPeriodEntity;
import com.mse.personal.finance.model.ReportingPeriod;
import com.mse.personal.finance.model.request.ReportingPeriodRequest;
import org.mapstruct.Mapper;

/**
 * Mapper between {@link ReportingPeriod} and its persistence representation {@link ReportingPeriodEntity}.
 *
 * @author D. Dimitrov
 */
@Mapper(componentModel = "spring")
public interface ReportingPeriodMapper {

	ReportingPeriod fromEntity(ReportingPeriodEntity reportingPeriodsEntity);

	ReportingPeriodEntity toEntity(ReportingPeriodRequest createRequest);

}