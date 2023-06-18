package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewLike {
    private int reviewId;
    private int userId;
    private boolean isLike;
}

