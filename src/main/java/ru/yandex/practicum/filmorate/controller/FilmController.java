package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;

    @GetMapping()
    public List<Film> getFilms() {
        return filmService.getFilms();
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable @NotNull Integer id) {
        return filmService.getFilmById(id);
    }

    @GetMapping(value = "/common")
    public List<Film> findCommonFilms(@RequestParam Integer userId, @RequestParam Integer friendId) {
        log.info("Получен запрос к эндпоинту: {} /common{}/{}", "GET", userId, friendId);
        return filmService.getCommonFilms(userId, friendId);
    }

    @PostMapping()
    public Film add(@NotNull @Valid @RequestBody Film film) {
        filmService.add(film);
        log.info("{} has added", film);
        return film;
    }

    @PutMapping()
    public Film update(@NotNull @Valid @RequestBody Film film) {
        Film updatedFilm = filmService.update(film);
        log.info(String.format("%s has updated", film));
        return updatedFilm;
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("{id}/like/{userId}")
    public void like(@PathVariable @NotBlank Integer id, @PathVariable @NotBlank Integer userId) {
        filmService.like(id, userId);
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("{id}/like/{userId}")
    public void removeLike(@PathVariable @NotNull Integer id, @PathVariable @NotBlank Integer userId) {
        filmService.removeLike(id, userId);
    }

    @GetMapping("/director/{directorId}")
    public List<Film> findFilmsByDirector(@PathVariable Integer directorId, @RequestParam String sortBy) {
        return filmService.findFilmByDirector(directorId, sortBy);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10", required = false) Integer count,
                                      @RequestParam(required = false) Integer genreId,
                                      @RequestParam(required = false) Integer year) {
        return filmService.getPopularFilms(count, genreId, year);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("{filmId}")
    public void delete(@PathVariable Integer filmId) {
        filmService.delete(filmId);
    }
}
