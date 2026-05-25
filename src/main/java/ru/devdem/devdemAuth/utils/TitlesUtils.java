package ru.devdem.devdemAuth.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;

public class TitlesUtils {

    private TitlesUtils() {
    }

    public static final Title joinTitle = Title.title(
            Component.text("УСПЕШНЫЙ ВХОД", TextColor.color(100, 255, 100)),
            Component.text("Ожидайте входа..")
    );
    public static final Title registerTitle = Title.title(
            Component.text("РЕГИСТРАЦИЯ", TextColor.color(300000)),
            Component.text("Введите /reg пароль")
    );
    public static final Title loginTitle = Title.title(
            Component.text("АВТОРИЗАЦИЯ", TextColor.color(300000)),
            Component.text("Введите /log пароль")
    );
}
