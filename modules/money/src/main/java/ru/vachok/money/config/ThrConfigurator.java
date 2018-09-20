package ru.vachok.money.config;


import org.slf4j.Logger;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @since 14.09.2018 (16:09)
 */
@Configuration
@EnableAsync
public class ThrConfigurator {

    private static final Logger LOGGER = AppComponents.getLogger();

    public ThreadPoolTaskExecutor getDefaultExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix(System.currentTimeMillis() + "_exe");
        executor.setMaxPoolSize(10);
        executor.setCorePoolSize(10);
        LOGGER.info("Get Default executor = max pool 10, core pool is 10. Name prefix 'Timestamp'_exe");
        ApplicationEvent applicationEvent = new AppEvents().failedApp();
        return executor;
    }
}
