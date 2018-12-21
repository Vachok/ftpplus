package ru.vachok.networker.config;


import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import ru.vachok.networker.componentsrepo.AppComponents;


/**
 * @since 11.09.2018 (11:41)
 */
@EnableAsync
public class ThreadConfig extends ThreadPoolTaskExecutor {

    private ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        this.executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(5);
        executor.setThreadNamePrefix(System.currentTimeMillis() + "task");
        executor.initialize();
        return executor;
    }

    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(2);
        threadPoolTaskScheduler.initialize();
        return threadPoolTaskScheduler;
    }

    public Runnable taskDecorator(Runnable runnable) {
        TaskDecorator taskDecorator = runnable1 -> runnable;
        String msg = taskDecorator.toString() + " " + this.getClass().getSimpleName() + ".taskDecorator(Runnable runnable)";
        AppComponents.getLogger().info(msg);
        return taskDecorator.decorate(runnable);
    }

    public void killAll() {
        threadPoolTaskExecutor().destroy();
        threadPoolTaskScheduler().destroy();
        Thread.currentThread().checkAccess();
        Thread.currentThread().interrupt();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(executor.getThreadNamePrefix() + "{");
        sb.append("activeCount/total=").append(executor.getActiveCount()).append("/");
        sb.append(Thread.activeCount());
        sb.append(", corePoolSize=").append(executor.getCorePoolSize());
        sb.append(", keepAliveSeconds=").append(executor.getKeepAliveSeconds());
        sb.append(", maxPoolSize=").append(executor.getMaxPoolSize());
        sb.append(", poolSize=").append(executor.getPoolSize());
        sb.append('}');
        return sb.toString();
    }
}
