package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.dto.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmStorage filmStorage;

    @Autowired
    public FilmController(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    @PostMapping()
    public Film add(@NotNull @Valid @RequestBody Film film) {
        filmStorage.add(film);
        log.info("{} has added", film);
        return film;
    }

    @GetMapping()
    public List<Film> getFilms() {
        return filmStorage.getFilms();
    }

    @PutMapping()
    public Film update(@NotNull @Valid @RequestBody Film film) {

        if (filmStorage.getFilmById(film.getId()) == null) {
            throw new ValidationException(String.format("Movie with id %s not found", film.getId()));
        }
        filmStorage.update(film);
        log.info(String.format("%s has updated", film));
        return film;
    }
}
