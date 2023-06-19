package ru.yandex.practicum.filmorate.exception;

public class DirectorAlreadyExistedException extends RuntimeException {
    public DirectorAlreadyExistedException(String message) {
        super(message);
    }
}
