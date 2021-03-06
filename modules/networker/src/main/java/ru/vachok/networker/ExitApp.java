// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import com.google.firebase.database.FirebaseDatabase;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ConfigurableApplicationContext;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.NetKeeper;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.ConstantsNet;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.exe.ThreadConfig;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.restapi.props.InitProperties;

import java.io.*;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


/**
 Действия, при выходе

 @see ru.vachok.networker.ExitAppTest
 @since 21.12.2018 (12:15) */
@SuppressWarnings("StringBufferReplaceableByString")
public class ExitApp extends Thread implements Externalizable {


    private static final Map<Long, Visitor> VISITS_MAP = new ConcurrentHashMap<>();

    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, ExitApp.class.getSimpleName());

    /**
     new {@link ArrayList}, записываемый в "exit.last"

     @see #exitAppDO()
     */
    private static final Collection<String> miniLoggerLast = new ArrayList<>();

    /**
     Имя файлв для {@link ObjectOutput}
     */
    private final String fileName = ExitApp.class.getSimpleName();

    /**
     Uptime в минутах. Как статус {@link System#exit(int)}
     */
    private final long toMinutes = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - ConstantsFor.START_STAMP);

    /**
     Причина выхода
     */
    private String reasonExit = "Give me a reason to hold on to what we've got ... ";

    /**
     Объект для записи, {@link Externalizable}
     */
    private Object toWriteObj;

    /**
     Для записи {@link #toWriteObj}

     @see #writeObj()
     */
    private OutputStream outFileStream;

    public Object getToWriteObj() {
        return toWriteObj;
    }

    @Deprecated
    public boolean isWriteOwnObject() {
        try (OutputStream fileOutputStream = new FileOutputStream(fileName);
             ObjectOutput objectOutputStream = new ObjectOutputStream(fileOutputStream)
        ) {
            objectOutputStream.writeObject(toWriteObj);
            return true;
        }
        catch (IOException e) {
            return false;
        }
    }

    @Contract(pure = true)
    static Map<Long, Visitor> getVisitsMap() {
        return VISITS_MAP;
    }

    public ExitApp(FileInputStream inFileStream) {
    }

    /**
     @param reasonExit {@link #reasonExit}
     */
    public ExitApp(String reasonExit) {
        this.reasonExit = reasonExit;
    }

    public ExitApp(Object toWriteObj) {
        this.toWriteObj = toWriteObj;
    }

    public ExitApp(String reason, FileOutputStream stream, Object keeperClass) {
        this.reasonExit = reason;
        this.outFileStream = stream;
        this.toWriteObj = keeperClass;
    }

    public ExitApp() {
        messageToUser.warn(this.getClass().getSimpleName(), "Starting", LocalDateTime.now().toString());
    }

    @Override
    public void writeExternal(@NotNull ObjectOutput out) throws IOException {
        out.writeObject(toWriteObj);
    }

    @Override
    public void readExternal(@NotNull ObjectInput in) throws IOException, ClassNotFoundException {
        this.toWriteObj = in.readObject();
    }

    /**
     {@link #copyAvail()}
     */
    @Override
    public void run() {
        FirebaseDatabase.getInstance().getReference(UsefulUtilities.thisPC()).removeValue((error, ref)->messageToUser
            .error("ExitApp.onComplete", error.toException().getMessage(), AbstractForms.networkerTrace(error.toException().getStackTrace())));
        VISITS_MAP.forEach((x, y)->miniLoggerLast.add(new Date(x) + " - " + y.getRemAddr()));
        miniLoggerLast.add(reasonExit);
        try {
            copyAvail();
        }
        catch (InvokeIllegalException e) {
            messageToUser.error("ExitApp.run", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
        }
        finally {
            writeObj();
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ExitApp{");
        sb.append("reasonExit='").append(reasonExit).append('\'');
        sb.append(", fileName='").append(fileName).append('\'');
        sb.append(", toMinutes=").append(toMinutes);
        sb.append('}');
        return sb.toString();
    }

    /**
     Копирует логи

     @see FileSystemWorker
     */
    private void copyAvail() throws InvokeIllegalException {
        File appLog = new File("g:\\My_Proj\\FtpClientPlus\\modules\\networker\\app.log");
        File filePingTv = new File(FileNames.PING_TV);
        if (filePingTv.exists()) {
            FileSystemWorker.copyOrDelFile(filePingTv, Paths.get(new StringBuilder()
                .append(".")
                .append(ConstantsFor.FILESYSTEM_SEPARATOR)
                .append("lan")
                .append(ConstantsFor.FILESYSTEM_SEPARATOR)
                .append("ptv_")
                .append(System.currentTimeMillis() / 1000).append(".txt")
                .toString()).toAbsolutePath().normalize(), true);
        }

        if (appLog.exists() && appLog.canRead()) {
            FileSystemWorker.copyOrDelFile(appLog, Paths.get("\\\\10.10.111.1\\Torrents-FTP\\app.log").toAbsolutePath().normalize(), false);
        }
        else {
            miniLoggerLast.add("No app.log");
            messageToUser.info("No app.log");
        }
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
    private void
    writeObj() {
        if (toWriteObj != null) {
            miniLoggerLast.add(toWriteObj.toString().getBytes().length / ConstantsFor.KBYTE + " kbytes of object written");
            try (ObjectOutput objectOutput = new ObjectOutputStream(outFileStream)) {
                objectOutput.writeObject(toWriteObj);
            }
            catch (IOException e) {
                FileSystemWorker.error("ExitApp.writeObj", e);
            }
        }
        else {
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
        BlockingDeque<String> devices = NetKeeper.getAllDevices();
        InitProperties initProperties = InitProperties.getInstance(InitProperties.DB_MEMTABLE);
        ConfigurableApplicationContext context = IntoApplication.getContext();
        initProperties.setProps(InitProperties.getTheProps());
        if (devices.size() > 0) {
            miniLoggerLast.add("Devices " + "iterator next: " + " = " + devices.iterator().next());
            miniLoggerLast.add("Last" + " = " + devices.getLast());
            miniLoggerLast.add("BlockingDeque " + "size/remainingCapacity/total" + " = " + devices.size() + "/" + devices
                .remainingCapacity() + "/" + ConstantsNet.IPS_IN_VELKOM_VLAN);
        }
        miniLoggerLast.add("exit at " + LocalDateTime.now() + UsefulUtilities.getUpTime());
        FileSystemWorker.writeFile("exit.last", miniLoggerLast.stream());
        miniLoggerLast.add(FileSystemWorker.delTemp());
        context.stop();
        MessageToUser.getInstance(MessageToUser.EMAIL, this.getClass().getSimpleName())
            .warn(getClass().getSimpleName(), "runtime: " + toMinutes, AbstractForms.fromArray(miniLoggerLast));
        System.exit(Math.toIntExact(toMinutes));
    }
}
