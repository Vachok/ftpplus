// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.componentsrepo.ArgsReader;
import ru.vachok.networker.componentsrepo.server.TelnetStarter;
import ru.vachok.networker.enums.PropertiesNames;
import ru.vachok.networker.enums.UsefulUtilites;
import ru.vachok.networker.exe.ThreadConfig;
import ru.vachok.networker.exe.runnabletasks.TemporaryFullInternet;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.message.DBMessenger;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.systray.SystemTrayHelper;

import java.awt.*;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.concurrent.RejectedExecutionException;


@SuppressWarnings("AccessStaticViaInstance")
@SpringBootApplication
@EnableScheduling
@EnableAutoConfiguration
public class IntoApplication {
    
    
    public static final boolean TRAY_SUPPORTED = System.getProperty("os.name").toLowerCase().contains(PropertiesNames.PR_WINDOWSOS) && SystemTray.isSupported();
    
    /**
     {@link MessageLocal}
     */
    private static final MessageToUser MESSAGE_LOCAL = DBMessenger.getInstance(TemporaryFullInternet.class.getSimpleName());
    
    protected static Properties localCopyProperties = AppComponents.getProps();
    
    private static ConfigurableApplicationContext configurableApplicationContext = new SpringApplication().run(IntoApplication.class);
    
    @Contract(pure = true)
    public static ConfigurableApplicationContext getConfigurableApplicationContext() {
        ThreadConfig.dumpToFile("IntoApplication.getConfigurableApplicationContext");
        return configurableApplicationContext;
    }
    
    public static String reloadConfigurableApplicationContext() {
        AppComponents.threadConfig().killAll();
        
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
        return configurableApplicationContext.getId();
    }
    
    public static void main(@NotNull String[] args) {
        if (!Arrays.toString(args).contains("test")) {
            startTelnet();
        }
        MESSAGE_LOCAL.info(IntoApplication.class.getSimpleName(), "main", MessageFormat
            .format("{0}/{1} LoadedClass/TotalLoadedClass", ManagementFactory.getClassLoadingMXBean().getLoadedClassCount(), ManagementFactory
                .getClassLoadingMXBean().getTotalLoadedClassCount()));
        
        if (configurableApplicationContext == null) {
            try {
                configurableApplicationContext = new SpringApplication().run(IntoApplication.class);
                configurableApplicationContext.registerShutdownHook();
            }
            catch (Exception e) {
                MESSAGE_LOCAL.error(MessageFormat.format("IntoApplication.main: {0}, ({1})", e.getMessage(), e.getClass().getName()));
            }
        }
        if (args.length > 0) {
            new ArgsReader(configurableApplicationContext, args).run();
        }
        else {
            startContext();
        }
    }
    
    public static boolean closeContext() {
        configurableApplicationContext.stop();
        configurableApplicationContext.close();
        
        return configurableApplicationContext.isActive() && configurableApplicationContext.isRunning();
    }
    
    protected static void afterSt() {
        @NotNull Runnable infoAndSched = new AppInfoOnLoad();
        AppComponents.threadConfig().getTaskExecutor().execute(infoAndSched);
    }
    
    /**
     Запуск до старта Spring boot app <br> Usages: {@link #main(String[])}
     <p>
     {@link Logger#warn(java.lang.String)} - день недели. <br>
     Если {@link UsefulUtilites#thisPC()} - {@link UsefulUtilites#HOSTNAME_DO213} или "home",
     {@link SystemTrayHelper#addTray(java.lang.String)} "icons8-плохие-поросята-32.png".
     Else - {@link SystemTrayHelper#addTray(java.lang.String)} {@link String} null<br>
     {@link SpringApplication#setMainApplicationClass(java.lang.Class)}
     
     @param isTrayNeed нужен трэй или нет.
     */
    protected static void beforeSt(boolean isTrayNeed) {
        @NotNull StringBuilder stringBuilder = new StringBuilder();
        Optional optionalTray = SystemTrayHelper.getI();
    
        if (optionalTray.isPresent() & isTrayNeed) {
            SystemTrayHelper.trayAdd((SystemTrayHelper) optionalTray.get());
            stringBuilder.append(AppComponents.ipFlushDNS());
        }
        stringBuilder.append(LocalDate.now().getDayOfWeek().getValue()).append(" - day of week\n");
        stringBuilder.append(LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault())).append("\n\n");
        stringBuilder.append("Current default encoding = ").append(System.getProperty(PropertiesNames.PR_ENCODING)).append("\n");
        System.setProperty(PropertiesNames.PR_ENCODING, "UTF8");
        stringBuilder.append(new TForms().fromArray(System.getProperties()));
        FileSystemWorker.writeFile("system", stringBuilder.toString());
    }
    
    private static void startContext() {
        ThreadMXBean threadMXBean = threadMXBeanConf();
        beforeSt(true);
        try {
            configurableApplicationContext.start();
        }
        catch (Exception e) {
            MESSAGE_LOCAL.error(MessageFormat.format("IntoApplication.startContext threw away: {0}, ({1}).\n\n{2}",
                e.getMessage(), e.getClass().getName(), new TForms().fromArray(e)));
        }
        
        MESSAGE_LOCAL.info(MessageFormat.format("Main loaded successful.\n{0} CurrentThreadUserTime\n{1} ThreadCount\n{2} PeakThreadCount",
            threadMXBean.getCurrentThreadUserTime(), threadMXBean.getThreadCount(), threadMXBean.getPeakThreadCount()));
        if (!configurableApplicationContext.isRunning() & !configurableApplicationContext.isActive()) {
            throw new RejectedExecutionException(configurableApplicationContext.toString());
        }
        else {
            afterSt();
        }
    }
    
    private static void startTelnet() {
        final Thread telnetThread = new Thread(new TelnetStarter());
        telnetThread.setDaemon(true);
        telnetThread.start();
        MESSAGE_LOCAL.warn(MessageFormat.format("telnetThread.isAlive({0})", telnetThread.isAlive()));
    }
    
    private static @NotNull ThreadMXBean threadMXBeanConf() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        threadMXBean.setThreadContentionMonitoringEnabled(true);
        threadMXBean.setThreadCpuTimeEnabled(true);
        threadMXBean.resetPeakThreadCount();
        return threadMXBean;
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", IntoApplication.class.getSimpleName() + "[\n", "\n]")
            .toString();
    }
}