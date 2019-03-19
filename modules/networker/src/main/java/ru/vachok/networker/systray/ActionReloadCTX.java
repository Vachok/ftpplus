package ru.vachok.networker.systray;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.ExitApp;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.concurrent.*;


/**
 Action on Reload Context button
 <p>

 @see ru.vachok.networker.IntoApplication
 @since 25.01.2019 (13:30) */
class ActionReloadCTX extends AbstractAction {
    
    
    /**
     {@link MessageLocal}
     */
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());

    private static final String[] ARGS = new String[0];

    @Override
    public void actionPerformed(ActionEvent e) {
        ExitApp exitApp = new ExitApp(getClass().getSimpleName());
        Future<?> submit = Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).submit(() -> exitApp.reloadCTX());
        try {
            submit.get(ConstantsFor.DELAY, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e1) {
            messageToUser.errorAlert("ActionReloadCTX", "actionPerformed", e1.getMessage());
            FileSystemWorker.error("ActionReloadCTX.actionPerformed", e1);
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ActionReloadCTX{");
        sb.append("ARGS=").append(Arrays.toString(ARGS));
        sb.append('}');
        return sb.toString();
    }
}
