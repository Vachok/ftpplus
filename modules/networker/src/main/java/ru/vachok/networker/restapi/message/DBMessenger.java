// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.message;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.restapi.database.DataConnectTo;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.time.LocalTime;
import java.util.concurrent.Semaphore;


/**
 @see ru.vachok.networker.restapi.message.DBMessengerTest
 @since 26.08.2018 (12:29) */
public class DBMessenger implements MessageToUser {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, DBMessenger.class.getSimpleName());
    
    private final Semaphore dbSemaphore = new Semaphore(1);
    
    private String headerMsg;
    
    private String titleMsg;
    
    private String bodyMsg;
    
    private boolean isInfo = true;
    
    public void setHeaderMsg(String headerMsg) {
        this.headerMsg = headerMsg;
        Thread.currentThread().setName("DBMsg-" + this.hashCode());
    }
    
    @Contract(pure = true)
    DBMessenger(String headerMsg) {
        this.headerMsg = headerMsg;
        this.titleMsg = DataConnectTo.getInstance(DataConnectTo.TESTING).toString();
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DBMessenger{");
        sb.append("titleMsg='").append(titleMsg).append('\'');
        String sendResult = "No sends ";
        sb.append(", sendResult='").append(sendResult).append('\'');
        sb.append(", isInfo=").append(isInfo);
        sb.append(", headerMsg='").append(headerMsg).append('\'');
        sb.append(", bodyMsg='").append(bodyMsg).append('\'');
        sb.append('}');
        return sb.toString();
    }
    
    @Override
    public void errorAlert(String headerMsg, String titleMsg, String bodyMsg) {
        this.headerMsg = headerMsg;
        this.titleMsg = titleMsg;
        this.bodyMsg = bodyMsg;
        this.isInfo = false;
        AppComponents.threadConfig().execByThreadConfig(this::dbSend);
    }
    
    @Override
    public void info(String headerMsg, String titleMsg, String bodyMsg) {
        this.headerMsg = headerMsg;
        this.titleMsg = titleMsg;
        this.bodyMsg = bodyMsg;
        this.isInfo = true;
        AppComponents.threadConfig().execByThreadConfig(this::dbSend);
    }
    
    @Override
    public void infoNoTitles(String bodyMsg) {
        this.bodyMsg = bodyMsg;
        info(this.headerMsg, this.titleMsg, this.bodyMsg);
    }
    
    @Override
    public void info(String bodyMsg) {
        this.bodyMsg = bodyMsg;
        info(this.headerMsg, this.titleMsg, bodyMsg);
    }
    
    @Override
    public void error(String bodyMsg) {
        this.bodyMsg = bodyMsg;
        this.headerMsg = PropertiesNames.ERROR;
        this.titleMsg = UsefulUtilities.thisPC();
        errorAlert(this.headerMsg, this.titleMsg, bodyMsg);
    }
    
    @Override
    public void error(String headerMsg, String titleMsg, String bodyMsg) {
        this.headerMsg = headerMsg;
        this.titleMsg = titleMsg;
        this.bodyMsg = bodyMsg;
        errorAlert(this.headerMsg, this.titleMsg, bodyMsg);
    }
    
    @Override
    public void warn(String headerMsg, String titleMsg, String bodyMsg) {
        this.headerMsg = headerMsg;
        this.titleMsg = MessageFormat.format("{0} (WARN)", titleMsg);
        this.bodyMsg = bodyMsg;
        AppComponents.threadConfig().execByThreadConfig(this::dbSend);
    }
    
    @Override
    public void warn(String bodyMsg) {
        this.bodyMsg = bodyMsg;
        warn(this.headerMsg, this.titleMsg, bodyMsg);
    }
    
    @Override
    public void warning(String headerMsg, String titleMsg, String bodyMsg) {
        this.headerMsg = headerMsg;
        this.titleMsg = titleMsg;
        this.bodyMsg = bodyMsg;
        warn(this.headerMsg, this.titleMsg, bodyMsg);
    }
    
    @Override
    public void warning(String bodyMsg) {
        this.bodyMsg = bodyMsg;
        warn(this.headerMsg, this.titleMsg, bodyMsg);
    }
    
    private void dbSend() {
        String sql = "insert into log.networker (classname, msgtype, msgvalue, pc, stack, upstring) values (?,?,?,?,?,?)";
        String pc = UsefulUtilities.thisPC() + " : " + UsefulUtilities.getUpTime();
        long upTime = ManagementFactory.getRuntimeMXBean().getUptime();
        
        if (!isInfo) {
            sql = sql.replace(ConstantsFor.PREF_NODE_NAME, "errors");
        }
        if (dbSemaphore.tryAcquire()) {
            messageToUser.info(this.getClass().getSimpleName(), dbSemaphore.toString(), Thread.currentThread().getState().name());
            dbConnect(sql, pc, getStack());
        }
        else if (dbSemaphore.hasQueuedThreads()) {
            messageToUser.warn(this.getClass().getSimpleName(), MessageFormat.format("{0} uptime", upTime), Thread.currentThread().getState().name());
        }
        else {
            messageToUser.error(this.getClass().getSimpleName(), dbSemaphore.toString(), Thread.currentThread().getState().name());
        }
    }
    
    private void dbConnect(String sql, String pc, String stack) {
        try (Connection con = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection("log")) {
            try (PreparedStatement p = con.prepareStatement(sql)) {
                p.setString(1, this.headerMsg);
                p.setString(2, this.titleMsg);
                p.setString(3, this.bodyMsg);
                p.setString(4, pc);
                p.setString(5, stack);
                p.setString(6, String.valueOf(LocalTime.now()));
                p.executeUpdate();
            }
        }
        catch (SQLException | RuntimeException e) {
            if (!e.getMessage().contains(ConstantsFor.ERROR_DUPLICATEENTRY)) {
                notDuplicate();
            }
        }
        finally {
            dbSemaphore.release();
            messageToUser.info(DBMessenger.class.getSimpleName(), dbSemaphore.availablePermits() + " available permits", Thread.currentThread().getState().name());
        }
    }
    
    private void notDuplicate() {
        MessageToUser msgToUsr = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, this.getClass().getSimpleName());
        String titleBody = MessageFormat.format("Title: {0}\nBody: {1}", this.titleMsg, this.bodyMsg);
        msgToUsr.warn(this.getClass().getSimpleName() + "->" + this.headerMsg, "send log error!", titleBody);
        this.headerMsg = "";
        this.bodyMsg = "";
        this.titleMsg = "";
    }
    
    private @NotNull String getStack() {
        StringBuilder stringBuilder = new StringBuilder();
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        stringBuilder.append(AbstractForms.fromArray(threadMXBean.dumpAllThreads(true, true)));
        return stringBuilder.toString();
    }
}