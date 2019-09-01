// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.message;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.database.RegRuMysqlLoc;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/**
 @see ru.vachok.networker.restapi.message.DBMessengerTest
 @since 26.08.2018 (12:29) */
public class DBMessenger implements MessageToUser {
    
    
    private static final String NOT_SUPPORTED = "Not Supported";
    
    private final MysqlDataSource dsLogs = new RegRuMysqlLoc(ConstantsFor.DBPREFIX + "webapp").getDataSource();
    
    private static DBMessenger dbMessenger = new DBMessenger("STATIC");
    
    private String headerMsg;
    
    private String titleMsg = UsefulUtilities.getUpTime();
    
    private String bodyMsg;
    
    private String sendResult = "No sends ";
    
    private boolean isInfo = true;
    
    private DBMessenger(String headerMsg) {
        Thread.currentThread().setName("dblg " + titleMsg);
        this.headerMsg = headerMsg;
    }
    
    @Contract(pure = true)
    public static DBMessenger getInstance(String name) {
        Thread.currentThread().setName("DBMsg-" + dbMessenger.hashCode());
        dbMessenger.headerMsg = name;
        return dbMessenger;
    }
    
    @Override
    public int hashCode() {
        int result = dsLogs.hashCode();
        result = 31 * result + titleMsg.hashCode();
        result = 31 * result + sendResult.hashCode();
        result = 31 * result + (isInfo ? 1 : 0);
        return result;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        
        DBMessenger messenger = (DBMessenger) o;
        
        if (isInfo != messenger.isInfo) {
            return false;
        }
        if (!dsLogs.equals(messenger.dsLogs)) {
            return false;
        }
        if (!titleMsg.equals(messenger.titleMsg)) {
            return false;
        }
        return sendResult.equals(messenger.sendResult);
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DBMessenger{");
        sb.append("DS_LOGS=").append(dsLogs.getURL());
        sb.append(", headerMsg='").append(headerMsg).append('\'');
        sb.append(", titleMsg='").append(titleMsg).append('\'');
        sb.append(", bodyMsg='").append(bodyMsg).append('\'');
        sb.append(", sendResult='").append(sendResult).append('\'');
        sb.append(", isInfo=").append(isInfo);
        sb.append('}');
        return sb.toString();
    }
    
    @Override
    public void errorAlert(String headerMsg, String titleMsg, String bodyMsg) {
        this.headerMsg = headerMsg;
        this.titleMsg = "ERROR! " + titleMsg;
        this.bodyMsg = bodyMsg;
        LoggerFactory.getLogger(headerMsg + ":" + titleMsg).error(bodyMsg);
        this.isInfo = false;
        Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).execute(this::dbSend);
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
        this.isInfo = true;
        Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).execute(this::dbSend);
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
        this.titleMsg = "WARNING: " + titleMsg;
        this.bodyMsg = bodyMsg;
        LoggerFactory.getLogger(headerMsg).warn(MessageFormat.format("{0} : {1}", titleMsg, bodyMsg));
        Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).execute(this::dbSend);
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
    
    private void dbSend() {
        final String sql = "insert into ru_vachok_networker (classname, msgtype, msgvalue, pc, stack) values (?,?,?,?,?)";
        long upTime = ManagementFactory.getRuntimeMXBean().getUptime();
        String pc = UsefulUtilities.thisPC() + ": " + UsefulUtilities.getUpTime();
        String stack = MessageFormat.format("UPTIME: {2}\n{0}\nPeak threads: {1}.",
                ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().toString(), ManagementFactory.getThreadMXBean().getPeakThreadCount(), upTime);
        if (!isInfo) {
            stack = setStack(stack);
        }
        synchronized(dsLogs) {
            try (Connection c = dsLogs.getConnection()) {
                try (PreparedStatement p = c.prepareStatement(sql)) {
                    p.setString(1, this.headerMsg);
                    p.setString(2, this.titleMsg);
                    p.setString(3, this.bodyMsg);
                    p.setString(4, pc);
                    p.setString(5, stack);
                    int executeUpdate = p.executeUpdate();
                }
            }
            catch (SQLException e) {
            
            }
            finally {
                Thread.currentThread().checkAccess();
                Thread.currentThread().interrupt();
            }
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