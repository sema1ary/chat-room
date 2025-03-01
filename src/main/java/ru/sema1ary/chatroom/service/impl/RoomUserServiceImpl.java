package ru.sema1ary.chatroom.service.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.sema1ary.chatroom.dao.RoomUserDao;
import ru.sema1ary.chatroom.model.user.RoomUser;
import ru.sema1ary.chatroom.model.user.UserStatus;
import ru.sema1ary.chatroom.service.RoomUserService;

import java.sql.SQLException;
import java.util.*;

@RequiredArgsConstructor
public class RoomUserServiceImpl implements RoomUserService {
    private final RoomUserDao roomUserDao;

    private final Random random = new Random();
    private final Map<String, RoomUser> userMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    @Override
    public void disable() {
        userMap.forEach((string, user) -> {
            user.setStatus(UserStatus.FREE);
            user.setInRoom(null);
            save(user);
        });
    }

    @Override
    public RoomUser save(@NonNull RoomUser user) {
        try {
            return roomUserDao.save(user);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveAll(@NonNull List<RoomUser> users) {
        try {
            roomUserDao.saveAll(users);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<RoomUser> findById(@NonNull Long id) {
        try {
            return roomUserDao.findById(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<RoomUser> findByUsername(@NonNull String username) {
        try {
            return roomUserDao.findByUsername(username);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<RoomUser> findAll() {
        try {
            return roomUserDao.findAll();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public RoomUser getUser(@NonNull String name) {
        return findByUsername(name).orElseGet(() -> save(RoomUser.builder()
                .username(name)
                .status(UserStatus.FREE)
                .build()));
    }

    @Override
    public RoomUser registerUser(@NonNull String name) {
        return userMap.put(name, getUser(name));
    }

    @Override
    public RoomUser getUserFromMap(@NonNull String name) {
        return userMap.get(name);
    }

    @Override
    public void unregisterUser(@NonNull String name) {
        userMap.remove(name);
    }

    @Override
    public List<RoomUser> getFreeUsers() {
        return findAll().stream().filter(roomUser -> roomUser.getStatus().equals(UserStatus.FREE)).toList();
    }

    @Override
    public List<RoomUser> getUsersInQueue() {
        return findAll().stream().filter(roomUser -> roomUser.getStatus().equals(UserStatus.QUEUE)).toList();
    }

    @Override
    public List<RoomUser> getBusyUsers() {
        return findAll().stream().filter(roomUser -> roomUser.getStatus().equals(UserStatus.BUSY)).toList();
    }

    @Override
    public Player findRoommate(Player sender) {
        List<RoomUser> availableRoommates = new ArrayList<>();

        getUsersInQueue().forEach(roomUser -> {
            if(!roomUser.getUsername().equals(sender.getName())) {
                availableRoommates.add(roomUser);
            }
        });

        if(availableRoommates.isEmpty()) {
            return null;
        }

        RoomUser roommateUser = availableRoommates.get(random.nextInt(availableRoommates.size()));
        return Bukkit.getPlayer(roommateUser.getUsername());
    }
}
