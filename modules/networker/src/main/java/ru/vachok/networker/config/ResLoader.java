package ru.vachok.networker.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.componentsrepo.ResoCache;


/**
 @since 22.09.2018 (11:21) */
@Configuration
public class ResLoader extends DefaultResourceLoader {

    private static ResoCache resoCache = AppComponents.resoCache();

    /*Fields*/

    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = ResLoader.class.getSimpleName();


}