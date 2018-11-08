package ru.vachok.money.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import ru.vachok.money.ConstantsFor;
import ru.vachok.money.components.AppVersion;
import ru.vachok.money.components.URLContent;
import ru.vachok.money.services.URLParser;
import ru.vachok.money.services.sockets.TellNetSRV;


/**
 @since 09.09.2018 (13:02) */
@ComponentScan
public class AppComponents {

    @Bean
    public static Logger getLogger() {
        return LoggerFactory.getLogger(ConstantsFor.APP_NAME);
    }

    @Bean
    public URLParser urlParser() {
        return new URLParser(new URLContent());
    }

    @Bean
    public AppVersion appVersion() {
        return new AppVersion();
    }

    @Bean
    @Scope("singleton")
    public static TellNetSRV tellNetSRV() {
        return new TellNetSRV();
    }
}