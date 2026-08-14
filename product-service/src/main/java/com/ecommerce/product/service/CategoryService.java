package com.ecommerce.product.service;

import com.ecommerce.product.dto.request.CategoryRequest;
import com.ecommerce.product.dto.response.CategoryResponse;
import com.ecommerce.product.entity.Category;
import com.ecommerce.product.exception.CategoryAlreadyExistsException;
import com.ecommerce.product.exception.CategoryNotFoundException;
import com.ecommerce.product.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public CategoryResponse create(CategoryRequest request) {

        if (categoryRepository.existsByName(request.getName())) {
            throw new CategoryAlreadyExistsException(request.getName());
        }

        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setActive(
                request.getActive() == null || request.getActive()
        );

        Instant now = Instant.now();
        category.setCreatedAt(now);
        category.setUpdatedAt(now);

        return toResponse(categoryRepository.save(category));
    }

    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private CategoryResponse toResponse(Category category) {

        CategoryResponse response = new CategoryResponse();

        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setActive(category.isActive());
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedAt(category.getUpdatedAt());

        return response;
    }

    public CategoryResponse getById(Long id) {

    Category category = categoryRepository.findById(id)
            .orElseThrow(() ->
                    new CategoryNotFoundException(id));

    return toResponse(category);
}

public CategoryResponse update(Long id, CategoryRequest request) {

    Category category = categoryRepository.findById(id)
            .orElseThrow(() ->
                    new CategoryNotFoundException(id));

    if (!category.getName().equals(request.getName())
            && categoryRepository.existsByName(request.getName())) {

        throw new CategoryAlreadyExistsException(request.getName());
    }

    category.setName(request.getName());
    category.setDescription(request.getDescription());

    if (request.getActive() != null) {
        category.setActive(request.getActive());
    }

    category.setUpdatedAt(Instant.now());

    return toResponse(categoryRepository.save(category));
}

public void delete(Long id) {

    Category category = categoryRepository.findById(id)
            .orElseThrow(() ->
                    new CategoryNotFoundException(id));

    categoryRepository.delete(category);
}
}