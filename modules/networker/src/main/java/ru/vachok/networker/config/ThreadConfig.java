package ru.vachok.networker.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import ru.vachok.networker.ConstantsFor;

/**
 * @since 11.09.2018 (11:41)
 */
@Configuration
public class ThreadConfig {

    public TaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setThreadNamePrefix(ConstantsFor.APP_NAME + "execution task");
        executor.initialize();
        return executor;
    }
}
