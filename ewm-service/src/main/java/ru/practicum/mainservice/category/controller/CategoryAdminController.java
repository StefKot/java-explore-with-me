package ru.practicum.mainservice.category.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.category.dto.CategoryDto;
import ru.practicum.mainservice.category.dto.NewCategoryDto;
import ru.practicum.mainservice.category.service.CategoryService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/categories")
@Validated
public class CategoryAdminController {
    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto create(@Valid @RequestBody NewCategoryDto newCategoryDto) {
        return categoryService.create(newCategoryDto);
    }

    @PatchMapping("/{catId}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto patch(@PathVariable Long catId,
                             @Valid @RequestBody CategoryDto categoryDto) {
        return categoryService.patch(catId, categoryDto);
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable Long catId) {
        categoryService.deleteById(catId);
    }
}