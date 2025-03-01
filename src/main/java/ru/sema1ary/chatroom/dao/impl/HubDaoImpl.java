package ru.sema1ary.chatroom.dao.impl;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;
import lombok.NonNull;
import ru.sema1ary.chatroom.dao.HubDao;
import ru.sema1ary.chatroom.model.Hub;

import java.sql.SQLException;
import java.util.Optional;

public class HubDaoImpl extends BaseDaoImpl<Hub, Long> implements HubDao {
    public HubDaoImpl(ConnectionSource connectionSource, Class<Hub> dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }

    @Override
    public Hub save(@NonNull Hub hub) throws SQLException {
        createOrUpdate(hub);
        return hub;
    }

    @Override
    public Optional<Hub> get() throws SQLException {
        Hub result = queryForId(1L);
        return Optional.ofNullable(result);
    }
}
