package ru.sema1ary.chatroom.dao;

import com.j256.ormlite.dao.Dao;
import lombok.NonNull;
import ru.sema1ary.chatroom.model.Hub;

import java.sql.SQLException;
import java.util.Optional;

public interface HubDao extends Dao<Hub, Long> {
    Hub save(@NonNull Hub hub) throws SQLException;

    Optional<Hub> get() throws SQLException;
}
