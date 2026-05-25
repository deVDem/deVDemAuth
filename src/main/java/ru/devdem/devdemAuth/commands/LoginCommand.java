package ru.devdem.devdemAuth.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import ru.devdem.devdemAuth.DevdemAuth;
import ru.devdem.devdemAuth.classes.DevdemUser;
import ru.devdem.devdemAuth.listeners.NoMoveListener;
import ru.devdem.devdemAuth.utils.PasswordUtils;
import ru.devdem.devdemAuth.utils.TitlesUtils;

public class LoginCommand implements BasicCommand {

    private final NoMoveListener moveListener;

    public LoginCommand(NoMoveListener listener) {
        moveListener = listener;
    }

    @Override
    public void execute(@NonNull CommandSourceStack commandSourceStack, String @NonNull [] args) {
        CommandSender sender = commandSourceStack.getSender();
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Эта команда доступна только игроку."));
            return;
        }

        DevdemUser user = moveListener.searchByName(player.getName());
        if (user == null) {
            player.sendMessage(Component.text("Вы не ожидаете авторизацию на этом сервере."));
            return;
        }
        if (user.getStatus() != DevdemUser.Status.LOGIN) {
            sender.sendMessage(Component.text("Тебе нужно пройти регистрацию. /reg пароль"));
            return;
        }
        if (args.length != 1) {
            sender.sendMessage(Component.text("Неправильно введена команда. /log пароль"));
            return;
        }
        String password = args[0];
        if (PasswordUtils.verifyPassword(password, user.getSalt(), user.getPasswordHash())) {
            sender.sendMessage(Component.text("Успешный вход."));
            player.showTitle(TitlesUtils.joinTitle);
            user.setLastIp(user.getNewIp());
            user.setLastDate(user.getNewDate());
            user.setStatus(DevdemUser.Status.JOINING);
            user.update();
            DevdemAuth.connectUser(player);
        } else {
            sender.sendMessage(Component.text("Неверный пароль."));
        }
    }
}
