package ru.devdem.devdemAuth.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.devdem.devdemAuth.DevdemAuth;
import ru.devdem.devdemAuth.classes.DevdemUser;
import ru.devdem.devdemAuth.utils.TitlesUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NoMoveListener implements Listener {

    private static final long AUTO_LOGIN_TIME_MS = 86_400_000L;
    private static final long MESSAGE_COOLDOWN_MS = 2_500L;

    private final Map<String, DevdemUser> loginUsers = new ConcurrentHashMap<>();


    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event) {
        DevdemUser user = searchByName(event.getPlayer().getName());
        if (user != null) {
            loginUsers.remove(normalizeName(user.getUsername()));
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String ip = getPlayerIp(player);
        String username = player.getName();
        DevdemUser user = DevdemUser.getUserByName(username);

        if (user == null) {
            player.kick(Component.text("Произошла ошибка #100. Нет в БД после velocity. Пришлите скриншот ошибки."), PlayerKickEvent.Cause.PLUGIN);
            return;
        }
        user.setNewIp(ip);
        user.setNewDate(Timestamp.valueOf(LocalDateTime.now()));
        loginUsers.put(normalizeName(user.getUsername()), user);
        if (user.getType() == DevdemUser.UserType.BEDROCK || user.getType() == DevdemUser.UserType.ONLINE) {
            player.showTitle(TitlesUtils.joinTitle);
            user.setLastIp(user.getNewIp());
            user.setLastDate(user.getNewDate());
            user.setStatus(DevdemUser.Status.JOINING);
            user.update();
            DevdemAuth.connectUser(player);
            return;
        }
        if (hasPassword(user) && shouldAutoLogin(ip, user.getLastIp(), user.getLastDate())) {
            // успешный вход по авто-логину
            player.showTitle(TitlesUtils.joinTitle);
            user.setLastIp(user.getNewIp());
            user.setLastDate(user.getNewDate());
            user.setStatus(DevdemUser.Status.JOINING);
            user.update();
            DevdemAuth.connectUser(player);
        } else {
            // запрашиваем пароль.
            if (!hasPassword(user)) {
                // регистрируемся
                player.showTitle(TitlesUtils.registerTitle);
                user.setStatus(DevdemUser.Status.REGISTRATION);
            } else {
                // логин
                player.showTitle(TitlesUtils.loginTitle);
                user.setStatus(DevdemUser.Status.LOGIN);
            }
        }
    }

    boolean shouldAutoLogin(String playerIP, String lastIP, Timestamp lastLoginDate) {
        if (playerIP == null || lastIP == null || lastLoginDate == null) {
            return false;
        }
        long now = System.currentTimeMillis();

        boolean sameIP = playerIP.equals(lastIP);
        boolean within24h = (now - lastLoginDate.getTime()) <= AUTO_LOGIN_TIME_MS;

        return sameIP && within24h;
    }


    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (searchByName(event.getPlayer().getName()) == null) {
            return;
        }
        // блокируем любые передвижения на сервере
        event.setTo(event.getFrom());
        handleEvent(event.getPlayer());
        event.setCancelled(true);
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        if (searchByName(event.getPlayer().getName()) == null) {
            return;
        }
        event.setCancelled(true);
        Bukkit.getScheduler().runTask(DevdemAuth.getPlugin(DevdemAuth.class), () -> handleEvent(event.getPlayer()));
    }

    private void handleEvent(Player player) {
        DevdemUser user = searchByName(player.getName());
        if (user == null) {
            return;
        }
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        if (user.lastHandled != null && (now.getTime() - user.lastHandled.getTime()) <= MESSAGE_COOLDOWN_MS) {
            return;
        }
        user.lastHandled = now;
        if (user.getStatus() == DevdemUser.Status.LOGIN) {
            player.showTitle(TitlesUtils.loginTitle);
        } else if (user.getStatus() == DevdemUser.Status.REGISTRATION) {
            player.showTitle(TitlesUtils.registerTitle);
        } else if (user.getStatus() == DevdemUser.Status.JOINING) {
            player.sendMessage(Component.text("Приятной игры!"));
            DevdemAuth.connectUser(player); // я надеюсь временное решение...
        } else {
            player.sendMessage(Component.text("Ты скорее всего уже авторизовался."));
            player.sendMessage(Component.text("Подожди подключение к серверу.."));
        }
    }

    public DevdemUser searchByName(String name) {
        return loginUsers.get(normalizeName(name));
    }

    private static boolean hasPassword(DevdemUser user) {
        String passwordHash = user.getPasswordHash();
        return passwordHash != null && !passwordHash.isBlank() && !Objects.equals(passwordHash.toLowerCase(Locale.ROOT), "null");
    }

    private static String getPlayerIp(Player player) {
        if (player.getAddress() == null) {
            return "";
        }
        return player.getAddress().getAddress().getHostAddress();
    }

    private static String normalizeName(String name) {
        return name == null ? "" : name.toLowerCase(Locale.ROOT);
    }
}
