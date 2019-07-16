// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe;


import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.support.ExecutorServiceAdapter;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.TaskUtils;
import org.springframework.stereotype.Service;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.exe.schedule.DeadLockMonitor;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.*;


/**
 Конфигуратор для {@link ThreadPoolTaskExecutor}
 <p>
 
 @see ru.vachok.networker.exe.ThreadConfigTest
 @since 11.09.2018 (11:41) */
@SuppressWarnings("MagicNumber")
@EnableAsync
@Service("taskExecutor")
public final class ThreadConfig extends ThreadPoolTaskExecutor {
    
    
    /**
     {@link ThreadPoolTaskScheduler}
     */
    private static final ThreadPoolTaskScheduler TASK_SCHEDULER;
    
    /**
     {@link ThreadPoolTaskExecutor}
     */
    private static final ThreadPoolTaskExecutor TASK_EXECUTOR;
    
    
    /**
     Instance
     */
    private static final ThreadConfig THREAD_CONFIG_INST = new ThreadConfig();
    
    private static final ThreadMXBean MX_BEAN_THREAD = ManagementFactory.getThreadMXBean();
    
    /**
     Название метода
     */
    private static final String EXECUTE_AS_THREAD_METH_NAME = "ThreadConfig.executeAsThread";
    
    private static final String A_EXECUTOR = "asyncExecutor = ";
    
    /**
     {@link MessageLocal}
     */
    private static MessageToUser messageToUser = new MessageLocal(ThreadConfig.class.getSimpleName());
    
    private Runnable r;
    
    static {
        TASK_SCHEDULER = new ThreadPoolTaskScheduler();
        TASK_EXECUTOR = new ThreadPoolTaskExecutor();
        TASK_SCHEDULER.initialize();
        TASK_EXECUTOR.initialize();
    }
    
    private ThreadConfig() {
    }
    
    public String dumpToFile() {
        ThreadInfo[] threadInfo = MX_BEAN_THREAD.dumpAllThreads(true, true);
        String fromArray = new TForms().fromArray(threadInfo, false);
        return FileSystemWorker.writeFile("stack.txt", fromArray);
    }
    
    /**
     @return {@link #TASK_EXECUTOR}
     */
    public ThreadPoolTaskExecutor getTaskExecutor() {
    
        TASK_EXECUTOR.getThreadPoolExecutor().setCorePoolSize(900);
        TASK_EXECUTOR.setQueueCapacity(1800);
        TASK_EXECUTOR.setWaitForTasksToCompleteOnShutdown(true);
        TASK_EXECUTOR.setAwaitTerminationSeconds(7);
        TASK_EXECUTOR.setThreadPriority(5);
        TASK_EXECUTOR.setThreadNamePrefix("EX");
    
        return TASK_EXECUTOR;
    }
    
    public static ThreadConfig getI() {
        return THREAD_CONFIG_INST;
    }
    
    public ThreadPoolTaskScheduler getTaskScheduler() {
        ScheduledThreadPoolExecutor scThreadPoolExecutor = TASK_SCHEDULER.getScheduledThreadPoolExecutor();
        scThreadPoolExecutor.setCorePoolSize(20);
        scThreadPoolExecutor.setMaximumPoolSize(50);
        scThreadPoolExecutor.setRemoveOnCancelPolicy(true);
        TASK_SCHEDULER.setErrorHandler(TaskUtils.LOG_AND_SUPPRESS_ERROR_HANDLER);
        TASK_SCHEDULER.prefersShortLivedTasks();
        TASK_SCHEDULER.setThreadNamePrefix("TS");
        TASK_SCHEDULER.setThreadPriority(3);
        TASK_SCHEDULER.setWaitForTasksToCompleteOnShutdown(false);
        TASK_SCHEDULER.setDaemon(true);
        return TASK_SCHEDULER;
    }
    
    /**
     Killer
     */
    @SuppressWarnings("MethodWithMultipleLoops") public boolean killAll() {
        TASK_SCHEDULER.shutdown();
        final StringBuilder builder = new StringBuilder();
        for (Runnable runnable : TASK_SCHEDULER.getScheduledThreadPoolExecutor().shutdownNow()) {
            builder.append(runnable).append("\n");
        }
        TASK_EXECUTOR.shutdown();
        for (Runnable runnable : TASK_EXECUTOR.getThreadPoolExecutor().shutdownNow()) {
            builder.append(runnable).append("\n");
        }
        messageToUser.warn(builder.toString());
        SimpleAsyncTaskExecutor simpleAsyncExecutor = new ASExec().getSimpleAsyncExecutor();
        ThreadGroup threadGroup = null;
        boolean threadGroupDestroyed = true;
        return TASK_EXECUTOR.getThreadPoolExecutor().isShutdown() & TASK_SCHEDULER.getScheduledThreadPoolExecutor().isShutdown();
    }
    
    public String thrNameSet(String className) {
        float localUptime = (System.currentTimeMillis() - ConstantsFor.START_STAMP) / 1000 / ConstantsFor.ONE_HOUR_IN_MIN;
        String delaysCount = String.format("%.01f", (localUptime / ConstantsFor.DELAY));
        String upStr = String.format("%.01f", localUptime);
        
        upStr += "m";
        if (localUptime > ConstantsFor.ONE_HOUR_IN_MIN) {
            localUptime /= ConstantsFor.ONE_HOUR_IN_MIN;
            upStr = String.format("%.02f", localUptime);
            upStr += "h";
        }
        String thrName = className + ";" + upStr + ";" + delaysCount;
        Thread.currentThread().setName(thrName);
        return thrName;
    }
    
    public boolean execByThreadConfig(Runnable runnable) {
        this.r = runnable;
        try {
            return execByThreadConfig();
        }
        catch (Exception e) {
            e.printStackTrace();
            TASK_EXECUTOR.initialize();
            TASK_EXECUTOR.execute(r);
            return false;
        }
    }
    
    @Override public String toString() {
        final StringBuilder sb = new StringBuilder("ThreadConfig{");
        long cpuTime = countCPUTime();
        
        sb.append(TASK_EXECUTOR.getThreadPoolExecutor()).append(" TASK EXECUTOR, ");
        sb.append(TASK_SCHEDULER.getScheduledExecutor()).append(" TASK SCHEDULER.\n <p>");
        sb.append(MX_BEAN_THREAD.getObjectName()).append(" object name, ");
        sb.append(MX_BEAN_THREAD.getTotalStartedThreadCount()).append(" total threads started, ");
        sb.append(MX_BEAN_THREAD.getThreadCount()).append(" current threads live, ");
        sb.append(MX_BEAN_THREAD.getPeakThreadCount()).append(" peak live. <br>");
        sb.append(getDLMon());
        sb.append('}');
        return sb.toString();
    }
    
    private long countCPUTime() {
        long retLong = 0;
        for (long threadId : MX_BEAN_THREAD.getAllThreadIds()) {
            long cpuTime = MX_BEAN_THREAD.getThreadCpuTime(threadId);
            retLong += cpuTime;
        }
        return retLong;
    }
    
    private static String getDLMon() {
        Future<String> dlMon = TASK_EXECUTOR.submit(new DeadLockMonitor());
        try {
            return dlMon.get(ConstantsFor.DELAY, TimeUnit.SECONDS);
        }
        catch (InterruptedException | ExecutionException | TimeoutException e) {
            return e.getMessage();
        }
    }
    
    /**
     @return executed or not
     
     @see ru.vachok.networker.exe.ThreadConfigTest#testExecByThreadConfig()
     */
    private boolean execByThreadConfig() {
        SimpleAsyncTaskExecutor simpleAsyncExecutor = new ASExec().getSimpleAsyncExecutor();
        
        if (!(simpleAsyncExecutor == null)) {
            simpleAsyncExecutor.execute(r);
            return true;
        }
        else {
            return false;
        }
    }
    
    /**
     Асинхронный {@link ThreadPoolTaskExecutor}
     <p>
     
     @see AsyncConfigurerSupport
     @since <a href="https://github.com/Vachok/ftpplus/commit/f40030246ec6f28cc9c484b9c56a3879da1162af" target=_blank>21.02.2019 (22:49)</a>
     */
    private class ASExec extends AsyncConfigurerSupport {
    
    
        private SimpleAsyncTaskExecutor simpleAsyncExecutor = new SimpleAsyncTaskExecutor();
    
        @Override
        public String toString() {
            boolean throttleActive = simpleAsyncExecutor.isThrottleActive();
        
            return throttleActive + " throttleActive. Concurrency limit : " + simpleAsyncExecutor.getConcurrencyLimit();
        }
    
        @Override
        public Executor getAsyncExecutor() {
            OperatingSystemMXBean mxBean = ManagementFactory.getOperatingSystemMXBean();
            simpleAsyncExecutor.setConcurrencyLimit(50);
            simpleAsyncExecutor.setThreadPriority(6);
            simpleAsyncExecutor.setConcurrencyLimit(mxBean.getAvailableProcessors() - 1);
            simpleAsyncExecutor.setTaskDecorator(this::decorateTask);
            System.out.println("simpleAsyncExecutor.isThrottleActive() = " + simpleAsyncExecutor.isThrottleActive());
            Executor executorServiceAdapter = new ExecutorServiceAdapter(simpleAsyncExecutor);
            return executorServiceAdapter;
        }
    
        private SimpleAsyncTaskExecutor getSimpleAsyncExecutor() {
            return simpleAsyncExecutor;
        }
    
        private Runnable decorateTask(Runnable runnable) {
            ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
            long threadCpuTime = threadMXBean.getCurrentThreadCpuTime();
            System.out.println(TimeUnit.NANOSECONDS.toMillis(threadCpuTime) + " CPU time in ms of thread " + runnable.getClass().getSimpleName());
            return runnable;
        }
    }
}
