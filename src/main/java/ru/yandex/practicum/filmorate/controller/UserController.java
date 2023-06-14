package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.Event;
import ru.yandex.practicum.filmorate.dto.Film;
import ru.yandex.practicum.filmorate.dto.User;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping()
    public List<User> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable @NotBlank Integer id) {
        return userService.findUserById(id);
    }

    @GetMapping("/{id}/friends")
    public List<User> getUserFriends(@PathVariable @NotBlank Integer id) {
        return userService.getUserFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable @NotBlank Integer id, @PathVariable @NotBlank Integer otherId) {
        return userService.getCommonFriends(id, otherId);
    }

    @GetMapping("/{id}/recommendations")
    public List<Film> getRecommendations(@PathVariable @NotNull Integer id) {
        return userService.getRecommendations(id);
    }

    @GetMapping("/{id}/feed")
    public List<Event> getEventsByUserId(@PathVariable @NotNull Integer id) {
        return userService.getEventsByUserId(id);
    }
    @PutMapping()
    public User update(@NotNull @Valid @RequestBody User user) {
        userService.update(user);
        log.info("{} has updated", user);
        return user;
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable @NotBlank Integer id, @PathVariable @NotBlank Integer friendId) {
        userService.addFriend(id, friendId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable @NotBlank Integer id, @PathVariable @NotBlank Integer friendId) {
        userService.removeFriend(id, friendId);
    }

    @PostMapping()
    public User add(@NotNull @Valid @RequestBody User user) {
        return userService.add(user);
    }

}
