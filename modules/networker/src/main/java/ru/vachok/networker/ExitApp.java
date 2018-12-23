package ru.vachok.networker;


import org.slf4j.Logger;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;


/**
 Действия, при выходе

 @since 21.12.2018 (12:15) */
public class ExitApp implements Runnable {

    /**
     {@link AppComponents#getLogger()}
     */
    private static final Logger LOGGER = AppComponents.getLogger();

    /**
     Причина выхода
     */
    private String reasonExit;

    /**
     Имя инициатора
     */
    private String name;

    /**
     Переменная для сохранения {@link ConstantsFor#getProps()} в БД
     {@link #run()}
     */
    private Properties properties;

    /**
     Uptime в минутах. Как статус {@link System#exit(int)}
     */
    private long toMinutes = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - ConstantsFor.START_STAMP);

    /**
     @param name       {@link #name}
     @param reasonExit {@link #reasonExit}
     */
    public ExitApp(String name, String reasonExit) {
        this.reasonExit = reasonExit;
        this.name = name;
    }

    /**
     {@link #reasonExit} is {@code  "No Reason. From " + ExitApp.class.getSimpleName}

     @param fromClass {@link #name}
     */
    public ExitApp(String fromClass) {
        this.reasonExit = "No Reason. From " + ExitApp.class.getSimpleName();
        this.name = fromClass;
    }

    /**
     {@link #copyAvail()}
     */
    @Override
    public void run() {
        this.properties = ConstantsFor.getProps();
        LOGGER.info("ExitApp.run");
        Thread.currentThread().setName("ExitApp.run");
        LOGGER.warn(reasonExit);
        try{
            copyAvail();
        }
        catch(IOException e){
            exitAppDO();
        }
    }

    private void copyAvail() throws IOException {
        File appLog = new File("\"g:\\\\My_Proj\\\\FtpClientPlus\\\\modules\\\\networker\\\\app.log\"");
        FileSystemWorker.copyOrDelFile(new File("available_last.txt"), ".\\lan\\vlans200" + System.currentTimeMillis() / 1000 + ".txt", true);
        FileSystemWorker.copyOrDelFile(new File("old_lan.txt"), ".\\lan\\old_lan_" + System.currentTimeMillis() / 1000 + ".txt", true);
        if(appLog.exists() && appLog.canRead()){
            FileSystemWorker.copyOrDelFile(appLog, "\\\\10.10.111.1\\Torrents-FTP\\app.log", false);
        }
        exitAppDO();
    }

    private void exitAppDO() {
        ConstantsFor.saveProps(properties);
        IntoApplication.getConfigurableApplicationContext().close();
        FileSystemWorker.delTemp();
        System.exit(Math.toIntExact(toMinutes));
    }
}
