package ru.yandex.practicum.filmorate.exception;


public class InvalidIdException extends RuntimeException {
    public InvalidIdException(String m) {
        super(m);
    }
}