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

import java.time.LocalDateTime;
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

    private static final String INITIALIZED = " initialized";

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

    /**
     {@link ThreadPoolTaskExecutor}
     */
    private ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();

    static {
        new MessageCons().errorAlert("ThreadConfig.static initializer");
        taskSchedulerConf();
    }

    public Runnable taskDecorator(Runnable runnable) {
        new MessageCons().errorAlert("ThreadConfig.taskDecorator");
        new MessageCons().info(ConstantsFor.STR_INPUT_OUTPUT, "runnable = [" + runnable + "]", "java.lang.Runnable");
        TaskDecorator taskDecorator = runnable1 -> runnable;
        String msg = taskDecorator.toString() + " " + this.getClass().getSimpleName() + ".taskDecorator(Runnable runnable)";
        AppComponents.getLogger().info(msg);
        return taskDecorator.decorate(runnable);
    }

    public static void executeAsThread(Runnable runnable) {
        executeAsThread(runnable, false);
    }

    /**
     Убивает {@link #taskExecutor} и {@link #TASK_SCHEDULER}
     */
    public void killAll() {
        taskExecutor.setAwaitTerminationSeconds(15);

        TASK_SCHEDULER.setWaitForTasksToCompleteOnShutdown(false);
        taskExecutor.setWaitForTasksToCompleteOnShutdown(true);

        TASK_SCHEDULER.shutdown();
        TASK_SCHEDULER.destroy();

        taskExecutor.shutdown();
        taskExecutor.destroy();
    }

    /**
     Запуск {@link Runnable}, как {@link Thread}

     @param r {@link Runnable}
     */
    public static void executeAsThread(Runnable r, boolean asDaemoExec) {
        CustomizableThreadCreator customizableThreadCreator = new CustomizableThreadCreator("AsThread: ");
        ThreadPoolTaskExecutor taskExecutor = new ThreadConfig().threadPoolTaskExecutor();
        customizableThreadCreator.setThreadGroup(taskExecutor.getThreadGroup());
        Thread thread = customizableThreadCreator.createThread(r);
        thread.setDaemon(asDaemoExec);
        thread.start();
    }

    public static void taskSchedulerConf() {
        TASK_SCHEDULER.initialize();
        new MessageCons().info("ThreadConfig.threadPoolTaskScheduler", INITIALIZED, LocalDateTime.now().toString());
        TASK_SCHEDULER.setThreadNamePrefix("schMIN-" + TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - ConstantsFor.START_STAMP));
        TASK_SCHEDULER.setPoolSize(15);
    }

    @Override
    public String toString() {
        this.taskExecutor = threadPoolTaskExecutor();
        final StringBuilder sb = new StringBuilder("ThreadConfig{");
        sb.append("taskExecutor=").append(taskExecutor.getThreadNamePrefix());
        sb.append(STR_ACTIVE_COUNT).append(threadPoolTaskExecutor().getActiveCount());
        sb.append(", TASK_SCHEDULER=").append(TASK_SCHEDULER.getThreadNamePrefix());
        sb.append(STR_ACTIVE_COUNT).append(TASK_SCHEDULER.getActiveCount());
        sb.append('}');
        return sb.toString();
    }

    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        taskExecutor.initialize();
        taskExecutor.setQueueCapacity(500);
        new MessageCons().info("ThreadConfig.threadPoolTaskExecutor", INITIALIZED, LocalDateTime.now().toString());
        taskExecutor.setMaxPoolSize(100);
        taskExecutor.setThreadNamePrefix("ts-" + (System.currentTimeMillis() - ConstantsFor.START_STAMP) / 1000);
        return taskExecutor;
    }

}
