package ru.vachok.networker.systray;


import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.services.TimeChecker;

import java.awt.*;
import java.awt.event.ActionListener;
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
        delActions();
        this.aListener = new ActionDefault();
    }

    public void errorAlert(String bodyMsg) {
        this.bodyMsg = bodyMsg;
        errorAlert(headerMsg, titleMsg, bodyMsg);
    }

    public MessageToTray(ActionListener aListener) {
        if(SystemTray.isSupported() || SystemTray.getSystemTray()!=null){
            this.aListener = aListener;
        }

    }

    @Override
    public void errorAlert(String headerMsg, String titleMsg, String bodyMsg) {
        this.headerMsg = headerMsg;
        this.titleMsg = titleMsg;
        this.bodyMsg = bodyMsg;
        if (SystemTray.isSupported() && trayIcon.equals(SystemTrayHelper.getTrayIcon())) {
            trayIcon.addActionListener(aListener);
            trayIcon.displayMessage(headerMsg, titleMsg + " " + bodyMsg, TrayIcon.MessageType.ERROR);
        }
        else{
            new MessageCons().errorAlert(headerMsg, titleMsg, bodyMsg);
        }
    }

    @Override
    public void info(String headerMsg, String titleMsg, String bodyMsg) {
        this.headerMsg = headerMsg;
        this.titleMsg = titleMsg;
        this.bodyMsg = bodyMsg;
        if(SystemTray.isSupported() && SystemTrayHelper.getTrayIcon()!=null){
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MessageToTray{");
        sb.append("aListener=").append(aListener.hashCode());
        sb.append(", bodyMsg='").append(bodyMsg).append('\'');
        sb.append(", headerMsg='").append(headerMsg).append('\'');
        sb.append(", titleMsg='").append(titleMsg).append('\'');
        sb.append(", trayIcon=").append(SystemTray.isSupported() && trayIcon.equals(SystemTrayHelper.getTrayIcon()));
        sb.append('}');
        return sb.toString();
    }
}