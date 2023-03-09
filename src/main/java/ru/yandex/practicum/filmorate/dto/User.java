package ru.yandex.practicum.filmorate.dto;

import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.validator.Login;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
public class User {
    private Integer id;
    @NotBlank
    @Email(message = "invalid email address")
    private String email;
    @Login()
    private String login;
    private String name;
    @PastOrPresent(message = "birthday must be in the past or present")
    private LocalDate birthday;
    @Builder.Default
    private Set<Integer> friends = new HashSet<>();
}
