// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.mailserver;


import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.ostpst.MakeConvert;
import ru.vachok.ostpst.OstToPst;

import java.awt.*;
import java.io.File;
import java.util.Properties;


/**
 @since 04.05.2019 (11:11) */
public class OstPstUtils implements MakeConvert {
    
    
    private Properties properties = AppComponents.getProps();
    
    private MakeConvert ostToPst = new OstToPst();
    
    
    public OstPstUtils(String fileName) {
        this.fileName = fileName;
    }
    
    public OstPstUtils() {
        this.fileName = properties.getProperty(ConstantsFor.PR_OSTFILENAME, "test.ost");
    }
    
    private String fileName;
    
    @Override public String convertToPST() {
        throw new IllegalComponentStateException("04.05.2019 (11:21)");
    }
    
    @Override public void showFileContent() {
    
        ostToPst.showFileContent();
    }
    
    @Override public void setFileName(String fileName) {
        this.fileName = fileName;
        properties.setProperty(ConstantsFor.PR_OSTFILENAME, fileName);
    }
    
    @Override public long copyierWithSave() {
        long fileWriteByte;
        long fileReadByte = Long.parseLong(properties.getProperty(ConstantsFor.PR_OSTREAD, "0"));
        ostToPst.setFileName(fileName);
        long fileLen = new File(fileName).length();
        while (fileLen > 0) {
            fileWriteByte = ostToPst.copyierWithSave();
            fileLen = fileLen - fileWriteByte;
            properties.setProperty(ConstantsFor.PR_OSTWRITE, String.valueOf(fileWriteByte));
            properties.setProperty(ConstantsFor.PR_OSTREAD, String.valueOf(fileLen));
        }
        return fileReadByte;
    }
}
