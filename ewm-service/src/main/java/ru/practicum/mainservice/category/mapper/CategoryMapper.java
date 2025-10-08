package ru.practicum.mainservice.category.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.mainservice.category.dto.CategoryDto;
import ru.practicum.mainservice.category.dto.NewCategoryDto;
import ru.practicum.mainservice.category.model.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    @Mapping(target = "id", ignore = true)
    Category newCategoryDtoToCategory(NewCategoryDto newCategoryDto);

    @Mapping(source = "id", target = "id")
    Category categoryDtoToCategory(CategoryDto categoryDto);

    CategoryDto toCategoryDto(Category category);
}