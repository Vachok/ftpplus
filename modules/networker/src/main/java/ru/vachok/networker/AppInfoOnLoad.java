// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.abstr.InternetUse;
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
import ru.vachok.networker.services.DBMessenger;
import ru.vachok.networker.services.MyCalen;

import java.io.File;
import java.io.IOException;
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
     {@link AppComponents#getProps()}
     */
    private final Properties appProps = AppComponents.getProps();
    
    /**
     Для записи результата работы класса.
     */
    private final List<String> miniLogger = new ArrayList<>();
    
    private int thisDelay = ConstantsNet.IPS_IN_VELKOM_VLAN / getScansDelay();
    
    /**
     {@link MessageCons}
     */
    private static final MessageToUser MESSAGE_LOCAL = new DBMessenger(AppInfoOnLoad.class.getSimpleName());
    
    public void setThisDelay(int thisDelay) {
        this.thisDelay = thisDelay;
    }
    
    /**
     Получение размера логов IIS-Exchange.
     <p>
     Путь до папки из {@link #appProps} iispath. <br> {@code Path iisLogsDir} = {@link Objects#requireNonNull(java.lang.Object)} -
     {@link Path#toFile()}.{@link File#listFiles()}. <br> Для каждого
     файла из папки, {@link File#length()}. Складываем {@code totalSize}. <br> {@code totalSize/}{@link ConstantsFor#MBYTE}.
 
     @return размер папки логов IIS в мегабайтах
     */
    public String getIISLogSize() {
        Path iisLogsDir = Paths.get(appProps.getProperty("iispath", "\\\\srv-mail3.eatmeat.ru\\c$\\inetpub\\logs\\LogFiles\\W3SVC1\\"));
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
            getWeekPCStats();
        }
        catch (IOException e) {
            MESSAGE_LOCAL.error(e.getMessage());
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
    @SuppressWarnings("MagicNumber") void dateSchedulers(ScheduledExecutorService scheduledExecutorService) {
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
        miniLogger.add(exitLast);
        MESSAGE_LOCAL.info(AppInfoOnLoad.class.getSimpleName() + ConstantsFor.STR_FINISH);
        boolean isWrite = FileSystemWorker.writeFile(getClass().getSimpleName() + ".mini", miniLogger.stream());
        MESSAGE_LOCAL.info(getClass().getSimpleName() + " = " + isWrite);
    }
    
    private int getScansDelay() {
        int parseInt = Integer.parseInt(appProps.getProperty(ConstantsFor.PR_SCANSINMIN, "111"));
        if (parseInt <= 0) {
            parseInt = 1;
        }
        float minDelay = ConstantsFor.ONE_HOUR_IN_MIN + ConstantsFor.ONE_DAY_HOURS;
        if (parseInt < minDelay | parseInt > ConstantsFor.ONE_HOUR_IN_MIN * 2) {
            parseInt = (int) minDelay;
        }
        return parseInt;
    }
    
    /**
     Немного инфомации о приложении.
     */
    private void infoForU() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(AppComponents.versionInfo()).append("\n");
        stringBuilder.append(ConstantsFor.getBuildStamp());
        MESSAGE_LOCAL.info("AppInfoOnLoad.infoForU", ConstantsFor.STR_FINISH, " = " + stringBuilder);
        miniLogger.add("infoForU ends. now schedStarter(). Result: " + stringBuilder);
        VersionInfo versionInfo = AppComponents.versionInfo();
        MESSAGE_LOCAL.info(getClass().getSimpleName() + ".run", versionInfo.toString(), " = " + getIISLogSize());
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
        miniLogger.add(thisPC);
    
        System.out.println("new AppComponents().launchRegRuFTPLibsUploader() = " + new AppComponents().launchRegRuFTPLibsUploader());
        
        schedWithService(scheduledExecutorService);
    }
    
    private void schedWithService(ScheduledExecutorService scheduledExecutorService) {
        final String minutesStr = ". MINUTES";
        
        scheduledExecutorService.scheduleWithFixedDelay(new NetMonitorPTV(), 0, 10, TimeUnit.SECONDS);
        scheduledExecutorService.scheduleWithFixedDelay(new AppComponents().temporaryFullInternet(), 1, ConstantsFor.DELAY, TimeUnit.MINUTES);
        scheduledExecutorService.scheduleWithFixedDelay(AppComponents.diapazonScan(), 2, thisDelay, TimeUnit.MINUTES); //уходим от прямого использования
        scheduledExecutorService.scheduleWithFixedDelay(new ScanOnline(), 3, 1, TimeUnit.MINUTES);
        scheduledExecutorService.scheduleWithFixedDelay(AppInfoOnLoad::squidLogsSave, 4, ConstantsFor.DELAY, TimeUnit.MINUTES);
        scheduledExecutorService.scheduleWithFixedDelay(new SquidAvaliblityChecker(), 5, ConstantsFor.DELAY * 2, TimeUnit.MINUTES);
        scheduledExecutorService.schedule(()->MESSAGE_LOCAL.info(new TForms().fromArray(appProps, false)), ConstantsFor.DELAY + 10, TimeUnit.MINUTES);
    
        String msg = new StringBuilder()
            .append(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(thisDelay)))
            .append(DiapazonScan.getInstance().getClass().getSimpleName())
            .append(" is starts next time.\n")
            .toString();
        miniLogger.add(msg + ". Trying start dateSchedulers***| Local time: " + LocalTime.now());
        miniLogger.add(NetMonitorPTV.class.getSimpleName() + " init delay 0, delay 10. SECONDS");
        miniLogger.add(TemporaryFullInternet.class.getSimpleName() + " init delay 1, delay " + ConstantsFor.DELAY + minutesStr);
        miniLogger.add(DiapazonScan.getInstance().getClass().getSimpleName() + " init delay 2, delay " + thisDelay + minutesStr);
        miniLogger.add(ScanOnline.class.getSimpleName() + " init delay 3, delay 1. MINUTES");
        miniLogger.add(AppComponents.class.getSimpleName() + ".getProps(true) 4, ConstantsFor.DELAY, TimeUnit.MINUTES");
    
        dateSchedulers(scheduledExecutorService);
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
