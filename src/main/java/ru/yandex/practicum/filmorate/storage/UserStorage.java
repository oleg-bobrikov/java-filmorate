package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.dto.User;

public interface UserStorage {
    User add(User user);
    User update(User user);
    void delete(int id);
    User getUserById(int id);
}
