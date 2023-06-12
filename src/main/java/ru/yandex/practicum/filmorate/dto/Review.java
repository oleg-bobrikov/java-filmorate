package ru.yandex.practicum.filmorate.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder(toBuilder = true)
public class Review {

    private Integer reviewId;

    @NotBlank(message = "Content cannot be empty")
    private String content;

    @NotNull
    private Boolean isPositive;

    @NotNull
    private Integer userId;

    @NotNull
    private Integer filmId;

    private int useful;

    public Map<String, Object> toMap() {
        Map<String, Object> params = new HashMap<>();
        params.put("ID", reviewId);
        params.put("CONTENT", content);
        params.put("IS_POSITIVE", isPositive);
        params.put("USER_ID", userId);
        params.put("FILM_ID", filmId);
        params.put("USEFUL", useful);
        return params;
    }
}
