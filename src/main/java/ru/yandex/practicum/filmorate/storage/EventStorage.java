package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Event;

import java.util.List;

@Repository
public interface EventStorage {
    List<Event> getEventsByUserId(Integer userId);

}
