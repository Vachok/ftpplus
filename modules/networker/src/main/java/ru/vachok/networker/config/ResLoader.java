package ru.vachok.networker.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import ru.vachok.networker.componentsrepo.ResoCache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 @since 22.09.2018 (11:21) */
@Configuration
public class ResLoader extends DefaultResourceLoader {

    private Map<Resource, ResoCache> resourceResoCacheMap = new ConcurrentHashMap<>();

    public ResLoader() {
        this.resourceResoCacheMap = ResLoader.this.getResourceCache(ResoCache.class);
    }
}