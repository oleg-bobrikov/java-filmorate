package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.*;

@Slf4j
@Service
@Validated
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;

    public FilmService(@Qualifier("filmH2Storage") FilmStorage filmStorage,
                       UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }


    public Film getFilmById(Integer id) {
        Optional<Film> filmOptional = filmStorage.findFilmById(id);
        if (filmOptional.isEmpty()) {
            throw new NotFoundException("Фильм с идентификатором " + id + " не найден.");
        }
        return filmOptional.get();
    }


    public List<Film> getCommonFilms(int userId, int friendId) {
        return filmStorage.getCommonFilms(userId, friendId);
    }

    public void like(Integer filmId, Integer userId) {
        Film film = getFilmById(filmId);
        User user = userService.findUserById(userId);
        filmStorage.addLike(film, user);
    }

    public Film update(Film film) {
        final Integer filmId = film.getId();
        Optional<Film> filmOptional = filmStorage.findFilmById(filmId);
        if (filmOptional.isEmpty()) {
            throw new NotFoundException("Фильм с идентификатором " + filmId + " не найден.");
        }

        return filmStorage.update(film);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeLike(Integer id, Integer userId) {
        Film film = getFilmById(id);
        User user = userService.findUserById(userId);

        filmStorage.removeLike(film, user);
    }

    public List<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public void add(Film film) {
        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }
        filmStorage.add(film);
    }

    public List<Film> searchFilmsByDirector(Integer directorId, String sortBy) {
        List<Film> list;
        if (sortBy.equals("year")) {
            list = filmStorage.searchFilmsByDirectorOrderedByYear(directorId);
        } else if (sortBy.equals("likes")) {
            list = filmStorage.searchFilmsByDirectorOrderedByLikes(directorId);
        } else {
            throw new NotFoundException("Список режессеров пуст.");
        }

        if (list.isEmpty()) {
            throw new NotFoundException("Список режессеров пуст.");
        }
        return list;
    }

    public List<Film> getTopFilmsFilteredByGenreAndYear(Integer count, Integer genreId, Integer year) {
        List<Film> result;
        if (genreId == null && year == null) {
            result = filmStorage.getTopFilms(count);
        } else if (genreId != null && year != null) {
            result = filmStorage.getTopFilmsFilteredByGenreAndYear(count, genreId, year);
        } else if (genreId == null) {
            result = filmStorage.getTopFilmsFilteredByYear(count, year);
        } else result = filmStorage.getTopFilmsFilteredByGenre(count, genreId);

        return result;
    }

    public void delete(Integer userId) {
        if (filmStorage.findFilmById(userId).isEmpty()) {
            throw new NotFoundException("Такого фильма нет.");
        }
        filmStorage.removeFilmById(userId);

    }

    public List<Film> searchFilms(Optional<String> query, Optional<List<String>> by) {
        if (query.isPresent() && by.isPresent()) {
            List<String> filter = by.get();
            if (filter.contains("director") && filter.contains("title")) {
                return filmStorage.searchFilmsByTitleAndDirectorName(query.get());
            } else if (filter.contains("director")) {
                return filmStorage.searchFilmsByDirectorName(query.get());
            } else if (filter.contains("title")) {
                return filmStorage.searchFilmsByTitle(query.get());
            }
        }
        return filmStorage.getAllFilms();
    }

    public List<Film> getRecommendations(Integer userId) {
        userService.findUserById(userId);
        return filmStorage.getRecommendations(userId);
    }

}