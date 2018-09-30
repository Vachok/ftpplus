package ru.vachok.money;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import ru.vachok.money.config.AppResLoader;
import ru.vachok.money.config.ThrAsyncConfigurator;
import ru.vachok.money.other.SystemTrayHelper;
import ru.vachok.mysqlandprops.EMailAndDB.SpeedRunActualize;

import static org.springframework.boot.SpringApplication.run;


@EnableAutoConfiguration
@SpringBootApplication
public class MoneyApplication {


    /*Fields*/
    private static final SpringApplication SPRING_APPLICATION = new SpringApplication();

    private static ResourceLoader resourceLoader = new AppResLoader();

    public static void main(String[] args) {
        new SystemTrayHelper().addTray();
        SPRING_APPLICATION.setMainApplicationClass(MoneyApplication.class);
        SPRING_APPLICATION.setLogStartupInfo(true);
        SPRING_APPLICATION.setResourceLoader(resourceLoader);
        run(MoneyApplication.class , args);
        startSchedule();
    }

    private static void startSchedule() {
        ThreadPoolTaskExecutor defaultExecutor = new ThrAsyncConfigurator().getDefaultExecutor();
        defaultExecutor.execute(new SpeedRunActualize());
    }
}
