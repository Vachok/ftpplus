package ru.vachok.networker.config;


import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import ru.vachok.networker.componentsrepo.AppComponents;


/**
 @since 11.09.2018 (11:41) */
@SuppressWarnings ("MagicNumber")
@EnableAsync
public class ThreadConfig extends ThreadPoolTaskExecutor {

    private static final ThreadPoolTaskExecutor TASK_EXECUTOR = new ThreadPoolTaskExecutor();

    private static final ThreadPoolTaskScheduler TASK_SCHEDULER = new ThreadPoolTaskScheduler();

    public Runnable taskDecorator(Runnable runnable) {
        TaskDecorator taskDecorator = runnable1 -> runnable;
        String msg = taskDecorator.toString() + " " + this.getClass().getSimpleName() + ".taskDecorator(Runnable runnable)";
        AppComponents.getLogger().info(msg);
        return taskDecorator.decorate(runnable);
    }

    public void killAll() {
        TASK_EXECUTOR.destroy();
        threadPoolTaskScheduler().destroy();
        Thread.currentThread().checkAccess();
        Thread.currentThread().interrupt();

    }

    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {

        TASK_SCHEDULER.setPoolSize(4);
        TASK_SCHEDULER.initialize();
        return TASK_SCHEDULER;
    }

    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        TASK_EXECUTOR.setCorePoolSize(75);
        TASK_EXECUTOR.setMaxPoolSize(100);
        TASK_EXECUTOR.setThreadNamePrefix(System.currentTimeMillis() + "task");
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
        sb.append(", corePoolSize=").append(TASK_EXECUTOR.getCorePoolSize());
        sb.append(", keepAliveSeconds=").append(TASK_EXECUTOR.getKeepAliveSeconds());
        sb.append(", maxPoolSize=").append(TASK_EXECUTOR.getMaxPoolSize());
        sb.append(", poolSize=").append(TASK_EXECUTOR.getPoolSize());
        sb.append('}');
        return sb.toString();
    }
}
