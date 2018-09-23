package ru.vachok.money.config;


import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;


/**
 @since 20.09.2018 (2:58) */
public class AppEvents {

    /*Fields*/

    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = AppEvents.class.getSimpleName();

    private static ConfigurableApplicationContext ctx = ConstantsFor.CONTEXT;

    private Throwable throwable = new ReflectiveOperationException();

    public ApplicationEvent failedApp() {
        return new ApplicationFailedEvent(AppCtx.getApplication(), null, ctx, throwable);
    }
}