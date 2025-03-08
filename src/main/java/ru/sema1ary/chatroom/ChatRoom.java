package ru.sema1ary.chatroom;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import lombok.SneakyThrows;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;
import ormlite.ConnectionSourceUtil;
import ru.sema1ary.chatroom.command.ChatRoomCommand;
import ru.sema1ary.chatroom.listener.ChatListener;
import ru.sema1ary.chatroom.listener.JoinListener;
import ru.sema1ary.chatroom.listener.PreJoinListener;
import ru.sema1ary.chatroom.listener.flower.FlowerListener;
import ru.sema1ary.chatroom.model.Hub;
import ru.sema1ary.chatroom.model.Room;
import ru.sema1ary.chatroom.model.user.RoomUser;
import ru.sema1ary.chatroom.placeholder.RoomPlaceholder;
import ru.sema1ary.chatroom.service.HubService;
import ru.sema1ary.chatroom.service.RoomService;
import ru.sema1ary.chatroom.service.RoomUserService;
import ru.sema1ary.chatroom.service.impl.HubServiceImpl;
import ru.sema1ary.chatroom.service.impl.RoomServiceImpl;
import ru.sema1ary.chatroom.service.impl.RoomUserServiceImpl;
import ru.sema1ary.chatroom.util.LiteCommandUtil;
import ru.vidoskim.bukkit.service.ConfigService;
import ru.vidoskim.bukkit.service.impl.ConfigServiceImpl;
import service.ServiceManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

//Ингредиенты для «Лаймовый пирог»:
//Масло сливочное — 100 г
//Мука пшеничная / Мука — 200 г
//Вода — 5 ст. л.
//Соль (щепотка)
//Лайм — 3 шт.
//Яйцо куриное — 2 шт.
//Молоко сгущенное (1 банка) — 400 г

public final class ChatRoom extends JavaPlugin {
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    private JdbcPooledConnectionSource connectionSource;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        ServiceManager.registerService(ConfigService.class, new ConfigServiceImpl(this));

        initConnectionSource();

        ServiceManager.registerService(HubService.class, new HubServiceImpl(getDao(Hub.class), this,
                ServiceManager.getService(ConfigService.class)));

        ServiceManager.registerService(RoomUserService.class, new RoomUserServiceImpl(this, getDao( RoomUser.class),
                miniMessage, ServiceManager.getService(ConfigService.class)));

        ServiceManager.registerService(RoomService.class,
                new RoomServiceImpl(getDao(Room.class),
                ServiceManager.getService(HubService.class),
                ServiceManager.getService(RoomUserService.class))
        );

        registerListeners();
        registerCommand();

        if(getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new RoomPlaceholder(ServiceManager.getService(RoomUserService.class)).register();
            getLogger().info("Successful PlaceholderAPI hook!");
        } else {
            getLogger().warning("Where PlaceholderAPI? Hook failed.");
        }
    }

    @Override
    public void onDisable() {
        ConnectionSourceUtil.closeConnection(true, connectionSource);
    }

    @SneakyThrows
    private void initConnectionSource() {
        if(ServiceManager.getService(ConfigService.class).get("sql-use")) {
            connectionSource = ConnectionSourceUtil.connectSQLDatabase(
                    ServiceManager.getService(ConfigService.class).get("sql-driver"),
                    ServiceManager.getService(ConfigService.class).get("sql-host"),
                    ServiceManager.getService(ConfigService.class).get("sql-database"),
                    ServiceManager.getService(ConfigService.class).get("sql-user"),
                    ServiceManager.getService(ConfigService.class).get("sql-password"),
                    Room.class, RoomUser.class, Hub.class);
            return;
        }

        Path databaseFilePath = Paths.get("plugins/chat-room/database.sqlite");
        if(!Files.exists(databaseFilePath) && !databaseFilePath.toFile().createNewFile()) {
            return;
        }

        connectionSource = ConnectionSourceUtil.connectNoSQLDatabase("sqlite",
                databaseFilePath.toString(), Room.class, RoomUser.class, Hub.class);
    }

    private <D extends Dao<T, ?>, T> D getDao(Class<T> daoClass) {
        return DaoManager.lookupDao(connectionSource, daoClass);
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PreJoinListener(
                ServiceManager.getService(RoomUserService.class)), this);
        getServer().getPluginManager().registerEvents(new ChatListener(miniMessage,
                ServiceManager.getService(RoomUserService.class),
                ServiceManager.getService(ConfigService.class)), this);
        getServer().getPluginManager().registerEvents(new FlowerListener(miniMessage,
                ServiceManager.getService(ConfigService.class)), this);
        getServer().getPluginManager().registerEvents(new JoinListener(this, miniMessage,
                ServiceManager.getService(ConfigService.class)), this);
    }

    private void registerCommand() {
        new LiteCommandUtil(ServiceManager.getService(RoomService.class)).create(
                new ChatRoomCommand(miniMessage,
                        ServiceManager.getService(HubService.class),
                        ServiceManager.getService(RoomService.class),
                        ServiceManager.getService(RoomUserService.class),
                        ServiceManager.getService(ConfigService.class))
                );
    }
}
