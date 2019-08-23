// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.componentsrepo.server.TelnetServer;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.exe.ThreadConfig;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.systray.SystemTrayHelper;

import java.awt.*;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
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
    private static final MessageToUser MESSAGE_LOCAL = MessageToUser.getInstance(MessageToUser.DB, IntoApplication.class.getSimpleName());
    
    private static final boolean IS_TRAY_SUPPORTED = SystemTray.isSupported();
    
    protected static Properties localCopyProperties = AppComponents.getProps();
    
    private static ConfigurableApplicationContext configurableApplicationContext = new SpringApplication().run(IntoApplication.class);
    
    @Contract(pure = true)
    public static ConfigurableApplicationContext getConfigurableApplicationContext() {
        ThreadConfig.dumpToFile("IntoApplication.getConfigurableApplicationContext");
        return configurableApplicationContext;
    }
    
    public static @NotNull String reloadConfigurableApplicationContext() {
        String killAssStr = AppComponents.threadConfig().killAll();
        MESSAGE_LOCAL.warn(killAssStr);
        if (configurableApplicationContext != null && configurableApplicationContext.isActive()) {
            configurableApplicationContext.stop();
            configurableApplicationContext.close();
        }
        try {
            configurableApplicationContext = SpringApplication.run(IntoApplication.class);
        }
        catch (ApplicationContextException e) {
            MESSAGE_LOCAL.error(FileSystemWorker.error(IntoApplication.class.getSimpleName() + ".reloadConfigurableApplicationContext", e));
        }
        return killAssStr;
    }
    
    public static void main(@NotNull String[] args) {
        if (!Arrays.toString(args).contains("test")) {
            UsefulUtilities.startTelnet();
        }
        if (configurableApplicationContext == null) {
            try {
                configurableApplicationContext = new SpringApplication().run(IntoApplication.class);
            }
            catch (RuntimeException e) {
                MESSAGE_LOCAL.error(MessageFormat.format("IntoApplication.main: {0}, ({1})", e.getMessage(), e.getClass().getName()));
            }
        }
        if (args.length > 0) {
            new IntoApplication.ArgsReader(args).run();
        }
        else {
            startContext();
        }
    }
    
    private static void startContext() {
        beforeSt();
        try {
            checkTray();
            configurableApplicationContext.start();
        }
        catch (RuntimeException e) {
            MESSAGE_LOCAL.error(MessageFormat.format("IntoApplication.startContext threw away: {0}, ({1}).\n\n{2}",
                    e.getMessage(), e.getClass().getName(), new TForms().fromArray(e)));
        }
        if (!configurableApplicationContext.isRunning() & !configurableApplicationContext.isActive()) {
            throw new RejectedExecutionException(configurableApplicationContext.toString());
        }
        else {
            afterSt();
        }
    }
    
    protected static void beforeSt() {
        @NotNull StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(UsefulUtilities.ipFlushDNS());
        stringBuilder.append(LocalDate.now().getDayOfWeek().getValue()).append(" - day of week\n");
        stringBuilder.append(LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault())).append("\n\n");
        stringBuilder.append("Current default encoding = ").append(System.getProperty(PropertiesNames.PR_ENCODING)).append("\n");
        System.setProperty(PropertiesNames.PR_ENCODING, "UTF8");
        stringBuilder.append(new TForms().fromArray(System.getProperties()));
        FileSystemWorker.writeFile(FileNames.SYSTEM, stringBuilder.toString());
    }
    
    public static void closeContext() {
        configurableApplicationContext.stop();
        configurableApplicationContext.close();
        if (configurableApplicationContext.isActive()) {
            configurableApplicationContext.isRunning();
        }
        AppComponents.threadConfig().killAll();
    }
    
    private static void checkTray() {
        Optional optionalTray = SystemTrayHelper.getI();
        try {
            if (IS_TRAY_SUPPORTED && optionalTray.isPresent()) {
                ((SystemTrayHelper) optionalTray.get()).trayAdd();
            }
        }
        catch (HeadlessException e) {
            MESSAGE_LOCAL.error(MessageFormat
                    .format("IntoApplication.checkTray {0} - {1}\nStack:\n{2}", e.getClass().getTypeName(), e.getMessage(), new TForms().fromArray(e)));
        }
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", IntoApplication.class.getSimpleName() + "[\n", "\n]")
                .toString();
    }
    
    protected static void afterSt() {
        @NotNull Runnable infoAndSched = new AppInfoOnLoad();
        Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).execute(infoAndSched);
    }
    


    /**
     @since 19.07.2019 (9:51)
     */
    public static class ArgsReader implements Runnable {
        
        
        private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
        
        private String[] appArgs;
        
        private ConcurrentMap<String, String> argsMap = new ConcurrentHashMap<>();
    
        public ArgsReader(String[] appArgs) {
            this.appArgs = appArgs;
        }
        
        @Override
        public void run() {
            fillArgsMap();
        }
        
        private void fillArgsMap() {
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
        
        private boolean parseMapEntry(@NotNull Map.Entry<String, String> stringStringEntry, Runnable exitApp) {
            boolean isTray = true;
            if (stringStringEntry.getKey().contains(PropertiesNames.PR_TOTPC)) {
                localCopyProperties.setProperty(PropertiesNames.PR_TOTPC, stringStringEntry.getValue());
            }
            if (stringStringEntry.getKey().equals("off")) {
                AppComponents.threadConfig().execByThreadConfig(exitApp);
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
        
        private void readArgs() {
            beforeSt();
            try {
                startContext();
            }
            catch (IllegalStateException e) {
                messageToUser.error(MessageFormat.format("ArgsReader.readArgs: {0}, ({1})", e.getMessage(), e.getClass().getName()));
            }
            afterSt();
        }
        
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("ArgsReader{");
            sb.append("appArgs=").append(new TForms().fromArray(appArgs));
            sb.append('}');
            return sb.toString();
        }
    }
}