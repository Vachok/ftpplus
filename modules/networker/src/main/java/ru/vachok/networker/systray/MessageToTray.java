package ru.vachok.networker.systray;


import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Collections;


/**
 Сообщения с учётом "локальных" особенностей
 <p>

 @since 21.01.2019 (23:46) */
public class MessageToTray implements MessageToUser {

    private final ActionListener aListener;

    /**
     {@link SystemTrayHelper#getTrayIcon()}
     */
    private final TrayIcon trayIcon = SystemTrayHelper.getTrayIcon();

    public MessageToTray() {
        this.aListener = new ActionDefault();
        delActions();
    }

    private void delActions() {
        ActionListener[] actionListeners = trayIcon.getActionListeners();
        if(actionListeners.length > 0){
            for(ActionListener a : actionListeners){
                trayIcon.removeActionListener(a);
            }
        }
        else{
            new MessageCons().info(getClass().getSimpleName(), "delActions", "actionListeners.length is 0");
        }
    }

    public MessageToTray(ActionListener aListener) {
        this.aListener = aListener;
        delActions();
    }

    @Override
    public void errorAlert(String s, String s1, String s2) {
        if(SystemTray.isSupported()){
            trayIcon.addActionListener(aListener);
            trayIcon.displayMessage(s, s1 + " " + s2, TrayIcon.MessageType.ERROR);
        }
        else{
            new MessageCons().errorAlert(s, s1, s2);
        }
        FileSystemWorker.recFile(s + ConstantsFor.LOG, Collections.singletonList((s1 + "\n\n" + s2)));
    }

    @Override
    public void info(String s, String s1, String s2) {
        if(SystemTray.isSupported()){
            trayIcon.addActionListener(aListener);
            trayIcon.displayMessage(s, s1 + " " + s2, TrayIcon.MessageType.INFO);
        }
        else{
            new MessageCons().info(s, s1, s2);
        }
    }

    @Override
    public void infoNoTitles(String s) {
        if(SystemTray.isSupported() && trayIcon!=null){
            trayIcon.displayMessage("FYI", s, TrayIcon.MessageType.INFO);
            trayIcon.addActionListener(aListener);
        }
        else{
            new MessageCons().infoNoTitles(s);
        }
    }

    @Override
    public void infoTimer(int i, String s) {
        throw new UnsupportedOperationException("Not impl to " + getClass().getSimpleName());
    }

    @Override
    public String confirm(String s, String s1, String s2) {
        throw new UnsupportedOperationException("Not impl to " + getClass().getSimpleName());

    }
}