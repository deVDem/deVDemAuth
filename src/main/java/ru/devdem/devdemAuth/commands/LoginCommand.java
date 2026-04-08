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
import ru.devdem.devdemAuth.utils.TitlesUtils;

public class LoginCommand implements BasicCommand {

    public NoMoveListener moveListener;

    public LoginCommand(NoMoveListener listener) {
        moveListener = listener;
    }

    @Override
    public void execute(@NonNull CommandSourceStack commandSourceStack, String @NonNull [] args) {
        CommandSender sender = commandSourceStack.getSender();
        DevdemUser user = moveListener.searchByName(sender.getName());
        if (user.getStatus() != DevdemUser.Status.LOGIN) {
            sender.sendMessage(Component.text("Тебе нужно пройти регистрацию. /reg пароль"));
            return;
        }
        if (args.length != 1) {
            sender.sendMessage(Component.text("Неправильно введена команда. /log пароль"));
        }
        String password = args[0];
        if (PasswordUtils.verifyPassword(password, user.getSalt(), user.getPasswordHash())) {
            sender.sendMessage(Component.text("Успешный вход."));
            sender.showTitle(TitlesUtils.joinTitle);
            user.setLastIp(user.getNewIp());
            user.setLastDate(user.getNewDate());
            user.setStatus(DevdemUser.Status.JOINING);
            user.update();
            DevdemAuth.ConnectUser(sender.getServer().getPlayer(sender.getName()));
        }
    }
}
