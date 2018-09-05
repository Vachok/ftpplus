package ru.vachok.networker.config;


import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


/**
 @since 30.08.2018 (13:22) */
public class AppCtx {

    private final AppComponents appComponents = new AppComponents();

    private AnnotationConfigApplicationContext appCtx = new AnnotationConfigApplicationContext();

    public ApplicationContext getAppCtx() {
        appCtx.scan("ru.vachok.networker.beans");
        return appCtx;
    }
}
