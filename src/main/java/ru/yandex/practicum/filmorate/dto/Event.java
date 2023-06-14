package ru.yandex.practicum.filmorate.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class Event {
    Instant timestamp;
    Integer userId;
    String eventType;
    String operation;
    Integer eventId;
    Integer entityId;
}
