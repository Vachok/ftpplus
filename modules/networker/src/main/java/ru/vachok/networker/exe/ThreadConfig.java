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
import org.springframework.util.CustomizableThreadCreator;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.config.DeadLockMonitor;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;


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
    private static ThreadPoolTaskScheduler TASK_SCHEDULER = new ThreadPoolTaskScheduler();
    
    /**
     {@link ThreadPoolTaskExecutor}
     */
    private static ThreadPoolTaskExecutor TASK_EXECUTOR = new ThreadPoolTaskExecutor();
    
    /**
     Instance
     */
    private static final ThreadConfig THREAD_CONFIG_INST = new ThreadConfig();
    
    /**
     Название метода
     */
    private static final String EXECUTE_AS_THREAD_METH_NAME = "ThreadConfig.executeAsThread";
    
    /**
     {@link MessageLocal}
     */
    private static MessageToUser messageToUser = new MessageLocal(ThreadConfig.class.getSimpleName());
    
    private Runnable r;
    
    private static final String AEXECUTOR = "asyncExecutor = ";
    
    private ThreadConfig() {
    }
    
    
    /**
     @return {@link #TASK_EXECUTOR}
     */
    public ThreadPoolTaskExecutor getTaskExecutor() {
        TASK_EXECUTOR.initialize();
        boolean prestartCoreThread = TASK_EXECUTOR.getThreadPoolExecutor().prestartCoreThread();
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
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = TASK_SCHEDULER.getScheduledThreadPoolExecutor();
        scheduledThreadPoolExecutor.setCorePoolSize(20);
        scheduledThreadPoolExecutor.setMaximumPoolSize(50);
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
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".execByThreadConfig", e));
            return false;
        }
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ThreadConfig{");
        sb.append("\n");
        sb.append(", <br><font color=\"#fcf594\">TASK_SCHEDULER=").append(TASK_SCHEDULER.getScheduledThreadPoolExecutor().toString().split("\\Q@\\E")[1]);
        sb.append(", <br>TASK_EXECUTOR=").append(TASK_EXECUTOR.getThreadPoolExecutor().toString().split("\\Q@\\E")[1]);
        sb.append(", <br>Locks: ").append(new DeadLockMonitor()).append("<p>");
        sb.append("</font>}");
        return sb.toString();
    }
    
    private boolean execByThreadConfig() {
        CustomizableThreadCreator customizableThreadCreator = new CustomizableThreadCreator("AsThread: ");
        customizableThreadCreator.setThreadPriority(8);
        Thread thread = customizableThreadCreator.createThread(r);
        Executor asyncExecutor = Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor());
        if (new ASExec().getAsyncExecutor() != null) {
            asyncExecutor = new ASExec().getAsyncExecutor();
            System.out.println(AEXECUTOR + (asyncExecutor != null ? asyncExecutor.getClass().getSimpleName() : null));
        }
        else {
            messageToUser.errorAlert(getClass().getSimpleName(), "asyncExecutor is " + null, thread.getName());
        }
        if (asyncExecutor != null) {
            System.out.println(AEXECUTOR + asyncExecutor.getClass().getSimpleName());
            asyncExecutor.execute(r);
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
    
    
    }
    
    
    
}
