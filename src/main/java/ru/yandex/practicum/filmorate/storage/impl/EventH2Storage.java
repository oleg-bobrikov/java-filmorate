package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.mapper.EventRowMapper;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.EventStorage;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class EventH2Storage implements EventStorage {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final EventRowMapper eventRowMapper;

    public EventH2Storage(JdbcTemplate jdbcTemplate, EventRowMapper eventRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.eventRowMapper = eventRowMapper;
        DataSource dataSource = jdbcTemplate.getDataSource();
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(Objects.requireNonNull(dataSource));
    }

    @Override
    public List<Event> getEventsByUserId(Integer userId) {
        String sql = "select * from EVENTS where USER_ID = :USER_ID order by EVENT_TIMESTAMP";
        HashMap<String, Object> params = new HashMap<>();
        params.put("USER_ID", userId);
        return namedParameterJdbcTemplate.query(sql, params, eventRowMapper);
    }

}
