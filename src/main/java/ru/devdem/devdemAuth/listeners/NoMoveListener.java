package ru.devdem.devdemAuth.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
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
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class NoMoveListener implements Listener {

    public Set<DevdemUser> loginUsers = new HashSet<>();


    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event) {
        DevdemUser user = searchByName(event.getPlayer().getName());
        if (user != null) {
            loginUsers.remove(user);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String ip = Objects.requireNonNull(event.getPlayer().getAddress()).getAddress().toString();
        String username = event.getPlayer().getName();
        DevdemUser user = DevdemUser.getUserByName(username);
        Player player = event.getPlayer();

        if (user == null) {
            player.kick(Component.text("Произошла ошибка #100. Нет в БД после velocity. Пришлите скриншот ошибки."), PlayerKickEvent.Cause.PLUGIN);
            return;
        }
        user.setNewIp(ip);
        user.setNewDate(Timestamp.valueOf(LocalDateTime.now()));
        loginUsers.add(user);
        if (user.getType() == DevdemUser.UserType.BEDROCK || user.getType() == DevdemUser.UserType.ONLINE) {
            player.showTitle(TitlesUtils.joinTitle);
            user.setLastIp(user.getNewIp());
            user.setLastDate(user.getNewDate());
            user.setStatus(DevdemUser.Status.JOINING);
            user.update();
            DevdemAuth.ConnectUser(player);
            return;
        }
        if (shouldAutoLogin(ip, user.getLastIp(), user.getLastDate().getTime()) &&
                (user.getPasswordHash() != null || !Objects.equals(user.getPasswordHash().toLowerCase(), "null"))) {
            // успешный вход по авто-логину
            player.showTitle(TitlesUtils.joinTitle);
            user.setLastIp(user.getNewIp());
            user.setLastDate(user.getNewDate());
            user.setStatus(DevdemUser.Status.JOINING);
            user.update();
            DevdemAuth.ConnectUser(player);
        } else {
            // запрашиваем пароль.
            if (user.getPasswordHash() == null || Objects.equals(user.getPasswordHash().toLowerCase(), "null")) {
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

    boolean shouldAutoLogin(String playerIP, String lastIP, long lastLoginTime) {
        long now = System.currentTimeMillis();

        boolean sameIP = playerIP.equals(lastIP);
        boolean within24h = (now - lastLoginTime) <= 86400000;

        return sameIP && within24h;
    }


    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        // блокируем любые передвижения на сервере
        event.setTo(event.getFrom());
        handleEvent(event.getPlayer());
        event.setCancelled(true);
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        event.setCancelled(true);
        handleEvent(event.getPlayer());
    }

    private void handleEvent(Player player) {
        DevdemUser user = searchByName(player.getName());
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        if (user.lastHandled == null) {
            user.lastHandled = now;
        }
        if ((now.getTime() - user.lastHandled.getTime()) <= 2500) { // лучше не спамить каждый тик в чат и тайтлом
            return;
        }
        user.lastHandled = now;
        if (user.getStatus() == DevdemUser.Status.LOGIN) {
            player.showTitle(TitlesUtils.joinTitle);
        } else if (user.getStatus() == DevdemUser.Status.REGISTRATION) {
            player.showTitle(TitlesUtils.registerTitle);
        } else if (user.getStatus() == DevdemUser.Status.JOINING) {
            player.sendMessage(Component.text("Приятной игры!"));
            DevdemAuth.ConnectUser(player); // я надеюсь временное решение...
        } else {
            player.sendMessage(Component.text("Ты скорее всего уже авторизовался."));
            player.sendMessage(Component.text("Подожди подключение к серверу.."));
        }
    }

    public DevdemUser searchByName(String name) {
        for (DevdemUser user : loginUsers) {
            if (Objects.equals(user.getUsername(), name)) {
                return user;
            }
        }
        return null;
    }
}
