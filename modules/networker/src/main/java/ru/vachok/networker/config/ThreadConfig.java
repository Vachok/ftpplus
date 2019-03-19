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

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 Конфигуратор для {@link ThreadPoolTaskExecutor}
 <p>
 
 @since 11.09.2018 (11:41) */
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
    private static final String EXECUTE_AS_THREAD_METHNAME = "ThreadConfig.executeAsThread";
    
    private static final ThreadLocal<Float> upTimer = ThreadLocal.withInitial(()->(System.currentTimeMillis() - ConstantsFor.START_STAMP) / 1000 / ConstantsFor.ONE_HOUR_IN_MIN);
    
    /**
     {@link MessageLocal}
     */
    private static MessageToUser messageToUser = new MessageLocal();
    
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
        TASK_EXECUTOR.getThreadPoolExecutor().setCorePoolSize(99);
        TASK_EXECUTOR.setQueueCapacity(700);
        TASK_EXECUTOR.setWaitForTasksToCompleteOnShutdown(true);
        TASK_EXECUTOR.setAwaitTerminationSeconds(8);
        TASK_EXECUTOR.setThreadPriority(6);
        TASK_EXECUTOR.setThreadNamePrefix("TE-");
        TASK_EXECUTOR.setRejectedExecutionHandler(new TaskDestroyer());
        
        BlockingQueue<Runnable> poolExecutor = TASK_EXECUTOR.getThreadPoolExecutor().getQueue();
        final String bodyMsg = " = " + prestartCoreThread + new TForms().fromArray(poolExecutor, false);
        FileSystemWorker.writeFile("getTaskExecutor.txt", bodyMsg);
        messageToUser.info(
            "ThreadConfig.getTaskExecutor",
            "prestartCoreThread",
            bodyMsg);
        messageToUser.info("ThreadConfig.getTaskExecutor", "ScheduledThreadPoolExecutor", " = " + TASK_SCHEDULER.getScheduledThreadPoolExecutor());
        return TASK_EXECUTOR;
    }
    
    public static ThreadConfig getI() {
        return THREAD_CONFIG_INST;
    }
    
    public ThreadPoolTaskScheduler getTaskScheduler() {
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = TASK_SCHEDULER.getScheduledThreadPoolExecutor();
        scheduledThreadPoolExecutor.setCorePoolSize(20);
        TASK_SCHEDULER.setThreadNamePrefix("TS");
        TASK_SCHEDULER.setThreadPriority(2);
        TASK_SCHEDULER.setWaitForTasksToCompleteOnShutdown(false);
        TASK_SCHEDULER.setDaemon(true);
        TASK_SCHEDULER.prefersShortLivedTasks();
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
        AtomicBoolean retBool = new AtomicBoolean(false);
        CustomizableThreadCreator customizableThreadCreator = new CustomizableThreadCreator("AsThread: ");
        customizableThreadCreator.setThreadPriority(9);
        Thread thread = customizableThreadCreator.createThread(r);
        Executor asyncExecutor = null;
        if (new ASExec(TASK_EXECUTOR).getAsyncExecutor() != null) {
            asyncExecutor = new ASExec(TASK_EXECUTOR).getAsyncExecutor();
        } else {
            if (upTimer.get() > ConstantsFor.ONE_HOUR_IN_MIN) {
                upTimer.set(upTimer.get() / ConstantsFor.ONE_HOUR_IN_MIN);
            }
            thrNameSet("ASThr");
            messageToUser.errorAlert(getClass().getSimpleName(), "asyncExecutor is " + null, thread.getName());
        }
        if (asyncExecutor != null) {
            asyncExecutor.execute(thread::start);
            retBool.set(true);
        } else {
            thread.start();
            retBool.set(false);
            new MessageSwing().errorAlert(EXECUTE_AS_THREAD_METHNAME, "thread.isAlive()", " = " + thread.isAlive());
            new TaskDestroyer().rejectedExecution(r, TASK_EXECUTOR.getThreadPoolExecutor());
        }
        return retBool.get();
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ThreadConfig{");
        sb.append(", THREAD_CONFIG_INST=").append(THREAD_CONFIG_INST.hashCode());
        sb.append(", upTimer=").append(upTimer.get());
        sb.append("\n");
        sb.append(", <br><font color=\"#fcf594\">TASK_SCHEDULER=").append(TASK_SCHEDULER.getScheduledThreadPoolExecutor());
        sb.append(", <br>TASK_EXECUTOR=").append(TASK_EXECUTOR.getThreadPoolExecutor());
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
        
        
        private ThreadPoolTaskExecutor threadPoolTaskExecutor;
        
        ASExec(ThreadPoolTaskExecutor threadPoolTaskExecutor) {
            threadPoolTaskExecutor.getThreadPoolExecutor().purge();
            this.threadPoolTaskExecutor = threadPoolTaskExecutor;
            threadPoolTaskExecutor.setRejectedExecutionHandler(new TaskDestroyer());
        }
        
        @Override
        public Executor getAsyncExecutor() {
            thrNameSet("A-");
            threadPoolTaskExecutor.setAllowCoreThreadTimeOut(true);
            threadPoolTaskExecutor.setThreadPriority(9);
            threadPoolTaskExecutor.setThreadNamePrefix("A-");
            threadPoolTaskExecutor.setRejectedExecutionHandler(new TaskDestroyer());
            return threadPoolTaskExecutor;
        }
        
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("ASExec{");
            sb.append("threadPoolTaskExecutor=").append(threadPoolTaskExecutor.getThreadPoolExecutor());
            sb.append('}');
            return sb.toString();
        }
    }
    
    
    /**
     Повторная попытка для задания.
     
     @since 22.02.2019 (13:22)
     */
    private class TasksReRunner implements RejectedExecutionHandler {
        
        
        private static final String CLASS_REJECTEDEXEC_METH = "TasksReRunner.rejectedExecution";
        
        private MessageToUser messageToUser = new MessageSwing();
        
        private Runnable reTask;
        
        private void resultOfExecution(boolean submitDone) {
            if (submitDone) {
                messageToUser.info(getClass().getSimpleName(), "resultOfExecution", reTask + " : " + true);
            } else {
                messageToUser = new MessageSwing();
                messageToUser.infoTimer((int) ConstantsFor.DELAY, getClass().getSimpleName() + " resultOfExecution " + reTask + " : " + false);
            }
        }
        
        @Override
        public void rejectedExecution(Runnable rejectedTask, ThreadPoolExecutor executor) {
            this.reTask = rejectedTask;
            messageToUser.error(CLASS_REJECTEDEXEC_METH, "rejectedTask", " = " + rejectedTask);
            try {
                ExecutorService serviceAdapter = new ExecutorServiceAdapter((TaskExecutor) executor);
                Future<?> submit = serviceAdapter.submit(reTask);
                submit.get(ConstantsFor.DELAY, TimeUnit.SECONDS);
                resultOfExecution(submit.isDone());
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                messageToUser.errorAlert("TasksReRunner", "rejectedExecution", e.getMessage());
                FileSystemWorker.error(CLASS_REJECTEDEXEC_METH, e);
                Thread.currentThread().interrupt();
            }
        }
        
        @Override
        public int hashCode() {
            int result = messageToUser.hashCode();
            result = 31 * result + (reTask != null ? reTask.hashCode() : 0);
            return result;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            
            ThreadConfig.TasksReRunner that = (ThreadConfig.TasksReRunner) o;
            
            return messageToUser.equals(that.messageToUser) && (reTask != null ? reTask.equals(that.reTask) : that.reTask == null);
        }
        
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("TasksReRunner{");
            sb.append("CLASS_REJECTEDEXEC_METH='").append(CLASS_REJECTEDEXEC_METH).append('\'');
            sb.append(", reTask=").append(reTask);
            sb.append('}');
            return sb.toString();
        }
    }
    
    
    private class TaskDestroyer implements RejectedExecutionHandler {
        
        
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            BlockingQueue<Runnable> queue = executor.getQueue();
            queue.forEach(queue::remove);
            executor.purge();
        }
    }
}
