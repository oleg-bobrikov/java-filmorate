package ru.yandex.practicum.filmorate.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import ru.yandex.practicum.filmorate.validator.IsAfterOrEqual;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
public class Film {
    private Integer id;
    @NotBlank(message = "Title cannot be empty")
    private String name;
    @Size(max = 200, message = "Maximum description length is 200 characters")
    private String description;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @IsAfterOrEqual(current = "1895-12-28", message = "Release date should be equal or after the 28th of December 1895")
    private LocalDate releaseDate;
    @Positive(message = "Duration should be positive")
    private int duration;
    @Getter(AccessLevel.NONE)
    private Set<Integer> likes;

    public Set<Integer> getLikes() {
        if (likes == null) {
            likes = new HashSet<>();
        }
        return likes;
    }


}
