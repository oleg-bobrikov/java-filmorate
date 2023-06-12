package ru.yandex.practicum.filmorate.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
@RequiredArgsConstructor
@Slf4j
public class GenreRowMapper implements RowMapper<Genre> {

    @Override
    public Genre mapRow(ResultSet rs, int rowNum) throws SQLException {

        return Genre.builder()
                .id(rs.getInt("ID"))
                .name(rs.getString("GENRE_NAME"))
                .build();
    }
}
