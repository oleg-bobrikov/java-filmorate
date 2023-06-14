package ru.yandex.practicum.filmorate.dto;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
public class ReviewLike {
    private int reviewId;
    private int userId;
    private boolean isLike;

    public Map<String, Object> toMap() {
        Map<String, Object> params = new HashMap<>();
        params.put("REVIEW_ID", reviewId);
        params.put("USER_ID", userId);
        params.put("IS_LIKE", isLike);
        return params;
    }
}

