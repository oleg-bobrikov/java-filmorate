package ru.yandex.practicum.filmorate.dto;

import lombok.Data;

import java.sql.Timestamp;

@Data

public class Event {
    Timestamp timestamp;
    Integer userId;
    String eventType;
    String operation;
    Integer eventId;
    Integer entityId;
}
