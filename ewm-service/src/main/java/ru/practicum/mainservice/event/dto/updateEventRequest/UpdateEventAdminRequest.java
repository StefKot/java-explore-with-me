package ru.practicum.mainservice.event.dto.updateEventRequest;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.mainservice.event.enums.StateActionAdmin;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(builderMethodName = "adminBuilder")
public class UpdateEventAdminRequest extends UpdateEventRequest {

    StateActionAdmin stateAction;
}