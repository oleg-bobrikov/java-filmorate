package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping()
    public List<Film> getFilms() {
        return filmService.getFilms();
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable @NotBlank Integer id) {
        return filmService.getFilmById(id);
    }

    /*@GetMapping("/popular")
    public List<Film> getPopular(@RequestParam(defaultValue = "10", required = false) Integer count) {
        return filmService.getPopular(count);
    }*/

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

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("{id}/like/{userId}")
    public void removeLike(@PathVariable @NotBlank Integer id, @PathVariable @NotBlank Integer userId) {
        filmService.removeLike(id, userId);
    }

    @GetMapping("/director/{directorId}")
    public List<Film> findFilmsByDirector(@PathVariable Integer directorId, @RequestParam String sortBy) {
        return filmService.findFilmByDirector(directorId, sortBy);
    }

    /*@GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10", required = false) @Min(value = 1,
            message = "Limit should be equals or more than 1.") int count,
                                                     @RequestParam(required = false) Optional<Integer> genreId,
                                                     @RequestParam(required = false) @Min(value = 1895,
                                              message = "Release year should be equal or after 1895") Optional<Integer> year) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("is_filtered_by_genre_id", false);
        params.put("is_filtered_by_year", false);
        params.put("genre_id", 0);
        params.put("year", 0);
        params.put("count", count);
        genreId.ifPresent(integer -> {
            params.put("genre_id", integer);
            params.put("is_filtered_by_genre_id", true);
        });
        year.ifPresent(integer -> {
            params.put("year", integer);
            params.put("is_filtered_by_year", true);
        });*/

        @GetMapping("/popular")
        public List<Film> getPopularFilms(@RequestParam(defaultValue = "10", required = false) Integer count,
                @RequestParam(required = false) Integer genreId,
                @RequestParam(required = false) Integer year) {
            return filmService.getPopularFilms(count, genreId, year);
        }

        /*if(count.isPresent()){
            params.put("count", count.get());
        }*/

        //return filmService.getPopularFilms(params);


    //GET /films/popular?count={limit}&genreId={genreId}&year={year}
    //@IsAfterOrEqual(current = "1895-12-28", message = "Release date should be equal or after the 28th of December 1895")

}
