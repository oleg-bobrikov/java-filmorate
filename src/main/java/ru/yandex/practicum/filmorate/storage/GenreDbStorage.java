package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.GenreDao;
import ru.yandex.practicum.filmorate.dto.Film;
import ru.yandex.practicum.filmorate.dto.Genre;

import java.util.List;
import java.util.Set;

@Component
@Qualifier
public class GenreDbStorage implements GenreStorage {
    @Autowired
    @Qualifier("genreDaoImplH2")
    private GenreDao genreDao;

    @Override
    public List<Genre> getAll() {
        return genreDao.getAll();
    }

    @Override
    public Genre getGenreById(Integer id) {
        return genreDao.getGenreById(id);
    }

    @Override
    public void updateFilmGenres(Film film, Set<Genre> genres) {
        genreDao.updateFilmGenres(film, genres);
    }
}
