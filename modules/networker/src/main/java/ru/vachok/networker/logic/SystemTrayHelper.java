package ru.vachok.networker.logic;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;


/**
 @since 29.09.2018 (22:33) */
public class SystemTrayHelper {

    /*Fields*/
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemTrayHelper.class.getSimpleName());

    public void addTray() {
        SystemTray systemTray = SystemTray.getSystemTray();
        Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/static/images/icons8-ip-адрес-15.png"));
        ActionListener actionListener = e -> {
            try{
                Desktop.getDesktop().browse(URI.create("http://localhost:8880"));
            }
            catch(IOException e1){
                LOGGER.error(e1.getMessage(), e1);
            }
        };
        ActionListener exitApp = e -> System.exit(0);
        PopupMenu popupMenu = new PopupMenu();
        MenuItem defItem = new MenuItem();
        defItem.setLabel("Exit");
        defItem.addActionListener(exitApp);
        popupMenu.add(defItem);
        TrayIcon trayIcon = new TrayIcon(image, "Networker", popupMenu);
        trayIcon.addActionListener(actionListener);

        try{
            if(SystemTray.isSupported()){
                systemTray.add(trayIcon);
            }
            else{
                LOGGER.warn("Tray not supported!");
            }
        }
        catch(AWTException e){
            LOGGER.warn(e.getMessage(), e);
        }
    }
}