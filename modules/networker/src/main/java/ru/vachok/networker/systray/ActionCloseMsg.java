package ru.vachok.networker.systray;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.net.MoreInfoWorker;
import ru.vachok.networker.net.NetScannerSvc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;


/**
 Убивает из {@link #trayIcon} все экшн-листенеры.
 <p>
 {@link MoreInfoWorker}, {@link NetScannerSvc}, {@link ActionOnAppStart}

 @since 29.01.2019 (12:21) */
public class ActionCloseMsg extends AbstractAction {

    /**
     {@link TrayIcon}
     */
    private transient TrayIcon trayIcon;

    private transient MessageToUser messageToUser;

    /**
     Creates an {@code Action}.

     @param messageToUser {@link MessageToUser}.
     */
    public ActionCloseMsg(MessageToUser messageToUser) {
        this.messageToUser = messageToUser;
        messageToUser.errorAlert("ActionCloseMsg.ActionCloseMsg", "", new Date().toString());
    }

    /**
     Конструктор по-умолчанию.
     <p>

     @param trayIcon {@link #trayIcon}
     */
    ActionCloseMsg(TrayIcon trayIcon) {
        this.trayIcon = trayIcon;
    }

    /**
     Действие.
     <p>
     1. {@link ActionDefault#ActionDefault()} - добавим, после очистки {@link ActionListener}[].

     @param e предпринятый экшн.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if(trayIcon!=null && trayIcon.getActionListeners().length > 0){
            ActionListener[] actionListeners = trayIcon.getActionListeners();
            for(ActionListener actionListener : actionListeners){
                trayIcon.removeActionListener(actionListener);
                messageToUser.info(actionListener.getClass().getSimpleName(), " removed...", actionListeners.length + " listeners left.");
            }
            trayIcon.addActionListener(new ActionDefault());
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ActionCloseMsg{");
        sb.append("trayIcon=").append(trayIcon.getActionListeners() != null);
        sb.append(ConstantsFor.TOSTRING_MESSAGE_TO_USER).append(messageToUser);
        sb.append('}');
        return sb.toString();
    }
}
