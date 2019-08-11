// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.props;


import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


/**
 @see ru.vachok.networker.restapi.props.PreferencesHelperTest
 @since 06.08.2019 (23:24) */
public class PreferencesHelper {
    
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    public Preferences getPref(String nodeName) {
        Preferences preferences = Preferences.userRoot().node(nodeName);
        try {
            preferences.flush();
            preferences.sync();
            preferences.exportNode(new FileOutputStream(preferences.name() + ".prefer"));
        }
        catch (IOException | BackingStoreException e) {
            messageToUser.error(e.getMessage());
        }
        return preferences;
    }
    
}