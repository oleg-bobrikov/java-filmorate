package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class MpaStorageTest {
    private final MpaStorage mpaStorage;

    @Test
    void getAll_returnAllMpa() {
        //arrange
        Mpa mpa1 = mpaStorage.findMpaById(1);
        Mpa mpa2 = mpaStorage.findMpaById(2);
        Mpa mpa3 = mpaStorage.findMpaById(3);
        Mpa mpa4 = mpaStorage.findMpaById(4);
        Mpa mpa5 = mpaStorage.findMpaById(5);

        //act
        List<Mpa> actual = mpaStorage.findAll();

        //assert
        assertThat(actual).asList().contains(mpa1, mpa2, mpa3, mpa4, mpa5);

    }

    @Test
    void getMpaById_returnMpa_forTheFirstId() {
        //act
        Mpa actual = mpaStorage.findMpaById(1);

        //assert
        assertThat(actual)
                .hasFieldOrPropertyWithValue("id", 1)
                .hasFieldOrPropertyWithValue("name", "G");
    }

    @Test
    void getMpaById_returnMpa_forTheFifthId() {
        //act
        Mpa actual = mpaStorage.findMpaById(5);

        //assert
        assertThat(actual)
                .hasFieldOrPropertyWithValue("id", 5)
                .hasFieldOrPropertyWithValue("name", "NC-17");
    }
}