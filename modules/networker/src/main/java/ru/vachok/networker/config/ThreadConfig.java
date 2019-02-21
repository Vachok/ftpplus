package ru.vachok.networker.config;


import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.util.CustomizableThreadCreator;
import ru.vachok.messenger.MessageCons;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.AppComponents;

import java.util.concurrent.TimeUnit;


/**
 @since 11.09.2018 (11:41) */
@SuppressWarnings({"MagicNumber", "FieldNotUsedInToString"})
@EnableAsync
@Service
public class ThreadConfig extends ThreadPoolTaskExecutor {

    /**
     {@link ThreadPoolTaskScheduler}
     */
    private static final ThreadPoolTaskScheduler TASK_SCHEDULER = new ThreadPoolTaskScheduler();

    private static final ThreadPoolTaskExecutor TASK_EXECUTOR = new ThreadPoolTaskExecutor();

    private static final ThreadConfig THREAD_CONFIG = new ThreadConfig();

    public static ThreadConfig getI() {
        return THREAD_CONFIG;
    }

    public ThreadPoolTaskScheduler getTaskScheduler() {
        return TASK_SCHEDULER;
    }

    private ThreadConfig() {
    }

    /**
     <i>Boiler Plate</i>
     */
    private static final String STR_ACTIVE_COUNT = ", activeCount=";

    public Runnable taskDecorator(Runnable runnable) {
        new MessageCons().info(ConstantsFor.STR_INPUT_OUTPUT, "runnable = [" + runnable + "]", "java.lang.Runnable");
        TaskDecorator taskDecorator = runnable1 -> runnable;
        String msg = taskDecorator.toString() + " " + this.getClass().getSimpleName() + ".taskDecorator(Runnable runnable)";
        AppComponents.getLogger().info(msg);
        return taskDecorator.decorate(runnable);
    }

    public ThreadPoolTaskExecutor getTaskExecutor() {
        TASK_EXECUTOR.initialize();
        TASK_EXECUTOR.setQueueCapacity(500);
        TASK_EXECUTOR.setMaxPoolSize(100);
        TASK_EXECUTOR.setThreadNamePrefix("ts-" + (System.currentTimeMillis() - ConstantsFor.START_STAMP) / 1000);
        return TASK_EXECUTOR;
    }

    /**
     Убивает {@link #taskExecutor} и {@link #TASK_SCHEDULER}
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

    public static void taskSchedulerConf() {
        TASK_SCHEDULER.initialize();
        TASK_SCHEDULER.setThreadNamePrefix("schMIN-" + TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - ConstantsFor.START_STAMP));
        TASK_SCHEDULER.setPoolSize(15);
    }

    /**
     Запуск {@link Runnable}, как {@link Thread}@param r {@link Runnable}
     <p>
     1. {@link ThreadConfig#getTaskExecutor()} - управление запуском.
     <p>
     @param r {@link Runnable}
     */
    public static void executeAsThread(Runnable r) {
        CustomizableThreadCreator customizableThreadCreator = new CustomizableThreadCreator("AsThread: ");
        ThreadPoolTaskExecutor taskExecutor = new ThreadConfig().getTaskExecutor();
        customizableThreadCreator.setThreadGroup(taskExecutor.getThreadGroup());
        Thread thread = customizableThreadCreator.createThread(r);
        thread.setDaemon(false);
        thread.start();
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
}
