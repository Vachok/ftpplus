package ru.vachok.ostpst.usermenu.traymenu;


import ru.vachok.ostpst.api.MessageToUser;
import ru.vachok.ostpst.api.MessengerOST;
import ru.vachok.ostpst.usermenu.MenuAWT;
import ru.vachok.ostpst.usermenu.UserMenu;
import ru.vachok.ostpst.utils.OstToPstException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.concurrent.Executors;


/**
 @since 29.05.2019 (9:34) */
public class TrayMenu implements UserMenu, Runnable {
    
    
    private String fileName;
    
    private MessageToUser messageToUser;
    
    public TrayMenu(String fileName, MessageToUser messageToUser) {
        this.fileName = fileName;
        this.messageToUser = messageToUser;
    }
    
    private TrayIcon trayIcon;
    
    public TrayMenu(String fileName) {
        this.fileName = fileName;
        this.messageToUser = new MessengerOST(getClass().getSimpleName());
        initDefault();
    }
    
    public void setDefault() {
        ImageIcon imageIcon = new ImageIcon(getClass().getResource("/img/trayicon.png"));
        Image searchImage = imageIcon.getImage();
        trayIcon.setImage(searchImage);
    }
    
    public void setSearching() {
        ImageIcon imageIcon = new ImageIcon(getClass().getResource("/img/search.png"));
        Image searchImage = imageIcon.getImage();
        trayIcon.setImage(searchImage);
    }
    
    @Override public void run() {
        showMenu();
    }
    
    @Override public void showMenu() {
        this.trayIcon = addTrayIcon();
        int numOfIcons = SystemTray.getSystemTray().getTrayIcons().length;
    
        if (SystemTray.isSupported() && numOfIcons == 0) {
            try {
                SystemTray.getSystemTray().add(trayIcon);
            }
            catch (AWTException e) {
                messageToUser.error(e.getMessage());
            }
        }
        else {
            throw new OstToPstException();
        }
    }
    
    private void initDefault() {
        ImageIcon imageIcon = new ImageIcon(getClass().getResource("/img/trayicon.png"));
        Image iconImage = imageIcon.getImage();
        trayIcon = new TrayIcon(iconImage);
    }
    
    private TrayIcon addTrayIcon() {
        setDefault();
        trayIcon.setToolTip("Push me!");
        trayIcon.setImageAutoSize(true);
        trayIcon.addActionListener(new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                System.out.println(fileName);
            }
        });
        trayIcon.setPopupMenu(getPopupMenu());
        return trayIcon;
    }
    
    private PopupMenu getPopupMenu() {
        PopupMenu popupMenu = new PopupMenu();
        MenuItem showAwt = new MenuItem("Show Main Window");
        MenuItem exitAwt = new MenuItem("Exit");
        
        showAwt.addActionListener(new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                Executors.newSingleThreadExecutor().execute(new MenuAWT());
            }
        });
        
        exitAwt.addActionListener(new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                exitProgram(fileName);
            }
        });
        
        popupMenu.add(showAwt);
        popupMenu.add(exitAwt);
        return popupMenu;
    }
}
