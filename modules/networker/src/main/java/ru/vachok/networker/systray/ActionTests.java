package ru.vachok.networker.systray;


import org.slf4j.Logger;
import ru.vachok.mysqlandprops.EMailAndDB.SpeedRunActualize;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.net.ScanOnline;
import ru.vachok.networker.services.SpeedChecker;

import javax.swing.*;
import java.awt.event.ActionEvent;


/**
 Класс запуска тестов
 <p>

 @since 28.01.2019 (1:21) */
public class ActionTests extends AbstractAction {

    private static final Logger LOGGER = AppComponents.getLogger();

    @Override
    public void actionPerformed(ActionEvent e) {
        new SpeedChecker().call();
        new SpeedRunActualize().run();
        new ScanOnline().run();
    }
}