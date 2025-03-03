package ru.sema1ary.chatroom.service.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import ru.sema1ary.chatroom.ChatRoom;
import ru.sema1ary.chatroom.dao.HubDao;
import ru.sema1ary.chatroom.model.Hub;
import ru.sema1ary.chatroom.model.user.RoomUser;
import ru.sema1ary.chatroom.service.HubService;
import ru.sema1ary.chatroom.util.LocationUtil;
import ru.vidoskim.bukkit.service.MessagesService;

import java.sql.SQLException;
import java.util.Optional;

@RequiredArgsConstructor
public class HubServiceImpl implements HubService {
    private final HubDao hubDao;
    private final ChatRoom plugin;
    private final MessagesService messagesService;

    @Override
    public Hub save(@NonNull Hub hub) {
        try {
            return hubDao.save(hub);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Hub> get() {
        try {
            return hubDao.get();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setHub(Location location) {
        Hub hub;
        Optional<Hub> optionalHub = get();
        if(optionalHub.isEmpty()) {
            hub = Hub.builder()
                    .location(LocationUtil.locationToString(location))
                    .build();
        } else  {
            hub = optionalHub.get();
            hub.setLocation(LocationUtil.locationToString(location));
        }

        save(hub);
    }

    @Override
    public void teleportToHub(RoomUser user) {
        Player player = Bukkit.getPlayer(user.getUsername());

        if(player == null || !player.isOnline()) {
            return;
        }

        Optional<Hub> optionalHub = get();
        if(optionalHub.isEmpty()) {
            plugin.getLogger().severe(messagesService.getMessage("hub-dont-set"));
            return;
        }

        Hub hub = optionalHub.get();
        player.teleportAsync(LocationUtil.stringToLocation(hub.getLocation()));
    }
}
