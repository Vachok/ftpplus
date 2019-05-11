// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageFile;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.abstr.ConnectToMe;
import ru.vachok.networker.abstr.InternetUse;
import ru.vachok.networker.accesscontrol.TemporaryFullInternet;
import ru.vachok.networker.accesscontrol.common.CommonRightsChecker;
import ru.vachok.networker.accesscontrol.inetstats.InetUserPCName;
import ru.vachok.networker.config.AppCtx;
import ru.vachok.networker.config.DeadLockMonitor;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.mailserver.MailIISLogsCleaner;
import ru.vachok.networker.net.*;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.services.MessageLocal;
import ru.vachok.networker.services.MyCalen;
import ru.vachok.networker.services.WeekPCStats;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.net.Socket;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.*;


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
     " uptime."
     */
    private static final String STR_UPTIME = " uptime.";
    
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
    
    private static int thisDelay;
    
    private static String unixThreadInfo = System.getProperty("os.name");
    
    public static String getUnixThreadInfo() {
        return unixThreadInfo;
    }
    
    public static void setUnixThreadInfo(String unixThreadInfo) {
        AppInfoOnLoad.unixThreadInfo = unixThreadInfo;
    }
    
    
    static {
        int scansDelay = Integer.parseInt(APP_PROPS.getProperty(ConstantsFor.PR_SCANSINMIN, "111"));
        if (scansDelay <= 0) {
            scansDelay = 1;
        }
        thisDelay = ConstantsNet.IPS_IN_VELKOM_VLAN / scansDelay;
        if (thisDelay < 80 | thisDelay > 112) {
            thisDelay = 85;
        }
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
    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
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
     {@link #infoForU(ApplicationContext)}
     */
    @Override
    public void run() {
        infoForU(AppCtx.scanForBeansAndRefreshContext());
        messageToUser.info(getClass().getSimpleName() + ".run", "thisDelay", " = " + thisDelay);
        ConstantsFor.INFO_MSG_RUNNABLE.run();
        AppComponents.threadConfig().execByThreadConfig(AppInfoOnLoad::starterTelnet);
    }
    
    @Override public String toString() {
        final StringBuilder sb = new StringBuilder("AppInfoOnLoad{");
        sb.append(", thisDelay=").append(thisDelay);
        sb.append("<br>").append(new TForms().fromArray(miniLogger, true));
        sb.append('}');
        return sb.toString();
    }
    
    /**
     Очистка pcuserauto
     */
    private static void trunkTableUsers() {
        try (Connection c = new RegRuMysql().getDefaultConnection(ConstantsFor.DBBASENAME_U0466446_VELKOM);
             PreparedStatement preparedStatement = c.prepareStatement("TRUNCATE TABLE pcuserauto")
        ) {
            preparedStatement.executeUpdate();
            miniLogger.add("TRUNCATE true\n" + ConstantsFor.getUpTime() + STR_UPTIME);
        }
        catch (SQLException e) {
            miniLogger.add("TRUNCATE false\n" + ConstantsFor.getUpTime() + STR_UPTIME);
        }
    }
    
    /**
     Сборщик прав \\srv-fs.eatmeat.ru\common_new
     <p>
     {@link Files#walkFileTree(java.nio.file.Path, java.nio.file.FileVisitor)}, где {@link Path} = \\srv-fs.eatmeat.ru\common_new и {@link FileVisitor}
     = new {@link CommonRightsChecker}.
     <p>
     <b>{@link IOException}:</b><br>
     {@link MessageToUser#errorAlert(java.lang.String, java.lang.String, java.lang.String)},
     {@link FileSystemWorker#error(java.lang.String, java.lang.Exception)}
     */
    private static void runCommonScan() {
        final long stMeth = System.currentTimeMillis();
        try {
            FileVisitor<Path> commonRightsChecker = new CommonRightsChecker();
            Files.walkFileTree(Paths.get("\\\\srv-fs.eatmeat.ru\\common_new"), commonRightsChecker);
        }
        catch (IOException e) {
            messageToUser.errorAlert("AppInfoOnLoad", "commonRightsMeth", e.getMessage());
            FileSystemWorker.error("AppInfoOnLoad.commonRightsMeth", e);
        }
        commonRightsMetrics(stMeth);
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
     Reconnect Socket, пока он открыт
     <p>
     1. {@link MyConsoleServer#setSocket(java.net.Socket)}. Создаём новый {@link Socket}. <br>
     2. {@link MyConsoleServer#getSocket()} - пока он не {@code isClosed}, 3. {@link MyConsoleServer#reconSock()} реконнект. <br><br>
     {@link IOException}, {@link InterruptedException}, {@link NullPointerException} : <br>
     4. {@link TForms#fromArray(Exception, boolean)} - преобразуем исключение в строку. <br>
     5. {@link AppComponents#threadConfig()} , 6 {@link ThreadConfig#getTaskExecutor()} перезапуск {@link MyConsoleServer#getI()}
     */
    @SuppressWarnings("resource")
    private static void starterTelnet() {
        ConnectToMe myConsoleServer = MyConsoleServer.getI();
        if (ConstantsFor.PR_OSNAME_LOWERCASE.contains("bsd") || APP_PROPS.getProperty(ConstantsFor.PR_TESTSERVER).contains("true")) {
            AppComponents.threadConfig().execByThreadConfig(AppInfoOnLoad::testServerStart);
        }
        else {
            ((MyConsoleServer) myConsoleServer).setSocket(new Socket());
            while (!((MyConsoleServer) myConsoleServer).getSocket().isClosed()) {
                try {
                    myConsoleServer.reconSock();
                }
                catch (IOException | InterruptedException | NullPointerException e1) {
                    messageToUser.info("AppInfoOnLoad.starterTelnet", "e1.getMessage()", e1.getMessage());
                    FileSystemWorker.error("SystemTrayHelper.starterTelnet", e1);
                    Thread.currentThread().interrupt();
                }
            }
            System.setOut(System.err);
        }
    }
    
    private static void testServerStart() {
        AppComponents.threadConfig().thrNameSet("11111");
        try {
            ConnectToMe connectToMe = new TestServer();
            connectToMe.runSocket();
        }
        catch (Exception e) {
            messageToUser.error(FileSystemWorker.error(AppInfoOnLoad.class.getSimpleName() + ".testServerStart", e));
        }
    }
    
    /**
     Проверяет день недели.
     
     @param scheduledExecutorService {@link ScheduledExecutorService}
     @return {@code msg = dateFormat.format(dateStart) + " pcuserauto (" + TimeUnit.MILLISECONDS.toHours(delayMs) + " delay hours)}
     */
    private static String checkDay(ScheduledExecutorService scheduledExecutorService) {
        messageToUser.info(ConstantsFor.STR_INPUT_OUTPUT, "", ConstantsFor.JAVA_LANG_STRING_NAME);
        Date dateStart = MyCalen.getNextDayofWeek(10, 0, DayOfWeek.MONDAY);
        DateFormat dateFormat = new SimpleDateFormat("MM.dd, hh:mm", Locale.getDefault());
        long delayMs = dateStart.getTime() - System.currentTimeMillis();
        String msg = dateFormat.format(dateStart) + " pcuserauto (" + TimeUnit.MILLISECONDS.toHours(delayMs) + " delay hours)";
        scheduledExecutorService.scheduleWithFixedDelay(AppInfoOnLoad::trunkTableUsers, delayMs, ConstantsFor.ONE_WEEK_MILLIS, TimeUnit.MILLISECONDS);
        messageToUser.infoNoTitles("msg = " + msg);
        return msg;
    }
    
    /**
     Запускает сканнер прав Common
     
     @param startMeth время старта
     */
    private static void commonRightsMetrics(long startMeth) {
        long mSecRun = System.currentTimeMillis() - new Date(startMeth).getTime();
        String metricOfCommonScan = new StringBuilder()
            .append(TimeUnit.MILLISECONDS.toMinutes(mSecRun))
            .append(" minutes to run ")
            .append(CommonRightsChecker.class.getSimpleName())
            .toString();
    
        new MessageFile().info("AppInfoOnLoad.runCommonScanMetrics", "metricOfCommonScan", " = " + metricOfCommonScan);
    }
    
    /**
     Стата за неделю по-ПК
     <p>
     1. {@link MyCalen#getNextDayofWeek(int, int, java.time.DayOfWeek)}. Получим {@link Date}, след. воскресенье 23:57.<br>
     {@link ThreadPoolTaskScheduler}, запланируем new {@link WeekPCStats} и new {@link MailIISLogsCleaner} на это время и на это время -1 час.<br><br>
     2. {@link FileSystemWorker#readFileToList(java.lang.String)}. Прочитаем exit.last, если он существует.
     {@link TForms#fromArray(java.util.List, boolean)} <br><br>
     3. {@link #checkDay(ScheduledExecutorService)} метрика. <br>
     4. {@link #checkDay(java.util.concurrent.ScheduledExecutorService)}. Выведем сообщение, когда и что ствртует.
     <p>
     
     @param scheduledExecutorService {@link ScheduledExecutorService}.
     */
    @SuppressWarnings("MagicNumber")
    private static void dateSchedulers(ScheduledExecutorService scheduledExecutorService) {
        long stArt = System.currentTimeMillis();
        long delay = TimeUnit.HOURS.toMillis(ConstantsFor.ONE_DAY_HOURS * 7);
    
        String classMeth = "AppInfoOnLoad.dateSchedulers";
        String exitLast = "No file";
        AppComponents.threadConfig().thrNameSet("dateSch");
        Date nextStartDay = MyCalen.getNextDayofWeek(23, 57, DayOfWeek.SUNDAY);
        StringBuilder stringBuilder = new StringBuilder();
        ThreadPoolTaskScheduler threadPoolTaskScheduler = AppComponents.threadConfig().getTaskScheduler();
    
        threadPoolTaskScheduler.scheduleWithFixedDelay(new WeekPCStats(ConstantsFor.SQL_SELECTFROM_PCUSERAUTO), nextStartDay, delay);
        stringBuilder.append(nextStartDay).append(" WeekPCStats() start\n");
        nextStartDay = new Date(nextStartDay.getTime() - TimeUnit.HOURS.toMillis(1));
    
        threadPoolTaskScheduler.scheduleWithFixedDelay(new MailIISLogsCleaner(), nextStartDay, delay);
        stringBuilder.append(nextStartDay).append(" MailIISLogsCleaner() start\n");
    
        if (new File("exit.last").exists()) {
            exitLast = new TForms().fromArray(FileSystemWorker.readFileToList("exit.last"), false);
        }
    
        stringBuilder.append("\n").append(methMetr(stArt, classMeth));
        exitLast = exitLast + "\n" + checkDay(scheduledExecutorService) + "\n" + stringBuilder;
        miniLogger.add(exitLast);
        messageToUser.info(AppInfoOnLoad.class.getSimpleName() + ConstantsFor.STR_FINISH);
        boolean isWrite = FileSystemWorker.writeFile(CLASS_NAME + ".mini", miniLogger.stream());
        messageToUser.info(CLASS_NAME + " = " + isWrite);
    }
    
    /**
     Немного инфомации о приложении.
     
     @param appCtx {@link ApplicationContext}
     */
    @SuppressWarnings("DuplicateStringLiteralInspection")
    private void infoForU(ApplicationContext appCtx) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(AppComponents.versionInfo().toString()).append("\n");
        stringBuilder.append(ConstantsFor.getBuildStamp());
        messageToUser.info("AppInfoOnLoad.infoForU", ConstantsFor.STR_FINISH, " = " + stringBuilder);
        miniLogger.add("infoForU ends. now schedStarter(). Result: " + stringBuilder);
        AppComponents.threadConfig().execByThreadConfig(new DeadLockMonitor());
        schedStarter();
    }
    
    /**
     Запуск заданий по-расписанию
     <p>
     Usages: {@link #infoForU(ApplicationContext)} <br>
     Uses: 1.1 {@link #dateSchedulers(ScheduledExecutorService)}, 1.2 {@link ConstantsFor#thisPC()}, 1.3 {@link ConstantsFor#thisPC()}.
     */
    private void schedStarter() {
        String osName = System.getProperty("os.name");
        messageToUser.info(osName);
        final long stArt = System.currentTimeMillis();
        ScheduledThreadPoolExecutor scheduledExecutorService = AppComponents.threadConfig().getTaskScheduler().getScheduledThreadPoolExecutor();
        
        String thisPC = ConstantsFor.thisPC();
        AppInfoOnLoad.miniLogger.add(thisPC);
    
        if (!thisPC.toLowerCase().contains("home")) {
            scheduledExecutorService.scheduleWithFixedDelay(AppInfoOnLoad::runCommonScan, ConstantsFor.INIT_DELAY, TimeUnit.DAYS.toSeconds(1),
                TimeUnit.SECONDS);
            AppInfoOnLoad.miniLogger.add("runCommonScan init delay " + ConstantsFor.INIT_DELAY + ", delay " + TimeUnit.DAYS.toSeconds(1) + ". SECONDS");
        }
        else if (osName.toLowerCase().contains(ConstantsFor.PR_WINDOWSOS)) {
            schedWithService(scheduledExecutorService);
        }
        else if (osName.toLowerCase().contains("bsd")) {
            OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
            messageToUser.warn(operatingSystemMXBean.getName(), operatingSystemMXBean.getVersion() + " proc = " + operatingSystemMXBean
                .getAvailableProcessors(), thisPC + " (av load: " + operatingSystemMXBean.getSystemLoadAverage() + ")");
            Thread unixThread = new Thread() {
                public void run() {
                    try {
                        messageToUser.warn(unixTrySched());
                    }
                    catch (Exception e) {
                        messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".schedStarter", e));
                    }
                }
            };
            unixThread.setName("unix");
            unixThread.start();
            setUnixThreadInfo(ManagementFactory.getThreadMXBean().getThreadInfo(unixThread.getId()).toString());
        }
        else {
            OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
            osName = operatingSystemMXBean.getName();
            messageToUser.error(osName);
        }
    }
    
    private void schedWithService(ScheduledExecutorService scheduledExecutorService) {
        scheduledExecutorService.scheduleWithFixedDelay(new NetMonitorPTV(), 0, 10, TimeUnit.SECONDS);
        scheduledExecutorService.scheduleWithFixedDelay(temporaryFullInternet, 1, ConstantsFor.DELAY, TimeUnit.MINUTES);
        scheduledExecutorService.scheduleWithFixedDelay(DiapazonedScan.getInstance(), 2, AppInfoOnLoad.thisDelay, TimeUnit.MINUTES);
        scheduledExecutorService.scheduleWithFixedDelay(new ScanOnline(), 3, 1, TimeUnit.MINUTES);
        scheduledExecutorService.scheduleWithFixedDelay(()->{
            new AppComponents().saveLogsToDB().startScheduled();
            InternetUse internetUse = new InetUserPCName();
            int cleanInetstatDB = internetUse.cleanTrash();
        }, 4, ConstantsFor.DELAY, TimeUnit.MINUTES);
        String msg = new StringBuilder()
            .append(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(AppInfoOnLoad.thisDelay)))
            .append(DiapazonedScan.getInstance().getClass().getSimpleName())
            .append(" is starts next time.\n")
            .toString();
        AppInfoOnLoad.miniLogger.add(msg + ". Trying start dateSchedulers***| Local time: " + LocalTime.now());
        AppInfoOnLoad.miniLogger.add(NetMonitorPTV.class.getSimpleName() + " init delay 0, delay 10. SECONDS");
        final String minutesStr = ". MINUTES";
        AppInfoOnLoad.miniLogger.add(TemporaryFullInternet.class.getSimpleName() + " init delay 1, delay " + ConstantsFor.DELAY + minutesStr);
        AppInfoOnLoad.miniLogger.add(DiapazonedScan.getInstance().getClass().getSimpleName() + " init delay 2, delay " + AppInfoOnLoad.thisDelay + minutesStr);
        AppInfoOnLoad.miniLogger.add(ScanOnline.class.getSimpleName() + " init delay 3, delay 1. MINUTES");
        AppInfoOnLoad.miniLogger.add(AppComponents.class.getSimpleName() + ".getProps(true) 4, ConstantsFor.DELAY, TimeUnit.MINUTES");
        messageToUser.info(AppInfoOnLoad.class.getSimpleName() + ".schedStarter()" + ConstantsFor.STR_FINISH);
        AppInfoOnLoad.dateSchedulers(scheduledExecutorService);
    }
    
    private String unixTrySched() throws RuntimeException {
        StringBuilder stringBuilder = new StringBuilder();
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        threadMXBean.setThreadContentionMonitoringEnabled(true);
        threadMXBean.setThreadCpuTimeEnabled(true);
    
        ScheduledExecutorService executorService = Executors.unconfigurableScheduledExecutorService(Executors.newScheduledThreadPool(ConstantsFor.ONE_DAY_HOURS));
        stringBuilder.append(executorService.toString());
        
        ScheduledFuture<?> ptvPing = executorService.scheduleWithFixedDelay(new NetMonitorPTV(), 0, ConstantsFor.ONE_DAY_HOURS, TimeUnit.SECONDS);
        ScheduledFuture<?> tmpInet = executorService.scheduleWithFixedDelay(new TemporaryFullInternet(), 0, ConstantsFor.ONE_DAY_HOURS, TimeUnit.SECONDS);
        ScheduledFuture<?> diapScan = executorService.scheduleWithFixedDelay(DiapazonedScan.getInstance(), 2, ConstantsFor.DELAY, TimeUnit.MINUTES);
        ScheduledFuture<?> scanOnline = executorService.scheduleWithFixedDelay(new ScanOnline(), 3, 3, TimeUnit.MINUTES);
        try {
            if (ptvPing.get() != null) {
                stringBuilder.append("ptvPing");
            }
            if (tmpInet.get() != null) {
                stringBuilder.append("tmpInet");
            }
            if (diapScan.get() != null) {
                stringBuilder.append("diapScan");
            }
            if (scanOnline.get() != null) {
                stringBuilder.append("scanOnline");
            }
        }
        catch (InterruptedException | ExecutionException e) {
            stringBuilder.append(FileSystemWorker.error(getClass().getSimpleName() + ".unixTrySched", e));
        }
        for (long id : threadMXBean.getAllThreadIds()) {
            FileSystemWorker.writeFile("scheduler.stack", Arrays.toString(threadMXBean.getThreadInfo(id).getStackTrace()));
            stringBuilder.append(threadMXBean.getThreadInfo(Thread.currentThread().getId()).toString());
        }
        setUnixThreadInfo(stringBuilder.toString());
        dateSchedulers(executorService);
        return stringBuilder.toString();
    }
}
