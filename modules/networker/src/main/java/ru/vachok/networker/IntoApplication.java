// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.config.AppCtx;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.MyServer;
import ru.vachok.networker.net.WeekPCStats;
import ru.vachok.networker.services.MessageLocal;
import ru.vachok.networker.services.SpeedChecker;
import ru.vachok.networker.systray.SystemTrayHelper;

import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;


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
     {@link AppComponents#getOrSetProps(boolean)}
     */
    private static final Properties LOCAL_PROPS = new Properties();

    /**
     {@link MessageLocal}
     */
    private static final MessageToUser messageToUser = new MessageLocal(IntoApplication.class.getSimpleName());

    private static final ThreadPoolTaskExecutor EXECUTOR = AppComponents.threadConfig().getTaskExecutor();

    /**
     {@link ConfigurableApplicationContext} = null.
     */
    @SuppressWarnings ("CanBeFinal")
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
     {@link SpringApplication#run(java.lang.Class, java.lang.String...)}. Инициализация {@link #configurableApplicationContext}. <br>
     {@link Logger#warn(java.lang.String)} - new {@link String} {@code msg} = {@link IntoApplication#afterSt()} <br>
     Если есть аргументы - {@link #readArgs(String[])} <br>
     {@link Logger#info(java.lang.String)} - время работы метода.

     @param args аргументы запуска
     @see SystemTrayHelper
     */
    public static void main(@Nullable String[] args) {
        FileSystemWorker.delFilePatterns(ConstantsFor.getStringsVisit());
        LOCAL_PROPS.putAll(AppComponents.getOrSetProps());

        LOCAL_PROPS.setProperty("ff", "false");
        if (args != null && args.length > 0) {
            //noinspection NullableProblems
            readArgs(args);
        } else {
            beforeSt(true);
            configurableApplicationContext.start();
            afterSt();
        } AppComponents.getOrSetProps(true);
    }

    /**
     Запуск после старта Spring boot app <br> Usages: {@link #main(String[])}
     <p>
     1. {@link AppComponents#threadConfig()}. Управление запуском и трэдами. <br><br>
     <b>Runnable</b><br>
     2. {@link IntoApplication#getWeekPCStats()} собирает инфо о скорости в файл. Если воскресенье, запускает {@link WeekPCStats} <br><br>
     <b>Далее:</b><br>
     3. {@link AppComponents#threadConfig()} (4. {@link ThreadConfig#getTaskExecutor()}) - запуск <b>Runnable</b> <br>
     5. {@link ThreadConfig#getTaskExecutor()} - запуск {@link AppInfoOnLoad}. <br><br>
     <b>{@link Exception}:</b><br>
     6. {@link TForms#fromArray(java.lang.Exception, boolean)} - искл. в строку. 7. {@link FileSystemWorker#writeFile(java.lang.String, java.util.List)} и
     запишем в файл.
     */
    private static void afterSt() {
        @NotNull Runnable infoAndSched = new AppInfoOnLoad();
        Runnable mySrv = MyServer.getI();
        EXECUTOR.submit(infoAndSched);
        EXECUTOR.submit(mySrv);
        EXECUTOR.submit(IntoApplication::getWeekPCStats);
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
    private static void readArgs(@NotNull String... args) {
        boolean isTray = true;
        Runnable exitApp = new ExitApp(IntoApplication.class.getSimpleName());
        List<@NotNull String> argsList = Arrays.asList(args);
        ConcurrentMap<String, String> argsMap = new ConcurrentHashMap<>();

        for (int i = 0; i < argsList.size(); i++) {
            String key = argsList.get(i);
            String value = "true";
            try {
                value = argsList.get(i + 1);
            } catch (ArrayIndexOutOfBoundsException ignore) {
                //
            }
            if (!value.contains("-")) {
                argsMap.put(key, value);
            } else {
                if (!key.contains("-")) {
                    argsMap.put("", "");
                } else {
                    argsMap.put(key, "true");
                }
            }
        }

        for (Map.Entry<String, String> stringStringEntry : argsMap.entrySet()) {
            if (stringStringEntry.getKey().contains(ConstantsFor.PR_TOTPC)) {
                LOCAL_PROPS.setProperty(ConstantsFor.PR_TOTPC, stringStringEntry.getValue());
            }
            if (stringStringEntry.getKey().equalsIgnoreCase("off")) {
                AppComponents.threadConfig().execByThreadConfig(exitApp);
            }
            if (stringStringEntry.getKey().contains("notray")) {
                messageToUser.info("IntoApplication.readArgs", "key", " = " + stringStringEntry.getKey());
                isTray = false;
            }
            if (stringStringEntry.getKey().contains("ff")) {
                Map<Object, Object> objectMap = Collections.unmodifiableMap(DBPropsCallable.takePr());
                LOCAL_PROPS.clear();
                LOCAL_PROPS.putAll(objectMap);
                FileSystemWorker.copyOrDelFile(new File("ConstantsFor.properties"), ".\\ConstantsFor.bak", false);
            }
            if (stringStringEntry.getKey().contains("lport")) {
                LOCAL_PROPS.setProperty("lport", stringStringEntry.getValue());
            }
        }

        beforeSt(isTray);
        configurableApplicationContext.start();
        afterSt();
    }

    /**
     Статистика по-пользователям за неделю.
     <p>
     Запуск new {@link SpeedChecker.ChkMailAndUpdateDB}, через {@link Executors#unconfigurableExecutorService(java.util.concurrent.ExecutorService)}
     <p>
     Если {@link LocalDate#getDayOfWeek()} equals {@link DayOfWeek#SUNDAY}, запуск new {@link WeekPCStats}
     */
    private static void getWeekPCStats() {
        if(LocalDate.now().getDayOfWeek().equals(DayOfWeek.SUNDAY)){
            AppComponents.threadConfig().execByThreadConfig(new WeekPCStats());
        }
    }


    public static void setConfigurableApplicationContext( ConfigurableApplicationContext configurableApplicationContext ) {
        IntoApplication.configurableApplicationContext = configurableApplicationContext;
    }

    private static void trayAdd() {
        SystemTrayHelper systemTrayHelper = SystemTrayHelper.getI(); if (ConstantsFor.thisPC().toLowerCase().contains(ConstantsFor.HOSTNAME_DO213)) {
            systemTrayHelper.addTray("icons8-плохие-поросята-32.png");
        }
        else{
            if(ConstantsFor.thisPC().toLowerCase().contains("home")){
                systemTrayHelper.addTray("icons8-house-26.png");
            }
            else{
                systemTrayHelper.addTray(ConstantsFor.FILENAME_ICON);
            }
        }
    }

    /**
     * Запуск до старта Spring boot app <br> Usages: {@link #main(String[])}
     * <p>
     * {@link Logger#warn(java.lang.String)} - день недели. <br>
     * Если {@link ConstantsFor#thisPC()} - {@link ConstantsFor#HOSTNAME_DO213} или "home",
     * {@link SystemTrayHelper#addTray(java.lang.String)} "icons8-плохие-поросята-32.png".
     * Else - {@link SystemTrayHelper#addTray(java.lang.String)} {@link String} null<br>
     * {@link SpringApplication#setMainApplicationClass(java.lang.Class)}
     *
     * @param isTrayNeed нужен трэй или нет.
     */
    private static void beforeSt( boolean isTrayNeed ) {
        if (isTrayNeed) {
            trayAdd();
        }
        @NotNull StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(LocalDate.now().getDayOfWeek().getValue());
        stringBuilder.append(" - day of week\n");
        stringBuilder.append(LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.FULL , Locale.getDefault()));
        messageToUser.info("IntoApplication.beforeSt" , "stringBuilder" , stringBuilder.toString());
        SPRING_APPLICATION.setMainApplicationClass(IntoApplication.class);
        SPRING_APPLICATION.setApplicationContextClass(AppCtx.class);
        System.setProperty("encoding" , "UTF8");
        FileSystemWorker.writeFile("system" , new TForms().fromArray(System.getProperties()));
    }
}