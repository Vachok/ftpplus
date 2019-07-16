// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.systray.actions;


import ru.vachok.messenger.MessageSwing;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.restapi.InitProperties;
import ru.vachok.networker.restapi.props.FilePropsLocal;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;


/**
 Class ru.vachok.networker.systray.actions.ActionOpenProgFolder
 <p>
 
 @see ru.vachok.networker.systray.actions.ActionOpenProgFolderTest
 @since 12.07.2019 (20:19) */
@SuppressWarnings("ClassHasNoToStringMethod")
public class ActionOpenProgFolder extends AbstractAction {
    
    
    protected static final String TITLE_MSG = "Sync properties?";
    
    protected static final String BODYMSG_DB = "Send current App Properties to DB?";
    
    private MessageToUser messageToUser = new MessageSwing();
    
    @Override
    public void actionPerformed(ActionEvent e) {
        openFolder();
        InitProperties initProperties = new FilePropsLocal(ConstantsFor.class.getSimpleName());
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