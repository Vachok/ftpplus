package ru.vachok.networker.systray;


import org.slf4j.Logger;
import ru.vachok.messenger.MessageSwing;
import ru.vachok.networker.AppInfoOnLoad;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.errorexceptions.MyNull;
import ru.vachok.networker.net.DiapazonedScan;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Date;
import java.util.concurrent.TimeUnit;


/**
 Action Some Info
 <p>

 @see SystemTrayHelper
 @since 25.01.2019 (9:33) */
class ActionSomeInfo extends AbstractAction {

    private static final Logger LOGGER = AppComponents.getLogger();

    @Override
    public void actionPerformed(ActionEvent e) {
        LOGGER.warn("ActionSomeInfo.actionPerformed");
        try {
            Date newScan = new Date(DiapazonedScan.getInstance().getStopClass() + TimeUnit.MINUTES.toMillis(111));
            new MessageSwing().infoNoTitles("New Scan at: " + newScan.toString() + " | " + ConstantsFor.getUpTime() + "\n" +
                Thread.activeCount() + " threads " + ConstantsFor.showMem() + AppInfoOnLoad.iisLogSize() + "\n" +
                AppComponents.versionInfo().toString() + "\n" +
                new TForms().fromArray(ConstantsFor.getProps()));
        } catch (MyNull myNull) {
            LOGGER.error(myNull.getMessage(), myNull);
        }
    }
}
