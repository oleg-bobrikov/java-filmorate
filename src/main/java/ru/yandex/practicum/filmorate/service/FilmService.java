package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.yandex.practicum.filmorate.dto.Film;
import ru.yandex.practicum.filmorate.dto.User;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    @Autowired
    @Qualifier("filmH2Storage")
    private FilmStorage filmStorage;

    private final UserService userService;

    public List<Film> getPopular(int count) {
        return filmStorage.getPopular(count);
    }

    public Film getFilmById(Integer id) {
        Optional<Film> filmOptional = filmStorage.getFilmById(id);
        if (filmOptional.isEmpty()) {
            throw new NotFoundException("Фильм с идентификатором " + id + " не найден.");
        }
        return filmOptional.get();
    }

    public void like(Integer id, Integer userId) {
        Film film = getFilmById(id);
        User user = userService.findUserById(userId);
        filmStorage.addLike(film, user);
    }

    public Film update(Film film) {
        final Integer filmId = film.getId();
        Optional<Film> filmOptional = filmStorage.getFilmById(filmId);
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

    public List<Film> searchFilms(String query, List<String> by) {
        HashMap<String, String> params = new HashMap<>();
        for(String filter: by){
            params.put(filter,query);
        }
        return filmStorage.searchFilms(params);
    }

    public List<Film> searchFilms() {
        return filmStorage.searchFilms(null);
    }

}