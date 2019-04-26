package ru.vachok.networker.services.actions;


import ru.vachok.messenger.MessageSwing;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.IntoApplication;
import ru.vachok.networker.TForms;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.DiapazonedScan;
import ru.vachok.networker.services.MessageLocal;
import ru.vachok.networker.systray.SystemTrayHelper;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.concurrent.TimeUnit;


/**
 Action Some Info
 <p>
 
 @see SystemTrayHelper
 @since 25.01.2019 (9:33) */
public class ActionSomeInfo extends AbstractAction {
    
    
    private transient MessageToUser messageToUser = new MessageLocal(ActionSomeInfo.class.getSimpleName());
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (IntoApplication.TRAY_SUPPORTED) {
            messageToUser = new MessageSwing();
        }
        Date newScan = new Date(DiapazonedScan.getInstance().getStopClassStampLong() + TimeUnit.MINUTES.toMillis(111));
        new Thread(ConstantsFor.INFO_MSG_RUNNABLE).start();
        messageToUser.info("ActionSomeInfo.actionPerformed", "newScan = ", newScan + "\nCharsets: \n" + FileSystemWorker
            .writeFile("charsets.info", new TForms().fromArray(Charset.availableCharsets().values(), false)));
        SystemTrayHelper.getI().delOldActions();
    }
}
