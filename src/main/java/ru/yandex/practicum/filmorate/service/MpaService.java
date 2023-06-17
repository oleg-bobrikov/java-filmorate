package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.Mpa;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MpaService {

    private final MpaStorage mpaStorage;

    public List<Mpa> getAll() {
        return mpaStorage.findAll();
    }

    public Mpa getMpaById(Integer id) {
        Mpa mpa = mpaStorage.findMpaById(id);
        if (mpa == null) {
            throw new NotFoundException("Рейтинг фильма с идентификатором " + id + " не найден.");
        }

        return mpa;
    }
}
