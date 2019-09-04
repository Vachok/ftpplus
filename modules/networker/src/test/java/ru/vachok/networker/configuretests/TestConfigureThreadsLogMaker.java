// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.configuretests;


import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.componentsrepo.data.enums.PropertiesNames;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.MessageToUser;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;


public class TestConfigureThreadsLogMaker implements TestConfigure, Serializable {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.DB, TestConfigureThreadsLogMaker.class.getSimpleName());
    
    private final long startTime;
    
    private transient ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    
    private transient PrintStream printStream;
    
    private String callingClass;
    
    private transient ThreadInfo threadInfo;
    
    private transient Runtime runtime = Runtime.getRuntime();
    
    public TestConfigureThreadsLogMaker(String callingClass, final long startNANOTime) {
        this.startTime = startNANOTime;
        this.callingClass = callingClass;
    }
    
    @Override
    public PrintStream getPrintStream() {
        return printStream;
    }
    
    @Override
    public void before() {
        runtime.gc();
        threadMXBean.setThreadCpuTimeEnabled(true);
        threadMXBean.setThreadContentionMonitoringEnabled(true);
        threadMXBean.resetPeakThreadCount();
        try {
            for (long threadId : threadMXBean.getAllThreadIds()) {
                String threadName = threadMXBean.getThreadInfo(threadId).getThreadName();
                if (callingClass.contains(threadName)) {
                    this.threadInfo = threadMXBean.getThreadInfo(threadId);
                }
            }
        
            String fileSeparator = System.getProperty(PropertiesNames.PRSYS_SEPARATOR);
            Files.createDirectories(Paths.get(TEST_FOLDER));
            OutputStream outputStream = new FileOutputStream(TEST_FOLDER + callingClass + ".log", true);
            this.printStream = new PrintStream(outputStream, true);
        }
        catch (IOException | RuntimeException e) {
            messageToUser.error(e.getMessage() + " see line: 68 ***");
        }
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TestConfigureThreadsLogMaker{");
        sb.append("startTime=").append(startTime);
        sb.append(", threadMXBean=").append(threadMXBean.getThreadInfo(Thread.currentThread().getId()));
        
        sb.append(", callingClass='").append(callingClass).append('\'');
        sb.append(", threadInfo=").append(new TForms().fromArray(threadInfo.getStackTrace()));
        sb.append('}');
        return sb.toString();
    }
    
    @Override
    public void after() {
        long cpuTime;
        try {
        
            cpuTime = threadMXBean.getThreadCpuTime(threadInfo.getThreadId());
        }
        catch (RuntimeException e) {
            cpuTime = 0;
        }
        
        try {
            String startInfo = "*** Starting " + threadInfo;
            long realTime = System.nanoTime() - startTime;
            printStream.println(startInfo);
            printStream.println();
            String rtInfo = MessageFormat.format("Real Time run = {0} (in seconds)\nCPU Time = {1} (in milliseconds). {2}",
                TimeUnit.NANOSECONDS.toSeconds(realTime), TimeUnit.NANOSECONDS.toMillis(cpuTime), LocalTime.now());
            printStream.println(rtInfo);
            printStream.println("cpuTime in nanos = " + cpuTime);
            printStream.println("End ***");
            printStream.println();
            printStream.println();
            printStream.close();
        }
        catch (RuntimeException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".after", e));
        }
        runtime.runFinalization();
        long maxMemory = runtime.totalMemory();
        long freeM = runtime.freeMemory();
        messageToUser.warning(MessageFormat.format("Memory = {0} MB.", (maxMemory - freeM) / ConstantsFor.MBYTE));
    }
    
}
