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
import ru.vachok.networker.accesscontrol.MatrixCtr;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.config.AppCtx;
import ru.vachok.networker.config.ThreadConfig;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;


/**
 Старт
 <p>
 1. {@link #main(String[])}<br> 1.1 {@link AppInfoOnLoad#infoForU(ApplicationContext)}
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

    private static ConfigurableApplicationContext configurableApplicationContext;

    static SpringApplication getSpringApplication() {
        return SPRING_APPLICATION;
    }

    static ConfigurableApplicationContext getConfigurableApplicationContext() {
        return configurableApplicationContext;
    }

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
        configurableApplicationContext = SpringApplication.run(IntoApplication.class, args);
        ConfigurableApplicationContext run = configurableApplicationContext;
        run.start();

        if(args.length > 0 && Arrays.toString(args).contains("off")){
            new ThreadConfig().killAll();
        }
        else{
            String msg = afterSt() + " " + run.toString();
            LOGGER.warn(msg);
        }
        String msgTimeSp = new StringBuilder()
            .append("IntoApplication.main method. ")
            .append(( float ) (System.currentTimeMillis() - stArt) / 1000)
            .append(" sec spend").toString();
        LOGGER.info(msgTimeSp);
    }

    /**
     Запуск до старта Spring boot app
     */
    private static void beforeSt() {
        ConstantsFor.takePr();
        ConstantsFor.showMem();
        LOGGER.info("IntoApplication.beforeSt");
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
        System.setProperty("CONST_TXT.encoding", "UTF8");
    }

    /**
     Запуск после старта Spring boot app
     */
    private static boolean afterSt() {

        ThreadConfig threadConfig = new ThreadConfig();
        Runnable infoAndSched = new AppInfoOnLoad();
        ConstantsFor.showMem();
        try{
            String s = Paths.get("").toFile().getCanonicalPath().toLowerCase();
            String showPath = Paths.get(".").toString() + "\n abs: " +
                Paths.get(".").toFile().getAbsolutePath();
            new Thread(() -> {

                InitProperties initProperties = new FileProps(s + "\\modules\\networker\\src\\main\\resources\\application");
                Properties props = initProperties.getProps();
                initProperties = new DBRegProperties(ConstantsFor.APP_NAME + "application");
                initProperties.setProps(props);
            }).start();
            LOGGER.error(showPath);
            threadConfig.threadPoolTaskExecutor().execute(infoAndSched);
            return true;
        }
        catch(IOException e){
            LOGGER.warn(e.getMessage(), e);
            return false;
        }
    }
}