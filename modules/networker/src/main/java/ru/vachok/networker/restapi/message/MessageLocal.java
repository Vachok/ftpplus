// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.message;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.componentsrepo.NetworkerStopException;

import java.text.MessageFormat;
import java.util.Objects;


public class MessageLocal implements MessageToUser {
    
    
    private static final String STR_BODYMSG = "bodyMsg='";
    
    private String bodyMsg = "NO BODY";
    
    private String titleMsg;
    
    private String headerMsg;
    
    public void setBodyMsg(String bodyMsg) {
        this.bodyMsg = bodyMsg;
        Thread.currentThread().setName(bodyMsg);
    }
    
    public void setTitleMsg(String titleMsg) {
        this.titleMsg = titleMsg;
    }
    
    @Override
    public void setHeaderMsg(String header) {
        this.headerMsg = header;
    }
    
    public MessageLocal(String className) {
        this.headerMsg = className;
        this.titleMsg = "constructing...";
        Thread.currentThread().setName(className);
    }
    
    @Contract(pure = true)
    public MessageLocal(String headerMsg, String titleMsg) {
        this.headerMsg = headerMsg;
        this.titleMsg = titleMsg;
        Thread.currentThread().setName(headerMsg);
    }
    
    public void igExc(Exception e) {
        LoggerFactory.getLogger(headerMsg).debug(e.getMessage(), e);
    }
    
    public void errorAlert(String s) {
        this.bodyMsg = s;
        errorAlert(headerMsg, titleMsg, s);
        
    }
    
    @Override
    public void errorAlert(String headerMsg, String titleMsg, String bodyMsg) {
        this.headerMsg = headerMsg;
        this.titleMsg = titleMsg;
        this.bodyMsg = bodyMsg;
    
        log("err");
    }
    
    @Override
    public void info(String headerMsg, String titleMsg, String bodyMsg) {
        this.headerMsg = headerMsg;
        this.titleMsg = titleMsg;
        this.bodyMsg = bodyMsg;
        
        log("info");
    }
    @Override
    public void error(String headerMsg, String titleMsg, String bodyMsg) {
        this.headerMsg = headerMsg;
        this.bodyMsg = bodyMsg;
        this.titleMsg = titleMsg;
    
        errorAlert(headerMsg, this.titleMsg, bodyMsg);
    }
    
    @Override
    public void warning(String headerMsg, String titleMsg, String bodyMsg) {
        warn(headerMsg, titleMsg, bodyMsg);
    }
    
    @Override
    public void info(String bodyMsg) {
        this.bodyMsg = bodyMsg;
        infoNoTitles(this.bodyMsg);
    }
    
    public void loggerFine(String bodyMsg) {
        Logger logger = LoggerFactory.getLogger(bodyMsg);
        logger.debug(bodyMsg);
    }
    
    private void log(@NotNull String typeLog) {
        Thread.currentThread().setName(Objects.requireNonNull(headerMsg, ()->this.getClass().getSimpleName() + " SET HEADER!"));
        
        Logger logger = LoggerFactory.getLogger(headerMsg);
        String msg;
        if (typeLog.equals("warn")) {
            msg = MessageFormat.format("|||{0}: {1} , {2} |||", headerMsg, titleMsg, bodyMsg);
            logger.warn(msg);
        }
        if (typeLog.equals("info")) {
            msg = MessageFormat.format("{0} : {1}", titleMsg, bodyMsg);
            logger.info(msg);
        }
        if (typeLog.equals("err")) {
            msg = MessageFormat.format("!*** {0} ERROR. {1}, used {0}, but : {2} ***!", headerMsg, titleMsg, bodyMsg);
            logger.error(msg);
    
        }
        try {
            writeToFile(typeLog);
        }
        catch (NetworkerStopException e) {
            System.err.println("Log not written");
        }
        finally {
        
        }
    }
    
    private void writeToFile(@NotNull String typeLog) throws NetworkerStopException {
        String[] messages = {bodyMsg, titleMsg, headerMsg};
        for (String s : messages) {
            if (s == null) {
                throw new NetworkerStopException(getClass().getSimpleName(), "writeToFile", 133);
            }
        }
        int logLevel = Integer.parseInt(AppComponents.getProps().getProperty("loglevel", String.valueOf(1)));
        switch (typeLog) {
            case "err":
                MessageToUser.getInstance(MessageToUser.FILE, headerMsg).errorAlert(headerMsg, titleMsg, bodyMsg);
            case "warn":
                MessageToUser.getInstance(MessageToUser.FILE, headerMsg).warning(headerMsg, titleMsg, bodyMsg);
        }
        if (logLevel > 1 & typeLog.equalsIgnoreCase("info")) {
            MessageToUser.getInstance(MessageToUser.FILE, headerMsg).error(headerMsg, titleMsg, bodyMsg);
        }
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
    public String toString() {
        final StringBuilder sb = new StringBuilder("MessageLocal{");
        sb.append(STR_BODYMSG).append(bodyMsg).append('\'');
        sb.append(", headerMsg='").append(headerMsg).append('\'');
        sb.append(", titleMsg='").append(titleMsg).append('\'');
        sb.append('}');
        return sb.toString();
    }
    
    @Override
    public void warn(String headerMsg, String titleMsg, String bodyMsg) {
        this.headerMsg = headerMsg;
        this.titleMsg = titleMsg;
        this.bodyMsg = bodyMsg;
        
        log("warn");
    }
    
    @Override
    public void warn(String bodyMsg) {
        this.bodyMsg = bodyMsg;
        warning(this.bodyMsg);
    }
    
    @Override
    public void warning(String bodyMsg) {
        this.bodyMsg = bodyMsg;
        warn(headerMsg, titleMsg, bodyMsg);
    }
    

    

}
