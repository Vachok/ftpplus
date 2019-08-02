// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.systray;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.accesscontrol.common.ArchivesAutoCleaner;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.Date;
import java.util.concurrent.*;


/**
 Action-class удаление временных файлов
 <p>
 
 @see ru.vachok.networker.systray.ActionDelTMPTest
 @since 25.01.2019 (9:26) */
class ActionDelTMP extends AbstractAction {
    
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ActionDelTMP.class.getSimpleName());
    
    private final ExecutorService executor;
    
    private final MenuItem delFiles;
    
    private final PopupMenu popupMenu;
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    ActionDelTMP(ExecutorService executor, MenuItem delFiles, PopupMenu popupMenu) {
        this.executor = executor;
        this.delFiles = delFiles;
        this.popupMenu = popupMenu;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Date date = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(ConstantsFor.ONE_YEAR));
        String msg = (new StringBuilder().append("starting clean for ").append(date).toString()).toUpperCase();
        delFiles.setLabel("Autoclean");
        popupMenu.add(delFiles);
        ArchivesAutoCleaner archivesAutoCleaner = ArchivesAutoCleaner.getInstance();
        Future<?> future = executor.submit(archivesAutoCleaner);
        try {
            future.get(9, TimeUnit.HOURS);
        }
        catch (InterruptedException | ExecutionException | TimeoutException ex) {
            messageToUser.error(MessageFormat.format("ActionDelTMP.actionPerformed: {0}, ({1})", ex.getMessage(), e.getClass().getName()));
        }
        LOGGER.info(msg);
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ActionDelTMP{");
        sb.append("executor=").append(executor.toString());
        sb.append('}');
        return sb.toString();
    }
}
