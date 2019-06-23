package ru.vachok.ostpst.api;


import java.awt.*;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;


public class MessengerOST implements MessageToUser {
    
    
    private String name;
    
    public MessengerOST(String loggerName) {
        this.name = loggerName;
    }
    
    @Override public void error(String message) {
        this.error(name, "", message);
    }
    
    @Override public void info(String loggerName, String msgTitle, String msgBody) {
        this.name = loggerName;
        Logger logger = Logger.getLogger(loggerName);
        for (Handler loggerHandler : logger.getHandlers()) {
            logger.removeHandler(loggerHandler);
        }
        logger.addHandler(new ConsoleHandler());
        logger.info(msgTitle + ": " + msgBody);
    }
    
    @Override public void info(String msgBody) {
        this.info(name, ": ", msgBody);
    }
    
    @Override public void warn(String contacts) {
        throw new IllegalComponentStateException("19.06.2019 (12:35)");
    }
    
    @Override public String confirm(String name, String msgTitle, String msgBody) {
        throw new IllegalComponentStateException("19.06.2019 (12:35)");
    }
    
    @Override public void error(String name, String msgTitle, String msgBody) {
        this.name = name;
        Logger logger = Logger.getLogger(name);
        try {
            logger.addHandler(new FileHandler(name + ".log"));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        logger.severe(msgTitle + ": " + msgBody);
    }
}
