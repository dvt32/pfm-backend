package com.mse.personal.finance.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mse.personal.finance.db.entity.CategoryEntity;
import com.mse.personal.finance.db.entity.UserEntity;
import com.mse.personal.finance.db.repository.CategoryRepository;
import com.mse.personal.finance.db.repository.TransactionRepository;
import com.mse.personal.finance.model.Category;
import com.mse.personal.finance.model.CategoryType;
import com.mse.personal.finance.model.request.CategoryLimitUpdateRequest;
import com.mse.personal.finance.model.request.CategoryRequest;
import com.mse.personal.finance.rest.exception.CategoryNotFoundException;
import com.mse.personal.finance.rest.exception.NameAlreadyExistsException;
import com.mse.personal.finance.rest.exception.UserDoesNotOwnResourceException;
import com.mse.personal.finance.service.mapper.CategoryMapper;

/**
 * Service for managing {@link Category} categories
 * of the currently logged-in user.
 * 
 * The entities are retrieved from the database
 * and then mapped to DTOs, which will be used by the REST controller.
 * 
 * Likewise, DTOs sent from the REST controller 
 * are mapped to entities and then added to the database.
 * 
 * @author dvt32
 */
@Service
public class CategoryService {

	private final CategoryRepository categoryRepository;
	private final CategoryMapper categoryMapper;
	private final ServiceUtils serviceUtils;
	private final TransactionRepository transactionRepository;
	
	public static final String SYSTEM_INCOME_CATEGORY_NAME = "SYS_INCOME";
	public static final String SYSTEM_EXPENSES_CATEGORY_NAME = "SYS_EXPENSES";
	
	private static final CategoryEntity[] EXAMPLE_CATEGORIES = {
		new CategoryEntity("Храна", CategoryType.EXPENSES, 0.0d, null, null),
		new CategoryEntity("Комунални сметки", CategoryType.EXPENSES, 0.0d, null, null),
		new CategoryEntity("Кола", CategoryType.EXPENSES, 0.0d, null, null),
		new CategoryEntity("Кредит", CategoryType.EXPENSES, 0.0d, null, null),
		new CategoryEntity("Наем", CategoryType.EXPENSES, 0.0d, null, null),
		new CategoryEntity("Застраховка", CategoryType.EXPENSES, 0.0d, null, null)
	};

	@Autowired
	public CategoryService(
		CategoryRepository categoryRepository, 
		CategoryMapper categoryMapper,
		ServiceUtils serviceUtils,
		TransactionRepository transactionRepository) 
	{
		this.categoryRepository = categoryRepository;
		this.categoryMapper = categoryMapper;
		this.serviceUtils = serviceUtils;
		this.transactionRepository = transactionRepository;
	}

	/**
	 * Returns all existing non-system user categories' data as a list of categories.
	 */
	public List<Category> getAllNonSystemCategories() {
		UserEntity currentlyLoggedInUserEntity = serviceUtils.getCurrentlyLoggedInUserEntity();
		
		List<CategoryEntity> allCategoryEntities = categoryRepository.findAllByOwner(currentlyLoggedInUserEntity);
		
		List<Category> allCategoryDTOs = allCategoryEntities.stream()
			.filter( categoryEntity -> !isSystemCategory(categoryEntity) )
			.map( categoryEntity -> categoryMapper.fromEntity(categoryEntity) )
			.collect( Collectors.toList() );
		
		return allCategoryDTOs; 
	}
	
	/**
	 * Returns all existing non-system user categories 
	 * of a specified type's data as a list of categories.
	 */
	public List<Category> getAllNonSystemCategoriesByType(CategoryType type) {
		UserEntity currentlyLoggedInUserEntity = serviceUtils.getCurrentlyLoggedInUserEntity();
		
		List<CategoryEntity> allCategoryEntitiesByType = categoryRepository.findAllByTypeAndOwner(type, currentlyLoggedInUserEntity);
		
		List<Category> allCategoryDTOsByType = allCategoryEntitiesByType.stream()
			.filter( categoryEntity -> !isSystemCategory(categoryEntity) )
			.map( categoryEntity -> categoryMapper.fromEntity(categoryEntity) )
			.collect( Collectors.toList() );
		
		return allCategoryDTOsByType; 
	}
	
	/**
	 * Returns the total current period sum 
	 * for all categories of the specified type.
	 * 
	 * Note: System categories' sum is not included in the total sum.
	 */
	public Double getTotalCurrentPeriodSumOfCategoriesByType(CategoryType type) {
		Double totalCurrentPeriodSum = null;
		
		Long ownerId = serviceUtils.getCurrentlyLoggedInUserEntity().getId();
		if (type == CategoryType.INCOME) {
			totalCurrentPeriodSum = categoryRepository.getTotalCurrentPeriodSumOfIncomeCategories(ownerId);
		}
		else if (type == CategoryType.EXPENSES) {
			totalCurrentPeriodSum = categoryRepository.getTotalCurrentPeriodSumOfExpenseCategories(ownerId);
		}
		
		return totalCurrentPeriodSum;
	}
	
	/**
	 * Returns an existing category's data.
	 */
	public Category getCategoryById(Long id) 
		throws CategoryNotFoundException, UserDoesNotOwnResourceException
	{
		boolean existsById = categoryRepository.existsById(id);
		if (!existsById) {
			throw new CategoryNotFoundException(id);
		}
		
		CategoryEntity categoryEntity = categoryRepository.findById(id).get();
		boolean belongsToCurrentlyLoggedInUser = serviceUtils.belongsToCurrentlyLoggedInUser(categoryEntity);
		if (!belongsToCurrentlyLoggedInUser) {
			throw new UserDoesNotOwnResourceException();
		}
		
		Category categoryDTO = categoryMapper.fromEntity(categoryEntity);
		
		return categoryDTO;
	}
	
	/**
	 * Returns an existing category's total added sum from transactions between two dates (inclusive).
	 */
	public Double getTotalAddedSumBetweenDatesById(Long id, Date startDate, Date endDate)
		throws CategoryNotFoundException, UserDoesNotOwnResourceException
	{
		boolean existsById = categoryRepository.existsById(id);
		if (!existsById) {
			throw new CategoryNotFoundException(id);
		}
		
		CategoryEntity categoryEntity = categoryRepository.findById(id).get();
		boolean belongsToCurrentlyLoggedInUser = serviceUtils.belongsToCurrentlyLoggedInUser(categoryEntity);
		if (!belongsToCurrentlyLoggedInUser) {
			throw new UserDoesNotOwnResourceException();
		}
		
		Long loggedInUserId = serviceUtils.getCurrentlyLoggedInUserEntity().getId();
		CategoryType categoryType = categoryEntity.getType();
		Double totalAddedSum = null;
		if (categoryType == CategoryType.INCOME) {
			totalAddedSum = transactionRepository.getTotalAddedSumBetweenDatesForIncomeCategoryById(loggedInUserId, startDate, endDate, id);
		}
		else if (categoryType == CategoryType.EXPENSES) {
			totalAddedSum = transactionRepository.getTotalAddedSumBetweenDatesForExpenseCategoryById(loggedInUserId, startDate, endDate, id);
		}
		
		return totalAddedSum;
	}
	
	/**
	 * Creates a new category from a category DTO, 
	 * which contains the category's data
	 * and returns the newly created category's data.
	 */
	public Category createNewCategory(CategoryRequest categoryRequest)
		throws NameAlreadyExistsException 
	{
		CategoryEntity categoryEntity = categoryMapper.toEntity(categoryRequest);
		
		// Owner is not included in the request DTO and must be set manually.
		// It is retrieved from the currently logged-in user's details.
		UserEntity owner = serviceUtils.getCurrentlyLoggedInUserEntity();
		categoryEntity.setOwner(owner);
		
		String categoryName = categoryRequest.getName();
		boolean nameAlreadyExists = categoryRepository.existsByNameAndOwner(categoryName, owner);
		if (nameAlreadyExists) {
			throw new NameAlreadyExistsException("Category with this name already exists for the current user!");
		}
		
		categoryEntity = categoryRepository.save(categoryEntity);
		
		Category categoryDTO = categoryMapper.fromEntity(categoryEntity);
		
		return categoryDTO;
	}
	
	/**
	 * Updates an existing category with a specified ID
	 * and returns the updated category's data.
	 */
	public Category updateCategoryById(Long id, CategoryRequest categoryRequest) 
		throws CategoryNotFoundException, UserDoesNotOwnResourceException
	{
		boolean existsById = categoryRepository.existsById(id);
		if (!existsById) {
			throw new CategoryNotFoundException(id);
		}
		
		CategoryEntity categoryEntity = categoryRepository.findById(id).get();
		boolean belongsToCurrentlyLoggedInUser = serviceUtils.belongsToCurrentlyLoggedInUser(categoryEntity);
		if (!belongsToCurrentlyLoggedInUser) {
			throw new UserDoesNotOwnResourceException();
		}
		
		CategoryEntity updatedCategoryEntity = categoryMapper.toEntity(categoryRequest);
		
		// ID and owner are not included in the request DTO and must be set manually.
		// ID is retrieved from the HTTP request's passed URL.
		// Owner is retrieved from the already existing entity.
		updatedCategoryEntity.setId(id);
		UserEntity categoryEntityOwner = categoryEntity.getOwner();
		updatedCategoryEntity.setOwner(categoryEntityOwner);
		
		String oldCategoryName = categoryEntity.getName();
		String newCategoryName = updatedCategoryEntity.getName();
		boolean nameChanged = !oldCategoryName.equals(newCategoryName);
		boolean nameAlreadyExists = categoryRepository.existsByNameAndOwner(newCategoryName, categoryEntityOwner);
		if (nameChanged && nameAlreadyExists) {
			throw new NameAlreadyExistsException("Category with this name already exists for the current user!");
		}
		
		updatedCategoryEntity = categoryRepository.save(updatedCategoryEntity);
		
		Category categoryDTO = categoryMapper.fromEntity(updatedCategoryEntity);

		return categoryDTO;
	}
	
	/**
	 * Sets an existing category with a specified ID's limit
	 * and returns the updated category's data.
	 */
	public Category setCategoryLimitById(Long id, CategoryLimitUpdateRequest request) 
		throws CategoryNotFoundException, UserDoesNotOwnResourceException
	{
		boolean existsById = categoryRepository.existsById(id);
		if (!existsById) {
			throw new CategoryNotFoundException(id);
		}
		
		CategoryEntity categoryEntity = categoryRepository.findById(id).get();
		boolean belongsToCurrentlyLoggedInUser = serviceUtils.belongsToCurrentlyLoggedInUser(categoryEntity);
		if (!belongsToCurrentlyLoggedInUser) {
			throw new UserDoesNotOwnResourceException();
		}
		
		String limit = request.getLimit();
		categoryEntity.setLimit(limit);
		categoryEntity = categoryRepository.save(categoryEntity);

		Category categoryDTO = categoryMapper.fromEntity(categoryEntity);
		
		return categoryDTO;
	}
	
	/**
	 * Deletes an existing category with a specified ID
	 * and returns the deleted category's data.
	 */
	public Category deleteCategoryById(Long id) 
		throws CategoryNotFoundException, UserDoesNotOwnResourceException
	{
		boolean existsById = categoryRepository.existsById(id);
		if (!existsById) {
			throw new CategoryNotFoundException(id);
		}
		
		CategoryEntity categoryEntity = categoryRepository.findById(id).get();
		boolean belongsToCurrentlyLoggedInUser = serviceUtils.belongsToCurrentlyLoggedInUser(categoryEntity);
		if (!belongsToCurrentlyLoggedInUser) {
			throw new UserDoesNotOwnResourceException();
		}
		
		Category categoryDTO = categoryMapper.fromEntity(categoryEntity);
		
		categoryRepository.deleteById(id);
		
		return categoryDTO;
	}
	
	/**
	 * Creates system categories for a specified user (used on registration).
	 */
	protected void createSystemCategoriesForUser(UserEntity user) {
		CategoryEntity systemIncomeCategory = new CategoryEntity();
		systemIncomeCategory.setName(SYSTEM_INCOME_CATEGORY_NAME);
		systemIncomeCategory.setType(CategoryType.INCOME);
		systemIncomeCategory.setCurrentPeriodSum(0.0d);
		systemIncomeCategory.setOwner(user);
		categoryRepository.save(systemIncomeCategory);
		
		CategoryEntity systemExpensesCategory = new CategoryEntity();
		systemExpensesCategory.setName(SYSTEM_EXPENSES_CATEGORY_NAME);
		systemExpensesCategory.setType(CategoryType.EXPENSES);
		systemExpensesCategory.setCurrentPeriodSum(0.0d);
		systemExpensesCategory.setOwner(user);
		categoryRepository.save(systemExpensesCategory);
	}
	
	/**
	 * Creates example categories for the currently logged-in user
	 * and returns a list of the created categories' data.
	 */
	public List<Category> createExampleCategoriesForCurrentlyLoggedInUser() {
		List<Category> createdCategories = new ArrayList<>();
		
		for (CategoryEntity categoryEntity : EXAMPLE_CATEGORIES) {
			UserEntity owner = serviceUtils.getCurrentlyLoggedInUserEntity();
			String name = categoryEntity.getName();
			
			boolean nameAlreadyExists = categoryRepository.existsByNameAndOwner(name, owner);
			if (nameAlreadyExists) {
				continue;
			}

			categoryEntity.setOwner(owner);

			categoryRepository.save(categoryEntity);
			
			Category categoryDTO = categoryMapper.fromEntity(categoryEntity);
			createdCategories.add(categoryDTO);			
		}
		
		return createdCategories;
	}
	
	/**
	 * Updates an existing category's sum by either adding to it or subtracting the passed sum value.
	 * The valid operation types are '+' and '-' (for adding and subtracting respectively).
	 */
	protected void performCategorySumOperationById(Long categoryId, char operationType, Double sum) {
		CategoryEntity categoryEntity = categoryRepository.findById(categoryId).get();
		Double oldCurrentPeriodSum = categoryEntity.getCurrentPeriodSum();
		
		Double newCurrentPeriodSum = oldCurrentPeriodSum;
		switch (operationType) {
		case '+':
			newCurrentPeriodSum += sum;
			break;
		case '-':
			newCurrentPeriodSum -= sum;
			break;
		}
		
		categoryEntity.setCurrentPeriodSum(newCurrentPeriodSum);
		categoryRepository.save(categoryEntity);
	}
	
	/**
	 * Returns true if the passed category entity is a system category, false otherwise.
	 */
	private boolean isSystemCategory(CategoryEntity categoryEntity) {
		boolean isSystemCategory = ( 
			categoryEntity.getName().equals(SYSTEM_INCOME_CATEGORY_NAME) ||
			categoryEntity.getName().equals(SYSTEM_EXPENSES_CATEGORY_NAME)
		);
		
		return isSystemCategory; 
	}
	
	/**
	 * Returns the number of accounts in the example accounts array.
	 */
	public int getNumberOfExampleCategories() {
		return EXAMPLE_CATEGORIES.length;
	}
	
	/**
	 * Returns the specified user's system income category.
	 */
	protected CategoryEntity getSystemIncomeCategoryForUser(UserEntity user) {
		CategoryEntity systemIncomeCategory = categoryRepository.findByNameAndOwner(SYSTEM_INCOME_CATEGORY_NAME, user);
		return systemIncomeCategory;
	}
	
	/**
	 * Returns the specified user's system expenses category.
	 */
	protected CategoryEntity getSystemExpensesCategoryForUser(UserEntity user) {
		CategoryEntity systemExpensesCategory = categoryRepository.findByNameAndOwner(SYSTEM_EXPENSES_CATEGORY_NAME, user);
		return systemExpensesCategory;
	}
	
}