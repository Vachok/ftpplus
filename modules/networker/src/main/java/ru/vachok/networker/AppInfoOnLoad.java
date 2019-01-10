package ru.vachok.networker;


import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import ru.vachok.messenger.MessageSwing;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.messenger.email.ESender;
import ru.vachok.mysqlandprops.EMailAndDB.SpeedRunActualize;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.accesscontrol.common.CommonRightsChecker;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.config.AppCtx;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.mailserver.MailIISLogsCleaner;
import ru.vachok.networker.net.DiapazonedScan;
import ru.vachok.networker.net.SwitchesAvailability;
import ru.vachok.networker.net.WeekPCStats;
import ru.vachok.networker.services.MyCalen;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
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
    private static Runnable r = () -> {
        try{
            FileVisitor<Path> commonRightsChecker = new CommonRightsChecker();
            Files.walkFileTree(Paths.get("\\\\srv-fs.eatmeat.ru\\common_new"), commonRightsChecker);
        }
        catch(IOException e){
            LOGGER.warn(e.getMessage(), e);
        }
    };

    /**
     Повторение более 3х раз в строках
     */
    private static final String STR_SEC_SPEND = ConstantsFor.STR_SEC_SPEND;

    /**
     Задержка выполнения для этого класса

     @see #schedStarter()
     */
    private static final int THIS_DELAY = 111;

    /**
     Запускает сканнер прав Common
     */
    static void runCommonScan(boolean runNow) {
        if(runNow){
            Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).execute(r);
        }
        else{
            String msg = new StringBuilder()
                .append(LocalTime.now()
                    .plusMinutes(5).toString())
                .append(" ")
                .append(CommonRightsChecker.class.getSimpleName())
                .append(" been run.").toString();
            LOGGER.info(msg);
        }
    }

    /**
     <b>1.1 Краткая сводка</b>
     Немного инфомации о приложении.

     @param appCtx {@link ApplicationContext}
     */
    private void infoForU(ApplicationContext appCtx) {
        final long stArt = System.currentTimeMillis();
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
    private void schedStarter() {
        Runnable speedRun = null;
        try{
            speedRun = new SpeedRunActualize();
        }
        catch(ExceptionInInitializerError e){
            LOGGER.warn(e.getMessage(), e);
        }
        Runnable swAval = new SwitchesAvailability();
        ScheduledExecutorService executorService = Executors.unconfigurableScheduledExecutorService(Executors.newScheduledThreadPool(4));
        executorService
            .scheduleWithFixedDelay(Objects.requireNonNull(speedRun), ConstantsFor.INIT_DELAY, TimeUnit.MINUTES.toSeconds(ConstantsFor.DELAY), TimeUnit.SECONDS);
        String thisPC = ConstantsFor.thisPC();
        if(!thisPC.toLowerCase().contains("home")){
            executorService
                .scheduleWithFixedDelay(swAval, 10, ConstantsFor.DELAY, TimeUnit.SECONDS);
            executorService.scheduleWithFixedDelay(r, 5, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);
        }
        executorService
            .scheduleWithFixedDelay(DiapazonedScan.getInstance(), 3, THIS_DELAY, TimeUnit.MINUTES);
        String msg = new StringBuilder()
            .append("Scheduled: DiapazonedScan.getInstance(). Initial delay 1. delay ")
            .append(THIS_DELAY)
            .append(" in minutes\n")
            .append(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(THIS_DELAY + 1))).toString();
        LOGGER.info(msg);
        dateSchedulers();
    }

    /**
     @see #infoForU(ApplicationContext)
     */
    @Override
    public void run() {
        infoForU(AppCtx.scanForBeansAndRefreshContext());
    }

    /**
     Стата за неделю по-ПК
     <p>
     Usages: {@link #schedStarter()} <br> Uses: 1.1 {@link MyCalen#getNextDayofWeek(int, int, DayOfWeek)}, 1.2 {@link ThreadConfig#threadPoolTaskScheduler()}@param s
     */
    @SuppressWarnings ("MagicNumber")
    private static void dateSchedulers() {
        MessageToUser messageToUser = new MessageSwing();
        Date nextStartDay = MyCalen.getNextDayofWeek(23, 57, DayOfWeek.SUNDAY);
        StringBuilder stringBuilder = new StringBuilder();
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadConfig().threadPoolTaskScheduler();
        long delay = TimeUnit.HOURS.toMillis(ConstantsFor.ONE_DAY_HOURS * 7);

        threadPoolTaskScheduler.scheduleWithFixedDelay(new WeekPCStats(), nextStartDay, delay);
        stringBuilder.append(nextStartDay.toString()).append(" WeekPCStats() start | \n");

        nextStartDay = new Date(nextStartDay.getTime() - TimeUnit.HOURS.toMillis(1));
        threadPoolTaskScheduler.scheduleWithFixedDelay(new MailIISLogsCleaner(), nextStartDay, delay);
        stringBuilder.append(nextStartDay.toString()).append(" MailIISLogsCleaner() start.\n");

        String logStr = stringBuilder.toString();
        LOGGER.warn(logStr);
        messageToUser.infoNoTitles(logStr + "\n" +
            checkDay() +
            iisLogSize() + "\n" +
            FileSystemWorker.readFile("exit.last"));
    }

    private static String checkDay() {
        Date dateStart = MyCalen.getNextDayofWeek(10, 0, DayOfWeek.MONDAY);
        String msg = dateStart + " - date to TRUNCATE , " +
            LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault()) + "\n" +
            ConstantsFor.ONE_WEEK_MILLIS + " ms delay.\n";
        ThreadConfig t = new ThreadConfig();
        ThreadPoolTaskScheduler threadPoolTaskScheduler = t.threadPoolTaskScheduler();
        threadPoolTaskScheduler.scheduleWithFixedDelay(AppInfoOnLoad::trunkTableUsers, dateStart, ConstantsFor.ONE_WEEK_MILLIS);
        return msg;
    }

    private static void trunkTableUsers() {
        MessageToUser messageToUser = new ESender(ConstantsFor.GMAIL_COM);
        try(Connection c = new RegRuMysql().getDefaultConnection(ConstantsFor.U_0466446_VELKOM);
            PreparedStatement preparedStatement = c.prepareStatement("TRUNCATE TABLE pcuserauto")){
            preparedStatement.executeUpdate();
            messageToUser.infoNoTitles("TRUNCATE true\n" + ConstantsFor.getUpTime() + " uptime.");
        }
        catch(SQLException e){
            messageToUser.infoNoTitles("TRUNCATE false\n" + ConstantsFor.getUpTime() + " uptime.");
        }
    }

    static String iisLogSize() {
        Path iisLogsDir = Paths.get("\\\\srv-mail3.eatmeat.ru\\c$\\inetpub\\logs\\LogFiles\\W3SVC1\\");
        long totalSize = 0L;
        for(File x : iisLogsDir.toFile().listFiles()){
            totalSize = totalSize + x.length();
        }
        return "\n" + totalSize / ConstantsFor.MBYTE + " MB of " + iisLogsDir + " IIS Logs";
    }

}
