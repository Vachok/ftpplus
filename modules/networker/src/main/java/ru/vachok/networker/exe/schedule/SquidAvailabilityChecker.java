package ru.vachok.networker.exe.schedule;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.SSHFactory;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

import java.util.concurrent.Callable;


public class SquidAvailabilityChecker implements Callable<String>, Runnable {
    
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    @Override public void run() {
        try {
            call();
        }
        catch (Exception e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".run", e));
        }
    }
    
    @Override public String call() throws Exception {
        return squidCheck();
    }
    
    private String squidCheck() {
        SSHFactory builder = new SSHFactory.Builder("srv-nat.eatmeat.ru", "ls", getClass().getSimpleName()).build();
        builder.setCommandSSH("sudo ps ax | grep squid && exit");
        String callChk = builder.call();
        if (callChk.contains("ssl_crtd")) {
            messageToUser.info(FileSystemWorker.writeFile(getClass().getSimpleName() + ".log", callChk));
            return callChk;
        }
        else {
            builder.setCommandSSH("sudo squid && sudo ps ax | grep squid && exit");
            callChk = builder.call();
            messageToUser.error(FileSystemWorker.writeFile(getClass().getSimpleName() + ".log", callChk));
            return callChk;
        }
    }
    
}

