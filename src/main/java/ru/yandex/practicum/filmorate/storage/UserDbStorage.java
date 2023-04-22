package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.UserDao;
import ru.yandex.practicum.filmorate.dto.User;

import java.util.List;
import java.util.Optional;

@Component
@Primary
public class UserDbStorage implements UserStorage {

    @Autowired
    @Qualifier("userDaoImplH2")
    private UserDao userDao;

    @Override
    public User add(User user) {
        return userDao.add(user);
    }

    @Override
    public User update(User user) {
        return userDao.update(user);
    }

    @Override
    public void deleteUserById(int id) {
        userDao.deleteUserById(id);
    }

    @Override
    public Optional<User> findUserById(int id) {
        User user = userDao.getUserById(id);
        if (user == null) {
            return Optional.empty();
        } else {
            return Optional.of(user);
        }
    }

    @Override
    public void addFriend(User user, User friend) {
        userDao.addFriend(user, friend);
    }

    @Override
    public List<User> getUserFriendsById(int id) {
        return userDao.getUserFriends(id);
    }

    @Override
    public List<User> getUsers() {
        return userDao.getUsers();
    }

    @Override
    public void removeFriend(User user, User friend) {
        userDao.removeFriend(user, friend);
    }
}
