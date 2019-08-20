// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.configuretests;


import ru.vachok.networker.TForms;
import ru.vachok.networker.enums.PropertiesNames;
import ru.vachok.networker.restapi.MessageToUser;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;


public class TestConfigureThreadsLogMaker implements TestConfigure {
    
    
    private final MessageToUser messageToUser;
    
    private final long startTime;
    
    private ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    
    private PrintStream printStream;
    
    private String callingClass;
    
    private ThreadInfo threadInfo;
    
    public TestConfigureThreadsLogMaker(String callingClass, final long startNANOTime) {
        this.startTime = startNANOTime;
        this.callingClass = callingClass;
        this.messageToUser = MessageToUser.getInstance(MessageToUser.DB, callingClass);
    }
    
    @Override
    public PrintStream getPrintStream() {
        return printStream;
    }
    
    @Override
    public void before() {
        threadMXBean.setThreadCpuTimeEnabled(true);
        threadMXBean.setThreadContentionMonitoringEnabled(true);
        threadMXBean.resetPeakThreadCount();
        for (long threadId : threadMXBean.getAllThreadIds()) {
            String threadName = threadMXBean.getThreadInfo(threadId).getThreadName();
            if (callingClass.contains(threadName)) {
                this.threadInfo = threadMXBean.getThreadInfo(threadId);
            }
        }
        try {
            String fileSeparator = System.getProperty(PropertiesNames.PRSYS_SEPARATOR);
            Files.createDirectories(Paths.get(TEST_FOLDER));
            OutputStream outputStream = new FileOutputStream(TEST_FOLDER + callingClass + ".log", true);
            this.printStream = new PrintStream(outputStream, true);
        }
        catch (IOException e) {
            e.printStackTrace();
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
        long cpuTime = 0;
        try {
            cpuTime = threadMXBean.getThreadCpuTime(threadInfo.getThreadId());
        }
        catch (RuntimeException ignore) {
            //
        }
        long realTime = System.nanoTime() - startTime;
        String startInfo = "*** Starting " + threadInfo;
        String rtInfo = MessageFormat.format("Real Time run = {0} (in seconds)\nCPU Time = {1} (in milliseconds). {2}",
            TimeUnit.NANOSECONDS.toSeconds(realTime), TimeUnit.NANOSECONDS.toMillis(cpuTime), LocalTime.now());
    
        printStream.println(startInfo);
        printStream.println();
        printStream.println(rtInfo);
        printStream.println("cpuTime in nanos = " + cpuTime);
        printStream.println("End ***");
        printStream.println();
        printStream.println();
        printStream.close();
        messageToUser.info(callingClass, startInfo, rtInfo);
        Thread.currentThread().checkAccess();
        Thread.currentThread().interrupt();
    }
    
}
