package ru.sema1ary.chatroom.service;

import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import ru.sema1ary.chatroom.model.Hub;
import service.Service;

import java.util.Optional;

public interface HubService extends Service {
    Hub save(@NonNull Hub hub);

    Optional<Hub> get();

    void setHub(Location location);

    void teleportToHub(Player player);
}
