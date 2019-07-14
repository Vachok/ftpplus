// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.Lifecycle;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.exe.ThreadConfig;
import ru.vachok.networker.exe.runnabletasks.TelnetStarter;
import ru.vachok.networker.exe.schedule.WeekStats;
import ru.vachok.networker.fileworks.DeleterTemp;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.TestServer;
import ru.vachok.networker.restapi.message.DBMessenger;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.systray.SystemTrayHelper;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 Старт
 <p>
 Dependencies:
 {@link ru.vachok.networker} - 5 : {@link AppComponents}, {@link AppInfoOnLoad}, {@link ConstantsFor}, {@link ExitApp}, {@link TForms}<br>
 {@link ru.vachok.networker.config} - 1 : {@link ThreadConfig} <br>
 {@link ru.vachok.networker.fileworks} - 1 : {@link FileSystemWorker} <br>
 {@link ru.vachok.networker.services} - 2 : {@link MessageLocal}, {@link WeekStats} <br>
 {@link ru.vachok.networker.systray} - 1 : {@link SystemTrayHelper}
 <p>
 
 @see AppInfoOnLoad
 @since 02.05.2018 (10:36) */
@SuppressWarnings("AccessStaticViaInstance") @SpringBootApplication
@EnableScheduling
@EnableAutoConfiguration
public class IntoApplication {
    
    
    public static final boolean TRAY_SUPPORTED = SystemTray.isSupported();
    
    @SuppressWarnings("StaticCollection") private static final Properties LOCAL_PROPS = AppComponents.getProps();
    
    private static final SpringApplication SPRING_APPLICATION = new SpringApplication();
    
    /**
     {@link MessageLocal}
     */
    private static final MessageToUser MESSAGE_LOCAL = new DBMessenger(IntoApplication.class.getSimpleName());
    
    private static ConfigurableApplicationContext configurableApplicationContext;
    
    public static boolean reloadConfigurableApplicationContext() {
        AppComponents.threadConfig().killAll();
    
        if (configurableApplicationContext != null && configurableApplicationContext.isActive()) {
            configurableApplicationContext.stop();
            configurableApplicationContext.close();
        }
        try {
            configurableApplicationContext = SpringApplication.run(IntoApplication.class);
        }
        catch (ApplicationContextException e) {
            System.err.println(FileSystemWorker.error(IntoApplication.class.getSimpleName() + ".reloadConfigurableApplicationContext", e));
        }
        return configurableApplicationContext.isRunning();
    }
    
    public static void main(String[] args) throws IOException {
        ThreadMXBean threadMXBean = threadMXBeanConf();
    
        MESSAGE_LOCAL.info(IntoApplication.class.getSimpleName(), "main", MessageFormat
            .format("{0}/{1} TotalLoadedClass/UnloadedClass", ManagementFactory.getClassLoadingMXBean().getTotalLoadedClassCount(), ManagementFactory
                .getClassLoadingMXBean().getUnloadedClassCount()));
        final Thread telnetThread = new Thread(new TelnetStarter());
        telnetThread.setDaemon(true);
        telnetThread.start();
    
        if (configurableApplicationContext == null) {
            try {
                configurableApplicationContext = SPRING_APPLICATION.run(IntoApplication.class);
            }
            catch (BeanCreationException e) {
                MESSAGE_LOCAL.error(FileSystemWorker.error(IntoApplication.class.getSimpleName() + ".main", e));
            }
        }
    
        delFilePatterns(ConstantsFor.getStringsVisit());
        if (args != null && args.length > 0) {
            readArgs(configurableApplicationContext, args);
        }
        else {
            try {
                beforeSt(true);
            }
            catch (NullPointerException e) {
                MESSAGE_LOCAL.error(FileSystemWorker.error(IntoApplication.class.getSimpleName() + ".main", e));
            }
            afterSt();
        }
        MESSAGE_LOCAL.info(MessageFormat
            .format("Main loaded successful.\n{0} CurrentThreadUserTime\n{1} ThreadCount\n{2} PeakThreadCount", threadMXBean.getCurrentThreadUserTime(), threadMXBean
                .getThreadCount(), threadMXBean.getPeakThreadCount()));
    }
    
    private static ThreadMXBean threadMXBeanConf() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        threadMXBean.setThreadContentionMonitoringEnabled(true);
        threadMXBean.setThreadCpuTimeEnabled(true);
        threadMXBean.resetPeakThreadCount();
        return threadMXBean;
    }
    
    private static void delFilePatterns(String[] patToDelArr) {
        File file = new File(".");
        for (String patToDel : patToDelArr) {
            FileVisitor<Path> deleterTemp = new DeleterTemp(patToDel);
            try {
                Path walkFileTree = Files.walkFileTree(file.toPath(), deleterTemp);
                System.out.println("walkFileTree = " + walkFileTree);
            }
            catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }
    
    /**
     Запуск после старта Spring boot app <br> Usages: {@link #main(String[])}
     <p>
     1. {@link AppComponents#threadConfig()}. Управление запуском и трэдами. <br><br>
     <b>Runnable</b><br>
     2. {@link AppInfoOnLoad#getWeekPCStats()} собирает инфо о скорости в файл. Если воскресенье, запускает {@link WeekStats} <br><br>
     <b>Далее:</b><br>
     3. {@link AppComponents#threadConfig()} (4. {@link ThreadConfig#getTaskExecutor()}) - запуск <b>Runnable</b> <br>
     5. {@link ThreadConfig#getTaskExecutor()} - запуск {@link AppInfoOnLoad}. <br><br>
     <b>{@link Exception}:</b><br>
     6. {@link TForms#fromArray(java.lang.Exception, boolean)} - искл. в строку. 7. {@link FileSystemWorker#writeFile(java.lang.String, java.util.List)} и
     запишем в файл.
     */
    private static void afterSt() {
        @NotNull Runnable infoAndSched = new AppInfoOnLoad();
        AppComponents.threadConfig().getTaskExecutor().execute(infoAndSched);
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
    private static void readArgs(Lifecycle context, @NotNull String... args) throws IOException {
        boolean isTray = true;
        Runnable exitApp = new ExitApp(IntoApplication.class.getSimpleName());
        List<@NotNull String> argsList = Arrays.asList(args);
        ConcurrentMap<String, String> argsMap = new ConcurrentHashMap<>();
    
        for (int i = 0; i < argsList.size(); i++) {
            String key = argsList.get(i);
            String value = "true";
            try {
                value = argsList.get(i + 1);
            }
            catch (ArrayIndexOutOfBoundsException ignore) {
                //
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
        for (Map.Entry<String, String> stringStringEntry : argsMap.entrySet()) {
            isTray = parseMapEntry(stringStringEntry, exitApp);
        }
        beforeSt(isTray);
        context.start();
        afterSt();
    }
    
    private static boolean parseMapEntry(Map.Entry<String, String> stringStringEntry, Runnable exitApp) {
        boolean isTray = true;
        if (stringStringEntry.getKey().contains(ConstantsFor.PR_TOTPC)) {
            LOCAL_PROPS.setProperty(ConstantsFor.PR_TOTPC, stringStringEntry.getValue());
        }
        if (stringStringEntry.getKey().equals("off")) {
            AppComponents.threadConfig().execByThreadConfig(exitApp);
        }
        if (stringStringEntry.getKey().contains("notray")) {
            MESSAGE_LOCAL.info("IntoApplication.readArgs", "key", " = " + stringStringEntry.getKey());
            isTray = false;
        }
        if (stringStringEntry.getKey().contains("ff")) {
            Map<Object, Object> objectMap = Collections.unmodifiableMap(AppComponents.getProps());
            LOCAL_PROPS.clear();
            LOCAL_PROPS.putAll(objectMap);
        }
        if (stringStringEntry.getKey().contains(TestServer.PR_LPORT)) {
            LOCAL_PROPS.setProperty(TestServer.PR_LPORT, stringStringEntry.getValue());
        }
        
        return isTray;
    }
    
    /**
     Запуск до старта Spring boot app <br> Usages: {@link #main(String[])}
     <p>
     {@link Logger#warn(java.lang.String)} - день недели. <br>
     Если {@link ConstantsFor#thisPC()} - {@link ConstantsFor#HOSTNAME_DO213} или "home",
     {@link SystemTrayHelper#addTray(java.lang.String)} "icons8-плохие-поросята-32.png".
     Else - {@link SystemTrayHelper#addTray(java.lang.String)} {@link String} null<br>
     {@link SpringApplication#setMainApplicationClass(java.lang.Class)}
 
     @param isTrayNeed нужен трэй или нет.
     */
    private static void beforeSt(boolean isTrayNeed) throws IOException {
        @NotNull StringBuilder stringBuilder = new StringBuilder();
        if (isTrayNeed) {
            SystemTrayHelper.trayAdd(SystemTrayHelper.getI());
            stringBuilder.append(AppComponents.ipFlushDNS());
        }
        stringBuilder.append("updateProps = ").append(new AppComponents().updateProps(LOCAL_PROPS));
        stringBuilder.append(LocalDate.now().getDayOfWeek().getValue());
        stringBuilder.append(" - day of week\n");
        stringBuilder.append(LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault()));
        MESSAGE_LOCAL.info("IntoApplication.beforeSt", "stringBuilder", stringBuilder.toString());
        System.setProperty(ConstantsFor.STR_ENCODING, "UTF8");
        FileSystemWorker.writeFile("system", new TForms().fromArray(System.getProperties()));
    }
}