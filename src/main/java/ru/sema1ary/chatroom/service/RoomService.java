package ru.sema1ary.chatroom.service;

import lombok.NonNull;
import ru.sema1ary.chatroom.model.Room;
import ru.sema1ary.chatroom.model.user.RoomUser;
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

    void delete(Room room);

    List<Room> getAvailableRooms();

    void findRoom(@NonNull RoomUser user);

    void startRoom(@NonNull Room room, @NonNull List<RoomUser> users);

    void stopRoom(@NonNull Room room);

    void cancelRoom(@NonNull RoomUser user, @NonNull Room room);
}
