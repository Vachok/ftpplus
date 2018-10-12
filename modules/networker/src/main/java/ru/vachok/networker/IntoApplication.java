package ru.vachok.networker;


import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.vachok.mysqlandprops.EMailAndDB.SpeedRunActualize;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.config.AppCtx;
import ru.vachok.networker.logic.SystemTrayHelper;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


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
     {@link AppCtx#scanForBeansAndRefreshContext()}
     */
    private static AnnotationConfigApplicationContext appCtx = AppCtx.scanForBeansAndRefreshContext();

    /**
     <h1>1. Точка входа в Spring Boot Application</h1>
     {@link #infoForU(ApplicationContext)}

     @param args null
     @see ru.vachok.networker.controller.MatrixCtr
     */
    public static void main(String[] args) {
        SystemTrayHelper.addTray("icons8-плохие-поросята-32.png");
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
        String msg = appCtx.getApplicationName() + " app name" + appCtx.getDisplayName() + " app display name\n" +
            ConstantsFor.getBuildStamp();
        LOGGER.info(msg);
        setWebType();
    }

    /**
     <b>Тип WEB-application</b>
     */
    private static void setWebType() {
        WebApplicationType webApplicationType = WebApplicationType.SERVLET;
        SPRING_APPLICATION.setWebApplicationType(webApplicationType);
        Runnable speedRun = new SpeedRunActualize();
        ScheduledExecutorService executorService =
            Executors.unconfigurableScheduledExecutorService(Executors.newSingleThreadScheduledExecutor());
        executorService.scheduleWithFixedDelay(speedRun, ConstantsFor.INIT_DELAY, ConstantsFor.DELAY, TimeUnit.SECONDS);
        String msg = "Initial Delay checker = " + ConstantsFor.INIT_DELAY + "\nDelay = " + ConstantsFor.DELAY;
        LOGGER.warn(msg);
    }
}