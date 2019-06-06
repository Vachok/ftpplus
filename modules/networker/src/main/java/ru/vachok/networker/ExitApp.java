// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.springframework.context.ConfigurableApplicationContext;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.exe.ThreadConfig;
import ru.vachok.networker.exe.schedule.DiapazonScan;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.services.MessageLocal;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


/**
 Действия, при выходе

 @since 21.12.2018 (12:15) */
@SuppressWarnings("StringBufferReplaceableByString")
public class ExitApp implements Runnable {
    
    
    private static final Map<Long, Visitor> VISITS_MAP = new ConcurrentHashMap<>();
    
    /**
     {@link #copyAvail()}
     */
    @Override
    public void run() {
        AppComponents.threadConfig().thrNameSet("exit");
        VISITS_MAP.forEach((x, y)->miniLoggerLast.add(new Date(x) + " - " + y.getRemAddr()));
        miniLoggerLast.add(reasonExit);
        copyAvail();
    }
    
    private static MessageToUser messageToUser = new MessageLocal(ExitApp.class.getSimpleName());

    /**
     new {@link ArrayList}, записываемый в "exit.last"

     @see #exitAppDO()
     */
    private static Collection<String> miniLoggerLast = new ArrayList<>();

    /**
     Причина выхода
     */
    private String reasonExit = "Give me a reason to hold on to what we've got ... ";

    /**
     Имя файлв для {@link ObjectOutput}
     */
    private String fileName = ExitApp.class.getSimpleName();

    /**
     Объект для записи, {@link Externalizable}
     */
    private Object toWriteObj = this;

    /**
     Для записи {@link #toWriteObj}

     @see #writeObj()
     */
    private OutputStream out;

    /**
     Uptime в минутах. Как статус {@link System#exit(int)}
     */
    private long toMinutes = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - ConstantsFor.START_STAMP);

    /**
     Сохранение состояния объектов.
     <p>

     @param reasonExit причина выхода
     @param toWriteObj, {@link Object}  для сохранения на диск
     @param out если требуется сохранить состояние
     */
    public ExitApp(String reasonExit, FileOutputStream out, Object toWriteObj) {
        this.reasonExit = reasonExit;
        this.toWriteObj = toWriteObj;
        this.out = out;
    }
    
    public ExitApp(String fileName, Object toWriteObj) {
        this.fileName = fileName;
        this.toWriteObj = toWriteObj;
    }
    
    /**
     @param reasonExit {@link #reasonExit}
     */
    public ExitApp(String reasonExit) {
        this.reasonExit = reasonExit;
    }
    
    public boolean writeOwnObject() {
        try (OutputStream fileOutputStream = new FileOutputStream(fileName);
             ObjectOutput objectOutputStream = new ObjectOutputStream(fileOutputStream)
        ) {
            objectOutputStream.writeObject(toWriteObj);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    static Map<Long, Visitor> getVisitsMap() {
        return VISITS_MAP;
    }
    
    static Map<String, File> scanFiles() {
        return DiapazonScan.getInstance().getScanFiles();
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
     Или {@link #miniLoggerLast} add {@code "No object"}.
     <p>
     Запуск {@link #exitAppDO()}
     */
    private void writeObj() {
        if (toWriteObj != null) {
            miniLoggerLast.add(toWriteObj.toString().getBytes().length / ConstantsFor.KBYTE + " kbytes of object written");
            try (ObjectOutput objectOutput = new ObjectOutputStream(out)) {
                objectOutput.writeObject(toWriteObj);
            } catch (IOException e) {
                FileSystemWorker.error("ExitApp.writeObj", e);
            }
        } else {
            miniLoggerLast.add("No object");
        }
        try {
            exitAppDO();
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
        }
    }

    /**
     Метод выхода
     <p>
     Добавление в {@link #miniLoggerLast}: {@code "exit at " + LocalDateTime.now().toString() + ConstantsFor.getUpTime()} <br>
     {@link FileSystemWorker#writeFile(java.lang.String, java.util.List)}. {@link List} = {@link #miniLoggerLast} <br>
     {@link FileSystemWorker#delTemp()}. Удаление мусора <br>
     {@link ConfigurableApplicationContext#close()}. Остановка контекста. <br>
     {@link ThreadConfig#killAll()} закрытие {@link java.util.concurrent.ExecutorService} и {@link java.util.concurrent.ScheduledExecutorService} <br>
     {@link System#exit(int)} int = <i>uptime</i> в минутах.
     */
    private void exitAppDO() throws IOException {
        BlockingDeque<String> devices = ConstantsNet.getAllDevices();
        Properties properties = AppComponents.getProps();
        miniLoggerLast.add("Devices " + "iterator next: " + " = " + devices.iterator().next());
        miniLoggerLast.add("Last" + " = " + devices.getLast());
        miniLoggerLast.add("BlockingDeque " + "size/remainingCapacity/total" + " = " + devices.size() + "/" + devices.remainingCapacity() + "/" + ConstantsNet.IPS_IN_VELKOM_VLAN);
        miniLoggerLast.add("exit at " + LocalDateTime.now() + ConstantsFor.getUpTime());
        miniLoggerLast.add("Properties in DATABASE : " + new AppComponents().updateProps(properties));
        miniLoggerLast.add("\n" + new TForms().fromArray(properties, false));
        FileSystemWorker.writeFile("exit.last", miniLoggerLast.stream());
        miniLoggerLast.add(FileSystemWorker.delTemp());
        AppComponents.threadConfig().killAll();
        System.exit(Math.toIntExact(toMinutes));
    }
    
    /**
     Копирует логи
 
     @see FileSystemWorker
     */
    @SuppressWarnings({"HardCodedStringLiteral", "FeatureEnvy"})
    private void copyAvail() {
        File appLog = new File("g:\\My_Proj\\FtpClientPlus\\modules\\networker\\app.log");
        File filePingTv = new File(ConstantsFor.FILENAME_PTV);
        FileSystemWorker.copyOrDelFile(filePingTv, new StringBuilder().append(ConstantsFor.FILESYSTEM_SEPARATOR + "lan" + ConstantsFor.FILESYSTEM_SEPARATOR + "ptv_")
            .append(System.currentTimeMillis() / 1000).append(".txt").toString(), true);
        Map<String, File> srvFiles = scanFiles();
        srvFiles.forEach((id, file)->FileSystemWorker
            .copyOrDelFile(file, file.getAbsolutePath().replace(file.getName(), "lan" + ConstantsFor.FILESYSTEM_SEPARATOR + file.getName()), true));
        if (appLog.exists() && appLog.canRead()) {
            FileSystemWorker.copyOrDelFile(appLog, "\\\\10.10.111.1\\Torrents-FTP\\app.log", false);
        }
        else {
            miniLoggerLast.add("No app.log");
            messageToUser.info("No app.log");
        }
        writeObj();
    }
    
}
