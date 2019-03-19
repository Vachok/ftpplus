package ru.vachok.networker.systray;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ExitApp;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.services.MessageLocal;

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
class ActionExit extends AbstractAction {
    
    
    private String reason;
    
    private transient MessageToUser messageToUser = new MessageLocal();
    
    ActionExit(String reason) {
        this.reason = reason;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        messageToUser.infoNoTitles(getClass().getSimpleName() + ".actionPerformed");
        try (FileOutputStream fileOutputStream = new FileOutputStream("alldev.map")) {
            Future<?> submit = AppComponents.threadConfig().getTaskExecutor().submit(new ExitApp(reason, fileOutputStream, ConstantsNet.getAllDevices()));
            submit.get(30, TimeUnit.SECONDS);
        } catch (IOException | InterruptedException | ExecutionException | TimeoutException ex) {
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
            System.exit(666);
        }
    }
}
