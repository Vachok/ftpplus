package ru.vachok.networker.config;


import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * @since 11.09.2018 (11:41)
 */
@EnableAsync
public class ThreadConfig extends ThreadPoolTaskExecutor {

    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(5);
        executor.setThreadNamePrefix(System.currentTimeMillis() + "execution task");
        executor.initialize();
        return executor;
    }

    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(2);
        threadPoolTaskScheduler.initialize();
        return threadPoolTaskScheduler;
    }

    public TaskDecorator taskDecorator(Runnable runnable) {
        return runnable1 -> runnable;
    }
}
