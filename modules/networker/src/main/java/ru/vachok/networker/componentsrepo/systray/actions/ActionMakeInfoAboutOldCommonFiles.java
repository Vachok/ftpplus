// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.systray.actions;


import org.springframework.beans.BeansException;
import org.springframework.context.ConfigurableApplicationContext;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.IntoApplication;
import ru.vachok.networker.ad.common.OldBigFilesInfoCollector;
import ru.vachok.networker.data.enums.FileNames;
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
        try (ConfigurableApplicationContext context = IntoApplication.getContext()) {
            OldBigFilesInfoCollector infoCollector = (OldBigFilesInfoCollector) context.getBean(OldBigFilesInfoCollector.class.getSimpleName());
            infoCollector.setStartPath(InitProperties.getInstance(InitProperties.DB_MEMTABLE).getProps().getProperty("oldcleanpath"));
            AppConfigurationLocal.getInstance().execute(infoCollector);
            return infoCollector.getFromDatabase();
        }
        catch (BeansException e) {
            return AbstractForms.fromArray(e);
        }
    }

    @Override
    public String toString() {
        return new StringJoiner(",\n", ActionMakeInfoAboutOldCommonFiles.class.getSimpleName() + "[\n", "\n]")
            .toString();
    }
}
