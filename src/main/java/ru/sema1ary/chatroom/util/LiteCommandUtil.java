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

    public LiteCommands<CommandSender> create(String prefix, String invalidUsageMessage, String playerOnlyMessage,
                                              String playerNotFoundMessage, Object... commands) {
        return LiteBukkitFactory.builder()
                .settings(settings -> settings
                        .fallbackPrefix(prefix)
                        .nativePermissions(true)
                )
                .argument(Room.class, new RoomArgument(roomService))
                .commands(commands)
                .message(LiteBukkitMessages.INVALID_USAGE, invalidUsageMessage)
                .message(LiteBukkitMessages.PLAYER_ONLY, playerOnlyMessage)
                .message(LiteBukkitMessages.PLAYER_NOT_FOUND, playerNotFoundMessage)
                .schematicGenerator(SchematicFormat.angleBrackets())
                .build();
    }
}
