package ru.sema1ary.chatroom.service;

import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import ru.sema1ary.chatroom.model.Room;
import service.Service;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("all")
public interface RoomService extends Service {
    Room save(@NonNull Room room);

    void saveAll(@NonNull List<Room> rooms);

    Optional<Room> findById(@NonNull Long id);

    Optional<Room> findByName(@NonNull String name);

    List<Room> findAll();

    Room createRoom(@NonNull String name, Location location);

    Room registerRoom(@NonNull Room room);

    Room getRoomFromMap(@NonNull String name);

    List<Room> getAvailableRooms();

    void unregisterRoom(@NonNull String name);

    boolean startRoom(@NonNull Player one, @NonNull Player two);

    void stopRoom(@NonNull Room room);
}
