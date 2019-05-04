// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.mailserver;


import ru.vachok.ostpst.MakeConvert;

import java.awt.*;


/**
 @since 04.05.2019 (11:11) */
public class OstPstUtils implements MakeConvert {
    
    
    private String fileName;
    
    @Override public String convertToPST() {
        throw new IllegalComponentStateException("04.05.2019 (11:21)");
    }
    
    @Override public void showFileContent() {
        throw new IllegalComponentStateException("04.05.2019 (11:21)");
    }
    
    @Override public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    @Override public long copyierWithSave() {
        throw new IllegalComponentStateException("04.05.2019 (11:21)");
    }
}
