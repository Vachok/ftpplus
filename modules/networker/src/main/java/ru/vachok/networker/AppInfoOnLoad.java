package ru.vachok.networker;


import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import ru.vachok.mysqlandprops.EMailAndDB.SpeedRunActualize;
import ru.vachok.networker.accesscontrol.common.CommonRightsChecker;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.config.AppCtx;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.SwitchesAvailability;
import ru.vachok.networker.services.MyCalen;
import ru.vachok.networker.services.WeekPCStats;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.DayOfWeek;
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
     Повторение более 3х раз в строках
     */
    private static final String STR_SEC_SPEND = " sec spend";

    /**
     Имя ПК. no0027.
     */
    private static final String STR_PC_NO0027 = "no0027";

    /**
     {@link File} - const.txt
     */
    private static final File CONST_TXT = new File("const.txt");

    /**
     @return {@link #CONST_TXT}
     */
    public static File getConstTxt() {
        return CONST_TXT;
    }

    @Override
    public void run() {
        infoForU(AppCtx.scanForBeansAndRefreshContext());
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
        String msgTimeSp = "IntoApplication.infoForU method. " + (float) (System.currentTimeMillis() - stArt) / 1000 +
            STR_SEC_SPEND;
        LOGGER.info(msgTimeSp);
    }

    /**
     Запуск заданий по-расписанию
     <p>
     Usages: {@link #infoForU(ApplicationContext)} <br> Uses: 1.1 {@link #weekStat()}, 1.2 {@link ConstantsFor#thisPC()}, 1.3 {@link ConstantsFor#thisPC()}, 1.4 {@link #runCommonScan()} .
     */
    private void schedStarter() {
        Runnable speedRun = null;
        try {
            speedRun = new SpeedRunActualize();
        } catch (ExceptionInInitializerError e) {
            LOGGER.warn(e.getMessage(), e);
        }
        Runnable swAval = new SwitchesAvailability();
        ScheduledExecutorService executorService = Executors.unconfigurableScheduledExecutorService(Executors.newScheduledThreadPool(2));

        executorService.scheduleWithFixedDelay(Objects.requireNonNull(speedRun), ConstantsFor.INIT_DELAY, TimeUnit.MINUTES.toSeconds(ConstantsFor.DELAY), TimeUnit.SECONDS);
        executorService.scheduleWithFixedDelay(swAval, 1, ConstantsFor.DELAY, TimeUnit.SECONDS);

        weekStat();

        if (ConstantsFor.thisPC().toLowerCase().contains(STR_PC_NO0027) ||
            ConstantsFor.thisPC().toLowerCase().contains("rups")) {
            runCommonScan();
        } else FileSystemWorker.cpConstTxt(true);
    }

    /**
     Стата за неделю по-ПК
     <p>
     Usages: {@link #schedStarter()} <br> Uses: 1.1 {@link MyCalen#getNextDayofWeek(int, int, DayOfWeek)}, 1.2 {@link ThreadConfig#threadPoolTaskScheduler()}
     */
    private static void weekStat() {
        Date nextStartDay = MyCalen.getNextDayofWeek(23, 57, DayOfWeek.SUNDAY);
        new ThreadConfig().threadPoolTaskScheduler().scheduleWithFixedDelay(
            new WeekPCStats(), nextStartDay, TimeUnit.HOURS.toMillis(ConstantsFor.ONE_DAY_HOURS * 7));
        String msgTimeSp = new StringBuilder()
            .append("new WeekPCStats: ")
            .append(nextStartDay.toString()).toString();
        LOGGER.warn(msgTimeSp);
    }

    /**
     Запускает сканнер прав Common
     <p>
     Usages: {@link #schedStarter()} Uses: {@link CommonRightsChecker}
     */
    private static void runCommonScan() {
        Runnable r = () -> {
            try {
                Files.walkFileTree(Paths.get("\\\\srv-fs.eatmeat.ru\\common_new"), new CommonRightsChecker());
            } catch (IOException e) {
                LOGGER.warn(e.getMessage(), e);
            }
        };
        Date startTime = MyCalen.getNextMonth();
        long delay = TimeUnit.DAYS.toMillis(ConstantsFor.ONE_MONTH_DAYS);
        ScheduledFuture<?> scheduleWithFixedDelay = new ThreadConfig().threadPoolTaskScheduler().scheduleWithFixedDelay(
            r, startTime, delay);
        try {
            String msg = "Common scanner : " + startTime.toString() + "  ||  " + delay + " TimeUnit.DAYS.toMillis(ConstantsFor.ONE_MONTH_DAYS)";
            LOGGER.warn(msg);
            scheduleWithFixedDelay.get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error(e.getMessage(), e);
            Thread.currentThread().interrupt();
            r.run();
        }
    }
}
