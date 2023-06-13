package ru.yandex.practicum.filmorate.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.Director;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
@RequiredArgsConstructor
@Slf4j
public class DirectorRowMapper implements RowMapper<Director> {
    @Override
    public Director mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Director.builder()
                .id(rs.getInt("ID"))
                .name(rs.getString("DIRECTOR_NAME"))
                .build();
    }
}
