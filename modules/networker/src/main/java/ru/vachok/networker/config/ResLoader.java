package ru.vachok.networker.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import ru.vachok.networker.componentsrepo.ResoCache;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;


/**
 @since 22.09.2018 (11:21) */
@Configuration
public class ResLoader extends DefaultResourceLoader {

    private ResoCache resoCache = ResoCache.getResoCache();

    public boolean loadNew() {
        String path = getClass().getResource("/static/texts/users.txt").getPath();
        Path pathRes = null;
        try{
            pathRes = Paths.get(path).toRealPath();
        }
        catch(IOException e){
            return false;
        }
        File file = pathRes.getParent().toFile();
        for(File filesWithTextResource : Objects.requireNonNull(file.listFiles())){
            resoCache.setFile(filesWithTextResource);
            resoCache.setFileName(filesWithTextResource.getName());
            resoCache.setFilePath(filesWithTextResource.getAbsolutePath());
            resoCache.setDescr("TXT File");
            resoCache.setLastModif(System.currentTimeMillis());
            return resoCache.exists();
        }
        throw new IllegalStateException("Вас тут быть не должно!");
    }
}