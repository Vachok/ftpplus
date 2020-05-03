// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import com.eclipsesource.json.JsonObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.vachok.networker.componentsrepo.NameOrIPChecker;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.componentsrepo.systray.SystemTrayHelper;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.awt.*;
import java.io.File;
import java.text.MessageFormat;
import java.time.LocalDate;
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

    @Contract(pure = true)
    static ConfigurableApplicationContext getContext() {
        MESSAGE_LOCAL.info(IntoApplication.class.getSimpleName(), "getContext()", String.valueOf(configurableApplicationContext.hashCode()));
        return configurableApplicationContext;
    }

    public static ConfigurableListableBeanFactory getBeansFactory() {
        return configurableApplicationContext.getBeanFactory();
    }

    public static void main(@NotNull String[] args) {
        Thread.currentThread().setName(IntoApplication.class.getSimpleName());
        JsonObject appStart = new JsonObject();
        appStart.add(PropertiesNames.TIMESTAMP, ConstantsFor.START_STAMP);
        appStart.add("hdate", new Date(ConstantsFor.START_STAMP).toString());
        appStart.add("configurableApplicationContext", configurableApplicationContext.hashCode());
        appStart.add("scheduleTrunkPcUserAuto", UsefulUtilities.scheduleTrunkPcUserAuto());
        FileSystemWorker.appendObjectToFile(new File(FileNames.APP_JSON), appStart);
        setUTF8Enc();
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

    @Override
    public String toString() {
        return new StringJoiner(",\n", IntoApplication.class.getSimpleName() + "[\n", "\n]")
                .add(new AppComponents().getFirebaseApp().getName())
            .toString();
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

    static void appInfoStarter() {
        @NotNull Runnable infoAndSched = AppInfoOnLoad.getI();
        AppComponents.threadConfig().getTaskExecutor().execute(infoAndSched, 50);
    }
}