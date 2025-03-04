package ru.sema1ary.chatroom.service;

import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Sound;
import ru.sema1ary.chatroom.model.user.RoomUser;
import service.Service;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("all")
public interface RoomUserService extends Service {
    RoomUser save(@NonNull RoomUser user);

    void saveAll(@NonNull List<RoomUser> users);

    Optional<RoomUser> findById(@NonNull Long id);

    Optional<RoomUser> findByUsername(@NonNull String username);

    List<RoomUser> findAll();

    RoomUser getUser(@NonNull String name);

    List<RoomUser> getFreeUsers();

    List<RoomUser> getUsersInQueue();

    List<RoomUser> getBusyUsers();

    boolean isInRoom(RoomUser user);

    RoomUser findRoommate(RoomUser sender);

    RoomUser getRoommate(RoomUser user);

    void teleportAsync(RoomUser user, Location location);

    void sendMessage(RoomUser user, String index);

    void sendTitle(RoomUser user, String title, String subtitle, Sound sound);
}
