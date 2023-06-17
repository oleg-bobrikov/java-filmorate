package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.dto.Mpa;

import java.util.List;

public interface MpaStorage {
    List<Mpa> findAll();

    Mpa findMpaById(Integer id);
}
