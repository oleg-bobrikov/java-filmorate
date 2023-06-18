package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


@Component
@Slf4j
public class DirectorH2Storage implements DirectorStorage {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final GeneratedKeyHolder generatedKeyHolder;

    public DirectorH2Storage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        DataSource dataSource = jdbcTemplate.getDataSource();
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(Objects.requireNonNull(dataSource));
        generatedKeyHolder = new GeneratedKeyHolder();
    }

    @Override
    public List<Director> findAll() {
        String sqlQueryGetAll = "SELECT ID, DIRECTOR_NAME FROM DIRECTORS ORDER BY ID";
        return jdbcTemplate.query(sqlQueryGetAll, this::mapRowToDirector);
    }


    @Override
    public Optional<Director> findDirectorById(Integer id) {
        String sqlQueryGetDirector = "select * from DIRECTORS where Id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sqlQueryGetDirector, this::mapRowToDirector, id));
        } catch (DataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Director> createDirector(Director director) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("DIRECTORS")
                .usingGeneratedKeyColumns("ID");

        int id = simpleJdbcInsert.executeAndReturnKey(director.toMap()).intValue();

        director.setId(id);

        return Optional.of(director);
    }

    @Override
    public Optional<Optional<Director>> updateDirector(Director director) {
        String sqlQueryUpdateDirector = "update DIRECTORS " +
                "set DIRECTOR_NAME = ?" +
                "where ID = ?";
        try {

            jdbcTemplate.update(sqlQueryUpdateDirector,
                    director.getName(),
                    director.getId());
        } catch (DataAccessException e) {
            return Optional.empty();
        }

        return Optional.ofNullable(findDirectorById(director.getId()));
    }


    @Override
    public void removeDirector(int id) {
        String sqlQueryDeleteDirector = "DELETE FROM DIRECTORS " +
                "where ID = ?";
        jdbcTemplate.update(sqlQueryDeleteDirector, id);

        String sqlQueryDeleteDirectorInDirectorFilm = "DELETE FROM directors_films " +
                "where DIRECTOR_ID = ?";
        jdbcTemplate.update(sqlQueryDeleteDirectorInDirectorFilm, id);

    }

    private Director mapRowToDirector(ResultSet resultSet, int rowNum) throws SQLException {

        return Director.builder()
                .id(resultSet.getInt("ID"))
                .name(Objects.requireNonNull(resultSet.getString("DIRECTOR_NAME")))
                .build();
    }

    @Override
    public void updateFilmDirector(Film film, Set<Director> directors) {

        String sql = "delete from directors_films where film_id = :film_id";
        Map<String, Object> params = new HashMap<>();
        params.put("film_id", film.getId());

        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(params), generatedKeyHolder);

        for (Director director : directors) {
            if (director == null) {
                continue;
            }
            params.put("director_id", director.getId());
            sql = "merge into directors_films key(film_id, director_id) " +
                    "values(:film_id, :director_id)";
            namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(params), generatedKeyHolder);
        }
    }
}
