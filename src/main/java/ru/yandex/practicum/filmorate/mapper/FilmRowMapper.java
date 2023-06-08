package ru.yandex.practicum.filmorate.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.Film;
import ru.yandex.practicum.filmorate.dto.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j

public class FilmRowMapper implements RowMapper<Film> {
    private final MpaStorage mpaStorage;

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        int mpaId = rs.getInt("MPA_FILM_RATING_ID");
        Mpa mpa = mpaId == 0 ? null : mpaStorage.getMpaById(mpaId);

        return  Film.builder()
                .id(rs.getInt("ID"))
                .name(rs.getString("FILM_NAME"))
                .description(rs.getString("DESCRIPTION"))
                .releaseDate(Objects.requireNonNull(rs.getDate("RELEASE_DATE")).toLocalDate())
                .duration(rs.getInt("DURATION"))
                .mpa(mpa)
                .build();

    }
}