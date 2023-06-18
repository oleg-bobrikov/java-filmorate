package ru.yandex.practicum.filmorate.storage.impl;

import org.slf4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.ReviewLike;
import ru.yandex.practicum.filmorate.mapper.ReviewRowMapper;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import javax.sql.DataSource;
import java.util.*;

@Component
public class ReviewH2Storage implements ReviewStorage {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ReviewH2Storage.class);
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final GeneratedKeyHolder generatedKeyHolder;
    private final ReviewRowMapper reviewRowMapper;

    public ReviewH2Storage(JdbcTemplate jdbcTemplate, ReviewRowMapper reviewRowMapper) {
        this.reviewRowMapper = reviewRowMapper;
        DataSource dataSource = jdbcTemplate.getDataSource();
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(Objects.requireNonNull(dataSource));
        generatedKeyHolder = new GeneratedKeyHolder();
    }

    @Override
    public Optional<Review> add(Review review) {
        String sql = "insert into REVIEWS (CONTENT, IS_POSITIVE, USER_ID, FILM_ID) " +
                "VALUES(:CONTENT, :IS_POSITIVE, :USER_ID, :FILM_ID);";

        Map<String, Object> params = review.toMap();

        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(params), generatedKeyHolder);

        int id = Objects.requireNonNull(generatedKeyHolder.getKey()).intValue();
        log.info("Создан отзыв с идентификатором: {}", id);

        return findReviewById(id);
    }

    @Override
    public Optional<Review> findReviewById(Integer reviewId) {
        String sql = "SELECT" +
                "    REVIEWS.ID AS ID," +
                "    REVIEWS.CONTENT AS CONTENT," +
                "    REVIEWS.IS_POSITIVE AS IS_POSITIVE," +
                "    REVIEWS.USER_ID AS USER_ID," +
                "    REVIEWS.FILM_ID AS FILM_ID," +
                "    SUM(" +
                "        CASE" +
                "            WHEN REVIEW_LIKES.IS_LIKE IS NULL THEN 0" +
                "            WHEN REVIEW_LIKES.IS_LIKE THEN 1" +
                "            ELSE -1" +
                "        END" +
                "    ) AS USEFUL" +
                " FROM" +
                "    REVIEWS" +
                "    LEFT JOIN REVIEW_LIKES ON REVIEW_LIKES.REVIEW_ID = REVIEWS.ID" +
                " WHERE" +
                "    REVIEWS.ID = :ID" +
                " GROUP BY" +
                "    REVIEWS.ID," +
                "    REVIEWS.CONTENT," +
                "    REVIEWS.IS_POSITIVE," +
                "    REVIEWS.USER_ID," +
                "    REVIEWS.FILM_ID";
        HashMap<String, Object> params = new HashMap<>();
        params.put("ID", reviewId);

        List<Review> reviews = namedParameterJdbcTemplate.query(sql, params, reviewRowMapper);
        if (reviews.isEmpty()) {
            log.info("Отзыв с идентификатором {} не найден.", reviewId);
            return Optional.empty();
        } else {
            log.info("Найден отзыв c идентификатором : {}", reviewId);
            return Optional.of(reviews.get(0));
        }
    }

    @Override
    public Optional<Review> update(Review review) {
        String sql = "UPDATE" +
                " REVIEWS " +
                " SET" +
                "    CONTENT = :CONTENT," +
                "    IS_POSITIVE = :IS_POSITIVE" +
                " WHERE" +
                "    ID =:ID";

        Map<String, Object> params = review.toMap();
        namedParameterJdbcTemplate.update(sql, params);
        log.info("Отзыв с идентификатором {} изменен.", review.getReviewId());

        return findReviewById(review.getReviewId());
    }

    @Override
    public void deleteReview(int id) {
        String sql = "delete from REVIEW_LIKES where REVIEW_ID = :ID; " +
                "delete from REVIEWS where ID = :ID;";

        Map<String, Object> params = new HashMap<>();
        params.put("ID", id);

        int rowsAffected = namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(params), generatedKeyHolder);
        if (rowsAffected > 0) {
            log.info("Отзыв с идентификатором {} удален", id);
        } else {
            log.info("Отзыв с идентификатором {} не найден", id);
        }

    }


    @Override
    public List<Review> getAllReviewsByFilmId(Integer filmId, Integer count) {
        String sql = "SELECT" +
                "    REVIEWS.ID AS ID," +
                "    REVIEWS.CONTENT AS CONTENT," +
                "    REVIEWS.IS_POSITIVE AS IS_POSITIVE," +
                "    REVIEWS.USER_ID AS USER_ID," +
                "    REVIEWS.FILM_ID AS FILM_ID," +
                "    SUM(" +
                "        CASE" +
                "            WHEN REVIEW_LIKES.IS_LIKE IS NULL THEN 0" +
                "            WHEN REVIEW_LIKES.IS_LIKE THEN 1" +
                "            ELSE -1" +
                "        END" +
                "    ) AS USEFUL" +
                " FROM" +
                "    REVIEWS" +
                "    LEFT JOIN REVIEW_LIKES ON REVIEW_LIKES.REVIEW_ID = REVIEWS.ID" +
                " WHERE" +
                "    REVIEWS.FILM_ID = :FILM_ID" +
                " GROUP BY" +
                "    REVIEWS.ID," +
                "    REVIEWS.CONTENT," +
                "    REVIEWS.IS_POSITIVE," +
                "    REVIEWS.USER_ID," +
                "    REVIEWS.FILM_ID" +
                " ORDER BY" +
                "    USEFUL DESC" +
                " LIMIT" +
                "    :COUNT";
        Map<String, Object> params = new HashMap<>();
        params.put("FILM_ID", filmId);
        params.put("COUNT", count);
        return namedParameterJdbcTemplate.query(sql, params, reviewRowMapper);
    }

    @Override
    public void addAnyLike(ReviewLike reviewLike) {
        String sql = " MERGE INTO REVIEW_LIKES KEY (REVIEW_ID, USER_ID, IS_LIKE) " +
                " VALUES (:REVIEW_ID, :USER_ID, :IS_LIKE)";

        Map<String, Object> params = reviewLike.toMap();

        namedParameterJdbcTemplate.update(sql, params);
        log.info("Отзыв с идентификатором {} получил {} от пользователя с идентификатором {}",
                reviewLike.getReviewId(), reviewLike.isLike() ? "лайк" : "дизлайк", reviewLike.getUserId());
    }

    @Override
    public void removeAnyLike(ReviewLike reviewLike) {
        String sql = "DELETE" +
                " FROM REVIEW_LIKES" +
                " WHERE REVIEW_ID = :REVIEW_ID" +
                " AND USER_ID = :USER_ID";

        Map<String, Object> params = reviewLike.toMap();
        int rows = namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(params));
        if (rows > 0) {
            log.info("Лайк на отзыв с идентификатором {} от пользователя с идентификатором {} удален.",
                    reviewLike.getReviewId(), reviewLike.getUserId());
        }
    }

    @Override
    public void removeDislike(ReviewLike reviewLike) {
        String sql = "DELETE" +
                " FROM REVIEW_LIKES" +
                " WHERE REVIEW_ID = :REVIEW_ID" +
                " AND USER_ID = :USER_ID" +
                " AND NOT IS_LIKE";

        Map<String, Object> params = reviewLike.toMap();
        int rows = namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(params));
        if (rows > 0) {
            log.info("Дизлайк на отзыв с идентификатором {} от пользователя с идентификатором {} удален.",
                    reviewLike.getReviewId(), reviewLike.getUserId());
        }
    }

    @Override
    public List<Review> getAllReviews(Integer count) {
        String sql = "SELECT" +
                "    REVIEWS.ID AS ID," +
                "    REVIEWS.CONTENT AS CONTENT," +
                "    REVIEWS.IS_POSITIVE AS IS_POSITIVE," +
                "    REVIEWS.USER_ID AS USER_ID," +
                "    REVIEWS.FILM_ID AS FILM_ID," +
                "    SUM(" +
                "        CASE" +
                "            WHEN REVIEW_LIKES.IS_LIKE IS NULL THEN 0" +
                "            WHEN REVIEW_LIKES.IS_LIKE THEN 1" +
                "            ELSE -1" +
                "        END" +
                "    ) AS USEFUL" +
                " FROM" +
                "    REVIEWS" +
                "    LEFT JOIN REVIEW_LIKES ON REVIEW_LIKES.REVIEW_ID = REVIEWS.ID" +
                " GROUP BY" +
                "    REVIEWS.ID," +
                "    REVIEWS.CONTENT," +
                "    REVIEWS.IS_POSITIVE," +
                "    REVIEWS.USER_ID," +
                "    REVIEWS.FILM_ID" +
                " ORDER BY" +
                "    USEFUL DESC" +
                " LIMIT" +
                "    :COUNT";
        Map<String, Object> params = new HashMap<>();
        params.put("COUNT", count);
        return namedParameterJdbcTemplate.query(sql, params, new ReviewRowMapper());
    }
}

