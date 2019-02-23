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

    private static final ThreadLocal<Float> upTimer = ThreadLocal.withInitial(() -> (System.currentTimeMillis() - ConstantsFor.START_STAMP) / 1000 / ConstantsFor.ONE_HOUR_IN_MIN);

    /**
     {@link MessageLocal}
     */
    private transient MessageToUser messageToUser = new MessageLocal();

    /**
     @return {@link #TASK_EXECUTOR}
     */
    public ThreadPoolTaskExecutor getTaskExecutor() {
        boolean prestartCoreThread = TASK_EXECUTOR.getThreadPoolExecutor().prestartCoreThread();
        TASK_EXECUTOR.getThreadPoolExecutor().setCorePoolSize(7);
        TASK_EXECUTOR.setQueueCapacity(500);
        TASK_EXECUTOR.setWaitForTasksToCompleteOnShutdown(true);
        TASK_EXECUTOR.setAwaitTerminationSeconds(7);
        TASK_EXECUTOR.setThreadPriority(6);
        TASK_EXECUTOR.setThreadNamePrefix("TASK-" + (System.currentTimeMillis() - ConstantsFor.START_STAMP) / 1000 / ConstantsFor.ONE_HOUR_IN_MIN);
        BlockingQueue<Runnable> poolExecutor = TASK_EXECUTOR.getThreadPoolExecutor().getQueue();
        messageToUser.info("ThreadConfig.getTaskExecutor", "prestartCoreThread", " = " + prestartCoreThread + new TForms().fromArray(poolExecutor, false));
        return TASK_EXECUTOR;
    }

    public static ThreadConfig getI() {
        return THREAD_CONFIG_INST;
    }

    public ThreadPoolTaskScheduler getTaskScheduler() {
        float localUptimer = upTimer.get();
        if (localUptimer > ConstantsFor.ONE_HOUR_IN_MIN) {
            localUptimer /= ConstantsFor.ONE_HOUR_IN_MIN;
        }
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = TASK_SCHEDULER.getScheduledThreadPoolExecutor();
        scheduledThreadPoolExecutor.setCorePoolSize(5);
        TASK_SCHEDULER.setThreadNamePrefix("SCHED-" + localUptimer);
        return TASK_SCHEDULER;
    }

    static {
        TASK_SCHEDULER = new ThreadPoolTaskScheduler();
        TASK_SCHEDULER.initialize();

        TASK_EXECUTOR = new ThreadPoolTaskExecutor();
        TASK_EXECUTOR.initialize();
    }

    private ThreadConfig() {
    }

    /**
     Killer
     */
    public void killAll() {
        TASK_SCHEDULER.shutdown();
        this.messageToUser = new MessageLocal();
        final StringBuilder builder = new StringBuilder();

        for (Runnable runnable : TASK_SCHEDULER.getScheduledThreadPoolExecutor().shutdownNow()) {
            builder.append(runnable.toString()).append("\n");
        }
        TASK_EXECUTOR.shutdown();
    }

    /**
     Запуск {@link Runnable}, как {@link Thread}@param r {@link Runnable}
     <p>
     1. {@link ThreadConfig#getTaskExecutor()} - управление запуском.
     <p>

     @param r {@link Runnable}
     */
    public void executeAsThread(Runnable r) {
        CustomizableThreadCreator customizableThreadCreator = new CustomizableThreadCreator("AsThread: ");
        customizableThreadCreator.setThreadPriority(9);
        Thread thread = customizableThreadCreator.createThread(r);
        Executor asyncExecutor = null;
        if (new ASExec(TASK_EXECUTOR).getAsyncExecutor() != null) {
            asyncExecutor = new ASExec(TASK_EXECUTOR).getAsyncExecutor();
        } else {
            if(upTimer.get() > 60){
                upTimer.set(upTimer.get() / ConstantsFor.ONE_HOUR_IN_MIN);
            }
            thread.setName("ASYN-Null-" + upTimer.get());
            messageToUser.errorAlert(getClass().getSimpleName(), "asyncExecutor is " + null, thread.getName());
        }
        if (asyncExecutor != null) {
            asyncExecutor.execute(thread::start);
            messageToUser.info(EXECUTE_AS_THREAD_METHNAME, "thread.toString()", " = " + thread.toString());
        } else {
            thread.start();
            new MessageSwing().errorAlert(EXECUTE_AS_THREAD_METHNAME, "thread.isAlive()", " = " + thread.isAlive());
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ThreadConfig{");
        sb.append(", THREAD_CONFIG_INST=").append(THREAD_CONFIG_INST.hashCode());
        sb.append(", upTimer=").append(upTimer.get());
        sb.append("\n");
        sb.append(", TASK_SCHEDULER=").append(TASK_SCHEDULER.getScheduledThreadPoolExecutor().toString());
        sb.append(", TASK_EXECUTOR=").append(TASK_EXECUTOR.getThreadPoolExecutor().toString());
        sb.append('}');
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
            threadPoolTaskExecutor.setRejectedExecutionHandler(new TasksReRunner());
        }

        @Override
        public Executor getAsyncExecutor() {
            float localUptimer = upTimer.get();
            if (localUptimer > ConstantsFor.ONE_HOUR_IN_MIN) {
                localUptimer /= ConstantsFor.ONE_HOUR_IN_MIN;
            }
            threadPoolTaskExecutor.setAllowCoreThreadTimeOut(true);
            threadPoolTaskExecutor.setThreadPriority(10);
            threadPoolTaskExecutor.setThreadNamePrefix("ASyn-" + localUptimer);
            return threadPoolTaskExecutor;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("ASExec{");
            sb.append("threadPoolTaskExecutor=").append(threadPoolTaskExecutor.getThreadPoolExecutor().toString());
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
                messageToUser.info(getClass().getSimpleName(), "resultOfExecution", String.valueOf(true));
            } else {
                messageToUser = new MessageSwing();
                messageToUser.infoTimer((int) ConstantsFor.DELAY, getClass().getSimpleName() + " resultOfExecution " + reTask.toString() + " " + false);
            }
        }

        @Override
        public void rejectedExecution(Runnable rejectedTask, ThreadPoolExecutor executor) {
            this.reTask = rejectedTask;
            messageToUser.info(CLASS_REJECTEDEXEC_METH, "rejectedTask", " = " + rejectedTask);

            try {
                ExecutorServiceAdapter serviceAdapter = new ExecutorServiceAdapter((TaskExecutor) executor);
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

            TasksReRunner that = (TasksReRunner) o;

            return messageToUser.equals(that.messageToUser) && (reTask!=null? reTask.equals(that.reTask): that.reTask==null);
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("TasksReRunner{");
            sb.append("CLASS_REJECTEDEXEC_METH='").append(CLASS_REJECTEDEXEC_METH).append('\'');
            sb.append(", reTask=").append(reTask.toString());
            sb.append('}');
            return sb.toString();
        }
    }
}
