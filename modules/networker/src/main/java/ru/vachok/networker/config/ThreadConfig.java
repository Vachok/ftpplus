package ru.vachok.networker.config;


import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.CustomizableThreadCreator;
import ru.vachok.messenger.MessageCons;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.AppComponents;


/**
 @since 11.09.2018 (11:41) */
@SuppressWarnings({"MagicNumber", "FieldNotUsedInToString"})
@EnableAsync
public class ThreadConfig extends ThreadPoolTaskExecutor {

    /**
     <i>Boiler Plate</i>
     */
    private static final String STR_ACTIVE_COUNT = ", activeCount=";

    /**
     {@link ThreadPoolTaskExecutor}
     */
    private ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();

    /**
     {@link ThreadPoolTaskScheduler}
     */
    private ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();

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
     Убивает {@link #taskExecutor} и {@link #taskScheduler}
     */
    public void killAll() {
        taskExecutor.setAwaitTerminationSeconds(15);

        taskScheduler.setWaitForTasksToCompleteOnShutdown(false);
        taskExecutor.setWaitForTasksToCompleteOnShutdown(true);

        taskScheduler.shutdown();
        taskScheduler.destroy();

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

    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {

        taskExecutor.initialize();
        taskExecutor.setMaxPoolSize(100);
        taskExecutor.setThreadNamePrefix("ts-" + (System.currentTimeMillis() - ConstantsFor.START_STAMP) / 1000);
        return taskExecutor;
    }

    @Override
    public String toString() {
        this.taskExecutor = threadPoolTaskExecutor();
        this.taskScheduler = threadPoolTaskScheduler();

        final StringBuilder sb = new StringBuilder("ThreadConfig{");
        sb.append("taskExecutor=").append(taskExecutor.getThreadNamePrefix());
        sb.append(STR_ACTIVE_COUNT).append(threadPoolTaskExecutor().getActiveCount());
        sb.append(", taskScheduler=").append(taskScheduler.getThreadNamePrefix());
        sb.append(STR_ACTIVE_COUNT).append(threadPoolTaskScheduler().getActiveCount());
        sb.append('}');
        return sb.toString();
    }

    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        taskScheduler.initialize();
        taskScheduler.setThreadNamePrefix("sch-" + (System.currentTimeMillis() - ConstantsFor.START_STAMP) / 1000);
        taskScheduler.setPoolSize(10);
        return taskScheduler;
    }
}
