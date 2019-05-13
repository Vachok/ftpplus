package ru.vachok.networker.mailserver;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.services.MessageLocal;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;


/**
 @since 13.05.2019 (9:00) */
public interface ConverterPost {
    
    
    default ClassLoader libraryLoad() throws MalformedURLException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        ClassLoader c = ClassLoader.getSystemClassLoader();
        URL[] libURLs = {new URL("http://networker.vachok.ru/lib/ostpst-8.0.1919.jar")};
        MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
        
        if (c instanceof URLClassLoader) {
            URLClassLoader urlClassLoader = (URLClassLoader) c;
            Class<?>[] paraTypes = new Class[1];
            paraTypes[0] = URL.class;
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", paraTypes);
            method.setAccessible(true);
            Object[] args = new Object[1];
            for (int i = 0; i < libURLs.length; i++) {
                args[0] = libURLs[i];
                method.invoke(urlClassLoader, args);
            }
        }
        else {
            Class<?> aClass;
            try (URLClassLoader urlClassLoader = new URLClassLoader(libURLs)) {
                aClass = urlClassLoader.loadClass("ru.vachok.ostpst.MakeConvert");
                Package aPackage = aClass.getPackage();
                c = aClass.getClassLoader();
            }
            catch (IOException e) {
                messageToUser.error(e.getMessage());
            }
        }
        return c;
    }
    Class<?> loadedClass(String className) throws ClassNotFoundException;
}
