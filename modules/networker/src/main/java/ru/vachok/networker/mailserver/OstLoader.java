package ru.vachok.networker.mailserver;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.services.MessageLocal;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;


public class OstLoader implements ConverterPost, MakeConvert {
    
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    private String fileName;
    
    public OstLoader(String fileName) {
        this.fileName = fileName;
    }
    
    @Override public void setFileName(String fileName) {
        messageToUser.info(getClass().getSimpleName() + ".setFileName", "fileName", " = " + fileName);
    }
    
    @Override public String convertToPST() {
        return "convertToPST";
    }
    
    @Override public void showFileContent() {
        messageToUser.info(getClass().getSimpleName() + ".showFileContent", "true", " = " + true);
    }
    
    @Override public long copyierWithSave() {
        return 0;
    }
    
    @Override public Class<?> loadedClass(String className) throws ClassNotFoundException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        try {
            classLoader = libraryLoad();
        }
        catch (MalformedURLException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return classLoader.loadClass(className);
    }
}
