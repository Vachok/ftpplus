package ru.vachok.networker.systray;


import org.slf4j.Logger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.accesscontrol.common.ArchivesAutoCleaner;
import ru.vachok.networker.componentsrepo.AppComponents;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 Action-class удаление временных файлов
 <p>

 @see SystemTrayHelper
 @since 25.01.2019 (9:26) */
class ActionDelTMP extends AbstractAction {

    private static final Logger LOGGER = AppComponents.getLogger();

    private final ThreadPoolTaskExecutor executor;

    private final MenuItem delFiles;

    private final PopupMenu popupMenu;

    ActionDelTMP(ThreadPoolTaskExecutor executor, MenuItem delFiles, PopupMenu popupMenu) {
        this.executor = executor;
        this.delFiles = delFiles;
        this.popupMenu = popupMenu;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        Date date = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(ConstantsFor.ONE_YEAR));
        String msg = (new StringBuilder().append("starting clean for ").append(date).toString()).toUpperCase();

        executor.setThreadGroup(new ThreadGroup(("CLR")));
        executor.setThreadNamePrefix("CLEAN");
        executor.setThreadNamePrefix(date + "-");
        executor.execute(new ArchivesAutoCleaner());

        delFiles.setLabel("Autoclean");
        popupMenu.add(delFiles);

        LOGGER.info(msg);
    }
}
