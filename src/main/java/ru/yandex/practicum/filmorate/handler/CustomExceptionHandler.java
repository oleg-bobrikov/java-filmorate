package ru.yandex.practicum.filmorate.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exception.DirectorAlreadyExistedException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ResponseError;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class CustomExceptionHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseError handle(NotFoundException exception) {
        log.error(exception.getMessage(), exception);
        return ResponseError.builder()
                .error("NOT FOUND")
                .status(404)
                .exception("ru.yandex.practicum.filmorate.exception.NotFoundException")
                .message(exception.getMessage())
                .path(getPath(exception))
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseError handle(MethodArgumentNotValidException exception) {
        log.error(exception.getMessage(), exception);
        return ResponseError.builder()
                .error("BAD REQUEST")
                .status(400)
                .exception("org.springframework.web.bind.MethodArgumentNotValidException")
                .message(exception.getMessage())
                .path(getPath(exception))
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseError handle(DataAccessException exception) {
        log.error(exception.getMessage(), exception);
        return ResponseError.builder()
                .error("INTERNAL_SERVER_ERROR")
                .status(500)
                .exception("org.springframework.dao.DataAccessException")
                .message(exception.getMessage())
                .path(getPath(exception))
                .build();
    }

    private String getPath(Exception exception) {
        String path = "";
        if (exception.getStackTrace().length > 0) {
            String path0 = exception.getStackTrace()[0].getFileName();
            if (path0 != null && path0.contains("UserController")) {
                path = "/users";
            } else if (path0 != null && path0.contains("FilmController")) {
                path = "/films";
            }
        }
        return path;
    }
    @ExceptionHandler({DirectorAlreadyExistedException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseError handleAlreadyExists(DirectorAlreadyExistedException exception) {

        log.error(exception.getMessage(), exception);
        return ResponseError.builder()
                .error("CONFLICT")
                .status(409)
                .exception("ru.yandex.practicum.filmorate.exception.DirectorAlreadyExistedException")
                .message(exception.getMessage())
                .path(getPath(exception))
                .build();
    }
}

