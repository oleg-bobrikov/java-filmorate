package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import javax.sql.DataSource;
import java.util.*;

@Component
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

        String sql = "insert into users (email, login, user_name, birthday) VALUES(:email, :login, :user_name, :birthday);";

        Map<String, Object> params = new HashMap<>();
        params.put("email", user.getEmail());
        params.put("login", user.getLogin());
        params.put("user_name", user.getName());
        params.put("birthday", user.getBirthday());

        int rowsAffected = namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(params), generatedKeyHolder);

        Integer id = Objects.requireNonNull(generatedKeyHolder.getKey()).intValue();

        log.info("rowsAffected = {}, id={}", rowsAffected, id);

        user.setId(id);

        return user;
    }

    @Override
    public User update(User user) {
        String sql = "update users  set email = :email, login = :login, user_name = :user_name, birthday = :birthday where id = :id";

        Map<String, Object> params = new HashMap<>();
        params.put("id", user.getId());
        params.put("login", user.getLogin());
        params.put("email", user.getEmail());
        params.put("user_name", user.getName());
        params.put("birthday", user.getBirthday());

        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(params), generatedKeyHolder);
        log.info("Пользователь с идентификатором {} изменен.", user.getId());

        return user;
    }


    @Override
    public Optional<User> findUserById(int id) {
        return Optional.ofNullable(getUserById(id));
    }

    private User getUserById(int id) {
        String sql = "select * from users where id = ?";
        List<User> users = jdbcTemplate.query(sql, new UserRowMapper(), id);
        if (users.isEmpty()) {
            log.info("Пользователь с идентификатором {} не найден.", id);
            return null;
        } else {
            log.info("Найден пользователь c идентификатором : {}", id);
            User user = users.get(0);
            sql = "select friend_id from user_friends where user_id = ?";
            List<Integer> friends = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt("friend_id"), id);
            user.setFriends(new HashSet<>(friends));
            return user;
        }
    }

    @Override
    public void addFriend(User user, User friend) {
        String sql = "insert into user_friends (user_id, friend_id) " +
                "values (:userId, :friendId);";

        Map<String, Object> params = new HashMap<>();
        params.put("userId", user.getId());
        params.put("friendId", friend.getId());

        namedParameterJdbcTemplate.update(sql, params);
        log.info("Для пользователя с идентификатором {} добавлен друг с идентификатором {}", user.getId(), friend.getId());
    }

    @Override
    public List<User> findUserFriendsById(int id) {
        String sql = "select * from users where id in " +
                "(select friend_id from user_friends where user_id = ?)";

        HashMap<Integer, User> friends = new HashMap<>();
        jdbcTemplate.query(sql, new UserRowMapper(), id)
                .forEach(user -> friends.put(user.getId(), user));

        sql = "SELECT" +
                "    user_id," +
                "    friend_id" +
                " FROM" +
                "    user_friends" +
                " WHERE" +
                "    user_id IN (" +
                "        SELECT" +
                "            id" +
                "        FROM" +
                "            users" +
                "        WHERE" +
                "            id IN (" +
                "                SELECT" +
                "                    friend_id" +
                "                FROM" +
                "                    user_friends" +
                "                WHERE" +
                "                    user_id = :userId" +
                "            )" +
                "    )";

        SqlParameterSource parameters = new MapSqlParameterSource("userId", id);
        SqlRowSet rs = namedParameterJdbcTemplate.queryForRowSet(sql, parameters);

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
        jdbcTemplate.query(sql, new UserRowMapper()).forEach(user -> results.put(user.getId(), user));

        sql = "select user_id, friend_id from user_friends";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql);
        while (rs.next()) {
            results.get(rs.getInt("user_id"))
                    .getFriends()
                    .add(rs.getInt("friend_id"));
        }
        return new ArrayList<>(results.values());
    }

    @Override
    public void removeFriend(User user, User friend) {
        String sql = "delete from USER_FRIENDS " +
                "where USER_ID = :USER_ID and FRIEND_ID = :FRIEND_ID";

        Map<String, Object> params = new HashMap<>();
        params.put("USER_ID", user.getId());
        params.put("FRIEND_ID", friend.getId());

        namedParameterJdbcTemplate.update(sql, params);
        log.info("Для пользователя с идентификатором {} удален друг с идентификатором {}", user.getId(), friend.getId());

        // create a history log separately,
        // cause log could not being created by "on delete trigger"
        // (observed ON DELETE CASCADE PROBLEM. It's deleting several rows instead of one which is required by Postman)
        sql = "insert into EVENTS (EVENT_TIMESTAMP, EVENT_TYPE, ENTITY_ID, USER_ID, OPERATION) " +
                "VALUES(CURRENT_TIMESTAMP, :EVENT_TYPE, :ENTITY_ID, :USER_ID, :OPERATION)";
        params.put("EVENT_TYPE", "FRIEND");
        params.put("ENTITY_ID", friend.getId());
        params.put("OPERATION", "REMOVE");
        namedParameterJdbcTemplate.update(sql, params);
    }


    @Override
    public void deleteUserById(int id) {
        String sql = " delete from users where id = :user_id";

        Map<String, Object> params = new HashMap<>();
        params.put("user_id", id);

        int rowsAffected = namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(params), generatedKeyHolder);
        if (rowsAffected > 0) {
            log.info("Фильм с идентификатором {} удален", id);
        } else {
            log.info("Фильм с идентификатором {} не найден", id);
        }
    }
}