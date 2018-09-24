package ru.vachok.money.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;


/**
 * @since 14.09.2018 (16:09)
 */
@Configuration
@EnableAsync
public class ThrAsyncConfigurator {

    /*Fields*/
    private static final Logger LOGGER = LoggerFactory.getLogger(ThrAsyncConfigurator.class.getSimpleName());

    public ThreadPoolTaskExecutor getDefaultExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix(System.currentTimeMillis() + "_exe");
        executor.setMaxPoolSize(10);
        executor.setCorePoolSize(10);
        LOGGER.info("Get Default executor = max pool 10, core pool is 10. Name prefix 'Timestamp'_exe");
        executor.initialize();
        return executor;
    }
}
