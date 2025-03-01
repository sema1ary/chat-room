package ru.sema1ary.chatroom.dao.impl;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import lombok.NonNull;
import ru.sema1ary.chatroom.dao.RoomUserDao;
import ru.sema1ary.chatroom.model.user.RoomUser;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

public class RoomUserDaoImpl extends BaseDaoImpl<RoomUser, Long> implements RoomUserDao {
    public RoomUserDaoImpl(ConnectionSource connectionSource, Class<RoomUser> dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }

    @Override
    public RoomUser save(@NonNull RoomUser user) throws SQLException {
        createOrUpdate(user);
        return user;
    }

    @Override
    public void saveAll(@NonNull List<RoomUser> users) throws SQLException {
        callBatchTasks((Callable<Void>) () -> {
            for (RoomUser user : users) {
                createOrUpdate(user);
            }
            return null;
        });
    }

    @Override
    public Optional<RoomUser> findById(@NonNull Long id) throws SQLException {
        RoomUser result = queryForId(id);
        return Optional.ofNullable(result);
    }

    @Override
    public Optional<RoomUser> findByUsername(@NonNull String username) throws SQLException {
        QueryBuilder<RoomUser, Long> queryBuilder = queryBuilder();
        Where<RoomUser, Long> where = queryBuilder.where();
        String columnName = "username";

        SelectArg selectArg = new SelectArg(SqlType.STRING, username.toLowerCase());
        where.raw("LOWER(" + columnName + ")" + " = LOWER(?)", selectArg);
        return Optional.ofNullable(queryBuilder.queryForFirst());
    }

    @Override
    public List<RoomUser> findAll() throws SQLException {
        return queryForAll();
    }
}
