// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.componentsrepo.systray.SystemTrayHelper;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.restapi.props.InitProperties;

import java.awt.*;
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
    
    private static Properties localCopyProperties = InitProperties.getTheProps();
    
    private static ConfigurableApplicationContext configurableApplicationContext;
    
    
    @Contract(pure = true)
    public static ConfigurableApplicationContext getConfigurableApplicationContext() {
        return configurableApplicationContext;
    }
    
    public static void main(@NotNull String[] args) {
        setUTF8Enc();
        if (!Arrays.toString(args).contains("test")) {
            UsefulUtilities.startTelnet();
            InitProperties.setPreference(AppInfoOnLoad.class.getSimpleName(), String.valueOf(0));
            MESSAGE_LOCAL.info(UsefulUtilities.scheduleTrunkPcUserAuto());
        }
        if (args.length > 0) {
            ArgsReader.run();
        }
        else {
            checkTray();
        }
    }
    
    protected static void setUTF8Enc() {
        @NotNull StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault())).append("\n\n");
        System.setProperty(PropertiesNames.ENCODING, "UTF8");
        stringBuilder.append(AbstractForms.fromArray(System.getProperties()));
        FileSystemWorker.writeFile(FileNames.SYSTEM, stringBuilder.toString());
    }
    
    static void checkTray() {
        SPRING_APPLICATION.run(IntoApplication.class);
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
    
    static void appInfoStarter() {
        @NotNull Runnable infoAndSched = new AppInfoOnLoad();
        AppComponents.threadConfig().getTaskExecutor().execute(infoAndSched, 50);
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", IntoApplication.class.getSimpleName() + "[\n", "\n]")
                .toString();
    }
    
    protected static void closeContext() {
        configurableApplicationContext.stop();
        configurableApplicationContext.close();
        if (configurableApplicationContext.isActive()) {
            configurableApplicationContext.refresh();
        }
        else {
            MESSAGE_LOCAL.info("AppComponents.threadConfig().killAll()");
        }
    }
}