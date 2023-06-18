package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.yandex.practicum.filmorate.dto.Film;
import ru.yandex.practicum.filmorate.dto.User;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.*;
import java.util.stream.Collectors;

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

    public List<Film> findFilmByDirector(Integer directorId, String sortBy) {
        List<Film> list = filmStorage.findFilmByDirector(directorId, sortBy);
        if (list.isEmpty()) {
            throw new NotFoundException("Список режессеров пуст.");
        }
        return list;
    }

    public List<Film> getPopularFilms(Integer count, Integer genreId, Integer year) {
        List<Film> result;
        if (genreId == null && year == null) {
            result = filmStorage.getPopular(count);
        } else if (genreId != null && year != null) {
            result = filmStorage.getPopularFilms(count, genreId, year);
        } else if (genreId == null) {
            result = filmStorage.getPopularFilmsSortedByYear(count, year);
        } else result = filmStorage.getPopularFilmsSortedByGenre(count, genreId);

        return result;
    }

    public void delete(Integer userId) {
        if (filmStorage.findFilmById(userId).isEmpty()) {
            throw new NotFoundException("Такого фильма нет.");
        }
        filmStorage.removeFilmById(userId);
    }

    public List<Film> searchFilms(String query, List<String> by) {
        Set searchResult = new LinkedHashSet();
        List<Film> sortedFilms = new LinkedList<>(searchResult);
        if (query == null && by == null) {
            searchResult.addAll(getFilms());
            sortedFilms.addAll(searchResult);
        } else {
            if (by.contains("title")) {
                searchResult.addAll(filmStorage.searchFilmsByTitle(query));
            }
            if (by.contains("director")) {
                searchResult.addAll(filmStorage.searchFilmsByDirector(query));
            }
            sortedFilms.addAll(searchResult);
        }
        return sortedFilms.stream()
                .sorted((o1, o2) -> o2.getLikes().size() - o1.getLikes().size())
                .collect(Collectors.toList());
    }
}