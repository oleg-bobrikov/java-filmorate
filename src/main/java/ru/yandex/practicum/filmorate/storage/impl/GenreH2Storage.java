package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.Film;
import ru.yandex.practicum.filmorate.dto.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import javax.sql.DataSource;
import java.util.*;

@Component
@Slf4j
public class GenreH2Storage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final GeneratedKeyHolder generatedKeyHolder;

    public GenreH2Storage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        DataSource dataSource = jdbcTemplate.getDataSource();
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(Objects.requireNonNull(dataSource));
        generatedKeyHolder = new GeneratedKeyHolder();
    }

    public List<Genre> getAll() {
        String sql = "select * from genres";
        HashMap<Integer, Genre> results = new HashMap<>();
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql);
        while (rs.next()) {
            Genre genre = Genre.builder()
                    .id(rs.getInt("id"))
                    .name(rs.getString("name"))
                    .build();
            results.put(genre.getId(), genre);
        }
        return new ArrayList<>(results.values());
    }

    @Override
    public Genre getGenreById(Integer id) {
        String sql = "select * from genres where id = ?";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql, id);
        if (rs.next()) {
            Genre genre = Genre.builder()
                    .id(id)
                    .name(rs.getString("name"))
                    .build();

            log.info("Найден жанр фильма c идентификатором: {}", id);

            return genre;
        } else {
            log.info("Жанр фильма с идентификатором {} не найден.", id);
            return null;
        }
    }

    @Override
    public void updateFilmGenres(Film film, Set<Genre> genres) {

        String sql = "delete from film_genres where film_id = :film_id";
        Map<String, Object> params = new HashMap<>();
        params.put("film_id", film.getId());

        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(params), generatedKeyHolder);

        for (Genre genre : genres) {
            if (genre == null) {
                continue;
            }
            params.put("genre_id", genre.getId());
            sql = "merge into film_genres key(film_id, genre_id) " +
                    "values(:film_id, :genre_id)";
            namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(params), generatedKeyHolder);
        }
    }
}
