package com.pos.product.service;

import com.pos.common.exception.ResourceNotFoundException;
import com.pos.product.dto.CategoryDto;
import com.pos.product.entity.Category;
import com.pos.product.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryDto> findAll() {
        return categoryRepository.findAll().stream().map(this::toDto).toList();
    }

    public CategoryDto findById(UUID id) {
        return toDto(categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id)));
    }

    @Transactional
    public CategoryDto create(CategoryDto request) {
        Category category = new Category();
        category.setName(request.getName());
        category.setParentId(request.getParentId());
        return toDto(categoryRepository.save(category));
    }

    private CategoryDto toDto(Category c) {
        CategoryDto dto = new CategoryDto();
        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setParentId(c.getParentId());
        return dto;
    }
}
