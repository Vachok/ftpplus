package ru.vachok.networker.systray;


import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageSwing;
import ru.vachok.networker.AppInfoOnLoad;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;
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

    ActionSomeInfo() {
        new MessageCons().errorAlert("ActionSomeInfo.ActionSomeInfo");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Date newScan = new Date(DiapazonedScan.getInstance().getStopClassStampLong() + TimeUnit.MINUTES.toMillis(111));
        new MessageSwing(660, 520, 45, 35).infoNoTitles("New Scan at: " + newScan.toString() + " | " +
            ConstantsFor.getUpTime() + ", " + "\n" +
            Thread.activeCount() + " threads " + ConstantsFor.getMemoryInfo() + AppInfoOnLoad.iisLogSize() + "\n" +
            AppComponents.versionInfo().toString() + "\n" +
            new TForms().fromArray(AppComponents.getProps()));
        SystemTrayHelper.delOldActions();
    }
}
