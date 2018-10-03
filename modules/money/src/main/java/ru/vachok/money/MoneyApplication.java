package ru.vachok.money;


import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import ru.vachok.messenger.email.ESender;
import ru.vachok.money.components.AppVersion;
import ru.vachok.money.components.URLContent;
import ru.vachok.money.config.AppComponents;
import ru.vachok.money.config.AppResLoader;
import ru.vachok.money.config.ThrAsyncConfigurator;
import ru.vachok.money.other.SystemTrayHelper;
import ru.vachok.money.services.URLParser;
import ru.vachok.mysqlandprops.EMailAndDB.SpeedRunActualize;

import java.util.Date;

import static org.springframework.boot.SpringApplication.run;


@EnableAutoConfiguration
@SpringBootApplication
public class MoneyApplication {


    /*Fields*/
    private static final SpringApplication SPRING_APPLICATION = new SpringApplication();

    private static ResourceLoader resourceLoader = new AppResLoader();

    public static void main(String[] args) {
        new SystemTrayHelper().addTrayDefaultMinimum();
        SPRING_APPLICATION.setMainApplicationClass(MoneyApplication.class);
        SPRING_APPLICATION.setLogStartupInfo(true);
        SPRING_APPLICATION.setResourceLoader(resourceLoader);
        run(MoneyApplication.class, args);
        urlS();
        startSchedule();
    }

    private static void urlS() {
        URLParser urlParser = new AppComponents().urlParser();
        urlParser.showContents();
        URLContent urlContent = urlParser.getUrlContent();
        LoggerFactory.getLogger(MoneyApplication.class.getSimpleName()).info(urlContent.getUrlPermissions());
        LoggerFactory.getLogger(MoneyApplication.class.getSimpleName()).info(urlContent.getUrlString());
        LoggerFactory.getLogger(MoneyApplication.class.getSimpleName()).info(urlContent.getContentType());
        LoggerFactory.getLogger(MoneyApplication.class.getSimpleName()).info(urlContent.getContentObj().toString());
    }

    private static void startSchedule() {
        ThreadPoolTaskExecutor defaultExecutor = new ThrAsyncConfigurator().getDefaultExecutor();
        defaultExecutor.execute(new SpeedRunActualize());
        String msg = defaultExecutor.getThreadPoolExecutor().toString();
        defaultExecutor.createThread(() -> {
            new ESender("143500@gmail.com")
                .info(ConstantsFor.APP_NAME,
                    "Started at " + new Date(ConstantsFor.START_STAMP),
                    new AppVersion().toString() + "\n" + msg + "\n" + new AppComponents().visitor().toString());
        }).start();

        LoggerFactory.getLogger(MoneyApplication.class.getSimpleName()).warn(msg);
    }
}
