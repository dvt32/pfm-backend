package com.mse.personal.finance.service.mapper;

import org.mapstruct.Mapper;

import com.mse.personal.finance.db.entity.CategoryEntity;
import com.mse.personal.finance.model.Category;
import com.mse.personal.finance.model.request.CategoryRequest;

/**
 * Mapper between {@link Category} and its persistence representation {@link CategoryEntity}.
 *
 * @author D. Dimitrov
 */
@Mapper(componentModel = "spring")
public interface CategoryMapper {

	Category fromEntity(CategoryEntity categoryEntity);

	CategoryEntity toEntity(CategoryRequest createRequest);

}