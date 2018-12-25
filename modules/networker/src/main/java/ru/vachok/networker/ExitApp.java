package ru.vachok.networker;


import org.slf4j.Logger;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.File;
import java.util.Properties;
import java.util.concurrent.TimeUnit;


/**
 Действия, при выходе

 @since 21.12.2018 (12:15) */
@SuppressWarnings ("StringBufferReplaceableByString")
public class ExitApp implements Runnable {

    /**
     {@link AppComponents#getLogger()}
     */
    private static final Logger LOGGER = AppComponents.getLogger();

    /**
     Переменная для сохранения {@link ConstantsFor#getProps()} в БД
     {@link #run()}
     */
    private Properties properties = new Properties();

    /**
     Причина выхода
     */
    private final String reasonExit;

    /**
     Uptime в минутах. Как статус {@link System#exit(int)}
     */
    private final long toMinutes = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - ConstantsFor.START_STAMP);

    /**
     @param reasonExit {@link #reasonExit}
     */
    public ExitApp(String reasonExit) {
        this.reasonExit = reasonExit;
    }

    /**
     {@link #copyAvail()}
     */
    @Override
    public void run() {
        this.properties = ConstantsFor.getProps();
        LOGGER.info(ConstantsFor.EXIT_APP_RUN);
        Thread.currentThread().setName(ConstantsFor.EXIT_APP_RUN);
        LOGGER.warn(reasonExit);
        copyAvail();
    }

    /**
     Копирует логи
     */
    @SuppressWarnings ({"HardCodedStringLiteral", "FeatureEnvy"})
    private void copyAvail() {
        File appLog = new File("g:\\My_Proj\\FtpClientPlus\\modules\\networker\\app.log\\");
        FileSystemWorker.copyOrDelFile(new File(ConstantsFor.AVAILABLE_LAST_TXT), new StringBuilder().append(".\\lan\\vlans200").append(System.currentTimeMillis() / 1000).append(".txt").toString(),
            true);
        FileSystemWorker.copyOrDelFile(new File(ConstantsFor.OLD_LAN_TXT), new StringBuilder().append(".\\lan\\old_lan_").append(System.currentTimeMillis() / 1000).append(".txt").toString(), true);
        if(appLog.exists() && appLog.canRead()){
            FileSystemWorker.copyOrDelFile(appLog, "\\\\10.10.111.1\\Torrents-FTP\\app.log", false);
        }
        exitAppDO();
    }

    /**
     Сохранение {@link ConstantsFor#saveProps(Properties)}, удаление временного и выход
     <p>
     Код выхода = <i>uptime</i> в минутах.
     */
    private void exitAppDO() {
        ConstantsFor.saveProps(properties);
        IntoApplication.getConfigurableApplicationContext().close();
        FileSystemWorker.delTemp();
        System.exit(Math.toIntExact(toMinutes));
    }
}
