package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;

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

    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("ID", this.id);
        result.put("DIRECTOR_NAME", this.name);
        return result;
    }
}
