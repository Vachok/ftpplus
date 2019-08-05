// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.systray.actions;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.accesscontrol.common.OldBigFilesInfoCollector;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
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
    
    private String fileName = ConstantsFor.FILENAME_OLDCOMMON;
    
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
    
    protected void makeAction() {
        Callable<String> infoCollector = new OldBigFilesInfoCollector(fileName);
        Future futureInfo = AppComponents.threadConfig().getTaskExecutor().submit(infoCollector);
        try {
            Object infoString = futureInfo.get(timeoutSeconds, TimeUnit.SECONDS);
            if (infoString != null) {
                messageToUser.info(getClass().getSimpleName() + ConstantsFor.STR_ACTIONPERFORMED, "infoString", " = " + infoString);
            }
            else {
                throw new InvokeIllegalException("25.06.2019 (10:20)");
            }
        }
        catch (InterruptedException | TimeoutException ex) {
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        catch (ExecutionException ex) {
            messageToUser.error(ex.getMessage());
        }
    }
}
