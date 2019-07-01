// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.abstr.InternetUse;
import ru.vachok.networker.accesscontrol.common.CommonRightsChecker;
import ru.vachok.networker.accesscontrol.inetstats.InetUserPCName;
import ru.vachok.networker.controller.MatrixCtr;
import ru.vachok.networker.exe.ThreadConfig;
import ru.vachok.networker.exe.runnabletasks.NetMonitorPTV;
import ru.vachok.networker.exe.schedule.DiapazonScan;
import ru.vachok.networker.exe.schedule.MailIISLogsCleaner;
import ru.vachok.networker.exe.schedule.SquidAvailabilityChecker;
import ru.vachok.networker.exe.schedule.WeekStats;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.mailserver.testserver.MailPOPTester;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.services.MessageLocal;
import ru.vachok.networker.services.MyCalen;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


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
    
    private static final ThreadConfig thrConfig = AppComponents.threadConfig();
    
    /**
     Для записи результата работы класса.
     */
    protected static final List<String> MINI_LOGGER = new ArrayList<>();
    
    private static int thisDelay = getScansDelay();
    
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
    public static String getIISLogSize() {
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
            if (hostName.equalsIgnoreCase(ConstantsFor.HOSTNAME_DO213) || hostName.toLowerCase().contains(ConstantsFor.HOSTNAME_HOME)) {
                appPr.setProperty(ConstantsFor.PR_APP_BUILDTIME, String.valueOf(System.currentTimeMillis()));
                retLong = System.currentTimeMillis();
            }
            else {
                retLong = Long.parseLong(appPr.getProperty(ConstantsFor.PR_APP_BUILDTIME, "1"));
            }
        }
        catch (UnknownHostException | NumberFormatException e) {
            System.err.println(e.getMessage() + " " + AppInfoOnLoad.class.getSimpleName() + ".getBuildStamp");
        }
        thrConfig.getTaskExecutor().execute(()->{
            try {
                new AppComponents().updateProps(appPr);
            }
            catch (IOException e) {
                System.err.println(e.getMessage() + " " + AppInfoOnLoad.class.getSimpleName() + ".getBuildStamp");
            }
        });
        return retLong;
    }
    
    /**
     Старт
     <p>
     {@link #infoForU()}
     <p>
     @see ru.vachok.networker.AppInfoOnLoadTest#testRun()
     */
    @Override
    public void run() {
        try {
            infoForU();
            getWeekPCStats();
        }
        catch (Exception e) {
            MESSAGE_LOCAL.error(e.getMessage());
        }
    }
    
    @Override public String toString() {
        final StringBuilder sb = new StringBuilder("AppInfoOnLoad{");
        sb.append(", thisDelay=").append(thisDelay);
        sb.append("<br>").append(new TForms().fromArray(MINI_LOGGER, true));
        sb.append('}');
        return sb.toString();
    }
    
    /**
     Стата за неделю по-ПК
     <p>
     1. {@link MyCalen#getNextDayofWeek(int, int, java.time.DayOfWeek)}. Получим {@link Date}, след. воскресенье 23:57.<br>
     {@link ThreadPoolTaskScheduler}, запланируем new {@link WeekStats} и new {@link MailIISLogsCleaner} на это время и на это время -1 час.<br><br>
     2. {@link FileSystemWorker#readFileToList(java.lang.String)}. Прочитаем exit.last, если он существует.
     {@link TForms#fromArray(java.util.List, boolean)} <br><br>
     3. {@link MyCalen#checkDay(ScheduledExecutorService)} метрика. <br>
     4. {@link MyCalen#checkDay(ScheduledExecutorService)}. Выведем сообщение, когда и что ствртует.
     <p>
 
     @param scheduledExecService {@link ScheduledExecutorService}.
     */
    @SuppressWarnings("MagicNumber")
    private static void dateSchedulers(ScheduledExecutorService scheduledExecService) {
        long delay = TimeUnit.HOURS.toMillis(ConstantsFor.ONE_DAY_HOURS * 7);
        String exitLast = "No file";
        thrConfig.thrNameSet("dateSch");
        Date nextStartDay = MyCalen.getNextDayofWeek(23, 57, DayOfWeek.SUNDAY);
        StringBuilder stringBuilder = new StringBuilder();
        ThreadPoolTaskScheduler threadPoolTaskScheduler = thrConfig.getTaskScheduler();
    
        threadPoolTaskScheduler.scheduleWithFixedDelay(new WeekStats(ConstantsFor.SQL_SELECTFROM_PCUSERAUTO), nextStartDay, delay);
        stringBuilder.append(nextStartDay).append(" WeekPCStats() start\n");
        nextStartDay = new Date(nextStartDay.getTime() - TimeUnit.HOURS.toMillis(1));
    
        threadPoolTaskScheduler.scheduleWithFixedDelay(new MailIISLogsCleaner(), nextStartDay, delay);
        stringBuilder.append(nextStartDay).append(" MailIISLogsCleaner() start\n");
    
        if (new File("exit.last").exists()) {
            exitLast = new TForms().fromArray(FileSystemWorker.readFileToList("exit.last"), false);
        }
        exitLast = exitLast + "\n" + MyCalen.checkDay(scheduledExecService) + "\n" + stringBuilder;
        MINI_LOGGER.add(exitLast);
        MESSAGE_LOCAL.info(AppInfoOnLoad.class.getSimpleName() + ConstantsFor.STR_FINISH);
        boolean isWrite = FileSystemWorker.writeFile(CLASS_NAME + ".mini", MINI_LOGGER.stream());
        scheduledExecService.schedule(AppInfoOnLoad::runCommonScan, thisDelay * 2, TimeUnit.SECONDS);
        Date furyDate = MyCalen.getNextDayofWeek(17, 40, DayOfWeek.TUESDAY);
//        threadPoolTaskScheduler.scheduleWithFixedDelay(new AdminFury(), furyDate, delay); todo 01.07.2019 (16:51)
        MESSAGE_LOCAL.info(CLASS_NAME + " = " + isWrite);
    }
    
    @SuppressWarnings("MagicNumber") private static int getScansDelay() {
        int scansInOneMin = Integer.parseInt(AppComponents.getUserPref().get(ConstantsFor.PR_SCANSINMIN, "111"));
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
     = new {@link CommonRightsChecker}.
     <p>
     <b>{@link IOException}:</b><br>
     {@link MessageToUser#errorAlert(String, String, String)},
     {@link FileSystemWorker#error(String, Exception)}
     */
    private static void runCommonScan() {
        CommonRightsChecker commonRightsChecker = new CommonRightsChecker(
            Paths.get("\\\\srv-fs.eatmeat.ru\\common_new"),
            Paths.get("\\\\srv-fs.eatmeat.ru\\Common_new\\14_ИТ_служба\\Внутренняя"));
        if (ConstantsFor.thisPC().toLowerCase().contains("rups")) {
            Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).execute(commonRightsChecker);
        }
        else {
            MESSAGE_LOCAL.warn(commonRightsChecker + " NOT RUN ON: " + ConstantsFor.thisPC());
        }
    }
    
    /**
     Немного инфомации о приложении.
     */
    private void infoForU() throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ConstantsFor.APP_VERSION).append("\n");
        stringBuilder.append(getBuildStamp());
        MESSAGE_LOCAL.info("AppInfoOnLoad.infoForU", ConstantsFor.STR_FINISH, " = " + stringBuilder);
        MINI_LOGGER.add("infoForU ends. now schedStarter(). Result: " + stringBuilder);
        try {
            MESSAGE_LOCAL.info(getClass().getSimpleName() + ".run", ConstantsFor.APP_VERSION, " = " + getIISLogSize());
        }
        catch (NullPointerException e) {
            System.err.println(e.getMessage() + " " + getClass().getSimpleName() + ".infoForU");
        }
        schedStarter();
    }
    
    /**
     Запуск заданий по-расписанию
     <p>
     Usages: {@link #infoForU()} <br>
     Uses: 1.1 {@link #dateSchedulers(ScheduledExecutorService)}, 1.2 {@link ConstantsFor#thisPC()}, 1.3 {@link ConstantsFor#thisPC()}.
     */
    private void schedStarter() throws Exception {
        String osName = ConstantsFor.PR_OSNAME_LOWERCASE;
        MESSAGE_LOCAL.warn(osName);
        ScheduledThreadPoolExecutor scheduledExecutorService = thrConfig.getTaskScheduler().getScheduledThreadPoolExecutor();
        String thisPC = ConstantsFor.thisPC();
        AppInfoOnLoad.MINI_LOGGER.add(thisPC);
        System.out.println("new AppComponents().launchRegRuFTPLibsUploader() = " + new AppComponents().launchRegRuFTPLibsUploader());
        schedWithService(scheduledExecutorService);
    }
    
    private void schedWithService(ScheduledExecutorService scheduledExecService) throws Exception {
        Runnable squidAvailabilityCheckerRun = new SquidAvailabilityChecker();
        Runnable netMonPTVRun = new NetMonitorPTV();
        Runnable tmpFullInetRun = new AppComponents().temporaryFullInternet();
        Runnable scanOnlineRun = new AppComponents().scanOnline();
        Runnable logsSaverRun = AppInfoOnLoad::squidLogsSave;
        Runnable diapazonScanRun = DiapazonScan.getInstance();
        Runnable istranetOrFortexRun = MatrixCtr::setCurrentProvider;
        Runnable popSmtpTest = new MailPOPTester();
        
        scheduledExecService.scheduleWithFixedDelay(netMonPTVRun, 0, 10, TimeUnit.SECONDS);
        scheduledExecService.scheduleWithFixedDelay(istranetOrFortexRun, ConstantsFor.DELAY, ConstantsFor.DELAY * thisDelay, TimeUnit.SECONDS);
        scheduledExecService.scheduleWithFixedDelay(popSmtpTest, ConstantsFor.DELAY * 2, ConstantsFor.DELAY * 40, TimeUnit.SECONDS);
        scheduledExecService.scheduleWithFixedDelay(tmpFullInetRun, 1, ConstantsFor.DELAY, TimeUnit.MINUTES);
        scheduledExecService.scheduleWithFixedDelay(diapazonScanRun, 2, AppInfoOnLoad.thisDelay, TimeUnit.MINUTES);
        scheduledExecService.scheduleWithFixedDelay(scanOnlineRun, 3, 2, TimeUnit.MINUTES);
        scheduledExecService.scheduleWithFixedDelay(logsSaverRun, 4, thisDelay, TimeUnit.MINUTES);
        scheduledExecService.scheduleWithFixedDelay(squidAvailabilityCheckerRun, 5, ConstantsFor.DELAY * 4, TimeUnit.MINUTES);
        
        MINI_LOGGER.add(thrConfig.toString());
        
        AppInfoOnLoad.dateSchedulers(scheduledExecService);
    }
    
    private static void getWeekPCStats() {
        if (LocalDate.now().getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
            thrConfig.execByThreadConfig(new WeekStats(ConstantsFor.SQL_SELECTFROM_PCUSERAUTO));
        }
    }
    
    private static void squidLogsSave() {
        new AppComponents().saveLogsToDB().startScheduled();
        InternetUse internetUse = new InetUserPCName();
        System.out.println("internetUse.cleanTrash() = " + internetUse.cleanTrash());
    }
}
