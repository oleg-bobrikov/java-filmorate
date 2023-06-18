package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.dto.*;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class FilmStorageTest {

    private final DirectorStorage directorStorage;
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final Film film1;
    private final Film film2;
    private final Film film3;
    private final Film film4;
    private final Film film5;
    private final User user1;
    private final User user2;
    private final User user3;
    private final User user4;
    private final User user5;
    private final Director director1;
    private final Director director2;
    private final Director director3;
    private final Director director4;

    @Autowired
    public FilmStorageTest(@Qualifier("filmH2Storage") FilmStorage filmStorage,
                           @Qualifier("userH2Storage") UserStorage userStorage,
                           @Qualifier("directorH2Storage") DirectorStorage directorStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.directorStorage = directorStorage;
        this.film1 = createFilm1();
        this.film2 = createFilm2();
        this.film3 = createFilm3();
        this.film4 = createFilm4();
        this.film5 = createFilm5();
        this.user1 = getCreatedUser1();
        this.user2 = getCreatedUser2();
        this.user3 = getCreatedUser3();
        this.user4 = getCreatedUser4();
        this.user5 = getCreatedUser5();
        director1 = new Director(1, "Pavel");
        director2 = new Director(2, "Roman");
        director3 = new Director(1, "Kate");
        director4 = new Director(1, "Potter");

        directorStorage.createDirector(director1);
        directorStorage.createDirector(director2);
        directorStorage.createDirector(director3);
        directorStorage.createDirector(director4);
    }

    private Film createFilm1() {
        Mpa mpa1 = Mpa.builder().id(1).build();
        Genre thriller = Genre.builder().id(4).build();
        Film newFilm = Film.builder()
                .name("Аватар")
                .description("3D")
                .duration(180)
                .releaseDate(LocalDate.of(2009, 12, 1))
                .mpa(mpa1).build();
        newFilm.setGenres(Set.of(thriller));
        return filmStorage.add(newFilm);
    }

    private User getCreatedUser1() {
        User newUser = User.builder()
                .email("guest1@ya.ru")
                .login("guest1@ya.ru")
                .name("guest1")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
        return userStorage.add(newUser);
    }

    private User getCreatedUser2() {
        User newUser = User.builder()
                .email("guest2@ya.ru")
                .login("guest2@ya.ru")
                .name("guest2")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
        return userStorage.add(newUser);
    }

    private User getCreatedUser3() {
        User newUser = User.builder()
                .email("guest3@ya.ru")
                .login("guest3@ya.ru")
                .name("guest3")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
        return userStorage.add(newUser);
    }

    private User getCreatedUser4() {
        User newUser = User.builder()
                .email("guest4@ya.ru")
                .login("guest4@ya.ru")
                .name("guest4")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
        return userStorage.add(newUser);
    }

    private User getCreatedUser5() {
        User newUser = User.builder()
                .email("guest5@ya.ru")
                .login("guest5@ya.ru")
                .name("guest5")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
        return userStorage.add(newUser);
    }

    private Film createFilm2() {
        Mpa mpa1 = Mpa.builder().id(1).build();
        Genre thriller = Genre.builder().id(4).build();
        Film newFilm = Film.builder()
                .name("Аватар 2")
                .description("5D")
                .duration(180)
                .releaseDate(LocalDate.of(2022, 12, 1))
                .mpa(mpa1).build();
        newFilm.setGenres(Set.of(thriller));
        return filmStorage.add(newFilm);
    }

    private Film createFilm3() {
        Mpa mpa1 = Mpa.builder().id(1).build();
        Genre thriller = Genre.builder().id(4).build();
        Film newFilm = Film.builder()
                .name("Wednesday")
                .description("popular")
                .duration(180)
                .releaseDate(LocalDate.of(2022, 12, 1))
                .mpa(mpa1).build();
        newFilm.setGenres(Set.of(thriller));
        return filmStorage.add(newFilm);
    }

    private Film createFilm4() {
        Mpa mpa1 = Mpa.builder().id(1).build();
        Genre thriller = Genre.builder().id(4).build();
        Film newFilm = Film.builder()
                .name("Harry Potter")
                .description("popular")
                .duration(180)
                .releaseDate(LocalDate.of(2022, 12, 1))
                .mpa(mpa1).build();
        newFilm.setGenres(Set.of(thriller));
        return filmStorage.add(newFilm);
    }

    private Film createFilm5() {
        Mpa mpa1 = Mpa.builder().id(1).build();
        Genre thriller = Genre.builder().id(4).build();
        Film newFilm = Film.builder()
                .name("Крепкий Орешик")
                .description("popular")
                .duration(180)
                .releaseDate(LocalDate.of(2022, 12, 1))
                .mpa(mpa1).build();
        newFilm.setGenres(Set.of(thriller));
        return filmStorage.add(newFilm);
    }


    @Test
    public void update_returnUpdatedFilm() {
        //arrange
        Mpa mpa1 = Mpa.builder().id(1).build();
        Mpa mpa2 = Mpa.builder().id(2).build();
        Genre genre1 = Genre.builder().id(1).build();
        Genre genre2 = Genre.builder().id(2).build();
        Film testFilm = Film.builder().name("Папаши")
                .description("Франзцузская комедия")
                .duration(92)
                .releaseDate(LocalDate.of(1983, 2, 1)).mpa(mpa1).build();
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
        assertThat(actual).hasFieldOrPropertyWithValue("id", testFilm.getId())
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
        Film newFilm = Film.builder().name("Перевозчик")
                .description("Триллер")
                .duration(92)
                .releaseDate(LocalDate.of(2002, 2, 1))
                .mpa(mpa1)
                .build();
        newFilm.setGenres(Set.of(genre1));
        Film createdFilm = filmStorage.add(newFilm);
        final int id = createdFilm.getId();

        //act
        filmStorage.removeFilmById(id);
        Optional<Film> actual = filmStorage.findFilmById(id);

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
        Film newFilm1 = Film.builder().name("Перевозчик 1")
                .description("Триллер")
                .duration(92)
                .releaseDate(LocalDate.of(2002, 2, 1))
                .mpa(mpa1)
                .build();
        newFilm1.setGenres(Set.of(genre1));
        Film createdFilm1 = filmStorage.add(newFilm1);
        newFilm1.setId(createdFilm1.getId());

        Film newFilm2 = Film.builder().name("Перевозчик 2")
                .description("Триллер")
                .duration(92)
                .releaseDate(LocalDate.of(2005, 2, 1))
                .mpa(mpa2).build();
        newFilm2.setGenres(Set.of(genre2));
        Film createdFilm2 = filmStorage.add(newFilm2);
        newFilm2.setId(createdFilm2.getId());

        //act
        List<Film> actual = filmStorage.getFilms();

        //assert
        assertThat(actual).asList().contains(createdFilm1, createdFilm2);
    }

    @Test
    void addLike() {
        //arrange
        Mpa mpa1 = Mpa.builder().id(1).build();
        Genre actionMovie = Genre.builder().id(6).build();
        Genre thriller = Genre.builder().id(4).build();
        Film newFilm = Film.builder().name("Перевозчик 3").description("Триллер").duration(92).releaseDate(LocalDate.of(2008, 2, 1)).mpa(mpa1).build();
        newFilm.setGenres(Set.of(actionMovie, thriller));

        Film createdFilm = filmStorage.add(newFilm);
        newFilm.setId(createdFilm.getId());

        User newUser = User.builder().email("Luc.Paul.Maurice.Besson@ya.ru").login("Besson@ya.ru").name("Luc Paul Maurice Besson").birthday(LocalDate.of(1958, 3, 18)).build();
        User createdUser = userStorage.add(newUser);

        //act
        filmStorage.addLike(createdFilm, createdUser);
        Optional<Film> actual = filmStorage.findFilmById(createdFilm.getId());

        //assert
        assertThat(actual).isPresent().hasValueSatisfying(film -> assertThat(new ArrayList<>(film.getLikes())).asList().hasSize(1).contains(createdUser.getId()));
    }

    @Test
    void removeLike() {
        //arrange
        Mpa mpa1 = Mpa.builder().id(1).build();
        Genre actionMovie = Genre.builder().id(6).build();
        Genre thriller = Genre.builder().id(4).build();
        Film newFilm = Film.builder().name("Перевозчик 4").description("Триллер").duration(92).releaseDate(LocalDate.of(2015, 2, 1)).mpa(mpa1).build();
        newFilm.setGenres(Set.of(actionMovie, thriller));

        Film createdFilm = filmStorage.add(newFilm);
        newFilm.setId(createdFilm.getId());

        User newUser = User.builder().email("Adam.Kuper@ya.ru").login("AdamKuper@ya.ru").name("Adam Kuper").birthday(LocalDate.of(1958, 3, 18)).build();
        User createdUser = userStorage.add(newUser);

        //act
        filmStorage.addLike(createdFilm, createdUser);
        filmStorage.removeLike(createdFilm, createdUser);
        Optional<Film> actual = filmStorage.findFilmById(createdFilm.getId());

        //assert
        assertThat(actual).isPresent().hasValueSatisfying(film -> assertThat(new ArrayList<>(film.getLikes())).asList().hasSize(0));
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
        assertThat(actual).asList().hasSize(2).contains(film3, film2);
    }

    @Test
    void getRecommendedFilms_returnRecommendedListOfFilms() {
        // arrange
        // act
        List<Film> actual = filmStorage.getRecommendations(user1.getId());
        // assert
        assertThat(actual).asList().isEmpty();

        // arrange
        filmStorage.addLike(film3, user1);
        filmStorage.addLike(film3, user2);
        filmStorage.addLike(film2, user2);

        // act
        actual = filmStorage.getRecommendations(user1.getId());
        assertThat(actual).asList()
                .hasSize(1)
                .satisfies(list -> assertThat(list.get(0)).isEqualTo(film2));
    }

    @Test
    void getCommonFilms_returnCommonFilmsSortedByPopularity() {
        //arrange

        userStorage.addFriend(user1, user2);

        filmStorage.addLike(film1, user1);
        filmStorage.addLike(film1, user2);
        filmStorage.addLike(film1, user3);
        final Film updatedFilm1 = filmStorage.findFilmById(film1.getId()).get();

        filmStorage.addLike(film2, user1);
        filmStorage.addLike(film2, user2);
        filmStorage.addLike(film2, user3);
        filmStorage.addLike(film2, user4);
        final Film updatedFilm2 = filmStorage.findFilmById(film2.getId()).get();

        filmStorage.addLike(film3, user1);
        filmStorage.addLike(film3, user2);
        filmStorage.addLike(film3, user3);
        filmStorage.addLike(film3, user4);
        filmStorage.addLike(film3, user5);
        final Film updatedFilm3 = filmStorage.findFilmById(film3.getId()).get();

        filmStorage.addLike(film4, user1);
        filmStorage.addLike(film5, user2);

        //act
        List<Film> films = filmStorage.getCommonFilms(1, 2);

        //assert
        assertThat(films).asList().hasSize(3)
                .contains(film3, film2, film1)
                .satisfies(list -> {
                    assertThat(list.get(0)).isEqualTo(updatedFilm3);
                    assertThat(list.get(1)).isEqualTo(updatedFilm2);
                    assertThat(list.get(2)).isEqualTo(updatedFilm1);
                });
    }

    @Test
    void searchFilms_returnSortedListFilmsByTitle() {
        //arrange
        filmStorage.addLike(film1, user1);
        filmStorage.addLike(film1, user2);
        filmStorage.addLike(film2, user3);

        //expected
        List<Film> exp = filmStorage.searchFilmsByTitle("ав");

        //assert
        Assertions.assertEquals(film1, exp.get(0));
        Assertions.assertEquals(film2, exp.get(1));
    }

    @Test
    public void searchFilms_returnSortedListFilmsByDirector() {
        //arrange
        filmStorage.addLike(film3, user1);
        filmStorage.addLike(film3, user2);
        filmStorage.addLike(film3, user3);
        filmStorage.addLike(film3, user4);
        filmStorage.addLike(film3, user5);
        filmStorage.addLike(film2, user1);
        filmStorage.addLike(film2, user2);
        filmStorage.addLike(film1, user3);
        film2.setDirectors(new HashSet<>(List.of(director1)));
        film1.setDirectors(new HashSet<>(List.of(director1)));
        directorStorage.updateFilmDirector(film1, new HashSet<>(List.of(director1)));
        directorStorage.updateFilmDirector(film2, new HashSet<>(List.of(director1)));

        //expected
        List<Film> exp = filmStorage.searchFilmsByDirector("pav");

        //assert
        Assertions.assertEquals(film2, exp.get(1));
        Assertions.assertEquals(film1, exp.get(0));
    }
}