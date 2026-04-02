package ru.devdem.devdemAuth.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NonNull;
import ru.devdem.devdemAuth.DevdemAuth;
import ru.devdem.devdemAuth.classes.DevdemUser;
import ru.devdem.devdemAuth.listeners.NoMoveListener;
import ru.devdem.devdemAuth.utils.PasswordUtils;

public class RegisterCommand implements BasicCommand {

    public NoMoveListener moveListener;

    public RegisterCommand(NoMoveListener listener) {
        moveListener = listener;
    }


    @Override
    public void execute(CommandSourceStack commandSourceStack, String @NonNull [] args) {
        CommandSender sender = commandSourceStack.getSender();
        DevdemUser user = moveListener.searchByName(sender.getName());
        if (user.getStatus() != DevdemUser.Status.REGISTRATION) {
            sender.sendMessage(Component.text("Ты уже зарегистрировался! /log пароль"));
            return;
        }
        if (args.length != 1) {
            sender.sendMessage(Component.text("Неправильно введена команда. /reg пароль"));
        }
        String password = args[0];
        sender.sendMessage(Component.text("Ваш новый пароль: "+password));
        sender.sendMessage(Component.text("Регистрация.."));
        user.setSalt(PasswordUtils.generateSalt());
        user.setPasswordHash(PasswordUtils.hashPassword(password, user.getSalt()));
        user.update();

        // проверяем пароль, на всякий
        if (PasswordUtils.verifyPassword(password, user.getSalt(), user.getPasswordHash())) {
            sender.sendMessage(Component.text("Успешная регистрация!"));
            user.setStatus(DevdemUser.Status.JOINING);
            DevdemAuth.ConnectUser(sender.getServer().getPlayer(sender.getName()));
        } else {
            sender.sendMessage(Component.text("Ошибка регистрации.."));
            sender.sendMessage(Component.text("Перезайдите на сервер"));
            //TODO: надо сделать кик игрока
        }

    }
}
