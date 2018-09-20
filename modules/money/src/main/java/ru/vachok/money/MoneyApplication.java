package ru.vachok.money;

import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationEvent;
import ru.vachok.money.config.AppComponents;
import ru.vachok.money.config.AppEventListener;
import ru.vachok.money.config.AppEvents;
import ru.vachok.money.services.ParserCBRru;
import ru.vachok.money.services.SpeedRunActualize;

import static org.springframework.boot.SpringApplication.run;


@EnableAutoConfiguration
@SpringBootApplication
public class MoneyApplication {


    /*Fields*/
    private static final Logger LOGGER = AppComponents.getLogger();

    public static void main(String[] args) {
        run(MoneyApplication.class , args);
        ParserCBRru cbrBean = ConstantsFor.CONTEXT.getBean(ParserCBRru.class);
        SpeedRunActualize speedRunActualize = ConstantsFor.CONTEXT.getBean(SpeedRunActualize.class);
        LOGGER.info(speedRunActualize.call());
        cbrBean.getMap("table").forEach((x, y) -> {
            String msg = x + " Integer";
            LOGGER.info(msg);
            LOGGER.info(y.toString());
        });
        String s = cbrBean.parseTag("table");
        LOGGER.info(s);
        ApplicationEvent applicationEvent = new AppEvents().failedApp();
        new AppEventListener().onApplicationEvent(applicationEvent);
    }
}
