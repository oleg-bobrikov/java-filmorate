package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Event {
    Long timestamp;
    Integer userId;
    String eventType;
    String operation;
    Integer eventId;
    Integer entityId;
}
