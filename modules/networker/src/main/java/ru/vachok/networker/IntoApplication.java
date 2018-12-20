package ru.vachok.networker;


import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.vachok.networker.accesscontrol.MatrixCtr;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.config.AppCtx;
import ru.vachok.networker.config.ThreadConfig;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;


/**
 Старт
 <p>
 1. {@link #main(String[])}<br> 1.1 {@link AppInfoOnLoad#infoForU(ApplicationContext)}
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
     {@link ConstantsFor#thisPC()}
     */
    private static final String THIS_PC = ConstantsFor.thisPC();

    /**
     <h1>1. Точка входа в Spring Boot Application</h1>
     <p>
     {@link AppInfoOnLoad#infoForU(ApplicationContext)}

     @param args null
     @see MatrixCtr
     */
    public static void main(String[] args) {
        final long stArt = System.currentTimeMillis();
        beforeSt();
        SpringApplication.run(IntoApplication.class, args);
        afterSt();

        String msgTimeSp = new StringBuilder()
            .append("IntoApplication.main method. ")
            .append((float) (System.currentTimeMillis() - stArt) / 1000)
            .append(" sec spend").toString();
        LOGGER.info(msgTimeSp);
    }

    /**
     Запуск до старта Spring boot app
     */
    private static void beforeSt() {
        ConstantsFor.showMem();
        LOGGER.info("IntoApplication.beforeSt");
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
        System.setProperty("CONST_TXT.encoding", "UTF8");
    }

    /**
     Запуск после старта Spring boot app
     */
    private static void afterSt() {
        LOGGER.info("IntoApplication.afterSt");
        ThreadConfig threadConfig = new ThreadConfig();
        Runnable infoAndSched = new AppInfoOnLoad();
        threadConfig.threadPoolTaskExecutor().execute(infoAndSched);
        ConstantsFor.showMem();
        try {
            String showPath = Paths.get(".").toString() + "\n abs: " +
                Paths.get(".").toFile().getAbsolutePath() + "\n canonical: " +
                Paths.get(".").toFile().getCanonicalPath();
            LOGGER.warn(showPath);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

}