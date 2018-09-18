package ru.vachok.networker;


import org.slf4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import ru.vachok.networker.componentsrepo.AppComponents;


/**
 @since 30.08.2018 (13:22) */
@Configuration
@EnableAsync
public class AppCtx {

    /*Fields*/
    private static final String SOURCE_CLASS = AppCtx.class.getSimpleName();

    private static final Logger LOGGER = AppComponents.getLogger();

    private static long methMetricGetCTX;

    public long getMethMetricGetCTX() {
        return methMetricGetCTX;
    }

    private static AnnotationConfigApplicationContext configApplicationContext = new AnnotationConfigApplicationContext();

    static AnnotationConfigApplicationContext scanForBeansAndRefreshContext() {
        configApplicationContext.scan("ru.vachok.networker.componentsrepo");
        configApplicationContext.scan("ru.vachok.networker.services");
        configApplicationContext.scan("ru.vachok.networker.config");
        return configApplicationContext;
    }
}
