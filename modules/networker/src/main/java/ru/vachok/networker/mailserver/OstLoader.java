// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.mailserver;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.abstr.MakeConvert;
import ru.vachok.networker.restapi.message.MessageLocal;


public class OstLoader implements LibraryLoader, MakeConvert {
    
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    private String fileName;
    
    public OstLoader(String fileName) {
        this.fileName = fileName;
    }
    
    @Override public Class<?> loadedClass() throws ClassNotFoundException {
        return null;
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
}
