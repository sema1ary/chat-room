package ru.sema1ary.chatroom.listener;

import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import ru.sema1ary.chatroom.service.RoomUserService;

@RequiredArgsConstructor
public class JoinListener implements Listener {
    private final RoomUserService userService;

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        userService.registerUser(event.getPlayer().getName());
    }
}
