package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {
    User add(User user);

    User update(User user);

    Optional<User> findUserById(int id);

    void addFriend(User user, User friend);

    List<User> findUserFriendsById(int id);

    List<User> getUsers();

    void removeFriend(User user, User friend);

    void deleteUserById(int id);
}
