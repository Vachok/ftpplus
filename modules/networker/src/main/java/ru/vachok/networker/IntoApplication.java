package ru.vachok.networker;


import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.FileProps;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.config.AppCtx;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.systray.SystemTrayHelper;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Collections;
import java.util.Locale;
import java.util.Properties;


/**
 Старт
 <p>
 1. {@link #main(String[])}<br>

 @see AppInfoOnLoad
 @since 02.05.2018 (10:36) */
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
     {@link ConfigurableApplicationContext} Usages: {@link #main(String[])},
     */
    private static ConfigurableApplicationContext configurableApplicationContext = null;

    /**
     Usages: {@link ExitApp#exitAppDO()}, {@link SystemTrayHelper#addItems(PopupMenu)}

     @return {@link #configurableApplicationContext}
     */
    public static ConfigurableApplicationContext getConfigurableApplicationContext() {
        return configurableApplicationContext;
    }

    /**
     <h1>1. Точка входа в Spring Boot Application</h1>
     <p>

     @param args null
     @see SystemTrayHelper#addItems(PopupMenu) {@link AppInfoOnLoad#infoForU(ApplicationContext)}
     */
    @SuppressWarnings("JavadocReference")
    public static void main(String[] args) {
        LOGGER.warn("IntoApplication.main");
        final long stArt = System.currentTimeMillis();
        boolean delFilePatterns = FileSystemWorker.delFilePatterns(ConstantsFor.STR_VISIT);
        beforeSt();
        configurableApplicationContext = SpringApplication.run(IntoApplication.class, args);
        configurableApplicationContext.start();
        String msg = new StringBuilder()
            .append(afterSt())
            .append(" delFilePatterns ").append(delFilePatterns).toString();
        LOGGER.warn(msg);

        String msgTimeSp = new StringBuilder()
            .append("IntoApplication.main method. ")
            .append((float) (System.currentTimeMillis() - stArt) / 1000)
            .append(" ")
            .append(ConstantsFor.STR_SEC_SPEND).toString();
        LOGGER.info(msgTimeSp);
        if (args.length > 0) {
            for (String s : args) {
                LOGGER.info(s);
                if (s.contains(ConstantsFor.PR_TOTPC)) {
                    ConstantsFor.getProps().setProperty(ConstantsFor.PR_TOTPC, s.replaceAll(ConstantsFor.PR_TOTPC, ""));
                }
                if (s.equalsIgnoreCase("off")) {
                    new ThreadConfig().killAll();
                }
            }
        }
    }

    /**
     Запуск до старта Spring boot app <br> Usages: {@link #main(String[])}
     */
    private static void beforeSt() {
        ConstantsFor.showMem();
        LOGGER.warn("IntoApplication.beforeSt");
        String msg = LocalDate.now().getDayOfWeek().getValue() + " - day of week\n" +
            LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault());
        LOGGER.warn(msg);
        if (ConstantsFor.thisPC().toLowerCase().contains(ConstantsFor.NO0027) || ConstantsFor.thisPC().toLowerCase().contains("home")) {
            SystemTrayHelper.addTray("icons8-плохие-поросята-32.png");
        } else {
            SystemTrayHelper.addTray(null);
        }
        SPRING_APPLICATION.setMainApplicationClass(IntoApplication.class);
        SPRING_APPLICATION.setApplicationContextClass(AppCtx.class);
        System.setProperty("encoding", "UTF8");
    }

    /**
     Запуск после старта Spring boot app <br> Usages: {@link #main(String[])}
     */
    @SuppressWarnings("MethodWithMultipleReturnPoints")
    private static boolean afterSt() {
        ThreadConfig threadConfig = new ThreadConfig();
        AppInfoOnLoad infoAndSched = new AppInfoOnLoad();
        Runnable r = IntoApplication::appProperties;
        try {
            infoAndSched.spToFile();
            String showPath = Paths.get(".").toString() + "\n abs: " +
                Paths.get(".").toFile().getAbsolutePath();
            new ThreadConfig().threadPoolTaskExecutor().execute(r);
            LOGGER.error(showPath);
            threadConfig.threadPoolTaskExecutor().execute(infoAndSched);
            return true;
        }
        catch(Exception e){
            FileSystemWorker.recFile(IntoApplication.class.getSimpleName() + ConstantsFor.LOG, Collections.singletonList(new TForms().fromArray(e, false)));
            return false;
        }
    }

    private static void appProperties() {
        LOGGER.warn("IntoApplication.appProperties");
        String s = null;
        try {
            s = Paths.get("").toFile().getCanonicalPath().toLowerCase();
        } catch (IOException e) {
            FileSystemWorker.recFile(IntoApplication.class.getSimpleName() + ConstantsFor.LOG, e.getMessage() + "\n" + new TForms().fromArray(e, false));
            LOGGER.warn(e.getMessage());
        }
        InitProperties initProperties = new FileProps(s + "\\modules\\networker\\src\\main\\resources\\application");
        Properties props = initProperties.getProps();
        props.setProperty("build.version", ConstantsFor.getProps().getProperty(ConstantsFor.PR_APP_VERSION));
        props.setProperty(ConstantsFor.PR_QSIZE, ConstantsFor.IPS_IN_VELKOM_VLAN + "");
        initProperties.setProps(props);
        initProperties = new DBRegProperties(ConstantsFor.APP_NAME + "application");
        initProperties.delProps();
        initProperties.setProps(props);
    }
}