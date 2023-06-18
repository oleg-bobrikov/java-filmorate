package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface FilmStorage {

    Film add(Film film);

    Film update(Film film);

    void removeFilmById(Integer id);

    List<Film> getFilms();

    Optional<Film> findFilmById(int id);

    void addLike(Film film, User user);

    void removeLike(Film film, User user);

    List<Film> getTopFilms(int count);

    List<Film> getCommonFilms(Integer userId, Integer friendId);

    List<Film> getTopFilmsFilteredByYear(Integer year, Integer integer);

    List<Film> getTopFilmsFilteredByGenreAndYear(Integer count, Integer genreId, Integer year);

    List<Film> getTopFilmsFilteredByGenre(Integer count, Integer genreId);

    List<Film> getRecommendations(Integer userId);

    List<Film> searchFilms(Map<String, Object> params);

    List<Film> searchFilmsByDirectorOrderedByYear(Integer directorId);

    List<Film> searchFilmsByDirectorOrderedByLikes(Integer directorId);

  }
