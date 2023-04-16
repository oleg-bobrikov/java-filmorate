package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.dto.Film;
import ru.yandex.practicum.filmorate.dto.Genre;

import java.util.List;
import java.util.Set;

public interface GenreDao {
    List<Genre> getAll();

    Genre getGenreById(Integer id);

    void updateFilmGenres(Film film, Set<Genre> genres);
}
