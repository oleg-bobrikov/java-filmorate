package ru.yandex.practicum.filmorate.storage.impl;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;

    @Override
    public User add(User user) {
        user.setId(nextId++);
        return users.put(user.getId(), user);
    }

    @Override
    public User update(User user) {
        users.put(user.getId(), user);
        return user;
    }

    /*@Override
    public void deleteUserById(int id) {
        users.remove(id);
    }*/

    @Override
    public Optional<User> findUserById(int id) {
        return Optional.of(users.get(id));
    }

    @Override
    public void addFriend(User user, User friend) {
        user.getFriends().add(friend.getId());
    }

    @Override
    public List<User> getUserFriendsById(int id) {
        return users.get(id).getFriends().stream()
                .map(users::get)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void removeFriend(User user, User friend) {
        user.getFriends().remove(friend.getId());
    }

    @Override
    public void deleteUserById(int id){
        users.remove(id);
    }


}
