package ru.sema1ary.chatroom.service;

import lombok.NonNull;
import org.bukkit.entity.Player;
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

    RoomUser registerUser(@NonNull String name);

    RoomUser getUserFromMap(@NonNull String name);

    void unregisterUser(@NonNull String name);

    List<RoomUser> getFreeUsers();

    List<RoomUser> getUsersInQueue();

    List<RoomUser> getBusyUsers();

    Player findRoommate(Player sender);
}
