// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.SSHFactory;
import ru.vachok.networker.TForms;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;
import ru.vachok.stats.connector.SSHWorker;

import java.io.File;
import java.util.List;
import java.util.Set;


/**
 @since 17.04.2019 (11:30) */
public class AccessListsCheckUniq implements SSHWorker, Runnable {
    
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    @Override public void run() {
        messageToUser.info(getClass().getSimpleName() + ".run", "connectTo()", " = " + connectTo());
    }
    
    @Override public String connectTo() {
        SSHFactory.Builder builder = new SSHFactory.Builder("srv-nat.eatmeat.ru", "uname -a", getClass().getSimpleName());
        SSHFactory sshFactory = builder.build();
        sshFactory.setCommandSSH("sudo cat /etc/pf/allowdomain && exit");
        String call = sshFactory.call();
        Set<String> stringSet = FileSystemWorker.readFileToSet(sshFactory.getTempFile());
        return new TForms().fromArray(stringSet, true);
    }
    
    private List<String> parseListFiles(File file) {
        return FileSystemWorker.readFileToList(file.getAbsolutePath());
    }
}
