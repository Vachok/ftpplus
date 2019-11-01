// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.props;


import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.*;
import java.util.prefs.*;


/**
 @see ru.vachok.networker.restapi.props.PreferencesHelperTest
 @since 06.08.2019 (23:24) */
public class PreferencesHelper {
    
    
    private MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, this.getClass().getSimpleName());
    
    public Preferences getPref() {
        Preferences preferences = Preferences.userRoot().node(ConstantsFor.PREF_NODE_NAME);
        
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