// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.configuretests;


import org.jetbrains.annotations.Contract;
import ru.vachok.networker.TForms;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;


public class TestConfigureThreadsLogMaker implements TestConfigure, Serializable {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.DB, TestConfigureThreadsLogMaker.class.getSimpleName());
    
    private static final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    
    private static final String fileSeparator = System.getProperty(PropertiesNames.PRSYS_SEPARATOR);
    
    private long startTime;
    
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
        if (callingClass == null) {
            this.callingClass = this.getClass().getSimpleName();
            Thread.currentThread().checkAccess();
            Thread.currentThread().setName(this.callingClass);
        }
        threadMXBean.setThreadCpuTimeEnabled(true);
        threadMXBean.setThreadContentionMonitoringEnabled(true);
        threadMXBean.resetPeakThreadCount();
        findThread();
        
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
        String rtInfo = callingClass;
        long maxMemory = 0;
        long freeM = 0;
        try {
            String startInfo = "*** Starting " + threadInfo;
            long realTime = System.nanoTime() - startTime;
            printStream.println(startInfo);
            printStream.println();
            rtInfo = MessageFormat.format("Real Time run = {0} (in seconds)\nCPU Time = {1} (in milliseconds). {2}",
                TimeUnit.NANOSECONDS.toSeconds(realTime), TimeUnit.NANOSECONDS.toMillis(cpuTime), LocalTime.now());
            printStream.println(rtInfo);
            printStream.println("cpuTime in nanos = " + cpuTime);
            printStream.println("End ***");
            printStream.println();
            printStream.println();
            printStream.close();
            maxMemory = runtime.totalMemory();
            freeM = runtime.freeMemory();
            
        }
        catch (RuntimeException e) {
            messageToUser.error("TestConfigureThreadsLogMaker.after", e.getMessage(), new TForms().exceptionNetworker(e.getStackTrace()));
        }
        messageToUser.info(callingClass, rtInfo, MessageFormat.format("Memory = {0} MB.", (maxMemory - freeM) / ConstantsFor.MBYTE));
        runtime.runFinalization();
    }
    
    private void writeFile() {
        String nameFile = TEST_FOLDER + callingClass + ".log";
        try {
            Files.createDirectories(Paths.get(TEST_FOLDER));
        }
        catch (IOException e) {
            messageToUser.warn(e.getMessage() + " see line: 87 ***");
        }
        try {
    
            OutputStream outputStream = new FileOutputStream(nameFile, true);
            this.printStream = new PrintStream(outputStream, true);
        }
        catch (IOException e) {
            messageToUser.error("TestConfigureThreadsLogMaker.writeFile", e.getMessage(), new TForms().exceptionNetworker(e.getStackTrace()));
        }
        messageToUser.info(this.callingClass, nameFile, MessageFormat.format("{0} nano start ({1})", startTime, LocalDate.now()));
    }
    
    private void findThread() {
        try {
            for (long threadId : threadMXBean.getAllThreadIds()) {
                String threadName = threadMXBean.getThreadInfo(threadId).getThreadName();
                if (callingClass.contains(threadName)) {
                    this.threadInfo = threadMXBean.getThreadInfo(threadId);
                }
            }
        }
        catch (RuntimeException e) {
            messageToUser.error("TestConfigureThreadsLogMaker.findThread", e.getMessage(), new TForms().exceptionNetworker(e.getStackTrace()));
        }
        if (threadInfo != null) {
            writeFile();
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
    
    @Contract("_ -> fail")
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        throw new NotSerializableException("ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker");
    }
    
    @Contract("_ -> fail")
    private void writeObject(ObjectOutputStream out) throws IOException {
        throw new NotSerializableException("ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker");
    }
}
