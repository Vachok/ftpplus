// Copyright (c) all rights. http://networker.vachok.ru 2019.

/*
 * Copyright (c) 2019.
 */

/*
 * Copyright (c) 2019.
 */

package ru.vachok.networker.config;


import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.support.ExecutorServiceAdapter;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.util.CustomizableThreadCreator;
import ru.vachok.messenger.MessageSwing;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;
import ru.vachok.networker.sysinfo.ThreadInformator;

import java.util.concurrent.*;


/**
 Конфигуратор для {@link ThreadPoolTaskExecutor}
 <p>

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

    /**
     Название метода
     */
    private static final String EXECUTE_AS_THREAD_METH_NAME = "ThreadConfig.executeAsThread";

    /**
     {@link MessageLocal}
     */
    private static MessageToUser messageToUser = new MessageLocal(ThreadConfig.class.getSimpleName());

    private ThreadConfig() {
        thrNameSet("tc_" + hashCode());
    }
    
    static {
        TASK_SCHEDULER = new ThreadPoolTaskScheduler();
        TASK_SCHEDULER.initialize();

        TASK_EXECUTOR = new ThreadPoolTaskExecutor();
        TASK_EXECUTOR.initialize();
    }

    /**
     @return {@link #TASK_EXECUTOR}
     */
    public ThreadPoolTaskExecutor getTaskExecutor() {
        boolean prestartCoreThread = TASK_EXECUTOR.getThreadPoolExecutor().prestartCoreThread();
        TASK_EXECUTOR.getThreadPoolExecutor().setCorePoolSize(900);
        TASK_EXECUTOR.setQueueCapacity(1800);
        TASK_EXECUTOR.setWaitForTasksToCompleteOnShutdown(true);
        TASK_EXECUTOR.setAwaitTerminationSeconds(7);
        TASK_EXECUTOR.setThreadPriority(4);
        TASK_EXECUTOR.setThreadNamePrefix("EX");
        TASK_EXECUTOR.setRejectedExecutionHandler(new TaskDestroyer());

        BlockingQueue<Runnable> poolExecutor = TASK_EXECUTOR.getThreadPoolExecutor().getQueue();
        StringBuilder bodyMsgB = new StringBuilder();
        bodyMsgB.append("BlockingQueue<Runnable> poolExecutor:\n");
        bodyMsgB.append(" = ").append(prestartCoreThread).append(new TForms().fromArray(poolExecutor, false));
        bodyMsgB.append("\nthis: ").append(this);
        FileSystemWorker.writeFile("getTaskExecutor.txt", bodyMsgB.toString());
        return TASK_EXECUTOR;
    }


    public static ThreadConfig getI() {
        return THREAD_CONFIG_INST;
    }


    public ThreadPoolTaskScheduler getTaskScheduler() {
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = TASK_SCHEDULER.getScheduledThreadPoolExecutor();
        scheduledThreadPoolExecutor.setCorePoolSize(20);
        scheduledThreadPoolExecutor.setMaximumPoolSize(50);
        scheduledThreadPoolExecutor.setRemoveOnCancelPolicy(true);
        TASK_SCHEDULER.setThreadNamePrefix("TS");
        TASK_SCHEDULER.setThreadPriority(3);
        TASK_SCHEDULER.setWaitForTasksToCompleteOnShutdown(false);
        TASK_SCHEDULER.setDaemon(true);
        TASK_SCHEDULER.setRejectedExecutionHandler(new TasksReRunner());
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


    /**
     Запуск {@link Runnable}, как {@link Thread}@param r {@link Runnable}
     <p>
     1. {@link ThreadConfig#getTaskExecutor()} - управление запуском.
     <p>

     @param r {@link Runnable}
     */
    public boolean execByThreadConfig(Runnable r) {
        CustomizableThreadCreator customizableThreadCreator = new CustomizableThreadCreator("AsThread: ");
        customizableThreadCreator.setThreadPriority(9);
        Thread thread = customizableThreadCreator.createThread(r);
        Executor asyncExecutor = Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor());
        if (new ASExec().getAsyncExecutor() != null) {
            asyncExecutor = new ASExec().getAsyncExecutor();
        } else {
            thrNameSet("ASThr");
            messageToUser.errorAlert(getClass().getSimpleName(), "asyncExecutor is " + null, thread.getName());
        }
        if (asyncExecutor != null) {
            asyncExecutor.execute(r);
            return true;
        } else {
            thread.start();
            messageToUser.error(EXECUTE_AS_THREAD_METH_NAME , "thread.isAlive()" , " = " + thread.isAlive());
            new TaskDestroyer().rejectedExecution(r, TASK_EXECUTOR.getThreadPoolExecutor());
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
        sb.append(new ThreadInformator().getInfoAbout());
        sb.append("</font>}");
        return sb.toString();
    }


    /**
     Асинхронный {@link ThreadPoolTaskExecutor}
     <p>

     @see AsyncConfigurerSupport
     @since <a href="https://github.com/Vachok/ftpplus/commit/f40030246ec6f28cc9c484b9c56a3879da1162af" target=_blank>21.02.2019 (22:49)</a>
     */
    private class ASExec extends AsyncConfigurerSupport {

        private Executor threadPoolTaskExecutor;

        ASExec() {
            this.threadPoolTaskExecutor = Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor());
        }

        @Override
        public Executor getAsyncExecutor() {
            thrNameSet("ASE");
            return threadPoolTaskExecutor;
        }
    }


    /**
     Повторная попытка для задания.

     @since 22.02.2019 (13:22)
     */
    private class TasksReRunner implements RejectedExecutionHandler {

        private MessageToUser messageToUser;


        private TasksReRunner() {
            try {
                messageToUser = new MessageSwing(this.getClass().getSimpleName());
            } catch (Exception e) {
                FileSystemWorker.error(getClass().getSimpleName() + ".TasksReRunner" , e);
                messageToUser = new MessageLocal(getClass().getSimpleName());
            }
        }


        @Override
        public void rejectedExecution(Runnable rejectedTask, ThreadPoolExecutor executor) {
            try {
                ExecutorService serviceAdapter = new ExecutorServiceAdapter((TaskExecutor) executor);
                Future<?> submit = serviceAdapter.submit(rejectedTask);
                submit.get(ConstantsFor.DELAY, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".rejectedExecution" , e));
                Thread.currentThread().checkAccess();
                Thread.currentThread().interrupt();
            }
        }
    }


    private class TaskDestroyer implements RejectedExecutionHandler {
    
    
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            BlockingQueue<Runnable> queue = executor.getQueue();
            queue.forEach(queue::remove);
            executor.purge();
            messageToUser.warn("rejectedTask !", "executor is purged", " = " + executor);
        }
    }
    
    
    
}
