// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import com.eclipsesource.json.JsonObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.vachok.networker.componentsrepo.NameOrIPChecker;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.componentsrepo.services.MyCalen;
import ru.vachok.networker.componentsrepo.services.RegRuFTPLibsUploader;
import ru.vachok.networker.componentsrepo.systray.SystemTrayHelper;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.events.MyEvent;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.restapi.props.InitProperties;
import ru.vachok.networker.sysinfo.AppConfigurationLocal;

import java.awt.*;
import java.io.File;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.*;


/**
 @see ru.vachok.networker.IntoApplicationTest */
@SuppressWarnings("AccessStaticViaInstance")
@SpringBootApplication
@EnableScheduling
@EnableAutoConfiguration
public class IntoApplication {


    /**
     {@link MessageLocal}
     */
    private static final MessageToUser MESSAGE_LOCAL = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, IntoApplication.class.getSimpleName());

    private static final boolean IS_TRAY_SUPPORTED = SystemTray.isSupported();

    private static final SpringApplication SPRING_APPLICATION = new SpringApplication(IntoApplication.class);

    private static final ConfigurableApplicationContext configurableApplicationContext = SPRING_APPLICATION.run(IntoApplication.class);

    public static String getAppIDFromContext() {
        return configurableApplicationContext.getId();
    }

    @Contract(pure = true)
    protected static ConfigurableApplicationContext getContext() {
        MESSAGE_LOCAL.info(IntoApplication.class.getSimpleName(), "getContext()", String.valueOf(configurableApplicationContext.hashCode()));
        return configurableApplicationContext;
    }

    public static ConfigurableListableBeanFactory getBeansFactory() {
        return configurableApplicationContext.getBeanFactory();
    }

    public static void main(@NotNull String[] args) {
        Thread.currentThread().setName(IntoApplication.class.getSimpleName());
        File fileLogJson = new File(FileNames.APP_JSON);
        configurableApplicationContext.setId(getAppID());
        if (new File(FileNames.APP_JSON).exists() && fileLogJson.length() > ConstantsFor.MBYTE) {
            fileLogJson.delete();
        }
        setUTF8Enc();
        JsonObject appStart = new JsonObject();
        appStart.add(PropertiesNames.TIMESTAMP, ConstantsFor.START_STAMP);
        appStart.add("hdate", new Date(ConstantsFor.START_STAMP).toString());
        appStart.add("configurableApplicationContext", configurableApplicationContext.getId());
        appStart.add("scheduleTrunkPcUserAuto", UsefulUtilities.scheduleTrunkPcUserAuto());
        FileSystemWorker.appendObjectToFile(new File(FileNames.APP_START), appStart);

        if (!Arrays.toString(args).contains("test")) {
            UsefulUtilities.startTelnet();
        }
        if (args.length > 0) {
            new ArgsReader(args).run();
        }
        else {
            checkTray();
        }
    }

    private static String getAppID() {
        if (UsefulUtilities.thisPC().toLowerCase().contains("home")) {
            return setID();
        }
        else {
            return InitProperties.getInstance(InitProperties.DB_MEMTABLE).getProps().getProperty(PropertiesNames.ID, "No ver");
        }
    }

    public static ConfigurableEnvironment getCTXEnvironment() {
        makeEvent("getCTXEnvironment");
        configurableApplicationContext.addApplicationListener(IntoApplication::onMyApplicationEvent);
        return configurableApplicationContext.getEnvironment();
    }

    public static Map<String, String> getAppArgs() {
        return ArgsReader.getAppArgs();
    }

    static void makeEvent(Object event) {
        ApplicationEvent myEvent = new MyEvent(event);
        File sshLog = new File(FileNames.SSH_LOG);
        if (sshLog.exists() & sshLog.length() > ConstantsFor.MBYTE) {
            sshLog.delete();
        }
        if (myEvent.getSource().toString().contains("SSH : ")) {
            FileSystemWorker.appendObjectToFile(sshLog, myEvent.getSource());
        }
    }

    protected static void setUTF8Enc() {
        @NotNull StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n\nSystem time: ").append(new Date(System.currentTimeMillis())).append(" atom time: ")
            .append(new Date(UsefulUtilities.getAtomicTime())).append("\n\n");
        stringBuilder.append(LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault())).append("\n\n");
        System.setProperty(PropertiesNames.ENCODING, "UTF8");
        stringBuilder.append(AbstractForms.fromArray(System.getProperties()));
        stringBuilder.append("http://").append(new NameOrIPChecker(UsefulUtilities.thisPC()).resolveInetAddress().getHostAddress()).append(":8880/");
        MessageToUser.getInstance(MessageToUser.EMAIL, IntoApplication.class.getSimpleName())
            .info(UsefulUtilities.thisPC(), "appInfoStarter", stringBuilder.toString());
    }

    static void checkTray() {
        Optional optionalTray = SystemTrayHelper.getI();
        try {
            if (IS_TRAY_SUPPORTED && optionalTray.isPresent()) {
                ((SystemTrayHelper) optionalTray.get()).trayAdd();
            }
        }
        catch (HeadlessException e) {
            MESSAGE_LOCAL.error(MessageFormat
                .format("IntoApplication.checkTray {0} - {1}\nStack:\n{2}", e.getClass().getTypeName(), e.getMessage(), AbstractForms.fromArray(e)));
        }
        finally {
            appInfoStarter();
        }
    }

    private static String setID() {
        Properties appPr = InitProperties.getTheProps();
        String appIdNew = MessageFormat.format("{0}.{1}-{2}", MyCalen.getWeekNumber(), LocalDate.now().getDayOfWeek().getValue(), (int) (LocalTime.now()
            .toSecondOfDay() / ConstantsFor.ONE_HOUR_IN_MIN));
        appPr.setProperty(PropertiesNames.ID, appIdNew);
        Properties valueWithJSON = getJSONProp(appIdNew);
        boolean isMem = false;
        try {
            isMem = InitProperties.getInstance(InitProperties.DB_MEMTABLE).setProps(appPr);
        }
        catch (Exception e) {
            MESSAGE_LOCAL.error("IntoApplication.setID", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
            AppConfigurationLocal.getInstance().execute(new RegRuFTPLibsUploader());
            MessageToUser.getInstance(MessageToUser.TRAY, IntoApplication.class.getSimpleName())
                .info(IntoApplication.class.getSimpleName(), "exec", "RegRuFTPLibsUploader");
        }
        InitProperties.setPreference(PropertiesNames.APPVERSION, appIdNew);
        MessageToUser.getInstance(MessageToUser.TRAY, IntoApplication.class.getSimpleName())
            .warn(IntoApplication.class.getSimpleName(), String.valueOf(isMem), valueWithJSON.getProperty(PropertiesNames.APPVERSION));
        return appIdNew;
    }

    static void appInfoStarter() {
        @NotNull Runnable infoAndSched = AppInfoOnLoad.getI();
        AppComponents.threadConfig().getTaskExecutor().execute(infoAndSched, 50);
    }

    private static Properties getJSONProp(String appIdNew) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add(PropertiesNames.APPVERSION, appIdNew);
        Properties properties = new Properties();
        properties.setProperty(PropertiesNames.APPVERSION, jsonObject.toString());
        return properties;
    }

    @Override
    public String toString() {
        return new StringJoiner(",\n", IntoApplication.class.getSimpleName() + "[\n", "\n]")
            .add(new AppComponents().getFirebaseApp().getName())
            .toString();
    }

    private static void onMyApplicationEvent(ApplicationEvent event) {
        File file = new File(event.getSource().getClass().getSimpleName() + ".stamp");
        if (file.exists() && file.length() > ConstantsFor.MBYTE) {
            file.delete();
        }
        FileSystemWorker.appendObjectToFile(file, new Date(event.getTimestamp()));
    }
}