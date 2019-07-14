// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.systray.actions;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.accesscontrol.common.Common2Years25MbytesInfoCollector;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.restapi.message.MessageLocal;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


/**
 Action on Reload Context button
 <p>
 
 @see ru.vachok.networker.IntoApplication
 @since 25.01.2019 (13:30)
 */
public class ActionMakeInfoAboutOldCommonFiles extends AbstractAction {
    
    /**
     {@link MessageLocal}
     */
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Callable<String> infoCollector = new Common2Years25MbytesInfoCollector("files.old");
        Future futureInfo = AppComponents.threadConfig().getTaskExecutor().submit(infoCollector);
    
        try {
            Object infoString = futureInfo.get();
            if (infoString != null) {
                messageToUser.info(getClass().getSimpleName() + ConstantsFor.STR_ACTIONPERFORMED, "infoString", " = " + infoString);
            }
            else {
                throw new InvokeIllegalException("25.06.2019 (10:20)");
            }
        }
        catch (InterruptedException ex) {
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        catch (ExecutionException ex) {
            messageToUser.error(ex.getMessage());
        }
    }
    
}
