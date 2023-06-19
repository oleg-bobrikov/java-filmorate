package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreStorage genreStorage;

    public List<Genre> getAll() {
        return genreStorage.findAll();
    }

    public Genre getGenreById(Integer id) {
        Genre genre = genreStorage.findGenreById(id);
        if (genre == null) {
            throw new NotFoundException("Жанр с идентификатором " + id + " не найден.");
        }

        return genre;
    }
}
