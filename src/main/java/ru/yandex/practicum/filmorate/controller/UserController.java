package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.dto.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    UserStorage userStorage;

    @Autowired
    public UserController(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @PostMapping()
    public User add(@NotNull @Valid @RequestBody User user) {

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        userStorage.update(user);
        log.info(String.format("%s has created", user));
        return user;
    }

    @GetMapping()
    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    @PutMapping()
    public User update(@NotNull @Valid @RequestBody User user) {
        if (userStorage.getUserById(user.getId()) == null) {
            throw new ValidationException(String.format("User with id %s not found", user.getId()));
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        userStorage.update(user);
        log.info(String.format("%s has updated", user));
        return user;
    }


}
