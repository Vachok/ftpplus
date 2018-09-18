package ru.vachok.networker.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ScheduledExecutorTask;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.TimeUnit;

/**
 * @since 11.09.2018 (11:41)
 */
@Configuration
public class ThreadConfig {

    public TaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(5);
        executor.setThreadNamePrefix(System.currentTimeMillis() + "execution task");
        executor.initialize();
        return executor;
    }

    public ScheduledExecutorTask taskScheduler(long delay, long period) {
        ScheduledExecutorTask scheduledExecutorTask = new ScheduledExecutorTask();
        scheduledExecutorTask.setDelay(delay);
        scheduledExecutorTask.setPeriod(period);
        scheduledExecutorTask.setTimeUnit(TimeUnit.SECONDS);
        return scheduledExecutorTask;
    }

    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(2);
        threadPoolTaskScheduler.initialize();
        return threadPoolTaskScheduler;
    }
}
