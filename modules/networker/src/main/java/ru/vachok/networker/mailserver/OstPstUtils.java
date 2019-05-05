// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.mailserver;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;
import ru.vachok.ostpst.MakeConvert;
import ru.vachok.ostpst.OstToPst;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;


/**
 @since 04.05.2019 (11:11) */
public class OstPstUtils implements MakeConvert {
    
    
    private static MessageToUser messageToUser = new MessageLocal(OstToPst.class.getSimpleName());
    
    private Properties properties = AppComponents.getProps();
    
    private MakeConvert ostToPst;
    
    
    public OstPstUtils(String fileName) {
        this.fileName = fileName;
        ostToPst = new OstToPst(fileName);
    }
    
    public OstPstUtils() {
        this.fileName = properties.getProperty(ConstantsFor.PR_OSTFILENAME, "test.ost");
        ostToPst = new OstToPst(fileName);
    }
    
    private String fileName;
    
    @Override public String convertToPST() {
        ostToPst.showFileContent();
        return FileSystemWorker.delTemp();
    }
    
    @Override public void showFileContent() {
        ostToPst.showFileContent();
    }
    
    public static void main(String[] args) {
        OstPstUtils utils = new OstPstUtils();
        Path rootFolderPath = Paths.get(".");
        String pathToPST = rootFolderPath.toAbsolutePath().toString().replace(".", "") + "mailconverter";
        utils.setFileName(pathToPST + "\\test.pst");
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
    
    @Override public void setFileName(String fileName) {
        this.fileName = fileName;
        properties.setProperty(ConstantsFor.PR_OSTFILENAME, fileName);
        new AppComponents().updateProps(properties);
    }
}
