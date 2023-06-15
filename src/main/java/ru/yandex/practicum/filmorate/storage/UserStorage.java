package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.dto.Event;

import ru.yandex.practicum.filmorate.dto.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {
    User add(User user);

    User update(User user);

    Optional<User> findUserById(int id);

    void addFriend(User user, User friend);

    List<User> getUserFriendsById(int id);

    List<User> getUsers();

    void removeFriend(User user, User friend);

    List<Event> getEventsByUserId(Integer userId);

    void deleteUserById(int id);
}
