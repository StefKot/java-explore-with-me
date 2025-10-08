package ru.practicum.mainservice.event.dto.updateEventRequest;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.mainservice.MainCommonUtils;
import ru.practicum.mainservice.event.dto.LocationDto;

import java.time.LocalDateTime;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class UpdateEventRequest {

    @Size(min = MainCommonUtils.MIN_LENGTH_ANNOTATION, max = MainCommonUtils.MAX_LENGTH_ANNOTATION)
    String annotation;

    Long category;

    @Size(min = MainCommonUtils.MIN_LENGTH_DESCRIPTION, max = MainCommonUtils.MAX_LENGTH_DESCRIPTION)
    String description;

    @JsonFormat(pattern = MainCommonUtils.DT_FORMAT, shape = JsonFormat.Shape.STRING)
    LocalDateTime eventDate;

    @Valid
    LocationDto location;

    Boolean paid;

    @PositiveOrZero
    Integer participantLimit;

    Boolean requestModeration;

    @Size(min = MainCommonUtils.MIN_LENGTH_TITLE, max = MainCommonUtils.MAX_LENGTH_TITLE)
    String title;
}