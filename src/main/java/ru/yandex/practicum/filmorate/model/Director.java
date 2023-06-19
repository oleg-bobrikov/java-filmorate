package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Builder
public class Director {
    private Integer id;

    @NotBlank(message = "Name cannot be empty")
    private String name;

    public Director(int id, String name) {
        this.id = id;
        this.name = name;
    }
}
