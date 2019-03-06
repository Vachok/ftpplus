package ru.vachok.networker.services;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;

/**
 Локальная имплементация {@link MessageToUser}
 <p>

 @since 30.01.2019 (17:05) */
public class MessageLocal implements MessageToUser {

    private String bodyMsg = "NO BODY";
    
    private String titleMsg = ConstantsFor.getUpTime();
    
    private String headerMsg = "Header from " + getClass().getSimpleName() + ":" + ConstantsFor.thisPC();
    
    public MessageLocal(String className) {
        this.headerMsg = className;
    }
    
    public MessageLocal() {
    
    }
    
    public void warning(String bodyMsg) {
        Logger logger = LoggerFactory.getLogger(headerMsg);
        this.bodyMsg = bodyMsg;
        String join = String.join(" ", headerMsg, titleMsg, bodyMsg);
        logger.warn(join);
    }
    
    @Override
    public void info(String bodyMsg) {
        infoNoTitles(this.bodyMsg);
    }

    @Override
    public void infoTimer(int timeSec, String bodyMsg) {
        throw new UnsupportedOperationException(headerMsg);
    }

    public void errorAlert(String s) {
        this.bodyMsg = s;
        errorAlert(headerMsg, titleMsg, s);
    }

    @Override
    public void errorAlert(String headerMsg, String titleMsg, String bodyMsg) {
        Logger logger = LoggerFactory.getLogger(headerMsg);
        this.headerMsg = headerMsg;
        this.titleMsg = titleMsg;
        this.bodyMsg = bodyMsg;
        String logRec = String.join("! ", headerMsg, titleMsg, bodyMsg);
        logger.error(logRec);
    }

    @Override
    public void info(String headerMsg, String titleMsg, String bodyMsg) {
        Logger logger = LoggerFactory.getLogger(headerMsg);
        this.headerMsg = headerMsg;
        this.titleMsg = titleMsg;
        this.bodyMsg = bodyMsg;
        String logRec = String.join(", ", headerMsg, titleMsg, bodyMsg);
        logger.info(logRec);
    }

    @Override
    public void infoNoTitles(String bodyMsg) {
        this.bodyMsg = bodyMsg;
        this.titleMsg = "infoNoTitles";
        info(headerMsg, titleMsg, bodyMsg);
    }

    @Override
    public void error(String bodyMsg) {
        errorAlert(bodyMsg);
    }

    @Override
    public void error(String headerMsg, String titmeMsg, String bodyMsg) {
        errorAlert(headerMsg, titmeMsg, bodyMsg);
    }

    @Override
    public void warn(String headerMsg, String titleMsg, String bodyMsg) {
        warning(headerMsg, titleMsg, titleMsg);
    }
    
    public void warn(String bodyMsg) {
        this.bodyMsg = bodyMsg;
        warning(this.bodyMsg);
    }

    @Override
    public void warning(String headerMsg, String titleMsg, String bodyMsg) {
        Logger logger = LoggerFactory.getLogger(headerMsg);
        String msg = titleMsg + " : " + bodyMsg;
        logger.warn(msg);
    }

    @Override
    public String confirm(String headerMsg, String titleMsg, String bodyMsg) {
        throw new UnsupportedOperationException(headerMsg);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MessageLocal{");
        sb.append("bodyMsg='").append(bodyMsg).append('\'');
        sb.append(", headerMsg='").append(headerMsg).append('\'');
        sb.append(", titleMsg='").append(titleMsg).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
