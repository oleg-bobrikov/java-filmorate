package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.dto.Film;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();
    private int nextId = 1;

    @PostMapping()
    public Film create(@NotNull @Valid @RequestBody Film film) {
        film.setId(nextId++);
        films.put(film.getId(), film);
        log.info(String.format("%s has created", film));
        return film;
    }

    @GetMapping()
    public List<Film> getFilms() {
        return new ArrayList<>(films.values());
    }

    @PutMapping()
    public Film update(@NotNull @Valid @RequestBody Film film) {

        if (films.get(film.getId()) == null) {
            throw new ValidationException(String.format("Movie with id %s not found", film.getId()));
        }
        films.put(film.getId(), film);
        log.info(String.format("%s has updated", film));
        return film;
    }
}
