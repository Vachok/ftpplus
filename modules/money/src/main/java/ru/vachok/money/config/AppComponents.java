package ru.vachok.money.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import ru.vachok.money.components.AppVersion;
import ru.vachok.money.components.URLContent;
import ru.vachok.money.services.URLParser;


/**
 @since 09.09.2018 (13:02) */
@ComponentScan
public class AppComponents {

    @Bean
    public URLParser urlParser() {
        return new URLParser(new URLContent());
    }

    @Bean
    public AppVersion appVersion() {
        return new AppVersion();
    }
}