package ru.sema1ary.chatroom.listener;

import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.sema1ary.chatroom.service.RoomUserService;

@RequiredArgsConstructor
public class QuitListener implements Listener {
    private final RoomUserService userService;

    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
        userService.unregisterUser(event.getPlayer().getName());
    }
}
