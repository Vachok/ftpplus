package ru.vachok.networker;


import org.apache.commons.net.ntp.TimeInfo;
import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
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
import ru.vachok.networker.services.MyCalen;
import ru.vachok.networker.services.WeekPCStats;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.*;


/**
 Старт
 <p>
 1. {@link #main(String[])}<br> 1.1 {@link #infoForU(ApplicationContext)}
 */
@SpringBootApplication
@EnableScheduling
public class IntoApplication {

    /*Fields*/

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
     {@link ConstantsFor#thisPC()}
     */
    private static final String THIS_PC = ConstantsFor.thisPC();

    /**
     Повторение более 3х раз в строках
     */
    private static final String STR_SEC_SPEND = " sec spend";

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
        final long stArt = System.currentTimeMillis();
        String msg = LocalDate.now().getDayOfWeek().getValue() + " - day of week\n" +
            LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault());
        LOGGER.warn(msg);
        if(THIS_PC.toLowerCase().contains("no0027") || THIS_PC.toLowerCase().contains("home")){
            SystemTrayHelper.addTray("icons8-плохие-поросята-32.png");
        }
        else{
            SystemTrayHelper.addTray(null);
        }
        SPRING_APPLICATION.setMainApplicationClass(IntoApplication.class);
        SPRING_APPLICATION.setApplicationContextClass(AppCtx.class);
        System.setProperty("file.encoding", "UTF8");
        SpringApplication.run(IntoApplication.class, args);
        infoForU(appCtx);
        String msgTimeSp = "IntoApplication.main method. " + ( float ) (System.currentTimeMillis() - stArt) / 1000 + STR_SEC_SPEND;
        LOGGER.info(msgTimeSp);
    }

    /**
     <b>1.1 Краткая сводка</b>
     Немного инфомации о приложении.

     @param appCtx {@link ApplicationContext}
     */
    private static void infoForU(ApplicationContext appCtx) {
        final long stArt = System.currentTimeMillis();
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
        String msgTimeSp = "IntoApplication.infoForU method. " + ( float ) (System.currentTimeMillis() - stArt) / 1000 +
            STR_SEC_SPEND;
        LOGGER.info(msgTimeSp);
    }

    /**
     Запуск заданий по-расписанию
     <p>
     Usages: {@link #infoForU(ApplicationContext)} <br>
     Uses: 1.1 {@link #weekStat()}, 1.2 {@link ConstantsFor#thisPC()}, 1.3 {@link ConstantsFor#thisPC()},
     1.4 {@link #runCommonScan()} .

     @throws InvocationTargetException иногда возникает. Причину не отловил
     */
    private static void schedStarter() throws InvocationTargetException {
        Runnable speedRun = null;
        try{
            speedRun = new SpeedRunActualize();
        }
        catch(ExceptionInInitializerError e){
            LOGGER.warn(e.getMessage(), e);
        }
        Runnable swAval = new SwitchesAvailability();
        ScheduledExecutorService executorService = Executors.unconfigurableScheduledExecutorService(Executors.newScheduledThreadPool(2));

        executorService.scheduleWithFixedDelay(Objects.requireNonNull(speedRun), ConstantsFor.INIT_DELAY, TimeUnit.MINUTES.toSeconds(ConstantsFor.DELAY), TimeUnit.SECONDS);
        executorService.scheduleWithFixedDelay(swAval, 1, ConstantsFor.DELAY, TimeUnit.SECONDS);

        weekStat();

        if(ConstantsFor.thisPC().toLowerCase().contains("no0027") ||
            ConstantsFor.thisPC().toLowerCase().contains("rups")){
            runCommonScan();
        }
        else{
            File file = new File("const.txt");
            try(OutputStream outputStream = new FileOutputStream(file);
                PrintWriter printWriter = new PrintWriter(outputStream, true)){
                TimeInfo timeInfo = MyCalen.getTimeInfo();
                timeInfo.computeDetails();
                printWriter.println(new Date(timeInfo.getReturnTime()));
                printWriter.println(ConstantsFor.toStringS() + "\n\n" + MyCalen.toStringS());
                if(ConstantsFor.thisPC().toLowerCase().contains("home") || ConstantsFor.thisPC().contains("10.10.111.")){
                    Path toCopy = Paths
                        .get("G:\\My_Proj\\FtpClientPlus\\modules\\networker\\src\\main\\resources\\static\\texts");
                    Files.deleteIfExists(toCopy);
                    Files.move(file.toPath(), toCopy);
                }
            }
            catch(IOException e){
                LOGGER.warn(e.getMessage());
            }
        }
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
            try{
                Files.walkFileTree(Paths.get("\\\\srv-fs.eatmeat.ru\\common_new"), new CommonRightsChecker());
            }
            catch(IOException e){
                LOGGER.warn(e.getMessage(), e);
            }
        };
        Date startTime = MyCalen.getNextMonth();
        long delay = TimeUnit.DAYS.toMillis(ConstantsFor.ONE_MONTH_DAYS);
        ScheduledFuture<?> scheduleWithFixedDelay = new ThreadConfig().threadPoolTaskScheduler().scheduleWithFixedDelay(
            r, startTime, delay);
        try{
            String msg = "Common scanner : " + startTime.toString() + "  ||  " + delay + " TimeUnit.DAYS.toMillis(ConstantsFor.ONE_MONTH_DAYS)";
            LOGGER.warn(msg);
            scheduleWithFixedDelay.get();
        }
        catch(InterruptedException | ExecutionException e){
            LOGGER.error(e.getMessage(), e);
            Thread.currentThread().interrupt();
            r.run();
        }
    }

    /**
     Удаление временных файлов.
     <p>
     Usages: {@link SystemTrayHelper#addTray(String)}, {@link ServiceInfoCtrl#closeApp()}, {@link MyServer#reconSock()}. <br> Uses: {@link CommonScan2YOlder} <br>
     */
    public static void delTemp() {
        try{
            Files.walkFileTree(Paths.get("."), new CommonScan2YOlder());
        }
        catch(IOException e){
            LOGGER.error(e.getMessage(), e);
        }
    }
}