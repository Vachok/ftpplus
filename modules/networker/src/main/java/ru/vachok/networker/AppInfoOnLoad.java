// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.jetbrains.annotations.Contract;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import ru.vachok.networker.ad.common.RightsChecker;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.fileworks.DeleterTemp;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.componentsrepo.services.MyCalen;
import ru.vachok.networker.data.NetKeeper;
import ru.vachok.networker.data.enums.*;
import ru.vachok.networker.data.synchronizer.SyncData;
import ru.vachok.networker.exe.ThreadConfig;
import ru.vachok.networker.exe.schedule.MailIISLogsCleaner;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.info.NetScanService;
import ru.vachok.networker.info.stats.Stats;
import ru.vachok.networker.mail.testserver.MailPOPTester;
import ru.vachok.networker.net.monitor.*;
import ru.vachok.networker.net.ssh.Tracerouting;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.restapi.props.InitProperties;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.*;

import static java.time.DayOfWeek.SUNDAY;


/**
 @see ru.vachok.networker.AppInfoOnLoadTest
 @since 19.12.2018 (9:40) */
public class AppInfoOnLoad implements Runnable {
    
    
    @SuppressWarnings("StaticVariableOfConcreteClass")
    private static final ThreadConfig thrConfig = AppComponents.threadConfig();
    
    private static final ScheduledThreadPoolExecutor SCHED_EXECUTOR = thrConfig.getTaskScheduler().getScheduledThreadPoolExecutor();
    
    private static final List<String> MINI_LOGGER = new ArrayList<>();
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, AppInfoOnLoad.class.getSimpleName());
    
    private static int thisDelay = UsefulUtilities.getScansDelay();
    
    @Override
    public void run() {
        String avCharsetsStr = new TForms().fromArray(Charset.availableCharsets());
        FileSystemWorker.writeFile(FileNames.AVAILABLECHARSETS_TXT, avCharsetsStr);
        thrConfig.execByThreadConfig(AppInfoOnLoad::setCurrentProvider);
        delFilePatterns();
        setNextLast();
        SyncData syncData = SyncData.getInstance(SyncData.INETSYNC);
        AppComponents.threadConfig().execByThreadConfig(syncData::superRun);
        if (UsefulUtilities.thisPC().toLowerCase().contains("home") & NetScanService.isReach(OtherKnownDevices.IP_SRVMYSQL_HOME)) {
            SyncData syncDataBcp = SyncData.getInstance(SyncData.BACKUPER);
            AppComponents.threadConfig().execByThreadConfig(syncDataBcp::superRun);
        }
        try {
            getWeekPCStats();
        }
        catch (RuntimeException e) {
            MessageToUser.getInstance(MessageToUser.DB, AppInfoOnLoad.class.getSimpleName())
                    .error("AppInfoOnLoad.run", e.getMessage(), AbstractForms.exceptionNetworker(e.getStackTrace()));
        }
        finally {
            infoForU();
        }
    }
    
    private static void setCurrentProvider() {
        try {
            NetKeeper.setCurrentProvider(new Tracerouting().call());
        }
        catch (Exception e) {
            NetKeeper.setCurrentProvider("<br><a href=\"/makeok\">" + e.getMessage() + "</a><br>");
            Thread.currentThread().interrupt();
        }
    }
    
    private void delFilePatterns() {
        DeleterTemp deleterTemp = new DeleterTemp(UsefulUtilities.getPatternsToDeleteFilesOnStart());
        thrConfig.execByThreadConfig(deleterTemp);
    }
    
    private void setNextLast() {
        String nextLast = String.valueOf(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY * 2));
        
        InitProperties.setPreference(PropertiesNames.LASTSCAN, nextLast);
        AppComponents.getProps().setProperty(PropertiesNames.LASTSCAN, nextLast);
        
        InitProperties.setPreference(PropertiesNames.NEXTSCAN, nextLast);
        AppComponents.getProps().setProperty(PropertiesNames.NEXTSCAN, nextLast);
    }
    
    private void getWeekPCStats() {
        if (LocalDate.now().getDayOfWeek().equals(SUNDAY)) {
            Stats stats = Stats.getInstance(InformationFactory.STATS_WEEKLY_INTERNET);
            ((Runnable) stats).run();
            stats = Stats.getInstance(InformationFactory.STATS_SUDNAY_PC_SORT);
            try {
                String pcStats = (String) ((Callable) stats).call();
                System.out.println("pcStats = " + pcStats);
            }
            catch (Exception e) {
                messageToUser.error(MessageFormat.format("AppInfoOnLoad.getWeekPCStats {0} - {1}", e.getClass().getTypeName(), e.getMessage()));
            }
        }
        messageToUser.warn(this.getClass().getSimpleName(), checkFileExitLastAndWriteMiniLog() + " checkFileExitLastAndWriteMiniLog", toString());
    }
    
    private void infoForU() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(UsefulUtilities.getBuildStamp());
        messageToUser.info("AppInfoOnLoad.infoForU", ConstantsFor.STR_FINISH, " = " + stringBuilder);
        getMiniLogger().add("infoForU ends. now ftpUploadTask(). Result: " + stringBuilder);
        try {
            messageToUser.info(UsefulUtilities.getIISLogSize());
            AppComponents.threadConfig().execByThreadConfig(()->FileSystemWorker
                    .writeFile("inetstats.tables", InformationFactory.getInstance(InformationFactory.DATABASE_INFO).getInfoAbout(FileNames.DIR_INETSTATS)));
        }
        catch (NullPointerException e) {
            messageToUser.error(MessageFormat.format("AppInfoOnLoad.infoForU threw away: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
        finally {
            this.startPeriodicTasks();
            ftpUploadTask();
        }
        
    }
    
    private boolean checkFileExitLastAndWriteMiniLog() {
        StringBuilder exitLast = new StringBuilder();
        if (new File("exit.last").exists()) {
            exitLast.append(new TForms().fromArray(FileSystemWorker.readFileToList("exit.last"), false));
        }
        getMiniLogger().add(exitLast.toString());
        return FileSystemWorker.writeFile(this.getClass().getSimpleName() + ".mini", getMiniLogger().stream());
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AppInfoOnLoad{");
        sb.append(", thisDelay=").append(thisDelay);
        sb.append(", thisPC=").append(UsefulUtilities.thisPC());
        sb.append("<br>").append(new TForms().fromArray(getMiniLogger(), true));
        sb.append('}');
        return sb.toString();
    }
    
    @Contract(pure = true)
    protected static List<String> getMiniLogger() {
        return MINI_LOGGER;
    }
    
    private void ftpUploadTask() {
        messageToUser.warn(PropertiesNames.SYS_OSNAME_LOWERCASE);
        AppInfoOnLoad.getMiniLogger().add(UsefulUtilities.thisPC());
        String ftpUpload = "new AppComponents().launchRegRuFTPLibsUploader() = " + new AppComponents().launchRegRuFTPLibsUploader();
        getMiniLogger().add(ftpUpload);
    }
    
    private void startPeriodicTasks() {
        Runnable netMonPTVRun = new NetMonitorPTV();
        Runnable tmpFullInetRun = new AppComponents().temporaryFullInternet();
        Runnable scanOnlineRun = new AppComponents().scanOnline();
        Runnable diapazonScanRun = DiapazonScan.getInstance();
        Runnable istranetOrFortexRun = AppInfoOnLoad::setCurrentProvider;
        Runnable popSmtpTest = new MailPOPTester();
        Runnable saveTHRTimes = thrConfig::getAllThreads;
        SCHED_EXECUTOR.scheduleWithFixedDelay(netMonPTVRun, 10, 10, TimeUnit.SECONDS);
        SCHED_EXECUTOR.scheduleWithFixedDelay(istranetOrFortexRun, ConstantsFor.DELAY, ConstantsFor.DELAY * thisDelay, TimeUnit.SECONDS);
        SCHED_EXECUTOR.scheduleWithFixedDelay(popSmtpTest, ConstantsFor.DELAY * 2, thisDelay, TimeUnit.MINUTES);
        SCHED_EXECUTOR.scheduleWithFixedDelay(tmpFullInetRun, 1, ConstantsFor.DELAY, TimeUnit.MINUTES);
        SCHED_EXECUTOR.scheduleWithFixedDelay(diapazonScanRun, 2, UsefulUtilities.getScansDelay(), TimeUnit.MINUTES);
        SCHED_EXECUTOR.scheduleWithFixedDelay(scanOnlineRun, 3, 2, TimeUnit.MINUTES);
        SCHED_EXECUTOR.scheduleWithFixedDelay((Runnable) InformationFactory.getInstance(InformationFactory.REGULAR_LOGS_SAVER), 4, thisDelay, TimeUnit.MINUTES);
        SCHED_EXECUTOR.scheduleWithFixedDelay(saveTHRTimes, 5, 5, TimeUnit.MINUTES);
        getMiniLogger().add(thrConfig.toString());
        this.startIntervalTasks();
    }
    
    private void startIntervalTasks() {
        Date nextStartDay = MyCalen.getNextDayofWeek(23, 57, SUNDAY);
        scheduleStats(nextStartDay);
        nextStartDay = new Date(nextStartDay.getTime() - TimeUnit.HOURS.toMillis(1));
        scheduleIISLogClean(nextStartDay);
        this.kudrMonitoring();
        AppInfoOnLoad.runCommonScan();
    }
    
    private void scheduleStats(Date nextStartDay) {
        Stats stats = Stats.getInstance(InformationFactory.STATS_WEEKLY_INTERNET);
        Stats instance = Stats.getInstance(InformationFactory.STATS_SUDNAY_PC_SORT);
        thrConfig.getTaskScheduler().scheduleWithFixedDelay((Runnable) instance, nextStartDay, ConstantsFor.ONE_WEEK_MILLIS);
        thrConfig.getTaskScheduler().scheduleWithFixedDelay((Runnable) stats, nextStartDay, ConstantsFor.ONE_WEEK_MILLIS);
        getMiniLogger().add(nextStartDay + " WeekPCStats() start\n");
    }
    
    private static void scheduleIISLogClean(Date nextStartDay) {
        Runnable iisCleaner = new MailIISLogsCleaner();
        thrConfig.getTaskScheduler().scheduleWithFixedDelay(iisCleaner, nextStartDay, ConstantsFor.ONE_WEEK_MILLIS);
        getMiniLogger().add(nextStartDay + " MailIISLogsCleaner() start\n");
    }
    
    private void kudrMonitoring() {
        Date next9AM;
        Runnable kudrWorkTime = new KudrWorkTime();
        int secondOfDayNow = LocalTime.now().toSecondOfDay();
        int officialStart = LocalTime.parse("08:30").toSecondOfDay();
        int officialEnd = LocalTime.parse("17:30").toSecondOfDay();
        ThreadPoolTaskScheduler taskScheduler = thrConfig.getTaskScheduler();
        if (secondOfDayNow < officialStart) {
            next9AM = MyCalen.getThisDay(8, 30);
            taskScheduler.scheduleWithFixedDelay(kudrWorkTime, next9AM, TimeUnit.HOURS.toMillis(ConstantsFor.ONE_DAY_HOURS));
        }
        else {
            next9AM = MyCalen.getNextDay(8, 30);
            taskScheduler.scheduleWithFixedDelay(kudrWorkTime, next9AM, TimeUnit.HOURS.toMillis(ConstantsFor.ONE_DAY_HOURS));
        }
        if (secondOfDayNow > 40000) {
            thrConfig.execByThreadConfig(kudrWorkTime);
        }
        messageToUser.warn(MessageFormat.format("{0} starts at {1}", kudrWorkTime.toString(), next9AM));
    }
    
    private static void runCommonScan() {
        Path pathStart = Paths.get("\\\\srv-fs.eatmeat.ru\\it$$\\Хлам\\");
        Path pathToSaveLogs = Paths.get(".");
        
        if (UsefulUtilities.thisPC().toLowerCase().contains("rups")) {
            pathStart = Paths.get("\\\\srv-fs.eatmeat.ru\\common_new");
            pathToSaveLogs = Paths.get("\\\\srv-fs.eatmeat.ru\\Common_new\\14_ИТ_служба\\Внутренняя");
        }
        if (new File(FileNames.COMMON_RGH).exists()) {
            new File(FileNames.COMMON_RGH).delete();
        }
        if (new File(FileNames.COMMON_OWN).exists()) {
            new File(FileNames.COMMON_OWN).delete();
        }
        Runnable checker = new RightsChecker(pathStart, pathToSaveLogs);
        Date day2030 = MyCalen.getThisDay(20, 30);
        long delayOneDay = TimeUnit.DAYS.toMillis(1);
        thrConfig.getTaskScheduler().scheduleAtFixedRate(checker, day2030, delayOneDay);
        MessageToUser.getInstance(MessageToUser.DB, AppInfoOnLoad.class.getSimpleName()).warn(checker.toString(), day2030.toString(), delayOneDay + " millis");
    }
    
}
