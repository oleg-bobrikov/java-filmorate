package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.Film;

import java.util.HashMap;
import java.util.Map;

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
    public void delete(int id) {
        films.remove(id);
    }

    @Override
    public Film getFilmById(int id) {
        return films.get(id);
    }
}
