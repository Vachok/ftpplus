// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.abstr.InternetUse;
import ru.vachok.networker.accesscontrol.common.CommonRightsChecker;
import ru.vachok.networker.accesscontrol.common.CommonSRV;
import ru.vachok.networker.accesscontrol.inetstats.InetUserPCName;
import ru.vachok.networker.accesscontrol.sshactions.SquidChecker;
import ru.vachok.networker.componentsrepo.VersionInfo;
import ru.vachok.networker.exe.runnabletasks.ScanOnline;
import ru.vachok.networker.exe.runnabletasks.TemporaryFullInternet;
import ru.vachok.networker.exe.schedule.DiapazonScan;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.mailserver.MailIISLogsCleaner;
import ru.vachok.networker.net.NetMonitorPTV;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.services.MessageLocal;
import ru.vachok.networker.services.MyCalen;
import ru.vachok.networker.statistics.WeekStats;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 Информация и шедулеры.
 <p>
 Перемещено из {@link IntoApplication}.
 
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
    private static final MessageToUser messageToUser = new MessageLocal(AppInfoOnLoad.class.getSimpleName());
    
    /**
     Для записи результата работы класса.
     */
    private static final List<String> miniLogger = new ArrayList<>();
    
    /**
     {@link AppComponents#temporaryFullInternet()}
     
     @see TemporaryFullInternet
     */
    private final TemporaryFullInternet temporaryFullInternet = new AppComponents().temporaryFullInternet();
    
    static String unixThreadInfo = System.getProperty("os.name");
    
    private static int thisDelay = ConstantsNet.IPS_IN_VELKOM_VLAN / getScansDelay();
    
    public static String getUnixThreadInfo() {
        return unixThreadInfo;
    }
    
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
    public static String iisLogSize() {
        Path iisLogsDir = Paths.get(APP_PROPS.getProperty("iispath", "\\\\srv-mail3.eatmeat.ru\\c$\\inetpub\\logs\\LogFiles\\W3SVC1\\"));
        long totalSize = 0L;
        for (File x : Objects.requireNonNull(iisLogsDir.toFile().listFiles())) {
            totalSize += x.length();
        }
        String s = totalSize / ConstantsFor.MBYTE + " MB IIS Logs\n";
        miniLogger.add(s);
        return s;
    }
    
    /**
     Старт
     <p>
     {@link #infoForU()}
     */
    @Override
    public void run() {
        try {
            infoForU();
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
        }
    }
    
    @Override public String toString() {
        final StringBuilder sb = new StringBuilder("AppInfoOnLoad{");
        sb.append(", thisDelay=").append(thisDelay);
        sb.append("<br>").append(new TForms().fromArray(miniLogger, true));
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
     
     @param scheduledExecutorService {@link ScheduledExecutorService}.
     */
    @SuppressWarnings("MagicNumber")
    static void dateSchedulers(ScheduledExecutorService scheduledExecutorService) {
        long stArt = System.currentTimeMillis();
        long delay = TimeUnit.HOURS.toMillis(ConstantsFor.ONE_DAY_HOURS * 7);
    
        String classMeth = "AppInfoOnLoad.dateSchedulers";
        String exitLast = "No file";
        AppComponents.threadConfig().thrNameSet("dateSch");
        Date nextStartDay = MyCalen.getNextDayofWeek(23, 57, DayOfWeek.SUNDAY);
        StringBuilder stringBuilder = new StringBuilder();
        ThreadPoolTaskScheduler threadPoolTaskScheduler = AppComponents.threadConfig().getTaskScheduler();
    
        threadPoolTaskScheduler.scheduleWithFixedDelay(new WeekStats(ConstantsFor.SQL_SELECTFROM_PCUSERAUTO), nextStartDay, delay);
        stringBuilder.append(nextStartDay).append(" WeekPCStats() start\n");
        nextStartDay = new Date(nextStartDay.getTime() - TimeUnit.HOURS.toMillis(1));
    
        threadPoolTaskScheduler.scheduleWithFixedDelay(new MailIISLogsCleaner(), nextStartDay, delay);
        stringBuilder.append(nextStartDay).append(" MailIISLogsCleaner() start\n");
    
        if (new File("exit.last").exists()) {
            exitLast = new TForms().fromArray(FileSystemWorker.readFileToList("exit.last"), false);
        }
    
        stringBuilder.append("\n").append(methMetr(stArt, classMeth));
        exitLast = exitLast + "\n" + MyCalen.checkDay(scheduledExecutorService) + "\n" + stringBuilder;
        miniLogger.add(exitLast);
        messageToUser.info(AppInfoOnLoad.class.getSimpleName() + ConstantsFor.STR_FINISH);
        boolean isWrite = FileSystemWorker.writeFile(CLASS_NAME + ".mini", miniLogger.stream());
        messageToUser.info(CLASS_NAME + " = " + isWrite);
    }
    
    private static int getScansDelay() {
        int parseInt = Integer.parseInt(APP_PROPS.getProperty(ConstantsFor.PR_SCANSINMIN, "111"));
        if (parseInt <= 0) {
            parseInt = 1;
        }
        if (parseInt < 80 | parseInt > 112) {
            parseInt = 85;
        }
        return parseInt;
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
        try {
            FileVisitor<Path> commonRightsChecker = new CommonRightsChecker();
            Files.walkFileTree(Paths.get("\\\\srv-fs.eatmeat.ru\\common_new"), commonRightsChecker);
        }
        catch (IOException e) {
            MessageToUser messageToUser = new MessageLocal(CommonSRV.class.getSimpleName());
            messageToUser.error(FileSystemWorker.error(CommonSRV.class.getSimpleName() + ".runCommonScan", e));
        }
    }
    
    /**
     Метрика метода
     <p>
     Считает время выполнения.
     
     @param stArt таймстэмп начала работы
     @param methName имя метода
     @return float {@link System#currentTimeMillis()} - таймстэмп из параметра, делённый на 1000.
     */
    private static String methMetr(long stArt, String methName) {
        String msgTimeSp = new StringBuilder()
            .append(methName)
            .append((float) (System.currentTimeMillis() - stArt) / 1000)
            .append(ConstantsFor.STR_SEC_SPEND)
            .toString();
        messageToUser.infoNoTitles(msgTimeSp);
        return msgTimeSp;
    }
    
    /**
     Немного инфомации о приложении.
     */
    private void infoForU() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(AppComponents.versionInfo()).append("\n");
        stringBuilder.append(ConstantsFor.getBuildStamp());
        messageToUser.info("AppInfoOnLoad.infoForU", ConstantsFor.STR_FINISH, " = " + stringBuilder);
        miniLogger.add("infoForU ends. now schedStarter(). Result: " + stringBuilder);
        VersionInfo versionInfo = AppComponents.versionInfo();
        messageToUser.info(getClass().getSimpleName() + ".run", versionInfo.toString(), " = " + iisLogSize());
        schedStarter();
    }
    
    /**
     Запуск заданий по-расписанию
     <p>
     Usages: {@link #infoForU()} <br>
     Uses: 1.1 {@link #dateSchedulers(ScheduledExecutorService)}, 1.2 {@link ConstantsFor#thisPC()}, 1.3 {@link ConstantsFor#thisPC()}.
     */
    private void schedStarter() {
        String osName = ConstantsFor.PR_OSNAME_LOWERCASE;
        messageToUser.warn(osName);
        ScheduledThreadPoolExecutor scheduledExecutorService = AppComponents.threadConfig().getTaskScheduler().getScheduledThreadPoolExecutor();
        String thisPC = ConstantsFor.thisPC();
        AppInfoOnLoad.miniLogger.add(thisPC);
        
        System.out.println("new AppComponents().launchRegRuFTPLibsUploader() = " + new AppComponents().launchRegRuFTPLibsUploader());
        
        if (!thisPC.toLowerCase().contains("home")) {
            scheduledExecutorService.scheduleWithFixedDelay(AppInfoOnLoad::runCommonScan, ConstantsFor.INIT_DELAY, TimeUnit.DAYS.toSeconds(1),
                TimeUnit.SECONDS);
            AppInfoOnLoad.miniLogger.add("runCommonScan init delay " + ConstantsFor.INIT_DELAY + ", delay " + TimeUnit.DAYS.toSeconds(1) + ". SECONDS");
        }
        if (osName.contains(ConstantsFor.PR_WINDOWSOS)) {
            schedWithService(scheduledExecutorService);
        }
        else {
            startUnixWithBeans();
        }
    }
    
    private void startUnixWithBeans() {
        Thread unixThread = new UnixThread(this);
        unixThread.setName("unix");
        unixThread.start();
    }
    
    private void schedWithService(ScheduledExecutorService scheduledExecutorService) {
        scheduledExecutorService.scheduleWithFixedDelay(new NetMonitorPTV(), 0, 10, TimeUnit.SECONDS);
        scheduledExecutorService.scheduleWithFixedDelay(temporaryFullInternet, 1, ConstantsFor.DELAY, TimeUnit.MINUTES);
        scheduledExecutorService.scheduleWithFixedDelay(DiapazonScan.getInstance(), 2, AppInfoOnLoad.thisDelay, TimeUnit.MINUTES);
        scheduledExecutorService.scheduleWithFixedDelay(new ScanOnline(), 3, 1, TimeUnit.MINUTES);
        scheduledExecutorService.scheduleWithFixedDelay(()->{
            new AppComponents().saveLogsToDB();
            InternetUse internetUse = new InetUserPCName();
            System.out.println("internetUse.cleanTrash() = " + internetUse.cleanTrash());
        }, 4, ConstantsFor.DELAY, TimeUnit.MINUTES);
        scheduledExecutorService.scheduleWithFixedDelay(new SquidChecker(), 5, ConstantsFor.DELAY * 2, TimeUnit.MINUTES);
        
        String msg = new StringBuilder()
            .append(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(AppInfoOnLoad.thisDelay)))
            .append(DiapazonScan.getInstance().getClass().getSimpleName())
            .append(" is starts next time.\n")
            .toString();
        AppInfoOnLoad.miniLogger.add(msg + ". Trying start dateSchedulers***| Local time: " + LocalTime.now());
        AppInfoOnLoad.miniLogger.add(NetMonitorPTV.class.getSimpleName() + " init delay 0, delay 10. SECONDS");
        final String minutesStr = ". MINUTES";
        AppInfoOnLoad.miniLogger.add(TemporaryFullInternet.class.getSimpleName() + " init delay 1, delay " + ConstantsFor.DELAY + minutesStr);
        AppInfoOnLoad.miniLogger.add(DiapazonScan.getInstance().getClass().getSimpleName() + " init delay 2, delay " + AppInfoOnLoad.thisDelay + minutesStr);
        AppInfoOnLoad.miniLogger.add(ScanOnline.class.getSimpleName() + " init delay 3, delay 1. MINUTES");
        AppInfoOnLoad.miniLogger.add(AppComponents.class.getSimpleName() + ".getProps(true) 4, ConstantsFor.DELAY, TimeUnit.MINUTES");
        messageToUser.info(AppInfoOnLoad.class.getSimpleName() + ".schedStarter()" + ConstantsFor.STR_FINISH);
        AppInfoOnLoad.dateSchedulers(scheduledExecutorService);
    }
    
}
