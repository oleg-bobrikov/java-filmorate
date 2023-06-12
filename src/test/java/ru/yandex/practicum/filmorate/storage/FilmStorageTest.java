package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.storage.impl.DirectorH2Storage;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class FilmStorageTest {
    @Autowired
    @Qualifier("filmH2Storage")
    private FilmStorage filmStorage;
    @Autowired
    @Qualifier("userH2Storage")
    private UserStorage userStorage;
    @Autowired
    private DirectorH2Storage directorStorage;

    private Film film1;
    private Film film2;
    private Film film3;
    private User user1;
    private User user2;
    private User user3;
    private Director director1;
    private static Director director2;
    private static Director director3;
    private static Director director4;

    @BeforeEach
    public void setUp() {
        Mpa mpa1 = Mpa.builder().id(1).build();
        Genre thriller = Genre.builder().id(4).build();
        Film newFilm = Film.builder()
                .name("Аватар")
                .description("3D")
                .duration(180)
                .releaseDate(LocalDate.of(2009, 12, 1))
                .mpa(mpa1)
                .build();
        newFilm.setGenres(Set.of(thriller));
        film1 = filmStorage.add(newFilm);

        newFilm = Film.builder()
                .name("Аватар 2")
                .description("5D")
                .duration(180)
                .releaseDate(LocalDate.of(2022, 12, 1))
                .mpa(mpa1)
                .build();
        newFilm.setGenres(Set.of(thriller));
        film2 = filmStorage.add(newFilm);

        newFilm = Film.builder()
                .name("Wednesday")
                .description("popular")
                .duration(180)
                .releaseDate(LocalDate.of(2022, 12, 1))
                .mpa(mpa1)
                .build();
        newFilm.setGenres(Set.of(thriller));
        film3 = filmStorage.add(newFilm);

        User user = User.builder()
                .email("guest1@ya.ru")
                .login("guest1@ya.ru")
                .name("guest1")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
        user1 = userStorage.add(user);

        user = User.builder()
                .email("guest2@ya.ru")
                .login("guest2@ya.ru")
                .name("guest2")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
        user2 = userStorage.add(user);

        user = User.builder()
                .email("guest3@ya.ru")
                .login("guest3@ya.ru")
                .name("guest3")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
        user3 = userStorage.add(user);

        director1 = new Director(1, "Pavel");
        director2 = new Director(2, "Roman");
        director3 = new Director(1, "Kate");
        director4 = new Director(1, "Авраам");

        directorStorage.createDirector(director1);
        directorStorage.createDirector(director2);
        directorStorage.createDirector(director3);
        directorStorage.createDirector(director4);
    }

    @Test
    public void update_returnUpdatedFilm() {
        //arrange
        Mpa mpa2 = Mpa.builder().id(2).build();
        Genre genre1 = Genre.builder().id(1).build();
        Genre genre2 = Genre.builder().id(2).build();

        //steps
        film1.setName("Папаши 2");
        film1.setDescription("Франзцузская комедия s2");
        film1.setDuration(102);
        film1.setReleaseDate(LocalDate.of(2021, 2, 1));
        film1.setMpa(mpa2);
        film1.setGenres(Set.of(genre1, genre2));

        //act
        Film actual = filmStorage.update(film1);

        //assert
        assertThat(actual)
                .hasFieldOrPropertyWithValue("id", film1.getId())
                .hasFieldOrPropertyWithValue("name", film1.getName())
                .hasFieldOrPropertyWithValue("description", film1.getDescription())
                .hasFieldOrPropertyWithValue("releaseDate", film1.getReleaseDate())
                .hasFieldOrPropertyWithValue("mpa", film1.getMpa())
                .hasFieldOrPropertyWithValue("genres", film1.getGenres());
    }

    @Test
    void deleteFilmById_returnEmpty() {
        //steps
        filmStorage.deleteFilmById(film1.getId());

        //assert
        Assertions.assertTrue(filmStorage.getFilmById(film1.getId()).isEmpty());
    }

    @Test
    void getFilms_returnFilms() {
        assertThat(filmStorage.getFilms())
                .asList()
                .contains(film1, film2);
    }

    @Test
    void addLike() {
        //steps
        filmStorage.addLike(film1, user1);

        //act
        Optional<Film> actual = filmStorage.getFilmById(film1.getId());

        //assert
        assertThat(actual)
                .isPresent()
                .hasValueSatisfying(film -> assertThat(new ArrayList<>(film.getLikes()))
                        .asList()
                        .hasSize(1)
                        .contains(user1.getId())
                );
    }

    @Test
    void removeLike() {
        //steps
        filmStorage.addLike(film1, user1);
        filmStorage.removeLike(film1, user1);

        //act
        Optional<Film> actual = filmStorage.getFilmById(film1.getId());

        //assert
        assertThat(actual)
                .isPresent()
                .hasValueSatisfying(film -> assertThat(new ArrayList<>(film.getLikes()))
                        .asList()
                        .hasSize(0)
                );
    }

    @Test
    void getPopular_return2PopularFilms() {
        //arrange
        filmStorage.addLike(film3, user1);
        filmStorage.addLike(film3, user2);
        filmStorage.addLike(film3, user3);
        filmStorage.addLike(film2, user1);
        filmStorage.addLike(film2, user2);
        filmStorage.addLike(film1, user1);

        //act
        List<Film> actual = filmStorage.getPopular(2);

        //assert
        assertThat(actual).asList()
                .hasSize(2)
                .contains(film3, film2);
    }

    @Test
    void searchFilms_returnSortedListFilmsByTitle() {
        //arrange
        filmStorage.addLike(film1, user1);
        filmStorage.addLike(film1, user2);
        filmStorage.addLike(film2, user3);

        //expected
        List<Film> exp = new ArrayList<>(filmStorage.searchFilms("ав", List.of("title")));

        //assert
        Assertions.assertEquals(film1, exp.get(0));
        Assertions.assertEquals(film2, exp.get(1));
    }

    @Test
    public void searchFilms_returnSortedListFilmsByDirector() {
        //arrange
        filmStorage.addLike(film2, user1);
        filmStorage.addLike(film2, user2);
        filmStorage.addLike(film1, user3);
        film2.setDirectors(new HashSet<>(List.of(director1)));
        film1.setDirectors(new HashSet<>(List.of(director1)));
        directorStorage.updateFilmDirector(film1, new HashSet<>(List.of(director1)));
        directorStorage.updateFilmDirector(film2, new HashSet<>(List.of(director1)));

        //expected
        List<Film> exp = new ArrayList<>(filmStorage.searchFilms("pav", List.of("director")));

        //assert
        Assertions.assertEquals(film2, exp.get(0));
        Assertions.assertEquals(film1, exp.get(1));
    }

    @Test
    void searchFilms_returnSortedListFilmsByDirectorAndTitle() {
        //arrange
        filmStorage.addLike(film2, user1);
        filmStorage.addLike(film3, user3);
        filmStorage.addLike(film3, user2);
        film2.setDirectors(new HashSet<>(List.of(director1)));
        film3.setDirectors(new HashSet<>(List.of(director4)));
        directorStorage.updateFilmDirector(film2, new HashSet<>(List.of(director1)));
        directorStorage.updateFilmDirector(film3, new HashSet<>(List.of(director4)));

        //expected
        List<Film> exp = new ArrayList<>(filmStorage.searchFilms("ав", List.of("director", "title")));

        Assertions.assertEquals(film3, exp.get(0));
        Assertions.assertEquals(film2, exp.get(1));
        Assertions.assertEquals(film1, exp.get(2));
    }
}