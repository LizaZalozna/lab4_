package org.example.categories.api;

import jakarta.validation.Valid;
import org.example.categories.service.CategoryService;
import org.example.categories.api.dto.CategoryRequest;
import org.example.categories.api.dto.CategoryResponse;
import org.example.notes.service.ApiMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final ApiMapper apiMapper;

    public CategoryController(CategoryService categoryService, ApiMapper apiMapper) {
        this.categoryService = categoryService;
        this.apiMapper = apiMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@Valid @RequestBody CategoryRequest request) {
        return apiMapper.toCategoryResponse(categoryService.create(request));
    }

    @GetMapping
    public List<CategoryResponse> getAll() {
        return categoryService.getAll().stream()
                .map(apiMapper::toCategoryResponse)
                .toList();
    }

    @PutMapping("/{id}")
    public CategoryResponse update(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return apiMapper.toCategoryResponse(categoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        categoryService.delete(id);
    }
}
