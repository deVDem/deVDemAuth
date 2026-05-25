package ru.devdem.devdemAuth;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import ru.devdem.devdemAuth.commands.LoginCommand;
import ru.devdem.devdemAuth.commands.RegisterCommand;
import ru.devdem.devdemAuth.listeners.NoMoveListener;
import ru.devdem.devdemAuth.utils.DatabaseManager;

public final class DevdemAuth extends JavaPlugin {

    private static DevdemAuth instance;
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        instance = this;

        PluginManager manager = getServer().getPluginManager();
        FileConfiguration config = getConfig();

        String host = config.getString("mysql.host");
        int port = config.getInt("mysql.port");
        String database = config.getString("mysql.database");
        String username = config.getString("mysql.username");
        String password = config.getString("mysql.password");
        boolean useSSL = config.getBoolean("mysql.useSSL");

        databaseManager = DatabaseManager.getInstance(host, port, database, username, password, useSSL);
        NoMoveListener noMoveListener = new NoMoveListener();
        manager.registerEvents(noMoveListener, this);
        registerCommand("log", new LoginCommand(noMoveListener));
        registerCommand("reg", new RegisterCommand(noMoveListener));
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.disconnect();
        }
        getServer().getMessenger().unregisterOutgoingPluginChannel(this);
    }

    public static void connectUser(Player player) {
        if (player == null) {
            if (instance != null) {
                instance.getLogger().warning("Не удалось подключить игрока к lobby: player == null");
            }
            return;
        }
        if (instance == null || !instance.isEnabled()) {
            player.sendMessage(Component.text("Сервер авторизации еще не готов к переносу в lobby."));
            return;
        }

        player.sendMessage(Component.text("Приятной игры!"));
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF("lobby");
        player.sendPluginMessage(instance, "BungeeCord", out.toByteArray());
    }

    @Deprecated(forRemoval = true)
    public static void ConnectUser(Player player) {
        connectUser(player);
    }

}
