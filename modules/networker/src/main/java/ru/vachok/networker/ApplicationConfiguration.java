package ru.vachok.networker;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;


@ComponentScan
public class ApplicationConfiguration {


    @Bean
    public static Logger logger() {
        return LoggerFactory.getLogger("ru.vachok");
    }
}
