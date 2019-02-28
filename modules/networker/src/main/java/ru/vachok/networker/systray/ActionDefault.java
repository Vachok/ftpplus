package ru.vachok.networker.systray;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.fileworks.FileSystemWorker;
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

// --Commented out by Inspection START (27.02.2019 12:42):
//    /**
//     {@link AppComponents#getLogger()}
//     */
//    private static final Logger LOGGER = LoggerFactory.getLogger(ActionDefault.class.getSimpleName());
// --Commented out by Inspection STOP (27.02.2019 12:42)

    private static MessageToUser messageToUser = new MessageLocal();

    private static final SystemTrayHelper SYSTEM_TRAY_HELPER = SystemTrayHelper.getI();

    private String goTo;

    public ActionDefault(String goTo) {
        this.goTo = goTo;
    }

    ActionDefault() {
        this.goTo = ConstantsFor.HTTP_LOCALHOST8880SLASH;
        if(ConstantsFor.IS_SYSTRAY_AVAIL && SYSTEM_TRAY_HELPER.getTrayIcon()!=null){
            SYSTEM_TRAY_HELPER.delOldActions();
            messageToUser.info("ActionDefault.ActionDefault",
                "SystemTrayHelper.getTrayIcon().getActionListeners().length",
                " = " + SYSTEM_TRAY_HELPER.getTrayIcon().getActionListeners().length);
        }
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        try{
            Desktop.getDesktop().browse(URI.create(goTo));

        }
        catch(IOException e1){
            messageToUser.errorAlert("ActionDefault", ConstantsFor.METHNAME_ACTIONPERFORMED, e1.getMessage());
            FileSystemWorker.error("ActionDefault.actionPerformed", e1);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ActionDefault{");
        sb.append("goTo='").append(goTo).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
