package ru.vachok.networker.configuretests;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;


public class TestConfigureThreadsLogMaker implements TestConfigure {
    
    
    private final long startTime;
    
    private ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    
    public TestConfigureThreadsLogMaker(String callingClass, final long startTime) {
        this.startTime = startTime;
        this.callingClass = callingClass;
    }
    
    private PrintStream printStream;
    
    private String callingClass;
    
    private ThreadInfo threadInfo;
    
    @Override
    public PrintStream getPrintStream() {
        return printStream;
    }
    
    @Override
    public void beforeClass() {
        threadMXBean.setThreadCpuTimeEnabled(true);
        threadMXBean.setThreadContentionMonitoringEnabled(true);
        for (long threadId : threadMXBean.getAllThreadIds()) {
            String threadName = threadMXBean.getThreadInfo(threadId).getThreadName();
            if (callingClass.contains(threadName)) {
                this.threadInfo = threadMXBean.getThreadInfo(threadId);
            }
            else {
                System.out.println("threadName = " + threadName);
            }
        }
        try {
            String fileSeparator = System.getProperty("file.separator");
            String absoluteExecutionRootTestFolder = Paths.get(".").toAbsolutePath().normalize() + fileSeparator + "tests" + fileSeparator;
            Files.createDirectories(Paths.get(absoluteExecutionRootTestFolder));
            OutputStream outputStream = new FileOutputStream(absoluteExecutionRootTestFolder + callingClass + ".log", true);
            this.printStream = new PrintStream(outputStream, true);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void afterClass() {
        long cpuTime = threadMXBean.getThreadCpuTime(threadInfo.getThreadId());
        long realTime = System.nanoTime() - startTime;
        printStream.println("*** Starting " + threadInfo + " at " + LocalDateTime.now());
        printStream.println();
        printStream.println("realTime in seconds = " + TimeUnit.NANOSECONDS.toSeconds(realTime));
        printStream.println("cpuTime in nanos = " + cpuTime);
        printStream.println("End ***");
        printStream.println();
        printStream.println();
        printStream.close();
        Thread.currentThread().checkAccess();
        Thread.currentThread().interrupt();
    }
    
}
