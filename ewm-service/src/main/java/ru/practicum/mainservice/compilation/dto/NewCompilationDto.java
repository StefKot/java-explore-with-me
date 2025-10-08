package ru.practicum.mainservice.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.mainservice.MainCommonUtils;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class NewCompilationDto {

    @NotBlank
    @Size(min = MainCommonUtils.MIN_LENGTH_COMPILATION_TITLE, max = MainCommonUtils.MAX_LENGTH_COMPILATION_TITLE)
    String title;

    @Builder.Default
    boolean pinned = false;

    List<Long> events;
}