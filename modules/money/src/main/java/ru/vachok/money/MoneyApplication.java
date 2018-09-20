package ru.vachok.money;

import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationEvent;
import ru.vachok.money.config.AppEventListener;
import ru.vachok.money.config.AppEvents;
import ru.vachok.money.services.ParserCBRru;

import static org.springframework.boot.SpringApplication.run;


@EnableAutoConfiguration
@SpringBootApplication
public class MoneyApplication {


    /*Fields*/
    private static final Logger LOGGER = ConstantsFor.getLogger();

    public static void main(String[] args) {
        run(MoneyApplication.class , args);
    }
}
