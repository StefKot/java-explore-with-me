package ru.practicum.mainservice.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.mainservice.MainCommonUtils;

import java.time.LocalDateTime;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class NewEventDto {

    @NotBlank
    @Size(min = MainCommonUtils.MIN_LENGTH_ANNOTATION, max = MainCommonUtils.MAX_LENGTH_ANNOTATION)
    String annotation;

    @NotNull
    Long category;

    @NotBlank
    @Size(min = MainCommonUtils.MIN_LENGTH_DESCRIPTION, max = MainCommonUtils.MAX_LENGTH_DESCRIPTION)
    String description;

    @NotNull
    @JsonFormat(pattern = MainCommonUtils.DT_FORMAT, shape = JsonFormat.Shape.STRING)
    LocalDateTime eventDate;

    @NotNull
    @Valid
    LocationDto location;

    @Builder.Default
    boolean paid = false;

    @Builder.Default
    @PositiveOrZero
    int participantLimit = 0;

    @Builder.Default
    boolean requestModeration = true;

    @NotBlank
    @Size(min = MainCommonUtils.MIN_LENGTH_TITLE, max = MainCommonUtils.MAX_LENGTH_TITLE)
    String title;
}