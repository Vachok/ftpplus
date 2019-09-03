// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.message;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.DataConnectTo;
import ru.vachok.networker.restapi.MessageToUser;

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.sql.*;
import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 @see ru.vachok.networker.restapi.message.DBMessengerTest
 @since 26.08.2018 (12:29) */
public class DBMessenger implements MessageToUser, Serializable {
    
    
    private static final String NOT_SUPPORTED = "Not Supported";
    
    private static DBMessenger dbMessenger = new DBMessenger("STATIC");
    
    private DataConnectTo dataConnectTo = DataConnectTo.getI(ConstantsFor.DBBASENAME_U0466446_WEBAPP);
    
    private String headerMsg;
    
    private String titleMsg;
    
    private String bodyMsg;
    
    private String sendResult = "No sends ";
    
    private boolean isInfo = true;
    
    private DBMessenger(String headerMsg) {
        Thread.currentThread().setName("dblg " + hashCode());
        this.headerMsg = headerMsg;
    }
    
    @Override
    public int hashCode() {
        int result = dataConnectTo.hashCode();
        result = 31 * result + (headerMsg != null ? headerMsg.hashCode() : 0);
        result = 31 * result + (titleMsg != null ? titleMsg.hashCode() : 0);
        result = 31 * result + (bodyMsg != null ? bodyMsg.hashCode() : 0);
        result = 31 * result + sendResult.hashCode();
        result = 31 * result + (isInfo ? 1 : 0);
        return result;
    }
    
    @Override
    public boolean equals(Object o) {
        if (dbMessenger == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        
        DBMessenger messenger = (DBMessenger) o;
        
        if (isInfo != messenger.isInfo) {
            return false;
        }
        if (!dataConnectTo.equals(messenger.dataConnectTo)) {
            return false;
        }
        if (headerMsg != null ? !headerMsg.equals(messenger.headerMsg) : messenger.headerMsg != null) {
            return false;
        }
        if (titleMsg != null ? !titleMsg.equals(messenger.titleMsg) : messenger.titleMsg != null) {
            return false;
        }
        if (bodyMsg != null ? !bodyMsg.equals(messenger.bodyMsg) : messenger.bodyMsg != null) {
            return false;
        }
        return sendResult.equals(messenger.sendResult);
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DBMessenger{");
        sb.append("titleMsg='").append(titleMsg).append('\'');
        sb.append(", sendResult='").append(sendResult).append('\'');
        sb.append(", isInfo=").append(isInfo);
        sb.append(", headerMsg='").append(headerMsg).append('\'');
        sb.append(", bodyMsg='").append(bodyMsg).append('\'');
        sb.append('}');
        return sb.toString();
    }
    
    @Contract(pure = true)
    public static DBMessenger getInstance(String name) {
        Thread.currentThread().setName("DBMsg-" + dbMessenger.hashCode());
        dbMessenger.headerMsg = name;
        return dbMessenger;
    }
    
    public void setHeaderMsg(String headerMsg) {
        dbMessenger.headerMsg = headerMsg;
        Thread.currentThread().setName("DBMsg-" + dbMessenger.hashCode());
    }
    
    @Override
    public void errorAlert(String headerMsg, String titleMsg, String bodyMsg) {
        dbMessenger.headerMsg = headerMsg;
        dbMessenger.titleMsg = "ERROR! " + titleMsg;
        dbMessenger.bodyMsg = bodyMsg;
        LoggerFactory.getLogger(headerMsg + ":" + titleMsg).error(bodyMsg);
        dbMessenger.isInfo = false;
        AppComponents.threadConfig().execByThreadConfig(dbMessenger::dbSend);
    }
    
    @Override
    public void infoNoTitles(String bodyMsg) {
        dbMessenger.bodyMsg = bodyMsg;
        info(headerMsg, titleMsg, dbMessenger.bodyMsg);
    }
    
    @Override
    public void info(String headerMsg, String titleMsg, String bodyMsg) {
        dbMessenger.headerMsg = headerMsg;
        dbMessenger.titleMsg = titleMsg;
        dbMessenger.bodyMsg = bodyMsg;
        dbMessenger.isInfo = true;
        AppComponents.threadConfig().execByThreadConfig(dbMessenger::dbSend);
    }
    
    @Override
    public void error(String bodyMsg) {
        dbMessenger.bodyMsg = bodyMsg;
        errorAlert(headerMsg, titleMsg, bodyMsg);
    }
    
    @Override
    public void info(String bodyMsg) {
        dbMessenger.bodyMsg = bodyMsg;
        info(headerMsg, titleMsg, bodyMsg);
    }
    
    @Override
    public void error(String headerMsg, String titleMsg, String bodyMsg) {
        dbMessenger.headerMsg = headerMsg;
        dbMessenger.titleMsg = titleMsg;
        dbMessenger.bodyMsg = bodyMsg;
        errorAlert(headerMsg, titleMsg, bodyMsg);
    }
    
    @Override
    public void warn(String headerMsg, String titleMsg, String bodyMsg) {
        dbMessenger.headerMsg = headerMsg;
        dbMessenger.titleMsg = "WARNING: " + titleMsg;
        dbMessenger.bodyMsg = bodyMsg;
        AppComponents.threadConfig().execByThreadConfig(dbMessenger::dbSend);
    }
    
    @Override
    public void warn(String bodyMsg) {
        dbMessenger.bodyMsg = bodyMsg;
        warn(headerMsg, titleMsg, bodyMsg);
    }
    
    @Override
    public void warning(String headerMsg, String titleMsg, String bodyMsg) {
        dbMessenger.headerMsg = headerMsg;
        dbMessenger.titleMsg = titleMsg;
        dbMessenger.bodyMsg = bodyMsg;
        warn(headerMsg, titleMsg, bodyMsg);
    }
    
    @Override
    public void warning(String bodyMsg) {
        dbMessenger.bodyMsg = bodyMsg;
        warn(headerMsg, titleMsg, bodyMsg);
    }
    
    private void dbSend() {
        final String sql = "insert into ru_vachok_networker (classname, msgtype, msgvalue, pc, stack) values (?,?,?,?,?)";
        long upTime = ManagementFactory.getRuntimeMXBean().getUptime();
        String pc = UsefulUtilities.thisPC() + " : " + UsefulUtilities.getUpTime();
        String stack = MessageFormat.format("{3}. UPTIME: {2}\n{0}\nPeak threads: {1}.",
                ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().toString(), ManagementFactory.getThreadMXBean().getPeakThreadCount(), upTime, pc);
        if (!isInfo) {
            stack = setStack(stack);
        }
        try (Connection con = dataConnectTo.getDataSource().getConnection()) {
            try (PreparedStatement p = con.prepareStatement(sql)) {
                p.setString(1, dbMessenger.headerMsg);
                p.setString(2, dbMessenger.titleMsg);
                p.setString(3, dbMessenger.bodyMsg);
                p.setString(4, pc);
                p.setString(5, stack);
                int executeUpdate = p.executeUpdate();
                System.out.println(MessageFormat
                        .format("{0} executeUpdate = {1} ({2}, {3}, {4})", dbMessenger.getClass().getSimpleName(), executeUpdate, headerMsg, titleMsg, bodyMsg));
                dbMessenger.headerMsg = "";
                dbMessenger.bodyMsg = "";
                dbMessenger.titleMsg = "";
            }
        }
        catch (SQLException e) {
            System.err.println(e.getMessage());
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
    }
    
    private @NotNull String setStack(String stack) {
        StringBuilder stringBuilder = new StringBuilder();
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        stringBuilder.append(stack).append("\n");
        
        long cpuTime = 0;
        for (long threadId : threadMXBean.getAllThreadIds()) {
            new TForms().fromArray(threadMXBean.dumpAllThreads(true, true));
            cpuTime += threadMXBean.getThreadCpuTime(threadId);
        }
        stringBuilder.append(TimeUnit.NANOSECONDS.toMillis(cpuTime)).append(" cpu time ms.").append("\n\nTraces: \n");
        Thread.currentThread().checkAccess();
        Map<Thread, StackTraceElement[]> threadStackMap = Thread.getAllStackTraces();
        String thrStackStr = new TForms().fromArray(threadStackMap);
        stringBuilder.append(thrStackStr).append("\n");
        return stringBuilder.toString();
    }
}