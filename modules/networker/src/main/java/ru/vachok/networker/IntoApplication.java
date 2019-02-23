package ru.vachok.networker;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import ru.vachok.messenger.MessageSwing;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.FileProps;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.config.AppCtx;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.MyServer;
import ru.vachok.networker.net.WeekPCStats;
import ru.vachok.networker.services.MessageLocal;
import ru.vachok.networker.systray.SystemTrayHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.TextStyle;
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
     new {@link SpringApplication}
     */
    private static final @NotNull SpringApplication SPRING_APPLICATION = new SpringApplication();

    /**
     {@link AppComponents#getProps(boolean)}
     */
    private static final Properties LOCAL_PROPS = AppComponents.getProps();

    public static Runnable infoMsgRunnable = () -> {
        final ThreadPoolTaskExecutor taskExecutor = AppComponents.threadConfig().getTaskExecutor();
        final ThreadPoolTaskScheduler taskScheduler = AppComponents.threadConfig().getTaskScheduler();
        new MessageSwing(550, 270, 37, 26).infoTimer(( int ) ConstantsFor.DELAY, "\n\n\n" + taskExecutor.getThreadPoolExecutor().toString() +
            "\n\n" + taskScheduler.getScheduledThreadPoolExecutor().toString());
    };

    public static Runnable getInfoMsgRunnable() {
        return infoMsgRunnable;
    }

    /**
     {@link ConfigurableApplicationContext} = null.
     */
    private static @NotNull ConfigurableApplicationContext configurableApplicationContext;

    /**
     {@link MessageLocal}
     */
    private static @NotNull MessageToUser messageToUser = new MessageLocal();

    private static ThreadPoolTaskExecutor executor = AppComponents.threadConfig().getTaskExecutor();

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
        FileSystemWorker.delFilePatterns(ConstantsFor.STRS_VISIT);
        beforeSt();
        if (args != null && args.length > 0) {
            readArgs(args);
        }
        configurableApplicationContext.start();
        afterSt();
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
        ExitApp exitApp = new ExitApp(IntoApplication.class.getSimpleName());
        for (@NotNull String arg : args) {
            messageToUser.info("IntoApplication.readArgs", "arg", arg);
            if (arg.contains(ConstantsFor.PR_TOTPC)) {
                LOCAL_PROPS.setProperty(ConstantsFor.PR_TOTPC, arg.replaceAll(ConstantsFor.PR_TOTPC, ""));
            }
            if (arg.equalsIgnoreCase("off")) {
                AppComponents.threadConfig().executeAsThread(exitApp);
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
        if(ConstantsFor.thisPC().toLowerCase().contains(ConstantsFor.NO0027)){
            SystemTrayHelper.addTray("icons8-плохие-поросята-32.png");
        }
        else
            if(ConstantsFor.thisPC().toLowerCase().contains("home")){
                SystemTrayHelper.addTray("icons8-house-26.png");
            }
            else{
            SystemTrayHelper.addTray(ConstantsFor.ICON_FILE_NAME);
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
     3. {@link AppComponents#threadConfig()} (4. {@link ThreadConfig#getTaskExecutor()}) - запуск <b>Runnable</b> <br>
     5. {@link ThreadConfig#getTaskExecutor()} - запуск {@link AppInfoOnLoad}. <br><br>
     <b>{@link Exception}:</b><br>
     6. {@link TForms#fromArray(java.lang.Exception, boolean)} - искл. в строку. 7. {@link FileSystemWorker#recFile(java.lang.String, java.util.List)} и
     запишем в файл.
     */
    private static void afterSt() {
        @NotNull Runnable infoAndSched = new AppInfoOnLoad();
        Runnable mySrv = MyServer.getI();

        executor.submit(() -> {
            try {
                appProperties();
            } catch (IOException e) {
                messageToUser.errorAlert("IntoApplication", "afterSt", e.getMessage());
                FileSystemWorker.error("IntoApplication.afterSt", e);
            }
        });
        executor.submit(infoAndSched);
        executor.submit(mySrv);

        new Thread(infoMsgRunnable).start();
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
    private static void appProperties() throws IOException {
        @Nullable String rootPathStr = Paths.get("").toFile().getCanonicalPath().toLowerCase();
        @NotNull InitProperties initProperties = new FileProps(rootPathStr + "\\modules\\networker\\src\\main\\resources\\application");
        Properties props = initProperties.getProps();
        
        props.setProperty("build.version", LOCAL_PROPS.getProperty(ConstantsFor.PR_APP_VERSION));
        props.setProperty(ConstantsFor.PR_QSIZE, ConstantsFor.IPS_IN_VELKOM_VLAN + "");

        initProperties.setProps(props);
        initProperties = new DBRegProperties(ConstantsFor.APP_NAME + "application");
        initProperties.delProps();
        initProperties.setProps(props);

        LOCAL_PROPS.putAll(props);
        messageToUser.info("IntoApplication.appProperties", "new TForms().fromArray(LOCAL_PROPS, false)", new TForms().fromArray(LOCAL_PROPS, false));
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("IntoApplication{");
        sb.append("SPRING_APPLICATION=").append(SPRING_APPLICATION.toString());
        sb.append(", LOCAL_PROPS=").append(LOCAL_PROPS.size());
        sb.append(", messageToUser=").append(messageToUser.toString());
        sb.append(", configurableApplicationContext=").append(configurableApplicationContext.getApplicationName());
        sb.append('}');
        return sb.toString();
    }
}