package ru.vachok.networker.systray;


import ru.vachok.messenger.MessageSwing;
import ru.vachok.networker.TForms;
import ru.vachok.networker.net.NetScannerSvc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;

/**
 Action после {@link NetScannerSvc#getPCsAsync()}
 <p>

 @see NetScannerSvc
 @since 25.01.2019 (13:06) */
public class ActionScanCompl extends AbstractAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            Desktop.getDesktop().browse(URI.create("http://localhost:8880/netscan"));
        } catch (IOException e1) {
            new MessageSwing().errorAlert(
                "NetScannerSvc",
                ActionDefault.ACTION_PERFORMED,
                new TForms().fromArray(e1, false));
        }
    }
}
