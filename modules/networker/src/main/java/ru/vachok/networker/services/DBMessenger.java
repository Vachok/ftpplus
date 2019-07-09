// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.services;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.IllegalInvokeEx;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * @since 26.08.2018 (12:29)
 */
public class DBMessenger implements MessageToUser {
    
    
    private ExecutorService thrConfig;
    
    private static final String NOT_SUPPORTED = "Not Supported";
    
    private String headerMsg;
    
    private String titleMsg;
    
    private String bodyMsg;
    
    public DBMessenger(String titleMsg) {
        this.headerMsg = ConstantsFor.thisPC();
        this.titleMsg = titleMsg;
        this.bodyMsg = ConstantsFor.getMemoryInfo();
        this.thrConfig = Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor());
        ;
    }
    
    @Override
    public void errorAlert(String headerMsg, String titleMsg, String bodyMsg) {
        this.headerMsg = headerMsg;
        this.titleMsg = titleMsg;
        this.bodyMsg = bodyMsg;
        
        Logger logger = LoggerFactory.getLogger(headerMsg + ":" + titleMsg);
        Runnable errSend = ()->dbSend(headerMsg, titleMsg, bodyMsg);
        
        thrConfig.execute(errSend);
        logger.error(bodyMsg);
    }
    
    @Override
    public void infoNoTitles(String bodyMsg) {
        Runnable info = ()->dbSend(headerMsg, titleMsg, bodyMsg);
        thrConfig.execute(info);
    }


    @Override
    public void info(String headerMsg, String titleMsg, String bodyMsg) {
        this.headerMsg = headerMsg;
        this.titleMsg = titleMsg;
        this.bodyMsg = bodyMsg;
    
        final Runnable dbSendRun = ()->dbSend(headerMsg, titleMsg, bodyMsg);
        thrConfig.execute(dbSendRun);
    }
    
    @Override
    public void error(String bodyMsg) {
        this.bodyMsg = bodyMsg;
        
        errorAlert(headerMsg, titleMsg, bodyMsg);
    }

    @Override
    public void info(String bodyMsg) {
        this.bodyMsg = bodyMsg;
        infoNoTitles(bodyMsg);
    }
    
    @Override
    public void error(String headerMsg, String titleMsg, String bodyMsg) {
        this.headerMsg = headerMsg;
        this.titleMsg = titleMsg;
        this.bodyMsg = bodyMsg;
        
        errorAlert(headerMsg, titleMsg, bodyMsg);
    }
    
    @Override
    public void warn(String headerMsg, String titleMsg, String bodyMsg) {
        this.headerMsg = headerMsg;
        this.titleMsg = titleMsg;
        this.bodyMsg = bodyMsg;
        
        Logger logger = LoggerFactory.getLogger(headerMsg + ":" + titleMsg);
        info(headerMsg, titleMsg, bodyMsg);
        logger.warn(bodyMsg);
    }
    
    @Override
    public void infoTimer(int i, String s) {
        throw new IllegalInvokeEx(NOT_SUPPORTED);
    }
    
    @Override
    public void warn(String bodyMsg) {
        this.bodyMsg = bodyMsg;
        
        Logger logger = LoggerFactory.getLogger(headerMsg + ":" + titleMsg);
        info(bodyMsg);
        logger.warn(bodyMsg);
    }
    
    @Override
    public void warning(String headerMsg, String titleMsg, String bodyMsg) {
        Logger logger = LoggerFactory.getLogger(headerMsg + ":" + titleMsg);
        warn(headerMsg, titleMsg, bodyMsg);
        logger.warn(bodyMsg);
    }
    
    @Override
    public void warning(String bodyMsg) {
        warn(bodyMsg);
    }
    
    @Override
    public String confirm(String s, String s1, String s2) {
        throw new IllegalInvokeEx(NOT_SUPPORTED);
    }
    
    private void dbSend(String headerMsg, String titleMsg, String bodyMsg) {
        final String sql = "insert into ru_vachok_networker (classname, msgtype, msgvalue, pc) values (?,?,?,?)";
        
        try (Connection c = new AppComponents().connection(ConstantsFor.DBPREFIX + "webapp");
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, headerMsg);
            p.setString(2, titleMsg);
            p.setString(3, bodyMsg);
            p.setString(4, ConstantsFor.thisPC() + " up: " + ConstantsFor.getUpTime());
            System.out.println(getClass().getSimpleName() + " p.executeUpdate = " + p.executeUpdate());
        }
        catch (SQLException | IOException e) {
            System.err.println(e.getMessage());
        }
    }
}