package ru.vachok.networker.config;


import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;


/**
 @since 30.08.2018 (13:22) */
@Configuration
@EnableAsync
public class AppCtx {

    private static AnnotationConfigApplicationContext configApplicationContext = new AnnotationConfigApplicationContext();

    public static ApplicationContext getConfigApplicationContext() {
        configApplicationContext.scan("ru.vachok.networker.beans");
        configApplicationContext.scan("ru.vachok.networker.services");
        configApplicationContext.scan("ru.vachok.networker.components");
        configApplicationContext.refresh();
        return configApplicationContext;
    }
}
