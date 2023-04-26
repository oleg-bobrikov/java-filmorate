package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import javax.sql.DataSource;
import java.util.*;

@Component
@Primary
@Slf4j
public class UserH2Storage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final GeneratedKeyHolder generatedKeyHolder;

    public UserH2Storage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        DataSource dataSource = jdbcTemplate.getDataSource();
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(Objects.requireNonNull(dataSource));
        generatedKeyHolder = new GeneratedKeyHolder();
    }

    @Override
    public User add(User user) {

        String sql = "insert into users (email, login, \"name\", birthday) VALUES(:email, :login, :name, :birthday);";

        Map<String, Object> params = new HashMap<>();
        params.put("email", user.getEmail());
        params.put("login", user.getLogin());
        params.put("name", user.getName());
        params.put("birthday", user.getBirthday());

        int rowsAffected = namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(params), generatedKeyHolder);

        Integer id = Objects.requireNonNull(generatedKeyHolder.getKey()).intValue();

        log.info("rowsAffected = {}, id={}", rowsAffected, id);

        user.setId(id);

        return user;
    }

    @Override
    public User update(User user) {
        String sql = "update users  set email = :email, login = :login, \"name\" = :name, birthday = :birthday where id = :id";

        Map<String, Object> params = new HashMap<>();
        params.put("id", user.getId());
        params.put("login", user.getLogin());
        params.put("email", user.getEmail());
        params.put("name", user.getName());
        params.put("birthday", user.getBirthday());

        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(params), generatedKeyHolder);
        log.info("Пользователь с идентификатором {} изменен.", user.getId());

        return user;
    }

    @Override
    public void deleteUserById(int id) {
        String sql = "delete from users " +
                "where id = :id;";

        Map<String, Object> params = new HashMap<>();
        params.put("id", id);

        int rowsAffected = namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(params), generatedKeyHolder);
        if (rowsAffected > 0) {
            log.info("Пользователь с идентификатором {} удален.", id);
        } else {
            log.info("Пользователь с идентификатором {} не найден.", id);
        }
    }

    @Override
    public Optional<User> findUserById(int id) {
        return Optional.ofNullable(getUserById(id));
    }

    private User getUserById(int id) {
        String sql = "select * from users where id = ?";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sql, id);
        if (userRows.next()) {
            User user = User.builder()
                    .id(id)
                    .email(userRows.getString("email"))
                    .login(userRows.getString("login"))
                    .name(userRows.getString("name"))
                    .birthday(Objects.requireNonNull(userRows.getDate("birthday")).toLocalDate())
                    .build();

            log.info("Найден пользователь c идентификатором : {}", id);
            sql = "select friend_id from user_friends where user_id = ?";
            List<Integer> friends = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt("friend_id"), id);
            user.setFriends(new HashSet<>(friends));

            return user;
        } else {
            log.info("Пользователь с идентификатором {} не найден.", id);
            return null;
        }
    }

    @Override
    public void addFriend(User user, User friend) {
        String sql = "insert into user_friends (user_id, friend_id) " +
                "values (:userId, :friendId);";

        Map<String, Object> params = new HashMap<>();
        params.put("userId", user.getId());
        params.put("friendId", friend.getId());

        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(params), generatedKeyHolder);
        log.info("Для пользователя с идентификатором {} добавлен друг с идентификатором {}", user.getId(), friend.getId());
    }

    @Override
    public List<User> getUserFriendsById(int id) {
        String sql = "select * from users where id in " +
                "(" +
                "select friend_id from user_friends where user_id = :userId" +
                ")";

        Map<String, Integer> params = new HashMap<>();
        params.put("userId", id);
        SqlRowSet rs = namedParameterJdbcTemplate.queryForRowSet(sql, new MapSqlParameterSource(params));
        HashMap<Integer, User> friends = new HashMap<>();
        while (rs.next()) {
            User user = User.builder()
                    .id(rs.getInt("id"))
                    .email(rs.getString("email"))
                    .login(rs.getString("login"))
                    .name(rs.getString("name"))
                    .birthday(Objects.requireNonNull(rs.getDate("birthday")).toLocalDate())
                    .build();
            friends.put(user.getId(), user);
        }

        sql = "select user_id, friend_id from user_friends where user_id in " +
                "(" +
                "select id from users where id in " +
                "(" +
                "select friend_id from user_friends where user_id = :userId" +
                ")" +
                ")";
        rs = namedParameterJdbcTemplate.queryForRowSet(sql, new MapSqlParameterSource(params));
        while (rs.next()) {
            friends.get(rs.getInt("user_id"))
                    .getFriends()
                    .add(rs.getInt("friend_id"));
        }
        return new ArrayList<>(friends.values());

    }

    @Override
    public List<User> getUsers() {
        String sql = "select * from users";
        HashMap<Integer, User> results = new HashMap<>();
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql);
        while (rs.next()) {
            User user = User.builder()
                    .id(rs.getInt("id"))
                    .email(rs.getString("email"))
                    .login(rs.getString("login"))
                    .name(rs.getString("name"))
                    .birthday(Objects.requireNonNull(rs.getDate("birthday")).toLocalDate())
                    .build();
            results.put(user.getId(), user);
        }

        sql = "select user_id, friend_id from user_friends";
        rs = jdbcTemplate.queryForRowSet(sql);
        while (rs.next()) {
            results.get(rs.getInt("user_id"))
                    .getFriends()
                    .add(rs.getInt("friend_id"));
        }
        return new ArrayList<>(results.values());
    }

    @Override
    public void removeFriend(User user, User friend) {
        String sql = "delete from user_friends " +
                "where user_id = :userId and friend_id = :friendId;";

        Map<String, Object> params = new HashMap<>();
        params.put("userId", user.getId());
        params.put("friendId", friend.getId());

        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(params), generatedKeyHolder);
        log.info("Для пользователя с идентификатором {} удален друг с идентификатором {}", user.getId(), friend.getId());
    }
}
