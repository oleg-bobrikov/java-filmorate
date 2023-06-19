package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.exception.DirectorAlreadyExistedException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DirectorService {
    private final DirectorStorage directorStorage;


    private static final Logger log = LoggerFactory.getLogger(DirectorService.class);

    public DirectorService(DirectorStorage directorStorage) {
        this.directorStorage = directorStorage;
    }

    public List<Director> getAll() {
        List<Director> directors = directorStorage.findAll();
        if (directors.isEmpty()) {
            log.warn("Список режессеров пуст!");
        }
        return directors;
    }

    public Director getDirectorById(Integer id) {
        return directorStorage.findDirectorById(id).orElseThrow(() ->
                new NotFoundException("Режессер с идентификатором " + id + " не найден."));
    }

    public Director createDirector(Director director) {
        return directorStorage.createDirector(director).orElseThrow(() ->
                new DirectorAlreadyExistedException("Режессер уже существует."));
    }

    public Director updateDirector(Director director) {
        return directorStorage.updateDirector(director).get().orElseThrow(() ->
                new NotFoundException("Такого режессера нет"));
    }

    public void removeDirector(int id) {
        directorStorage.findDirectorById(id).orElseThrow(() ->
                new NotFoundException("Такого режессера нет."));
        directorStorage.removeDirector(id);
    }

    public Map<String, Object> directorToMap(Director director) {
        Map<String, Object> result = new HashMap<>();
        result.put("ID", director.getId());
        result.put("DIRECTOR_NAME", director.getName());
        return result;
    }
}
