// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.systray.actions;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.ExitApp;
import ru.vachok.networker.enums.FileNames;
import ru.vachok.networker.net.NetKeeper;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.systray.SystemTrayHelper;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.FileOutputStream;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


/**
 Action Exit App
 <p>

 @see SystemTrayHelper
 @since 25.01.2019 (9:59) */
@SuppressWarnings("ClassHasNoToStringMethod") public class ActionExit extends AbstractAction {


    private String reason;

    private transient MessageToUser messageToUser = new MessageLocal(ActionExit.class.getSimpleName());
    
    
    public ActionExit(String reason) {
        this.reason = reason;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        messageToUser.infoNoTitles(getClass().getSimpleName() + ConstantsFor.STR_ACTIONPERFORMED);
        try (FileOutputStream fileOutputStream = new FileOutputStream(FileNames.FILENAME_ALLDEVMAP)) {
            Future<?> submit = AppComponents.threadConfig().getTaskExecutor().submit(new ExitApp(reason, fileOutputStream, NetKeeper.class));
            submit.get(ConstantsFor.DELAY , TimeUnit.SECONDS);
        } catch (Exception ex) {
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
            System.exit(ConstantsFor.EXIT_STATUSBAD);
        }
    }
}
