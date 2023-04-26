package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.dto.Film;
import ru.yandex.practicum.filmorate.dto.Genre;
import ru.yandex.practicum.filmorate.dto.Mpa;
import ru.yandex.practicum.filmorate.dto.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmStorageTest {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Test
    public void update_returnUpdatedFilm() {
        //arrange
        Mpa mpa1 = Mpa.builder().id(1).build();
        Mpa mpa2 = Mpa.builder().id(2).build();
        Genre genre1 = Genre.builder().id(1).build();
        Genre genre2 = Genre.builder().id(2).build();
        Film testFilm = Film.builder()
                .name("Папаши")
                .description("Франзцузская комедия")
                .duration(92)
                .releaseDate(LocalDate.of(1983, 2, 1))
                .mpa(mpa1)
                .build();
        testFilm.setGenres(Set.of(genre1));
        Film createdFilm = filmStorage.add(testFilm);
        testFilm.setId(createdFilm.getId());

        testFilm.setName("Папаши 2");
        testFilm.setDescription("Франзцузская комедия s2");
        testFilm.setDuration(102);
        testFilm.setReleaseDate(LocalDate.of(2021, 2, 1));
        testFilm.setMpa(mpa2);
        testFilm.setGenres(Set.of(genre1, genre2));

        //act
        Film actual = filmStorage.update(testFilm);

        //assert
        assertThat(actual)
                .hasFieldOrPropertyWithValue("id", testFilm.getId())
                .hasFieldOrPropertyWithValue("name", testFilm.getName())
                .hasFieldOrPropertyWithValue("description", testFilm.getDescription())
                .hasFieldOrPropertyWithValue("releaseDate", testFilm.getReleaseDate())
                .hasFieldOrPropertyWithValue("mpa", testFilm.getMpa())
                .hasFieldOrPropertyWithValue("genres", testFilm.getGenres());
    }

    @Test
    void deleteFilmById_returnEmpty() {
        //arrange
        Mpa mpa1 = Mpa.builder().id(1).build();
        Genre genre1 = Genre.builder().id(1).build();
        Film newFilm = Film.builder()
                .name("Перевозчик")
                .description("Триллер")
                .duration(92)
                .releaseDate(LocalDate.of(2002, 2, 1))
                .mpa(mpa1)
                .build();
        newFilm.setGenres(Set.of(genre1));
        Film createdFilm = filmStorage.add(newFilm);
        final int id = createdFilm.getId();

        //act
        filmStorage.deleteFilmById(id);
        Optional<Film> actual = filmStorage.getFilmById(id);

        //assert
        assertThat(actual).isEmpty();
    }

    @Test
    void getFilms_returnFilms() {
        //arrange
        Mpa mpa1 = Mpa.builder().id(1).build();
        Mpa mpa2 = Mpa.builder().id(2).build();
        Genre genre1 = Genre.builder().id(1).build();
        Genre genre2 = Genre.builder().id(2).build();
        Film newFilm1 = Film.builder()
                .name("Перевозчик 1")
                .description("Триллер")
                .duration(92)
                .releaseDate(LocalDate.of(2002, 2, 1))
                .mpa(mpa1)
                .build();
        newFilm1.setGenres(Set.of(genre1));
        Film createdFilm1 = filmStorage.add(newFilm1);
        newFilm1.setId(createdFilm1.getId());

        Film newFilm2 = Film.builder()
                .name("Перевозчик 2")
                .description("Триллер")
                .duration(92)
                .releaseDate(LocalDate.of(2005, 2, 1))
                .mpa(mpa2)
                .build();
        newFilm2.setGenres(Set.of(genre2));
        Film createdFilm2 = filmStorage.add(newFilm2);
        newFilm2.setId(createdFilm2.getId());

        //act
        List<Film> actual = filmStorage.getFilms();

        //assert
        assertThat(actual)
                .asList()
                .contains(createdFilm1, createdFilm2);
    }

    @Test
    void addLike() {
        //arrange
        Mpa mpa1 = Mpa.builder().id(1).build();
        Genre actionMovie = Genre.builder().id(6).build();
        Genre thriller = Genre.builder().id(4).build();
        Film newFilm = Film.builder()
                .name("Перевозчик 3")
                .description("Триллер")
                .duration(92)
                .releaseDate(LocalDate.of(2008, 2, 1))
                .mpa(mpa1)
                .build();
        newFilm.setGenres(Set.of(actionMovie, thriller));

        Film createdFilm = filmStorage.add(newFilm);
        newFilm.setId(createdFilm.getId());

        User newUser = User.builder()
                .email("Luc.Paul.Maurice.Besson@ya.ru")
                .login("Besson@ya.ru")
                .name("Luc Paul Maurice Besson")
                .birthday(LocalDate.of(1958, 3, 18))
                .build();
        User createdUser = userStorage.add(newUser);

        //act
        filmStorage.addLike(createdFilm, createdUser);
        Optional<Film> actual = filmStorage.getFilmById(createdFilm.getId());

        //assert
        assertThat(actual)
                .isPresent()
                .hasValueSatisfying(film -> assertThat(new ArrayList<>(film.getLikes()))
                        .asList()
                        .hasSize(1)
                        .contains(createdUser.getId())
                );
    }

    @Test
    void removeLike() {
        //arrange
        Mpa mpa1 = Mpa.builder().id(1).build();
        Genre actionMovie = Genre.builder().id(6).build();
        Genre thriller = Genre.builder().id(4).build();
        Film newFilm = Film.builder()
                .name("Перевозчик 4")
                .description("Триллер")
                .duration(92)
                .releaseDate(LocalDate.of(2015, 2, 1))
                .mpa(mpa1)
                .build();
        newFilm.setGenres(Set.of(actionMovie, thriller));

        Film createdFilm = filmStorage.add(newFilm);
        newFilm.setId(createdFilm.getId());

        User newUser = User.builder()
                .email("Adam.Kuper@ya.ru")
                .login("AdamKuper@ya.ru")
                .name("Adam Kuper")
                .birthday(LocalDate.of(1958, 3, 18))
                .build();
        User createdUser = userStorage.add(newUser);

        //act
        filmStorage.addLike(createdFilm, createdUser);
        filmStorage.removeLike(createdFilm, createdUser);
        Optional<Film> actual = filmStorage.getFilmById(createdFilm.getId());

        //assert
        assertThat(actual)
                .isPresent()
                .hasValueSatisfying(film -> assertThat(new ArrayList<>(film.getLikes()))
                        .asList()
                        .hasSize(0)
                );
    }

    @Test
    void getPopular() {
        //arrange
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
        Film createdFilm1 = filmStorage.add(newFilm);

        newFilm = Film.builder()
                .name("Аватар 2")
                .description("5D")
                .duration(180)
                .releaseDate(LocalDate.of(2022, 12, 1))
                .mpa(mpa1)
                .build();
        newFilm.setGenres(Set.of(thriller));
        Film createdFilm2 = filmStorage.add(newFilm);

        newFilm = Film.builder()
                .name("Wednesday")
                .description("popular")
                .duration(180)
                .releaseDate(LocalDate.of(2022, 12, 1))
                .mpa(mpa1)
                .build();
        newFilm.setGenres(Set.of(thriller));
        Film createdFilm3 = filmStorage.add(newFilm);

        User user = User.builder()
                .email("guest1@ya.ru")
                .login("guest1@ya.ru")
                .name("guest1")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
        User createdUser1 = userStorage.add(user);

        user = User.builder()
                .email("guest2@ya.ru")
                .login("guest2@ya.ru")
                .name("guest2")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
        User createdUser2 = userStorage.add(user);

        user = User.builder()
                .email("guest3@ya.ru")
                .login("guest3@ya.ru")
                .name("guest3")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
        User createdUser3 = userStorage.add(user);

        filmStorage.addLike(createdFilm3, createdUser1);
        filmStorage.addLike(createdFilm3, createdUser2);
        filmStorage.addLike(createdFilm3, createdUser3);
        filmStorage.addLike(createdFilm2, createdUser1);
        filmStorage.addLike(createdFilm2, createdUser2);
        filmStorage.addLike(createdFilm1, createdUser1);

        //act
        List<Film> actual = filmStorage.getPopular(2);

        //assert
        assertThat(actual).asList()
                .hasSize(2)
                .contains(createdFilm3, createdFilm2);
    }
}