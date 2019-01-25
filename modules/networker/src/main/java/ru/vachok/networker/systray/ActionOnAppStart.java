package ru.vachok.networker.systray;


import ru.vachok.mysqlandprops.EMailAndDB.SpeedRunActualize;
import ru.vachok.networker.AppInfoOnLoad;
import ru.vachok.networker.config.ThreadConfig;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.time.LocalTime;
import java.util.Date;

/**
 Actions on application start
 <p>

 @see ru.vachok.networker.AppInfoOnLoad
 @since 25.01.2019 (11:58) */
public class ActionOnAppStart extends AbstractAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        long when = e.getWhen();
        Thread threadSP = new ThreadConfig().threadPoolTaskExecutor().createThread(new SpeedRunActualize());
        threadSP.setName("SpeedRunActualize");
        threadSP.start();
        String messageSW = "SpeedRunActualize running " + threadSP.isAlive() + "\n" +
            LocalTime.now().toString() + " now.\nAction at: \n" +
            new Date(when) + "\n" +
            AppInfoOnLoad.class.getSimpleName();
        new MessageToTray(new ActionDefault()).infoNoTitles(messageSW);
    }
}
