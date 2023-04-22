package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.MpaDao;
import ru.yandex.practicum.filmorate.dto.Mpa;

import java.util.List;

@Component
public class MpaDbStorage implements MpaStorage {
    @Autowired
    @Qualifier("mpaDaoImplH2")
    private MpaDao mpaDao;

    @Override
    public List<Mpa> getAll() {
        return mpaDao.getAll();
    }

    @Override
    public Mpa getMpaById(Integer id) {
        return mpaDao.getMpaById(id);
    }
}
