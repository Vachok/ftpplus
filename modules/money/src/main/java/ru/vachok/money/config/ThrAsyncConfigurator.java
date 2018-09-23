package ru.vachok.money.config;


import org.slf4j.Logger;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import ru.vachok.money.ConstantsFor;


/**
 * @since 14.09.2018 (16:09)
 */
@Configuration
@EnableAsync
public class ThrAsyncConfigurator {

    private static final Logger LOGGER = ConstantsFor.getLogger();

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
