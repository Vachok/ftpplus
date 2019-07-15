// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.services.actions;


import ru.vachok.messenger.MessageSwing;
import ru.vachok.networker.restapi.message.MessageToTray;
import ru.vachok.networker.systray.SystemTrayHelper;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 {@link ActionListener} для {@link ActDirectoryCTRL#queryStringExists(java.lang.String, org.springframework.ui.Model)}
 <p>
 @see ActDirectoryCTRL
 @see MessageToTray
 @since 30.01.2019 (10:01) */
public class ListenUserInfo implements ActionListener {

    private final String queryString;

    private final String attributeValue;

    private final String finalAdSrvDetails;

    private static final SystemTrayHelper SYSTEM_TRAY_HELPER = SystemTrayHelper.getI();

    public ListenUserInfo(String queryString, String attributeValue, String finalAdSrvDetails) {
        this.queryString = queryString;
        this.attributeValue = attributeValue;
        this.finalAdSrvDetails = finalAdSrvDetails;
        ActionListener[] actionListeners = SYSTEM_TRAY_HELPER.getTrayIcon().getActionListeners();
        if(actionListeners.length > 0){
            for(ActionListener actionListener : actionListeners){
                SYSTEM_TRAY_HELPER.getTrayIcon().removeActionListener(actionListener);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        new MessageSwing(750, 750, 66, 38)
            .infoTimer(30, queryString + "\n\n" + attributeValue + "\n" + finalAdSrvDetails.replaceAll("<br>", "\n"));
    }
}
