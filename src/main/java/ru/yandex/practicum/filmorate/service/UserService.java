package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.User;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(Integer id, Integer friendId) {
        if (id == null) {
            throw new ValidationException("user id  not set.");
        }
        if (friendId == null) {
            throw new ValidationException("friend id not set");
        }
        if (id <= 0) {
            throw new ValidationException("user id should be positive value.");
        }
        if (friendId <= 0) {
            throw new ValidationException("friend id should be positive value.");
        }
        User user = userStorage.getUserById(id);
        if (user == null) {
            throw new NotFoundException("user with id=" + id + " not found.");
        }
        User friend = userStorage.getUserById(friendId);
        if (friend == null) {
            throw new NotFoundException("user with id=" + friendId + " not found.");
        }
        Set<Integer> friends = user.getFriends();
        if (friends == null) {
            friends = new HashSet<>();
        }
        friends.add(friendId);
        user.setFriends(friends);

        Set<Integer> otherFriends = friend.getFriends();
        if (otherFriends == null) {
            otherFriends = new HashSet<>();
        }
        otherFriends.add(id);
        friend.setFriends(otherFriends);
    }

    public User add(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        userStorage.add(user);
        log.info(String.format("%s has created", user));
        return user;
    }

    public List<User> getCommonFriends(Integer id, Integer otherId) {
        Set<Integer> friends = getUserById(id).getFriends();
        Set<Integer> otherFriends = getUserById(otherId).getFriends();

        if (friends == null || otherFriends == null) {
            return new ArrayList<>();
        }

        return userStorage
                .getUserById(id)
                .getFriends()
                .stream()
                .filter(otherFriends::contains)
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }

    public List<User> getUserFriends(Integer id) {
        User user = userStorage.getUserById(id);
        Set<Integer> friends = user.getFriends();
        if (friends == null) {
            friends = new HashSet<>();
        }
        return friends.stream().map(userStorage::getUserById).collect(Collectors.toList());

    }

    public void removeFriend(Integer id, Integer friendId) {
        if (id == null) {
            throw new ValidationException("user id  not set.");
        }
        if (friendId == null) {
            throw new ValidationException("friend id not set");
        }
        if (id <= 0) {
            throw new ValidationException("user id should be positive value.");
        }
        if (friendId <= 0) {
            throw new ValidationException("friend id should be positive value.");
        }
        User user = userStorage.getUserById(id);
        if (user == null) {
            throw new NotFoundException("user with id=" + id + " not found.");
        }
        User friend = userStorage.getUserById(friendId);
        if (friend == null) {
            throw new NotFoundException("user with id=" + friendId + " not found.");
        }
        Set<Integer> friends = user.getFriends();
        if (friends == null) {
            friends = new HashSet<>();
        }
        friends.remove(friendId);
        user.setFriends(friends);

        Set<Integer> otherFriends = friend.getFriends();
        if (otherFriends == null) {
            otherFriends = new HashSet<>();
        }
        otherFriends.remove(id);
        friend.setFriends(otherFriends);
    }

    public User getUserById(Integer id) {
        if (id == null) {
            throw new ValidationException("Не заполен параметр id.");
        }

        if (id <= 0) {
            throw new ValidationException("Параметр id должен быть положительным целым числом.");
        }
        User user = userStorage.getUserById(id);
        if (user == null) {
            throw new NotFoundException("Пользователь с идентификатором " + id + " не найден.");
        }

        return user;
    }

    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    public void update(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        User userToUpdate = getUserById(user.getId());
        userStorage.update(user);
    }
}
