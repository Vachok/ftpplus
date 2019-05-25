// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.fileworks;


import com.pff.PSTException;
import com.pff.PSTFile;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.ostpst.ConstantsOst;
import ru.vachok.ostpst.utils.CharsetEncoding;
import ru.vachok.ostpst.utils.FileSystemWorkerOST;

import java.io.IOException;


/**
 @since 23.05.2019 (16:23) */
public class PSTFileNameConverter {
    
    
    private MessageToUser messageToUser = new MessageCons(getClass().getSimpleName());
    
    PSTFile getPSTFile(String fileName) {
        System.setProperty(ConstantsOst.STR_ENCODING, "UTF8");
        try {
            String anotherCharset = new CharsetEncoding("windows-1251", "UTF-8").getStrInAnotherCharset(fileName);
            return new PSTFile(anotherCharset);
        }
        catch (PSTException | IOException e) {
            return getPSTFileNoException(fileName);
        }
    }
    
    private PSTFile getPSTFileNoException(String fileName) {
        PSTFile pstFile = null;
        try {
            pstFile = new PSTFile(fileName);
        }
        catch (PSTException | IOException e) {
            messageToUser.error(FileSystemWorkerOST.error(getClass().getSimpleName() + ".getPSTFileNoException", e));
        }
        return pstFile;
    }
}
