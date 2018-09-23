package ru.vachok.money.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;


/**
 @since 23.09.2018 (10:43) */
@Configuration
public class AppResLoader extends DefaultResourceLoader {

    /*Fields*/

    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = AppResLoader.class.getSimpleName();

    private static final Logger LOGGER = LoggerFactory.getLogger(SOURCE_CLASS);

    @Override
    public ClassLoader getClassLoader() throws NullPointerException {
        LOGGER.info(super.getClassLoader().getClass().getTypeName());
        return super.getClassLoader();
    }

}