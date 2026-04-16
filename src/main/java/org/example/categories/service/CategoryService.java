package org.example.categories.service;

import org.example.categories.service.exception.BusinessRuleException;
import org.example.categories.service.exception.NotFoundException;
import org.example.categories.repository.model.CategoryEntity;
import org.example.categories.repository.CategoryRepository;
import org.example.categories.api.dto.CategoryRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public CategoryEntity create(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new BusinessRuleException("Category with name '" + request.getName() + "' already exists");
        }

        CategoryEntity category = CategoryEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        return categoryRepository.save(category);
    }

    public List<CategoryEntity> getAll() {
        return categoryRepository.findAll();
    }

    public CategoryEntity update(Long id, CategoryRequest request) {
        CategoryEntity category = getById(id);
        if (!category.getName().equalsIgnoreCase(request.getName())
                && categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new BusinessRuleException("Category with name '" + request.getName() + "' already exists");
        }
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        return categoryRepository.save(category);
    }

    public CategoryEntity getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category with id " + id + " not found"));
    }

    public void delete(Long id) {
        getById(id);
        categoryRepository.deleteById(id);
    }
}