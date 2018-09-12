package ru.vachok.networker.config;


import org.slf4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import ru.vachok.networker.beans.AppComponents;

import java.util.concurrent.TimeUnit;


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

    public static AnnotationConfigApplicationContext scanForBeansAndRefreshContext() {
        long startMeth = System.currentTimeMillis();
        configApplicationContext.scan("ru.vachok.networker.beans");
        configApplicationContext.scan("ru.vachok.networker.services");
        configApplicationContext.scan("ru.vachok.networker.components");
        long stopMeth = System.currentTimeMillis();
        AppCtx.methMetricGetCTX = TimeUnit.MILLISECONDS.toSeconds(stopMeth - startMeth);
        String msg = methMetricGetCTX + " second " + SOURCE_CLASS;
        LOGGER.info(msg);
        return configApplicationContext;
    }
}
