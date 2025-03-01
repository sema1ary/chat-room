package ru.sema1ary.chatroom.dao.impl;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import lombok.NonNull;
import ru.sema1ary.chatroom.dao.RoomDao;
import ru.sema1ary.chatroom.model.Room;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

public class RoomDaoImpl extends BaseDaoImpl<Room, Long> implements RoomDao {
    public RoomDaoImpl(ConnectionSource connectionSource, Class<Room> dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }

    @Override
    public Room save(@NonNull Room room) throws SQLException {
        assignEmptyCollections(room);
        createOrUpdate(room);
        return room;
    }

    @Override
    public void saveAll(@NonNull List<Room> rooms) throws SQLException {
        callBatchTasks((Callable<Void>) () -> {
            for (Room room : rooms) {
                assignEmptyCollections(room);
                createOrUpdate(room);
            }
            return null;
        });
    }

    @Override
    public Optional<Room> findById(@NonNull Long id) throws SQLException {
        Room result = queryForId(id);
        return Optional.ofNullable(result);
    }

    @Override
    public Optional<Room> findByName(@NonNull String name) throws SQLException {
        QueryBuilder<Room, Long> queryBuilder = queryBuilder();
        Where<Room, Long> where = queryBuilder.where();
        String columnName = "name";

        SelectArg selectArg = new SelectArg(SqlType.STRING, name.toLowerCase());
        where.raw("LOWER(" + columnName + ")" + " = LOWER(?)", selectArg);
        return Optional.ofNullable(queryBuilder.queryForFirst());
    }

    @Override
    public List<Room> findAll() throws SQLException {
        return queryForAll();
    }

    private void assignEmptyCollections(Room room) throws SQLException {
        if (room.getUsers() == null) {
            room.setUsers(getEmptyForeignCollection("users"));
        }
    }
}
