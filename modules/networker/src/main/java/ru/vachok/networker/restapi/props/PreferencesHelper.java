// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.props;


import ru.vachok.networker.restapi.MessageToUser;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;


/**
 @see ru.vachok.networker.restapi.props.PreferencesHelperTest
 @since 06.08.2019 (23:24) */
public class PreferencesHelper {
    
    
    private MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, this.getClass().getSimpleName());
    
    public Preferences getPref() {
        Preferences preferences = Preferences.userRoot().node("networker");
        
        try {
            String fileName = preferences.name() + ".prefer";
            Preferences.importPreferences(new FileInputStream(fileName));
            preferences.flush();
            preferences.sync();
            preferences.exportNode(new FileOutputStream(fileName));
        }
        catch (IOException | BackingStoreException | InvalidPreferencesFormatException e) {
            messageToUser.error(e.getMessage());
        }
        return preferences;
    }
    
}