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
import ru.vachok.networker.componentsrepo.server.TelnetServer;
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
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.RejectedExecutionException;


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
    
    private static Properties localCopyProperties = AppComponents.getProps();
    
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
            IntoApplication.ArgsReader.run();
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
    
    private static void checkTray() {
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
    
    private static void appInfoStarter() {
        @NotNull Runnable infoAndSched = new AppInfoOnLoad();
        AppComponents.threadConfig().getTaskExecutor().execute(infoAndSched, 50);
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", IntoApplication.class.getSimpleName() + "[\n", "\n]")
                .toString();
    }
    

    /**
     @since 19.07.2019 (9:51)
     */
    private static class ArgsReader {
        
        
        private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, IntoApplication.ArgsReader.class.getSimpleName());
    
        private static String[] appArgs;
    
        private static ConcurrentMap<String, String> argsMap = new ConcurrentHashMap<>();
    
        ArgsReader(String[] appArgs) {
            IntoApplication.ArgsReader.appArgs = appArgs;
        }
    
        public static void run() {
            fillArgsMap();
        }
    
        private static void fillArgsMap() {
            List<@NotNull String> argsList = Arrays.asList(appArgs);
            Runnable exitApp = new ExitApp(IntoApplication.class.getSimpleName());
            boolean isTray = true;
            for (int i = 0; i < argsList.size(); i++) {
                String key = argsList.get(i);
                String value;
                try {
                    value = argsList.get(i + 1);
                }
                catch (ArrayIndexOutOfBoundsException ignore) {
                    value = "true";
                }
                if (!value.contains("-")) {
                    argsMap.put(key, value);
                }
                else {
                    if (!key.contains("-")) {
                        argsMap.put("", "");
                    }
                    else {
                        argsMap.put(key, "true");
                    }
                }
            }
            for (Map.Entry<String, String> argValueEntry : argsMap.entrySet()) {
                isTray = parseMapEntry(argValueEntry, exitApp);
                if (argValueEntry.getValue().equals("test")) {
                    throw new RejectedExecutionException("TEST. VALUE");
                }
                if (argValueEntry.getKey().equals("test")) {
                    throw new RejectedExecutionException("TEST. KEY");
                }
            }
            if (isTray && SystemTrayHelper.getI().isPresent()) {
                ((SystemTrayHelper) SystemTrayHelper.getI().get()).trayAdd();
            }
            readArgs();
        }
    
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("ArgsReader{");
            sb.append("appArgs=").append(AbstractForms.fromArray(appArgs));
            sb.append('}');
            return sb.toString();
        }
    
        private static boolean parseMapEntry(@NotNull Map.Entry<String, String> stringStringEntry, Runnable exitApp) {
            boolean isTray = true;
            if (stringStringEntry.getKey().contains(PropertiesNames.TOTPC)) {
                localCopyProperties.setProperty(PropertiesNames.TOTPC, stringStringEntry.getValue());
            }
            if (stringStringEntry.getKey().equals("off")) {
                AppComponents.threadConfig().execByThreadConfig(exitApp, "ArgsReader.parseMapEntry");
            }
            if (stringStringEntry.getKey().contains("notray")) {
                messageToUser.info("IntoApplication.readArgs", "key", " = " + stringStringEntry.getKey());
                isTray = false;
            }
            if (stringStringEntry.getKey().contains("ff")) {
                Map<Object, Object> objectMap = Collections.unmodifiableMap(AppComponents.getProps());
                localCopyProperties.clear();
                localCopyProperties.putAll(objectMap);
            }
            if (stringStringEntry.getKey().contains(TelnetServer.PR_LPORT)) {
                localCopyProperties.setProperty(TelnetServer.PR_LPORT, stringStringEntry.getValue());
            }
            
            return isTray;
        }
    
        private static void readArgs() {
            setUTF8Enc();
            try {
                checkTray();
            }
            catch (IllegalStateException e) {
                messageToUser.error(MessageFormat.format("ArgsReader.readArgs: {0}, ({1})", e.getMessage(), e.getClass().getName()));
            }
            finally {
                appInfoStarter();
            }
        }
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