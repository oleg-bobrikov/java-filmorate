package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.impl.DirectorH2Storage;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class DirectorStorageTest {
    @Autowired
    private DirectorH2Storage directorStorage;

    private static Director director1;
    private static Director director2;
    private static Director director3;
    private static Director director4;

    @BeforeEach
    void testCreateDirectors() {
        director1 = new Director(1, "Pavel");
        director2 = new Director(2, "Roman");
        director3 = new Director(1, "Kate");
        director4 = new Director(1, "Potter");

        directorStorage.createDirector(director1);
        directorStorage.createDirector(director2);
        directorStorage.createDirector(director3);
        directorStorage.createDirector(director4);
    }

    @Test
    void testGetAllDirectors() {

        Director director1 = directorStorage.findDirectorById(1).get();
        Director director2 = directorStorage.findDirectorById(2).get();
        Director director3 = directorStorage.findDirectorById(3).get();
        Director director4 = directorStorage.findDirectorById(4).get();

        List<Director> list = directorStorage.findAll();

        assertEquals(list.getClass(), directorStorage.findAll().getClass());
        assertEquals(4, directorStorage.findAll().size());
        assertThat(list).asList().contains(director1, director2, director3, director4);
    }


    @Test
    void testGetDirectorById() {
        Optional<Director> directorOptional = directorStorage.findDirectorById(1);
        assertThat(directorOptional).isPresent()
                .hasValueSatisfying(director ->
                        assertThat(director)
                                .hasFieldOrPropertyWithValue("id", 1)
                                .hasFieldOrPropertyWithValue("name", "Pavel"));
    }


    @Test
    void testUpdateDirector() {
        Director director = new Director(2, "Vlad");
        directorStorage.updateDirector(director);
        assertNotEquals(director2, director);
        assertEquals(director, directorStorage.findDirectorById(2).get());
        assertThat(director)
                .hasFieldOrPropertyWithValue("id", 2)
                .hasFieldOrPropertyWithValue("name", "Vlad");
    }

    @Test
    void testRemoveDirector() {
        directorStorage.removeDirector(1);
        assertEquals(3, directorStorage.findAll().size());

        Director director2 = directorStorage.findDirectorById(2).get();
        Director director3 = directorStorage.findDirectorById(3).get();
        Director director4 = directorStorage.findDirectorById(4).get();

        List<Director> list = directorStorage.findAll();

        assertEquals(list.getClass(), directorStorage.findAll().getClass());
        assertThat(list).asList().contains(director2, director3, director4);
    }
}




