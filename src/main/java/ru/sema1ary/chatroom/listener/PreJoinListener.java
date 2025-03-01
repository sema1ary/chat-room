package ru.sema1ary.chatroom.listener;

import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import ru.sema1ary.chatroom.model.user.RoomUser;
import ru.sema1ary.chatroom.model.user.UserStatus;
import ru.sema1ary.chatroom.service.RoomUserService;

@RequiredArgsConstructor
public class PreJoinListener implements Listener {
    private final RoomUserService userService;

    @EventHandler
    private void onJoin(AsyncPlayerPreLoginEvent event) {
        String username = event.getName();

        if(username.isEmpty()) {
            return;
        }

        if(userService.findByUsername(username).isEmpty()) {
            userService.save(RoomUser.builder()
                    .username(username)
                    .status(UserStatus.FREE)
                    .build());
        }
    }
}
