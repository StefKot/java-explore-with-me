package ru.practicum.mainservice.comment.dto;

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
public class NewCommentDto {

    @NotBlank
    @Size(min = MainCommonUtils.MIN_LENGTH_COMMENT, max = MainCommonUtils.MAX_LENGTH_COMMENT)
    String text;
}