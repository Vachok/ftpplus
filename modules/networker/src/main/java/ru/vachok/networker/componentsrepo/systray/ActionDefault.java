// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.systray;


import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.restapi.props.InitProperties;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;


/**
 Default Tray Action
 <p>
 
 @see SystemTrayHelper
 @since 25.01.2019 (9:56) */
public class ActionDefault extends AbstractAction {
    
    
    public static final String TOSTRING_SAMACCOUNTNAME = ", samAccountName='";
    
    public static final String HTTP_LOCALHOST8880SLASH = "http://localhost:8880/";
    
    /**
     {@link MessageLocal}
     */
    private static final MessageToUser messageToUser = new MessageCons(ActionDefault.class.getSimpleName());
    
    private String goTo;
    
    public ActionDefault(String goTo) {
        this.goTo = goTo;
    }
    
    public ActionDefault() {
        this.goTo = HTTP_LOCALHOST8880SLASH;
        if (!SystemTray.isSupported()) {
            throw new UnsupportedOperationException();
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Optional optionalTray = SystemTrayHelper.getI();
        if (optionalTray.isPresent()) {
            ((SystemTrayHelper) optionalTray.get()).delOldActions();
            try {
                Desktop.getDesktop().browse(URI.create(goTo));
                InitProperties.reloadApplicationPropertiesFromFile();
            }
            catch (IOException | IllegalArgumentException e1) {
                messageToUser.errorAlert("ActionDefault", ConstantsFor.METHNAME_ACTIONPERFORMED, e1.getMessage());
            }
        }
        Thread.currentThread().checkAccess();
        Thread.currentThread().interrupt();
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ActionDefault{");
        sb.append("goTo='").append(goTo).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
