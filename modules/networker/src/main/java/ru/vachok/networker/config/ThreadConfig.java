package ru.vachok.networker.config;


import org.springframework.scheduling.annotation.Async;
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
import ru.vachok.networker.services.MessageLocal;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;


/**
 @since 11.09.2018 (11:41) */
@SuppressWarnings ({"MagicNumber", "FieldNotUsedInToString"})
@EnableAsync
@Service
public class ThreadConfig extends ThreadPoolTaskExecutor {

    private static final String CLASS_THREAD_CONFIG_METH = "ThreadConfig.ThreadConfig";

    private static final String TASK_EXECUTOR_STR = "TASK_EXECUTOR = ";

    private static final String GET_ASYNC_EXECUTOR_METHNAME = "ThreadConfig.getAsyncExecutor";

    /**
     {@link ThreadPoolTaskScheduler}
     */
    private static final ThreadPoolTaskScheduler TASK_SCHEDULER;

    private static final ThreadPoolTaskExecutor TASK_EXECUTOR;

    /**
     <i>Boiler Plate</i>
     */
    private static final String STR_ACTIVE_COUNT = ", activeCount=";

    private static final ThreadConfig THREAD_CONFIG = new ThreadConfig();

    private static final String EXECUTE_AS_THREAD_METHNAME = "ThreadConfig.executeAsThread";

    private static MessageToUser messageToUser = new MessageLocal();

    private static float upTimer = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - ConstantsFor.START_STAMP);

    public ThreadPoolTaskExecutor getTaskExecutor() {
        TASK_EXECUTOR.setQueueCapacity(500);
        TASK_EXECUTOR.setWaitForTasksToCompleteOnShutdown(true);
        TASK_EXECUTOR.setAwaitTerminationSeconds(7);
        TASK_EXECUTOR.setThreadPriority(6);
        TASK_EXECUTOR.setThreadNamePrefix("run-" + (System.currentTimeMillis() - ConstantsFor.START_STAMP) / 1000 / ConstantsFor.ONE_HOUR_IN_MIN);

        messageToUser.info("ThreadConfig.getTaskExecutor", TASK_EXECUTOR_STR, TASK_EXECUTOR.getThreadPoolExecutor().toString());
        return TASK_EXECUTOR;
    }

    public static ThreadConfig getI() {
        return THREAD_CONFIG;
    }

    public ThreadPoolTaskScheduler getTaskScheduler() {
        return TASK_SCHEDULER;
    }

    private ThreadConfig() {
    }

    /**
     Killer
     */
    public void killAll() {
        TASK_EXECUTOR.setAwaitTerminationSeconds(15);

        TASK_SCHEDULER.setWaitForTasksToCompleteOnShutdown(false);
        TASK_EXECUTOR.setWaitForTasksToCompleteOnShutdown(true);

        TASK_SCHEDULER.shutdown();
        TASK_SCHEDULER.destroy();

        TASK_EXECUTOR.shutdown();
        TASK_EXECUTOR.destroy();
    }

    static {
        TASK_SCHEDULER = new ThreadPoolTaskScheduler();
        TASK_EXECUTOR = new ThreadPoolTaskExecutor();
        TASK_EXECUTOR.initialize();
        TASK_SCHEDULER.initialize();
    }

    /**
     Запуск {@link Runnable}, как {@link Thread}@param r {@link Runnable}
     <p>
     1. {@link ThreadConfig#getTaskExecutor()} - управление запуском.
     <p>

     @param r {@link Runnable}
     */
    @Async
    public void executeAsThread(Runnable r) {
        CustomizableThreadCreator customizableThreadCreator = new CustomizableThreadCreator("AsThread: ");
        customizableThreadCreator.setThreadPriority(9);
        Thread thread = customizableThreadCreator.createThread(r);
        Executor asyncExecutor;
        if(new ASExec().getAsyncExecutor()!=null){
            asyncExecutor = new ASExec().getAsyncExecutor();
        }
        else{
            asyncExecutor = TASK_EXECUTOR;
        }
        if(asyncExecutor!=null){
            asyncExecutor.execute(thread);
            messageToUser.info(EXECUTE_AS_THREAD_METHNAME, "thread.toString()", " = " + thread.toString());
        }
        else{
            thread.start();
            new MessageSwing().errorAlert(EXECUTE_AS_THREAD_METHNAME, "thread.isAlive()", " = " + thread.isAlive());
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ThreadConfig{");
        sb.append("taskExecutor=").append(TASK_EXECUTOR.getThreadNamePrefix());
        sb.append(STR_ACTIVE_COUNT).append(getTaskExecutor().getActiveCount());
        sb.append(", TASK_SCHEDULER=").append(TASK_SCHEDULER.getThreadNamePrefix());
        sb.append(STR_ACTIVE_COUNT).append(TASK_SCHEDULER.getActiveCount());
        sb.append('}');
        return sb.toString();
    }

    public static void taskSchedulerConf() {
        TASK_SCHEDULER.setThreadNamePrefix("schMIN-" + upTimer);
        TASK_SCHEDULER.setPoolSize(15);
    }

    class ASExec extends AsyncConfigurerSupport {

        @Override
        public Executor getAsyncExecutor() {
            messageToUser = new MessageLocal();
            if(upTimer > ConstantsFor.ONE_HOUR_IN_MIN){
                upTimer = upTimer / ConstantsFor.ONE_HOUR_IN_MIN;
            }
            TASK_EXECUTOR.setThreadGroupName("A-" + upTimer);
            int countAllCoreThreads = TASK_EXECUTOR.getThreadPoolExecutor().prestartAllCoreThreads();

            messageToUser.info(GET_ASYNC_EXECUTOR_METHNAME, "upTimer", " = " + upTimer);
            messageToUser.errorAlert(getClass().getSimpleName(), GET_ASYNC_EXECUTOR_METHNAME, TASK_EXECUTOR.getThreadPoolExecutor().toString());

            BlockingQueue<Runnable> runnableBlockingQueue = TASK_EXECUTOR.getThreadPoolExecutor().getQueue();
            messageToUser.infoNoTitles(new TForms().fromArray(runnableBlockingQueue, false));
            return TASK_EXECUTOR;
        }
    }
}
