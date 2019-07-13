// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.services;


import org.slf4j.LoggerFactory;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.exe.ThreadConfig;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;


/**
 * @since 26.08.2018 (12:29)
 * @see ru.vachok.networker.services.DBMessengerTest
 */
@SuppressWarnings("FeatureEnvy")
public class DBMessenger implements MessageToUser {
    
    
    private static final MessageToUser MESSAGE_TO_USER = new MessageLocal(DBMessenger.class.getSimpleName(), "Over database");
    
    private final Connection connection;
    
    private final ThreadConfig threadConfig;
    
    private static final String NOT_SUPPORTED = "Not Supported";
    
    Runnable dbSendRun;
    
    private String headerMsg;
    
    private String titleMsg = ConstantsFor.getUpTime();
    
    private String bodyMsg;
    
    public DBMessenger(String headerMsgClassNameAsUsual) {
        this.headerMsg = headerMsgClassNameAsUsual;
        this.bodyMsg = "null";
        this.connection = new AppComponents().connection(ConstantsFor.DBNAME_WEBAPP);
        threadConfig = AppComponents.threadConfig();
    }
    
    /**
     Главный посредник с {@link #dbSend(String, String, String)}
     <p>
     
     @param headerMsg заголовок
     @param titleMsg нвзвание
     @param bodyMsg тело
     */
    @Override
    public void errorAlert(String headerMsg, String titleMsg, String bodyMsg) {
        this.headerMsg = headerMsg;
        this.titleMsg = titleMsg;
        this.bodyMsg = bodyMsg;
        LoggerFactory.getLogger(headerMsg + ":" + titleMsg).error(bodyMsg);
        threadConfig.execByThreadConfig(()->dbSend(headerMsg, titleMsg, bodyMsg));
    }
    
    @Override
    public void infoNoTitles(String bodyMsg) {
        this.bodyMsg = bodyMsg;
        info(headerMsg, titleMsg, this.bodyMsg);
    }


    @Override
    public void info(String headerMsg, String titleMsg, String bodyMsg) {
        this.headerMsg = headerMsg;
        this.titleMsg = titleMsg;
        this.bodyMsg = bodyMsg;
        LoggerFactory.getLogger(headerMsg + " : " + titleMsg).info(bodyMsg);
    
        errorAlert(bodyMsg, titleMsg, bodyMsg);
    }
    
    @Override
    public void error(String bodyMsg) {
        this.bodyMsg = bodyMsg;
        errorAlert(headerMsg, titleMsg, bodyMsg);
    }

    @Override
    public void info(String bodyMsg) {
        this.bodyMsg = bodyMsg;
        info(headerMsg, titleMsg, bodyMsg);
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
        LoggerFactory.getLogger(headerMsg + ":" + titleMsg).warn(bodyMsg);
    
        errorAlert(headerMsg, titleMsg, bodyMsg);
    }
    
    @Override
    public void infoTimer(int i, String s) {
        throw new InvokeIllegalException(NOT_SUPPORTED);
    }
    
    @Override
    public void warn(String bodyMsg) {
        this.bodyMsg = bodyMsg;
        warn(headerMsg, titleMsg, bodyMsg);
    }
    
    @Override
    public void warning(String headerMsg, String titleMsg, String bodyMsg) {
        this.headerMsg = headerMsg;
        this.titleMsg = titleMsg;
        this.bodyMsg = bodyMsg;
        warn(headerMsg, titleMsg, bodyMsg);
    }
    
    @Override
    public void warning(String bodyMsg) {
        this.bodyMsg = bodyMsg;
        warn(headerMsg, titleMsg, bodyMsg);
    }
    
    @Override
    public String confirm(String s, String s1, String s2) {
        throw new InvokeIllegalException(NOT_SUPPORTED);
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DBMessenger{");
        sb.append(MessageLocal.TOSTRING_HEADER_MSG).append(headerMsg).append('\'');
        sb.append(MessageLocal.TOSTRING_TITLE_MSG).append(titleMsg).append('\'');
        sb.append(MessageLocal.TOSTRING_BODY_MSG).append(bodyMsg).append('\'');
        sb.append('}');
        return sb.toString();
    }
    
    private String dbSend(String classname, String msgtype, String msgvalue) {
        final String sql = "insert into ru_vachok_networker (classname, msgtype, msgvalue, pc) values (?,?,?,?)";
    
        String msgType = MessageFormat
            .format("{0} | \nMinutes ticked... {1}", msgtype, TimeUnit.SECONDS.toMinutes(ConstantsFor.getMyTime()));
        String pc = ConstantsFor.thisPC() + ": " + ConstantsFor.getUpTime();
        
        try (Connection c = new AppComponents().connection(ConstantsFor.DBPREFIX + "webapp");
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, classname);
            p.setString(2, msgType);
            p.setString(3, msgvalue);
            p.setString(4, pc);
            return MessageFormat.format("{0} executeUpdate.\nclassname aka headerMsg - {1}: msgType aka titleMsg - {2}\nBODY: {3}", p
                .executeUpdate(), classname, msgType, msgvalue, pc);
        }
        catch (SQLException e) {
            return FileSystemWorker.error(getClass().getSimpleName() + ".dbSend", e);
        }
    }
}