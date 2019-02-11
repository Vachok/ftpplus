package ru.vachok.networker.systray;


import org.slf4j.Logger;
import ru.vachok.messenger.MessageSwing;
import ru.vachok.networker.AppInfoOnLoad;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.net.DiapazonedScan;
import ru.vachok.networker.services.SpeedChecker;

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

    /**
     {@link AppComponents#getLogger()}
     */
    private static final Logger LOGGER = AppComponents.getLogger();

    ActionSomeInfo() {
        SystemTrayHelper.delOldActions();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        LOGGER.warn("ActionSomeInfo.actionPerformed");
        Date newScan = new Date(DiapazonedScan.getInstance().getStopClass() + TimeUnit.MINUTES.toMillis(111));
        new MessageSwing(400, 500, 35, 20).infoNoTitles("New Scan at: " +
            newScan.toString() +
            " | " + ConstantsFor.getUpTime() + ", " + SpeedChecker.ChkMailAndUpdateDB.todayInfo() + "\n" +
            Thread.activeCount() + " threads " + ConstantsFor.getMemoryInfo() + AppInfoOnLoad.iisLogSize() + "\n" +
            AppComponents.versionInfo().toString() + "\n" +
            new TForms().fromArray(ConstantsFor.getProps()));
    }
}
