// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.springframework.context.ConfigurableApplicationContext;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.NetScanFileWorker;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.services.MessageLocal;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 Действия, при выходе

 @since 21.12.2018 (12:15) */
@SuppressWarnings("StringBufferReplaceableByString")
public class ExitApp implements Runnable {

    /**
     {@link ConstantsFor#HTTP_LOCALHOST8880SLASH} {@code "pages/commit.html"}.
     */
    private static final String GO_TO = ConstantsFor.HTTP_LOCALHOST8880SLASH + "pages/commit.html";

    private static final String classMeth = "ExitApp.readCommit";

    private static final String RELOAD_CTX = ".reloadCTX";
    
    private static final String METH_COPY = "ExitApp.copyAvail";

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

    public static void reloadCTX() {
        ThreadConfig threadConfig = AppComponents.threadConfig();
        threadConfig.thrNameSet("RelCTX");

        threadConfig.killAll();

        List<Runnable> runnableList = threadConfig.getTaskScheduler().getScheduledThreadPoolExecutor().shutdownNow();
        LinkedBlockingDeque<Runnable> deqRun = new LinkedBlockingDeque<>();
        AtomicBoolean addToDeq = new AtomicBoolean(deqRun.addAll(runnableList));
        try {
            if (addToDeq.get()) {
                for (Runnable x : deqRun) {
                    Runnable remME = (deqRun).remove();

                    miniLoggerLast.add(remME.toString());
                    messageToUser.info(ExitApp.class.getSimpleName() + RELOAD_CTX , "remME" , " = " + remME + "\n" + "LinkedBlockingDeque with runnables aize = " + deqRun.size());
                }
                addToDeq.set(deqRun.addAll(threadConfig.getTaskExecutor().getThreadPoolExecutor().shutdownNow()));
                if (addToDeq.get()) {
                    ConfigurableApplicationContext context = new IntoApplication().getConfigurableApplicationContext();
                    context.stop();
                    messageToUser.info(ExitApp.class.getSimpleName() + RELOAD_CTX , "ConstantsFor.class.hashCode()" , " = " + ConstantsFor.class.hashCode());
                }
            }
        } catch (Exception e) {
            messageToUser.errorAlert(ExitApp.class.getSimpleName() , "reloadCTX" , e.getMessage());
            FileSystemWorker.error(ExitApp.class.getSimpleName() + RELOAD_CTX , e);
        }
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

    /**
     {@link #copyAvail()}
     */
    @Override
    public void run() {
        AppComponents.threadConfig().thrNameSet("exit");
        AppComponents.getVisitsMap().forEach((x, y)->miniLoggerLast.add(new Date(x) + " - " + y.getRemAddr()));
        File commitFile = new File("G:\\My_Proj\\FtpClientPlus\\modules\\networker\\src\\main\\resources\\static\\pages\\commit.html");
        if (!commitFile.exists()) {
            commitFile = new File("C:\\Users\\ikudryashov\\IdeaProjects\\spring\\modules\\networker\\src\\main\\resources\\static\\pages\\commit.html");
        }
        if (commitFile.exists() && commitFile.canRead()) {
            try {
                Desktop.getDesktop().browse(URI.create(GO_TO));
            }
            catch (IOException e) {
                messageToUser.errorAlert("ExitApp", "run", e.getMessage());
            }
            readCommit(commitFile);
        }
        else {
            messageToUser.info("NO FILES COMMIT");
        }
        miniLoggerLast.add(reasonExit);
        copyAvail();
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
        exitAppDO();
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
    private void exitAppDO() {
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

    private void readCommit(File file) {
        messageToUser.info(classMeth , file.getAbsolutePath() + " Modified:" , " " + new Date(file.lastModified()));
        String readFile = file.getAbsolutePath();
        messageToUser.info(classMeth , "commit" , " = " + readFile);
    }
    
    /**
     Копирует логи
 
     @see FileSystemWorker
     */
    @SuppressWarnings({"HardCodedStringLiteral", "FeatureEnvy"})
    private void copyAvail() {
        File appLog = new File("g:\\My_Proj\\FtpClientPlus\\modules\\networker\\app.log");
        File filePingTv = new File(ConstantsFor.FILENAME_PTV);
        FileSystemWorker.copyOrDelFile(filePingTv, new StringBuilder().append(".\\lan\\ptv_").append(System.currentTimeMillis() / 1000).append(".txt").toString(), true);
        ConcurrentMap<String, File> srvFiles = NetScanFileWorker.getI().getScanFiles();
        srvFiles.forEach((id, file)->FileSystemWorker
            .copyOrDelFile(file, file.getAbsolutePath().replace(file.getName(), "lan\\" + file.getName()), true));
        if (appLog.exists() && appLog.canRead()) {
            FileSystemWorker.copyOrDelFile(appLog, "\\\\10.10.111.1\\Torrents-FTP\\app.log", false);
        }
        else {
            miniLoggerLast.add("No app.log");
            messageToUser.info("No app.log");
        }
        writeObj();
    }
    
    private void libCopy() {
        Path path = Paths.get(".");
        List<File> myLibs = Arrays.asList(Objects.requireNonNull(new File(path.toString() + "\\ostpst\\build\\libs\\").listFiles()));
        myLibs.addAll(Arrays.asList(Objects.requireNonNull(new File("g:\\My_Proj\\libs\\messenger\\build\\libs\\").listFiles())));
        myLibs.forEach(x->{
            if (x.exists() && x.getName().toLowerCase().contains(".jar")) {
                FileSystemWorker.copyOrDelFile(x, "\\lib\\" + x.getName(), false);
            }
        });
    }
    
}
