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

import java.io.*;
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
import java.util.Date;
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
    private static final Runnable r = AppInfoOnLoad::commonRights;

    private DiapazonedScan diapazonedScan = DiapazonedScan.getInstance();

    /**
     Задержка выполнения для этого класса

     @see #schedStarter()
     */
    private static final int THIS_DELAY = 111;

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
        LOGGER.warn("AppInfoOnLoad.commonRights");
        try {
            FileVisitor<Path> commonRightsChecker = new CommonRightsChecker();
            Files.walkFileTree(Paths.get("\\\\srv-fs.eatmeat.ru\\common_new"), commonRightsChecker);
        } catch (IOException e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    void spToFile() {
        ExecutorService service = Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor());
        service.submit(new SpeedChecker.ChkMailAndUpdateDB());
        if(LocalDate.now().getDayOfWeek().equals(DayOfWeek.SUNDAY)){
            ExecutorService serviceW = Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor());
            serviceW.submit(new WeekPCStats());
        }
    }

    /**
     @see #infoForU(ApplicationContext)
     */
    @Override
    public void run() {
        diaScanReader();
        infoForU(AppCtx.scanForBeansAndRefreshContext());
    }

    private void diaScanReader() {
        try (ObjectInput input = new ObjectInputStream(new FileInputStream(diapazonedScan.getClass().getSimpleName()))) {
            this.diapazonedScan = (DiapazonedScan) input.readObject();
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.error(getClass().getSimpleName(), e.getMessage(), e);
        }
    }

    /**
     Запуск заданий по-расписанию
     <p>
     Usages: {@link #infoForU(ApplicationContext)} <br> Uses: 1.1 {@link #dateSchedulers()}, 1.2 {@link ConstantsFor#thisPC()}, 1.3
     {@link ConstantsFor#thisPC()} .@param s
     */
    private void schedStarter() {
        final long stArt = System.currentTimeMillis();
        ScheduledExecutorService executorService = Executors.unconfigurableScheduledExecutorService(Executors.newScheduledThreadPool(4));
        String thisPC = ConstantsFor.thisPC();
        if(!thisPC.toLowerCase().contains("home")){
            executorService.scheduleWithFixedDelay(r, 5, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);
        }
        executorService
            .scheduleWithFixedDelay(diapazonedScan, 3, THIS_DELAY, TimeUnit.MINUTES);

        executorService.scheduleWithFixedDelay(ScanOnline.getI(), 4, 1, TimeUnit.MINUTES);
        executorService.scheduleWithFixedDelay(ScanOffline.getI(), 5, 2, TimeUnit.MINUTES);
        String msg = new StringBuilder()
            .append(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(THIS_DELAY)))
            .append(DiapazonedScan.getInstance().getClass().getSimpleName())
            .append(" is starts next time.\n")
            .append(methMetr(stArt))
            .toString();
        new MessageCons().infoNoTitles(msg);
        try{
            dateSchedulers();
        }
        catch(MyNull e){
            new MessageToTray().errorAlert(getClass().getSimpleName(), e.getMessage(), new TForms().fromArray(e, false));
        }
    }

    /**
     Стата за неделю по-ПК
     <p>
     Usages: {@link #schedStarter()} <br> Uses: 1.1 {@link MyCalen#getNextDayofWeek(int, int, DayOfWeek)}, 1.2 {@link ThreadConfig#threadPoolTaskScheduler()}@param s
     */
    @SuppressWarnings("MagicNumber")
    private static void dateSchedulers() throws MyNull {
        Thread.currentThread().setName("AppInfoOnLoad.dateSchedulers");
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

    private static String methMetr(long stArt) {
        String msgTimeSp = new StringBuilder()
            .append("AppInfoOnLoad.schedStarter: ")
            .append(( float ) (System.currentTimeMillis() - stArt) / 1000)
            .append(ConstantsFor.STR_SEC_SPEND)
            .toString();
        new MessageCons().infoNoTitles(msgTimeSp);
        return msgTimeSp;
    }

    private static String checkDay() {
        Date dateStart = MyCalen.getNextDayofWeek(10, 0, DayOfWeek.MONDAY);
        DateFormat dateFormat = new SimpleDateFormat();
        String msg = dateFormat.format(dateStart) + " pcuserauto";
        ThreadConfig t = new ThreadConfig();
        ThreadPoolTaskScheduler threadPoolTaskScheduler = t.threadPoolTaskScheduler();
        threadPoolTaskScheduler.scheduleWithFixedDelay(AppInfoOnLoad::trunkTableUsers, dateStart, ConstantsFor.ONE_WEEK_MILLIS);
        return msg;
    }

    public static String iisLogSize() {
        Path iisLogsDir = Paths.get("\\\\srv-mail3.eatmeat.ru\\c$\\inetpub\\logs\\LogFiles\\W3SVC1\\");
        long totalSize = 0L;
        for (File x : Objects.requireNonNull(iisLogsDir.toFile().listFiles())) {
            totalSize = totalSize + x.length();
        }
        String s = totalSize / ConstantsFor.MBYTE + " MB IIS Logs\n";
        LOGGER.warn(s);
        return s;
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
