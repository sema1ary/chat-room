package ru.sema1ary.chatroom.command.argument;

import dev.rollczi.litecommands.argument.Argument;
import dev.rollczi.litecommands.argument.parser.ParseResult;
import dev.rollczi.litecommands.argument.resolver.ArgumentResolver;
import dev.rollczi.litecommands.invocation.Invocation;
import dev.rollczi.litecommands.suggestion.SuggestionContext;
import dev.rollczi.litecommands.suggestion.SuggestionResult;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import ru.sema1ary.chatroom.model.Room;
import ru.sema1ary.chatroom.service.RoomService;

import java.util.Optional;

@RequiredArgsConstructor
public class RoomArgument extends ArgumentResolver<CommandSender, Room> {
    private final RoomService roomService;

    @Override
    protected ParseResult<Room> parse(Invocation<CommandSender> invocation, Argument<Room> argument, String s) {
        Optional<Room> optionalRoom = roomService.findByName(s);

        return optionalRoom.map(ParseResult::success).orElseGet(() -> ParseResult.failure("Room not found"));

    }

    @Override
    public SuggestionResult suggest(Invocation<CommandSender> invocation, Argument<Room> argument, SuggestionContext context) {
        return roomService.findAll().stream().map(Room::getName).collect(SuggestionResult.collector());
    }
}
