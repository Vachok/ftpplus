package ru.vachok.networker.config;


import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.AppComponents;


/**
 @since 11.09.2018 (11:41) */
@SuppressWarnings ("MagicNumber")
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

    public Runnable taskDecorator(Runnable runnable) {
        TaskDecorator taskDecorator = runnable1 -> runnable;
        String msg = taskDecorator.toString() + " " + this.getClass().getSimpleName() + ".taskDecorator(Runnable runnable)";
        AppComponents.getLogger().info(msg);
        return taskDecorator.decorate(runnable);
    }

    /**
     * Убивает {@link #TASK_EXECUTOR} и {@link #TASK_SCHEDULER}
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
    public int hashCode() {
        return TASK_EXECUTOR.hashCode();
    }

    @Override
    public boolean equals(Object o_p) {
        if(this==o_p){
            return true;
        }
        if(o_p==null || getClass()!=o_p.getClass()){
            return false;
        }

        ThreadConfig that = ( ThreadConfig ) o_p;

        return TASK_EXECUTOR.equals(TASK_EXECUTOR);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(TASK_EXECUTOR.getThreadNamePrefix() + "{");
        sb.append("activeCount/total=").append(TASK_EXECUTOR.getActiveCount()).append("/");
        sb.append(Thread.activeCount());
        sb.append("(").append(TASK_EXECUTOR.getActiveCount()).append(" TASK_EXECUTOR)");
        sb.append(", corePoolSize=").append(TASK_EXECUTOR.getCorePoolSize());
        sb.append(", keepAliveSeconds=").append(TASK_EXECUTOR.getKeepAliveSeconds());
        sb.append(", maxPoolSize=").append(TASK_EXECUTOR.getMaxPoolSize());
        sb.append(", poolSize=").append(TASK_EXECUTOR.getPoolSize());
        sb.append(", <b>hash = ").append(TASK_EXECUTOR.hashCode());
        sb.append("</b>, prefix=").append(TASK_EXECUTOR.getThreadNamePrefix()).append("<br>\n");
        sb.append(", TASK_SCHEDULED= ").append(TASK_SCHEDULER.getActiveCount());
        sb.append(", TASK_SCHEDULER= ").append(TASK_SCHEDULER.getThreadNamePrefix());
        sb.append('}');
        return sb.toString();
    }
}
