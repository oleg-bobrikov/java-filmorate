package ru.yandex.practicum.filmorate.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.ReviewLike;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ReviewLikeRowMapper implements RowMapper<ReviewLike> {

    @Override
    public ReviewLike mapRow(ResultSet rs, int rowNum) throws SQLException {
        return ReviewLike.builder()
                .reviewId(rs.getInt("REVIEW_ID"))
                .userId(rs.getInt("USER_ID"))
                .isLike(rs.getBoolean("IS_LIKE"))
                .build();
    }
}
