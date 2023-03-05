package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.dto.Film;

import java.util.List;

public interface FilmStorage {
    void create(Film film);
    void update(Film film);
    void delete(Film film);
    List<Film> findAll();
    Film getFilmById(int id);
}
