package ru.vachok.ostpst.api;


/**
 @since 19.06.2019 (11:46) */
public interface MessageToUser {
    
    
    void error(String message);
    void info(String loggerName, String msgTitle, String msgBody);
    void info(String msgBody);
    void warn(String contacts);
    String confirm(String name, String msgTitle, String msgBody);
    void error(String name, String msgTitle, String msgBody);
}
