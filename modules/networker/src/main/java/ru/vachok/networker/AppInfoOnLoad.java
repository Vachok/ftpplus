// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.abstr.NetKeeper;
import ru.vachok.networker.accesscontrol.common.usermanagement.RightsChecker;
import ru.vachok.networker.accesscontrol.sshactions.Tracerouting;
import ru.vachok.networker.enums.ConstantsNet;
import ru.vachok.networker.enums.FileNames;
import ru.vachok.networker.enums.OtherKnownDevices;
import ru.vachok.networker.enums.PropertiesNames;
import ru.vachok.networker.exe.ThreadConfig;
import ru.vachok.networker.exe.runnabletasks.external.SaveLogsToDB;
import ru.vachok.networker.exe.schedule.MailIISLogsCleaner;
import ru.vachok.networker.exe.schedule.WeekStats;
import ru.vachok.networker.fileworks.DeleterTemp;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.mailserver.testserver.MailPOPTester;
import ru.vachok.networker.net.monitor.DiapazonScan;
import ru.vachok.networker.net.monitor.KudrWorkTime;
import ru.vachok.networker.net.monitor.NetMonitorPTV;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.restapi.props.DBPropsCallable;
import ru.vachok.networker.services.MyCalen;
import ru.vachok.networker.statistics.Stats;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static java.time.DayOfWeek.SUNDAY;


/**
 Информация и шедулеры.
 <p>
 Перемещено из {@link IntoApplication}.
 <p>
 
 @see ru.vachok.networker.AppInfoOnLoadTest
 @since 19.12.2018 (9:40) */
public class AppInfoOnLoad implements Runnable {
    
    
    /**
     {@link Class#getSimpleName()}
     */
    private static final String CLASS_NAME = AppInfoOnLoad.class.getSimpleName();
    
    /**
     {@link AppComponents#getProps()}
     */
    private static final Properties APP_PROPS = AppComponents.getProps();
    
    /**
     {@link MessageCons}
     */
    private static final MessageToUser MESSAGE_LOCAL = new MessageLocal(AppInfoOnLoad.class.getSimpleName());
    
    @SuppressWarnings("StaticVariableOfConcreteClass")
    private static final ThreadConfig thrConfig = AppComponents.threadConfig();
    
    private static final ScheduledThreadPoolExecutor SCHED_EXECUTOR = thrConfig.getTaskScheduler().getScheduledThreadPoolExecutor();
    
    /**
     Для записи результата работы класса.
     */
    protected static final List<String> MINI_LOGGER = new ArrayList<>();
    
    private static int thisDelay = getScansDelay();
    
    private Stats stats;
    
    @Contract(pure = true)
    public static int getThisDelay() {
        return thisDelay;
    }
    
    /**
     Получение размера логов IIS-Exchange.
     <p>
     Путь до папки из {@link #APP_PROPS} iispath. <br> {@code Path iisLogsDir} = {@link Objects#requireNonNull(java.lang.Object)} -
     {@link Path#toFile()}.{@link File#listFiles()}. <br> Для каждого
     файла из папки, {@link File#length()}. Складываем {@code totalSize}. <br> {@code totalSize/}{@link ConstantsFor#MBYTE}.
 
     @return размер папки логов IIS в мегабайтах
     */
    public static @NotNull String getIISLogSize() {
        Path iisLogsDir = Paths.get(APP_PROPS.getProperty("iispath", "\\\\srv-mail3.eatmeat.ru\\c$\\inetpub\\logs\\LogFiles\\W3SVC1\\"));
        long totalSize = 0L;
        for (File x : Objects.requireNonNull(iisLogsDir.toFile().listFiles())) {
            totalSize += x.length();
        }
        String s = totalSize / ConstantsFor.MBYTE + " MB IIS Logs\n";
        MINI_LOGGER.add(s);
        return s;
    }
    
    /**
     @return время билда
     */
    public static long getBuildStamp() {
        long retLong = 1L;
        Properties appPr = AppComponents.getProps();
        try {
            String hostName = InetAddress.getLocalHost().getHostName();
            if (hostName.equalsIgnoreCase(OtherKnownDevices.DO0213_KUDR) || hostName.toLowerCase().contains(OtherKnownDevices.HOSTNAME_HOME)) {
                appPr.setProperty(PropertiesNames.PR_APP_BUILDTIME, String.valueOf(System.currentTimeMillis()));
                retLong = System.currentTimeMillis();
            }
            else {
                retLong = Long.parseLong(appPr.getProperty(PropertiesNames.PR_APP_BUILDTIME, "1"));
            }
        }
        catch (UnknownHostException | NumberFormatException e) {
            System.err.println(e.getMessage() + " " + AppInfoOnLoad.class.getSimpleName() + ".getBuildStamp");
        }
        boolean isAppPropsSet = new DBPropsCallable().setProps(appPr);
        return retLong;
    }
    
    @Override
    public void run() {
        FileSystemWorker.writeFile("availableCharsets.txt", new TForms().fromArray(Charset.availableCharsets()));
        thrConfig.execByThreadConfig(AppInfoOnLoad::setCurrentProvider);
        delFilePatterns(UsefulUtilities.getStringsVisit());
        thrConfig.execByThreadConfig(AppInfoOnLoad::runCommonScan);
        try {
            infoForU();
            getWeekPCStats();
            AppComponents.getUserPref().putInt(SaveLogsToDB.class.getSimpleName(), new SaveLogsToDB().getDBInfo());
            AppComponents.getUserPref();
        }
        catch (RuntimeException e) {
            MESSAGE_LOCAL.error(e.getMessage());
        }
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
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AppInfoOnLoad{");
        sb.append(", thisDelay=").append(thisDelay);
        sb.append("<br>").append(new TForms().fromArray(MINI_LOGGER, true));
        sb.append('}');
        return sb.toString();
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
            taskScheduler.scheduleWithFixedDelay(kudrWorkTime, next9AM, TimeUnit.HOURS.toMillis(UsefulUtilities.ONE_DAY_HOURS));
        }
        else {
            next9AM = MyCalen.getNextDay(8, 30);
            taskScheduler.scheduleWithFixedDelay(kudrWorkTime, next9AM, TimeUnit.HOURS.toMillis(UsefulUtilities.ONE_DAY_HOURS));
        }
        if (secondOfDayNow > 40000) {
            thrConfig.execByThreadConfig(kudrWorkTime);
        }
        MESSAGE_LOCAL.warn(MessageFormat.format("{0} starts at {1}", kudrWorkTime.toString(), next9AM));
        AppComponents.onePCMonStart();
    }
    
    @SuppressWarnings("MagicNumber")
    private void startIntervalTasks() {
        Date nextStartDay = MyCalen.getNextDayofWeek(23, 57, SUNDAY);
        scheduleStats(nextStartDay);
        nextStartDay = new Date(nextStartDay.getTime() - TimeUnit.HOURS.toMillis(1));
        scheduleIISLogClean(nextStartDay);
        kudrMonitoring();
    }
    
    private boolean checkFileExitLastAndWriteMiniLog() {
        StringBuilder exitLast = new StringBuilder();
        if (new File("exit.last").exists()) {
            exitLast.append(new TForms().fromArray(FileSystemWorker.readFileToList("exit.last"), false));
        }
        exitLast.append("\n").append(MyCalen.planTruncateTableUsers(SCHED_EXECUTOR)).append("\n");
        MINI_LOGGER.add(exitLast.toString());
        return FileSystemWorker.writeFile(CLASS_NAME + ".mini", MINI_LOGGER.stream());
    }
    
    private static void scheduleIISLogClean(Date nextStartDay) {
        Runnable iisCleaner = new MailIISLogsCleaner();
        thrConfig.getTaskScheduler().scheduleWithFixedDelay(iisCleaner, nextStartDay, ConstantsFor.ONE_WEEK_MILLIS);
        MINI_LOGGER.add(nextStartDay + " MailIISLogsCleaner() start\n");
    }
    
    private void scheduleStats(Date nextStartDay) {
        this.stats = new WeekStats();
        thrConfig.getTaskScheduler().scheduleWithFixedDelay(()->stats.getPCStats(), nextStartDay, ConstantsFor.ONE_WEEK_MILLIS);
        thrConfig.getTaskScheduler().scheduleWithFixedDelay(()->stats.getInetStats(), nextStartDay, ConstantsFor.ONE_WEEK_MILLIS);
        MINI_LOGGER.add(nextStartDay + " WeekPCStats() start\n");
    }
    
    @SuppressWarnings("MagicNumber")
    private static int getScansDelay() {
        int scansInOneMin = Integer.parseInt(AppComponents.getUserPref().get(PropertiesNames.PR_SCANSINMIN, "111"));
        if (scansInOneMin <= 0) {
            scansInOneMin = 85;
        }
        if (scansInOneMin > 800) {
            scansInOneMin = 800;
        }
        return ConstantsNet.IPS_IN_VELKOM_VLAN / scansInOneMin;
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
        try {
            Thread.sleep(1000);
        }
        catch (InterruptedException e) {
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
    
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
        thrConfig.execByThreadConfig(checker);
    }
    
    /**
     Немного инфомации о приложении.
     */
    private void infoForU() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getBuildStamp());
        MESSAGE_LOCAL.info("AppInfoOnLoad.infoForU", ConstantsFor.STR_FINISH, " = " + stringBuilder);
        MINI_LOGGER.add("infoForU ends. now ftpUploadTask(). Result: " + stringBuilder);
        try {
            MESSAGE_LOCAL.info(getIISLogSize());
        }
        catch (NullPointerException e) {
            MESSAGE_LOCAL.error(MessageFormat.format("AppInfoOnLoad.infoForU threw away: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
        ftpUploadTask();
    }
    
    private void ftpUploadTask() {
        MESSAGE_LOCAL.warn(PropertiesNames.PR_OSNAME_LOWERCASE);
        AppInfoOnLoad.MINI_LOGGER.add(UsefulUtilities.thisPC());
        String ftpUpload = "new AppComponents().launchRegRuFTPLibsUploader() = " + new AppComponents().launchRegRuFTPLibsUploader();
        MINI_LOGGER.add(ftpUpload);
        startPeriodicTasks();
    }
    
    private void startPeriodicTasks() {
        Runnable netMonPTVRun = new NetMonitorPTV();
        Runnable tmpFullInetRun = new AppComponents().temporaryFullInternet();
        Runnable scanOnlineRun = new AppComponents().scanOnline();
        Runnable logsSaverRun = ()->SaveLogsToDB.getI().startScheduled();
        Runnable diapazonScanRun = DiapazonScan.getInstance();
        Runnable istranetOrFortexRun = AppInfoOnLoad::setCurrentProvider;
        Runnable popSmtpTest = new MailPOPTester();
        long srvMail3TestDelay = ConstantsFor.DELAY * UsefulUtilities.MY_AGE;
        SCHED_EXECUTOR.scheduleWithFixedDelay(netMonPTVRun, 10, 10, TimeUnit.SECONDS);
        SCHED_EXECUTOR.scheduleWithFixedDelay(istranetOrFortexRun, ConstantsFor.DELAY, ConstantsFor.DELAY * thisDelay, TimeUnit.SECONDS);
        SCHED_EXECUTOR.scheduleWithFixedDelay(popSmtpTest, ConstantsFor.DELAY * 2, srvMail3TestDelay, TimeUnit.SECONDS);
        SCHED_EXECUTOR.scheduleWithFixedDelay(tmpFullInetRun, 1, ConstantsFor.DELAY, TimeUnit.MINUTES);
        SCHED_EXECUTOR.scheduleWithFixedDelay(diapazonScanRun, 2, AppInfoOnLoad.thisDelay, TimeUnit.MINUTES);
        SCHED_EXECUTOR.scheduleWithFixedDelay(scanOnlineRun, 3, 2, TimeUnit.MINUTES);
        SCHED_EXECUTOR.scheduleWithFixedDelay(logsSaverRun, 4, AppInfoOnLoad.thisDelay, TimeUnit.MINUTES);
        MINI_LOGGER.add(thrConfig.toString());
        this.startIntervalTasks();
    }
    
    private void getWeekPCStats() {
        if (LocalDate.now().getDayOfWeek().equals(SUNDAY)) {
            thrConfig.execByThreadConfig(()->stats.getPCStats());
            thrConfig.execByThreadConfig(()->stats.getInetStats());
        }
        MESSAGE_LOCAL.warn(this.getClass().getSimpleName(), checkFileExitLastAndWriteMiniLog() + " checkFileExitLastAndWriteMiniLog", toString());
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
}
