package ru.vachok.networker.systray;


import ru.vachok.messenger.MessageCons;
import ru.vachok.networker.ExitApp;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.net.NetListKeeper;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 Action Exit App
 <p>

 @see SystemTrayHelper
 @since 25.01.2019 (9:59) */
class ActionExit extends AbstractAction {

    private String reason;

    public ActionExit(String reason) {
        this.reason = reason;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        new MessageCons().infoNoTitles(getClass().getSimpleName() + ".actionPerformed");

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(NetListKeeper.class.getSimpleName() + ".ser");
            ThreadConfig.executeAsThread(new ExitApp(reason, fileOutputStream, NetListKeeper.class));
        } catch (IOException ex) {
            new MessageCons().errorAlert("ActionExit", "actionPerformed", ex.getMessage());
        }
    }
}
