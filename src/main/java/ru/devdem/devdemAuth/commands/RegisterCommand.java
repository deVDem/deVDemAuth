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

public class RegisterCommand implements BasicCommand {

    private final NoMoveListener moveListener;

    public RegisterCommand(NoMoveListener listener) {
        moveListener = listener;
    }


    @Override
    public void execute(CommandSourceStack commandSourceStack, String @NonNull [] args) {
        CommandSender sender = commandSourceStack.getSender();
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Эта команда доступна только игроку."));
            return;
        }

        DevdemUser user = moveListener.searchByName(player.getName());
        if (user == null) {
            player.sendMessage(Component.text("Вы не ожидаете регистрацию на этом сервере."));
            return;
        }
        if (user.getStatus() != DevdemUser.Status.REGISTRATION) {
            sender.sendMessage(Component.text("Ты уже зарегистрировался! /log пароль"));
            return;
        }
        if (args.length != 1) {
            sender.sendMessage(Component.text("Неправильно введена команда. /reg пароль"));
            return;
        }
        String password = args[0];
        sender.sendMessage(Component.text("Регистрация.."));
        user.setSalt(PasswordUtils.generateSalt());
        user.setPasswordHash(PasswordUtils.hashPassword(password, user.getSalt()));
        user.setLastIp(user.getNewIp());
        user.setLastDate(user.getNewDate());

        // проверяем пароль, на всякий
        if (PasswordUtils.verifyPassword(password, user.getSalt(), user.getPasswordHash()) && user.update()) {
            sender.sendMessage(Component.text("Успешная регистрация!"));
            user.setStatus(DevdemUser.Status.JOINING);
            DevdemAuth.connectUser(player);
            player.showTitle(TitlesUtils.joinTitle);
        } else {
            sender.sendMessage(Component.text("Ошибка регистрации.."));
            sender.sendMessage(Component.text("Перезайдите на сервер"));
            player.kick(Component.text("Ошибка: не удалось зарегистрировать вас. Перезайдите позже"));
        }

    }
}
