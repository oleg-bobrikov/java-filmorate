package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.dto.Film;
import ru.yandex.practicum.filmorate.dto.Genre;
import ru.yandex.practicum.filmorate.dto.Mpa;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class GenreStorageTest {
    @Autowired
    private GenreStorage genreStorage;
    @Autowired
    @Qualifier("filmH2Storage")
    private FilmStorage filmStorage;

    @Test
    void getAll_returnAllGenres() {
        //arrange
        Genre genre1 = genreStorage.getGenreById(1);
        Genre genre2 = genreStorage.getGenreById(2);
        Genre genre3 = genreStorage.getGenreById(3);
        Genre genre4 = genreStorage.getGenreById(4);
        Genre genre5 = genreStorage.getGenreById(5);
        Genre genre6 = genreStorage.getGenreById(6);

        //act
        List<Genre> actual = genreStorage.getAll();

        //assert
        assertThat(actual).asList().contains(genre1, genre2, genre3, genre4, genre5, genre6);
    }

    @Test
    void updateFilmGenres_assignNewGenres() {
        //arrange
        Mpa mpa1 = Mpa.builder().id(1).build();
        Genre thriller = genreStorage.getGenreById(4);
        Genre drama = genreStorage.getGenreById(1);
        Film newFilm = Film.builder()
                .name("Бойцовский клуб")
                .description("режиссер Дэвид Финчер")
                .duration(180)
                .releaseDate(LocalDate.of(1999, 12, 1))
                .mpa(mpa1)
                .build();
        newFilm.setGenres(Set.of(thriller));
        Film createdFilm1 = filmStorage.add(newFilm);


        //act
        genreStorage.updateFilmGenres(createdFilm1, Set.of(thriller, drama));
        Optional<Film> actual = filmStorage.getFilmById(createdFilm1.getId());


        //assert
        assertThat(actual)
                .isPresent()
                .hasValueSatisfying(film -> assertThat(new ArrayList<>(film.getGenres()))
                        .asList()
                        .hasSize(2)
                        .contains(thriller, drama));
    }

    @Test
    void getMpaById_returnGenre_forTheFirstId() {

        //act
        Genre actual = genreStorage.getGenreById(1);

        //assert
        assertThat(actual)
                .hasFieldOrPropertyWithValue("id", 1)
                .hasFieldOrPropertyWithValue("name", "Комедия");
    }

    @Test
    void getMpaById_returnGenre_forTheSixthId() {

        //act
        Genre actual = genreStorage.getGenreById(6);

        //assert
        assertThat(actual)
                .hasFieldOrPropertyWithValue("id", 6)
                .hasFieldOrPropertyWithValue("name", "Боевик");
    }
}
