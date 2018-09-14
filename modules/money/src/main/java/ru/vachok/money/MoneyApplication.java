package ru.vachok.money;

import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.vachok.money.config.AppComponents;

import static org.springframework.boot.SpringApplication.run;


@EnableAutoConfiguration
@SpringBootApplication
public class MoneyApplication {


    /*Fields*/
    private static final Logger LOGGER = AppComponents.getLogger();

    public static void main(String[] args) {
        run(MoneyApplication.class , args);
        LOGGER.info(String.valueOf(AppComponents.getSpeedActualizer()));
    }
}
