package ru.vachok.networker.systray;


import ru.vachok.messenger.MessageSwing;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.SSHFactory;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.fileworks.FileSystemWorker;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.concurrent.*;

/**
 Action GIT web start
 <p>

 @see SystemTrayHelper
 @since 25.01.2019 (9:43) */
class ActionGITStart extends AbstractAction {

    private static MessageToUser messageToUser = new MessageSwing();

    ActionGITStart() {
        if(ConstantsFor.IS_SYSTRAY_AVAIL && SystemTrayHelper.getTrayIcon()!=null){
            SystemTrayHelper.delOldActions();
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
            .toString()).build().call();
        Future<String> submit = AppComponents.threadConfig().getTaskExecutor().submit(sshStr);
        try {
            int timeOut30 = 30;
            messageToUser.infoTimer(( int ) ConstantsFor.DELAY, getClass().getSimpleName() + "\nFuture<String> submit = " + submit.get(timeOut30,
                TimeUnit.SECONDS));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            FileSystemWorker.recFile(getClass().getSimpleName(), (e.getMessage() + "\n" + new TForms().fromArray(e, false)));
            Thread.currentThread().interrupt();
        }
    }
}
