package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.FilmDao;
import ru.yandex.practicum.filmorate.dto.Film;
import ru.yandex.practicum.filmorate.dto.User;

import java.util.List;

@Component
@Primary
public class FilmDbStorage implements FilmStorage {
    @Autowired
    @Qualifier("filmDaoImplH2")
    private FilmDao filmDao;

    @Override
    public Film add(Film film) {
        return filmDao.add(film);
    }

    @Override
    public Film update(Film film) {
        return filmDao.update(film);
    }

    @Override
    public void delete(int id) {

    }

    @Override
    public List<Film> getFilms() {
        return filmDao.getFilms();
    }


    @Override
    public Film getFilmById(int id) {
        return filmDao.getFilmById(id);
    }

    @Override
    public void addLike(Film film, User user) {
        filmDao.addLike(film, user);
    }

    @Override
    public void removeLike(Film film, User user) {
        filmDao.removeLike(film, user);
    }

    @Override
    public List<Film> getPopular(int count) {
        return filmDao.getPopular(count);
    }
}
