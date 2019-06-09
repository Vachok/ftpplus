// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.systray.actions;


import ru.vachok.messenger.MessageSwing;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.*;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.systray.SystemTrayHelper;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.concurrent.*;

/**
 Action GIT web start
 <p>

 @see SystemTrayHelper
 @since 25.01.2019 (9:43) */
public class ActionGITStart extends AbstractAction {

    private static MessageToUser messageToUser = new MessageSwing();

    private static final SystemTrayHelper SYSTEM_TRAY_HELPER = SystemTrayHelper.getI();
    
    public ActionGITStart() {
        if (IntoApplication.TRAY_SUPPORTED && SYSTEM_TRAY_HELPER.getTrayIcon() != null) {
            SYSTEM_TRAY_HELPER.delOldActions();
        }
    }

    @Override
    public void actionPerformed(ActionEvent eAct) {
        Callable<String> sshStr = () -> new SSHFactory.Builder(ConstantsFor
            .IPADDR_SRVGIT, new StringBuilder()
            .append("sudo git instaweb;")
            .append("sudo cd /usr/home/dpetrov/;")
            .append("sudo git instaweb -p 11111;")
            .append("sudo cd /usr/home/kudr/;")
            .append("sudo git instaweb -p 9999;")
            .append("exit;")
            .toString(), getClass().getSimpleName()).build().call();
        Future<String> submit = AppComponents.threadConfig().getTaskExecutor().submit(sshStr);
        try {
            int timeOut30 = 30;
            messageToUser.infoTimer(( int ) ConstantsFor.DELAY, getClass().getSimpleName() + "\nFuture<String> submit = " + submit.get(timeOut30,
                TimeUnit.SECONDS));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            FileSystemWorker.writeFile(getClass().getSimpleName(), (e.getMessage() + "\n" + new TForms().fromArray(e, false)));
            Thread.currentThread().interrupt();
        }
    }
}
