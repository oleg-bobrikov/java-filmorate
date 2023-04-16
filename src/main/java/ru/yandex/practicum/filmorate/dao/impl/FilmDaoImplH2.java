package ru.yandex.practicum.filmorate.dao.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.FilmDao;
import ru.yandex.practicum.filmorate.dto.Film;
import ru.yandex.practicum.filmorate.dto.Genre;
import ru.yandex.practicum.filmorate.dto.Mpa;
import ru.yandex.practicum.filmorate.dto.User;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import javax.sql.DataSource;
import java.util.*;


@Component("filmDaoImplH2")
@Slf4j
public class FilmDaoImplH2 implements FilmDao {
    @Autowired
    private final JdbcTemplate jdbcTemplate;
    @Autowired
    private GenreStorage genreStorage;
    @Autowired
    MpaStorage mpaStorage;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final GeneratedKeyHolder generatedKeyHolder;

    public FilmDaoImplH2(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        DataSource dataSource = jdbcTemplate.getDataSource();
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(Objects.requireNonNull(dataSource));
        generatedKeyHolder = new GeneratedKeyHolder();
    }

    @Override
    public Film add(Film film) {
        String sql = "insert into films (\"name\", description, release_date, duration, mpa_film_rating_id) " +
                "VALUES(:name, :description, :release_date, :duration, :mpa_film_rating_id);";

        Map<String, Object> params = new HashMap<>();
        params.put("name", film.getName());
        params.put("description", film.getDescription());
        params.put("release_date", film.getReleaseDate());
        params.put("duration", film.getDuration());
        Mpa mpa = film.getMpa();
        params.put("mpa_film_rating_id", mpa == null ? null : mpa.getId());

        int rowsAffected = namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(params), generatedKeyHolder);

        Integer id = Objects.requireNonNull(generatedKeyHolder.getKey()).intValue();

        log.info("rowsAffected = {}, id={}", rowsAffected, id);

        film.setId(id);
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
    public List<Film> getFilms() {
        String sql = "select * from get_films";

        HashMap<Integer, Film> results = new HashMap<>();
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql);
        while (rs.next()) {
            Mpa mpa = Mpa.builder()
                    .id(rs.getInt("MPA_FILM_RATING_ID"))
                    .name(rs.getString("MPA_FILM_RATING_NAME"))
                    .build();
            Film film = Film.builder()
                    .id(rs.getInt("ID"))
                    .name(rs.getString("name"))
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
        sql = "select distinct * from get_film_genres";
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
    public Film getFilmById(int id) {
        String sql = "select * from films where id = ?";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql, id);
        if (rs.next()) {
            Mpa mpa = mpaStorage.getMpaById(rs.getInt("MPA_FILM_RATING_ID"));

            Film film = Film.builder()
                    .id(rs.getInt("ID"))
                    .name(rs.getString("name"))
                    .description(rs.getString("DESCRIPTION"))
                    .releaseDate(Objects.requireNonNull(rs.getDate("RELEASE_DATE")).toLocalDate())
                    .duration(rs.getInt("DURATION"))
                    .mpa(mpa)
                    .build();

            log.info("Найден фильм c иднетификатором: {}", id);
            sql = "select GENRE_ID from film_genres where FILM_ID = ?";
            rs = jdbcTemplate.queryForRowSet(sql, id);
            while (rs.next()) {
                film.getGenres().add(genreStorage.getGenreById(rs.getInt("GENRE_ID")));
            }
            return film;
        } else {
            log.info("Фильм с идентификатором {} не найден.", id);
            return null;
        }
    }

    @Override
    public Film update(Film film) {
        String sql = "update films set \"name\" = :name, description = :description, release_date = :release_date," +
                "duration = :duration, mpa_film_rating_id = :mpa_film_rating_id where id = :id";

        Mpa mpa = film.getMpa();

        Map<String, Object> params = new HashMap<>();
        params.put("name", film.getName());
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

        Set<Genre> newGenres = new TreeSet<>(Comparator.comparing(Genre::getId));
        for (Genre genre : film.getGenres()) {
            newGenres.add(genreStorage.getGenreById(genre.getId()));
        }
        genreStorage.updateFilmGenres(film, newGenres);
        film.setGenres(newGenres);

        return film;
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

        String sql =
                "SELECT FILMS.ID, " +
                "   FILMS.\"name\", " +
                "   FILMS.DESCRIPTION, " +
                "   FILMS.RELEASE_DATE, " +
                "   FILMS.DURATION, " +
                "   FILMS.MPA_FILM_RATING_ID, " +
                "   MFR.\"NAME\" AS MPA_FILM_RATING_NAME " +
                "FROM FILMS AS FILMS " +
                "LEFT JOIN " +
                "  (SELECT FILM_ID, " +
                "          COUNT(USER_ID) AS TOTAL " +
                "   FROM FILM_LIKES " +
                "   GROUP BY FILM_ID " +
                "   ORDER BY COUNT(USER_ID)) AS LIKES ON FILMS.ID = LIKES.FILM_ID " +
                "INNER JOIN MPA_FILM_RATINGS AS MFR ON MFR.ID = FILMS.MPA_FILM_RATING_ID " +
                "ORDER BY IFNULL (LIKES.TOTAL, 0) DESC " +
                "FETCH FIRST ? ROWS ONLY;";

        HashMap<Integer, Film> results = new HashMap<>();
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql, count);
        while (rs.next()) {
            Mpa mpa = Mpa.builder()
                    .id(rs.getInt("MPA_FILM_RATING_ID"))
                    .name(rs.getString("MPA_FILM_RATING_NAME"))
                    .build();
            Film film = Film.builder()
                    .id(rs.getInt("ID"))
                    .name(rs.getString("name"))
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
        sql = "select distinct * from get_film_genres";
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

    public Set<Genre> getFilmGenresById(int filmId) {
        String sql = "select genre_id from film_genres where film_id = ?";

        Set<Genre> genres = new HashSet<>();
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql, filmId);
        while (rs.next()) {
            genres.add(genreStorage.getGenreById(rs.getInt("GENRE_ID")));
        }
        return genres;
    }
}
