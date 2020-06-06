// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.systray.actions;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ExitApp;
import ru.vachok.networker.componentsrepo.systray.SystemTrayHelper;
import ru.vachok.networker.data.NetKeeper;
import ru.vachok.networker.data.enums.ConstantsFor;

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
public class ActionExit extends AbstractAction {


    private static final String ALLDEV_MAP = "alldev.map";

    private final String reason;

    private final transient MessageToUser messageToUser = ru.vachok.networker.restapi.message.MessageToUser
        .getInstance(ru.vachok.networker.restapi.message.MessageToUser.LOCAL_CONSOLE, ActionExit.class.getSimpleName());

    public ActionExit(String reason) {
        this.reason = reason;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        messageToUser.infoNoTitles(getClass().getSimpleName() + ConstantsFor.STR_ACTIONPERFORMED);
        try (FileOutputStream fileOutputStream = new FileOutputStream(ALLDEV_MAP)) {
            new ExitApp(reason, fileOutputStream, NetKeeper.class).run();
        }
        catch (Error | IOException ex) {
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
            System.exit(ConstantsFor.EXIT_STATUSBAD);
        }
    }
}
