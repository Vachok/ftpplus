package ru.vachok.networker.config;


import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


/**
 @since 30.08.2018 (13:22) */
public class AppCtx {

    private AnnotationConfigApplicationContext configApplicationContext = new AnnotationConfigApplicationContext();

    public ApplicationContext getConfigApplicationContext() {
        configApplicationContext.scan("ru.vachok.networker.beans");
        return configApplicationContext;
    }
}
