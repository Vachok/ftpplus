package ru.vachok.money.other;


import java.awt.*;


/**
 @since 29.09.2018 (22:33) */
public class SystemTrayHelper {

    public static void main(String[] args) {
        SystemTray systemTray = SystemTray.getSystemTray();
        TrayIcon[] trayIcons = systemTray.getTrayIcons();
    }
}