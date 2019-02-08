package ru.vachok.networker.services;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vachok.messenger.MessageToUser;

/**
 Локальная имплементация {@link MessageToUser}
 <p>

 @since 30.01.2019 (17:05) */
public class MessageLocal implements MessageToUser {

    private String bodyMsg = "NO BODY";

    private String titleMsg = "";

    private String headerMsg = "";

    public void warning(String bodyMsg) {
        Logger logger = LoggerFactory.getLogger(headerMsg);
        this.bodyMsg = bodyMsg;
        String join = String.join(" ", headerMsg, titleMsg, bodyMsg);
        logger.warn(join);
    }

    public void warn(String s) {
        this.bodyMsg = s;
        warning(bodyMsg);
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
        String logRec = String.join(" ", headerMsg, titleMsg, bodyMsg);
        logger.error(logRec);
    }

    @Override
    public void info(String headerMsg, String titleMsg, String bodyMsg) {
        Logger logger = LoggerFactory.getLogger(headerMsg);
        this.headerMsg = headerMsg;
        this.titleMsg = titleMsg;
        this.bodyMsg = bodyMsg;
        String logRec = String.join(" ", headerMsg, titleMsg, bodyMsg);
        logger.info(logRec);
    }

    @Override
    public void infoNoTitles(String s) {
        this.bodyMsg = s;
        info(headerMsg, titleMsg, s);
    }

    @Override
    public void infoTimer(int i, String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String confirm(String s, String s1, String s2) {
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
