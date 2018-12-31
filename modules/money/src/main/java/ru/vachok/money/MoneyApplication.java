package ru.vachok.money;


import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import ru.vachok.messenger.email.ESender;
import ru.vachok.money.components.AppVersion;
import ru.vachok.money.config.AppComponents;
import ru.vachok.money.config.ThreadConfig;
import ru.vachok.money.other.SystemTrayHelper;
import ru.vachok.mysqlandprops.EMailAndDB.SpeedRunActualize;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.springframework.boot.SpringApplication.run;


/**
 Стартер.
 <p>

 @since 02.05.2018 (19:59) */
@EnableAutoConfiguration
@SpringBootApplication
public class MoneyApplication {

    /**
     {@link ConfigurableApplicationContext}
     */
    private static ConfigurableApplicationContext runningApp = null;

    /**
     @return {@link #runningApp}
     */
    static ConfigurableApplicationContext getRunningApp() {
        return runningApp;
    }

    /**
     Запуск

     @param args null
     */
    public static void main(String[] args) {
        new SystemTrayHelper().addTrayDefaultMinimum();
        runningApp = run(MoneyApplication.class, args);
        startSchedule();
    }

    /**
     Установка заданий по-распмсанию.
     */
    private static void startSchedule() {
        ThreadPoolTaskExecutor defaultExecutor = new ThreadConfig().getDefaultExecutor();
        defaultExecutor.execute(new SpeedRunActualize());
        String msg = defaultExecutor.getThreadPoolExecutor().toString();
        Runnable r = () ->
        {
            new ESender("143500@gmail.com")
                .info(ConstantsFor.APP_NAME,
                    ConstantsFor.APP_NAME + ". Started at " + new Date(ConstantsFor.START_STAMP),
                    new AppVersion().toString() + "\n" + msg + "\n" + filesRnd());
        };
        ScheduledExecutorService scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(2);
        scheduledThreadPoolExecutor.scheduleWithFixedDelay(r, ConstantsFor.DELAY, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);
        AppComponents.getLogger().warn(msg);
    }

    /**
     @return имя пк или random-список файлов.
     */
    private static String filesRnd() {
        String localPc = ConstantsFor.localPc();
        if(localPc.equalsIgnoreCase("home")){
            File[] files = new File("g:\\myEX\\").listFiles();
            StringBuilder stringBuilder = new StringBuilder();
            for(int i = 0; i < 20; i++){
                stringBuilder
                    .append(files[new SecureRandom().nextInt(Objects.requireNonNull(files).length)].getName())
                    .append("\n");
            }
            try(FileOutputStream fileOutputStream = new FileOutputStream(new File("g:\\myEX\\files.txt"))){
                fileOutputStream.write(stringBuilder.toString().getBytes());
            }
            catch(IOException e){
                LoggerFactory.getLogger(MoneyApplication.class.getSimpleName()).error(e.getMessage(), e);
            }
            return stringBuilder.toString();
        }
        return localPc;
    }
}
