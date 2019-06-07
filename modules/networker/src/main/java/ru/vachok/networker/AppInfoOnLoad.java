// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.abstr.InternetUse;
import ru.vachok.networker.accesscontrol.common.CommonRightsChecker;
import ru.vachok.networker.accesscontrol.inetstats.InetUserPCName;
import ru.vachok.networker.componentsrepo.VersionInfo;
import ru.vachok.networker.exe.runnabletasks.NetMonitorPTV;
import ru.vachok.networker.exe.runnabletasks.ScanOnline;
import ru.vachok.networker.exe.runnabletasks.SpeedChecker;
import ru.vachok.networker.exe.runnabletasks.TemporaryFullInternet;
import ru.vachok.networker.exe.schedule.DiapazonScan;
import ru.vachok.networker.exe.schedule.MailIISLogsCleaner;
import ru.vachok.networker.exe.schedule.SquidAvaliblityChecker;
import ru.vachok.networker.exe.schedule.WeekStats;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.services.MessageLocal;
import ru.vachok.networker.services.MyCalen;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.Executors;
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
    private static final MessageToUser MESSAGE_LOCAL = new MessageLocal(AppInfoOnLoad.class.getSimpleName());
    
    /**
     Для записи результата работы класса.
     */
    private static final List<String> MINI_LOGGER = new ArrayList<>();
    
    private static int thisDelay = ConstantsNet.IPS_IN_VELKOM_VLAN / getScansDelay();
    
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
        MINI_LOGGER.add(s);
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
            getWeekPCStats();
        }
        catch (IOException e) {
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
     
     @param scheduledExecutorService {@link ScheduledExecutorService}.
     */
    @SuppressWarnings("MagicNumber")
    static void dateSchedulers(ScheduledExecutorService scheduledExecutorService) {
        long delay = TimeUnit.HOURS.toMillis(ConstantsFor.ONE_DAY_HOURS * 7);
    
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
    
        exitLast = exitLast + "\n" + MyCalen.checkDay(scheduledExecutorService) + "\n" + stringBuilder;
        MINI_LOGGER.add(exitLast);
        MESSAGE_LOCAL.info(AppInfoOnLoad.class.getSimpleName() + ConstantsFor.STR_FINISH);
        boolean isWrite = FileSystemWorker.writeFile(CLASS_NAME + ".mini", MINI_LOGGER.stream());
        MESSAGE_LOCAL.info(CLASS_NAME + " = " + isWrite);
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
            MESSAGE_LOCAL.error(e.getMessage());
        }
    }
    
    /**
     Немного инфомации о приложении.
     */
    private void infoForU() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(AppComponents.versionInfo()).append("\n");
        stringBuilder.append(ConstantsFor.getBuildStamp());
        MESSAGE_LOCAL.info("AppInfoOnLoad.infoForU", ConstantsFor.STR_FINISH, " = " + stringBuilder);
        MINI_LOGGER.add("infoForU ends. now schedStarter(). Result: " + stringBuilder);
        VersionInfo versionInfo = AppComponents.versionInfo();
        MESSAGE_LOCAL.info(getClass().getSimpleName() + ".run", versionInfo.toString(), " = " + iisLogSize());
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
        MESSAGE_LOCAL.warn(osName);
        ScheduledThreadPoolExecutor scheduledExecutorService = AppComponents.threadConfig().getTaskScheduler().getScheduledThreadPoolExecutor();
        String thisPC = ConstantsFor.thisPC();
        AppInfoOnLoad.MINI_LOGGER.add(thisPC);
        
        System.out.println("new AppComponents().launchRegRuFTPLibsUploader() = " + new AppComponents().launchRegRuFTPLibsUploader());
        
        if (!thisPC.toLowerCase().contains("home")) {
            scheduledExecutorService.scheduleWithFixedDelay(AppInfoOnLoad::runCommonScan, ConstantsFor.INIT_DELAY, TimeUnit.DAYS.toSeconds(1),
                TimeUnit.SECONDS);
            AppInfoOnLoad.MINI_LOGGER.add("runCommonScan init delay " + ConstantsFor.INIT_DELAY + ", delay " + TimeUnit.DAYS.toSeconds(1) + ". SECONDS");
        }
    
        schedWithService(scheduledExecutorService);
    }
    
    private void schedWithService(ScheduledExecutorService scheduledExecutorService) {
        final String minutesStr = ". MINUTES";
        
        scheduledExecutorService.scheduleWithFixedDelay(new NetMonitorPTV(), 0, 10, TimeUnit.SECONDS);
        scheduledExecutorService.scheduleWithFixedDelay(new AppComponents().temporaryFullInternet(), 1, ConstantsFor.DELAY, TimeUnit.MINUTES);
        scheduledExecutorService.scheduleWithFixedDelay(DiapazonScan.getInstance(), 2, AppInfoOnLoad.thisDelay, TimeUnit.MINUTES);
        scheduledExecutorService.scheduleWithFixedDelay(new ScanOnline(), 3, 1, TimeUnit.MINUTES);
        scheduledExecutorService.scheduleWithFixedDelay(AppInfoOnLoad::squidLogsSave, 4, ConstantsFor.DELAY, TimeUnit.MINUTES);
        scheduledExecutorService.scheduleWithFixedDelay(new SquidAvaliblityChecker(), 5, ConstantsFor.DELAY * 2, TimeUnit.MINUTES);
        scheduledExecutorService.schedule(()->MESSAGE_LOCAL.info(new TForms().fromArray(APP_PROPS, false)), ConstantsFor.DELAY + 10, TimeUnit.MINUTES);
        
        String msg = new StringBuilder()
            .append(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(AppInfoOnLoad.thisDelay)))
            .append(DiapazonScan.getInstance().getClass().getSimpleName())
            .append(" is starts next time.\n")
            .toString();
        AppInfoOnLoad.MINI_LOGGER.add(msg + ". Trying start dateSchedulers***| Local time: " + LocalTime.now());
        AppInfoOnLoad.MINI_LOGGER.add(NetMonitorPTV.class.getSimpleName() + " init delay 0, delay 10. SECONDS");
        AppInfoOnLoad.MINI_LOGGER.add(TemporaryFullInternet.class.getSimpleName() + " init delay 1, delay " + ConstantsFor.DELAY + minutesStr);
        AppInfoOnLoad.MINI_LOGGER.add(DiapazonScan.getInstance().getClass().getSimpleName() + " init delay 2, delay " + AppInfoOnLoad.thisDelay + minutesStr);
        AppInfoOnLoad.MINI_LOGGER.add(ScanOnline.class.getSimpleName() + " init delay 3, delay 1. MINUTES");
        AppInfoOnLoad.MINI_LOGGER.add(AppComponents.class.getSimpleName() + ".getProps(true) 4, ConstantsFor.DELAY, TimeUnit.MINUTES");
        
        AppInfoOnLoad.dateSchedulers(scheduledExecutorService);
    }
    
    /**
     Статистика по-пользователям за неделю.
     <p>
     Запуск new {@link SpeedChecker.ChkMailAndUpdateDB}, через {@link Executors#unconfigurableExecutorService(java.util.concurrent.ExecutorService)}
     <p>
     Если {@link LocalDate#getDayOfWeek()} equals {@link DayOfWeek#SUNDAY}, запуск new {@link WeekStats}
     */
    private static void getWeekPCStats() {
        if (LocalDate.now().getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
            AppComponents.threadConfig().execByThreadConfig(new WeekStats(ConstantsFor.SQL_SELECTFROM_PCUSERAUTO));
        }
    }
    
    private static void squidLogsSave() {
        new AppComponents().saveLogsToDB().run();
        InternetUse internetUse = new InetUserPCName();
        System.out.println("internetUse.cleanTrash() = " + internetUse.cleanTrash());
    }
}
