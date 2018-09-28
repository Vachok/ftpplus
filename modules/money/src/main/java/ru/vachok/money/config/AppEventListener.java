package ru.vachok.money.config;


import org.slf4j.Logger;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import ru.vachok.money.ConstantsFor;


/**
 @since 20.09.2018 (2:54) */
public class AppEventListener implements ApplicationListener {

    /*Fields*/

    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = AppEventListener.class.getSimpleName();

    private static final Logger LOGGER = ConstantsFor.getLogger();


    /**
     Handle an application event.  @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        LOGGER.warn(SOURCE_CLASS);
    }
}