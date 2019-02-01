package ru.vachok.networker;


import org.slf4j.Logger;
import ru.vachok.messenger.MessageCons;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static ru.vachok.networker.IntoApplication.getConfigurableApplicationContext;


/**
 Действия, при выходе

 @since 21.12.2018 (12:15) */
@SuppressWarnings("StringBufferReplaceableByString")
public class ExitApp implements Runnable {

    /**
     {@link AppComponents#getLogger()}
     */
    private static final Logger LOGGER = AppComponents.getLogger();

    /**
     Thread name
     */
    private static final String EXIT_APP_RUN = "ExitApp.run";

    private List<String> stringList = new ArrayList<>();

    /**
     Причина выхода
     */
    private String reasonExit;

    private Object toWriteObj;

    private FileOutputStream out;

    /**
     Uptime в минутах. Как статус {@link System#exit(int)}
     */
    private long toMinutes = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - ConstantsFor.START_STAMP);

    /**
     @param reasonExit причина выхода
     @param объект,    для сохранения на диск
     @param out        если требуется сохранить состояние
     */
    public ExitApp(String reasonExit, FileOutputStream out, Object toWriteObj) {
        this.reasonExit = reasonExit;
        this.toWriteObj = toWriteObj;
        this.out = out;
    }

    public ExitApp(String reasonExit, FileOutputStream out) {
        this.reasonExit = reasonExit;
        this.out = out;
        this.toWriteObj = ConstantsFor.class;
    }

    /**
     @param reasonExit {@link #reasonExit}
     */
    public ExitApp(String reasonExit) {
        this.reasonExit = reasonExit;
    }

    /**
     Копирует логи
     */
    @SuppressWarnings({"HardCodedStringLiteral", "FeatureEnvy"})
    private void copyAvail() {
        File appLog = new File("g:\\My_Proj\\FtpClientPlus\\modules\\networker\\app.log");
        FileSystemWorker.copyOrDelFile(new File(ConstantsFor.AVAILABLE_LAST_TXT), new StringBuilder().append(".\\lan\\vlans200").append(System.currentTimeMillis() / 1000).append(".txt").toString(),
            true);
        FileSystemWorker.copyOrDelFile(new File(ConstantsFor.OLD_LAN_TXT), new StringBuilder().append(".\\lan\\old_lan_").append(System.currentTimeMillis() / 1000).append(".txt").toString(), true);
        if (appLog.exists() && appLog.canRead()) {
            FileSystemWorker.copyOrDelFile(appLog, "\\\\10.10.111.1\\Torrents-FTP\\app.log", false);
        } else {
            stringList.add("No app.log");
            LOGGER.info("No app.log");
        }
        writeObj();
    }

    private void writeObj() {
        if (toWriteObj != null) {
            stringList.add(toWriteObj.toString());
            try (ObjectOutput objectOutput = new ObjectOutputStream(out)) {
                objectOutput.writeObject(toWriteObj);
            } catch (IOException e) {
                new MessageCons().errorAlert("ExitApp", "writeObj", TForms.from(e));
            }
        } else {
            stringList.add("No object");
        }
        exitAppDO();
    }

    /**
     Сохранение {@link ConstantsFor#saveProps(Properties)}, удаление временного и выход
     <p>
     Код выхода = <i>uptime</i> в минутах.
     */
    private void exitAppDO() {
        stringList.add("exit at " + LocalDateTime.now().toString() + ConstantsFor.getUpTime());
        FileSystemWorker.recFile("exit.last", stringList);
        FileSystemWorker.delTemp();
        getConfigurableApplicationContext().close();
        new ThreadConfig().killAll();
        System.exit(Math.toIntExact(toMinutes));
    }

    /**
     {@link #copyAvail()}
     */
    @Override
    public void run() {
        Thread.currentThread().setName(ExitApp.EXIT_APP_RUN);
        LOGGER.warn(reasonExit);
        stringList.add(reasonExit);
        copyAvail();
    }
}
