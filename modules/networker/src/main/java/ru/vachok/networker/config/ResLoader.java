package ru.vachok.networker.config;


import org.springframework.core.io.DefaultResourceLoader;


/**
 @since 22.09.2018 (11:21) */
public class ResLoader extends DefaultResourceLoader {

    /*Fields*/

    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = ResLoader.class.getSimpleName();
}