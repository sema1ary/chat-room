package ru.sema1ary.chatroom.dao;

import com.j256.ormlite.dao.Dao;
import lombok.NonNull;
import ru.sema1ary.chatroom.model.user.RoomUser;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface RoomUserDao extends Dao<RoomUser, Long> {
    RoomUser save(@NonNull RoomUser user) throws SQLException;

    void saveAll(@NonNull List<RoomUser> users) throws SQLException;

    Optional<RoomUser> findById(@NonNull Long id) throws SQLException;

    Optional<RoomUser> findByUsername(@NonNull String username) throws SQLException;

    List<RoomUser> findAll() throws SQLException;
}
