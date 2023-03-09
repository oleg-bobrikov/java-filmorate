package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;

    }

    @GetMapping()
    public List<Film> getFilms() {
        return filmService.getFilms();
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Integer id) {
        return filmService.getFilmById(id);
    }

    @GetMapping("/popular")
    public List<Film> getPopular(@RequestParam(required = false) Integer count) {
        return filmService.getPopular(count == null ? 10 : count);
    }

    @PostMapping()
    public Film add(@NotNull @Valid @RequestBody Film film) {
        filmService.add(film);
        log.info("{} has added", film);
        return film;
    }

    @PutMapping()
    public Film update(@NotNull @Valid @RequestBody Film film) {
        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }
        Film updatedFilm = filmService.update(film);
        log.info(String.format("%s has updated", film));
        return updatedFilm;
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("{id}/like/{userId}")
    public void like(@PathVariable Integer id, @PathVariable Integer userId) {
        filmService.like(id, userId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("{id}/like/{userId}")
    public void removeLike(@PathVariable Integer id, @PathVariable Integer userId) {
        filmService.removeLike(id, userId);
    }
}
