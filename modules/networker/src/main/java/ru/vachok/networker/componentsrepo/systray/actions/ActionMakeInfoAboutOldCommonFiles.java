// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.systray.actions;


import ru.vachok.networker.IntoApplication;
import ru.vachok.networker.ad.common.OldBigFilesInfoCollector;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.restapi.props.InitProperties;
import ru.vachok.networker.sysinfo.AppConfigurationLocal;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.StringJoiner;


/**
 Action on Reload Context button
 <p>

 @see ActionMakeInfoAboutOldCommonFilesTest
 @since 25.01.2019 (13:30) */
public class ActionMakeInfoAboutOldCommonFiles extends AbstractAction {


    /**
     {@link MessageLocal}
     */
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, ActionMakeInfoAboutOldCommonFiles.class.getSimpleName());

    private String fileName = FileNames.FILES_OLD;

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setTimeoutSeconds(long timeoutSeconds) {
        this.fileName = fileName + ".t";
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        makeAction();
    }

    protected String makeAction() {
        OldBigFilesInfoCollector infoCollector = (OldBigFilesInfoCollector) IntoApplication.getConfigurableApplicationContext()
                .getBean(OldBigFilesInfoCollector.class.getSimpleName());
        infoCollector.setStartPath(InitProperties.getInstance(InitProperties.DB_MEMTABLE).getProps().getProperty("oldcleanpath"));
        AppConfigurationLocal.getInstance().execute(infoCollector);
        return infoCollector.getFromDatabase();
    }

    @Override
    public String toString() {
        return new StringJoiner(",\n", ActionMakeInfoAboutOldCommonFiles.class.getSimpleName() + "[\n", "\n]")
            .toString();
    }
}
