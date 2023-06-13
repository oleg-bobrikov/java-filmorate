package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.yandex.practicum.filmorate.dto.Film;
import ru.yandex.practicum.filmorate.dto.User;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import javax.validation.Valid;
import javax.validation.ValidationException;
import java.util.*;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;

    private UserStorage userStorage;

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


    public List<Film> getCommonFilms(int userId, int friendId) {
        return filmStorage.getCommonFilms(userId, friendId);
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
    public List<Film> getPopularFilms(Integer count, Integer genreId, Integer year) {
        List<Film> result = new ArrayList<>();
        if (genreId == null && year == null){
            result =  filmStorage.getPopular(count);
        } else if (genreId != null && year != null){
            result = filmStorage.getPopularFilms(count, genreId, year);
        } else if (genreId == null){
            result = filmStorage.getPopularFilmsSortedByYear(count, year);
        } else result = filmStorage.getPopularFilmsSortedByGenre(count, genreId);


        /*if (count == null){
            if (year == null){
                result = filmStorage.getPopular(genreId);
            } else result = filmStorage.getPopularFilmsSortedByYear(year);
        }

        if (genreId == null && year == null){
            result =  filmStorage.getPopular(count);
        }*/
        return result;


       /* @Override
        public List<Film> getTopFilms(Integer count, Integer genreId, Integer year) {
            if (genreId != null && year != null) {
                return filmStorage.getPopularFilms(count, genreId, year);
            } else if (genreId == null && year == null) {
                return filmStorage.getPopularFilms(count);
            } else if (genreId != null) {
                return filmStorage.getPopularFilms(count, genreId);
            } else {
                return filmStorage.getPopularFilmsSortedByYear(count, year);
            }
        }*/


        //List<Film> list = filmStorage.getPopularFilms((HashMap<String, Object>) params);

    }
    /*public Film delete(Integer filmId){
        if (filmId == null){
            throw new ValidationException("Идентификатор фильма не может быть пустым.");
        }
        if (filmStorage.getFilmById(filmId).isEmpty()){
            throw new NotFoundException("Данного фильма не существует.");
        }
        return filmStorage.delete(filmId);
    }*/
}