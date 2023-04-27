package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.Film;
import ru.yandex.practicum.filmorate.dto.Genre;
import ru.yandex.practicum.filmorate.dto.Mpa;
import ru.yandex.practicum.filmorate.dto.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import javax.sql.DataSource;
import java.util.*;

@Component
@Primary
@Slf4j
public class FilmH2Storage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    private final GenreStorage genreStorage;

    private final MpaStorage mpaStorage;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final GeneratedKeyHolder generatedKeyHolder;

    public FilmH2Storage(JdbcTemplate jdbcTemplate, GenreStorage genreStorage, MpaStorage mpaStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
        DataSource dataSource = jdbcTemplate.getDataSource();
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(Objects.requireNonNull(dataSource));
        generatedKeyHolder = new GeneratedKeyHolder();
    }

    @Override
    public Film add(Film film) {
        // update mpa
        Mpa mpa = film.getMpa();
        if (mpa != null) {
            film.setMpa(mpaStorage.getMpaById(mpa.getId()));
        }

        String sql = "insert into films (film_name, description, release_date, duration, mpa_film_rating_id) " +
                "VALUES(:film_name, :description, :release_date, :duration, :mpa_film_rating_id);";

        Map<String, Object> params = new HashMap<>();
        params.put("film_name", film.getName());
        params.put("description", film.getDescription());
        params.put("release_date", film.getReleaseDate());
        params.put("duration", film.getDuration());
        params.put("mpa_film_rating_id", mpa == null ? null : mpa.getId());

        int rowsAffected = namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(params), generatedKeyHolder);

        Integer id = Objects.requireNonNull(generatedKeyHolder.getKey()).intValue();

        log.info("rowsAffected = {}, id={}", rowsAffected, id);

        film.setId(id);

        // update genres
        Set<Genre> newGenres = new TreeSet<>(Comparator.comparing(Genre::getId));

        for (Genre genre : film.getGenres()) {
            Integer genreId = genre.getId();
            newGenres.add(genreStorage.getGenreById(genreId));
        }
        genreStorage.updateFilmGenres(film, newGenres);
        film.setGenres(newGenres);

        return film;
    }

    @Override
    public Film update(Film film) {
        // update mpa
        Mpa mpa = film.getMpa();
        if (mpa != null) {
            film.setMpa(mpaStorage.getMpaById(mpa.getId()));
        }

        String sql =
                "update films set " +
                        " film_name = :film_name, " +
                        " description = :description, " +
                        " release_date = :release_date, " +
                        " duration = :duration, " +
                        " mpa_film_rating_id = :mpa_film_rating_id " +
                        "where id = :id";

        Map<String, Object> params = new HashMap<>();
        params.put("film_name", film.getName());
        params.put("description", film.getDescription());
        params.put("release_date", film.getReleaseDate());
        params.put("duration", film.getDuration());
        params.put("id", film.getId());
        params.put("mpa_film_rating_id", mpa == null ? null : mpa.getId());
        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(params), generatedKeyHolder);
        log.info("Фильм с идентификатором {} изменен.", film.getId());

        int rowsAffected = namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(params), generatedKeyHolder);
        Integer id = Objects.requireNonNull(generatedKeyHolder.getKey()).intValue();
        log.info("rowsAffected = {}, id={}", rowsAffected, id);

        // update genres
        Set<Genre> newGenres = new TreeSet<>(Comparator.comparing(Genre::getId));
        for (Genre genre : film.getGenres()) {
            newGenres.add(genreStorage.getGenreById(genre.getId()));
        }
        genreStorage.updateFilmGenres(film, newGenres);
        film.setGenres(newGenres);

        return film;
    }

    @Override
    public void deleteFilmById(int id) {
        String sql = "delete from film_genres where film_id = :film_id; " +
                "delete from film_likes where film_id = :film_id; " +
                "delete from films where id = :film_id; ";

        Map<String, Object> params = new HashMap<>();
        params.put("film_id", id);

        int rowsAffected = namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(params), generatedKeyHolder);
        if (rowsAffected > 0) {
            log.info("Фильм с идентификатором {} удален", id);
        } else {
            log.info("Фильм с идентификатором {} не найден", id);
        }

    }

    @Override
    public List<Film> getFilms() {
        String sql = "SELECT " +
                " FILMS.ID AS ID, " +
                " FILMS.FILM_NAME AS FILM_NAME, " +
                " FILMS.DESCRIPTION AS DESCRIPTION, " +
                " FILMS.RELEASE_DATE AS RELEASE_DATE, " +
                " FILMS.DURATION AS DURATION, " +
                " IFNULL (FILMS.MPA_FILM_RATING_ID, 0) AS MPA_FILM_RATING_ID, " +
                " MFR.mpa_film_rating_name AS MPA_FILM_RATING_NAME " +
                "FROM" +
                " FILMS AS FILMS " +
                "LEFT JOIN MPA_FILM_RATINGS AS MFR ON MFR.ID = FILMS.MPA_FILM_RATING_ID;";

        HashMap<Integer, Film> results = new HashMap<>();
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql);
        while (rs.next()) {
            int mpaId = rs.getInt("MPA_FILM_RATING_ID");
            Mpa mpa = mpaId == 0 ? null : Mpa.builder()
                    .id(mpaId)
                    .name(rs.getString("MPA_FILM_RATING_NAME"))
                    .build();
            Film film = Film.builder()
                    .id(rs.getInt("ID"))
                    .name(rs.getString("film_name"))
                    .description(rs.getString("DESCRIPTION"))
                    .releaseDate(Objects.requireNonNull(rs.getDate("RELEASE_DATE")).toLocalDate())
                    .duration(rs.getInt("DURATION"))
                    .mpa(mpa)
                    .build();
            results.put(film.getId(), film);
        }

        sql = "select film_id, user_id from film_likes";
        rs = jdbcTemplate.queryForRowSet(sql);
        while (rs.next()) {
            results.get(rs.getInt("film_id"))
                    .getLikes()
                    .add(rs.getInt("user_id"));
        }
        sql = "SELECT " +
                " film_genres.film_id AS film_id, " +
                " film_genres.genre_id AS genre_id, " +
                " genres.genre_name AS genre_name " +
                "FROM " +
                " film_genres AS film_genres " +
                "INNER JOIN " +
                " genres AS genres " +
                "ON film_genres.GENRE_ID = genres.id";
        rs = jdbcTemplate.queryForRowSet(sql);
        while (rs.next()) {
            int filmId = rs.getInt("FILM_ID");
            int genreId = rs.getInt("GENRE_ID");
            Film film = results.get(filmId);
            Set<Genre> genres = film.getGenres();
            Genre genre = genreStorage.getGenreById(genreId);
            genres.add(genre);
        }
        return new ArrayList<>(results.values());
    }

    @Override
    public Optional<Film> getFilmById(int id) {
        String sql = "select * from films where id = ?";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql, id);
        if (rs.next()) {
            Mpa mpa = mpaStorage.getMpaById(rs.getInt("MPA_FILM_RATING_ID"));

            Film film = Film.builder()
                    .id(rs.getInt("ID"))
                    .name(rs.getString("FILM_NAME"))
                    .description(rs.getString("DESCRIPTION"))
                    .releaseDate(Objects.requireNonNull(rs.getDate("RELEASE_DATE")).toLocalDate())
                    .duration(rs.getInt("DURATION"))
                    .mpa(mpa)
                    .build();

            log.info("Найден фильм c иднетификатором: {}", id);

            // get genres
            sql = "select GENRE_ID from film_genres where FILM_ID = ?";
            rs = jdbcTemplate.queryForRowSet(sql, id);
            while (rs.next()) {
                film.getGenres().add(genreStorage.getGenreById(rs.getInt("GENRE_ID")));
            }

            // get likes
            sql = "select USER_ID from FILM_LIKES where FILM_ID = ?";
            rs = jdbcTemplate.queryForRowSet(sql, id);
            while (rs.next()) {
                film.getLikes().add(rs.getInt("USER_ID"));
            }

            return Optional.of(film);
        } else {
            log.info("Фильм с идентификатором {} не найден.", id);
            return Optional.empty();
        }
    }

    @Override
    public void addLike(Film film, User user) {
        String sql = "merge into film_likes key (film_id, user_id) VALUES(:film_id, :user_id)";

        Map<String, Object> params = new HashMap<>();
        params.put("film_id", film.getId());
        params.put("user_id", user.getId());

        int updatedRows = namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(params), generatedKeyHolder);
        log.info("Updated rows: {}", updatedRows);
        log.info("Фильм с идентификатором {} получил лайк от пользователя с идентификатором {}", film.getId(), user.getId());

    }

    @Override
    public void removeLike(Film film, User user) {
        String sql = "delete from film_likes where film_id = :film_id and user_id = :user_id";

        Map<String, Object> params = new HashMap<>();
        params.put("film_id", film.getId());
        params.put("user_id", user.getId());

        int updatedRows = namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(params), generatedKeyHolder);

        log.info("Updated rows: {}", updatedRows);
        log.info("Фильм с идентификатором {} получил лайк от пользователя с идентификатором {}", film.getId(), user.getId());
    }

    @Override
    public List<Film> getPopular(int count) {

        String sql = "DROP TABLE IF EXISTS popular_films_tmp";
        jdbcTemplate.execute(sql);
        sql = " CREATE TEMPORARY TABLE IF NOT EXISTS popular_films_tmp (" +
                "    id INT NOT NULL UNIQUE," +
                "    film_name VARCHAR(100)," +
                "    DESCRIPTION VARCHAR(200)," +
                "    RELEASE_DATE TIMESTAMP NOT NULL," +
                "    DURATION INTEGER NOT NULL," +
                "    MPA_FILM_RATING_ID INTEGER," +
                "    MPA_FILM_RATING_NAME VARCHAR(50)" +
                ")";
        jdbcTemplate.execute(sql);
        sql = " INSERT INTO" +
                "    popular_films_tmp (" +
                "        SELECT" +
                "            FILMS.ID," +
                "            FILMS.FILM_NAME," +
                "            FILMS.DESCRIPTION," +
                "            FILMS.RELEASE_DATE," +
                "            FILMS.DURATION," +
                "            IFNULL(MFR.ID, 0) AS MPA_FILM_RATING_ID," +
                "            MFR.MPA_FILM_RATING_NAME AS MPA_FILM_RATING_NAME" +
                "        FROM" +
                "            FILMS AS FILMS" +
                "            LEFT JOIN (" +
                "                SELECT" +
                "                    FILM_ID," +
                "                    COUNT(USER_ID) AS TOTAL" +
                "                FROM" +
                "                    FILM_LIKES" +
                "                GROUP BY" +
                "                    FILM_ID" +
                "                ORDER BY" +
                "                    COUNT(USER_ID)" +
                "            ) AS LIKES ON FILMS.ID = LIKES.FILM_ID" +
                "            LEFT JOIN MPA_FILM_RATINGS AS MFR ON MFR.ID = FILMS.MPA_FILM_RATING_ID" +
                "        ORDER BY" +
                "            IFNULL(LIKES.TOTAL, 0) DESC" +
                "        FETCH FIRST" +
                "            :count ROWS ONLY" +
                "    );";
        Map<String, Object> params = new HashMap<>();
        params.put("count", count);
        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(params), generatedKeyHolder);

        HashMap<Integer, Film> results = new HashMap<>();
        sql = "SELECT * FROM popular_films_tmp";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql);
        while (rs.next()) {
            //get mpa
            int mpa_id = rs.getInt("MPA_FILM_RATING_ID");
            Mpa mpa = mpa_id == 0 ? null : Mpa.builder()
                    .id(mpa_id)
                    .name(rs.getString("MPA_FILM_RATING_NAME"))
                    .build();

            Film film = Film.builder()
                    .id(rs.getInt("ID"))
                    .name(rs.getString("FILM_NAME"))
                    .description(rs.getString("DESCRIPTION"))
                    .releaseDate(Objects.requireNonNull(rs.getDate("RELEASE_DATE")).toLocalDate())
                    .duration(rs.getInt("DURATION"))
                    .mpa(mpa)
                    .build();
            results.put(film.getId(), film);
        }

        //get likes
        sql = "SELECT" +
                "    film_id," +
                "    user_id" +
                " FROM" +
                "    film_likes" +
                " WHERE" +
                "    film_id IN (" +
                "        SELECT" +
                "            id" +
                "        FROM" +
                "            popular_films_tmp" +
                "    )";
        rs = jdbcTemplate.queryForRowSet(sql);
        while (rs.next()) {
            results.get(rs.getInt("film_id"))
                    .getLikes().add(rs.getInt("user_id"));
        }

        //get genres
        sql = "SELECT" +
                "    film_genres.film_id AS film_id," +
                "    + film_genres.genre_id AS genre_id" +
                " FROM" +
                "    film_genres AS film_genres" +
                "    INNER JOIN genres AS genres ON film_genres.GENRE_ID = genres.id" +
                "    AND film_genres.film_id IN (" +
                "        SELECT" +
                "            id" +
                "        FROM" +
                "            popular_films_tmp" +
                "    )";
        rs = jdbcTemplate.queryForRowSet(sql);
        while (rs.next()) {
            int filmId = rs.getInt("FILM_ID");
            int genreId = rs.getInt("GENRE_ID");
            Film film = results.get(filmId);
            film.getGenres().add(genreStorage.getGenreById(genreId));
        }

        sql = "DROP TABLE IF EXISTS popular_films_tmp;";
        jdbcTemplate.update(sql);

        return new ArrayList<>(results.values());
    }

}
