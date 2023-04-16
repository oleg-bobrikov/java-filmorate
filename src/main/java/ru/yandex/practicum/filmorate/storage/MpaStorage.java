package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.dto.Mpa;

import java.util.List;

public interface MpaStorage {
    List<Mpa> getAll();

    Mpa getMpaById(Integer id);
}
