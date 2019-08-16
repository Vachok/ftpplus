// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.inetstats;


import org.jetbrains.annotations.Contract;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.UsefulUtilities;
import ru.vachok.networker.accesscontrol.NameOrIPChecker;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.info.DatabaseInfo;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageToTray;
import ru.vachok.networker.statistics.Stats;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;


/**
 @see ru.vachok.networker.accesscontrol.inetstats.InternetUseTest
 @since 02.04.2019 (10:24) */
public abstract class InternetUse extends Stats implements Callable<Integer> {
    
    
    private static final MessageToUser messageToUser = new MessageToTray(InternetUse.class.getSimpleName());
    
    protected static String aboutWhat = "null";
    
    public abstract String getUsage(String userCred);
    
    public void showLog() {
        int cleanTrash = InternetUse.getCleanedRows();
        messageToUser.info(this.getClass().getSimpleName(), "CLEANED: ", String.valueOf(cleanTrash));
    }
    
    @Contract(pure = true)
    public static int getCleanedRows() {
        AppComponents.threadConfig().getTaskScheduler().getScheduledThreadPoolExecutor()
                .scheduleWithFixedDelay(DatabaseInfo::cleanTrash, 0, UsefulUtilities.getDelay(), TimeUnit.MINUTES);
        return DatabaseInfo.cleanedRows;
    }
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        InternetUse.aboutWhat = aboutWhat;
        try {
            InetAddress inetAddress = new NameOrIPChecker(aboutWhat).resolveIP();
            aboutWhat = inetAddress.getHostAddress();
        }
        catch (UnknownHostException e) {
            return MessageFormat.format("InternetUse.getInfoAbout: {0}, ({1})", e.getMessage(), e.getClass().getName());
        }
        return new InetIPUser().getUsage(aboutWhat);
    }
    
    @Override
    public abstract void setClassOption(Object classOption);
    
    @Override
    public String getInfo() {
        throw new TODOException("16.08.2019 (16:38)");
    }
    
    @Override
    public String writeLog(String logName, String information) {
        return FileSystemWorker.writeFile(logName, information);
    }
    
    @Override
    public Integer call() {
        return DatabaseInfo.cleanTrash();
    }
}
