// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.systray.actions;


import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.props.InitProperties;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;


/**
 Class ActionOpenProgFolder
 <p>
 
 @see ru.vachok.networker.systray.actions.ActionOpenProgFolderTest
 @since 12.07.2019 (20:19) */
@SuppressWarnings("ClassHasNoToStringMethod")
public class ActionOpenProgFolder extends AbstractAction {
    
    
    protected static final String TITLE_MSG = "Sync properties?";
    
    protected static final String BODYMSG_DB = "Send current App Properties to DB?";
    
    @Override
    public void actionPerformed(ActionEvent e) {
        openFolder();
        InitProperties.reloadApplicationPropertiesFromFile();
    }
    
    private void openFolder() {
        Path workingNowPathRoot = Paths.get(".");
        try {
            Process execOpen = Runtime.getRuntime().exec(MessageFormat.format("explorer \"{0}\n", workingNowPathRoot));
        }
        catch (IOException e1) {
            System.err.println(e1.getMessage() + " " + getClass().getSimpleName() + ConstantsFor.STR_ACTIONPERFORMED);
        }
    }
}