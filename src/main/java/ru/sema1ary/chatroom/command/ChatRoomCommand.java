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
import ru.sema1ary.chatroom.ChatRoom;
import ru.sema1ary.chatroom.model.Room;
import ru.sema1ary.chatroom.model.user.RoomUser;
import ru.sema1ary.chatroom.model.user.UserStatus;
import ru.sema1ary.chatroom.service.HubService;
import ru.sema1ary.chatroom.service.RoomService;
import ru.sema1ary.chatroom.service.RoomUserService;
import ru.vidoskim.bukkit.service.MessagesService;

@RequiredArgsConstructor
@Command(name = "chatroom")
public class ChatRoomCommand {
    private final ChatRoom plugin;
    private final MiniMessage miniMessage;
    private final HubService hubService;
    private final RoomService roomService;
    private final RoomUserService userService;
    private final MessagesService messagesService;

    @Async
    @Execute(name = "reload")
    @Permission("chatroom.reload")
    void reload(@Context CommandSender sender) {
        messagesService.reload(plugin);
        sender.sendMessage(miniMessage.deserialize(messagesService.getMessage("successful-reload")));
    }

    @Async
    @Execute(name = "create")
    @Permission("chatroom.create")
    void create(@Context Player sender, @Arg("название") String name) {
        if(roomService.getRoomFromMap(name) != null) {
            sender.sendMessage(miniMessage.deserialize(messagesService.getMessage("room-error-already-exists")));
            return;
        }

        roomService.createRoom(name, sender.getLocation());
        sender.sendMessage(miniMessage.deserialize(messagesService.getMessage("room-successful-created")));
    }

    @Async
    @Execute(name = "sethub")
    @Permission("chatroom.sethub")
    void setHub(@Context Player sender) {
        hubService.setHub(sender.getLocation());
        sender.sendMessage(miniMessage.deserialize(messagesService.getMessage("hub-successful-set")));
    }

    @Async
    @Execute(name = "start")
    @Permission("chatroom.start")
    void start(@Context Player sender) {
        RoomUser user = userService.getUserFromMap(sender.getName());
        if(user.getStatus().equals(UserStatus.QUEUE)) {
            sender.sendMessage(miniMessage.deserialize(messagesService
                    .getMessage("start-error-already-in-queue")));
            return;
        }

        if (user.getStatus().equals(UserStatus.BUSY)) {
            sender.sendMessage(miniMessage.deserialize(messagesService
                    .getMessage("start-error-already-in-room")));
            return;
        }

        user.setStatus(UserStatus.QUEUE);
        userService.save(user);
        sender.sendMessage(miniMessage.deserialize(messagesService.getMessage("start-successful-queued")));

        Player roommate = userService.findRoommate(sender);

        if(roommate == null || !roommate.isOnline()) {
            return;
        }

        sender.sendMessage(miniMessage.deserialize(messagesService.getMessage("start-roommate-found")));
        roommate.sendMessage(miniMessage.deserialize(messagesService.getMessage("start-roommate-found")));

        if(!roomService.startRoom(sender, roommate)) {
            sender.sendMessage(miniMessage.deserialize(messagesService.getMessage("start-error-no-rooms-available")));
            return;
        }

        sender.sendMessage(miniMessage.deserialize(messagesService.getMessage("room-successful-found")));
    }

    @Async
    @Execute(name = "stop")
    @Permission("chatroom.stop")
    void stop(@Context Player sender) {
        RoomUser user = userService.getUserFromMap(sender.getName());
        if(user.getStatus().equals(UserStatus.FREE)) {
            sender.sendMessage(miniMessage.deserialize(messagesService.getMessage("stop-dont-in-queue-error")));
            return;
        }

        if(user.getStatus().equals(UserStatus.QUEUE)) {
            user.setStatus(UserStatus.FREE);
            userService.save(user);
            hubService.teleportToHub(sender);
            sender.sendMessage(miniMessage.deserialize(messagesService.getMessage("stop-successful")));
            return;
        }

        Room room = user.getInRoom();
        roomService.stopRoom(room);

        user.setStatus(UserStatus.FREE);
        userService.save(user);
        sender.sendMessage(miniMessage.deserialize(messagesService.getMessage("stop-successful")));
    }
}
