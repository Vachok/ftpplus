package ru.vachok.networker.systray;


import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.net.DiapazonedScan;

import javax.swing.*;
import java.awt.event.ActionEvent;


/**
 Класс запуска тестов
 <p>

 @since 28.01.2019 (1:21) */
public class ActionTests extends AbstractAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        ThreadConfig.executeAsThread(DiapazonedScan.getInstance());
        new MessageToTray(
            new ActionDefault(ConstantsFor.HTTP_LOCALHOST_8880_SLASH + "/showalldev?needsopen"))
            .errorAlert(ConstantsFor.ALL_DEVICES.size() + " deq size");
    }
}