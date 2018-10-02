package ru.vachok.networker.componentsrepo;


import org.springframework.stereotype.Component;
import ru.vachok.networker.config.ResLoader;

import java.io.IOException;
import java.util.List;


/**
 @since 01.10.2018 (9:46) */
@Component
public class ServiceInform {

    private String resourcesTXT;

    private ResoCache resoCache = ResoCache.getResoCache();

    private ResLoader loader = new ResLoader();

    public String getResourcesTXT() {
        loader.getResourceCache(resoCache.getClass());
        try{
            setResourcesTXT();
        }
        catch(IOException | NullPointerException e){
            return e.getMessage();
        }
        return resourcesTXT;
    }

    private String setResourcesTXT() throws IOException {
        if(loader.loadNew()){
            List<ResoCache> resoCacheResources = resoCache.getResources();
            StringBuilder stringBuilder = new StringBuilder();
            for(ResoCache resoCacheResource : resoCacheResources){
                stringBuilder
                    .append(resoCacheResource.getFilename())
                    .append(" ")
                    .append(resoCacheResource.getFilePath())
                    .append("<br>")
                    .append(resoCacheResource.getURL());

            }
            this.resourcesTXT = stringBuilder.toString();
            return resourcesTXT;
        }
        else{
            return resoCache.toString();
        }
    }
}
