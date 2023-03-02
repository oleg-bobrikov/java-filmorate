package ru.yandex.practicum.filmorate.exception;


import lombok.Builder;
import lombok.Data;
import lombok.Value;

import java.time.LocalDateTime;

@Data
@Builder
@Value
public class ResponseError {
    public LocalDateTime timestamp;
    public String error;
    public String exception;
    public int status;
    public String message;
    public String path;
}
