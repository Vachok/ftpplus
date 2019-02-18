package ru.vachok.networker;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageSwing;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.messenger.email.ESender;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.accesscontrol.common.CommonRightsChecker;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.config.AppCtx;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.errorexceptions.MyNull;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.mailserver.MailIISLogsCleaner;
import ru.vachok.networker.net.DiapazonedScan;
import ru.vachok.networker.net.NetMonitorPTV;
import ru.vachok.networker.net.ScanOnline;
import ru.vachok.networker.net.WeekPCStats;
import ru.vachok.networker.services.MyCalen;
import ru.vachok.networker.services.SpeedChecker;

import java.io.File;
import java.io.IOException;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
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
     {@link LoggerFactory#getLogger(java.lang.String)}
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CLASS_NAME);

    /**
     Задержка выполнения для этого класса

     @see #schedStarter()
     */
    private static final int THIS_DELAY = 111;

    /**
     {@link ConstantsFor#getProps()}
     */
    private static final Properties APP_PROPS = ConstantsFor.getProps();

    /**
     " uptime."
     */
    private static final String STR_UPTIME = " uptime.";

    /**
     {@link DiapazonedScan#getInstance()}
     */
    private static DiapazonedScan diapazonedScan = DiapazonedScan.getInstance();

    /**
     {@link MessageCons}
     */
    private static MessageToUser messageToUser = new MessageCons();

    /**
     Запуск {@link CommonRightsChecker}
     <p>
     {@link AppInfoOnLoad#commonRightsMeth()}
     */
    private static final Runnable commonRights = AppInfoOnLoad::commonRightsMeth;

    /**
     Получение размера логов IIS-Exchange.
     <p>
     Путь до папки из {@link #APP_PROPS} iispath. <br> {@code Path iisLogsDir} = {@link Objects#requireNonNull(java.lang.Object)} - {@link Path#toFile()}.{@link File#listFiles()}. <br> Для каждого
     файла из папки, {@link File#length()}. Складываем {@code totalSize}. <br> {@code totalSize/}{@link ConstantsFor#MBYTE}.

     @return размер папки логов IIS в мегабайтах
     */
    public static String iisLogSize() {
        Path iisLogsDir = Paths.get(APP_PROPS.getProperty("iispath", "\\\\srv-mail3.eatmeat.ru\\c$\\inetpub\\logs\\LogFiles\\W3SVC1\\"));
        long totalSize = 0L;
        for (File x : Objects.requireNonNull(iisLogsDir.toFile().listFiles())) {
            totalSize = totalSize + x.length();
        }
        String s = totalSize / ConstantsFor.MBYTE + " MB IIS Logs\n";
        LOGGER.warn(s);
        return s;
    }

    /**
     Статистика по-пользователям за неделю.
     <p>
     Запуск new {@link SpeedChecker.ChkMailAndUpdateDB}, через {@link Executors#unconfigurableExecutorService(java.util.concurrent.ExecutorService)}
     <p>
     Если {@link LocalDate#getDayOfWeek()} equals {@link DayOfWeek#SUNDAY}, запуск new {@link WeekPCStats}
     */
    static void getWeekPCStats() {
        if (LocalDate.now().getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
            ExecutorService serviceW = Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor());
            serviceW.submit(new WeekPCStats());
        }
    }

    /**
     Запускает сканнер прав Common
     */
    protected static void runCommonScan() {
        String msg = new StringBuilder()
            .append(LocalTime.now()
                .plusMinutes(5).toString())
            .append(" ")
            .append(CommonRightsChecker.class.getSimpleName())
            .append(" been run.").toString();
        LOGGER.info(msg);
    }

    /**
     Сборщик прав \\srv-fs.eatmeat.ru\common_new
     <p>
     {@link Files#walkFileTree(java.nio.file.Path, java.nio.file.FileVisitor)}, где {@link Path} = \\srv-fs.eatmeat.ru\common_new и {@link FileVisitor} = new {@link CommonRightsChecker}.
     <p>
     <b>{@link IOException}:</b><br>
     {@link MessageToUser#errorAlert(java.lang.String, java.lang.String, java.lang.String)}, {@link FileSystemWorker#error(java.lang.String, java.lang.Exception)}
     */
    private static void commonRightsMeth() {
        messageToUser.infoNoTitles("AppInfoOnLoad.commonRights");
        try {
            FileVisitor<Path> commonRightsChecker = new CommonRightsChecker();
            Files.walkFileTree(Paths.get("\\\\srv-fs.eatmeat.ru\\common_new"), commonRightsChecker);
        } catch (IOException e) {
            messageToUser.errorAlert("AppInfoOnLoad", "commonRightsMeth", e.getMessage());
            FileSystemWorker.error("AppInfoOnLoad.commonRightsMeth", e);
        }
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
     2. {@link FileSystemWorker#readFileToList(java.lang.String)}. Прочитаем exit.last, если он существует. {@link TForms#fromArray(java.util.List, boolean)} <br><br>
     3. {@link #checkDay(ScheduledExecutorService)} метрика. <br>
     4. {@link #checkDay(java.util.concurrent.ScheduledExecutorService)}. Выведем сообщение, когда и что ствртует.
     <p>

     @param scheduledExecutorService {@link ScheduledExecutorService}.
     @throws MyNull искл.
     */
    @SuppressWarnings("MagicNumber")
    private static void dateSchedulers(ScheduledExecutorService scheduledExecutorService) throws MyNull {
        String classMeth = "AppInfoOnLoad.dateSchedulers";
        new MessageCons().errorAlert(classMeth);
        Thread.currentThread().setName(classMeth);
        long stArt = System.currentTimeMillis();
        Date nextStartDay = MyCalen.getNextDayofWeek(23, 57, DayOfWeek.SUNDAY);
        StringBuilder stringBuilder = new StringBuilder();
        long delay = TimeUnit.HOURS.toMillis(ConstantsFor.ONE_DAY_HOURS * 7);
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.initialize();
        threadPoolTaskScheduler.scheduleWithFixedDelay(new WeekPCStats(), nextStartDay, delay);
        stringBuilder.append(nextStartDay.toString()).append(" WeekPCStats() start\n");
        nextStartDay = new Date(nextStartDay.getTime() - TimeUnit.HOURS.toMillis(1));
        threadPoolTaskScheduler.scheduleWithFixedDelay(new MailIISLogsCleaner(), nextStartDay, delay);
        stringBuilder.append(nextStartDay.toString()).append(" MailIISLogsCleaner() start\n");
        String exitLast = "No file";
        if (new File("exit.last").exists()) {
            exitLast = new TForms().fromArray(FileSystemWorker.readFileToList("exit.last"), false);
        }
        stringBuilder.append("\n").append(methMetr(stArt, classMeth));
        String logStr = stringBuilder.toString();
        exitLast = exitLast + "\n" + checkDay(scheduledExecutorService) + "\n" + logStr;
        LOGGER.warn(logStr);
        new MessageCons().info(ConstantsFor.STR_INPUT_OUTPUT, "scheduledExecutorService = [" + scheduledExecutorService.toString() + "]", "void");
        String finalExitLast = exitLast;
        new MessageSwing(555, 333, 36, 31).infoTimer(45, finalExitLast);
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
     Очистка pcuserauto
     */
    private static void trunkTableUsers() {
        MessageToUser eSender = new ESender(ConstantsFor.GMAIL_COM);
        try (Connection c = new RegRuMysql().getDefaultConnection(ConstantsFor.U_0466446_VELKOM);
             PreparedStatement preparedStatement = c.prepareStatement("TRUNCATE TABLE pcuserauto")) {
            preparedStatement.executeUpdate();
            eSender.infoNoTitles("TRUNCATE true\n" + ConstantsFor.getUpTime() + STR_UPTIME);
        } catch (SQLException e) {
            eSender.infoNoTitles("TRUNCATE false\n" + ConstantsFor.getUpTime() + STR_UPTIME);
        }
    }

    /**
     Немного инфомации о приложении.

     @param appCtx {@link ApplicationContext}
     */
    private void infoForU(ApplicationContext appCtx) {
        new MessageCons().errorAlert("AppInfoOnLoad.infoForU");
        new MessageCons().info(ConstantsFor.STR_INPUT_OUTPUT, "appCtx = [" + appCtx + "]", "void");
        String msg = new StringBuilder()
            .append(appCtx.getApplicationName())
            .append(" app name")
            .append(appCtx.getDisplayName())
            .append(" app display name\n")
            .append(ConstantsFor.getBuildStamp()).toString();
        LOGGER.info(msg);
        schedStarter();
    }

    /**
     Запуск заданий по-расписанию
     <p>
     Usages: {@link #infoForU(ApplicationContext)} <br>
     Uses: 1.1 {@link #dateSchedulers(ScheduledExecutorService)}, 1.2 {@link ConstantsFor#thisPC()}, 1.3 {@link ConstantsFor#thisPC()}.
     */
    @SuppressWarnings("MagicNumber")
    private void schedStarter() {
        String classMeth = "AppInfoOnLoad.schedStarter";
        new MessageCons().errorAlert(classMeth);
        final long stArt = System.currentTimeMillis();
        ScheduledExecutorService scheduledExecutorService = Executors.unconfigurableScheduledExecutorService(Executors.newScheduledThreadPool(5));
        List<String> miniLogger = new ArrayList<>();
        miniLogger.add(this.getClass().getSimpleName());

        ThreadConfig threadConfig = AppComponents.threadConfig();
        String thisPC = ConstantsFor.thisPC();
        miniLogger.add(thisPC);

        if (!thisPC.toLowerCase().contains("home")) {
            scheduledExecutorService.scheduleWithFixedDelay(commonRights, 10, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);
            miniLogger.add(commonRights.toString());
            miniLogger.add(threadConfig.toString());
        }
        scheduledExecutorService.scheduleWithFixedDelay(ScanOnline.getI(), 3, 1, TimeUnit.MINUTES);
        scheduledExecutorService.scheduleWithFixedDelay(diapazonedScan, 2, THIS_DELAY, TimeUnit.MINUTES);
        scheduledExecutorService.scheduleWithFixedDelay(new NetMonitorPTV(), 0, 10, TimeUnit.SECONDS);

        String msg = new StringBuilder()
            .append(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(THIS_DELAY)))
            .append(DiapazonedScan.getInstance().getClass().getSimpleName())
            .append(" is starts next time.\n")
            .append(methMetr(stArt, classMeth))
            .toString();
        miniLogger.add(msg);

        try {
            dateSchedulers(scheduledExecutorService);
        } catch (MyNull e) {
            new MessageCons().errorAlert(CLASS_NAME, "schedStarter", e.getMessage());
        }
        new MessageCons().infoNoTitles(new TForms().fromArray(miniLogger, false));
        FileSystemWorker.recFile(CLASS_NAME, miniLogger.stream());
    }

    /**
     Старт
     <p>
     {@link #infoForU(ApplicationContext)}
     */
    @Override
    public void run() {
        infoForU(AppCtx.scanForBeansAndRefreshContext());
    }
}
