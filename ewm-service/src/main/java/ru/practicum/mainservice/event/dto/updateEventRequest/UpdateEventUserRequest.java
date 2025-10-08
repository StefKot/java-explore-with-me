package ru.practicum.mainservice.event.dto.updateEventRequest;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.mainservice.event.enums.StateActionUser;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(builderMethodName = "userBuilder")
public class UpdateEventUserRequest extends UpdateEventRequest {

    StateActionUser stateAction;
}