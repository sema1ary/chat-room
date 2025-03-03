package ru.sema1ary.chatroom.command;

import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.async.Async;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.sema1ary.chatroom.model.Room;
import ru.sema1ary.chatroom.model.user.RoomUser;
import ru.sema1ary.chatroom.model.user.UserStatus;
import ru.sema1ary.chatroom.service.HubService;
import ru.sema1ary.chatroom.service.RoomService;
import ru.sema1ary.chatroom.service.RoomUserService;
import ru.sema1ary.chatroom.util.LocationUtil;
import ru.vidoskim.bukkit.service.ConfigurationService;
import ru.vidoskim.bukkit.service.MessagesService;

import java.util.Optional;

@RequiredArgsConstructor
@Command(name = "chatroom")
public class ChatRoomCommand {
    private final MiniMessage miniMessage;
    private final HubService hubService;
    private final RoomService roomService;
    private final RoomUserService userService;
    private final MessagesService messagesService;
    private final ConfigurationService configurationService;

    @Async
    @Execute(name = "reload")
    @Permission("chatroom.reload")
    void reload(@Context CommandSender sender) {
        messagesService.reload();
        configurationService.reload();
        sender.sendMessage(miniMessage.deserialize(messagesService.getMessage("successful-reload")));
    }

    @Async
    @Execute(name = "create")
    @Permission("chatroom.create")
    void create(@Context Player sender, @Arg("название") String name) {
        Optional<Room> optionalRoom = roomService.findByName(name);

        RoomUser user = userService.getUser(sender.getName());

        if(optionalRoom.isPresent()) {
            userService.sendMessage(user, "room-error-already-exists");
            return;
        }

        roomService.save(Room.builder()
                .name(name)
                .isAvailable(true)
                .location(LocationUtil.locationToString(sender.getLocation()))
                .build());

        userService.sendMessage(user, "room-successful-created");
    }

    @Async
    @Execute(name = "delete", aliases = {"remove", "del"})
    @Permission("chatroom.delete")
    void delete(@Context CommandSender sender, @Arg("комната") Room room) {
        roomService.delete(room);
        userService.sendMessage(userService.getUser(sender.getName()), "room-successful-deleted");
    }

    @Async
    @Execute(name = "sethub")
    @Permission("chatroom.sethub")
    void setHub(@Context Player sender) {
        hubService.setHub(sender.getLocation());
        userService.sendMessage(userService.getUser(sender.getName()), "hub-successful-set");
    }

    @Async
    @Execute(name = "start")
    @Permission("chatroom.start")
    void start(@Context Player sender) {
        RoomUser user = userService.getUser(sender.getName());
        roomService.findRoom(user);
    }

    @Async
    @Execute(name = "skip")
    @Permission("chatroom.skip")
    void skip(@Context Player sender) {
        RoomUser user = userService.getUser(sender.getName());
        Room room = user.getInRoom();

        if(room == null) {
            userService.sendMessage(user, "stop-dont-in-queue-error");
            return;
        }

        roomService.stopRoom(room);
    }

    @Async
    @Execute(name = "stop")
    @Permission("chatroom.stop")
    void stop(@Context Player sender) {
        RoomUser user = userService.getUser(sender.getName());

        if(user.getStatus().equals(UserStatus.QUEUE)) {
            user.setStatus(UserStatus.FREE);
            userService.save(user);
            userService.sendMessage(user, "stop-successful");
            return;
        }

        Room room = user.getInRoom();

        if(room == null) {
            userService.sendMessage(user, "stop-dont-in-queue-error");
            return;
        }

        roomService.cancelRoom(user, room);
    }
}
