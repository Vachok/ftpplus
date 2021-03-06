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
import java.text.MessageFormat;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;


public class TestConfigureThreadsLogMaker implements TestConfigure, Serializable {


    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, TestConfigureThreadsLogMaker.class.getSimpleName());

    private static final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

    private static final String fileSeparator = System.getProperty(PropertiesNames.SYS_SEPARATOR);

    private final long startTime;

    private transient PrintStream printStream;

    private String callingClass;

    private transient ThreadInfo threadInfo;

    private final transient Runtime runtime = Runtime.getRuntime();

    @Override
    public PrintStream getPrintStream() {
        return printStream;
    }

    public TestConfigureThreadsLogMaker(String callingClass, final long startNANOTime) {
        this.startTime = startNANOTime;
        this.callingClass = callingClass;
        try {
            this.printStream = new PrintStream(new FileOutputStream(this.getClass().getSimpleName()));
        }
        catch (FileNotFoundException e) {
            messageToUser.error("TestConfigureThreadsLogMaker", "TestConfigureThreadsLogMaker", e.getMessage() + " see line: 140");
        }
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
        try {
            findThread();
        }
        catch (RuntimeException e) {
            e.printStackTrace();
        }

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
            messageToUser.warn(TestConfigureThreadsLogMaker.class.getSimpleName(), e.getMessage(), " see line: 112 ***");
        }
        finally {
            messageToUser.info(callingClass, rtInfo, MessageFormat.format("Memory = {0} MB.", (maxMemory - freeM) / ConstantsFor.MBYTE));
            Runtime.getRuntime().runFinalization();
        }

    }

    private void findThread() {
        for (long threadId : threadMXBean.getAllThreadIds()) {
            String threadName = threadMXBean.getThreadInfo(threadId).getThreadName();
            if (callingClass.contains(threadName)) {
                this.threadInfo = threadMXBean.getThreadInfo(threadId);
            }
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
