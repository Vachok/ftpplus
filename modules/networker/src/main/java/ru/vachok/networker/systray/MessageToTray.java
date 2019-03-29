package ru.vachok.networker.systray;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.services.MessageLocal;
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

    private static final SystemTrayHelper SYSTEM_TRAY_HELPER = SystemTrayHelper.getI();

    /**
     {@link SystemTrayHelper#getTrayIcon()}
     */
    private TrayIcon trayIcon = SYSTEM_TRAY_HELPER.getTrayIcon();

    private String headerMsg = ConstantsFor.APPNAME_WITHMINUS.replace('-', ' ');

    private String titleMsg = new Date(new TimeChecker().call().getReturnTime()).toString();

    private String bodyMsg = "No body";

    private MessageToUser messageToUser = new MessageLocal(MessageToTray.class.getSimpleName());

    public MessageToTray() throws NullPointerException, IllegalStateException {
        if (!ConstantsFor.IS_SYSTRAY_AVAIL) {
            throw new UnsupportedOperationException("***System Tray not Available!***");
        }
    }

    public MessageToTray(ActionListener aListener) throws HeadlessException, IllegalStateException {
        if (ConstantsFor.IS_SYSTRAY_AVAIL && SYSTEM_TRAY_HELPER.getTrayIcon() != null) {
            delActions();
            this.aListener = aListener;
        }
        else{
            throw new UnsupportedOperationException("***System Tray not Available!***");
        }
    }


    public MessageToTray( String simpleName ) {
        this.headerMsg = simpleName;
    }


    @Override
    public void error(String s) {
        errorAlert(s);
    }

    @Override
    public void error(String s, String s1, String s2) {
        errorAlert(s, s1, s2);
    }
    private void delActions() {
        if(trayIcon!=null && trayIcon.getActionListeners().length > 0){
            ActionListener[] actionListeners = trayIcon.getActionListeners();
            for(ActionListener a : actionListeners){
                trayIcon.removeActionListener(a);
                messageToUser.infoNoTitles(a.getClass().getSimpleName() + " removed");
            }
        }
        else{
            messageToUser.info(getClass().getSimpleName(), "delActions", "actionListeners.length is 0");
        }
    }

    public void errorAlert(String bodyMsg) {
        this.bodyMsg = bodyMsg;
        errorAlert(headerMsg, titleMsg, bodyMsg);
    }

    @Override
    public void errorAlert(String headerMsg, String titleMsg, String bodyMsg) {
        this.headerMsg = headerMsg;
        this.titleMsg = titleMsg;
        this.bodyMsg = bodyMsg;
        if(SystemTray.isSupported() && trayIcon.equals(SYSTEM_TRAY_HELPER.getTrayIcon())){
            trayIcon.addActionListener(aListener);
            trayIcon.displayMessage(headerMsg, titleMsg + " " + bodyMsg, TrayIcon.MessageType.ERROR);
        }
        else{
            messageToUser.errorAlert(headerMsg, titleMsg, bodyMsg);
        }
    }

    @Override
    public void info(String headerMsg, String titleMsg, String bodyMsg) {
        this.headerMsg = headerMsg;
        this.titleMsg = titleMsg;
        this.bodyMsg = bodyMsg;
        if(SystemTray.isSupported() && SYSTEM_TRAY_HELPER.getTrayIcon()!=null){
            trayIcon.addActionListener(aListener);
            trayIcon.displayMessage(headerMsg, titleMsg + " " + bodyMsg, TrayIcon.MessageType.INFO);
        }
        else{
            messageToUser.info(headerMsg, titleMsg, bodyMsg);
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
        if(ConstantsFor.IS_SYSTRAY_AVAIL && trayIcon!=null){
            trayIcon.addActionListener(aListener);
            trayIcon.displayMessage(headerMsg, titleMsg + " " + bodyMsg, TrayIcon.MessageType.WARNING);
        }
        else{
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
        sb.append('}');
        return sb.toString();
    }
}