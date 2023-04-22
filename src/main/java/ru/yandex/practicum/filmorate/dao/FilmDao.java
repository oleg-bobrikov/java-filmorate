package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.dto.Film;
import ru.yandex.practicum.filmorate.dto.User;

import java.util.List;

public interface FilmDao {
    Film add(Film film);

    List<Film> getFilms();

    Film getFilmById(int id);

    Film update(Film film);

    void addLike(Film film, User user);

    void removeLike(Film film, User user);

    List<Film> getPopular(int count);
}
