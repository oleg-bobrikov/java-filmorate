package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.nio.file.Paths.get;

@Repository
@Slf4j
public class FilmH2Storage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    private final GenreStorage genreStorage;
    private final DirectorStorage directorStorage;

    private final MpaStorage mpaStorage;
    private final FilmRowMapper filmRowMapper;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final GeneratedKeyHolder generatedKeyHolder;

    public FilmH2Storage(JdbcTemplate jdbcTemplate, GenreStorage genreStorage,
                         MpaStorage mpaStorage, FilmRowMapper filmRowMapper, DirectorStorage directorStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreStorage = genreStorage;
        this.directorStorage = directorStorage;
        this.mpaStorage = mpaStorage;
        this.filmRowMapper = filmRowMapper;
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
    public void deleteFilmById(int id) {
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
                " FILMS AS FILMS " +
                " LEFT JOIN MPA_FILM_RATINGS AS MFR ON MFR.ID = FILMS.MPA_FILM_RATING_ID;";

        HashMap<Integer, Film> results = new HashMap<>();
        jdbcTemplate.query(sql, filmRowMapper).forEach(film -> results.put(film.getId(), film));

        //likes
        sql = "select film_id, user_id from film_likes";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql);
        while (rs.next()) {
            results.get(rs.getInt("film_id"))
                    .getLikes()
                    .add(rs.getInt("user_id"));
        }

        //genres

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
        // director
        sql = "SELECT " +
                " directors_films.film_id , " +
                " directors_films.director_id, " +
                " dir.director_name " +
                "FROM " +
                " directors_films  " +
                "INNER JOIN " +
                " directors AS dir " +
                "ON directors_films.director_id = dir.id";
        rs = jdbcTemplate.queryForRowSet(sql);
        while (rs.next()) {
            int filmId = rs.getInt("FILM_ID");
            int directorId = rs.getInt("director_id");
            String directorName = rs.getString("director_name");
            Film film = results.get(filmId);
            Set<Director> directors = film.getDirectors();

            Director director = new Director(directorId, directorName);
            directors.add(director);
        }

        return new ArrayList<>(results.values());
    }

    @Override
    public Optional<Film> getFilmById(int id) {
        String sql = "select * from films where id = ?";
        List<Film> films = jdbcTemplate.query(sql, filmRowMapper, id);
        if (films.isEmpty()) {
            log.info("Фильм с идентификатором {} не найден.", id);
            return Optional.empty();
        } else {
            Film film = films.get(0);
            // get genres
            sql = "select GENRE_ID from film_genres where FILM_ID = ?";
            SqlRowSet genresRowSet = jdbcTemplate.queryForRowSet(sql, film.getId());
            while (genresRowSet.next()) {
                film.getGenres().add(genreStorage.getGenreById(genresRowSet.getInt("GENRE_ID")));
            }
            // get director
            sql = "select director_id from directors_films where FILM_ID = ?";
            SqlRowSet directorRowSet = jdbcTemplate.queryForRowSet(sql, film.getId());
            while (directorRowSet.next()) {
                film.getDirectors().add(directorStorage.getDirectorById(directorRowSet.getInt("DIRECTOR_ID")).get());
            }
            // get likes
            sql = "select USER_ID from FILM_LIKES where FILM_ID = ?";
            SqlRowSet likesRowSet = jdbcTemplate.queryForRowSet(sql, film.getId());
            while (likesRowSet.next()) {
                film.getLikes().add(likesRowSet.getInt("USER_ID"));
            }
            return Optional.of(films.get(0));
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
        Optional<Film> filmOptional = getFilmById(film.getId());
        filmOptional.ifPresent(value -> film.setLikes(value.getLikes()));

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


        if (sortBy.equals("year")) {
            List<Integer> filmsId = jdbcTemplate.query(sqlQueryByYear, (rs, rowNum) -> rs.getInt("film_id"), directorId);

            for (Integer id : filmsId) {
                films.add(getFilmById(id).get());
            }
            return films;
        } else {
            List<Integer> filmsId = jdbcTemplate.query(sqlQueryByLikes, (rs, rowNum) -> rs.getInt("film_id"), directorId);
            for (Integer id : filmsId) {
                films.add(getFilmById(id).get());
            }
            return films;
        }
    }

    /*@Override
    public List<Film> getPopularFilms(HashMap<String, Object> params) {

        *//*String sqlQueryPopularFilms = "SELECT id, film_name, description, release_date, duration, mpa_film_rating_id " +
                "FROM films LEFT JOIN film_likes ON films.id = film_likes.film_id " +
                "left JOIN FILM_GENRES ON films.id = FILM_GENRES.FILM_ID " +
                "WHERE ((NOT :filtered_by_year) OR  YEAR = :YEAR) " +
                "AND ((NOT :filtered_by_genre_id) OR  genre_id = :genre_id)" +
                "GROUP BY films.id " +
                "ORDER BY COUNT(film_likes.user_id) DESC LIMIT ? ";*//*
        String sqlQueryPopularFilms = "WITH FILTERED_FILMS AS ( " +
                "SELECT films.id AS film_id  " +
                "FROM FILMS " +
                "WHERE :is_filtered_by_year OR EXTRACT(YEAR FROM FILMS.RELEASE_DATE) = :year " +
                "UNION  " +
                "SELECT film_genres.film_id  " +
                "FROM FILM_GENRES  " +
                "WHERE :is_filtered_by_genre_id OR genre_id = :genre_id) " +
                "SELECT films.ID, films.FILM_NAME, films.DESCRIPTION, films.RELEASE_DATE, films.DURATION" +
                " FROM FILMS  " +
                "   INNER JOIN ( " +
                "       SELECT FILTERED_FILMS.FILM_ID  " +
                "       FROM FILTERED_FILMS  " +
                "      LEFT JOIN FILM_LIKES ON FILTERED_FILMS.FILM_ID = FILM_LIKES.FILM_ID  " +
                "    GROUP BY FILTERED_FILMS.FILM_ID " +
                "    ORDER BY COUNT(FILM_LIKES.USER_ID) DESC) AS SORTED_FILMS " +
                "         ON SORTED_FILMS.FILM_ID = FILMS.ID " +
                "LIMIT :count";

        return  namedParameterJdbcTemplate.query(sqlQueryPopularFilms, params, filmRowMapper);

    }*/
    @Override
    @Transactional
    public List<Film> getPopularFilmsSortedByYear(Integer count, Integer year){
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


        return films;
    }
    @Override
    public List<Film> getPopularFilms(Integer count, Integer genreId, Integer year){
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

        return films;
    }
    @Override
    public List<Film> getPopularFilmsSortedByGenre(Integer count, Integer genreId){
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
                " INNER JOIN GENRES ON GENRES.ID = FG.GENRE_ID "  +
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
            List<Genre> genres = namedParameterJdbcTemplate.query(sql, params);
            film.setGenres(new HashSet<>(genres));

            // get directors
            sql = "select " +
                    " DIRECTORS.ID, " +
                    " DIRECTOR_NAME " +
                    " FROM " +
                    "   directors_films " +
                    " INNER JOIN DIRECTORS ON DIRECTORS_FILMS.DIRECTOR_ID = DIRECTORS.ID" +
                    " WHERE FILM_ID = :FILM_ID";
            List<Director> directors = namedParameterJdbcTemplate.query(sql, params);
            film.setDirectors(new HashSet<>(directors));

            // get likes
            sql = "select USER_ID from FILM_LIKES where FILM_ID = :FILM_ID";
            List<Integer> likes = namedParameterJdbcTemplate.query(sql, params);
            film.setLikes(new HashSet<>(likes));
        }
    }

    /*@Override
    public Film delete(Integer filmId) {
        Film film = getFilmById(filmId).get();
        String sqlQuery = "DELETE FROM films WHERE id = ? ";
        return jdbcTemplate.update(sqlQuery, filmId);
    }*/

}
