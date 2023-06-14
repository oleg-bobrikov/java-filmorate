package ru.yandex.practicum.filmorate.trigger;

import org.h2.api.Trigger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AddReviewLikeTrigger implements Trigger {
    @Override
    public void init(Connection connection, String s, String s1, String s2, boolean b, int i) throws SQLException {
        Trigger.super.init(connection, s, s1, s2, b, i);
    }

    @Override
    public void fire(Connection connection, Object[] oldRow, Object[] newRow) throws SQLException {
        String sql = "insert into EVENTS (EVENT_TIMESTAMP, EVENT_TYPE, ENTITY_ID, USER_ID, OPERATION) " +
                "VALUES(CURRENT_TIMESTAMP, ?, ?, ?, ?)";

        PreparedStatement prep = connection.prepareStatement(sql);
        prep.setString(1, "LIKE"); // EVENT_TYPE
        prep.setInt(2, (Integer) newRow[0]); // ENTITY_ID
        prep.setInt(3, (Integer) newRow[1]); // USER_ID
        prep.setString(4, "ADD"); // OPERATION

        prep.execute();
    }

    @Override
    public void close() throws SQLException {
        Trigger.super.close();
    }

    @Override
    public void remove() throws SQLException {
        Trigger.super.remove();
    }
}
