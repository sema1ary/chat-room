package ru.sema1ary.chatroom.util;

import dev.rollczi.litecommands.LiteCommands;
import dev.rollczi.litecommands.bukkit.LiteBukkitFactory;
import dev.rollczi.litecommands.bukkit.LiteBukkitMessages;
import dev.rollczi.litecommands.schematic.SchematicFormat;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import ru.sema1ary.chatroom.command.argument.RoomArgument;
import ru.sema1ary.chatroom.model.Room;
import ru.sema1ary.chatroom.service.RoomService;

@RequiredArgsConstructor
@SuppressWarnings("all")
public class LiteCommandUtil {
    private final RoomService roomService;

    public LiteCommands<CommandSender> create(Object... commands) {
        return LiteBukkitFactory.builder()
                .settings(settings -> settings
                        .fallbackPrefix("chatroom")
                        .nativePermissions(true)
                )
                .argument(Room.class, new RoomArgument(roomService))
                .commands(commands)
                .message(LiteBukkitMessages.INVALID_USAGE, "&cНеверное использование!")
                .message(LiteBukkitMessages.PLAYER_ONLY, "&cЭта команда только для игроков!")
                .message(LiteBukkitMessages.PLAYER_NOT_FOUND, "&cЭтот игрок не найден.")
                .message(LiteBukkitMessages.MISSING_PERMISSIONS, "&cУ вас нет прав.")
                .schematicGenerator(SchematicFormat.angleBrackets())
                .build();
    }
}
