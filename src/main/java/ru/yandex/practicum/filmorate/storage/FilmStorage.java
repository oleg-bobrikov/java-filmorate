package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.dto.Film;
import ru.yandex.practicum.filmorate.dto.User;

import java.util.List;

public interface FilmStorage {

    Film add(Film film);

    Film update(Film film);

    void delete(int id);

    List<Film> getFilms();

    Film getFilmById(int id);

    void addLike(Film film, User user);

    void removeLike(Film film, User user);

    List<Film> getPopular(int count);
}
