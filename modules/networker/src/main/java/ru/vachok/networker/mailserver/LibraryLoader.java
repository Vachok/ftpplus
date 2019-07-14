// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.mailserver;


import org.jetbrains.annotations.Nullable;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;


/**
 @since 13.05.2019 (9:00) */
public interface LibraryLoader {
    
    
    default @Nullable Class<?> libraryLoad(URL[] libURLs, String className) throws MalformedURLException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        ClassLoader c = ClassLoader.getSystemClassLoader();
        MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
        if (libURLs == null || libURLs.length == 0) {
            libURLs = new URL[]{new URL("http://networker.vachok.ru/lib/ostpst.jar")};
        }
        
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
                Class<OstLoader> ostLoader = (Class<OstLoader>) urlClassLoader.loadClass(className);
                return ostLoader;
            }
            catch (IOException e) {
                messageToUser.error(e.getMessage());
            }
        }
        return null;
    }
    
    Class<?> loadedClass() throws ClassNotFoundException;
}
