// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.message;


import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.systray.SystemTrayHelper;

import java.awt.*;
import java.awt.event.ActionListener;


/**
 @see MessageToTrayTest
 @since 21.01.2019 (23:46) */
public class MessageToTray implements MessageToUser {
    
    
    private ActionListener aListener;
    
    /**
     {@link SystemTrayHelper#getTrayIcon()}
     */
    private TrayIcon trayIcon;
    
    private static MessageToTray messageToTray = new MessageToTray("Single");
    
    private String titleMsg = this.hashCode() + " hash of " + this.getClass().getTypeName();
    
    private String headerMsg;
    
    private String bodyMsg = InformationFactory.getRunningInformation();
    
    public static MessageToTray getInstance(String headerMsg) {
        messageToTray.setHeaderMsg(headerMsg);
        return messageToTray;
    }
    
    private MessageToUser messageToUser = new MessageLocal(MessageToTray.class.getSimpleName());
    
    public MessageToTray(String simpleName) {
        this.headerMsg = simpleName;
    }
    
    public void setHeaderMsg(String headerMsg) {
        this.headerMsg = headerMsg;
    }
    
    public void errorAlert(String bodyMsg) {
        this.bodyMsg = bodyMsg;
        errorAlert(headerMsg, titleMsg, bodyMsg);
    }
    
    @Override
    public String confirm(String s, String s1, String s2) {
        throw new InvokeIllegalException("Not impl to " + getClass().getSimpleName());
        
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MessageToTray{");
        try {
            sb.append("aListener=").append(aListener);
        }
        catch (RuntimeException e) {
            sb.append(e.getMessage());
        }
        sb.append(", trayIcon=").append(trayIcon);
        sb.append(", headerMsg='").append(headerMsg).append('\'');
        sb.append(", titleMsg='").append(titleMsg).append('\'');
        sb.append(", bodyMsg='").append(bodyMsg).append('\'');
        sb.append(", messageToUser=").append(messageToUser);
        sb.append('}');
        return sb.toString();
    }
    
    @Override
    public void info(String headerMsg, String titleMsg, String bodyMsg) {
        this.headerMsg = headerMsg;
        this.titleMsg = titleMsg;
        this.bodyMsg = bodyMsg;
        if (SystemTray.isSupported() && SystemTrayHelper.getI().isPresent()) {
            SystemTrayHelper systemTrayHelper = (SystemTrayHelper) SystemTrayHelper.getI().get();
            this.trayIcon = systemTrayHelper.getTrayIcon();
            trayIcon.addActionListener(aListener);
            trayIcon.displayMessage(headerMsg, titleMsg + " " + bodyMsg, TrayIcon.MessageType.INFO);
        }
        else {
            messageToUser.info(headerMsg, titleMsg, bodyMsg);
        }
    }
    
    @Override
    public void error(String s) {
        errorAlert(s);
    }
    
    @Override
    public void error(String s, String s1, String s2) {
        errorAlert(s, s1, s2);
    }
    
    @Override
    public void errorAlert(String headerMsg, String titleMsg, String bodyMsg) {
        this.headerMsg = headerMsg;
        this.titleMsg = titleMsg;
        this.bodyMsg = bodyMsg;
        if (SystemTray.isSupported() && trayIcon != null) {
            trayIcon.addActionListener(aListener);
            trayIcon.displayMessage(headerMsg, titleMsg + " " + bodyMsg, TrayIcon.MessageType.ERROR);
        }
        else {
            messageToUser.errorAlert(headerMsg, titleMsg, bodyMsg);
        }
    }
    
    private void delActions() {
        if (trayIcon != null && trayIcon.getActionListeners().length > 0) {
            ActionListener[] actionListeners = trayIcon.getActionListeners();
            for (ActionListener a : actionListeners) {
                trayIcon.removeActionListener(a);
                messageToUser.infoNoTitles(a.getClass().getSimpleName() + " removed");
            }
        }
        else {
            messageToUser.info(getClass().getSimpleName(), "delActions", "actionListeners.length is 0");
        }
    }
    
    @Override
    public void infoNoTitles(String bodyMsg) {
        this.bodyMsg = bodyMsg;
        if (SystemTray.isSupported() && trayIcon != null) {
            trayIcon.displayMessage(headerMsg, bodyMsg, TrayIcon.MessageType.INFO);
            trayIcon.addActionListener(aListener);
        }
        else {
            messageToUser.info(headerMsg, titleMsg, bodyMsg);
        }
    }
    
    @Override
    public void info(String s) {
        infoNoTitles(s);
    }
    
    @Override
    public void infoTimer(int i, String s) {
        throw new UnsupportedOperationException("Not impl to " + getClass().getSimpleName());
    }
    
    @Override
    public void warn(String s, String s1, String s2) {
        this.headerMsg = s;
        this.titleMsg = s1;
        this.bodyMsg = s2;
        if (SystemTray.isSupported() && trayIcon != null) {
            trayIcon.addActionListener(aListener);
            trayIcon.displayMessage(headerMsg, titleMsg + " " + bodyMsg, TrayIcon.MessageType.WARNING);
        }
        else {
            messageToUser.errorAlert(headerMsg, titleMsg, bodyMsg);
        }
    }
    
    @Override
    public void warn(String s) {
        this.bodyMsg = s;
        warn(headerMsg, titleMsg, s);
    }
    
    @Override
    public void warning(String s, String s1, String s2) {
        warn(s, s1, s2);
    }
    
    @Override
    public void warning(String s) {
        warn(s);
    }
    
    
}