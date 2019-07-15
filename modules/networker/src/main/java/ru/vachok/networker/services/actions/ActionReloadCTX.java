// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.services.actions;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;


/**
 Action on Reload Context button
 <p>
 
 @see ru.vachok.networker.IntoApplication
 @since 25.01.2019 (13:30)
 @deprecated 05.06.2019 (15:29) */
@Deprecated
class ActionReloadCTX extends AbstractAction {
    
    
    private static final String[] ARGS = new String[0];
    
    /**
     {@link MessageLocal}
     */
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    @Override
    public void actionPerformed(ActionEvent e) {
        throw new UnsupportedOperationException("05.06.2019 (15:29) DEPRECATED");
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ActionMakeInfoAboutOldCommonFiles{");
        sb.append("ARGS=").append(Arrays.toString(ARGS));
        sb.append('}');
        return sb.toString();
    }
}
