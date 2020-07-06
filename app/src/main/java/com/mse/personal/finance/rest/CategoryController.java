package com.mse.personal.finance.rest;

import java.util.Date;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mse.personal.finance.model.Category;
import com.mse.personal.finance.model.CategoryType;
import com.mse.personal.finance.model.request.CategoryLimitUpdateRequest;
import com.mse.personal.finance.model.request.CategoryRequest;
import com.mse.personal.finance.rest.exception.CategoryNotFoundException;
import com.mse.personal.finance.rest.exception.InvalidDataException;
import com.mse.personal.finance.rest.exception.NameAlreadyExistsException;
import com.mse.personal.finance.rest.exception.UserDoesNotOwnResourceException;
import com.mse.personal.finance.service.CategoryService;

/**
 * REST controller for CRUD operations 
 * upon {@link Category} categories
 * for the currently logged-in user.
 * 
 * @author dvt32
 */
@RestController
@RequestMapping(path = "/categories")
public class CategoryController {
	
	private final CategoryService categoryService;

	@Autowired
	public CategoryController(CategoryService categoryService) {
		this.categoryService = categoryService;
	}
	
	/**
	 * Returns all categories' data as a JSON array.
	 * 
	 * @return the list of categories (empty if no categories available)
	 */
	@GetMapping
	public List<Category> getAllCategories() {
		List<Category> allCategories = categoryService.getAllNonSystemCategories();
		return allCategories;
	}
	
	/**
	 * Returns all categories of the specified type's data as a JSON array.
	 * 
	 * @return the list of categories (empty if no categories available)
	 */
	@GetMapping(params = "type")
	public List<Category> getAllCategoriesByType(@RequestParam CategoryType type) {
		List<Category> allCategoriesByType = categoryService.getAllNonSystemCategoriesByType(type);
		return allCategoriesByType;
	}
	
	/**
	 * Returns the total current period sum for all categories of the specified type.
	 * 
	 * @return the total current period sum
	 */
	@GetMapping("/total-current-period-sum")
	public Double getTotalCurrentPeriodSumOfCategoriesByType(@RequestParam CategoryType type) {
		Double totalCurrentPeriodSum = categoryService.getTotalCurrentPeriodSumOfCategoriesByType(type);
		return totalCurrentPeriodSum;
	}
	
	/**
	 * Returns a specific category's data as a JSON object
	 * 
	 * @param id The id of the category to be retrieved
	 * @return the category's data
	 */
	@GetMapping("/{id}")
	public Category getCategoryById(@PathVariable Long id) {
		Category category = null;
		
		try {
			category = categoryService.getCategoryById(id);
		} 
		catch (CategoryNotFoundException e) {
			throw new CategoryNotFoundException(id);
		}
		catch (UserDoesNotOwnResourceException e) {
			throw new UserDoesNotOwnResourceException();
		}
		
		return category;
	}
	
	/**
	 * Returns the total added sum from transactions between two dates (inclusive) to a specified category.
	 * 
	 * @return the total added sum
	 */
	@GetMapping("/{id}/added-sum")
	public Double getTotalAddedSumBetweenDatesById(
		@PathVariable Long id,
		@RequestParam @DateTimeFormat(pattern = "dd.MM.yyyy") Date startDate,
		@RequestParam @DateTimeFormat(pattern = "dd.MM.yyyy") Date endDate) 
	{
		Double totalAddedSum = null;
		
		try {
			totalAddedSum = categoryService.getTotalAddedSumBetweenDatesById(id, startDate, endDate);
		} 
		catch (CategoryNotFoundException e) {
			throw new CategoryNotFoundException(id);
		}
		catch (UserDoesNotOwnResourceException e) {
			throw new UserDoesNotOwnResourceException();
		}
		
		return totalAddedSum;
	}
	
	/**
	 * This method creates a category by passing the account's data in a POST request's body
	 * and returns the newly created category's data as a JSON object.
	 * 
	 * The data is validated before the category is stored in the database.
	 * 
	 * @param categoryRequest An object containing the category-to-be-created's data
	 * @param bindingResult The validator of the passed data
	 * 
	 * @return the created category's data
	 */
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Category createCategory(
		@RequestBody @Valid CategoryRequest categoryRequest, 
		BindingResult bindingResult)
	{
		if (bindingResult.hasErrors()) {
			String errorMessage = ControllerUtils.getErrorMessageFromBindingResult(bindingResult);
			throw new InvalidDataException(errorMessage);
		}
		
		Category createdCategory = null;
		try {
			createdCategory = categoryService.createNewCategory(categoryRequest);
		} 
		catch (NameAlreadyExistsException e) {
			String exceptionMessage = e.getMessage();
			throw new NameAlreadyExistsException(exceptionMessage);
		}
		
		return createdCategory;
	}
	
	/**
	 * This method creates example categories for the currently logged-in user
	 * and returns the created categories' data as a JSON array.
	 */
	@PostMapping("/create-example-categories")
	@ResponseStatus(HttpStatus.CREATED)
	public List<Category> createExampleCategoriesForCurrentlyLoggedInUser() {
		List<Category> createdCategories = categoryService.createExampleCategoriesForCurrentlyLoggedInUser();
		return createdCategories;
	}
	
	/**
	 * This method updates an existing category in the database 
	 * by passing the updated category's data in a POST request's body
	 * and returns the updated category's data as a JSON object.
	 * 
	 * The data is validated before the category is updated in the database.
	 * 
	 * @param id The category-to-be-updated's ID
	 * @param categoryRequest An object containing the category-to-be-updated's data
	 * @param bindingResult The validator of the passed data
	 * 
	 * @return the updated category's data
	 */
	@PutMapping("/{id}")
	public Category updateCategoryById(
		@PathVariable Long id, 
		@RequestBody @Valid CategoryRequest categoryRequest, 
		BindingResult bindingResult) 
	{
		if (bindingResult.hasErrors()) {
			String errorMessageFromBindingResult = ControllerUtils.getErrorMessageFromBindingResult(bindingResult);
			throw new InvalidDataException(errorMessageFromBindingResult);
		}
		
		Category updatedCategory = null;
		try {
			updatedCategory = categoryService.updateCategoryById(id, categoryRequest);
		}
		catch (CategoryNotFoundException e) {
			throw new CategoryNotFoundException(id);
		}
		catch (UserDoesNotOwnResourceException e) {
			throw new UserDoesNotOwnResourceException();
		}
		catch (NameAlreadyExistsException e) {
			String exceptionMessage = e.getMessage();
			throw new NameAlreadyExistsException(exceptionMessage);
		}
		
		return updatedCategory;
	}
	
	/**
	 * This method sets an existing category's limit in the database 
	 * by passing an ID and the limit (in the request body)
	 * and returns the updated category's data as a JSON object.
	 * 
	 * @param id The ID of the category
	 * @param categoryLimitUpdateRequest An object containing the category's limit (amount or percentage)
	 * @return the updated category's data
	 */
	@PatchMapping("/{id}/limit")
	public Category setCategoryLimitById(
		@PathVariable Long id, 
		@RequestBody @Valid CategoryLimitUpdateRequest categoryLimitUpdateRequest) 
	{
		Category categoryWithSetLimit = null;
		
		try {
			categoryWithSetLimit = categoryService.setCategoryLimitById(id, categoryLimitUpdateRequest);
		} 
		catch (CategoryNotFoundException e) {
			throw new CategoryNotFoundException(id);
		}
		catch (UserDoesNotOwnResourceException e) {
			throw new UserDoesNotOwnResourceException();
		}
		
		return categoryWithSetLimit;
	}
	
	/**
	 * This method deletes an existing category in the database by passing an ID
	 * and returns the deleted category's data as a JSON object.
	 * 
	 * If the category does not exist, an exception is thrown.
	 * 
	 * @param id The ID of the category
	 * @return the deleted category's data
	 */
	@DeleteMapping("/{id}")
	public Category deleteCategoryById(@PathVariable Long id) {
		Category deletedCategory = null;
		
		try {
			deletedCategory = categoryService.deleteCategoryById(id);
		} 
		catch (CategoryNotFoundException e) {
			throw new CategoryNotFoundException(id);
		}
		catch (UserDoesNotOwnResourceException e) {
			throw new UserDoesNotOwnResourceException();
		}
		
		return deletedCategory;
	}

}