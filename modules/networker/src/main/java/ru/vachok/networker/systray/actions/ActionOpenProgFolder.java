// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.systray.actions;


import ru.vachok.messenger.MessageSwing;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.exceptions.ReminderException;

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
        try {
            updateProps();
        }
        catch (ReminderException ex) {
            messageToUser.error(MessageFormat.format("ActionOpenProgFolder.actionPerformed says: {0}. Parameters: {1}", ex.getMessage(), ""));
        }
    }
    
    private void updateProps() throws ReminderException {
        try {
            new AppComponents().updateProps();
        }
        catch (IOException ex) {
            messageToUser.error(ex.getMessage());
            throw new ReminderException("NEED TO DO!", new Throwable(getClass().getTypeName()));
        }
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