package ru.vachok.networker;


import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import ru.vachok.messenger.MessageSwing;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.EMailAndDB.SpeedRunActualize;
import ru.vachok.networker.accesscontrol.common.CommonRightsChecker;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.config.AppCtx;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.mailserver.MailIISLogsCleaner;
import ru.vachok.networker.net.DiapazonedScan;
import ru.vachok.networker.net.SwitchesAvailability;
import ru.vachok.networker.services.MyCalen;
import ru.vachok.networker.services.WeekPCStats;

import java.io.IOException;
import java.nio.file.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Date;
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
     Повторение более 3х раз в строках
     */
    private static final String STR_SEC_SPEND = ConstantsFor.STR_SEC_SPEND;

    /**
     Задержка выполнения для этого класса

     @see #schedStarter()
     */
    private static final int THIS_DELAY = 111;

    private static MessageToUser messageToUser = new MessageSwing();

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
     @see #infoForU(ApplicationContext)
     */
    @Override
    public void run() {
        infoForU(AppCtx.scanForBeansAndRefreshContext());
    }

    /**
     Стата за неделю по-ПК
     <p>
     Usages: {@link #schedStarter()} <br> Uses: 1.1 {@link MyCalen#getNextDayofWeek(int, int, DayOfWeek)}, 1.2 {@link ThreadConfig#threadPoolTaskScheduler()}
     */
    @SuppressWarnings ("MagicNumber")
    private static void dateSchedulers() {
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
        messageToUser.infoNoTitles(logStr + "\n" + new TForms().fromArray(ConstantsFor.getProps()));
    }

    /**
     Запуск заданий по-расписанию
     <p>
     Usages: {@link #infoForU(ApplicationContext)} <br> Uses: 1.1 {@link #dateSchedulers()}, 1.2 {@link ConstantsFor#thisPC()}, 1.3 {@link ConstantsFor#thisPC()} .
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
        String msgTimeSp = "IntoApplication.infoForU method. " + ( float ) (System.currentTimeMillis() - stArt) / 1000 +
            STR_SEC_SPEND;
        messageToUser.infoNoTitles(MyCalen.toStringS() + "\n" + msgTimeSp);
    }

}
