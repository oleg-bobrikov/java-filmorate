package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.mapper.DirectorRowMapper;
import ru.yandex.practicum.filmorate.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.mapper.LikeRowMapper;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import javax.sql.DataSource;
import java.util.*;

@Component
@Slf4j
public class FilmH2Storage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    private final GenreStorage genreStorage;
    private final DirectorStorage directorStorage;

    private final MpaStorage mpaStorage;
    private final FilmRowMapper filmRowMapper;
    private final GenreRowMapper genreRowMapper;
    private final DirectorRowMapper directorRowMapper;
    private final LikeRowMapper likeRowMapper;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final GeneratedKeyHolder generatedKeyHolder;

    public FilmH2Storage(JdbcTemplate jdbcTemplate,
                         GenreStorage genreStorage,
                         MpaStorage mpaStorage,
                         FilmRowMapper filmRowMapper,
                         DirectorStorage directorStorage,
                         GenreRowMapper genreRowMapper,
                         DirectorRowMapper directorRowMapper,
                         LikeRowMapper likeRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreStorage = genreStorage;
        this.directorStorage = directorStorage;
        this.mpaStorage = mpaStorage;
        this.filmRowMapper = filmRowMapper;
        this.genreRowMapper = genreRowMapper;
        this.directorRowMapper = directorRowMapper;
        this.likeRowMapper = likeRowMapper;
        DataSource dataSource = jdbcTemplate.getDataSource();
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(Objects.requireNonNull(dataSource));
        generatedKeyHolder = new GeneratedKeyHolder();
    }

    public List<Film> searchFilmsByTitle(String title) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("IS_FILTERED_BY_DIRECTOR_NAME", false);
        params.put("IS_FILTERED_BY_FILM_NAME", true);
        params.put("FILM_SEARCH_STRING", title);
        params.put("DIRECTOR_SEARCH_STRING", "");

        return searchFilms(params);
    }

    public List<Film> searchFilmsByDirectorName(String directorName) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("IS_FILTERED_BY_DIRECTOR_NAME", true);
        params.put("IS_FILTERED_BY_FILM_NAME", false);
        params.put("FILM_SEARCH_STRING", "");
        params.put("DIRECTOR_SEARCH_STRING", directorName);

        return searchFilms(params);
    }

    public List<Film> searchFilmsByTitleAndDirectorName(String searchString) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("IS_FILTERED_BY_DIRECTOR_NAME", true);
        params.put("IS_FILTERED_BY_FILM_NAME", true);
        params.put("FILM_SEARCH_STRING", searchString);
        params.put("DIRECTOR_SEARCH_STRING", searchString);

        return searchFilms(params);
    }

    public List<Film> getAllFilms() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("IS_FILTERED_BY_DIRECTOR_NAME", false);
        params.put("IS_FILTERED_BY_FILM_NAME", false);
        params.put("FILM_SEARCH_STRING", "");
        params.put("DIRECTOR_SEARCH_STRING", "");

        return searchFilms(params);
    }

    @Override
    public List<Film> searchFilms(Map<String, Object> params) {

        String sql =
                "SELECT" +
                        "    FILMS.ID AS ID," +
                        "    FILMS.FILM_NAME AS FILM_NAME," +
                        "    FILMS.DESCRIPTION AS DESCRIPTION," +
                        "    FILMS.RELEASE_DATE AS RELEASE_DATE," +
                        "    FILMS.DURATION AS DURATION," +
                        "    IFNULL(FILMS.MPA_FILM_RATING_ID, 0)  AS MPA_FILM_RATING_ID " +
                        "FROM" +
                        "    (" +
                        "        SELECT" +
                        "            FILMS.ID AS FILM_ID" +
                        "        FROM" +
                        "            FILMS" +
                        "        WHERE" +
                        "            NOT :IS_FILTERED_BY_FILM_NAME" +
                        "            AND NOT :IS_FILTERED_BY_DIRECTOR_NAME" +
                        "            OR (" +
                        "                :IS_FILTERED_BY_FILM_NAME" +
                        "                AND LOWER(FILMS.FILM_NAME) LIKE LOWER(:FILM_SEARCH_STRING)" +
                        "            )" +
                        "        UNION" +
                        "        SELECT" +
                        "            FILM_ID" +
                        "        FROM" +
                        "            DIRECTORS_FILMS" +
                        "            INNER JOIN DIRECTORS ON DIRECTORS.ID = DIRECTORS_FILMS.DIRECTOR_ID" +
                        "        WHERE" +
                        "            :IS_FILTERED_BY_DIRECTOR_NAME" +
                        "            AND LOWER(DIRECTORS.DIRECTOR_NAME) LIKE LOWER(:DIRECTOR_SEARCH_STRING)" +
                        "    ) AS FILTERED_FILMS" +
                        "    LEFT JOIN FILM_likes ON FILTERED_FILMS.FILM_ID = FILM_likes.FILM_ID" +
                        "    INNER JOIN FILMS ON FILMS.ID = FILTERED_FILMS.FILM_ID " +
                        "GROUP BY" +
                        "    ID," +
                        "    FILM_NAME," +
                        "    DESCRIPTION," +
                        "    RELEASE_DATE," +
                        "    DURATION," +
                        "    MPA_FILM_RATING_ID " +
                        "ORDER BY" +
                        "    COUNT(FILM_likes.FILM_ID) DESC ";


        HashMap<String, Object> sqlParams = new HashMap<>();
        sqlParams.put("IS_FILTERED_BY_DIRECTOR_NAME", params.get("IS_FILTERED_BY_DIRECTOR_NAME"));
        sqlParams.put("IS_FILTERED_BY_FILM_NAME", params.get("IS_FILTERED_BY_FILM_NAME"));
        sqlParams.put("FILM_SEARCH_STRING", "%" + params.get("FILM_SEARCH_STRING") + "%");
        sqlParams.put("DIRECTOR_SEARCH_STRING", "%" + params.get("DIRECTOR_SEARCH_STRING") + "%");
        Object count = params.get("COUNT");
        if (count != null) {
            sqlParams.put("COUNT", count);
            sql = sql + "FETCH FIRST :COUNT ROWS ONLY";
        }

        List<Film> films = namedParameterJdbcTemplate.query(sql, sqlParams, filmRowMapper);
        restoreFilms(films);
        return films;
    }

    @Override
    public Film add(Film film) {
        // update mpa
        Mpa mpa = film.getMpa();
        if (mpa != null) {
            film.setMpa(mpaStorage.findMpaById(mpa.getId()));
        }

        String sql = "insert into films (film_name, description, release_date, duration, mpa_film_rating_id) " +
                "VALUES(:film_name, :description, :release_date, :duration, :mpa_film_rating_id);";

        Map<String, Object> params = filmParams(film);
        params.put("mpa_film_rating_id", mpa == null ? null : mpa.getId());

        int rowsAffected = namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(params), generatedKeyHolder);

        Integer id = Objects.requireNonNull(generatedKeyHolder.getKey()).intValue();

        log.info("rowsAffected = {}, id={}", rowsAffected, id);

        film.setId(id);

        // update genres
        Set<Genre> newGenres = new TreeSet<>(Comparator.comparing(Genre::getId));

        for (Genre genre : film.getGenres()) {
            Integer genreId = genre.getId();
            newGenres.add(genreStorage.findGenreById(genreId));
        }
        genreStorage.updateFilmGenres(film, newGenres);
        film.setGenres(newGenres);
        //update director

        Set<Director> newDirector = new TreeSet<>(Comparator.comparing(Director::getId));

        for (Director director : film.getDirectors()) {
            Integer directorId = director.getId();
            newDirector.add(directorStorage.findDirectorById(directorId).get());
        }
        directorStorage.updateFilmDirector(film, newDirector);
        film.setDirectors(newDirector);

        return film;
    }

    private Map<String, Object> filmParams(Film film) {
        Map<String, Object> params = new HashMap<>();
        params.put("film_name", film.getName());
        params.put("description", film.getDescription());
        params.put("release_date", film.getReleaseDate());
        params.put("duration", film.getDuration());
        return params;
    }

    @Override
    public Film update(Film film) {
        // update mpa
        Mpa mpa = film.getMpa();
        if (mpa != null) {
            film.setMpa(mpaStorage.findMpaById(mpa.getId()));
        }

        String sql =
                "update films set " +
                        " film_name = :film_name, " +
                        " description = :description, " +
                        " release_date = :release_date, " +
                        " duration = :duration, " +
                        " mpa_film_rating_id = :mpa_film_rating_id " +
                        "where id = :id";

        Map<String, Object> params = filmParams(film);
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
            newGenres.add(genreStorage.findGenreById(genre.getId()));
        }
        genreStorage.updateFilmGenres(film, newGenres);
        film.setGenres(newGenres);

        // update director
        Set<Director> newDirector = new TreeSet<>(Comparator.comparing(Director::getId));
        for (Director director : film.getDirectors()) {
            newDirector.add(directorStorage.findDirectorById(director.getId()).get());
        }
        directorStorage.updateFilmDirector(film, newDirector);
        film.setDirectors(newDirector);

        return film;
    }

    @Override
    public void removeFilmById(Integer id) {
        String sql = "delete from films where id = :film_id";

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
                " MFR.mpa_film_rating_name AS MPA_FILM_RATING_NAME" +
                " FROM" +
                " FILMS " +
                " LEFT JOIN MPA_FILM_RATINGS AS MFR ON MFR.ID = FILMS.MPA_FILM_RATING_ID";
        List<Film> films = namedParameterJdbcTemplate.query(sql, filmRowMapper);
        restoreFilms(films);
        return films;
    }

    @Override
    public Optional<Film> findFilmById(int id) {
        String sql = "select * from films where id = ?";
        List<Film> films = jdbcTemplate.query(sql, new FilmRowMapper(mpaStorage), id);
        if (films.isEmpty()) {
            log.info("Фильм с идентификатором {} не найден.", id);
            return Optional.empty();
        } else {
            restoreFilms(films);
            return Optional.of(films.get(0));
        }
    }


    @Override
    public List<Film> getCommonFilms(Integer userId, Integer friendId) {
        String sqlQuery = "with sorted_films as " +
                "( SELECT FILM_ID " +
                " FROM FILM_LIKES AS film_likes" +
                " WHERE film_likes.FILM_ID IN" +
                " (SELECT film_likes.FILM_ID AS FILM_ID" +
                " FROM FILM_LIKES AS film_likes" +
                " WHERE FILM_LIKES.USER_ID IN(:user_id, :friend_id)" +
                "  GROUP BY FILM_ID" +
                "     HAVING COUNT(FILM_LIKES.USER_ID) = 2)" +
                " GROUP BY FILM_ID" +
                " ORDER BY count(USER_ID) DESC" +
                ")" +
                " SELECT * FROM Films " +
                " INNER JOIN sorted_films" +
                " ON FILMS.ID = sorted_films.FILM_ID";

        Map<String, Object> params = new HashMap<>();
        params.put("user_id", 1);
        params.put("friend_id", 2);

        List<Film> films = namedParameterJdbcTemplate.query(sqlQuery, params, filmRowMapper);
        restoreFilms(films);

        return films;
    }

    @Override
    public void addLike(Film film, User user) {
        String sql = "merge into film_likes key (film_id, user_id) VALUES (:film_id, :user_id)";

        Map<String, Object> params = new HashMap<>();
        params.put("film_id", film.getId());
        params.put("user_id", user.getId());

        namedParameterJdbcTemplate.update(sql, params);
        log.info("Фильм с идентификатором {} получил лайк от пользователя с идентификатором {}", film.getId(), user.getId());
        Optional<Film> filmOptional = findFilmById(film.getId());
        filmOptional.ifPresent(value -> film.setLikes(value.getLikes()));
    }

    @Override
    public void removeLike(Film film, User user) {
        String sql = "delete from film_likes where film_id = :FILM_ID and user_id = :USER_ID";

        Map<String, Object> params = new HashMap<>();
        params.put("FILM_ID", film.getId());
        params.put("USER_ID", user.getId());

        namedParameterJdbcTemplate.update(sql, params);

        log.info("У фильма с идентификатором {} удален лайк от пользователя с идентификатором {}", film.getId(), user.getId());
        Optional<Film> filmOptional = findFilmById(film.getId());
        filmOptional.ifPresent(value -> film.setLikes(value.getLikes()));

        // create a history log separately,
        // cause log could not being created by "on delete trigger"
        // (observed ON DELETE CASCADE PROBLEM. It's deleting several rows instead of one which is required by Postman)
        sql = "insert into EVENTS (EVENT_TIMESTAMP, EVENT_TYPE, ENTITY_ID, USER_ID, OPERATION) " +
                "VALUES(CURRENT_TIMESTAMP, :EVENT_TYPE, :ENTITY_ID, :USER_ID, :OPERATION)";
        params.put("EVENT_TYPE", "LIKE");
        params.put("ENTITY_ID", film.getId());
        params.put("OPERATION", "REMOVE");
        namedParameterJdbcTemplate.update(sql, params);
    }

    @Override
    public List<Film> getTopFilms(int count) {

        HashMap<String, Object> params = new HashMap<>();
        params.put("IS_FILTERED_BY_DIRECTOR_NAME", false);
        params.put("IS_FILTERED_BY_FILM_NAME", false);
        params.put("FILM_SEARCH_STRING", "");
        params.put("DIRECTOR_SEARCH_STRING", "");
        params.put("COUNT", count);
        return searchFilms(params);
    }

    @Override
    public List<Film> searchFilmsByDirectorOrderedByYear(Integer directorId) {
        String sql = "SELECT" +
                "    FILMS.ID," +
                "    FILMS.FILM_NAME," +
                "    FILMS.DESCRIPTION," +
                "    FILMS.RELEASE_DATE," +
                "    FILMS.DURATION," +
                "    IFNULL(FILMS.MPA_FILM_RATING_ID, 0) AS MPA_FILM_RATING_ID " +
                "FROM FILMS " +
                "INNER JOIN directors_films ON directors_films.film_id = FILMS.ID AND directors_films.director_id = :DIRECTOR_ID " +
                "ORDER BY YEAR(films.release_date)";

        HashMap<String, Object> params = new HashMap<>();
        params.put("DIRECTOR_ID", directorId);

        List<Film> films = namedParameterJdbcTemplate.query(sql, params, filmRowMapper);
        restoreFilms(films);
        return films;
    }

    public List<Film> searchFilmsByDirectorOrderedByLikes(Integer directorId) {
        String sql = "SELECT" +
                "    FILMS.ID," +
                "    FILMS.FILM_NAME," +
                "    FILMS.DESCRIPTION," +
                "    FILMS.RELEASE_DATE," +
                "    FILMS.DURATION," +
                "    IFNULL(FILMS.MPA_FILM_RATING_ID, 0) AS MPA_FILM_RATING_ID " +
                "FROM FILMS " +
                "INNER JOIN directors_films ON directors_films.film_id = FILMS.ID AND directors_films.director_id = :DIRECTOR_ID " +
                "LEFT JOIN FILM_LIKES ON FILMS.ID = FILM_LIKES.FILM_ID " +
                "GROUP BY " +
                "FILMS.ID, " +
                "FILMS.FILM_NAME, " +
                "FILMS.DESCRIPTION, " +
                "FILMS.RELEASE_DATE," +
                "FILMS.DURATION, " +
                "IFNULL(FILMS.MPA_FILM_RATING_ID, 0) " +
                "ORDER BY COUNT(FILM_LIKES.FILM_ID) DESC ";

        HashMap<String, Object> params = new HashMap<>();
        params.put("DIRECTOR_ID", directorId);

        List<Film> films = namedParameterJdbcTemplate.query(sql, params, filmRowMapper);
        restoreFilms(films);
        return films;
    }

    @Override
    public List<Film> getRecommendations(Integer userId) {
        String sql = "DROP TABLE IF EXISTS SIMILAR_USERS_BY_PRIORITY";
        jdbcTemplate.execute(sql);
        sql = " CREATE TEMPORARY TABLE IF NOT EXISTS SIMILAR_USERS_BY_PRIORITY (" +
                "    USER_ID INT NOT NULL," +
                "    PRIORITY INT" +
                ")";
        jdbcTemplate.execute(sql);

        sql = " INSERT INTO SIMILAR_USERS_BY_PRIORITY (" +
                " SELECT" +
                "            FILM_LIKES.USER_ID," +
                "            COUNT(FILM_LIKES.FILM_ID) AS PRIORITY" +
                "        FROM" +
                "            FILM_LIKES" +
                "        WHERE" +
                "            USER_ID <> :USER_ID" +
                "            AND FILM_ID IN (" +
                "                SELECT" +
                "                    FILM_ID" +
                "                FROM" +
                "                    (" +
                "                        SELECT" +
                "                            FILM_ID" +
                "                        FROM" +
                "                            FILM_LIKES" +
                "                        WHERE" +
                "                            USER_ID = :USER_ID" +
                "                    ) AS USER_FILMS" +
                "            )" +
                "        GROUP BY" +
                "            USER_ID" +
                "    )";
        HashMap<String, Object> params = new HashMap<>();
        params.put("USER_ID", userId);
        namedParameterJdbcTemplate.update(sql, params);

        sql = "SELECT" +
                "    DISTINCT FILMS.ID," +
                "    FILMS.FILM_NAME," +
                "    FILMS.DESCRIPTION," +
                "    FILMS.RELEASE_DATE," +
                "    FILMS.DURATION," +
                "    IFNULL(FILMS.MPA_FILM_RATING_ID, 0) AS MPA_FILM_RATING_ID" +
                " FROM" +
                "    SIMILAR_USERS_BY_PRIORITY AS SIMILAR_USERS_BY_PRIORITY" +
                "    INNER JOIN FILM_LIKES ON SIMILAR_USERS_BY_PRIORITY.USER_ID = FILM_LIKES.USER_ID" +
                "    INNER JOIN FILMS ON FILMS.ID = FILM_LIKES.FILM_ID" +
                "    INNER JOIN (" +
                "        SELECT" +
                "            MAX(PRIORITY) AS PRIORITY" +
                "        FROM" +
                "         SIMILAR_USERS_BY_PRIORITY AS SIMILAR_USERS_BY_PRIORITY" +
                "    ) AS MAX_PRIORITY ON SIMILAR_USERS_BY_PRIORITY.PRIORITY = MAX_PRIORITY.PRIORITY" +
                " WHERE" +
                "    NOT FILM_LIKES.FILM_ID IN (" +
                "        SELECT" +
                "            FILM_ID" +
                "        FROM" +
                "            (" +
                "                SELECT" +
                "                    FILM_ID" +
                "                FROM" +
                "                    FILM_LIKES" +
                "                WHERE" +
                "                    USER_ID = :USER_ID" +
                "            )" +
                "    )";

        List<Film> films = namedParameterJdbcTemplate.query(sql, params, filmRowMapper);
        restoreFilms(films);
        sql = "DROP TABLE IF EXISTS SIMILAR_USERS_BY_PRIORITY";
        jdbcTemplate.execute(sql);
        return films;
    }

    @Override
    public List<Film> getTopFilmsFilteredByYear(Integer count, Integer year) {
        String sql = "WITH FILTERED_FILMS AS (" +
                "SELECT DISTINCT FILMS.ID," +
                "  FILMS.FILM_NAME," +
                "   FILMS.DESCRIPTION," +
                "   FILMS.RELEASE_DATE," +
                "   FILMS.DURATION," +
                "   FILMS.MPA_FILM_RATING_ID, " +
                "   FILM_GENRES.GENRE_ID " +
                "FROM FILMS  " +
                "INNER JOIN FILM_GENRES ON FILMS.ID = FILM_GENRES.FILM_ID " +
                "WHERE EXTRACT(YEAR FROM FILMS.RELEASE_DATE) = :YEAR) " +
                "SELECT FILTERED_FILMS.ID, " +
                "       DESCRIPTION, " +
                "       FILM_NAME, " +
                "       RELEASE_DATE, " +
                "       DURATION, " +
                "       MPA_FILM_RATING_ID " +
                "FROM FILTERED_FILMS  " +
                "       left join FILM_LIKES on FILM_LIKES.FILM_ID = FILTERED_FILMS.ID " +
                "       left join MPA_FILM_RATINGS R on R.ID = FILTERED_FILMS.MPA_FILM_RATING_ID " +
                "GROUP BY FILTERED_FILMS.ID, " +
                "       FILTERED_FILMS.DESCRIPTION, " +
                "       FILTERED_FILMS.FILM_NAME, " +
                "       FILTERED_FILMS.RELEASE_DATE, " +
                "       FILTERED_FILMS.DURATION, " +
                "       FILTERED_FILMS.MPA_FILM_RATING_ID " +
                "ORDER BY COUNT(FILM_LIKES.USER_ID) desc " +
                "LIMIT :COUNT";

        HashMap<String, Object> params = new HashMap<>();
        params.put("YEAR", year);
        params.put("COUNT", count);

        List<Film> films = namedParameterJdbcTemplate.query(sql, params, filmRowMapper);
        restoreFilms(films);
        return films;
    }

    @Override
    public List<Film> getTopFilmsFilteredByGenreAndYear(Integer count, Integer genreId, Integer year) {
        String sql = "WITH FILTERED_FILMS AS (" +
                "SELECT DISTINCT FILMS.ID," +
                "  FILMS.FILM_NAME," +
                "   FILMS.DESCRIPTION," +
                "   FILMS.RELEASE_DATE," +
                "   FILMS.DURATION," +
                "   FILMS.MPA_FILM_RATING_ID, " +
                "   FILM_GENRES.GENRE_ID " +
                "FROM FILMS  " +
                "INNER JOIN FILM_GENRES ON FILMS.ID = FILM_GENRES.FILM_ID " +
                "WHERE FILM_GENRES.GENRE_ID = :GENRE_ID AND EXTRACT(YEAR FROM FILMS.RELEASE_DATE) = :YEAR) " +
                "SELECT FILTERED_FILMS.ID, " +
                "       DESCRIPTION, " +
                "       FILM_NAME, " +
                "       RELEASE_DATE, " +
                "       DURATION, " +
                "       MPA_FILM_RATING_ID " +
                "FROM FILTERED_FILMS  " +
                "       left join FILM_LIKES on FILM_LIKES.FILM_ID = FILTERED_FILMS.ID " +
                "       left join MPA_FILM_RATINGS R on R.ID = FILTERED_FILMS.MPA_FILM_RATING_ID " +
                "GROUP BY FILTERED_FILMS.ID, " +
                "       FILTERED_FILMS.DESCRIPTION, " +
                "       FILTERED_FILMS.FILM_NAME, " +
                "       FILTERED_FILMS.RELEASE_DATE, " +
                "       FILTERED_FILMS.DURATION, " +
                "       FILTERED_FILMS.MPA_FILM_RATING_ID " +
                "ORDER BY COUNT(FILM_LIKES.USER_ID) desc " +
                "LIMIT :COUNT";

        HashMap<String, Object> params = new HashMap<>();
        params.put("GENRE_ID", genreId);
        params.put("YEAR", year);
        params.put("COUNT", count);

        List<Film> films = namedParameterJdbcTemplate.query(sql, params, filmRowMapper);
        restoreFilms(films);
        return films;
    }

    @Override
    public List<Film> getTopFilmsFilteredByGenre(Integer count, Integer genreId) {
        String sql = "WITH FILTERED_FILMS AS (" +
                "SELECT DISTINCT FILMS.ID," +
                "  FILMS.FILM_NAME," +
                "   FILMS.DESCRIPTION," +
                "   FILMS.RELEASE_DATE," +
                "   FILMS.DURATION," +
                "   FILMS.MPA_FILM_RATING_ID, " +
                "   FILM_GENRES.GENRE_ID " +
                "FROM FILMS  " +
                "INNER JOIN FILM_GENRES ON FILMS.ID = FILM_GENRES.FILM_ID " +
                "WHERE FILM_GENRES.GENRE_ID = :GENRE_ID) " +
                "SELECT FILTERED_FILMS.ID, " +
                "       DESCRIPTION, " +
                "       FILM_NAME, " +
                "       RELEASE_DATE, " +
                "       DURATION, " +
                "       MPA_FILM_RATING_ID " +
                "FROM FILTERED_FILMS  " +
                "       left join FILM_LIKES on FILM_LIKES.FILM_ID = FILTERED_FILMS.ID " +
                "       left join MPA_FILM_RATINGS R on R.ID = FILTERED_FILMS.MPA_FILM_RATING_ID " +
                "GROUP BY FILTERED_FILMS.ID, " +
                "       FILTERED_FILMS.DESCRIPTION, " +
                "       FILTERED_FILMS.FILM_NAME, " +
                "       FILTERED_FILMS.RELEASE_DATE, " +
                "       FILTERED_FILMS.DURATION, " +
                "       FILTERED_FILMS.MPA_FILM_RATING_ID " +
                "ORDER BY COUNT(FILM_LIKES.USER_ID) desc " +
                "LIMIT :COUNT";

        HashMap<String, Object> params = new HashMap<>();
        params.put("GENRE_ID", genreId);
        params.put("COUNT", count);

        List<Film> films = namedParameterJdbcTemplate.query(sql, params, filmRowMapper);
        restoreFilms(films);
        return films;
    }

    private void restoreFilms(List<Film> films) {
        HashMap<String, Object> params = new HashMap<>();
        for (Film film : films) {
            params.put("FILM_ID", film.getId());

            // get genres
            String sql = "SELECT " +
                    " GENRES.ID, " +
                    " GENRES.GENRE_NAME" +
                    " FROM FILM_GENRES" +
                    " INNER JOIN GENRES ON GENRES.ID = FILM_GENRES.GENRE_ID  AND FILM_GENRES.FILM_ID = :FILM_ID";
            List<Genre> genres = namedParameterJdbcTemplate.query(sql, params, genreRowMapper);
            film.setGenres(new HashSet<>(genres));

            // get directors
            sql = "select " +
                    " DIRECTORS.ID, " +
                    " DIRECTOR_NAME " +
                    " FROM " +
                    "   directors_films " +
                    " INNER JOIN DIRECTORS ON DIRECTORS_FILMS.DIRECTOR_ID = DIRECTORS.ID" +
                    " WHERE FILM_ID = :FILM_ID";
            List<Director> directors = namedParameterJdbcTemplate.query(sql, params, directorRowMapper);
            film.setDirectors(new HashSet<>(directors));

            // get likes
            sql = "select USER_ID from FILM_LIKES where FILM_ID = :FILM_ID";
            List<Integer> likes = namedParameterJdbcTemplate.query(sql, params, likeRowMapper);
            film.setLikes(new HashSet<>(likes));
        }
    }
}
