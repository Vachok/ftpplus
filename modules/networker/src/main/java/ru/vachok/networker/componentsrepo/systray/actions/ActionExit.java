// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.systray.actions;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ExitApp;
import ru.vachok.networker.componentsrepo.systray.SystemTrayHelper;
import ru.vachok.networker.data.NetKeeper;
import ru.vachok.networker.data.enums.ConstantsFor;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 Action Exit App
 <p>

 @see SystemTrayHelper
 @since 25.01.2019 (9:59) */
@SuppressWarnings("ClassHasNoToStringMethod")
public class ActionExit extends AbstractAction {


    public static final String ALLDEV_MAP = "alldev.map";

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
            Future<?> submit = AppComponents.threadConfig().getTaskExecutor().submit(new ExitApp(reason, fileOutputStream, NetKeeper.class));
            submit.get(ConstantsFor.DELAY, TimeUnit.SECONDS);
        }
        catch (Error | IOException | InterruptedException | TimeoutException | ExecutionException ex) {
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
            System.exit(ConstantsFor.EXIT_STATUSBAD);
        }
    }
}
