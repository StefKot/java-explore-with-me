package ru.practicum.mainservice.category.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.mainservice.category.dto.CategoryDto;
import ru.practicum.mainservice.category.dto.NewCategoryDto;
import ru.practicum.mainservice.category.model.Category;

import java.util.List;

public interface CategoryService {
    CategoryDto create(NewCategoryDto newCategoryDto);

    List<CategoryDto> getAll(Pageable pageable);

    CategoryDto getById(Long catId);

    CategoryDto patch(Long catId, CategoryDto categoryDto);

    void deleteById(Long catId);

    Category getCategoryById(Long catId);
}