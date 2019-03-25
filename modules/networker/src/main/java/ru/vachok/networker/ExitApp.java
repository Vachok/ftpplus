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
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static ru.vachok.networker.IntoApplication.getConfigurableApplicationContext;


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
    private OutputStream out = null;

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
                    ConfigurableApplicationContext context = getConfigurableApplicationContext();
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
            messageToUser.info(getClass().getSimpleName() + ".writeOwnObject: ", fileName, " = " + new File(fileName).length() / ConstantsFor.KBYTE);
            return true;
        } catch (IOException e) {
            messageToUser.errorAlert(getClass().getSimpleName(), "writeOwnObject", e.getMessage());
            FileSystemWorker.error("ExitApp.writeOwnObject", e);
            return false;
        }
    }

    /**
     Копирует логи

     @see FileSystemWorker
     */
    @SuppressWarnings({"HardCodedStringLiteral", "FeatureEnvy"})
    private void copyAvail() {
        File appLog = new File("g:\\My_Proj\\FtpClientPlus\\modules\\networker\\app.log");
        File scan200 = new File(ConstantsNet.FILENAME_NEWLAN210);
        File scan210 = new File(ConstantsNet.FILENAME_NEWLAN200210);
        File oldLanFile0 = new File(ConstantsNet.FILENAME_OLDLANTXT0);
        File oldLanFile1 = new File(ConstantsNet.FILENAME_OLDLANTXT1);
        File filePingTv = new File(ConstantsFor.FILENAME_PTV);

        if (Stream.of(scan200 , scan210 , oldLanFile0 , oldLanFile1 , filePingTv).anyMatch(file1 -> !file1.exists())) {
            try {
                Path isFile200 = Files.createFile(scan200.toPath());
                Path isFile210 = Files.createFile(scan210.toPath());
                Path oldLanPath0 = Files.createFile(oldLanFile0.toPath());
                Path oldLanPath1 = Files.createFile(oldLanFile1.toPath());
                Path pingTvPath = Files.createFile(filePingTv.toPath());
                messageToUser.info(METH_COPY , "isFile210" , " = " + isFile210 + "\nisFile200 = " + isFile200 + "\noldLanPath0 = " + oldLanPath0 + "\noldLanPath1 = " + oldLanFile1 + "\npingTvPath= " + pingTvPath);
            }
            catch (IOException e) {
                FileSystemWorker.error(METH_COPY , e);
            }
        }

        FileSystemWorker.copyOrDelFile(scan200, new StringBuilder().append("\\lan\\vlans200_").append(System.currentTimeMillis() / 1000).append(".txt").toString(), true);
        FileSystemWorker.copyOrDelFile(scan210, new StringBuilder().append(".\\lan\\vlans210_").append(System.currentTimeMillis() / 1000).append(".txt").toString(), true);
        FileSystemWorker.copyOrDelFile(oldLanFile0, new StringBuilder().append(".\\lan\\0old_lan_").append(System.currentTimeMillis() / 1000).append(".txt").toString(), true);
        FileSystemWorker.copyOrDelFile(oldLanFile1, new StringBuilder().append(".\\lan\\1old_lan_").append(System.currentTimeMillis() / 1000).append(".txt").toString(), true);
        ConcurrentMap<String, File> srvFiles = NetScanFileWorker.getI().getFilesScan();
        srvFiles.forEach(( id , file ) -> FileSystemWorker.copyOrDelFile(file , new StringBuilder().append(".\\lan\\").append(file.getName().replaceAll(ConstantsNet.FILENAME_SERVTXT , "")).append(System.currentTimeMillis() / 1000).append(".txt").toString() , true));
        if (appLog.exists() && appLog.canRead()) {
            FileSystemWorker.copyOrDelFile(appLog, "\\\\10.10.111.1\\Torrents-FTP\\app.log", false);
        }
        else {
            miniLoggerLast.add("No app.log");
            messageToUser.info("No app.log");
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
        miniLoggerLast.add("Devices " + "iterator next: " + " = " + devices.iterator().next());
        miniLoggerLast.add("Last" + " = " + devices.getLast());
        miniLoggerLast.add("BlockingDeque " + "size/remainingCapacity/total" + " = " + devices.size() + "/" + devices.remainingCapacity() + "/" + ConstantsNet.IPS_IN_VELKOM_VLAN);
        miniLoggerLast.add("exit at " + LocalDateTime.now() + ConstantsFor.getUpTime());
        FileSystemWorker.writeFile("exit.last", miniLoggerLast.stream());
        FileSystemWorker.delTemp();
        getConfigurableApplicationContext().close();
        AppComponents.threadConfig().killAll();
        System.exit(Math.toIntExact(toMinutes));
    }


    private void readCommit(File file) {
        messageToUser.info(classMeth , file.getAbsolutePath() + " Modified:" , " " + new Date(file.lastModified()));
        String readFile = file.getAbsolutePath();
        messageToUser.info(classMeth , "commit" , " = " + readFile);
    }


    /**
     {@link #copyAvail()}
     */
    @Override
    public void run() {
        AppComponents.threadConfig().thrNameSet("exit");
        File commitFile = new File("G:\\My_Proj\\FtpClientPlus\\modules\\networker\\src\\main\\resources\\static\\pages\\commit.html");
        if (!commitFile.exists()) {
            commitFile = new File("C:\\Users\\ikudryashov\\IdeaProjects\\spring\\modules\\networker\\src\\main\\resources\\static\\pages\\commit.html");
        }
        if (commitFile.exists() && commitFile.canRead()) {
            try {
                Desktop.getDesktop().browse(URI.create(GO_TO));
            } catch (IOException e) {
                messageToUser.errorAlert("ExitApp", "run", e.getMessage());
            }
            readCommit(commitFile);
        } else {
            messageToUser.info("NO FILES COMMIT");
        }
        miniLoggerLast.add(reasonExit);
        AppComponents.getOrSetProps(true);
        copyAvail();
    }
}
