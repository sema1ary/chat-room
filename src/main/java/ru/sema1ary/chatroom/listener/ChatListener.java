package ru.sema1ary.chatroom.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import ru.sema1ary.chatroom.model.user.RoomUser;
import ru.sema1ary.chatroom.service.RoomUserService;
import ru.vidoskim.bukkit.service.ConfigService;

@RequiredArgsConstructor
public class ChatListener implements Listener {
    private final MiniMessage miniMessage;
    private final RoomUserService userService;
    private final ConfigService configService;

    @EventHandler
    private void onChat(AsyncChatEvent event) {
        Player playerOne = event.getPlayer();
        RoomUser user = userService.getUser(playerOne.getName());

        if(!userService.isInRoom(user)) {
            userService.sendMessage(user, "chat-error-only-in-room");
        } else {
            RoomUser roommate = userService.getRoommate(user);
            Player playerTwo = Bukkit.getPlayer(roommate.getUsername());

            if(playerTwo == null || !playerTwo.isOnline()) {
                return;
            }

            String format = configService.get("chat-format");
            String selfFormat = configService.get("self-chat-format");

            playerOne.sendMessage(miniMessage.deserialize(selfFormat.replace("{sender}"
                    , "Вы")).append(event.message()));
            playerTwo.sendMessage(miniMessage.deserialize(format.replace("{sender}"
                    , "Собеседник")).append(event.message()));
        }

        event.setCancelled(true);
    }
}
