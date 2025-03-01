package ru.sema1ary.chatroom.dao;

import com.j256.ormlite.dao.Dao;
import lombok.NonNull;
import ru.sema1ary.chatroom.model.Room;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface RoomDao extends Dao<Room, Long> {
    Room save(@NonNull Room room) throws SQLException;

    void saveAll(@NonNull List<Room> rooms) throws SQLException;

    Optional<Room> findById(@NonNull Long id) throws SQLException;

    Optional<Room> findByName(@NonNull String name) throws SQLException;

    List<Room> findAll() throws SQLException;
}
