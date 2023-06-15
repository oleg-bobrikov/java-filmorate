package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.mapper.DirectorRowMapper;
import ru.yandex.practicum.filmorate.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.mapper.LikeRowMapper;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    @Override
    public List<Film> searchFilms(Map<String, String> params) {

        boolean isFilteredByDirector = params.containsKey("director");
        String directorSearchString = isFilteredByDirector ? params.get("director") : "";

        boolean isFilteredByTitle = params.containsKey("title");
        String titleSearchString = isFilteredByTitle ? params.get("title") : "";

        String sql =
                "SELECT " +
                        "  FILMS.ID,  " +
                        "  FILMS.FILM_NAME,  " +
                        "  FILMS.DESCRIPTION,  " +
                        "  FILMS.RELEASE_DATE,  " +
                        "  FILMS.DURATION,  " +
                        "  FILMS.MPA_FILM_RATING_ID  " +
                        "FROM  " +
                        "  ( " +
                        "    SELECT  " +
                        "      FILTERED_FILMS.FILM_ID  " +
                        "    FROM  " +
                        "      ( " +
                        "        SELECT  " +
                        "          FILMS.ID AS FILM_ID  " +
                        "        FROM  " +
                        "          FILMS  " +
                        "        WHERE  " +
                        " NOT :IS_FILTERED_BY_FILM_NAME AND NOT :IS_FILTERED_BY_DIRECTOR_NAME" +
                        "          OR ( " +
                        "            :IS_FILTERED_BY_FILM_NAME  " +
                        "            AND LOWER(FILMS.FILM_NAME) LIKE LOWER(:FILM_SEARCH) " +
                        "          )  " +
                        "        UNION " +
                        "        SELECT " +
                        "          FILM_ID " +
                        "        FROM " +
                        "          DIRECTORS_FILMS " +
                        "          INNER JOIN DIRECTORS ON DIRECTORS.ID = DIRECTORS_FILMS.DIRECTOR_ID " +
                        "        WHERE " +
                        " :IS_FILTERED_BY_DIRECTOR_NAME " +
                        "          AND  " +
                        "            LOWER(DIRECTORS.DIRECTOR_NAME) LIKE LOWER(:DIRECTOR_SEARCH) " +
                        "      ) as FILTERED_FILMS " +
                        "      LEFT JOIN FILM_likes ON FILTERED_FILMS.FILM_ID = FILM_likes.FILM_ID " +
                        "    GROUP BY " +
                        "      FILTERED_FILMS.FILM_ID " +
                        "    ORDER BY " +
                        "      COUNT(FILM_likes.FILM_ID) DESC " +
                        "  ) as SORTED_FILMS " +
                        "  LEFT JOIN FILMS ON FILMS.ID = SORTED_FILMS.FILM_ID ";


        HashMap<String, Object> sqlParams = new HashMap<>();
        sqlParams.put("IS_FILTERED_BY_DIRECTOR_NAME", isFilteredByDirector);
        sqlParams.put("IS_FILTERED_BY_FILM_NAME", isFilteredByTitle);
        sqlParams.put("FILM_SEARCH", "%" + titleSearchString + "%");
        sqlParams.put("DIRECTOR_SEARCH", "%" + directorSearchString + "%");

        List<Film> films = namedParameterJdbcTemplate.query(sql, sqlParams, filmRowMapper);
        restoreFilms(films);
        return films;
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
            newGenres.add(genreStorage.getGenreById(genreId));
        }
        genreStorage.updateFilmGenres(film, newGenres);
        film.setGenres(newGenres);
        //update director

        Set<Director> newDirector = new TreeSet<>(Comparator.comparing(Director::getId));

        for (Director director : film.getDirectors()) {
            Integer directorId = director.getId();
            newDirector.add(directorStorage.getDirectorById(directorId).get());
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
            newGenres.add(genreStorage.getGenreById(genre.getId()));
        }
        genreStorage.updateFilmGenres(film, newGenres);
        film.setGenres(newGenres);

        // update director
        Set<Director> newDirector = new TreeSet<>(Comparator.comparing(Director::getId));
        for (Director director : film.getDirectors()) {
            newDirector.add(directorStorage.getDirectorById(director.getId()).get());
        }
        directorStorage.updateFilmDirector(film, newDirector);
        film.setDirectors(newDirector);

        return film;
    }

    @Override
    public void deleteFilmById(Integer id) {
        String sql = "delete from film_genres where film_id = :film_id; " +
                "delete from film_likes where film_id = :film_id; " +
                "delete from films where id = :film_id; " +
                "delete from directors_films where film_id =:film_id";

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
    public Optional<Film> getFilmById(int id) {
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
        String sql = "merge into film_likes key (film_id, user_id) VALUES(:film_id, :user_id)";

        Map<String, Object> params = new HashMap<>();
        params.put("film_id", film.getId());
        params.put("user_id", user.getId());

        int updatedRows = namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(params), generatedKeyHolder);
        log.info("Updated rows: {}", updatedRows);
        log.info("Фильм с идентификатором {} получил лайк от пользователя с идентификатором {}", film.getId(), user.getId());
        Optional<Film> filmOptional = getFilmById(film.getId());
        filmOptional.ifPresent(value -> film.setLikes(value.getLikes()));
    }

    @Override
    public void removeLike(Film film, User user) {
        String sql = "delete from film_likes where film_id = :film_id and user_id = :user_id";

        Map<String, Object> params = new HashMap<>();
        params.put("film_id", film.getId());
        params.put("user_id", user.getId());

        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(params), generatedKeyHolder);

        log.info("У фильма с идентификатором {} удален лайк от пользователя с идентификатором {}", film.getId(), user.getId());
        Optional<Film> filmOptional = getFilmById(film.getId());
        filmOptional.ifPresent(value -> film.setLikes(value.getLikes()));
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
                "            FILMS.film_name AS film_name," +
                "            FILMS.DESCRIPTION," +
                "            FILMS.RELEASE_DATE," +
                "            FILMS.DURATION," +
                "            IFNULL(MFR.ID, 0) AS MPA_FILM_RATING_ID," +
                "            MFR.MPA_FILM_RATING_NAME AS MPA_FILM_RATING_NAME" +
                "        FROM" +
                "            FILMS" +
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

        sql = "SELECT * FROM popular_films_tmp";
        HashMap<Integer, Film> films = new HashMap<>();
        jdbcTemplate.query(sql, filmRowMapper).forEach(film -> films.put(film.getId(), film));

        //get likes
        sql = "SELECT film_id, user_id FROM film_likes " +
                "WHERE " +
                "film_id IN " +
                "(SELECT id FROM popular_films_tmp)";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql);
        while (rs.next()) {
            films.get(rs.getInt("film_id"))
                    .getLikes().add(rs.getInt("user_id"));
        }

        //get genres
        sql = "SELECT" +
                "    film_genres.film_id AS film_id," +
                "    + film_genres.genre_id AS genre_id" +
                " FROM" +
                "    film_genres" +
                "    INNER JOIN genres ON film_genres.GENRE_ID = genres.id" +
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
            Film film = films.get(filmId);
            film.getGenres().add(genreStorage.getGenreById(genreId));
        }

        // director
        sql = "SELECT" +
                "    directors_films.film_id ," +
                "    + directors_films.director_id " +
                " FROM" +
                "    directors_films " +
                "    INNER JOIN directors  ON directors_films.director_id = directors.id" +
                "    AND directors_films.film_id IN (" +
                "        SELECT" +
                "            id" +
                "        FROM" +
                "            popular_films_tmp" +
                "    )";
        rs = jdbcTemplate.queryForRowSet(sql);
        while (rs.next()) {
            int filmId = rs.getInt("FILM_ID");
            int directorId = rs.getInt("DIRECTOR_ID");
            Film film = films.get(filmId);
            film.getDirectors().add(directorStorage.getDirectorById(directorId).get());
        }

        sql = "DROP TABLE IF EXISTS popular_films_tmp;";
        jdbcTemplate.update(sql);


        return new ArrayList<>(films.values());
    }

    @Override
    public List<Film> findFilmByDirector(Integer directorId, String sortBy) {
        List<Film> films = new ArrayList<>();
        String sqlQueryByLikes = "SELECT df.film_id, COUNT(fl.user_id) AS p " +
                "FROM directors_films AS df " +
                "LEFT OUTER JOIN film_likes AS fl ON df.film_id = fl.film_id " +
                "WHERE director_id = ? " +
                "GROUP BY df.film_id " +
                "ORDER BY  p DESC ";


        String sqlQueryByYear = "SELECT df.film_id " +
                "FROM directors_films AS df " +
                "LEFT OUTER JOIN films ON df.film_id = films.id " +
                "WHERE director_id = ? " +
                "ORDER BY YEAR(films.release_date)";


        List<Integer> filmsId;
        if (sortBy.equals("year")) {
            filmsId = jdbcTemplate.query(sqlQueryByYear, (rs, rowNum) -> rs.getInt("film_id"), directorId);

            for (Integer id : filmsId) {
                films.add(getFilmById(id).get());
            }
        } else {
            filmsId = jdbcTemplate.query(sqlQueryByLikes, (rs, rowNum) -> rs.getInt("film_id"), directorId);
            for (Integer id : filmsId) {
                films.add(getFilmById(id).get());
            }
        }
        return films;
    }

    @Override
    public List<Film> getRecommendations(Integer userId) {
        String sql = "SELECT" +
                "    DISTINCT FILMS.ID," +
                "    FILMS.FILM_NAME," +
                "    FILMS.DESCRIPTION," +
                "    FILMS.RELEASE_DATE," +
                "    FILMS.DURATION," +
                "    IFNULL(FILMS.MPA_FILM_RATING_ID, 0) AS MPA_FILM_RATING_ID," +
                "    MPA_FILM_RATINGS.MPA_FILM_RATING_NAME" +
                " FROM" +
                "    (" +
                "        SELECT" +
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
                "    ) AS SIMILAR_USERS_BY_PRIORITY" +
                "    INNER JOIN FILM_LIKES ON SIMILAR_USERS_BY_PRIORITY.USER_ID = FILM_LIKES.USER_ID" +
                "    INNER JOIN FILMS ON FILMS.ID = FILM_LIKES.FILM_ID" +
                "    LEFT JOIN MPA_FILM_RATINGS ON MPA_FILM_RATINGS.ID = FILMS.MPA_FILM_RATING_ID" +
                "    INNER JOIN (" +
                "        SELECT" +
                "            MAX(PRIORITY) AS PRIORITY" +
                "        FROM" +
                "            (" +
                "                SELECT" +
                "                    COUNT(FILM_LIKES.FILM_ID) AS PRIORITY" +
                "                FROM" +
                "                    FILM_LIKES" +
                "                WHERE" +
                "                    USER_ID <> :USER_ID" +
                "                    AND FILM_ID IN (" +
                "                        SELECT" +
                "                            FILM_ID" +
                "                        FROM" +
                "                            (" +
                "                                SELECT" +
                "                                    FILM_ID" +
                "                                FROM" +
                "                                    FILM_LIKES" +
                "                                WHERE" +
                "                                    USER_ID = :USER_ID" +
                "                            ) AS USER_FILMS" +
                "                    )" +
                "                GROUP BY" +
                "                    USER_ID" +
                "            ) AS SIMILAR_USERS_BY_PRIORITY" +
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
        HashMap<String, Object> params = new HashMap<>();
        params.put("USER_ID", userId);
        List<Film> films = namedParameterJdbcTemplate.query(sql, params, filmRowMapper);
        restoreFilms(films);

        return films;
    }

    @Override
    public List<Film> getPopularFilmsSortedByYear(Integer count, Integer year) {
        String sqlQueryPopularFilms = "WITH SORTERED_FILMS AS(" +
                "SELECT DISTINCT f.ID," +
                "  f.FILM_NAME," +
                "   f.DESCRIPTION," +
                "   f.RELEASE_DATE," +
                "   f.DURATION,\n" +
                "   f.MPA_FILM_RATING_ID " +
                "FROM films AS f " +
                "WHERE EXTRACT(YEAR FROM f.RELEASE_DATE) = ? " +
                ")" +
                "SELECT SF.ID," +
                "       SF.FILM_NAME, " +
                "       SF.DESCRIPTION," +
                "       SF.RELEASE_DATE, " +
                "       SF.DURATION," +
                "       SF.MPA_FILM_RATING_ID," +
                "       FILM_GENRES.FILM_ID " +
                "FROM SORTERED_FILMS AS SF " +
                "LEFT OUTER JOIN FILM_LIKES FL ON FL.FILM_ID = SF.ID " +
                "LEFT OUTER JOIN MPA_FILM_RATINGS R ON R.ID = SF.MPA_FILM_RATING_ID " +
                "LEFT OUTER JOIN FILM_GENRES ON SF.ID = FILM_GENRES.FILM_ID " +
                "GROUP BY SF.ID," +
                "         SF.FILM_NAME," +
                "         SF.DESCRIPTION," +
                "         SF.RELEASE_DATE, " +
                "         SF.DURATION," +
                "         SF.MPA_FILM_RATING_ID " +
                "ORDER BY COUNT(FL.USER_ID) DESC " +
                "LIMIT ? ";
        List<Film> films = jdbcTemplate.query(sqlQueryPopularFilms, this::mapRowFilm, year, count);
        restoreFilms(films);
        return films;
    }

    @Override
    public List<Film> getPopularFilms(Integer count, Integer genreId, Integer year) {
        String sqlQueryGetPopularFilms = "WITH SORTERED_FILMS AS (" +
                "SELECT DISTINCT f.ID," +
                "  f.FILM_NAME," +
                "   f.DESCRIPTION," +
                "   f.RELEASE_DATE," +
                "   f.DURATION," +
                "   f.MPA_FILM_RATING_ID " +
                "FROM films AS f " +
                "LEFT OUTER JOIN FILM_GENRES on F.ID = FILM_GENRES.FILM_ID " +
                "where GENRE_ID = ? " +
                "       and EXTRACT(YEAR FROM f.RELEASE_DATE) = ? " +
                ")" +
                "select SF.ID, " +
                "       SF.DESCRIPTION, " +
                "       SF.FILM_NAME, " +
                "       SF.RELEASE_DATE, " +
                "       SF.DURATION, " +
                "       SF.MPA_FILM_RATING_ID " +
                "from SORTERED_FILMS SF " +
                "       LEFT OUTER JOIN FILM_LIKES FL on FL.FILM_ID = SF.ID " +
                "       LEFT OUTER JOIN MPA_FILM_RATINGS R on R.ID = SF.MPA_FILM_RATING_ID " +
                "GROUP BY SF.ID, " +
                "       SF.DESCRIPTION, " +
                "       SF.FILM_NAME, " +
                "       SF.RELEASE_DATE, " +
                "       SF.DURATION, " +
                "       SF.MPA_FILM_RATING_ID " +
                "ORDER BY count(FL.USER_ID) DESC " +
                "LIMIT ?";

        List<Film> films = jdbcTemplate.query(sqlQueryGetPopularFilms, this::mapRowFilm, genreId, year, count);
        restoreFilms(films);
        return films;
    }

    @Override
    public List<Film> getPopularFilmsSortedByGenre(Integer count, Integer genreId) {
        String sqlQueryGetPopularFilms = "WITH SORTERED_FILMS AS (" +
                "SELECT DISTINCT f.ID," +
                "  f.FILM_NAME," +
                "   f.DESCRIPTION," +
                "   f.RELEASE_DATE," +
                "   f.DURATION," +
                "   f.MPA_FILM_RATING_ID, " +
                "   FG.GENRE_ID " +
                "FROM films AS f " +
                " LEFT OUTER JOIN FILM_GENRES FG ON F.ID = FG.FILM_ID " +
                " INNER JOIN GENRES ON GENRES.ID = FG.GENRE_ID " +
                "WHERE FG.GENRE_ID = ? " +
                ")" +
                "select SF.ID, " +
                "       DESCRIPTION, " +
                "       FILM_NAME, " +
                "       RELEASE_DATE, " +
                "       DURATION, " +
                "       MPA_FILM_RATING_ID " +
                "from SORTERED_FILMS SF " +
                "       left join FILM_LIKES FL on FL.FILM_ID = SF.ID " +
                "       left join MPA_FILM_RATINGS R on R.ID = SF.MPA_FILM_RATING_ID " +
                "group by SF.ID, " +
                "       SF.DESCRIPTION, " +
                "       SF.FILM_NAME, " +
                "       SF.RELEASE_DATE, " +
                "       SF.DURATION, " +
                "       SF.MPA_FILM_RATING_ID " +
                "order by count(FL.USER_ID) desc " +
                "limit ?";
        List<Film> films = jdbcTemplate.query(sqlQueryGetPopularFilms, this::mapRowFilm, genreId, count);
        restoreFilms(films);
        return films;
    }


    public Film mapRowFilm(ResultSet rs, int rowNum) throws SQLException {
        int mpaId = rs.getInt("MPA_FILM_RATING_ID");
        Mpa mpa = mpaId == 0 ? null : mpaStorage.getMpaById(mpaId);

        return Film.builder()
                .id(rs.getInt("ID"))
                .name(rs.getString("FILM_NAME"))
                .description(rs.getString("DESCRIPTION"))
                .releaseDate(Objects.requireNonNull(rs.getDate("RELEASE_DATE")).toLocalDate())
                .duration(rs.getInt("DURATION"))
                .mpa(mpa)
                .build();
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
