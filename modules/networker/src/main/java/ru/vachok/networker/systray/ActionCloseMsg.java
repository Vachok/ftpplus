package ru.vachok.networker.systray;


import org.slf4j.Logger;
import ru.vachok.networker.componentsrepo.AppComponents;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 @since 29.01.2019 (12:21) */
public class ActionCloseMsg extends AbstractAction {

    private transient TrayIcon trayIcon;

    /**
     Creates an {@code Action}.@param LOGGER
     */
    public ActionCloseMsg(Logger LOGGER) {
        LOGGER.warn("ActionCloseMsg.ActionCloseMsg");
    }

    ActionCloseMsg(TrayIcon trayIcon) {
        this.trayIcon = trayIcon;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Logger l = AppComponents.getLogger();
        l.warn("ActionCloseMsg.actionPerformed");
        ActionListener[] actionListeners = trayIcon.getActionListeners();
        for (ActionListener actionListener : actionListeners) {
            trayIcon.removeActionListener(actionListener);
            l.info(actionListener.getClass().getSimpleName(), " removed");
        }
        trayIcon.addActionListener(new ActionDefault());
    }
}
