package ru.vachok.networker.systray;


import ru.vachok.messenger.MessageCons;
import ru.vachok.networker.ExitApp;
import ru.vachok.networker.config.ThreadConfig;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 Action Exit App
 <p>

 @see SystemTrayHelper
 @since 25.01.2019 (9:59) */
class ActionExit extends AbstractAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        new MessageCons().infoNoTitles(getClass().getSimpleName() + ".actionPerformed");
        ThreadConfig.executeAsThread(new ExitApp(SystemTrayHelper.class.getSimpleName()));
    }
}
