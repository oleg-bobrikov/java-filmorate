package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.User;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    @Autowired
    private final UserStorage userStorage;

    public void addFriend(Integer id, Integer friendId) {
        Optional<User> userOpt = userStorage.findUserById(id);
        if (userOpt.isEmpty()) {
            throw new NotFoundException("user with id=" + id + " not found.");
        }
        Optional<User> friendOpt = userStorage.findUserById(friendId);
        if (friendOpt.isEmpty()) {
            throw new NotFoundException("user with id=" + friendId + " not found.");
        }
        userStorage.addFriend(userOpt.get(), friendOpt.get());

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
        Set<Integer> friends = findUserById(id).getFriends();
        Set<Integer> otherFriends = findUserById(otherId).getFriends();

        return friends.stream()
                .filter(otherFriends::contains)
                .map(userStorage::findUserById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public List<User> getUserFriends(Integer id) {
        return userStorage.getUserFriendsById(id);
    }

    public void removeFriend(Integer id, Integer friendId) {
        Optional<User> user = userStorage.findUserById(id);
        if (user.isEmpty()) {
            throw new NotFoundException("user with id=" + id + " not found.");
        }
        Optional<User> friend = userStorage.findUserById(friendId);
        if (friend.isEmpty()) {
            throw new NotFoundException("user with id=" + friendId + " not found.");
        }
        userStorage.removeFriend(user.get(), friend.get());
    }

    public User findUserById(Integer id) {
        Optional<User> user = userStorage.findUserById(id);
        if (user.isEmpty()) {
            throw new NotFoundException("Пользователь с идентификатором " + id + " не найден.");
        }

        return user.get();
    }

    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    public void update(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        findUserById(user.getId());
        userStorage.update(user);
    }
}
