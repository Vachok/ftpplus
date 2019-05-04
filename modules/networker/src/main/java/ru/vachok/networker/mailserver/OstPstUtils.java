// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.mailserver;


import ru.vachok.ostpst.MakeConvert;


/**
 @since 04.05.2019 (11:11) */
public class OstPstUtils implements MakeConvert {
    
    
    private String fileName;
    
    @Override public String convertToPST() {
        return null;
    }
    
    @Override public void showFileContent() {
    
    }
    
    @Override public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    @Override public long copyierWithSave() {
        return 0;
    }
}
