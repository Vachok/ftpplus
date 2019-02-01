package ru.vachok.networker;


import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import ru.vachok.messenger.MessageCons;
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
import ru.vachok.networker.net.ScanOffline;
import ru.vachok.networker.net.ScanOnline;
import ru.vachok.networker.net.WeekPCStats;
import ru.vachok.networker.services.MyCalen;
import ru.vachok.networker.services.SpeedChecker;
import ru.vachok.networker.systray.ActionOnAppStart;
import ru.vachok.networker.systray.MessageToTray;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
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
     {@link AppComponents#getLogger()}
     */
    private static final Logger LOGGER = AppComponents.getLogger();

    /**
     Запуск {@link CommonRightsChecker}
     */
    private static final Runnable commonRights = AppInfoOnLoad::commonRights;

    private static final String CLASS_NAME = "AppInfoOnLoad";

    /**
     Задержка выполнения для этого класса

     @see #schedStarter()
     */
    private static final int THIS_DELAY = 111;

    private static DiapazonedScan diapazonedScan = DiapazonedScan.getInstance();

    public static String iisLogSize() {
        new MessageCons().errorAlert("AppInfoOnLoad.iisLogSize");
        new MessageCons().info(ConstantsFor.STR_INPUT_OUTPUT, "", ConstantsFor.JAVA_LANG_STRING_NAME);
        Path iisLogsDir = Paths.get("\\\\srv-mail3.eatmeat.ru\\c$\\inetpub\\logs\\LogFiles\\W3SVC1\\");
        long totalSize = 0L;
        for (File x : Objects.requireNonNull(iisLogsDir.toFile().listFiles())) {
            totalSize = totalSize + x.length();
        }
        String s = totalSize / ConstantsFor.MBYTE + " MB IIS Logs\n";
        LOGGER.warn(s);
        return s;
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

    private static void commonRights() {
        new MessageCons().errorAlert("AppInfoOnLoad.commonRights");
        try {
            FileVisitor<Path> commonRightsChecker = new CommonRightsChecker();
            Files.walkFileTree(Paths.get("\\\\srv-fs.eatmeat.ru\\common_new"), commonRightsChecker);
        } catch (IOException e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    private static String methMetr(long stArt) {
        String msgTimeSp = new StringBuilder()
            .append("AppInfoOnLoad.schedStarter: ")
            .append((float) (System.currentTimeMillis() - stArt) / 1000)
            .append(ConstantsFor.STR_SEC_SPEND)
            .toString();
        new MessageCons().infoNoTitles(msgTimeSp);
        return msgTimeSp;
    }

    /**
     Стата за неделю по-ПК
     <p>
     Usages: {@link #schedStarter()} <br> Uses: 1.1 {@link MyCalen#getNextDayofWeek(int, int, DayOfWeek)}, 1.2 {@link ThreadConfig#threadPoolTaskScheduler()}@param s
     */
    @SuppressWarnings("MagicNumber")
    private static void dateSchedulers() throws MyNull {
        String classMeth = "AppInfoOnLoad.dateSchedulers";
        new MessageCons().errorAlert(classMeth);
        Thread.currentThread().setName(classMeth);
        long stArt = System.currentTimeMillis();
        Date nextStartDay = MyCalen.getNextDayofWeek(23, 57, DayOfWeek.SUNDAY);
        StringBuilder stringBuilder = new StringBuilder();
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadConfig().threadPoolTaskScheduler();
        long delay = TimeUnit.HOURS.toMillis(ConstantsFor.ONE_DAY_HOURS * 7);

        threadPoolTaskScheduler.scheduleWithFixedDelay(new WeekPCStats(), nextStartDay, delay);
        stringBuilder.append(nextStartDay.toString()).append(" WeekPCStats() start |  ");

        nextStartDay = new Date(nextStartDay.getTime() - TimeUnit.HOURS.toMillis(1));
        threadPoolTaskScheduler.scheduleWithFixedDelay(new MailIISLogsCleaner(), nextStartDay, delay);
        stringBuilder.append(nextStartDay.toString()).append(" MailIISLogsCleaner() start. ");

        String exitLast = "No file";
        if (new File("exit.last").exists()) {
            exitLast = new TForms().fromArray(FileSystemWorker.readFileToList("exit.last"), false);
        }
        stringBuilder.append("\n").append(methMetr(stArt));
        String logStr = stringBuilder.toString();
        LOGGER.warn(logStr);
        new MessageToTray(new ActionOnAppStart()).info(checkDay(), exitLast, iisLogSize());
    }

    private static String checkDay() {
        new MessageCons().errorAlert("AppInfoOnLoad.checkDay");
        new MessageCons().info(ConstantsFor.STR_INPUT_OUTPUT, "", ConstantsFor.JAVA_LANG_STRING_NAME);
        Date dateStart = MyCalen.getNextDayofWeek(10, 0, DayOfWeek.MONDAY);
        DateFormat dateFormat = new SimpleDateFormat();
        String msg = dateFormat.format(dateStart) + " pcuserauto";
        ThreadConfig t = new ThreadConfig();
        ThreadPoolTaskScheduler threadPoolTaskScheduler = t.threadPoolTaskScheduler();
        threadPoolTaskScheduler.scheduleWithFixedDelay(AppInfoOnLoad::trunkTableUsers, dateStart, ConstantsFor.ONE_WEEK_MILLIS);
        new MessageCons().infoNoTitles("msg = " + msg);
        return msg;
    }

    private static void trunkTableUsers() {
        MessageToUser messageToUser = new ESender(ConstantsFor.GMAIL_COM);
        try (Connection c = new RegRuMysql().getDefaultConnection(ConstantsFor.U_0466446_VELKOM);
             PreparedStatement preparedStatement = c.prepareStatement("TRUNCATE TABLE pcuserauto")) {
            preparedStatement.executeUpdate();
            messageToUser.infoNoTitles("TRUNCATE true\n" + ConstantsFor.getUpTime() + " uptime.");
        } catch (SQLException e) {
            messageToUser.infoNoTitles("TRUNCATE false\n" + ConstantsFor.getUpTime() + " uptime.");
        }
    }

    void spToFile() {
        ExecutorService service = Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor());
        service.submit(new SpeedChecker.ChkMailAndUpdateDB());
        if (LocalDate.now().getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
            ExecutorService serviceW = Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor());
            serviceW.submit(new WeekPCStats());
        }
    }

    /**
     <b>1.1 Краткая сводка</b>
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
     Usages: {@link #infoForU(ApplicationContext)} <br> Uses: 1.1 {@link #dateSchedulers()}, 1.2 {@link ConstantsFor#thisPC()}, 1.3 {@link ConstantsFor#thisPC()} .@param s
     */
    @SuppressWarnings("MagicNumber")
    private void schedStarter() {
        new MessageCons().errorAlert("AppInfoOnLoad.schedStarter");
        final long stArt = System.currentTimeMillis();

        List<String> miniLogger = new ArrayList<>();
        miniLogger.add(this.getClass().getSimpleName());

        ThreadConfig threadConfig = new ThreadConfig();
        ScanOffline scanOffline = ScanOffline.getI();
        ScanOnline scanOnline = ScanOnline.getI();
        String thisPC = ConstantsFor.thisPC();
        miniLogger.add(thisPC);

        ScheduledExecutorService executorService = threadConfig.threadPoolTaskScheduler().getScheduledThreadPoolExecutor();
        if (!thisPC.toLowerCase().contains("home")) {
            executorService.scheduleWithFixedDelay(commonRights, 10, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);
            miniLogger.add(commonRights.toString());
            miniLogger.add(threadConfig.toString());
        }
        executorService.scheduleWithFixedDelay(diapazonedScan, 2, THIS_DELAY, TimeUnit.MINUTES);
        executorService.scheduleWithFixedDelay(scanOnline, 3, 1, TimeUnit.MINUTES);
        executorService.scheduleWithFixedDelay(scanOffline, 200, 70, TimeUnit.SECONDS);

        String msg = new StringBuilder()
            .append(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(THIS_DELAY)))
            .append(DiapazonedScan.getInstance().getClass().getSimpleName())
            .append(" is starts next time.\n")
            .append(methMetr(stArt))
            .toString();
        miniLogger.add(msg);

        try {
            dateSchedulers();
        } catch (MyNull e) {
            new MessageCons().errorAlert(CLASS_NAME, "schedStarter", e.getMessage());
        }
        new MessageCons().infoNoTitles(new TForms().fromArray(miniLogger, false));
        FileSystemWorker.recFile(CLASS_NAME, miniLogger.stream());
    }

    private void deSer() {
        File rootPathFile = new File(".");
        for (File file : Objects.requireNonNull(rootPathFile.listFiles())) {
            if (file.getName().toLowerCase().contains(".ser")) {
                try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file.getAbsolutePath()))) {
                    Object readObject = objectInputStream.readObject();
                    tryCasting(readObject, file);
                } catch (IOException | ClassNotFoundException e) {
                    new MessageCons().errorAlert(CLASS_NAME, "deSer", TForms.from(e));
                }
            }
        }
    }

    private void tryCasting(Object readObject, File file) throws ClassNotFoundException {
        String inClassName = file.getName().replace(".ser", "");
        Class<?> inClass = Class.forName(inClassName);
        boolean inClassInstance = inClass.isInstance(readObject);
        if (inClassInstance) {
            inClass.cast(readObject);
            String valStr = "readObject = " + readObject.toString() + " AppInfoOnLoad.tryCasting";
            new MessageCons().info("SOUTV", "AppInfoOnLoad.tryCasting", valStr);
        } else {
            new MessageCons().errorAlert("AppInfoOnLoad.tryCasting");
            new MessageCons().info("readObject = [" + readObject + "], file = [" + file + "]", "input parameters] [Returns:", "void");
        }
    }

    /**
     @see #infoForU(ApplicationContext)
     */
    @Override
    public void run() {
        infoForU(AppCtx.scanForBeansAndRefreshContext());
        deSer();
    }

}
