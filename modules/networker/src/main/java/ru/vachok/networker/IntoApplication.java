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

import java.awt.*;
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
 1. {@link #main(String[])}<br>
 @see AppInfoOnLoad
 @since 02.05.2018 (10:36)
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
     {@link ConfigurableApplicationContext}
     Usages: {@link #main(String[])},
     */
    private static ConfigurableApplicationContext configurableApplicationContext = null;

    /**
     Usages: {@link SystemTrayHelper#addItems(PopupMenu)}

     @return {@link #SPRING_APPLICATION}
     */
    static SpringApplication getSpringApplication() {
        return SPRING_APPLICATION;
    }

    /**
     Usages: {@link ExitApp#exitAppDO()}, {@link SystemTrayHelper#addItems(PopupMenu)}

     @return {@link #configurableApplicationContext}
     */
    static ConfigurableApplicationContext getConfigurableApplicationContext() {
        return configurableApplicationContext;
    }

    /**
     <h1>1. Точка входа в Spring Boot Application</h1>
     <p>

     @param args null
     @see SystemTrayHelper#addItems(PopupMenu)
     {@link AppInfoOnLoad#infoForU(ApplicationContext)}
     */
    @SuppressWarnings ("JavadocReference")
    public static void main(String[] args) {
        final long stArt = System.currentTimeMillis();
        beforeSt();
        configurableApplicationContext = SpringApplication.run(IntoApplication.class, args);
        configurableApplicationContext.start();
        if(args.length > 0 && Arrays.toString(args).contains("off")){
            new ThreadConfig().killAll();
        }
        else{
            String msg = new StringBuilder()
                .append(afterSt())
                .append("\n")
                .append(new TForms().fromArray(configurableApplicationContext.getBeanDefinitionNames(), false)).toString();
            LOGGER.warn(msg);
        }
        String msgTimeSp = new StringBuilder()
            .append("IntoApplication.main method. ")
            .append(( float ) (System.currentTimeMillis() - stArt) / 1000)
            .append(" ")
            .append(ConstantsFor.STR_SEC_SPEND).toString();
        LOGGER.info(msgTimeSp);
    }

    /**
     Запуск до старта Spring boot app <br>
     Usages: {@link #main(String[])}
     */
    private static void beforeSt() {
        new ThreadConfig().threadPoolTaskExecutor().execute(ConstantsFor::takePr);
        ConstantsFor.showMem();
        LOGGER.info("IntoApplication.beforeSt");
        String msg = LocalDate.now().getDayOfWeek().getValue() + " - day of week\n" +
            LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault());
        LOGGER.warn(msg);
        if(THIS_PC.toLowerCase().contains(ConstantsFor.NO0027) || THIS_PC.toLowerCase().contains("home")){
            SystemTrayHelper.addTray("icons8-плохие-поросята-32.png");
        }
        else{
            SystemTrayHelper.addTray(null);
        }
        SPRING_APPLICATION.setMainApplicationClass(IntoApplication.class);
        SPRING_APPLICATION.setApplicationContextClass(AppCtx.class);
        System.setProperty("encoding", "UTF8");
    }

    /**
     Запуск после старта Spring boot app <br>
     Usages: {@link #main(String[])}
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
                props.setProperty("build.version", ConstantsFor.getProps().getProperty("appVersion"));
                props.setProperty("qsize", ConstantsFor.IPS_IN_VELKOM_VLAN + "");
                initProperties.setProps(props);
                initProperties = new DBRegProperties(ConstantsFor.APP_NAME + "application");
                initProperties.delProps();
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