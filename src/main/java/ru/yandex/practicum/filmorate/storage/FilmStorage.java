package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.dto.Film;
import ru.yandex.practicum.filmorate.dto.User;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {

    Film add(Film film);

    Film update(Film film);

    void deleteFilmById(int id);

    List<Film> getFilms();

    Optional<Film> getFilmById(int id);

    void addLike(Film film, User user);

    void removeLike(Film film, User user);

    List<Film> getPopular(int count);

    List<Film> findFilmByDirector(Integer directorId, String sortBy);
}
