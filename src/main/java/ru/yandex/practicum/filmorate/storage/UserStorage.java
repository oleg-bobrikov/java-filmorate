package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.dto.User;

import java.util.List;

public interface UserStorage {
    void create(User user);
    void update(User user);
    void delete(User user);
    List<User> findAll();
    User getUserById(int id);
}
