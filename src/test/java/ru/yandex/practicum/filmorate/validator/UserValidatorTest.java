package ru.yandex.practicum.filmorate.validator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.dto.User;

import java.time.LocalDate;

import static ru.yandex.practicum.filmorate.validator.ValidatorTest.dtoHasErrorMessage;

class UserValidatorTest {

   @Test
    public void user_errorMessage_invalidEmailAddress() {
        User user = User.builder()
                .login("Tolstoy")
                .name("Nick Name")
                .email("mail.ru")
                .birthday(LocalDate.of(1828, 9, 9))
                .build();

        Assertions.assertTrue(dtoHasErrorMessage(user, "invalid email address"));
    }

   @Test
    public void user_errorMessage_failLogin() {
        User user = User.builder()
                .login("Lev Tolstoy")
                .name("War and Peace")
                .email("lev.tolstoy@yandex.ru")
                .birthday(LocalDate.of(1828, 9, 9))
                .build();

        Assertions.assertTrue(dtoHasErrorMessage(user, "login should be not empty or contain whitespaces"));
    }

   @Test
    public void user_errorMessage_failBirthday() {
        User user = User.builder()
                .login("LevTolstoy")
                .name("War and Peace")
                .email("lev.tolstoy@yandex.ru")
                .birthday(LocalDate.of(2028, 9, 9))
                .build();

        Assertions.assertTrue(dtoHasErrorMessage(user, "birthday must be in the past or present"));
    }
}