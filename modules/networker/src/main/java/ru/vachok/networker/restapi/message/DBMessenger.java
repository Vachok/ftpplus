// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.message;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.fileworks.FileSystemWorker;
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
 * @since 26.08.2018 (12:29)
 @see ru.vachok.networker.restapi.message.DBMessengerTest
 */
public class DBMessenger implements MessageToUser {
    
    
    private final MysqlDataSource DS_LOGS = new RegRuMysqlLoc(ConstantsFor.DBPREFIX + "webapp").getDataSource();
    
    private static final String NOT_SUPPORTED = "Not Supported";
    
    private String headerMsg;
    
    private String titleMsg = ConstantsFor.getUpTime();
    
    private String bodyMsg;
    
    private static DBMessenger dbMessenger = new DBMessenger("STATIC");
    
    private String sendResult = "No sends ";
    
    @Contract(pure = true)
    public static DBMessenger getInstance(String name) {
        Thread.currentThread().setName("SIN-" + DBMessenger.class.getSimpleName());
        dbMessenger.headerMsg = name;
        return dbMessenger;
    }
    
    @Contract(value = "null -> false", pure = true)
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
        if (headerMsg != null ? !headerMsg.equals(messenger.headerMsg) : messenger.headerMsg != null) {
            return false;
        }
        if (!titleMsg.equals(messenger.titleMsg)) {
            return false;
        }
        return bodyMsg != null ? bodyMsg.equals(messenger.bodyMsg) : messenger.bodyMsg == null;
    }
    
    @Override
    public int hashCode() {
        int result = headerMsg != null ? headerMsg.hashCode() : 0;
        result = 31 * result + titleMsg.hashCode();
        result = 31 * result + (bodyMsg != null ? bodyMsg.hashCode() : 0);
        result = 31 * result + (isInfo ? 1 : 0);
        return result;
    }
    
    private boolean isInfo = true;
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DBMessenger{");
        sb.append("DS_LOGS=").append(DS_LOGS.getURL());
        sb.append(", headerMsg='").append(headerMsg).append('\'');
        sb.append(", titleMsg='").append(titleMsg).append('\'');
        sb.append(", bodyMsg='").append(bodyMsg).append('\'');
        sb.append(", sendResult='").append(sendResult).append('\'');
        sb.append(", isInfo=").append(isInfo);
        sb.append('}');
        return sb.toString();
    }
    
    public DBMessenger(String headerMsgClassNameAsUsual) {
        this.headerMsg = headerMsgClassNameAsUsual;
        this.bodyMsg = "null";
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
        this.titleMsg = "ERROR! " + titleMsg;
        this.bodyMsg = bodyMsg;
        LoggerFactory.getLogger(headerMsg + ":" + titleMsg).error(bodyMsg);
        this.isInfo = false;
        Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).execute(()->dbSend(headerMsg, titleMsg, bodyMsg));
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
        
        Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).execute(()->dbSend(headerMsg, titleMsg, bodyMsg));
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
        LoggerFactory.getLogger(headerMsg + ":" + titleMsg).warn(bodyMsg);
        Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).execute(()->dbSend(headerMsg, titleMsg, bodyMsg));
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
    
    private String dbSend(String classname, String msgtype, String msgvalue) {
        final String sql = "insert into ru_vachok_networker (classname, msgtype, msgvalue, pc, stack) values (?,?,?,?,?)";
        long upTime = ManagementFactory.getRuntimeMXBean().getUptime();
        String pc = ConstantsFor.thisPC() + ": " + ConstantsFor.getUpTime();
        String stack = MessageFormat.format("UPTIME: {2}\n{0}\nPeak threads: {1}.",
            ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().toString(), ManagementFactory.getThreadMXBean().getPeakThreadCount(), upTime);
        if (!isInfo) {
            stack = setStack(stack);
        }
        try (final Connection c = DS_LOGS.getConnection()) {
            try (PreparedStatement p = c.prepareStatement(sql)) {
                p.setString(1, classname);
                p.setString(2, msgtype);
                p.setString(3, msgvalue);
                p.setString(4, pc);
                p.setString(5, stack);
                return MessageFormat.format("{0} executeUpdate.\nclassname aka headerMsg - {1}: msgType aka titleMsg - {2}\nBODY: {3}",
                    p.executeUpdate(), classname, msgtype, msgvalue, pc);
            }
        }
        catch (SQLException e) {
            return FileSystemWorker.error(getClass().getSimpleName() + ".dbSend", e);
        }
        finally {
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