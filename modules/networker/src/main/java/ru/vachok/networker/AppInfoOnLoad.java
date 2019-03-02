package ru.vachok.networker;


import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageFile;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.messenger.email.ESender;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.accesscontrol.TemporaryFullInternet;
import ru.vachok.networker.accesscontrol.common.CommonRightsChecker;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.config.AppCtx;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.mailserver.MailIISLogsCleaner;
import ru.vachok.networker.net.*;
import ru.vachok.networker.services.MessageLocal;
import ru.vachok.networker.services.MyCalen;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
     Задержка выполнения для этого класса

     @see #schedStarter()
     */
    private static final int THIS_DELAY = 111;

    /**
     {@link AppComponents#getOrSetProps()}
     */
    private static final Properties APP_PROPS = AppComponents.getOrSetProps();

    /**
     " uptime."
     */
    private static final String STR_UPTIME = " uptime.";

    /**
     {@link MessageCons}
     */
    private static final MessageToUser messageToUser = new MessageLocal();

    /**
     Для записи результата работы класса.
     */
    private static final List<String> miniLogger = new ArrayList<>();

    /**
     Получение размера логов IIS-Exchange.
     <p>
     Путь до папки из {@link #APP_PROPS} iispath. <br> {@code Path iisLogsDir} = {@link Objects#requireNonNull(java.lang.Object)} -
     {@link Path#toFile()}.{@link File#listFiles()}. <br> Для каждого
     файла из папки, {@link File#length()}. Складываем {@code totalSize}. <br> {@code totalSize/}{@link ConstantsFor#MBYTE}.

     @return размер папки логов IIS в мегабайтах
     */
    @SuppressWarnings ("StaticMethodOnlyUsedInOneClass")
    public static String iisLogSize() {
        Path iisLogsDir = Paths.get(APP_PROPS.getProperty("iispath", "\\\\srv-mail3.eatmeat.ru\\c$\\inetpub\\logs\\LogFiles\\W3SVC1\\"));
        long totalSize = 0L;
        for (File x : Objects.requireNonNull(iisLogsDir.toFile().listFiles())) {
            totalSize = totalSize + x.length();
        }
        String s = totalSize / ConstantsFor.MBYTE + " MB IIS Logs\n";
        miniLogger.add(s);
        return s;
    }

    /**
     Очистка pcuserauto
     */
    private static void trunkTableUsers() {
        MessageToUser eSender = new ESender(ConstantsFor.EADDR_143500GMAILCOM);
        try (Connection c = new RegRuMysql().getDefaultConnection(ConstantsFor.DBDASENAME_U0466446_VELKOM);
             PreparedStatement preparedStatement = c.prepareStatement("TRUNCATE TABLE pcuserauto")) {
            preparedStatement.executeUpdate();
            eSender.infoNoTitles("TRUNCATE true\n" + ConstantsFor.getUpTime() + STR_UPTIME);
        } catch (SQLException e) {
            eSender.infoNoTitles("TRUNCATE false\n" + ConstantsFor.getUpTime() + STR_UPTIME);
        }
    }

    /**
     Reconnect Socket, пока он открыт
     <p>
     1. {@link MyServer#setSocket(java.net.Socket)}. Создаём новый {@link Socket}. <br>
     2. {@link MyServer#getSocket()} - пока он не {@code isClosed}, 3. {@link MyServer#reconSock()} реконнект. <br><br>
     {@link IOException}, {@link InterruptedException}, {@link NullPointerException} : <br>
     4. {@link TForms#fromArray(Exception, boolean)} - преобразуем исключение в строку. <br>
     5. {@link AppComponents#threadConfig()} , 6 {@link ThreadConfig#getTaskExecutor()} перезапуск {@link MyServer#getI()}
     */
    private static void starterTelnet() {
        MyServer.setSocket(new Socket());
        //noinspection resource
        while (!MyServer.getSocket().isClosed()) {
            try {
                MyServer.reconSock();
            } catch (IOException | InterruptedException | NullPointerException e1) {
                messageToUser.info("AppInfoOnLoad.starterTelnet", "e1.getMessage()", e1.getMessage());
                FileSystemWorker.error("SystemTrayHelper.starterTelnet", e1);
                Thread.currentThread().interrupt();
            }
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
        } catch (IOException e) {
            messageToUser.errorAlert("AppInfoOnLoad", "commonRightsMeth", e.getMessage());
            FileSystemWorker.error("AppInfoOnLoad.commonRightsMeth", e);
        }
        commonRightsMetrics(stMeth);
    }

    /**
     Метрика метода
     <p>
     Считает время выполнения.

     @param stArt    таймстэмп начала работы
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

        threadPoolTaskScheduler.scheduleWithFixedDelay(new WeekPCStats(), nextStartDay, delay);
        stringBuilder.append(nextStartDay.toString()).append(" WeekPCStats() start\n");
        nextStartDay = new Date(nextStartDay.getTime() - TimeUnit.HOURS.toMillis(1));

        threadPoolTaskScheduler.scheduleWithFixedDelay(new MailIISLogsCleaner(), nextStartDay, delay);
        stringBuilder.append(nextStartDay.toString()).append(" MailIISLogsCleaner() start\n");

        if (new File("exit.last").exists()) {
            exitLast = new TForms().fromArray(FileSystemWorker.readFileToList("exit.last"), false);
        }

        stringBuilder.append("\n").append(methMetr(stArt, classMeth));
        exitLast = exitLast + "\n" + checkDay(scheduledExecutorService) + "\n" + stringBuilder.toString();
        miniLogger.add(exitLast);

        FileSystemWorker.recFile(CLASS_NAME + ".mini", miniLogger);
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
     {@link AppComponents#temporaryFullInternet()}

     @see TemporaryFullInternet
     */
    private TemporaryFullInternet temporaryFullInternet = new AppComponents().temporaryFullInternet();

    /**
     Немного инфомации о приложении.

     @param appCtx {@link ApplicationContext}
     */
    private void infoForU(ApplicationContext appCtx) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(appCtx.getApplicationName());
        stringBuilder.append(" app name");
        stringBuilder.append(appCtx.getDisplayName());
        stringBuilder.append(" app display name\n");
        stringBuilder.append(ConstantsFor.getBuildStamp());
        messageToUser.info("AppInfoOnLoad.infoForU", "stringBuilder", " = " + stringBuilder.toString());
        miniLogger.add("infoForU ends. now schedStarter(). Result: " + stringBuilder.toString());
        schedStarter();
    }

    /**
     Запуск заданий по-расписанию
     <p>
     Usages: {@link #infoForU(ApplicationContext)} <br>
     Uses: 1.1 {@link #dateSchedulers(ScheduledExecutorService)}, 1.2 {@link ConstantsFor#thisPC()}, 1.3 {@link ConstantsFor#thisPC()}.
     */
    private void schedStarter() {
        String classMeth = "AppInfoOnLoad.schedStarter";
        miniLogger.add("***" + classMeth);
        final long stArt = System.currentTimeMillis();
        ScheduledThreadPoolExecutor scheduledExecutorService = AppComponents.threadConfig().getTaskScheduler().getScheduledThreadPoolExecutor();
        String thisPC = ConstantsFor.thisPC();
        miniLogger.add(thisPC);

        if (!thisPC.toLowerCase().contains("home")) {
            scheduledExecutorService.scheduleWithFixedDelay(AppInfoOnLoad::runCommonScan, ConstantsFor.INIT_DELAY, TimeUnit.DAYS.toSeconds(1),
                TimeUnit.SECONDS);
            miniLogger.add("runCommonScan init delay " + ConstantsFor.INIT_DELAY + ", delay " + TimeUnit.DAYS.toSeconds(1) + ". SECONDS");
        }
        scheduledExecutorService.scheduleWithFixedDelay(ScanOnline.getI(), 3, 1, TimeUnit.MINUTES);
        scheduledExecutorService.scheduleWithFixedDelay(DiapazonedScan.getInstance(), 2, THIS_DELAY, TimeUnit.MINUTES);
        scheduledExecutorService.scheduleWithFixedDelay(new NetMonitorPTV(), 0, 10, TimeUnit.SECONDS);
        long testDelay = getTestDelay();
        scheduledExecutorService.scheduleWithFixedDelay(temporaryFullInternet, 1, testDelay, TimeUnit.MINUTES);
        String msg = new StringBuilder()
            .append(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(THIS_DELAY)))
            .append(DiapazonedScan.getInstance().getClass().getSimpleName())
            .append(" is starts next time.\n")
            .append(methMetr(stArt, classMeth))
            .toString();
        miniLogger.add(msg + ". Trying start dateSchedulers***| Local time: " + LocalTime.now().toString());
        miniLogger.add(NetMonitorPTV.class.getSimpleName() + " init delay 0, delay 10. SECONDS");
        miniLogger.add(TemporaryFullInternet.class.getSimpleName() + " init delay 1, delay " + testDelay + ". MINUTES");
        miniLogger.add(DiapazonedScan.getInstance().getClass().getSimpleName() + " init delay 2, delay " + THIS_DELAY + ". MINUTES");
        miniLogger.add(ScanOnline.getI().getClass().getSimpleName() + " init delay 3, delay 1. MINUTES");
        dateSchedulers(scheduledExecutorService);
    }

    private long getTestDelay() {
        if(ConstantsFor.thisPC().toLowerCase().contains("home")){
            return 1;
        }
        else{
            return ConstantsFor.DELAY;
        }
    }

    /**
     Старт
     <p>
     {@link #infoForU(ApplicationContext)}
     */
    @Override
    public void run() {
        infoForU(AppCtx.scanForBeansAndRefreshContext());
        AppComponents.threadConfig().executeAsThread(AppInfoOnLoad::starterTelnet);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AppInfoOnLoad{");
        sb.append("miniLogger=").append(new TForms().fromArray(miniLogger, false));
        sb.append('}');
        FileSystemWorker.recFile(getClass().getSimpleName() + ".mini", miniLogger.stream());
        return sb.toString();
    }
}
