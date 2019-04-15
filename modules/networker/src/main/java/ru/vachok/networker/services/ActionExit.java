package ru.vachok.networker.services;


import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.systray.SystemTrayHelper;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.concurrent.*;


/**
 Action Exit App
 <p>
 
 @see SystemTrayHelper
 @since 25.01.2019 (9:59) */
@SuppressWarnings("ClassHasNoToStringMethod")
public class ActionExit extends AbstractAction {
    
    
    private Runnable exitInstructions;
    
    private transient MessageToUser messageToUser = new MessageCons(ActionExit.class.getSimpleName());
    
    public ActionExit(Runnable toExecute) {
        this.exitInstructions = toExecute;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        messageToUser.infoNoTitles(getClass().getSimpleName() + ".actionPerformed");
        Future<?> submit = Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).submit(exitInstructions);
        try {
            submit.get(100, TimeUnit.SECONDS);
        }
        catch (InterruptedException | ExecutionException | TimeoutException ex) {
            messageToUser.error(ex.getMessage());
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
    }
}
