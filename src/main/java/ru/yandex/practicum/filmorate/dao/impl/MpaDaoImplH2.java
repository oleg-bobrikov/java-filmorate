package ru.yandex.practicum.filmorate.dao.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.MpaDao;
import ru.yandex.practicum.filmorate.dto.Mpa;

import java.util.*;

@Component("mpaDaoImplH2")
@Slf4j
public class MpaDaoImplH2 implements MpaDao {
    @Autowired
    private final JdbcTemplate jdbcTemplate;

    public MpaDaoImplH2(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Mpa> getAll() {
        String sql = "select * from MPA_FILM_RATINGS";
        HashMap<Integer, Mpa> results = new HashMap<>();
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql);
        while (rs.next()) {
            Mpa mpa = Mpa.builder()
                    .id(rs.getInt("id"))
                    .name(rs.getString("name"))
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
                    .name(rs.getString("name"))
                    .build();

            log.info("Найден рейтинг фильма c идентификатором: {}", id);

            return mpa;
        } else {
            log.info("Рейтинг фильма с идентификатором {} не найден.", id);
            return null;
        }
    }
}
