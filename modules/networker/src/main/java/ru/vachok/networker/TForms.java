// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vachok.mysqlandprops.props.FileProps;
import ru.vachok.mysqlandprops.props.InitProperties;

import javax.mail.Address;
import javax.servlet.http.Cookie;
import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.WatchEvent;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Stream;


/**
 Помощник для {@link Arrays#toString()}
 <p>
 Делает похожие действия, но сразу так, как нужно для {@link ru.vachok.networker.IntoApplication}
 
 @since 06.09.2018 (9:33) */
@SuppressWarnings("ALL")
public class TForms {
    
    
    /**
     {@link LoggerFactory#getLogger(java.lang.String)}
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TForms.class.getSimpleName());
    
    private static final String STR_LINE_CLASS = " line, class: ";
    
    private static final String STR_VALUE = ", value: ";
    
    private static final String STR_DISASTER = " occurred disaster!<br>";
    
    private static final String STR_METHFILE = " method.<br>File: ";
    
    private StringBuilder brStringBuilder = new StringBuilder();
    
    private StringBuilder nStringBuilder = new StringBuilder();
    
    public String fromArray(Properties properties) {
        InitProperties initProperties = new FileProps(ConstantsFor.APPNAME_WITHMINUS);
        initProperties.setProps(properties);
        nStringBuilder.append(ConstantsFor.STR_N);
        properties.forEach((x, y)->{
            String msg = x + " : " + y;
            LOGGER.info(msg);
            nStringBuilder.append(x).append(" :: ").append(y).append(ConstantsFor.STR_N);
        });
        return nStringBuilder.toString();
    }
    
    /**
     Преобразрвание исключений.
     
     @param e {@link Exception}
     @param isHTML где показывается строка. На ВЕБ или нет.
     @return {@link #brStringBuilder} или {@link #nStringBuilder}
     */
    public String fromArray(Exception e, boolean isHTML) {
        this.brStringBuilder = new StringBuilder();
        this.nStringBuilder = new StringBuilder();
    
        brStringBuilder.append(LocalDateTime.now()).append(ConstantsFor.STR_BR).append("<h3>").append(e.getMessage()).append(" Exception message.</h3><p>");
    
        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
            parseTrace(stackTraceElement);
        }
        brStringBuilder.append("<p>");
        nStringBuilder.append(ConstantsFor.STR_N).append(ConstantsFor.STR_N).append(ConstantsFor.STR_N);
    
        if (e.getSuppressed().length > 0) {
            parseThrowable(e);
        }
        if (isHTML) {
            return brStringBuilder.toString();
        }
        else {
            return nStringBuilder.toString();
        }
    }
    
    public String fromEnum(Enumeration<String> enumStrings, boolean br) {
        nStringBuilder.append(ConstantsFor.STR_N);
        brStringBuilder.append(ConstantsFor.STR_P);
        while (enumStrings.hasMoreElements()) {
            String str = enumStrings.nextElement();
            nStringBuilder.append(str).append(ConstantsFor.STR_N);
            brStringBuilder.append(str).append(ConstantsFor.STR_BR);
        }
        nStringBuilder.append(ConstantsFor.STR_N);
        brStringBuilder.append("</p>");
        if (br) {
            return brStringBuilder.toString();
        }
        else {
            return nStringBuilder.toString();
        }
    }
    
    public String fromArray(Queue<String> stringQueue, boolean br) {
        brStringBuilder = new StringBuilder();
        nStringBuilder = new StringBuilder();
        brStringBuilder.append(ConstantsFor.STR_P);
        while (stringQueue.iterator().hasNext()) {
            brStringBuilder.append(stringQueue.poll()).append(ConstantsFor.STR_BR);
            nStringBuilder.append(stringQueue.poll()).append(ConstantsFor.STR_N);
        }
        if (br) {
            return brStringBuilder.toString();
        }
        else {
            return nStringBuilder.toString();
        }
    }
    
    public String fromArray(@NotNull Cookie[] cookies, boolean br) {
        brStringBuilder.append(ConstantsFor.STR_P);
        for (Cookie c : cookies) {
            brStringBuilder
                .append(c.getName()).append(" ").append(c.getComment()).append(" ").append(c.getMaxAge()).append(ConstantsFor.STR_BR);
            nStringBuilder
                .append(c.getName()).append(" ").append(c.getComment()).append(" ").append(c.getMaxAge()).append(ConstantsFor.STR_N);
        }
        if (br) {
            return brStringBuilder.toString();
        }
        else {
            return nStringBuilder.toString();
        }
    }
    
    public String fromArray(@NotNull Address[] mailAddress, boolean br) {
        for (Address address : mailAddress) {
            brStringBuilder
                .append(address)
                .append("br");
            nStringBuilder
                .append(address)
                .append(ConstantsFor.STR_N);
        }
        if (br) {
            return brStringBuilder.toString();
        }
        else {
            return nStringBuilder.toString();
        }
    }
    
    public String fromArray(@NotNull Set<?> cacheSet, boolean br) {
        brStringBuilder.append(ConstantsFor.STR_P);
        nStringBuilder.append(ConstantsFor.STR_N);
        for (Object o : cacheSet) {
            brStringBuilder
                .append(o)
                .append(ConstantsFor.STR_BR);
            nStringBuilder
                .append(o)
                .append(ConstantsFor.STR_N);
        }
        if (br) {
            return brStringBuilder.toString();
        }
        else {
            return nStringBuilder.toString();
        }
    }
    
    public String fromArray(@NotNull Throwable[] suppressed) {
        nStringBuilder.append("suppressed throwable!\n".toUpperCase());
        for (Throwable throwable : suppressed) {
            nStringBuilder.append(throwable.getMessage());
        }
        return nStringBuilder.toString();
    }
    
    public String fromArrayUsers(@NotNull ConcurrentMap<?, ?> pcUsers, boolean isHTML) {
        pcUsers.forEach((x, y)->{
            nStringBuilder
                .append(ConstantsFor.STR_N)
                .append(x)
                .append(ConstantsFor.STR_N)
                .append(y);
            brStringBuilder
                .append("<p><h4>")
                .append(x)
                .append(ConstantsFor.STR_BR)
                .append(y)
                .append("</p>");
        });
        if (isHTML) {
            return brStringBuilder.toString();
        }
        else {
            return nStringBuilder.toString();
        }
    }
    
    public String fromArray(Map<?, ?> mapDefObj, boolean isHTML) {
        brStringBuilder = new StringBuilder();
        nStringBuilder = new StringBuilder();
        brStringBuilder.append(ConstantsFor.STR_P);
        
        for (Map.Entry<?, ?> entry : mapDefObj.entrySet()) {
            brStringBuilder.append(entry.getKey().toString()).append(" : ").append(entry.getValue().toString()).append(ConstantsFor.STR_BR);
            nStringBuilder.append(entry.getKey().toString()).append(" : ").append(entry.getValue().toString()).append(ConstantsFor.STR_N);
        }
        
        if (isHTML) {
            brStringBuilder.append(ConstantsFor.STR_P);
            return brStringBuilder.toString();
        }
        else {
            return nStringBuilder.toString();
        }
    }
    
    public String fromArray(InetAddress[] allByName, boolean b) {
        brStringBuilder.append(ConstantsFor.STR_BR);
        for (InetAddress inetAddress : allByName) {
            brStringBuilder
                .append(inetAddress)
                .append(ConstantsFor.STR_BR);
            nStringBuilder
                .append(inetAddress)
                .append(ConstantsFor.STR_N);
        }
        if (b) {
            return brStringBuilder.toString();
        }
        else {
            return nStringBuilder.toString();
        }
    }
    
    public String fromArray(@NotNull List<?> objList, boolean isHTML) {
        this.brStringBuilder = new StringBuilder();
        this.nStringBuilder = new StringBuilder();
        
        objList.forEach(objFromList->{
            brStringBuilder
                .append(ConstantsFor.STR_BR)
                .append(objFromList.toString());
            nStringBuilder
                .append(ConstantsFor.STR_N)
                .append(objFromList.toString());
        });
        if (isHTML) {
            return brStringBuilder.toString();
        }
        else {
            return nStringBuilder.toString();
        }
    }
    
    public String fromArray(Stream<?> objStream, boolean isHTML) {
        objStream.forEach(object->{
            brStringBuilder
                .append(ConstantsFor.STR_BR)
                .append(object);
            nStringBuilder
                .append(ConstantsFor.STR_N)
                .append(object);
        });
        
        if (isHTML) {
            return brStringBuilder.toString();
        }
        else {
            return nStringBuilder.toString();
        }
    }
    
    public String fromArray(Object[] objects, boolean b) {
        brStringBuilder.append(ConstantsFor.STR_P);
        for (Object o : objects) {
            brStringBuilder
                .append(o)
                .append(ConstantsFor.STR_BR);
            nStringBuilder
                .append(o)
                .append(ConstantsFor.STR_N);
        }
        if (b) {
            return brStringBuilder.toString();
        }
        else {
            return nStringBuilder.toString();
        }
    }
    
    public String fromArray(Properties p, boolean b) {
        brStringBuilder.append(ConstantsFor.STR_P);
        p.forEach((x, y)->{
            String str = "Property: ";
            String str1 = STR_VALUE;
            brStringBuilder
                .append(str).append(x)
                .append(str1).append(y).append(ConstantsFor.STR_BR);
            nStringBuilder
                .append(str).append(x)
                .append(str1).append(y).append(ConstantsFor.STR_N);
        });
        if (b) {
            return brStringBuilder.toString();
        }
        else {
            return nStringBuilder.toString();
        }
    }
    
    public String fromArray(BlockingQueue<Runnable> runnableBlockingQueue, boolean b) {
        this.nStringBuilder = new StringBuilder();
        this.brStringBuilder = new StringBuilder();
        nStringBuilder.append(ConstantsFor.STR_N);
        Iterator<Runnable> runnableIterator = runnableBlockingQueue.stream().iterator();
        int count = 0;
        while (runnableIterator.hasNext()) {
            Runnable next = runnableIterator.next();
            count++;
            nStringBuilder.append(count).append(") ").append(next).append(ConstantsFor.STR_N);
            brStringBuilder.append(count).append(") ").append(next).append(ConstantsFor.STR_BR);
        }
        if (b) {
            return brStringBuilder.toString();
        }
        else {
            return nStringBuilder.toString();
        }
    }
    
    public String sshCheckerMapWithDates(Map<String, Long> sshCheckerMap, boolean isHTML) {
        this.brStringBuilder = new StringBuilder();
        this.nStringBuilder = new StringBuilder();
        sshCheckerMap.forEach((x, y)->{
            try {
                byte[] address = InetAddress.getByName(x).getAddress();
                x = InetAddress.getByAddress(address).toString();
            }
            catch (UnknownHostException e) {
                x = x + " no name";
            }
            brStringBuilder.append("<b>").append(x).append("</b><font color=\"gray\"> ").append(y).append("</font> (").append(new Date(y)).append(")<br>");
            nStringBuilder.append(x).append(" ").append(y).append(" (").append(new Date(y)).append(")\n");
        });
        if (isHTML) {
            return brStringBuilder.toString();
        }
        else {
            return nStringBuilder.toString();
        }
    }
    
    public String fromArray(ResultSetMetaData resultSetMetaData, int colIndex, boolean isHTML) throws SQLException {
        this.brStringBuilder = new StringBuilder();
        this.nStringBuilder = new StringBuilder();
        
        brStringBuilder.append(resultSetMetaData.getColumnCount() + " collumns<br>");
        nStringBuilder.append(resultSetMetaData.getColumnCount() + " collumns\n");
    
        brStringBuilder.append(resultSetMetaData.getCatalogName(colIndex)).append(" getCatalogName").append(ConstantsFor.STR_BR);
        nStringBuilder.append(resultSetMetaData.getCatalogName(colIndex)).append(" getCatalogName").append(ConstantsFor.STR_N);
    
        brStringBuilder.append(resultSetMetaData.getColumnName(colIndex)).append(" getColumnName").append(ConstantsFor.STR_BR);
        nStringBuilder.append(resultSetMetaData.getColumnName(colIndex)).append(" getColumnName").append(ConstantsFor.STR_N);
    
        brStringBuilder.append(resultSetMetaData.getColumnDisplaySize(colIndex)).append(" getColumnDisplaySize").append(ConstantsFor.STR_BR);
        nStringBuilder.append(resultSetMetaData.getColumnDisplaySize(colIndex)).append(" getColumnDisplaySize").append(ConstantsFor.STR_N);
    
        brStringBuilder.append(resultSetMetaData.getColumnType(colIndex)).append(" getColumnType").append(ConstantsFor.STR_BR);
        nStringBuilder.append(resultSetMetaData.getColumnType(colIndex)).append(" getColumnType").append(ConstantsFor.STR_N);
        
        if (isHTML) {
            return brStringBuilder.toString();
        }
        else {
            return nStringBuilder.toString();
        }
    }
    
    public String fromArray(Collection<?> values, boolean isHTML) {
        return fromArray(values.stream(), isHTML);
    }
    
    public String fromArrayW(List<WatchEvent<?>> watchEventlist, boolean isHTML) {
        this.brStringBuilder = new StringBuilder();
        this.nStringBuilder = new StringBuilder();
        watchEventlist.stream().forEach(x->{
            brStringBuilder.append(x.count()).append(" events").append(ConstantsFor.STR_BR);
            nStringBuilder.append(x.count()).append(" events").append(ConstantsFor.STR_N);
    
            brStringBuilder.append(x.kind()).append(" ").append(x.context()).append(ConstantsFor.STR_BR);
            nStringBuilder.append(x.kind()).append(" ").append(x.context()).append(ConstantsFor.STR_N);
        });
        if (isHTML) {
            return brStringBuilder.toString();
        }
        else {
            return nStringBuilder.toString();
        }
    }
    
    public String fromArray(long[] threads) {
        this.nStringBuilder = new StringBuilder();
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        for (long tID : threads) {
            ThreadInfo threadInfo = threadMXBean.getThreadInfo(tID);
            LockInfo lockInfo = threadInfo.getLockInfo();
            nStringBuilder.append(lockInfo.toString());
        }
        return nStringBuilder.toString();
    }
    
    public String fromArray(ThreadInfo[] infos, boolean isHTML) {
        this.brStringBuilder = new StringBuilder();
        this.nStringBuilder = new StringBuilder();
        
        for (ThreadInfo threadInfo : infos) {
            brStringBuilder.append(threadInfo.getThreadName()).append(" ").append(threadInfo.getThreadState()).append(ConstantsFor.STR_BR);
            brStringBuilder.append(threadInfo.getThreadName()).append(" ").append(threadInfo.getThreadState()).append(ConstantsFor.STR_N);
            for (StackTraceElement element : threadInfo.getStackTrace()) {
                parseTrace(element);
            }
            brStringBuilder.append(ConstantsFor.STR_BR);
            nStringBuilder.append(ConstantsFor.STR_N);
            try {
                String lockInfoStr = threadInfo.getLockInfo().toString();
    
                brStringBuilder.append(lockInfoStr).append(ConstantsFor.STR_BR);
                nStringBuilder.append(lockInfoStr).append(ConstantsFor.STR_N);
            }
            catch (RuntimeException e) {
                nStringBuilder.append(new TForms().fromArray(e, false));
            }
        }
    
        if (isHTML) {
            return brStringBuilder.toString();
        }
        else {
            return nStringBuilder.toString();
        }
    }
    
    public String fromArray(@NotNull Preferences pref, boolean isHTML) {
        this.brStringBuilder = new StringBuilder();
        this.nStringBuilder = new StringBuilder();
    
        brStringBuilder.append("USER PREFS").append(ConstantsFor.STR_BR);
        nStringBuilder.append("USER PREFS").append(ConstantsFor.STR_N);
        try {
            String[] keys = pref.userRoot().keys();
            for (String key : keys) {
                brStringBuilder.append(key).append(" value: ").append(pref.get(key, "")).append(ConstantsFor.STR_BR);
                nStringBuilder.append(key).append(" value: ").append(pref.get(key, "")).append(ConstantsFor.STR_N);
            }
        }
        catch (BackingStoreException e) {
            brStringBuilder.append(new TForms().fromArray(e, true));
            nStringBuilder.append(new TForms().fromArray(e, false));
        }
        if (isHTML) {
            return brStringBuilder.toString();
        }
        else {
            return nStringBuilder.toString();
        }
    }
    
    public String fromArray(@NotNull Enumeration<?> enumOf, boolean isHtml) {
        this.nStringBuilder = new StringBuilder();
        this.brStringBuilder = new StringBuilder();
        while (enumOf.hasMoreElements()) {
            Object nextElement = enumOf.nextElement();
            brStringBuilder.append(nextElement).append(ConstantsFor.STR_BR);
            nStringBuilder.append(nextElement).append(ConstantsFor.STR_N);
        }
        if (isHtml) {
            return brStringBuilder.toString();
        }
        else {
            return nStringBuilder.toString();
        }
    }
    
    public String fromArray(@NotNull Address[] from) {
        this.nStringBuilder = new StringBuilder();
        for (Address address : from) {
            nStringBuilder.append(address.toString()).append(", ");
        }
        return nStringBuilder.toString();
    }
    
    public String fromArray(Exception e) {
        return fromArray(e, false);
    }
    
    public String fromArray(Set<?> set) {
        return fromArray(set, false);
    }
    
    public String fromArray(List<?> listObjects) {
        return fromArray(listObjects, false);
    }
    
    public String fromArray(Map<?, ?> files) {
        return fromArray(files, false);
    }
    
    public String fromArray(Deque<?> deque) {
        return fromArray(deque, false);
    }
    
    public String fromArray(StackTraceElement[] elements) {
        return fromArray(elements, false);
    }
    
    public String fromArray(Queue<?> queue) {
        return fromArray(queue, false);
    }
    
    public String fromArray(Enumeration<?> enumeration) {
        return fromArray(enumeration, false);
    }
    
    public String fromArray(@NotNull Throwable cause) {
        this.nStringBuilder = new StringBuilder();
        nStringBuilder.append(cause.getMessage()).append(" ").append(LocalDateTime.now()).append("\n\n");
        nStringBuilder.append(fromArray(cause.getStackTrace()));
        return nStringBuilder.toString();
    }
    
    public String fromArray(Collection<?> collectToString) {
        return fromArray(collectToString, false);
    }
    
    public void fromArray(@NotNull ThreadInfo[] threadInfos) {
        this.nStringBuilder = new StringBuilder();
        for (ThreadInfo threadInfo : threadInfos) {
            nStringBuilder.append(threadInfo.toString()).append("\n");
        }
    }
    
    public String fromArray(String[] stringsArray) {
        return fromArray(stringsArray, false);
    }
    
    public String fromArray(Object[] array) {
        return fromArray(array, false);
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TForms{");
        sb.append("STR_LINE_CLASS='").append(STR_LINE_CLASS).append('\'');
        sb.append(", STR_VALUE='").append(STR_VALUE).append('\'');
        sb.append(", STR_N='").append(ConstantsFor.STR_N).append('\'');
        sb.append(", STR_BR='").append(ConstantsFor.STR_BR).append('\'');
        sb.append(", STR_P='").append(ConstantsFor.STR_P).append('\'');
        sb.append(", STR_DISASTER='").append(STR_DISASTER).append('\'');
        sb.append(", STR_METHFILE='").append(STR_METHFILE).append('\'');
        sb.append(", brStringBuilder=").append(brStringBuilder);
        sb.append(", nStringBuilder=").append(nStringBuilder);
        sb.append('}');
        return sb.toString();
    }
    
    /**
     Если {@link Exception} содержит getSuppressed.
     <p>
     Парсит {@link Throwable}
 
     @param e {@link Exception}
     @see TForms#fromArray(java.lang.Exception, boolean)
     */
    @SuppressWarnings("MethodWithMultipleLoops")
    private void parseThrowable(@NotNull Exception e) {
        Throwable[] eSuppressed = e.getSuppressed();
        nStringBuilder.append("Suppressed.length = ").append(eSuppressed.length).append(ConstantsFor.STR_N);
        nStringBuilder.append("Suppressed.length = ").append(eSuppressed.length).append(ConstantsFor.STR_BR);
        for (Throwable throwable : eSuppressed) {
            nStringBuilder.append(throwable.toString()).append(ConstantsFor.STR_N);
            brStringBuilder.append(throwable.toString()).append(ConstantsFor.STR_BR);
            for (StackTraceElement stackTraceElement : throwable.getStackTrace()) {
                parseTrace(stackTraceElement);
            }
            nStringBuilder.append(ConstantsFor.STR_N).append(ConstantsFor.STR_N);
            brStringBuilder.append(ConstantsFor.STR_P);
        }
    }
    
    /**
     Парсинг элемента трэйса
     <p>
     
     @param stackTraceElement {@link StackTraceElement} из {@link Exception} или {@link Throwable}
     @see #parseThrowable(Exception)
     @see #fromArray(Exception, boolean)
     */
    private void parseTrace(@NotNull StackTraceElement stackTraceElement) {
        nStringBuilder.append(stackTraceElement.getFileName()).append(": ");
        nStringBuilder.append(stackTraceElement.toString()).append(ConstantsFor.STR_N);
        brStringBuilder.append(stackTraceElement.getFileName()).append(": ");
        brStringBuilder.append(stackTraceElement.toString()).append(ConstantsFor.STR_BR);
    }
}
