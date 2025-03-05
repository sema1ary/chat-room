package ru.sema1ary.chatroom.service.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Sound;
import ru.sema1ary.chatroom.dao.RoomDao;
import ru.sema1ary.chatroom.model.Room;
import ru.sema1ary.chatroom.model.user.RoomUser;
import ru.sema1ary.chatroom.model.user.UserStatus;
import ru.sema1ary.chatroom.service.HubService;
import ru.sema1ary.chatroom.service.RoomService;
import ru.sema1ary.chatroom.service.RoomUserService;
import ru.sema1ary.chatroom.util.LocationUtil;

import java.sql.SQLException;
import java.util.*;

@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {
    private final RoomDao roomDao;
    private final HubService hubService;
    private final RoomUserService userService;

    @Override
    public void disable() {
        findAll().forEach(room -> {
            room.setAvailable(true);
            save(room);
        });
    }

    @Override
    public Room save(@NonNull Room room) {
        try {
            return roomDao.save(room);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveAll(@NonNull List<Room> rooms) {
        try {
            roomDao.saveAll(rooms);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Room> findById(@NonNull Long id) {
        try {
            return roomDao.findById(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Room> findByName(@NonNull String name) {
        try {
            return roomDao.findByName(name);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Room> findAll() {
        try {
            return roomDao.findAll();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Room room) {
        try {
            roomDao.delete(room);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Room> getAvailableRooms() {
        return findAll().stream().filter(Room::isAvailable).toList();
    }

    @Override
    public void findRoom(@NonNull RoomUser user) {
        switch (user.getStatus()) {
            case QUEUE -> {
                userService.sendMessage(user, "start-error-already-in-queue");
                return;
            }
            case BUSY -> {
                userService.sendMessage(user, "start-error-already-in-room");
                return;
            }
        }

        user.setStatus(UserStatus.QUEUE);
        userService.save(user);

        List<Room> availableRooms = getAvailableRooms();
        if(availableRooms.isEmpty()) {
            userService.sendMessage(user, "start-error-no-rooms-available");
            return;
        }

        userService.sendMessage(user, "start-successful-queued");
        userService.sendTitle(user, "start-queue-title-1", "start-queue-title-2", Sound.BLOCK_NOTE_BLOCK_BELL);

        RoomUser roommate = userService.findRoommate(user);
        if(roommate == null) {
            return;
        }

        userService.showPlayer(user, roommate);
        userService.showPlayer(roommate, user);

        startRoom(availableRooms.get(0), List.of(user, roommate));
    }

    @Override
    public void startRoom(@NonNull Room room, @NonNull List<RoomUser> users) {
        room.setAvailable(false);
        save(room);

        Location roomLocation = LocationUtil.stringToLocation(room.getLocation());

        users.forEach(user -> {
            user.setInRoom(room);
            user.setStatus(UserStatus.BUSY);

            userService.sendMessage(user, "room-successful-found");
            userService.teleportAsync(user, roomLocation);

            userService.save(user);
        });
    }

    @Override
    public void stopRoom(@NonNull Room room) {
        room.setAvailable(true);
        save(room);

        room.getUsers().forEach(user -> {
            user.setStatus(UserStatus.FREE);
            user.setInRoom(null);
            userService.save(user);

            hubService.teleportToHub(user);

            userService.hidePlayers(user);
            userService.sendMessage(user, "skip-successful");
            findRoom(user);
        });
    }

    @Override
    public void cancelRoom(@NonNull RoomUser user, @NonNull Room room) {
        room.setAvailable(true);
        save(room);

        room.getUsers().forEach(roomUser -> {
            roomUser.setStatus(UserStatus.FREE);
            roomUser.setInRoom(null);
            userService.save(roomUser);

            userService.hidePlayers(user);
            hubService.teleportToHub(roomUser);

            if(!user.getUsername().equals(roomUser.getUsername())) {
                userService.sendMessage(roomUser, "stop-roommate-message");
                findRoom(roomUser);
                return;
            }

            userService.sendMessage(roomUser, "stop-successful");
            userService.sendTitle(roomUser, "stop-queue-title-1", "stop-queue-title-2",
                    Sound.ENTITY_CAT_DEATH);
        });
    }
}
