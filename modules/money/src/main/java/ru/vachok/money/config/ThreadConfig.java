package ru.vachok.money.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Date;


/**
 * @since 14.09.2018 (16:09)
 */
@Configuration
@EnableAsync
public class ThreadConfig {

    /*Fields*/
    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadConfig.class.getSimpleName());

    public ThreadPoolTaskExecutor getDefaultExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix(System.currentTimeMillis() + "_exe");
        executor.setThreadGroupName(new Date().toString());
        executor.setMaxPoolSize(10);
        executor.setCorePoolSize(10);
        executor.setWaitForTasksToCompleteOnShutdown(false);
        executor.setThreadPriority(6);
        executor.initialize();
        return executor;
    }
}
