// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.mailserver;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.services.MessageLocal;
import ru.vachok.ostpst.MakeConvert;
import ru.vachok.ostpst.OstToPst;

import java.util.Properties;


/**
 @since 04.05.2019 (11:11) */
public class OstPstUtils  {
    
    
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
    
}
