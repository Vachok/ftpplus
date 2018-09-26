package ru.vachok.networker;


import org.slf4j.Logger;
import org.springframework.boot.ResourceBanner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.vachok.mysqlandprops.EMailAndDB.SpeedRunActualize;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.componentsrepo.VersionInfo;
import ru.vachok.networker.config.AppCtx;
import ru.vachok.networker.config.ResLoader;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 {@link IntoApplication}
 <p>
 1. {@link #main(String[])}<br>
 1.1 {@link #infoForU(ApplicationContext)}
 */
@SpringBootApplication
@EnableScheduling
public class IntoApplication {

    /**
     {@link AppComponents#getLogger()}
     */
    private static final Logger LOGGER = AppComponents.getLogger();

    /*Fields*/
    private static final long BUILD_ST = ConstantsFor.getBuildStamp();

    private static final VersionInfo VERSION_INFO = AppComponents.versionInfo();

    /**
     {@link SpringApplication}
     */
    private static final SpringApplication SPRING_APPLICATION = new SpringApplication();

    /**
     {@link AppCtx#scanForBeansAndRefreshContext()}
     */
    private static AnnotationConfigApplicationContext appCtx = AppCtx.scanForBeansAndRefreshContext();

    private static WebApplicationType webApplicationType;

    /**
     <h1>1. Точка входа в Spring Boot Application</h1>
     {@link #infoForU(ApplicationContext)}

     @param args null
     @see ru.vachok.networker.controller.MatrixCtr
     */
    public static void main(String[] args) {
        SPRING_APPLICATION.setMainApplicationClass(IntoApplication.class);
        SPRING_APPLICATION.setApplicationContextClass(AppCtx.class);
        SPRING_APPLICATION.setResourceLoader(new ResLoader());
        SpringApplication.run(IntoApplication.class, args);
        infoForU(appCtx);
        VERSION_INFO.setTimeStamp(BUILD_ST + "");
    }

    /**
     <b>1.1 Краткая сводка</b>
     Немного инфомации о приложении.

     @param appCtx {@link ApplicationContext}
     */
    private static void infoForU(ApplicationContext appCtx) {
        String msg = appCtx.getApplicationName() + " app name" + appCtx.getDisplayName() + " app display name\n";
        LOGGER.info(msg);
        setWebType();
    }

    private static void setWebType() {
        webApplicationType = WebApplicationType.SERVLET;
        SPRING_APPLICATION.setWebApplicationType(webApplicationType);
        String name = SPRING_APPLICATION.getWebApplicationType().name();
        LOGGER.info(name);
        org.springframework.core.io.Resource resource = new ResLoader().getResource("static/images/pic03.jpg");
        ResourceBanner resourceBanner = new ResourceBanner(resource);
        SPRING_APPLICATION.setBanner(resourceBanner);
        Runnable speedRun = new SpeedRunActualize();
        ScheduledExecutorService executorService =
            Executors.unconfigurableScheduledExecutorService(Executors.newSingleThreadScheduledExecutor());
        executorService.scheduleWithFixedDelay(speedRun, ConstantsFor.INIT_DELAY, ConstantsFor.DELAY, TimeUnit.SECONDS);
    }
}