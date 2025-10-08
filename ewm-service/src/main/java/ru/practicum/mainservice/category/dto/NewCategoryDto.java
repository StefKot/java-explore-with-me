package ru.practicum.mainservice.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.mainservice.MainCommonUtils;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class NewCategoryDto {

    @Size(max = MainCommonUtils.MAX_LENGTH_CATEGORY_NAME)
    @NotBlank
    String name;
}