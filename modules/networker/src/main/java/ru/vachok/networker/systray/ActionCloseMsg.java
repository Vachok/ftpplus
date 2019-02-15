package ru.vachok.networker.systray;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.net.MoreInfoGetter;
import ru.vachok.networker.net.NetScannerSvc;
import ru.vachok.networker.services.MessageLocal;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;


/**
 Убивает из {@link #trayIcon} все экшн-листенеры.
 <p>
 {@link MoreInfoGetter}, {@link NetScannerSvc}, {@link ActionOnAppStart}

 @since 29.01.2019 (12:21) */
public class ActionCloseMsg extends AbstractAction {

    /**
     {@link TrayIcon}
     */
    private transient TrayIcon trayIcon = null;

    /**
     Creates an {@code Action}.

     @param messageToUser {@link MessageToUser}.
     */
    public ActionCloseMsg(MessageToUser messageToUser) {
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
        MessageToUser l = new MessageLocal();
        l.infoNoTitles("ActionCloseMsg.actionPerformed");
        if(trayIcon!=null && trayIcon.getActionListeners().length > 0){
            ActionListener[] actionListeners = trayIcon.getActionListeners();
            for(ActionListener actionListener : actionListeners){
                trayIcon.removeActionListener(actionListener);
                l.info(actionListener.getClass().getSimpleName(), " removed...", actionListeners.length + " listeners left.");
            }
            trayIcon.addActionListener(new ActionDefault());
        }
    }
}
