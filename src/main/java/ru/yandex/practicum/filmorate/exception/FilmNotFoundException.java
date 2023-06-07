package ru.yandex.practicum.filmorate.exception;

public class FilmNotFoundException extends InvalidIdException {
    public FilmNotFoundException(Integer id) {
        super(String.format("Фильм с id = %d не найден", id));
    }

    public FilmNotFoundException(final String message) {
        super(message);
    }
}
