package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.User;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public void addFriend(Integer id, Integer friendId) {
        if (id == null) {
            throw new ValidationException("user id  not set.");
        }
        if (friendId == null) {
            throw new ValidationException("friend id not set");
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
        friends.add(friendId);
        user.setFriends(friends);

        Set<Integer> otherFriends = friend.getFriends();
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

        return friends.stream()
                .filter(otherFriends::contains)
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }

    public List<User> getUserFriends(Integer id) {
        Set<Integer> friends = userStorage.getUserById(id).getFriends();
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
        User user = userStorage.getUserById(id);
        if (user == null) {
            throw new NotFoundException("user with id=" + id + " not found.");
        }
        User friend = userStorage.getUserById(friendId);
        if (friend == null) {
            throw new NotFoundException("user with id=" + friendId + " not found.");
        }
        Set<Integer> friends = user.getFriends();
        friends.remove(friendId);

        Set<Integer> otherFriends = friend.getFriends();
        otherFriends.remove(id);
    }

    public User getUserById(Integer id) {
        if (id == null) {
            throw new ValidationException("Не заполен параметр id.");
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
        getUserById(user.getId());
        userStorage.update(user);
    }
}
