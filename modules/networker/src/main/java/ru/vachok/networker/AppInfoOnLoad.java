// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import org.jetbrains.annotations.Contract;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import ru.vachok.networker.ad.common.RightsChecker;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.fileworks.DeleterTemp;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.componentsrepo.services.MyCalen;
import ru.vachok.networker.data.NetKeeper;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.data.enums.OtherKnownDevices;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.data.synchronizer.SyncData;
import ru.vachok.networker.exe.ThreadConfig;
import ru.vachok.networker.exe.schedule.MailIISLogsCleaner;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.info.NetScanService;
import ru.vachok.networker.info.stats.Stats;
import ru.vachok.networker.mail.testserver.MailPOPTester;
import ru.vachok.networker.net.monitor.DiapazonScan;
import ru.vachok.networker.net.monitor.KudrWorkTime;
import ru.vachok.networker.net.monitor.NetMonitorPTV;
import ru.vachok.networker.net.ssh.Tracerouting;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.DBMessenger;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.restapi.props.InitProperties;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
        Thread.currentThread().setName(this.getClass().getSimpleName());
        String avCharsetsStr = AbstractForms.fromArray(Charset.availableCharsets());
        FileSystemWorker.writeFile(FileNames.AVAILABLECHARSETS_TXT, avCharsetsStr);
        String name = "AppInfoOnLoad.run";
        thrConfig.execByThreadConfig(AppInfoOnLoad::setCurrentProvider, name);
        delFilePatterns();
        setNextLast();
        SyncData syncData = SyncData.getInstance(SyncData.INETSYNC);
        AppComponents.threadConfig().execByThreadConfig(syncData::superRun, name);
        if (UsefulUtilities.thisPC().toLowerCase().contains("home") & NetScanService.isReach(OtherKnownDevices.IP_SRVMYSQL_HOME)) {
            SyncData syncDataBcp = SyncData.getInstance(SyncData.BACKUPER);
            AppComponents.threadConfig().execByThreadConfig(syncDataBcp::superRun, name);
        }
        try {
            getWeekPCStats();
            infoForU();
        }
        catch (RuntimeException e) {
            MessageToUser.getInstance(MessageToUser.DB, AppInfoOnLoad.class.getSimpleName())
                    .error(name, e.getMessage(), AbstractForms.exceptionNetworker(e.getStackTrace()));
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
        thrConfig.execByThreadConfig(deleterTemp, "AppInfoOnLoad.delFilePatterns");
    }
    
    private void setNextLast() {
        String nextLast = String.valueOf(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY));
        
        InitProperties.setPreference(PropertiesNames.LASTSCAN, nextLast);
        AppComponents.getProps().setProperty(PropertiesNames.LASTSCAN, nextLast);
        
        InitProperties.setPreference(PropertiesNames.NEXTSCAN, nextLast);
        AppComponents.getProps().setProperty(PropertiesNames.NEXTSCAN, nextLast);
        
        showInConsole();
    }
    
    private void showInConsole() {
        long lastCheck = InitProperties.getUserPref().getLong(PropertiesNames.LASTSCAN, 0);
        long nextCheck = InitProperties.getUserPref().getLong(PropertiesNames.NEXTSCAN, 0);
        messageToUser.warn(this.getClass().getSimpleName(), new Date(lastCheck).toString(), new Date(nextCheck).toString());
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
        String name = "AppInfoOnLoad.infoForU";
        messageToUser.info(name, ConstantsFor.STR_FINISH, " = " + stringBuilder);
        getMiniLogger().add("infoForU ends. now ftpUploadTask(). Result: " + stringBuilder);
        try {
            Runnable runInfoForU = ()->FileSystemWorker
                    .writeFile("inetstats.tables", InformationFactory.getInstance(InformationFactory.DATABASE_INFO).getInfoAbout(FileNames.DIR_INETSTATS));
            messageToUser.info(UsefulUtilities.getIISLogSize());
            AppComponents.threadConfig().execByThreadConfig(runInfoForU, name);
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
            exitLast.append(AbstractForms.fromArray(FileSystemWorker.readFileToList("exit.last")));
        }
        getMiniLogger().add(exitLast.toString());
        return FileSystemWorker.writeFile(this.getClass().getSimpleName() + ".mini", getMiniLogger().stream());
    }
    
    private static void scheduleIISLogClean(Date nextStartDay) {
        Runnable iisCleaner = new MailIISLogsCleaner();
        thrConfig.getTaskScheduler().scheduleWithFixedDelay(iisCleaner, nextStartDay, ConstantsFor.ONE_WEEK_MILLIS);
        getMiniLogger().add(nextStartDay + " MailIISLogsCleaner() start\n");
        AppComponents.threadConfig().getTaskScheduler().getScheduledThreadPoolExecutor()
            .scheduleWithFixedDelay(AppInfoOnLoad::dbSendFile, 0, ConstantsFor.DELAY, TimeUnit.MINUTES);
        
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
        AppComponents.threadConfig().getTaskScheduler().getScheduledThreadPoolExecutor()
            .scheduleWithFixedDelay(new AppComponents().pcNamesScanner(), 0, ConstantsFor.DELAY, TimeUnit.MINUTES);
    
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
    
    private static void dbSendFile() {
        DataConnectTo dataConnectTo = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I);
        dataConnectTo.createTable(ConstantsFor.DBNAME_LOG_DBMESSENGER, Collections.emptyList());
        final String sql = "INSERT INTO log.dbmessenger (`tstamp`, `upstring`, `json`) VALUES (?, ?, ?)";
        try (Connection connection = dataConnectTo.getDefaultConnection(ConstantsFor.DBNAME_LOG_DBMESSENGER);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            Path path = Paths.get(FileNames.APP_JSON);
            Queue<String> logJson = FileSystemWorker.readFileToQueue(path);
            while (!logJson.isEmpty()) {
                String jsonStr = logJson.remove();
                if (!jsonStr.isEmpty()) {
                    JsonObject jsonObject = Json.parse(jsonStr).asObject();
                    preparedStatement.setTimestamp(1, Timestamp
                        .valueOf(LocalDateTime.ofEpochSecond((jsonObject.getLong(PropertiesNames.TIMESTAMP, 0) / 1000), 0, ZoneOffset.ofHours(3))));
                    preparedStatement.setString(2, UsefulUtilities.thisPC());
                    preparedStatement.setString(3, jsonObject.toString());
                    preparedStatement.executeUpdate();
                }
                
            }
            Files.deleteIfExists(path);
        }
        catch (SQLException | IOException e) {
            System.err.println(MessageFormat.format("{0}, message: {1}. See line: 176 ***", DBMessenger.class.getSimpleName(), e.getMessage()));
        }
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
            thrConfig.execByThreadConfig(kudrWorkTime, "AppInfoOnLoad.kudrMonitoring");
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
