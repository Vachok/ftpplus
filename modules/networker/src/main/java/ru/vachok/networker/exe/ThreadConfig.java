// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.support.ExecutorServiceAdapter;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.TaskUtils;
import org.springframework.stereotype.Service;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.text.MessageFormat;
import java.util.Date;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;


/**
 Конфигуратор для {@link ThreadPoolTaskExecutor}
 <p>
 
 @see ru.vachok.networker.exe.ThreadConfigTest
 @since 11.09.2018 (11:41) */
@SuppressWarnings("MagicNumber")
@EnableAsync
@Service("taskExecutor")
public class ThreadConfig extends ThreadPoolTaskExecutor {
    
    
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
    
    private static final int PROCESSORS = ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();
    
    /**
     {@link MessageLocal}
     */
    private static MessageToUser messageToUser = ru.vachok.networker.restapi.message.MessageToUser
            .getInstance(ru.vachok.networker.restapi.message.MessageToUser.LOCAL_CONSOLE, ThreadConfig.class.getSimpleName());
    
    private Runnable r;
    
    private ThreadConfig() {
        dumpToFile("ThreadConfig");
    }
    
    public static @NotNull String dumpToFile(String fileName) {
        String fromArray = new TForms().fromArray(Thread.currentThread().getStackTrace());
        ReentrantLock reentrantLock = new ReentrantLock();
        fileName = "thr_" + fileName + "-stack.txt";
        reentrantLock.lock();
        try (OutputStream outputStream = new FileOutputStream(fileName, true);
             PrintStream printStream = new PrintStream(outputStream, true)) {
            printStream.println();
            printStream.println(new Date());
            printStream.println(fromArray);
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format("ThreadConfig.dumpToFile: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
        finally {
            reentrantLock.unlock();
        }
        return "DUMPED: " + fileName;
    }
    
    static {
        TASK_SCHEDULER = new ThreadPoolTaskScheduler();
        TASK_EXECUTOR = new ThreadPoolTaskExecutor();
        TASK_SCHEDULER.initialize();
        TASK_EXECUTOR.initialize();
    }
    
    
    /**
     @return {@link #TASK_EXECUTOR}
     */
    public ThreadPoolTaskExecutor getTaskExecutor() {
        TASK_EXECUTOR.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
        TASK_EXECUTOR.getThreadPoolExecutor().setCorePoolSize(35);
        TASK_EXECUTOR.setQueueCapacity(500);
        TASK_EXECUTOR.setWaitForTasksToCompleteOnShutdown(true);
        TASK_EXECUTOR.setAwaitTerminationSeconds(6);
        TASK_EXECUTOR.setThreadPriority(7);
        TASK_EXECUTOR.setThreadNamePrefix("E-");
        return TASK_EXECUTOR;
    }
    
    public ThreadPoolTaskScheduler getTaskScheduler() {
        ScheduledThreadPoolExecutor scThreadPoolExecutor = TASK_SCHEDULER.getScheduledThreadPoolExecutor();
        scThreadPoolExecutor.setCorePoolSize(PROCESSORS);
        scThreadPoolExecutor.setMaximumPoolSize(20);
        scThreadPoolExecutor.setRemoveOnCancelPolicy(true);
        scThreadPoolExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        TASK_SCHEDULER.setErrorHandler(TaskUtils.LOG_AND_PROPAGATE_ERROR_HANDLER);
        TASK_SCHEDULER.prefersShortLivedTasks();
        TASK_SCHEDULER.setThreadNamePrefix("S-");
        TASK_SCHEDULER.setThreadPriority(2);
        TASK_SCHEDULER.setWaitForTasksToCompleteOnShutdown(false);
        TASK_SCHEDULER.setDaemon(true);
        return TASK_SCHEDULER;
    }
    
    @Contract(pure = true)
    public static ThreadConfig getI() {
        return THREAD_CONFIG_INST;
    }
    
    /**
     Killer
     */
    @SuppressWarnings("MethodWithMultipleLoops")
    public String killAll() {
        AppComponents.getUserPref();
        final StringBuilder builder = new StringBuilder();
        BlockingQueue<Runnable> runnableBlockingQueueSched = TASK_SCHEDULER.getScheduledThreadPoolExecutor().getQueue();
        BlockingQueue<Runnable> runnableBlockingQueue = TASK_EXECUTOR.getThreadPoolExecutor().getQueue();
        runnableBlockingQueue.clear();
        runnableBlockingQueueSched.clear();
        builder.append("CPU: ").append(UsefulUtilities.getOS()).append("\n");
        builder.append("MEMORY: ").append(UsefulUtilities.getMemory()).append("\n");
        builder.append(toString());
        FileSystemWorker.writeFile(this.getClass().getSimpleName() + ".killAll.txt", builder.toString());
        return builder.toString();
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ThreadConfig{");
        sb.append(TASK_EXECUTOR.getThreadPoolExecutor()).append(" TASK EXECUTOR, ");
        sb.append(TASK_SCHEDULER.getScheduledExecutor()).append(" TASK SCHEDULER.\n <p>");
        sb.append('}');
        return sb.toString();
    }
    
    public static @NotNull String thrNameSet(String className) {
    
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
        catch (RuntimeException e) {
            messageToUser.error(MessageFormat.format("ThreadConfig.execByThreadConfig {0} - {1}", e.getClass().getTypeName(), e.getMessage()));
            Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).execute(r);
            return false;
        }
    }
    
    /**
     @return executed or not
     
     @see ru.vachok.networker.exe.ThreadConfigTest#testExecByThreadConfig()
     */
    private boolean execByThreadConfig() {
        SimpleAsyncTaskExecutor simpleAsyncExecutor = new ASExec().getSimpleAsyncExecutor();
        if (simpleAsyncExecutor == null) {
            return false;
        }
        else {
            simpleAsyncExecutor.setThreadPriority(1);
            simpleAsyncExecutor.execute(r);
            return true;
        }
    }
    
    /**
     Асинхронный {@link ThreadPoolTaskExecutor}
     <p>
     
     @see AsyncConfigurerSupport
     @since <a href="https://github.com/Vachok/ftpplus/commit/f40030246ec6f28cc9c484b9c56a3879da1162af" target=_blank>21.02.2019 (22:49)</a>
     */
    private class ASExec extends AsyncConfigurerSupport {
    
    
        private SimpleAsyncTaskExecutor simpleAsyncExecutor = new SimpleAsyncTaskExecutor("A");
    
        ASExec() {
        }
        
        @Override
        public String toString() {
            boolean throttleActive = simpleAsyncExecutor.isThrottleActive();
            return throttleActive + " throttleActive. Concurrency limit : " + simpleAsyncExecutor.getConcurrencyLimit();
        }
    
        @Override
        public Executor getAsyncExecutor() {
            simpleAsyncExecutor.setConcurrencyLimit(50);
            simpleAsyncExecutor.setThreadPriority(6);
            simpleAsyncExecutor.setConcurrencyLimit(PROCESSORS - 2);
            System.out.println("simpleAsyncExecutor.isThrottleActive() = " + simpleAsyncExecutor.isThrottleActive());
            return new ExecutorServiceAdapter(simpleAsyncExecutor);
        }
    
        @Contract(pure = true)
        private SimpleAsyncTaskExecutor getSimpleAsyncExecutor() {
            return simpleAsyncExecutor;
        }
    }
}
