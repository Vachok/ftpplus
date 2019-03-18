package ru.vachok.networker.systray;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.services.MessageLocal;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;


/**
 Default Tray Action
 <p>

 @see SystemTrayHelper
 @since 25.01.2019 (9:56) */
public class ActionDefault extends AbstractAction {

    /**
     {@link MessageLocal}
     */
    private static MessageToUser messageToUser = new MessageLocal(ActionDefault.class.getSimpleName());

    private static final SystemTrayHelper SYSTEM_TRAY_HELPER = SystemTrayHelper.getI();

    private String goTo;

    public ActionDefault(String goTo) {
        this.goTo = goTo;
    }

    ActionDefault() {
        this.goTo = ConstantsFor.HTTP_LOCALHOST8880SLASH;
        if(ConstantsFor.IS_SYSTRAY_AVAIL && SYSTEM_TRAY_HELPER.getTrayIcon()!=null){
            SYSTEM_TRAY_HELPER.delOldActions();
        }
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        try{
            Desktop.getDesktop().browse(URI.create(goTo));
        } catch (IOException | IllegalArgumentException e1) {
            messageToUser.errorAlert("ActionDefault", "actionPerformed", e1.getMessage());
        }
        Thread.currentThread().checkAccess();
        Thread.currentThread().interrupt();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ActionDefault{");
        sb.append("goTo='").append(goTo).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
