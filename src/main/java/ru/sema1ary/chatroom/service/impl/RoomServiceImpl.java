package ru.sema1ary.chatroom.service.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import ru.sema1ary.chatroom.dao.RoomDao;
import ru.sema1ary.chatroom.model.Room;
import ru.sema1ary.chatroom.model.user.RoomUser;
import ru.sema1ary.chatroom.model.user.UserStatus;
import ru.sema1ary.chatroom.service.HubService;
import ru.sema1ary.chatroom.service.RoomService;
import ru.sema1ary.chatroom.service.RoomUserService;
import ru.sema1ary.chatroom.util.LocationUtil;
import ru.vidoskim.bukkit.service.MessagesService;

import java.sql.SQLException;
import java.util.*;

@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {
    private final RoomDao roomDao;
    private final MiniMessage miniMessage;
    private final HubService hubService;
    private final RoomUserService userService;
    private final MessagesService messagesService;

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
                sendMessage(user, "start-error-already-in-queue");
                return;
            }
            case BUSY -> {
                sendMessage(user, "start-error-already-in-room");
                return;
            }
        }

        user.setStatus(UserStatus.QUEUE);
        userService.save(user);

        List<Room> availableRooms = getAvailableRooms();
        if(availableRooms.isEmpty()) {
            sendMessage(user, "start-error-no-rooms-available");
            return;
        }

        RoomUser roommate = userService.findRoommate(user);
        if(roommate == null) {
            return;
        }

        sendMessage(user, "start-successful-queued");
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

            sendMessage(user, "room-successful-found");
            teleportAsync(user, roomLocation);

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

            sendMessage(user, "skip-successful");
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

            hubService.teleportToHub(roomUser);

            if(!user.getUsername().equals(roomUser.getUsername())) {
                sendMessage(roomUser, "stop-roommate-message");
                findRoom(roomUser);
                return;
            }

            sendMessage(roomUser, "stop-successful");
        });
    }

    private void teleportAsync(RoomUser user, Location location) {
        Player player = Bukkit.getPlayer(user.getUsername());
        if(player != null && player.isOnline()) {
            player.teleportAsync(location);
        }
    }

    private void sendMessage(RoomUser user, String index) {
        Player player = Bukkit.getPlayer(user.getUsername());

        if(player == null || !player.isOnline()) {
            return;
        }

        player.sendMessage(miniMessage.deserialize(messagesService.getMessage(index)));
    }
}
