package ru.yandex.practicum.filmorate.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Integer id) {
        super(String.format("Пользователь с id = %d не найден", id));
    }

    public UserNotFoundException(final String message) {
        super(message);
    }
}
