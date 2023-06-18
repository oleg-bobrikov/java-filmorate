package ru.yandex.practicum.filmorate.storage.impl;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.*;
import java.util.stream.Collectors;


@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();
    private int nextId = 1;

    @Override
    public Film add(Film film) {
        film.setId(nextId++);
        return films.put(film.getId(), film);
    }

    @Override
    public Film update(Film film) {
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public void removeFilmById(Integer id) {
        films.remove(id);
    }

    @Override
    public List<Film> getFilms() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Optional<Film> findFilmById(int id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public void addLike(Film film, User user) {
        film.getLikes().add(user.getId());

    }

    @Override
    public void removeLike(Film film, User user) {
        film.getLikes().remove(user.getId());
    }

    @Override
    public List<Film> getTopFilms(int count) {

        return getFilms().stream()
                .sorted((o1, o2) -> o2.getLikes().size() - o1.getLikes().size())
                .limit(count)
                .collect(Collectors.toList());
    }

    @Override
    public List<Film> getCommonFilms(Integer userId, Integer friendId) {
        return new ArrayList<>();
    }

    @Override
    public List<Film> getTopFilmsFilteredByYear(Integer year, Integer integer) {
        return new ArrayList<>();
    }

    @Override
    public List<Film> getTopFilmsFilteredByGenreAndYear(Integer count, Integer genreId, Integer year) {
        return new ArrayList<>();
    }

    @Override
    public List<Film> getTopFilmsFilteredByGenre(Integer count, Integer genreId) {
        return new ArrayList<>();
    }

    @Override
    public List<Film> getRecommendations(Integer userId) {
        return new ArrayList<>();
    }

    @Override
    public List<Film> searchFilms(Map<String, String> params) {
        return new ArrayList<>();
    }

    @Override
    public List<Film> searchFilmsByDirectorOrderedByYear(Integer directorId) {
        return new ArrayList<>();
    }

    @Override
    public List<Film> searchFilmsByDirectorOrderedByLikes(Integer directorId) {
        return new ArrayList<>();
    }

}
