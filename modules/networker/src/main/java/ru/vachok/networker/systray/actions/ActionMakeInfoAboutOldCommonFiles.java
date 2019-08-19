// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.systray.actions;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.accesscontrol.common.OldBigFilesInfoCollector;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.enums.FileNames;
import ru.vachok.networker.restapi.message.MessageLocal;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.StringJoiner;
import java.util.concurrent.*;


/**
 Action on Reload Context button
 <p>
 
 @see ru.vachok.networker.systray.actions.ActionMakeInfoAboutOldCommonFilesTest
 @since 25.01.2019 (13:30)
 */
public class ActionMakeInfoAboutOldCommonFiles extends AbstractAction {
    
    /**
     {@link MessageLocal}
     */
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    private long timeoutSeconds;
    
    private String fileName = FileNames.FILENAME_OLDCOMMON;
    
    public void setTimeoutSeconds(long timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
        this.fileName = fileName + ".t";
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        makeAction();
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", ActionMakeInfoAboutOldCommonFiles.class.getSimpleName() + "[\n", "\n]")
            .toString();
    }
    
    protected String makeAction() {
        Callable<String> infoCollector = new OldBigFilesInfoCollector(fileName);
        Future<String> submit = Executors.newSingleThreadExecutor().submit(infoCollector);
        try {
            return submit.get(timeoutSeconds, TimeUnit.SECONDS);
        }
        catch (InterruptedException | ExecutionException e) {
            messageToUser.error(e.getMessage());
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
            throw new InvokeIllegalException(getClass().getSimpleName() + " FAILED");
        }
        catch (TimeoutException e) {
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
            throw new InvokeIllegalException("TIMEOUT " + timeoutSeconds);
        }
    }
}
