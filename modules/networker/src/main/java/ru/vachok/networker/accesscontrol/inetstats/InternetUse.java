// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.inetstats;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
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
import java.util.UnknownFormatConversionException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;


/**
 @see ru.vachok.networker.accesscontrol.inetstats.InternetUseTest
 @since 02.04.2019 (10:24) */
public abstract class InternetUse extends Stats implements Callable<Integer> {
    
    
    private static final MessageToUser messageToUser = new MessageToTray(InternetUse.class.getSimpleName());
    
    protected static String aboutWhat = "null";
    
    public String getUsage(String userCred) {
        throw new TODOException("16.08.2019 (19:16)");
    }
    
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
    
    @Contract(" -> new")
    public static @NotNull InternetUse getI() {
        InetAddress inetAddress = InetAddress.getLoopbackAddress();
        try {
            inetAddress = InetAddress.getByName(aboutWhat);
            return new InetIPUser();
        }
        catch (UnknownHostException | UnknownFormatConversionException e) {
            System.out.println("inetAddress = " + inetAddress);
            return new InetUserPCName();
        }
    }
    
    public String getInfoAbout(String aboutWhat) {
        InternetUse.aboutWhat = aboutWhat;
        try {
            InetAddress inetAddress = new NameOrIPChecker(aboutWhat).resolveIP();
            aboutWhat = inetAddress.getHostAddress();
        }
        catch (UnknownHostException e) {
            return new InetUserPCName().getUsage(aboutWhat);
        }
        return new InetIPUser().getUsage(aboutWhat);
    }
    
    @Override
    public abstract void setClassOption(Object classOption);
    
    @Override
    public String getInfo() {
        return DatabaseInfo.getInfoInstance(aboutWhat).getConnectStatistics();
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
