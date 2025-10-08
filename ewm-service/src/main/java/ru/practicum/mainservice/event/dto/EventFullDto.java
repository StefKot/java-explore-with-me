package ru.practicum.mainservice.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.mainservice.MainCommonUtils;
import ru.practicum.mainservice.category.dto.CategoryDto;
import ru.practicum.mainservice.event.enums.EventState;
import ru.practicum.mainservice.user.dto.UserShortDto;

import java.time.LocalDateTime;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventFullDto {

    String annotation;

    CategoryDto category;

    Long confirmedRequests;

    @JsonFormat(pattern = MainCommonUtils.DT_FORMAT, shape = JsonFormat.Shape.STRING)
    LocalDateTime createdOn;

    String description;

    @JsonFormat(pattern = MainCommonUtils.DT_FORMAT, shape = JsonFormat.Shape.STRING)
    LocalDateTime eventDate;

    Long id;

    UserShortDto initiator;

    LocationDto location;

    Boolean paid;

    Integer participantLimit;

    @JsonFormat(pattern = MainCommonUtils.DT_FORMAT, shape = JsonFormat.Shape.STRING)
    LocalDateTime publishedOn;

    Boolean requestModeration;

    EventState state;

    String title;

    Long views;
}