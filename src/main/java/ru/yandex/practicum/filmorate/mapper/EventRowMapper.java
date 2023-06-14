package ru.yandex.practicum.filmorate.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.Event;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class EventRowMapper implements RowMapper<Event> {
    @Override
    public Event mapRow(ResultSet rs, int rowNum) throws SQLException {

        return Event.builder()
                .timestamp(rs.getTimestamp("EVENT_TIMESTAMP").toInstant())
                .userId(rs.getInt("USER_ID"))
                .eventType(rs.getString("EVENT_TYPE"))
                .operation(rs.getString("OPERATION"))
                .eventId(rs.getInt("EVENT_ID"))
                .entityId(rs.getInt("ENTITY_ID"))
                .build();
    }
}
