package ru.vachok.networker;


import org.apache.commons.net.ntp.TimeInfo;
import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.vachok.mysqlandprops.EMailAndDB.SpeedRunActualize;
import ru.vachok.networker.accesscontrol.MatrixCtr;
import ru.vachok.networker.accesscontrol.common.CommonRightsChecker;
import ru.vachok.networker.accesscontrol.common.CommonScan2YOlder;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.config.AppCtx;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.controller.ServiceInfoCtrl;
import ru.vachok.networker.net.MyServer;
import ru.vachok.networker.net.SwitchesAvailability;
import ru.vachok.networker.services.TimeChecker;
import ru.vachok.networker.services.WeekPCStats;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.*;


/**
 {@link IntoApplication}
 <p>
 1. {@link #main(String[])}<br> 1.1 {@link #infoForU(ApplicationContext)}
 */
@SpringBootApplication
@EnableScheduling
public class IntoApplication {

    /**
     {@link AppComponents#getLogger()}
     */
    private static final Logger LOGGER = AppComponents.getLogger();

    /**
     {@link SpringApplication}
     */
    private static final SpringApplication SPRING_APPLICATION = new SpringApplication();

    /**
     Имя ПК, на котором запущена программа.
     <p>
     <p>
     {@link ConstantsFor#thisPC()}
     */
    private static final String THIS_PC = ConstantsFor.thisPC();

    /**
     {@link AppCtx#scanForBeansAndRefreshContext()}
     */
    private static AnnotationConfigApplicationContext appCtx = AppCtx.scanForBeansAndRefreshContext();

    /**
     <h1>1. Точка входа в Spring Boot Application</h1>
     <p>
     {@link #infoForU(ApplicationContext)}

     @param args null
     @see MatrixCtr
     */
    public static void main(String[] args) {
        String msg = LocalDate.now().getDayOfWeek().getValue() + " - day of week\n" +
            LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault());
        LOGGER.warn(msg);
        if (THIS_PC.toLowerCase().contains("no0027") || THIS_PC.toLowerCase().contains("home")) {
            SystemTrayHelper.addTray("icons8-плохие-поросята-32.png");
        } else {
            SystemTrayHelper.addTray(null);
        }
        SPRING_APPLICATION.setMainApplicationClass(IntoApplication.class);
        SPRING_APPLICATION.setApplicationContextClass(AppCtx.class);
        System.setProperty("file.encoding", "UTF8");
        SpringApplication.run(IntoApplication.class, args);
        infoForU(appCtx);
    }

    /**
     <b>1.1 Краткая сводка</b>
     Немного инфомации о приложении.

     @param appCtx {@link ApplicationContext}
     */
    private static void infoForU(ApplicationContext appCtx) {
        String msg = new StringBuilder()
            .append(appCtx.getApplicationName())
            .append(" app name")
            .append(appCtx.getDisplayName())
            .append(" app display name\n")
            .append(ConstantsFor.getBuildStamp()).toString();
        LOGGER.info(msg);
        try{
            schedStarter();
        }
        catch(InvocationTargetException e){
            Throwable targetException = e.getTargetException();
            LOGGER.error(e.getMessage(), e);
            LOGGER.warn(targetException.getMessage());
            new Thread(IntoApplication::new).start();
            Thread.currentThread().interrupt();
        }
    }

    /**
     Удаление временных файлов.
     <p>
     Usages: {@link SystemTrayHelper#addTray(String)}, {@link ServiceInfoCtrl#closeApp()}, {@link MyServer#reconSock()}. <br> Uses: {@link CommonScan2YOlder} <br>
     */
    public static void delTemp() {
        try {
            Files.walkFileTree(Paths.get("."), new CommonScan2YOlder());
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     <b>Тип WEB-application</b>
     */
    private static void schedStarter() throws InvocationTargetException {
        WebApplicationType webApplicationType = WebApplicationType.SERVLET;
        SPRING_APPLICATION.setWebApplicationType(webApplicationType);
        Runnable speedRun = new SpeedRunActualize();
        ScheduledExecutorService executorService =
            Executors.unconfigurableScheduledExecutorService(Executors.newSingleThreadScheduledExecutor());
        long delay = ConstantsFor.DELAY;
        executorService.scheduleWithFixedDelay(speedRun, ConstantsFor.INIT_DELAY,
            TimeUnit.MINUTES.toSeconds(delay), TimeUnit.SECONDS);
        executorService.scheduleWithFixedDelay(new SwitchesAvailability(), 1, delay, TimeUnit.SECONDS);
        if (ConstantsFor.thisPC().toLowerCase().contains("no0027") ||
            ConstantsFor.thisPC().toLowerCase().contains("rups")) {
            runCommonScan();
        }
        ScheduledExecutorService scheduledExecutorService =
            Executors.unconfigurableScheduledExecutorService(Executors.newSingleThreadScheduledExecutor());
        long initialDelay = TimeUnit.SECONDS.toHours(ConstantsFor.INIT_DELAY * 2);
        String initialDelayStr = initialDelay + " init_HOURS (for stat)";
        scheduledExecutorService.scheduleWithFixedDelay(new WeekPCStats(), initialDelay, delay, TimeUnit.HOURS);
        LOGGER.warn(initialDelayStr);
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

        Date startTime = getNextSat();

        long delay = TimeUnit.DAYS.toMillis(7);
        ScheduledFuture<?> scheduleWithFixedDelay = new ThreadConfig().threadPoolTaskScheduler().scheduleWithFixedDelay(
            r, startTime, delay);
        try {
            String msg = "Common scanner : " + startTime.toString();
            LOGGER.warn(msg);
            scheduleWithFixedDelay.get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error(e.getMessage(), e);
            Thread.currentThread().interrupt();
            r.run();
        }
    }

    /**
     Дата запуска common scanner
     <p>
     Usage: {@link #runCommonScan()} <br> Uses: - <br>

     @return new {@link Date} следующая суббота 0:01
     */
    private static Date getNextSat() {
        TimeChecker timeChecker = new TimeChecker();
        TimeInfo call = timeChecker.call();
        Calendar.Builder builder = new Calendar.Builder();
        LocalDate localDate = LocalDate.now();
        DayOfWeek satDay = DayOfWeek.SATURDAY;
        ConstantsFor.setAtomicTime(call.getReturnTime());
        if (localDate.getDayOfWeek().toString().equalsIgnoreCase(satDay.toString())) return new Date();
        else {
            int toSat = satDay.getValue() - localDate.getDayOfWeek().getValue();
            Date retDate = builder
                .setDate(
                    localDate.getYear(),
                    localDate.getMonth().getValue() - 1,
                    localDate.getDayOfMonth() + toSat)
                .setTimeOfDay(0, 1, 0).build().getTime();
            call.computeDetails();
            String msg = retDate.toString() + " " + toSat + " \nTimeChecker information: " + call.getMessage();
            LOGGER.info(msg);
            return retDate;
        }
    }
}