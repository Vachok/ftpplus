package ru.vachok.networker;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static ru.vachok.networker.IntoApplication.getConfigurableApplicationContext;


/**
 Действия, при выходе

 @since 21.12.2018 (12:15) */
@SuppressWarnings("StringBufferReplaceableByString")
public class ExitApp implements Runnable {

    /**
     {@link LoggerFactory#getLogger(java.lang.String)}
     <p>
     Logger name = {@link Class#getSimpleName()}
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ExitApp.class.getSimpleName());

    /**
     Thread name
     <p>
     "ExitApp.run"
     */
    private static final String EXIT_APP_RUN = "ExitApp.run";

    /**
     new {@link ArrayList}, записываемый в "exit.last"

     @see #exitAppDO()
     */
    private List<String> stringList = new ArrayList<>();

    /**
     Причина выхода
     */
    private String reasonExit;

    /**
     Объект для записи, {@link Externalizable}
     */
    private Object toWriteObj;

    /**
     Для записи {@link #toWriteObj}

     @see #writeObj()
     */
    private FileOutputStream out;

    /**
     Uptime в минутах. Как статус {@link System#exit(int)}
     */
    private long toMinutes = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - ConstantsFor.START_STAMP);

    /**
     Сохранение состояния объектов.
     <p>

     @param reasonExit  причина выхода
     @param toWriteObj, {@link Object}  для сохранения на диск
     @param out         если требуется сохранить состояние
     */
    public ExitApp(String reasonExit, FileOutputStream out, Object toWriteObj) {
        this.reasonExit = reasonExit;
        this.toWriteObj = toWriteObj;
        this.out = out;
    }

    /**
     @param reasonExit {@link #reasonExit}
     */
    public ExitApp(String reasonExit) {
        this.reasonExit = reasonExit;
    }

    public void reloadCTX() {
        ThreadConfig threadConfig = AppComponents.threadConfig();
        threadConfig.getTaskScheduler().getScheduledThreadPoolExecutor().shutdown();
        threadConfig.getTaskExecutor().getThreadPoolExecutor().shutdown();
        List<Runnable> runnableList = threadConfig.getTaskScheduler().getScheduledThreadPoolExecutor().shutdownNow();
        runnableList.clear();
        runnableList = threadConfig.getTaskExecutor().getThreadPoolExecutor().shutdownNow();
        runnableList.clear();
        getConfigurableApplicationContext().refresh();
    }

    /**
     Копирует логи

     @see FileSystemWorker
     */
    @SuppressWarnings({"HardCodedStringLiteral", "FeatureEnvy"})
    private void copyAvail() {
        File appLog = new File("g:\\My_Proj\\FtpClientPlus\\modules\\networker\\app.log");
        FileSystemWorker.copyOrDelFile(new File(ConstantsFor.FILENAME_AVAILABLELASTTXT), new StringBuilder().append(".\\lan\\vlans200").append(System.currentTimeMillis() / 1000).append(".txt").toString(),
            true);
        FileSystemWorker.copyOrDelFile(new File(ConstantsFor.FILENAME_OLDLANTXT), new StringBuilder().append(".\\lan\\old_lan_").append(System.currentTimeMillis() / 1000).append(".txt").toString(), true);
        FileSystemWorker.copyOrDelFile(new File("ping.tv"), ".\\lan\\tv_" + System.currentTimeMillis() / 1000 + ".ping", true);
        if (appLog.exists() && appLog.canRead()) {
            FileSystemWorker.copyOrDelFile(appLog, "\\\\10.10.111.1\\Torrents-FTP\\app.log", false);
        } else {
            stringList.add("No app.log");
            LOGGER.info("No app.log");
        }
        writeObj();
    }

    /**
     Запись {@link Externalizable}
     <p>
     Возможность сохранить состояние объекта.
     <p>
     Если {@link #toWriteObj} не {@code null} - {@link ObjectOutput#writeObject(java.lang.Object)}
     <b>{@link IOException}:</b><br>
     {@link FileSystemWorker#error(java.lang.String, java.lang.Exception)}
     <p>
     Или {@link #stringList} add {@code "No object"}.
     <p>
     Запуск {@link #exitAppDO()}
     */
    private void writeObj() {
        if (toWriteObj != null) {
            stringList.add(toWriteObj.toString());
            try (ObjectOutput objectOutput = new ObjectOutputStream(out)) {
                objectOutput.writeObject(toWriteObj);
            } catch (IOException e) {
                FileSystemWorker.error("ExitApp.writeObj", e);
            }
        } else {
            stringList.add("No object");
        }
        exitAppDO();
    }

    /**
     Метод выхода
     <p>
     Добавление в {@link #stringList}: {@code "exit at " + LocalDateTime.now().toString() + ConstantsFor.getUpTime()} <br>
     {@link FileSystemWorker#recFile(java.lang.String, java.util.List)}. {@link List} = {@link #stringList} <br>
     {@link FileSystemWorker#delTemp()}. Удаление мусора <br>
     {@link ConfigurableApplicationContext#close()}. Остановка контекста. <br>
     {@link ThreadConfig#killAll()} закрытие {@link java.util.concurrent.ExecutorService} и {@link java.util.concurrent.ScheduledExecutorService} <br>
     {@link System#exit(int)} int = <i>uptime</i> в минутах.
     */
    private void exitAppDO() {
        stringList.add("exit at " + LocalDateTime.now().toString() + ConstantsFor.getUpTime());
        FileSystemWorker.recFile("exit.last", stringList);
        FileSystemWorker.delTemp();
        getConfigurableApplicationContext().close();

        AppComponents.threadConfig().killAll();

        System.exit(Math.toIntExact(toMinutes));
    }

    /**
     {@link #copyAvail()}
     */
    @Override
    public void run() {
        Thread.currentThread().setName(ExitApp.EXIT_APP_RUN);
        stringList.add(reasonExit);
        AppComponents.getOrSetProps(true);
        copyAvail();
    }
}
