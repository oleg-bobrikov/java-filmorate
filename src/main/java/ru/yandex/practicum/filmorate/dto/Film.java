package ru.yandex.practicum.filmorate.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.validator.IsAfterOrEqual;

import javax.validation.constraints.*;
import java.time.LocalDate;

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
}
