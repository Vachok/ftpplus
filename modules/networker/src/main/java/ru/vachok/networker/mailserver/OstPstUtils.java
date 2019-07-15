// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.mailserver;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.Properties;


/**
 @since 04.05.2019 (11:11) */
public abstract class OstPstUtils implements LibraryLoader {
    
    
    private Properties properties = AppComponents.getProps();
    
    private MessageToUser messageToUser = new MessageLocal(OstPstUtils.class.getSimpleName());
    
    
    public OstPstUtils(String fileName) {
        this.fileName = fileName;
    }
    
    public OstPstUtils() {
        this.fileName = properties.getProperty(ConstantsFor.PR_OSTFILENAME, "test.ost");
    }
    
    private String fileName;
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public void showFileContent() {
        messageToUser.info("showFileContent()");
    }
    
    public String convertToPST() {
        messageToUser.info("convertToPST");
        return "ok";
    }
    
    public void copyierWithSave() {
        messageToUser.info("copyierWithSave");
    }
    
    @Override public Class<?> loadedClass() throws ClassNotFoundException {
        try {
            return libraryLoad(null, "ru.vachok.ostpst.MakeConvert");
        }
        catch (MalformedURLException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            throw new IllegalComponentStateException("13.05.2019 (15:18)");
        }
    }
}
