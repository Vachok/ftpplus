package ru.vachok.networker.systray;


import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.TimeChecker;

import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Date;


/**
 Сообщения с учётом "локальных" особенностей
 <p>

 @since 21.01.2019 (23:46) */
public class MessageToTray implements MessageToUser {

    private ActionListener aListener;

    /**
     {@link SystemTrayHelper#getTrayIcon()}
     */
    private TrayIcon trayIcon = SystemTrayHelper.getTrayIcon();

    private String headerMsg = ConstantsFor.APP_NAME.replace('-', ' ');

    private String titleMsg = new Date(new TimeChecker().call().getReturnTime()).toString();

    private String bodyMsg = "No body";

    public MessageToTray() {
        this.aListener = new ActionDefault();
        delActions();
    }

    public void errorAlert(String bodyMsg) {
        this.bodyMsg = bodyMsg;
        errorAlert(headerMsg, titleMsg, bodyMsg);
    }

    public MessageToTray(ActionListener aListener) {
        this.aListener = aListener;
        delActions();
    }

    @Override
    public void errorAlert(String headerMsg, String titleMsg, String bodyMsg) {
        this.headerMsg = headerMsg;
        this.titleMsg = titleMsg;
        this.bodyMsg = bodyMsg;
        if(SystemTray.isSupported()){
            trayIcon.addActionListener(aListener);
            trayIcon.displayMessage(headerMsg, titleMsg + " " + bodyMsg, TrayIcon.MessageType.ERROR);
        }
        else{
            new MessageCons().errorAlert(headerMsg, titleMsg, bodyMsg);
        }
        FileSystemWorker.recFile(headerMsg + ConstantsFor.LOG, Collections.singletonList((titleMsg + "\n\n" + bodyMsg)));
    }

    @Override
    public void info(String headerMsg, String titleMsg, String bodyMsg) {
        this.headerMsg = headerMsg;
        this.titleMsg = titleMsg;
        this.bodyMsg = bodyMsg;
        if(SystemTray.isSupported()){
            trayIcon.addActionListener(aListener);
            trayIcon.displayMessage(headerMsg, titleMsg + " " + bodyMsg, TrayIcon.MessageType.INFO);
        }
        else{
            new MessageCons().info(headerMsg, titleMsg, bodyMsg);
        }
    }

    @Override
    public void infoNoTitles(String bodyMsg) {
        this.bodyMsg = bodyMsg;
        if(SystemTray.isSupported() && trayIcon!=null){
            trayIcon.displayMessage(headerMsg, bodyMsg, TrayIcon.MessageType.INFO);
            trayIcon.addActionListener(aListener);
        }
        else{
            new MessageCons().info(headerMsg, titleMsg, bodyMsg);
        }
    }

    private void delActions() {
        ActionListener[] actionListeners = trayIcon.getActionListeners();
        if (actionListeners.length > 0) {
            for (ActionListener a : actionListeners) {
                trayIcon.removeActionListener(a);
                AppComponents.getLogger().info(a.getClass().getSimpleName() + " removed");
            }
        } else {
            new MessageCons().info(getClass().getSimpleName(), "delActions", "actionListeners.length is 0");
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