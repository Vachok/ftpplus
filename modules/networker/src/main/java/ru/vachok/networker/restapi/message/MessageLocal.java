// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.message;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.restapi.MessageToUser;

import java.text.MessageFormat;


public class MessageLocal implements MessageToUser {
    
    
    public static final String STR_BODYMSG = "bodyMsg='";
    
    private String bodyMsg = "NO BODY";
    
    private String titleMsg;
    
    private String headerMsg;
    
    
    public MessageLocal(String className) {
        this.headerMsg = className;
        this.titleMsg = ConstantsFor.getUpTime();
    }
    
    public MessageLocal(String headerMsg, String titleMsg) {
        this.headerMsg = headerMsg;
        this.titleMsg = titleMsg;
    }
    
    /**
     @deprecated since 02.04.2019 (17:25)
     */
    @Deprecated
    private MessageLocal() {
        this.headerMsg = "DEPRECATED";
    }
    
    public void errorAlert(String s) {
        this.bodyMsg = s;
        errorAlert(headerMsg, titleMsg, s);
        
    }
    
    public void igExc(Exception e) {
        LoggerFactory.getLogger(headerMsg).debug(e.getMessage(), e);
    }
    
    @Override
    public void warning(String headerMsg, String titleMsg, String bodyMsg) {
        final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(headerMsg);
        String msg = titleMsg + " : " + bodyMsg;
        logger.warning(msg);
    }
    
    @Override
    public void info(String bodyMsg) {
        this.bodyMsg = bodyMsg;
        infoNoTitles(this.bodyMsg);
    }
    
    @Override
    public void infoTimer(int timeSec, String bodyMsg) {
        throw new UnsupportedOperationException(headerMsg);
    }
    
    @Override
    public void errorAlert(String headerMsg, String titleMsg, String bodyMsg) {
        Logger logger = LoggerFactory.getLogger(headerMsg);
        this.headerMsg = headerMsg;
        this.titleMsg = titleMsg;
        this.bodyMsg = bodyMsg;
    
        String logRec = String.join(".", headerMsg, titleMsg);
        logger.error(MessageFormat.format("{0}: {1}", logRec, bodyMsg));
        
    }
    
    @Override
    public void info(String headerMsg, String titleMsg, String bodyMsg) {
        this.headerMsg = headerMsg;
        this.titleMsg = titleMsg;
        this.bodyMsg = bodyMsg;
    
        Logger logger = LoggerFactory.getLogger(headerMsg);
        String logRec = String.join(".", headerMsg, titleMsg);
        logger.info(MessageFormat.format("{0}: {1}", logRec, bodyMsg));
    }
    
    @Override
    public void infoNoTitles(String bodyMsg) {
        this.bodyMsg = bodyMsg;
        info(headerMsg, titleMsg, bodyMsg);
    }
    
    @Override
    public void error(String bodyMsg) {
        this.bodyMsg = bodyMsg;
        errorAlert(bodyMsg);
    }
    
    @Override
    public void error(String headerMsg, String titmeMsg, String bodyMsg) {
        errorAlert(headerMsg, titleMsg, bodyMsg);
    }
    
    @Override
    public void warn(String headerMsg, String titleMsg, String bodyMsg) {
        warning(headerMsg, titleMsg, bodyMsg);
    }
    
    public void warn(String bodyMsg) {
        this.bodyMsg = bodyMsg;
        warning(this.bodyMsg);
    }
    
    public void warning(String bodyMsg) {
        this.bodyMsg = bodyMsg;
        LoggerFactory.getLogger(headerMsg).warn(bodyMsg);
    }
    
    @Override
    public String confirm(String headerMsg, String titleMsg, String bodyMsg) {
        throw new UnsupportedOperationException(headerMsg);
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MessageLocal{");
        sb.append(STR_BODYMSG).append(bodyMsg).append('\'');
        sb.append(", headerMsg='").append(headerMsg).append('\'');
        sb.append(", titleMsg='").append(titleMsg).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
