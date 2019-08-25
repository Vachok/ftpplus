// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import ru.vachok.messenger.MessageCons;
import ru.vachok.networker.ad.usermanagement.RightsChecker;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.data.NetKeeper;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.componentsrepo.data.enums.FileNames;
import ru.vachok.networker.componentsrepo.data.enums.PropertiesNames;
import ru.vachok.networker.componentsrepo.fileworks.DeleterTemp;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.componentsrepo.services.MyCalen;
import ru.vachok.networker.exe.ThreadConfig;
import ru.vachok.networker.exe.runnabletasks.external.SaveLogsToDB;
import ru.vachok.networker.exe.schedule.MailIISLogsCleaner;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.info.Stats;
import ru.vachok.networker.mail.testserver.MailPOPTester;
import ru.vachok.networker.net.monitor.DiapazonScan;
import ru.vachok.networker.net.monitor.KudrWorkTime;
import ru.vachok.networker.net.monitor.NetMonitorPTV;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.DBMessenger;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.ssh.Tracerouting;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static java.time.DayOfWeek.SUNDAY;


/**
 @see ru.vachok.networker.AppInfoOnLoadTest
 @since 19.12.2018 (9:40) */
public class AppInfoOnLoad implements Runnable {
    
    /**
     {@link MessageCons}
     */
    private static final MessageToUser MESSAGE_LOCAL = new MessageLocal(AppInfoOnLoad.class.getSimpleName());
    
    @SuppressWarnings("StaticVariableOfConcreteClass")
    private static final ThreadConfig thrConfig = AppComponents.threadConfig();
    
    private static final ScheduledThreadPoolExecutor SCHED_EXECUTOR = thrConfig.getTaskScheduler().getScheduledThreadPoolExecutor();
    
    private static final List<String> MINI_LOGGER = new ArrayList<>();
    
    private static int thisDelay = UsefulUtilities.getScansDelay();
    
    private InformationFactory informationFactory = InformationFactory.getInstance(InformationFactory.INET_USAGE);
    
    private MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, this.getClass().getSimpleName());
    
    @Override
    public void run() {
        AppComponents.getUserPref().putInt(SaveLogsToDB.class.getSimpleName(), new SaveLogsToDB().getDBInfo());
        AppComponents.getUserPref();
        FileSystemWorker.writeFile("availableCharsets.txt", new TForms().fromArray(Charset.availableCharsets()));
        thrConfig.execByThreadConfig(AppInfoOnLoad::setCurrentProvider);
        delFilePatterns(UsefulUtilities.getStringsVisit());
        thrConfig.execByThreadConfig(AppInfoOnLoad::runCommonScan);
        try {
            infoForU();
            getWeekPCStats();
        }
        catch (RuntimeException e) {
            MESSAGE_LOCAL.error(e.getMessage());
        }
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AppInfoOnLoad{");
        sb.append(", thisDelay=").append(thisDelay);
        sb.append("<br>").append(new TForms().fromArray(getMiniLogger(), true));
        sb.append('}');
        return sb.toString();
    }
    
    /**
     Для записи результата работы класса.
     */
    @Contract(pure = true)
    protected static List<String> getMiniLogger() {
        return MINI_LOGGER;
    }
    
    /**
     Сборщик прав \\srv-fs.eatmeat.ru\common_new
     <p>
     {@link Files#walkFileTree(Path, java.nio.file.FileVisitor)}, где {@link Path} = \\srv-fs.eatmeat.ru\common_new и {@link FileVisitor}
     = new {@link RightsChecker}.
     <p>
     <b>{@link IOException}:</b><br>
     {@link MessageToUser#errorAlert(String, String, String)},
     {@link FileSystemWorker#error(String, Exception)}
     */
    private static void runCommonScan() {
        Path pathStart = Paths.get("\\\\srv-fs.eatmeat.ru\\it$$\\Хлам\\");
        Path pathToSaveLogs = Paths.get(".");
    
        if (UsefulUtilities.thisPC().toLowerCase().contains("rups")) {
            pathStart = Paths.get("\\\\srv-fs.eatmeat.ru\\common_new");
            pathToSaveLogs = Paths.get("\\\\srv-fs.eatmeat.ru\\Common_new\\14_ИТ_служба\\Внутренняя");
        }
        if (new File(FileNames.FILENAME_COMMONRGH).exists()) {
            new File(FileNames.FILENAME_COMMONRGH).delete();
        }
        if (new File(FileNames.FILENAME_COMMONOWN).exists()) {
            new File(FileNames.FILENAME_COMMONOWN).delete();
        }
        Runnable checker = new RightsChecker(pathStart, pathToSaveLogs);
        AppComponents.threadConfig().execByThreadConfig(checker);
    }
    
    private static void delFilePatterns(@NotNull String[] patToDelArr) {
        File file = new File(".");
        for (String patToDel : patToDelArr) {
            FileVisitor<Path> deleterTemp = new DeleterTemp(Collections.singletonList(patToDel));
            try {
                Files.walkFileTree(file.toPath(), deleterTemp);
            }
            catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }
    
    /**
     Немного инфомации о приложении.
     */
    private void infoForU() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(UsefulUtilities.getBuildStamp());
        MESSAGE_LOCAL.info("AppInfoOnLoad.infoForU", ConstantsFor.STR_FINISH, " = " + stringBuilder);
        getMiniLogger().add("infoForU ends. now ftpUploadTask(). Result: " + stringBuilder);
        try {
            MESSAGE_LOCAL.info(UsefulUtilities.getIISLogSize());
        }
        catch (NullPointerException e) {
            MESSAGE_LOCAL.error(MessageFormat.format("AppInfoOnLoad.infoForU threw away: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
        ftpUploadTask();
    }
    
    private void ftpUploadTask() {
        MESSAGE_LOCAL.warn(PropertiesNames.PR_OSNAME_LOWERCASE);
        AppInfoOnLoad.getMiniLogger().add(UsefulUtilities.thisPC());
        String ftpUpload = "new AppComponents().launchRegRuFTPLibsUploader() = " + new AppComponents().launchRegRuFTPLibsUploader();
        getMiniLogger().add(ftpUpload);
        this.startPeriodicTasks();
    }
    
    private void startPeriodicTasks() {
        Runnable netMonPTVRun = new NetMonitorPTV();
        Runnable tmpFullInetRun = new AppComponents().temporaryFullInternet();
        Runnable scanOnlineRun = new AppComponents().scanOnline();
        Runnable diapazonScanRun = DiapazonScan.getInstance();
        Runnable istranetOrFortexRun = AppInfoOnLoad::setCurrentProvider;
        Runnable popSmtpTest = new MailPOPTester();
        SCHED_EXECUTOR.scheduleWithFixedDelay(UsefulUtilities::getCPUTime, 1, 3, TimeUnit.SECONDS);
        SCHED_EXECUTOR.scheduleWithFixedDelay(netMonPTVRun, 10, 10, TimeUnit.SECONDS);
        SCHED_EXECUTOR.scheduleWithFixedDelay(istranetOrFortexRun, ConstantsFor.DELAY, ConstantsFor.DELAY * thisDelay, TimeUnit.SECONDS);
        SCHED_EXECUTOR.scheduleWithFixedDelay(popSmtpTest, ConstantsFor.DELAY * 2, thisDelay, TimeUnit.MINUTES);
        SCHED_EXECUTOR.scheduleWithFixedDelay(tmpFullInetRun, 1, ConstantsFor.DELAY, TimeUnit.MINUTES);
        SCHED_EXECUTOR.scheduleWithFixedDelay(diapazonScanRun, 2, UsefulUtilities.getScansDelay(), TimeUnit.MINUTES);
        SCHED_EXECUTOR.scheduleWithFixedDelay(scanOnlineRun, 3, 2, TimeUnit.MINUTES);
        SCHED_EXECUTOR
            .scheduleAtFixedRate(()->DBMessenger.getInstance(this.getClass().getSimpleName()).info(this.databaseLogSquidSave()), 4, thisDelay, TimeUnit.MINUTES);
        getMiniLogger().add(thrConfig.toString());
        this.startIntervalTasks();
    }
    
    /**
     Трэйсроуте до 8.8.8.8
     <p>
     С целью определения шлюза по-умолчанию, и соотв. провайдера.
     
     @see AppComponents#sshActs()
     */
    private static void setCurrentProvider() {
        try {
            NetKeeper.setCurrentProvider(new Tracerouting().call());
        }
        catch (Exception e) {
            NetKeeper.setCurrentProvider("<br><a href=\"/makeok\">" + e.getMessage() + "</a><br>");
            Thread.currentThread().interrupt();
        }
    }
    
    private String databaseLogSquidSave() {
        String infoAbout = informationFactory.getInfoAbout("100");
        informationFactory.writeLog(this.getClass().getSimpleName() + ".log", infoAbout);
        return infoAbout;
    }
    
    @SuppressWarnings("MagicNumber")
    private void startIntervalTasks() {
        Date nextStartDay = MyCalen.getNextDayofWeek(23, 57, SUNDAY);
        scheduleStats(nextStartDay);
        nextStartDay = new Date(nextStartDay.getTime() - TimeUnit.HOURS.toMillis(1));
        scheduleIISLogClean(nextStartDay);
        this.kudrMonitoring();
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
    
    protected void kudrMonitoring() {
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
        MESSAGE_LOCAL.warn(MessageFormat.format("{0} starts at {1}", kudrWorkTime.toString(), next9AM));
        AppComponents.onePCMonStart();
        MESSAGE_LOCAL.info(this.databaseLogSquidSave());
    }
    
    private boolean checkFileExitLastAndWriteMiniLog() {
        StringBuilder exitLast = new StringBuilder();
        if (new File("exit.last").exists()) {
            exitLast.append(new TForms().fromArray(FileSystemWorker.readFileToList("exit.last"), false));
        }
        exitLast.append("\n").append(MyCalen.planTruncateTableUsers(SCHED_EXECUTOR)).append("\n");
        getMiniLogger().add(exitLast.toString());
        return FileSystemWorker.writeFile(this.getClass().getSimpleName() + ".mini", getMiniLogger().stream());
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
        MESSAGE_LOCAL.warn(this.getClass().getSimpleName(), checkFileExitLastAndWriteMiniLog() + " checkFileExitLastAndWriteMiniLog", toString());
    }
    
}
