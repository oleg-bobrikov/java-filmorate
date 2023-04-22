package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.dto.User;

import java.util.List;

public interface UserDao {
    User add(User user);

    User getUserById(int id);

    User update(User user);

    List<User> getUsers();

    void addFriend(User user, User friend);

    List<User> getUserFriends(int id);

    void removeFriend(User user, User friend);

    void deleteUserById(int id);
}
