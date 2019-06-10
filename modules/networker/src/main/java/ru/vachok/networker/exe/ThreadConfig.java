// Copyright (c) all rights. http://networker.vachok.ru 2019.

/*
 * Copyright (c) 2019.
 */

/*
 * Copyright (c) 2019.
 */

package ru.vachok.networker.exe;


import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.support.ExecutorServiceAdapter;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.exe.schedule.DeadLockMonitor;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.*;


/**
 Конфигуратор для {@link ThreadPoolTaskExecutor}
 <p>
 
 @since 11.09.2018 (11:41) */
@SuppressWarnings("MagicNumber")
@EnableAsync
@Service("taskExecutor")
public final class ThreadConfig extends ThreadPoolTaskExecutor {
    
    
    /**
     {@link ThreadPoolTaskScheduler}
     */
    private static final ThreadPoolTaskScheduler TASK_SCHEDULER = new ThreadPoolTaskScheduler();
    
    /**
     {@link ThreadPoolTaskExecutor}
     */
    private static final ThreadPoolTaskExecutor TASK_EXECUTOR = new ThreadPoolTaskExecutor();
    
    /**
     Instance
     */
    private static final ThreadConfig THREAD_CONFIG_INST = new ThreadConfig();
    
    private static final ThreadMXBean MX_BEAN_THREAD = ManagementFactory.getThreadMXBean();
    
    /**
     Название метода
     */
    private static final String EXECUTE_AS_THREAD_METH_NAME = "ThreadConfig.executeAsThread";
    
    private static final String AEXECUTOR = "asyncExecutor = ";
    
    /**
     {@link MessageLocal}
     */
    private static MessageToUser messageToUser = new MessageLocal(ThreadConfig.class.getSimpleName());
    
    private Runnable r;
    
    private ThreadConfig() {
    }
    
    public String dumpToFile() {
        ThreadInfo[] threadInfos = MX_BEAN_THREAD.dumpAllThreads(true, true);
        String fromArray = new TForms().fromArray(threadInfos, false);
        return FileSystemWorker.writeFile("stack.txt", fromArray);
    }
    
    /**
     @return {@link #TASK_EXECUTOR}
     */
    public ThreadPoolTaskExecutor getTaskExecutor() {
        TASK_EXECUTOR.initialize();
        TASK_EXECUTOR.getThreadPoolExecutor().setCorePoolSize(900);
        TASK_EXECUTOR.setQueueCapacity(1800);
        TASK_EXECUTOR.setWaitForTasksToCompleteOnShutdown(true);
        TASK_EXECUTOR.setAwaitTerminationSeconds(7);
        TASK_EXECUTOR.setThreadPriority(4);
        TASK_EXECUTOR.setThreadNamePrefix("EX");
        TASK_EXECUTOR.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        return TASK_EXECUTOR;
    }
    
    public static ThreadConfig getI() {
        return THREAD_CONFIG_INST;
    }
    
    public ThreadPoolTaskScheduler getTaskScheduler() {
        TASK_SCHEDULER.initialize();
        ScheduledThreadPoolExecutor scThreadPoolExecutor = TASK_SCHEDULER.getScheduledThreadPoolExecutor();
        scThreadPoolExecutor.setCorePoolSize(20);
        scThreadPoolExecutor.setMaximumPoolSize(50);
        TASK_SCHEDULER.setThreadNamePrefix("TS");
        TASK_SCHEDULER.setThreadPriority(4);
        TASK_SCHEDULER.setWaitForTasksToCompleteOnShutdown(false);
        TASK_SCHEDULER.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        return TASK_SCHEDULER;
    }
    
    /**
     Killer
     */
    public void killAll() {
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
        ThreadGroup threadGroup = new ASExec().getSimpleAsyncExecutor().getThreadGroup();
        if (threadGroup != null) {
            threadGroup.destroy();
        }
    }
    
    public void thrNameSet(String className) {
        float localUptimer = (System.currentTimeMillis() - ConstantsFor.START_STAMP) / 1000 / ConstantsFor.ONE_HOUR_IN_MIN;
        String delaysCount = String.format("%.01f", (localUptimer / ConstantsFor.DELAY));
        String upStr = String.format("%.01f", localUptimer);
        
        upStr += "m";
        if (localUptimer > ConstantsFor.ONE_HOUR_IN_MIN) {
            localUptimer /= ConstantsFor.ONE_HOUR_IN_MIN;
            upStr = String.format("%.02f", localUptimer);
            upStr += "h";
        }
        String thrName = className + ";" + upStr + ";" + delaysCount;
        Thread.currentThread().setName(thrName);
    }
    
    public boolean execByThreadConfig(Runnable runnable) {
        this.r = runnable;
        try {
            boolean execByThreadConfig = execByThreadConfig();
            messageToUser.warn(getClass().getSimpleName(), runnable.toString(), " = " + execByThreadConfig);
            return execByThreadConfig;
        }
        catch (Exception e) {
            TASK_EXECUTOR.execute(r);
            return false;
        }
    }
    
    @Override public String toString() {
        final StringBuilder sb = new StringBuilder("ThreadConfig{");
        sb.append(MX_BEAN_THREAD.getTotalStartedThreadCount()).append(" total threads started, ");
        sb.append(MX_BEAN_THREAD.getThreadCount()).append(" current threads live, ");
        sb.append(MX_BEAN_THREAD.getPeakThreadCount()).append(" peak live. <br>");
        sb.append(getDLMon());
        sb.append('}');
        return sb.toString();
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
    
    private boolean execByThreadConfig() {
        SimpleAsyncTaskExecutor simpleAsyncExecutor = new ASExec().getSimpleAsyncExecutor();
        Thread thread = simpleAsyncExecutor.getThreadFactory().newThread(r);
        messageToUser.errorAlert(getClass().getSimpleName(), "asyncExecutor is " + null, thread.getName());
    
        if (simpleAsyncExecutor != null) {
            System.out.println(AEXECUTOR + simpleAsyncExecutor.getClass().getSimpleName());
            simpleAsyncExecutor.execute(r);
            return true;
        }
        else {
            thread.setName("ALONG...");
            thread.start();
            messageToUser.error(EXECUTE_AS_THREAD_METH_NAME, "thread.isAlive()", " = " + thread.isAlive());
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
        public Executor getAsyncExecutor() {
            thrNameSet("ESA");
            simpleAsyncExecutor.setConcurrencyLimit(50);
            simpleAsyncExecutor.setThreadPriority(8);
            simpleAsyncExecutor.setTaskDecorator(runnable->r);
            return new ExecutorServiceAdapter(simpleAsyncExecutor);
        }
    
        private SimpleAsyncTaskExecutor getSimpleAsyncExecutor() {
            return simpleAsyncExecutor;
        }
    
        private void setSimpleAsyncExecutor(SimpleAsyncTaskExecutor simpleAsyncExecutor) {
            this.simpleAsyncExecutor = simpleAsyncExecutor;
        }
        
        
    }
    
    
    
}
