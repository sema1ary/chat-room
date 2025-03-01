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
    private final Map<String, Room> roomMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    @Override
    public void enable() {
        findAll().forEach(this::registerRoom);
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
    public Room createRoom(@NonNull String name, Location location) {
        return registerRoom(
                save(Room.builder()
                        .name(name)
                        .location(LocationUtil.locationToString(location))
                        .isAvailable(true)
                        .build())
        );
    }

    @Override
    public Room registerRoom(@NonNull Room room) {
        return roomMap.put(room.getName(), room);
    }

    @Override
    public Room getRoomFromMap(@NonNull String name) {
        return roomMap.get(name);
    }

    @Override
    public List<Room> getAvailableRooms() {
        List<Room> availableRooms = new ArrayList<>();
        roomMap.forEach((string, room) -> {
            if(getRoomFromMap(string).isAvailable()) {
                availableRooms.add(room);
            }
        });

        return availableRooms;
    }

    @Override
    public void unregisterRoom(@NonNull String name) {
        roomMap.remove(name);
    }

    @Override
    public boolean startRoom(@NonNull Player one, @NonNull Player two) {
        List<Room> availableRooms = getAvailableRooms();
        if(availableRooms.isEmpty()) {
            return false;
        }

        Room room = availableRooms.get(0);
        Location roomLocation = LocationUtil.stringToLocation(room.getLocation());
        room.setAvailable(false);
        save(room);

        one.teleportAsync(roomLocation);
        two.teleportAsync(roomLocation);

        RoomUser oneUser = userService.getUserFromMap(one.getName());
        RoomUser twoUser = userService.getUserFromMap(two.getName());

        oneUser.setInRoom(room);
        twoUser.setInRoom(room);
        oneUser.setStatus(UserStatus.BUSY);
        twoUser.setStatus(UserStatus.BUSY);

        userService.saveAll(List.of(oneUser, twoUser));
        return true;
    }

    @Override
    public void stopRoom(@NonNull Room room) {
        room.getUsers().forEach(user -> {
            user.setInRoom(null);
            user.setStatus(UserStatus.QUEUE);
            userService.save(user);

            Player player = Bukkit.getPlayer(user.getUsername());

            if(player == null || !player.isOnline()) {
                return;
            }

            player.sendMessage(miniMessage.deserialize(messagesService.getMessage("stop-users-message")));
            hubService.teleportToHub(Bukkit.getPlayer(user.getUsername()));
        });

        room.setAvailable(true);
        save(room);
    }
}
