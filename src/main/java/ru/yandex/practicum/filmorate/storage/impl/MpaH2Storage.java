package ru.yandex.practicum.filmorate.storage.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class MpaH2Storage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Mpa> getAll() {
        String sql = "select * from MPA_FILM_RATINGS";
        HashMap<Integer, Mpa> results = new HashMap<>();
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql);
        while (rs.next()) {
            Mpa mpa = Mpa.builder()
                    .id(rs.getInt("id"))
                    .name(rs.getString("MPA_FILM_RATING_name"))
                    .build();
            results.put(mpa.getId(), mpa);
        }
        return new ArrayList<>(results.values());
    }


    @Override
    public Mpa getMpaById(Integer id) {
        String sql = "select * from MPA_FILM_RATINGS where id = ?";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql, id);
        if (rs.next()) {
            Mpa mpa = Mpa.builder()
                    .id(id)
                    .name(rs.getString("MPA_FILM_RATING_name"))
                    .build();

            log.info("Найден рейтинг фильма c идентификатором: {}", id);

            return mpa;
        } else {
            log.info("Рейтинг фильма с идентификатором {} не найден.", id);
            return null;
        }
    }
}
