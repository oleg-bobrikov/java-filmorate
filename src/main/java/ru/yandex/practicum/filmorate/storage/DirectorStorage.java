package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.dto.Director;
import ru.yandex.practicum.filmorate.dto.Film;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface DirectorStorage {
    List<Director> getAll();
    Optional<Director> getDirectorById(Integer id);
    Optional<Director> createDirector(Director director);
    Optional<Optional<Director>> updateDirector(Director director);
    void removeDirector(int id);
    void updateFilmDirector(Film film, Set<Director> directors);
   // Optional<Director> getDirectorByFilm(int film_id);

}
