package ru.yandex.practicum.filmorate.dto;

import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Builder
public class Event {
    Timestamp timestamp;
    Integer userId;
    String eventType;
    String operation;
    Integer eventId;
    Integer entityId;
}
