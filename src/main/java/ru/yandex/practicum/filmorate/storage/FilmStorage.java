package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.dto.Film;

public interface FilmStorage {

    Film add(Film film);

    Film update(Film film);

    void delete(int id);

    Film getFilmById(int id);
}
