package ru.vachok.networker.systray;


import ru.vachok.messenger.MessageSwing;
import ru.vachok.networker.AppInfoOnLoad;
import ru.vachok.networker.net.DiapazonedScan;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 Читает {@code DiapazonedScan.dev}
 <p>
 Переехал из {@link DiapazonedScan#welcomeMessage(java.lang.String)}

 @see MessageToTray
 @since 30.01.2019 (11:19) */
public class ReaderForScannedIP implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e1) {
        AppInfoOnLoad.diaScanReader();
        new MessageSwing(new ActionDefault()).infoTimer(5, "ReaderForScannedIP.actionPerformed");
    }
}
