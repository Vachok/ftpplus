package ru.vachok.money.other;


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

    public void addTrayDefaultMinimum() {
        SystemTray systemTray = SystemTray.getSystemTray();
        Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/static/images/icons8-монеты-15.png"));
        PopupMenu popupMenu = popMenuSetter();
        TrayIcon trayIcon = new TrayIcon(image, "Money", popupMenu);
        try{
            if(SystemTray.isSupported()){
                systemTray.add(trayIcon);
            }
            else{
                LOGGER.warn("Tray not supported!");
                Thread.currentThread().interrupt();
            }
        }
        catch(AWTException e){
            LOGGER.warn(e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
        ActionListener actionListener = e -> {
            try{
                Desktop.getDesktop().browse(URI.create("http://localhost:8881"));
            }
            catch(IOException e1){
                LOGGER.error(e1.getMessage(), e1);
            }
        };
        trayIcon.addActionListener(actionListener);
    }

    private PopupMenu popMenuSetter() {
        PopupMenu popupMenu = new PopupMenu();
        MenuItem exitItem = new MenuItem();
        MenuItem openSysInfoPage = new MenuItem();
        MenuItem calcCtrl = new MenuItem();
        MenuItem carDB = new MenuItem();
        MenuItem ftpHome = new MenuItem();
        MenuItem moneyItem = new MenuItem();
        MenuItem rebootSys = new MenuItem();
        MenuItem offSys = new MenuItem();

        ActionListener exitApp = e -> System.exit(0);
        exitItem.addActionListener(exitApp);
        exitItem.setLabel("Exit");
        openSysInfoPage.addActionListener(e -> {
            try{
                Desktop.getDesktop().browse(URI.create("http://localhost:8881/sysinfo"));
            }
            catch(IOException ex){
                LOGGER.error(ex.getMessage(), ex);
            }
        });
        openSysInfoPage.setLabel("Открыть System Info Page");
        calcCtrl.addActionListener(e -> {
            try{
                Desktop.getDesktop().browse(URI.create("http://localhost:8881/calc"));
            }
            catch(IOException e1){
                LOGGER.error(e1.getMessage(), e1);
            }
        });
        calcCtrl.setLabel("Calc");
        carDB.addActionListener(e -> {
            try{
                Desktop.getDesktop().browse(URI.create("http://localhost:8881/chkcar"));
            }
            catch(IOException e1){
                LOGGER.error(e1.getMessage(), e1);
            }
        });
        carDB.setLabel("Car");
        ftpHome.addActionListener(e -> {
            try{
                Desktop.getDesktop().browse(URI.create("http://localhost:8881/ftp"));
            }
            catch(IOException e1){
                LOGGER.error(e1.getMessage(), e1);
            }
        });
        ftpHome.setLabel("FTP");
        moneyItem.addActionListener(e -> {
            try{
                Desktop.getDesktop().browse(URI.create("http://localhost:8881/money"));
            }
            catch(IOException e1){
                LOGGER.error(e1.getMessage(), e1);
            }
        });
        moneyItem.setLabel("Считаем деньги");
        rebootSys.addActionListener(e -> {
            try{
                Runtime.getRuntime().exec("shutdown /r /f");
            }
            catch(IOException e1){
                LOGGER.error(e1.getMessage(), e1);
            }
        });
        rebootSys.setLabel("REBOOT THIS PC!");
        offSys.addActionListener(e -> {
            try{
                Runtime.getRuntime().exec("shutdown /p /f");
            }
            catch(IOException e1){
                LOGGER.error(e1.getMessage(), e1);
            }
        });
        offSys.setLabel("TURN OFF THIS PC!");

        popupMenu.add(calcCtrl);
        popupMenu.add(carDB);
        popupMenu.add(ftpHome);
        popupMenu.add(moneyItem);
        popupMenu.add(openSysInfoPage);
        popupMenu.addSeparator();
        popupMenu.add(rebootSys);
        popupMenu.add(offSys);
        popupMenu.addSeparator();
        popupMenu.add(exitItem);
        return popupMenu;
    }
}