package ru.sema1ary.chatroom.listener;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import ru.vidoskim.bukkit.service.ConfigurationService;

@RequiredArgsConstructor
public class JoinListener implements Listener {
    private final MiniMessage miniMessage;
    private final ConfigurationService configurationService;

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        if(configurationService.get("should-hide-player-nicks")) {
            Player player = event.getPlayer();

            player.playerListName(miniMessage.deserialize(
                    configurationService.get("playerlist-name")
            ));

            Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

            Team team = scoreboard.getTeam("chatroom_nick_team");
            if (team == null) {
                team = scoreboard.registerNewTeam("chatroom_nick_team");
            }

            team.addEntry(player.getName());
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        }
    }
}
