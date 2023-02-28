package ru.yandex.practicum.filmorate.validator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.dto.Film;

import java.time.LocalDate;

import static ru.yandex.practicum.filmorate.validator.ValidatorTest.dtoHasErrorMessage;

class FilmValidatorTest {

    @Test
    public void film_errorMessage_nameIsBlank() {
        Film film = Film.builder()
                .name("")
                .description("Слепой экс-капитан Фаусто в сопровождении выделенного ему солдата" +
                        "отправляется в путешествие «в свет».")
                .releaseDate(LocalDate.of(1974, 1, 1))
                .duration(103)
                .build();

        Assertions.assertTrue(dtoHasErrorMessage(film, "Title cannot be empty"));
    }

    @Test
    public void film_errorMessage_failDescription() {
        Film film = Film.builder()
                .name("Запах женщины")
                .description("Слепой экс-капитан Фаусто в сопровождении выделенного ему солдата " +
                        "отправляется в путешествие «в свет». " +
                        "Он прекрасно ориентируется в пространстве. " +
                        "У него своя идеология сильной в любых обстоятельствах личности. " +
                        "И наконец Фаусто по-прежнему неотразим. " +
                        "Только запах женщины для него значит куда больше, " +
                        "чем для других соблазнителей... ")
                .releaseDate(LocalDate.of(1974, 1, 1))
                .duration(103)
                .build();

        Assertions.assertTrue(dtoHasErrorMessage(film, "Maximum description length is 200 characters"));
    }

    @Test
    public void film_errorMessage_releaseDate() {
        Film film = Film.builder()
                .name("Запах женщины")
                .description("Слепой экс-капитан Фаусто в сопровождении выделенного ему солдата" +
                        "отправляется в путешествие «в свет».")
                .releaseDate(LocalDate.of(1890, 3, 25))
                .duration(103)
                .build();

        Assertions.assertTrue(dtoHasErrorMessage(film, "Release date should be equal or after the 28th of December 1895"));
    }

    @Test
    public void film_errorMessage_failDuration() {
        Film film = Film.builder()
                .name("Запах женщины")
                .description("Слепой экс-капитан Фаусто в сопровождении выделенного ему солдата" +
                        "отправляется в путешествие «в свет».")
                .releaseDate(LocalDate.of(1890, 3, 25))
                .duration(-1)
                .build();

        Assertions.assertTrue(dtoHasErrorMessage(film, "Duration should be positive"));
    }

}