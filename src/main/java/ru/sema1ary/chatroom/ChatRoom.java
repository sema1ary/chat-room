package ru.sema1ary.chatroom;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import lombok.SneakyThrows;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;
import ormlite.ConnectionSourceUtil;
import ru.sema1ary.chatroom.command.ChatRoomCommand;
import ru.sema1ary.chatroom.listener.JoinListener;
import ru.sema1ary.chatroom.listener.PreJoinListener;
import ru.sema1ary.chatroom.listener.QuitListener;
import ru.sema1ary.chatroom.model.Hub;
import ru.sema1ary.chatroom.model.Room;
import ru.sema1ary.chatroom.model.user.RoomUser;
import ru.sema1ary.chatroom.service.HubService;
import ru.sema1ary.chatroom.service.RoomService;
import ru.sema1ary.chatroom.service.RoomUserService;
import ru.sema1ary.chatroom.service.impl.HubServiceImpl;
import ru.sema1ary.chatroom.service.impl.RoomServiceImpl;
import ru.sema1ary.chatroom.service.impl.RoomUserServiceImpl;
import ru.vidoskim.bukkit.service.MessagesService;
import ru.vidoskim.bukkit.util.LiteCommandUtil;
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
        initConnectionSource();

        MessagesService messagesService = new MessagesService();
        messagesService.reload(this);

        ServiceManager.registerService(HubService.class, new HubServiceImpl(getDao(Hub.class), this, messagesService));
        ServiceManager.registerService(RoomUserService.class, new RoomUserServiceImpl(getDao( RoomUser.class)));
        ServiceManager.registerService(RoomService.class, new RoomServiceImpl(getDao(Room.class),
                miniMessage, ServiceManager.getService(HubService.class),
                ServiceManager.getService(RoomUserService.class), messagesService));

        getServer().getPluginManager().registerEvents(new PreJoinListener(
                ServiceManager.getService(RoomUserService.class)), this);
        getServer().getPluginManager().registerEvents(new JoinListener(
                ServiceManager.getService(RoomUserService.class)), this);
        getServer().getPluginManager().registerEvents(new QuitListener(
                ServiceManager.getService(RoomUserService.class)), this);

        new LiteCommandUtil().create(messagesService.getMessage("commands-prefix"),
                messagesService.getMessage("commands-invalid-usage"),
                messagesService.getMessage("commands-player-only"),
                messagesService.getMessage("commands-player-not-found"),

                new ChatRoomCommand(this, miniMessage,
                        ServiceManager.getService(HubService.class),
                        ServiceManager.getService(RoomService.class),
                        ServiceManager.getService(RoomUserService.class),
                        messagesService)
        );
    }

    @Override
    public void onDisable() {
        ServiceManager.disableServices();

        ConnectionSourceUtil.closeConnection(connectionSource);
    }

    @SneakyThrows
    private void initConnectionSource() {
        if(getConfig().getBoolean("configuration.sql.use")) {
            initSQLConnectionSource(getConfig().getString("configuration.sql.driver"));
            return;
        }

        Path databaseFilePath = Paths.get("plugins/chat-room/database.sqlite");
        if(!Files.exists(databaseFilePath) && !databaseFilePath.toFile().createNewFile()) {
            return;
        }

        connectionSource = ConnectionSourceUtil.connectNoSQLDatabase("sqlite",
                databaseFilePath.toString(), Room.class, RoomUser.class, Hub.class);
    }

    private void initSQLConnectionSource(String driver) {
        connectionSource = ConnectionSourceUtil.connectSQLDatabase(driver,
                getConfig().getString("configuration.sql.host"),
                getConfig().getString("configuration.sql.database"),
                getConfig().getString("configuration.sql.user"),
                getConfig().getString("configuration.sql.password"),
                Room.class, RoomUser.class, Hub.class);
    }

    private <D extends Dao<T, ?>, T> D getDao(Class<T> daoClass) {
        return DaoManager.lookupDao(connectionSource, daoClass);
    }
}
