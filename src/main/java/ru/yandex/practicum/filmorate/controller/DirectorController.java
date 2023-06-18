package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {

    private final DirectorService directorService;

    @GetMapping()
    public List<Director> getAll() {
        return directorService.getAll();
    }

    @GetMapping("/{id}")
    public Director getDirectorById(@PathVariable Integer id) {
        return directorService.getDirectorById(id);
    }

    @PostMapping()
    public Director createDirector(@RequestBody @Valid Director director) {
        directorService.createDirector(director);
        log.info("{} has created", director);
        return director;
    }

    @PutMapping()
    public Director updateDirector(@Valid @RequestBody Director director) {
        Director updatedDirector = directorService.updateDirector(director);
        log.info("{} has updated", director);
        return updatedDirector;
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void removeDirector(@PathVariable Integer id) {
        directorService.removeDirector(id);
    }
}
