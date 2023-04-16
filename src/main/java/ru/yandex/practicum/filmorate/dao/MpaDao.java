package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.dto.Mpa;

import java.util.List;

public interface MpaDao {
    List<Mpa> getAll();

    Mpa getMpaById(Integer id);
}
