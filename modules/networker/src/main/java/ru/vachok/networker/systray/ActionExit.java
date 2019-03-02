package ru.vachok.networker.systray;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.ExitApp;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.net.NetListKeeper;
import ru.vachok.networker.services.MessageLocal;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 Action Exit App
 <p>

 @see SystemTrayHelper
 @since 25.01.2019 (9:59) */
@SuppressWarnings("ClassHasNoToStringMethod")
class ActionExit extends AbstractAction {

    private String reason;

    private transient MessageToUser messageToUser = new MessageLocal();

    ActionExit(String reason) {
        this.reason = reason;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        messageToUser.infoNoTitles(getClass().getSimpleName() + ".actionPerformed");
        try (FileOutputStream fileOutputStream = new FileOutputStream(NetListKeeper.class.getSimpleName() + ".ser")) {
            AppComponents.threadConfig().executeAsThread(new ExitApp(reason, fileOutputStream, NetListKeeper.class));
        } catch (IOException ex) {
            messageToUser.errorAlert("ActionExit", ConstantsFor.METHNAME_ACTIONPERFORMED, ex.getMessage());
        }
    }
}
