package ru.sema1ary.chatroom.service.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import ru.sema1ary.chatroom.ChatRoom;
import ru.sema1ary.chatroom.dao.RoomUserDao;
import ru.sema1ary.chatroom.model.Room;
import ru.sema1ary.chatroom.model.user.RoomUser;
import ru.sema1ary.chatroom.model.user.UserStatus;
import ru.sema1ary.chatroom.service.RoomUserService;
import ru.vidoskim.bukkit.service.ConfigurationService;
import ru.vidoskim.bukkit.service.MessagesService;

import java.sql.SQLException;
import java.util.*;

@RequiredArgsConstructor
public class RoomUserServiceImpl implements RoomUserService {
    private final ChatRoom plugin;
    private final RoomUserDao roomUserDao;
    private final MiniMessage miniMessage;
    private final MessagesService messagesService;
    private final ConfigurationService configurationService;

    private final Random random = new Random();

    @Override
    public void disable() {
        getUsersInQueue().forEach(user -> {
            user.setStatus(UserStatus.FREE);
            save(user);
        });

        getBusyUsers().forEach(user -> {
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
    public boolean isInRoom(RoomUser user) {
        return user.getInRoom() != null;
    }

    @Override
    public RoomUser findRoommate(RoomUser user) {
        List<RoomUser> availableRoommates = new ArrayList<>();

        getUsersInQueue().forEach(roomUser -> {
            if(!roomUser.getUsername().equals(user.getUsername())) {
                availableRoommates.add(roomUser);
            }
        });

        if(availableRoommates.isEmpty()) {
            return null;
        }

        return availableRoommates.get(random.nextInt(availableRoommates.size()));
    }

    @Override
    public RoomUser getRoommate(RoomUser user) {
        if(!isInRoom(user)) {
            return null;
        }

        Room room = user.getInRoom();
        Optional<RoomUser> optionalUser = room.getUsers().stream().filter(roomUser -> !roomUser.getUsername()
                .equalsIgnoreCase(user.getUsername())).findFirst();

        return optionalUser.orElse(null);

    }

    @Override
    public void teleportAsync(RoomUser user, Location location) {
        Player player = Bukkit.getPlayer(user.getUsername());
        if(player != null && player.isOnline()) {
            player.teleportAsync(location);
        }
    }

    @Override
    public void sendMessage(RoomUser user, String index) {
        Player player = Bukkit.getPlayer(user.getUsername());

        if(player == null || !player.isOnline()) {
            return;
        }

        player.sendMessage(miniMessage.deserialize(PlaceholderAPI.setPlaceholders(player, messagesService.getMessage(index))));
    }

    @Override
    public void sendTitle(RoomUser user, String titleIndex, String subtitleIndex, Sound sound) {
        Player player = Bukkit.getPlayer(user.getUsername());

        if(player == null || !player.isOnline()) {
            return;
        }

        if(configurationService.get("enable-titles")) {
            player.showTitle(Title.title(miniMessage.deserialize(configurationService.get(titleIndex)),
                    miniMessage.deserialize(configurationService.get(subtitleIndex))));
        }

        if(configurationService.get("enable-titles-sound")) {
            player.playSound(player.getLocation(), sound, 1L, 0L);
        }
    }

    @Override
    public void hidePlayers(RoomUser user) {
        Player player = Bukkit.getPlayer(user.getUsername());

        if(player == null || !player.isOnline()) {
            return;
        }

        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> player.hidePlayer(plugin, onlinePlayer));
    }

    @Override
    public void showPlayer(RoomUser user, RoomUser roommate) {
        Player player = Bukkit.getPlayer(user.getUsername());
        Player roommatePlayer = Bukkit.getPlayer(roommate.getUsername());

        if(player == null || !player.isOnline() || roommatePlayer == null || !roommatePlayer.isOnline()) {
            return;
        }

        player.showPlayer(plugin, roommatePlayer);
    }
}
