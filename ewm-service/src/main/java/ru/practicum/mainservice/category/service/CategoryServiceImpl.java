package ru.practicum.mainservice.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainservice.category.dto.CategoryDto;
import ru.practicum.mainservice.category.dto.NewCategoryDto;
import ru.practicum.mainservice.category.mapper.CategoryMapper;
import ru.practicum.mainservice.category.model.Category;
import ru.practicum.mainservice.category.repository.CategoryRepository;
import ru.practicum.mainservice.exception.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryDto create(NewCategoryDto newCategoryDto) {
        log.info("Добавление новой категории {}", newCategoryDto);

        return categoryMapper.toCategoryDto(categoryRepository.save(categoryMapper.newCategoryDtoToCategory(newCategoryDto)));
    }

    @Override
    public List<CategoryDto> getAll(Pageable pageable) {
        log.info("Вывод всех категорий с разбивкой на страницы {}", pageable);

        return categoryRepository.findAll(pageable).stream()
                .map(categoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getById(Long catId) {
        log.info("Вывод категории с ID {}", catId);

        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория с таким ID отсутствует"));

        return categoryMapper.toCategoryDto(category);
    }

    @Override
    @Transactional
    public CategoryDto patch(Long catId, CategoryDto categoryDto) {
        log.info("Обновление категории с ID {} новые параметры {}", catId, categoryDto);

        categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория с таким ID отсутствует"));

        categoryDto.setId(catId);
        return categoryMapper.toCategoryDto(categoryRepository.save(categoryMapper.categoryDtoToCategory(categoryDto)));
    }

    @Override
    @Transactional
    public void deleteById(Long catId) {
        log.info("Удаление категории с ID {}", catId);

        categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория с таким ID отсутствует"));

        categoryRepository.deleteById(catId);
    }

    @Override
    public Category getCategoryById(Long catId) {
        log.info("Вывод категории с ID {}", catId);

        return categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория с таким ID отсутствует"));
    }
}