package ru.vachok.networker.config;


import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.CustomizableThreadCreator;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.AppComponents;


/**
 @since 11.09.2018 (11:41) */
@SuppressWarnings("MagicNumber")
@EnableAsync
public class ThreadConfig extends ThreadPoolTaskExecutor {

    /**
     {@link ThreadPoolTaskExecutor}
     */
    private static final ThreadPoolTaskExecutor TASK_EXECUTOR = new ThreadPoolTaskExecutor();

    /**
     {@link ThreadPoolTaskScheduler}
     */
    private static final ThreadPoolTaskScheduler TASK_SCHEDULER = new ThreadPoolTaskScheduler();

    /**
     Запуск {@link Runnable}, как {@link Thread}

     @param r {@link Runnable}
     */
    public static void executeAsThread(Runnable r) {
        CustomizableThreadCreator customizableThreadCreator = new CustomizableThreadCreator("OnChk: ");
        customizableThreadCreator.setThreadGroup(TASK_EXECUTOR.getThreadGroup());
        Thread thread = customizableThreadCreator.createThread(r);
        thread.start();
    }

    public Runnable taskDecorator(Runnable runnable) {
        TaskDecorator taskDecorator = runnable1 -> runnable;
        String msg = taskDecorator.toString() + " " + this.getClass().getSimpleName() + ".taskDecorator(Runnable runnable)";
        AppComponents.getLogger().info(msg);
        return taskDecorator.decorate(runnable);
    }

    /**
     Убивает {@link #TASK_EXECUTOR} и {@link #TASK_SCHEDULER}
     */
    public void killAll() {
        threadPoolTaskScheduler().destroy();
        Thread.currentThread().checkAccess();
        Thread.currentThread().interrupt();
        threadPoolTaskExecutor().setAwaitTerminationSeconds(15);
        threadPoolTaskExecutor().destroy();

    }

    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        TASK_SCHEDULER.destroy();
        TASK_SCHEDULER.setThreadNamePrefix("sc-" + (System.currentTimeMillis() - ConstantsFor.START_STAMP) / 1000);
        TASK_SCHEDULER.setPoolSize(4);
        TASK_SCHEDULER.initialize();
        return TASK_SCHEDULER;
    }

    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        TASK_EXECUTOR.destroy();
        TASK_EXECUTOR.setMaxPoolSize(100);
        TASK_EXECUTOR.setThreadNamePrefix("ts-" + (System.currentTimeMillis() - ConstantsFor.START_STAMP) / 1000);
        TASK_EXECUTOR.initialize();
        return TASK_EXECUTOR;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ThreadConfig{");
        sb.append("TASK_EXECUTOR=").append(TASK_EXECUTOR);
        sb.append(", TASK_SCHEDULER=").append(TASK_SCHEDULER);
        sb.append(", threadPoolTaskExecutor=").append(threadPoolTaskExecutor());
        sb.append(", threadPoolTaskScheduler=").append(threadPoolTaskScheduler());
        sb.append('}');
        return sb.toString();
    }
}
