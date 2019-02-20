package ru.vachok.networker;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.FileProps;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.config.AppCtx;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.WeekPCStats;
import ru.vachok.networker.services.MessageLocal;
import ru.vachok.networker.systray.SystemTrayHelper;

import java.io.File;
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
     {@link LoggerFactory#getLogger(java.lang.String)}.
     <p>
     {@link String} = {@link Class#getSimpleName()}
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(IntoApplication.class.getSimpleName());

    /**
     new {@link SpringApplication}
     */
    private static final @NotNull SpringApplication SPRING_APPLICATION = new SpringApplication();

    /**
     {@link AppComponents#getProps(boolean)}
     */
    private static final Properties LOCAL_PROPS = AppComponents.getProps();

    /**
     {@link MessageLocal}
     */
    private static final @NotNull MessageToUser messageToUser = new MessageLocal();

    /**
     {@link ConfigurableApplicationContext} = null.
     */
    private static @NotNull ConfigurableApplicationContext configurableApplicationContext;

    /**
     @return {@link #configurableApplicationContext}
     */
    public static @NotNull ConfigurableApplicationContext getConfigurableApplicationContext() {
        return configurableApplicationContext;
    }

    static {
        configurableApplicationContext = SpringApplication.run(IntoApplication.class);
    }

    /**
     Точка входа в Spring Boot Application
     <p>
     {@link FileSystemWorker#delFilePatterns(java.lang.String[])}. Удаление останков от предидущего запуска. <br>
     {@link IntoApplication#beforeSt()} <br>
     {@link SpringApplication#run(java.lang.Class, java.lang.String...)}. Инициализация {@link #configurableApplicationContext}. <br>
     {@link Logger#warn(java.lang.String)} - new {@link String} {@code msg} = {@link IntoApplication#afterSt()} <br>
     Если есть аргументы - {@link #readArgs(String[])} <br>
     {@link Logger#info(java.lang.String)} - время работы метода.

     @param args аргументы запуска
     @see SystemTrayHelper
     */
    public static void main(@Nullable String[] args) {
        final long stArt = System.currentTimeMillis();
        FileSystemWorker.delFilePatterns(ConstantsFor.STRS_VISIT);

        beforeSt();

        configurableApplicationContext = SpringApplication.run(IntoApplication.class, args);
        configurableApplicationContext.start();

        @NotNull String msg = afterSt() + " afterSt";
        LOGGER.warn(msg);

        if (args != null && args.length > 0) {
            readArgs(args);
        }

        @NotNull StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("IntoApplication.main method. ");
        stringBuilder.append((float) (System.currentTimeMillis() - stArt) / 1000);
        stringBuilder.append(" ");
        stringBuilder.append(ConstantsFor.STR_SEC_SPEND);
        messageToUser.info("IntoApplication.main", "stringBuilder.toString()", stringBuilder.toString());
    }

    /**
     Чтение аргументов {@link #main(String[])}
     <p>
     {@code for} {@link String}:
     {@link ConstantsFor#PR_TOTPC} - {@link Properties#setProperty(java.lang.String, java.lang.String)}.
     Property: {@link ConstantsFor#PR_TOTPC}, value: {@link String#replaceAll(String, String)} ({@link ConstantsFor#PR_TOTPC}, "") <br>
     {@code off}: {@link ThreadConfig#killAll()}

     @param args аргументы запуска.
     */
    private static void readArgs(@NotNull String[] args) {
        for (@NotNull String arg : args) {
            messageToUser.info("IntoApplication.readArgs", "arg", arg);
            if (arg.contains(ConstantsFor.PR_TOTPC)) {
                LOCAL_PROPS.setProperty(ConstantsFor.PR_TOTPC, arg.replaceAll(ConstantsFor.PR_TOTPC, ""));
            }
            if (arg.equalsIgnoreCase("off")) {
                AppComponents.threadConfig().killAll();
            }
        }
    }

    /**
     Запуск до старта Spring boot app <br> Usages: {@link #main(String[])}
     <p>
     {@link Logger#warn(java.lang.String)} - день недели. <br>
     Если {@link ConstantsFor#thisPC()} - {@link ConstantsFor#NO0027} или "home",
     {@link SystemTrayHelper#addTray(java.lang.String)} "icons8-плохие-поросята-32.png".
     Else - {@link SystemTrayHelper#addTray(java.lang.String)} {@link String} null<br>
     {@link SpringApplication#setMainApplicationClass(java.lang.Class)}
     */
    private static void beforeSt() {
        @NotNull StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(LocalDate.now().getDayOfWeek().getValue());
        stringBuilder.append(" - day of week\n");
        stringBuilder.append(LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault()));
        messageToUser.info("IntoApplication.beforeSt", "stringBuilder", stringBuilder.toString());
        if (ConstantsFor.thisPC().toLowerCase().contains(ConstantsFor.NO0027) || ConstantsFor.thisPC().toLowerCase().contains("home")) {
            SystemTrayHelper systemTrayHelper = SystemTrayHelper.getInstance("icons8-плохие-поросята-32.png");
            systemTrayHelper.addTray();
        } else {
            SystemTrayHelper.getInstance(ConstantsFor.ICON_FILE_NAME).addTray();
        }
        SPRING_APPLICATION.setMainApplicationClass(IntoApplication.class);
        SPRING_APPLICATION.setApplicationContextClass(AppCtx.class);
        System.setProperty("encoding", "UTF8");
        FileSystemWorker.recFile("system", new TForms().fromArray(System.getProperties()));
    }

    /**
     Запуск после старта Spring boot app <br> Usages: {@link #main(String[])}
     <p>
     1. {@link AppComponents#threadConfig()}. Управление запуском и трэдами. <br><br>
     <b>Runnable</b><br>
     2. {@link AppInfoOnLoad#getWeekPCStats()} собирает инфо о скорости в файл. Если воскресенье, запускает {@link WeekPCStats} <br><br>
     <b>Далее:</b><br>
     3. {@link AppComponents#threadConfig()} (4. {@link ThreadConfig#threadPoolTaskExecutor()}) - запуск <b>Runnable</b> <br>
     5. {@link ThreadConfig#threadPoolTaskExecutor()} - запуск {@link AppInfoOnLoad}. <br><br>
     <b>{@link Exception}:</b><br>
     6. {@link TForms#fromArray(java.lang.Exception, boolean)} - искл. в строку. 7. {@link FileSystemWorker#recFile(java.lang.String, java.util.List)} и
     запишем в файл.

     @return запускилось = {@code true}.
     */
    private static boolean afterSt() {
        ThreadConfig threadConfig = AppComponents.threadConfig();
        @NotNull AppInfoOnLoad infoAndSched = new AppInfoOnLoad();
        Runnable r = IntoApplication::appProperties;
        try {
            AppInfoOnLoad.getWeekPCStats();
            @NotNull String showPath = Paths.get(".").toString() + "\n abs: " + Paths.get(".").toFile().getAbsolutePath();
            AppComponents.threadConfig().threadPoolTaskExecutor().execute(r);
            LOGGER.warn(showPath);
            threadConfig.threadPoolTaskExecutor().execute(infoAndSched);
            return true;
        } catch (Exception e) {
            FileSystemWorker.recFile(IntoApplication.class.getSimpleName() + ConstantsFor.LOG, Collections.singletonList(new TForms().fromArray(e, false)));
            return false;
        }
    }

    /**
     application.LOCAL_PROPS
     <p>
     new {@link FileProps} ({@link File#getCanonicalPath()} - ""+{@code "\\modules\\networker\\src\\main\\resources\\application"}) <br>
     {@link InitProperties#getProps()}. Получаем {@code props} <br>
     Сэтаем в файл:<br>
     {@code "build.version"} = {@link AppComponents#getProps()} {@link ConstantsFor#PR_APP_VERSION} и {@link ConstantsFor#PR_QSIZE} = {@link ConstantsFor#IPS_IN_VELKOM_VLAN} <br>
     {@link InitProperties#setProps(java.util.Properties)} запись {@code props} в <b>application.LOCAL_PROPS</b>
     <p>
     new {@link DBRegProperties} - {@link ConstantsFor#APP_NAME} + {@code "application"} <br>
     {@link InitProperties#delProps()}
     {@link InitProperties#setProps(java.util.Properties)} запись в БД.
     <p>
     {@link AppComponents#getProps()} putAll - {@code props}
     */
    private static void appProperties() {
        @Nullable String rootPathStr = ".";
        try {
            rootPathStr = Paths.get("").toFile().getCanonicalPath().toLowerCase();
        } catch (IOException e) {
            FileSystemWorker.recFile(IntoApplication.class.getSimpleName() + ConstantsFor.LOG, e.getMessage() + "\n" + new TForms().fromArray(e, false));
            LOGGER.warn(e.getMessage());
        }
        @NotNull InitProperties initProperties = new FileProps(rootPathStr + "\\modules\\networker\\src\\main\\resources\\application");
        Properties props = initProperties.getProps();
        props.setProperty("build.version", LOCAL_PROPS.getProperty(ConstantsFor.PR_APP_VERSION));
        props.setProperty(ConstantsFor.PR_QSIZE, ConstantsFor.IPS_IN_VELKOM_VLAN + "");
        initProperties.setProps(props);
        initProperties = new DBRegProperties(ConstantsFor.APP_NAME + "application");
        initProperties.delProps();
        initProperties.setProps(props);
        LOCAL_PROPS.putAll(props);
    }
}