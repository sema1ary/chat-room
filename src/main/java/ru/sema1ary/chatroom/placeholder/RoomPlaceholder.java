package ru.sema1ary.chatroom.placeholder;

import com.j256.ormlite.dao.ForeignCollection;
import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import ru.sema1ary.chatroom.model.user.RoomUser;
import ru.sema1ary.chatroom.service.RoomUserService;

import java.util.List;

@RequiredArgsConstructor
public class RoomPlaceholder extends PlaceholderExpansion {
    private final RoomUserService userService;

    @Override
    public @NotNull String getIdentifier() {
        return "chatroom";
    }

    @Override
    public @NotNull String getAuthor() {
        return "sema1ary";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if(player == null || player.getName() == null) {
            return null;
        }

        RoomUser user = userService.getUser(player.getName());

        if(params.equalsIgnoreCase("is_in_room")) {
            return String.valueOf(userService.isInRoom(user));
        }

        if(params.equalsIgnoreCase("player_room")) {
            if(user.getInRoom() == null) {
                return "";
            }

            return user.getInRoom().getId().toString();
        }

        if(params.equalsIgnoreCase("player_roommate")) {
            ForeignCollection<RoomUser> userInRoom = user.getInRoom().getUsers();
            List<RoomUser> users = userInRoom.stream().filter(roomUser -> !roomUser.getUsername()
                            .equalsIgnoreCase(player.getName())).toList();

            if(!users.isEmpty()) {
                return users.get(0).getUsername();
            }

            return "";
        }

        return null;
    }
}
