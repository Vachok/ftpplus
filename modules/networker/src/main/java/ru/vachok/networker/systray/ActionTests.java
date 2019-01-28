package ru.vachok.networker.systray;


import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.net.ScanOffline;
import ru.vachok.networker.net.ScanOnline;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.time.LocalTime;


/**
 Класс запуска тестов
 <p>

 @since 28.01.2019 (1:21) */
public class ActionTests extends AbstractAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        ThreadConfig threadConfig = new ThreadConfig();
        ScanOnline scanOnline = ScanOnline.getI();
        threadConfig.threadPoolTaskExecutor().execute(scanOnline);
        String s = " is complete";
        new MessageToTray(new ActionDefault(ConstantsFor.SHOWALLDEV_NEEDSOPEN))
            .info(getClass().getSimpleName(), LocalTime.now().toString(), scanOnline.hashCode() + s);
        ScanOffline scanOffline = ScanOffline.getI();
        threadConfig.threadPoolTaskExecutor().execute(scanOffline);
        new MessageToTray(new ActionDefault(ConstantsFor.SHOWALLDEV_NEEDSOPEN))
            .info(getClass().getSimpleName(), LocalTime.now().toString(), scanOffline.hashCode() + s);

    }
}