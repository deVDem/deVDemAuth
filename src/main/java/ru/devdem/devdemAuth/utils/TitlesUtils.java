package ru.devdem.devdemAuth.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;

public class TitlesUtils {

    public static Title joinTitle = Title.title(
            Component.text("§aУСПЕШНЫЙ ВХОД", TextColor.color(100, 255, 100)),
            Component.text("Ожидайте входа..")
            );
    public static Title registerTitle = Title.title(
            Component.text("§bРЕГИСТРАЦИЯ", TextColor.color(300000)),
            Component.text("Введите /reg пароль")
    );
    public static Title loginTitle = Title.title(
            Component.text("АВТОРИЗАЦИЯ", TextColor.color(300000)),
            Component.text("Введите /log пароль")
    );
}
