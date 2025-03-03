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
    private void onPreJoin(AsyncPlayerPreLoginEvent event) {
        String name = event.getName();

        if(name.isEmpty()) {
            return;
        }

        if(userService.findByUsername(name).isEmpty()) {
            userService.save(RoomUser.builder()
                    .username(name)
                    .status(UserStatus.FREE)
                    .build());
        }
    }
}
