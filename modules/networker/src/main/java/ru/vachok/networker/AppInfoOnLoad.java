package ru.vachok.networker;


import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.messenger.email.ESender;
import ru.vachok.mysqlandprops.EMailAndDB.SpeedRunActualize;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.accesscontrol.common.CommonRightsChecker;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.config.AppCtx;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.errorexceptions.MyNull;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.mailserver.MailIISLogsCleaner;
import ru.vachok.networker.net.DiapazonedScan;
import ru.vachok.networker.net.SwitchesAvailability;
import ru.vachok.networker.net.WeekPCStats;
import ru.vachok.networker.services.MessageToTray;
import ru.vachok.networker.services.MyCalen;
import ru.vachok.networker.services.SpeedChecker;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.*;


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
     Задержка выполнения для этого класса

     @see #schedStarter()
     */
    private static final int THIS_DELAY = 111;

    /**
     Запускает сканнер прав Common
     */
    static void runCommonScan() {
        String msg = new StringBuilder()
            .append(LocalTime.now()
                .plusMinutes(5).toString())
            .append(" ")
            .append(CommonRightsChecker.class.getSimpleName())
            .append(" been run.").toString();
        LOGGER.info(msg);
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
            FileSystemWorker.recFile(
                this.getClass().getSimpleName() + ConstantsFor.LOG,
                Collections.singletonList(new TForms().fromArray(e.getStackTrace(), false)));
        }
        Runnable swAval = new SwitchesAvailability();
        ScheduledExecutorService executorService = Executors.unconfigurableScheduledExecutorService(Executors.newScheduledThreadPool(4));
        executorService
            .scheduleWithFixedDelay(Objects.requireNonNull(speedRun), 3, 3, TimeUnit.MINUTES);
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
        try{
            dateSchedulers();
        }
        catch(MyNull e){
            new MessageToTray().errorAlert(getClass().getSimpleName(), e.getMessage(), new TForms().fromArray(e, false));
        }
    }

    void spToFile() {
        ExecutorService service = Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor());
        service.submit(new SpeedChecker.SpFromMail());
        if(LocalDate.now().getDayOfWeek().equals(DayOfWeek.SUNDAY)){
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
     Стата за неделю по-ПК
     <p>
     Usages: {@link #schedStarter()} <br> Uses: 1.1 {@link MyCalen#getNextDayofWeek(int, int, DayOfWeek)}, 1.2 {@link ThreadConfig#threadPoolTaskScheduler()}@param s
     */
    @SuppressWarnings ("MagicNumber")
    private static void dateSchedulers() throws MyNull {
        Date nextStartDay = MyCalen.getNextDayofWeek(23, 57, DayOfWeek.SUNDAY);
        StringBuilder stringBuilder = new StringBuilder();
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadConfig().threadPoolTaskScheduler();
        long delay = TimeUnit.HOURS.toMillis(ConstantsFor.ONE_DAY_HOURS * 7);

        threadPoolTaskScheduler.scheduleWithFixedDelay(new WeekPCStats(), nextStartDay, delay);
        stringBuilder.append(nextStartDay.toString()).append(" WeekPCStats() start |  ");

        nextStartDay = new Date(nextStartDay.getTime() - TimeUnit.HOURS.toMillis(1));
        threadPoolTaskScheduler.scheduleWithFixedDelay(new MailIISLogsCleaner(), nextStartDay, delay);
        stringBuilder.append(nextStartDay.toString()).append(" MailIISLogsCleaner() start. ");

        String logStr = stringBuilder.toString();
        LOGGER.warn(logStr);
        new MessageToTray().infoNoTitles(checkDay() + iisLogSize());
    }

    private static String checkDay() {
        Date dateStart = MyCalen.getNextDayofWeek(10, 0, DayOfWeek.MONDAY);
        String msg = dateStart + " - date to TRUNCATE , ";
        ThreadConfig t = new ThreadConfig();
        ThreadPoolTaskScheduler threadPoolTaskScheduler = t.threadPoolTaskScheduler();
        threadPoolTaskScheduler.scheduleWithFixedDelay(AppInfoOnLoad::trunkTableUsers, dateStart, ConstantsFor.ONE_WEEK_MILLIS);
        return msg;
    }

    static String iisLogSize() throws MyNull {
        Path iisLogsDir = Paths.get("\\\\srv-mail3.eatmeat.ru\\c$\\inetpub\\logs\\LogFiles\\W3SVC1\\");
        long totalSize = 0L;
        for(File x : iisLogsDir.toFile().listFiles()){
            totalSize = totalSize + x.length();
        }
        return totalSize / ConstantsFor.MBYTE + " MB of " + iisLogsDir + " IIS Logs\n";
    }

    /**
     @see #infoForU(ApplicationContext)
     */
    @Override
    public void run() {
        infoForU(AppCtx.scanForBeansAndRefreshContext());
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

}
