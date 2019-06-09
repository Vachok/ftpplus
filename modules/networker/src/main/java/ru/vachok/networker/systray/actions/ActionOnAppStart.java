// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.systray.actions;


import ru.vachok.networker.AppComponents;
import ru.vachok.networker.AppInfoOnLoad;
import ru.vachok.networker.exe.runnabletasks.SpeedChecker;
import ru.vachok.networker.systray.MessageToTray;
import ru.vachok.networker.systray.SystemTrayHelper;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


/**
 Actions on application start
 <p>
 
 @see ru.vachok.networker.AppInfoOnLoad
 @since 25.01.2019 (11:58) */
public class ActionOnAppStart extends AbstractAction {
    
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Callable<Long> longCallable = new SpeedChecker();
        Future<Long> submit = AppComponents.threadConfig().getTaskExecutor().submit(longCallable);
        String messageSW = null;
        try {
            DateFormat dateFormat = new SimpleDateFormat();
            messageSW = "When arrive: " + dateFormat.format(new Date(submit.get()));
        }
        catch (InterruptedException | ExecutionException ignore) {
            Thread.currentThread().interrupt();
        }
        new MessageToTray(new ActionCloseMsg(Objects.requireNonNull(SystemTrayHelper.getI()).getTrayIcon())).info(getClass().getSimpleName(), new AppInfoOnLoad().getIISLogSize(),
            messageSW);
    }
}
