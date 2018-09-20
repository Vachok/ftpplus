package ru.vachok.money.config;


import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Configuration;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Logger;


/**
 @since 20.09.2018 (23:44) */
@Configuration @ConditionalOnMissingClass
public class CLoaderURL {

//fixme 20.09.2018 (23:58) org.springframework.boot.loader.jar.JarURLConnection.getInputStream(JarURLConnection.java:163)

    public URLClassLoader getClassLoader(){
        try{
            URL[] urls = {new URL("G:\\My_Proj\\FtpClientPlus\\modules\\money\\src\\main\\resources\\templates")};
            return new URLClassLoader(urls);
        }
        catch(MalformedURLException e){
            Logger.getLogger(CLoaderURL.class.getSimpleName())
                .throwing(CLoaderURL.class.getName(), "getClassLoader", e);
        }
        throw new IllegalThreadStateException();
    }
}