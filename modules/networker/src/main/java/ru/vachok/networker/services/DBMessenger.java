// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.services;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/**
 * @since 26.08.2018 (12:29)
 * @see ru.vachok.networker.services.DBMessengerTest
 */
public class DBMessenger implements MessageToUser {
    
    
    private static final MessageToUser MESSAGE_TO_USER = new MessageLocal(DBMessenger.class.getSimpleName(), "Over database");
    
    private final Connection connection;
    
    private ExecutorService thrConfig;
    
    private static final String NOT_SUPPORTED = "Not Supported";
    
    private String headerMsg;
    
    private String titleMsg;
    
    private String bodyMsg;
    
    public DBMessenger(String headerMsgClassNameAsUsual) {
        this.titleMsg = ConstantsFor.getUpTime() + "\n" + ConstantsFor.getMemoryInfo();
        this.headerMsg = headerMsgClassNameAsUsual;
        
        this.thrConfig = Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor());
        this.connection = new AppComponents().connection(ConstantsFor.DBNAME_WEBAPP);
        
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
        this.bodyMsg = bodyMsg;
        info(headerMsg, titleMsg, this.bodyMsg);
        MESSAGE_TO_USER.info(this.bodyMsg);
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
        infoNoTitles(this.bodyMsg);
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
        throw new InvokeIllegalException(NOT_SUPPORTED);
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
        throw new InvokeIllegalException(NOT_SUPPORTED);
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DBMessenger{");
        sb.append("bodyMsg='").append(bodyMsg).append('\'');
        sb.append(", headerMsg='").append(headerMsg).append('\'');
        sb.append(", titleMsg='").append(titleMsg).append('\'');
        sb.append('}');
        return sb.toString();
    }
    
    @SuppressWarnings("SpellCheckingInspection")
    private void dbSend(String classname, String msgtype, String msgvalue) {
        final String sql = "insert into ru_vachok_networker (classname, msgtype, msgvalue, pc) values (?,?,?,?)";
        
        try (Connection c = new AppComponents().connection(ConstantsFor.DBPREFIX + "webapp");
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, classname);
            p.setString(2, MessageFormat.format("{0}\n{1}", msgtype, TimeUnit.MILLISECONDS.toHours(ConstantsFor.getMyTime()) / ConstantsFor.ONE_HOUR_IN_MIN));
            p.setString(3, msgvalue);
            p.setString(4, ConstantsFor.thisPC() + ": " + ConstantsFor.getUpTime());
            p.executeUpdate();
        }
        catch (SQLException e) {
            MESSAGE_TO_USER.error(FileSystemWorker.error(getClass().getSimpleName() + ".dbSend", e));
        }
    }
}