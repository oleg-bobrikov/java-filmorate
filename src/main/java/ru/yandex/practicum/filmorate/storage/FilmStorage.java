package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.dto.Film;

import java.util.List;

public interface FilmStorage {

    Film add(Film film);

    Film update(Film film);

    void delete(int id);
    List<Film> getFilms();
    Film getFilmById(int id);

}
