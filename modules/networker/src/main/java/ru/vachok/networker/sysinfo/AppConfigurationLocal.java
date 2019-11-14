package ru.vachok.networker.sysinfo;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.exe.runnabletasks.OnStartTasksLoader;
import ru.vachok.networker.exe.schedule.ScheduleDefiner;

import java.util.concurrent.*;


@FunctionalInterface
@SuppressWarnings("MethodWithMultipleReturnPoints")
public interface AppConfigurationLocal extends Runnable {
    
    
    String ON_START_LOADER = "OnStartTasksLoader";
    
    String SCHEDULE_DEFINER = "ScheduleDefiner";
    
    @Contract(pure = true)
    static AppConfigurationLocal getInstance() {
        return getInstance("");
    }
    
    @Contract(pure = true)
    static AppConfigurationLocal getInstance(@NotNull String type) {
        switch (type) {
            case (SCHEDULE_DEFINER):
                return new ScheduleDefiner();
            case (ON_START_LOADER):
                return new OnStartTasksLoader();
            default:
                return AppComponents.threadConfig();
        }
        
    }
    
    default void execute(Runnable runnable) {
        AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor().execute(runnable);
    }
    
    default void schedule(Runnable runnable, int timeInMinPerion) {
        schedule(runnable, 0, TimeUnit.MINUTES.toMillis(timeInMinPerion));
    }
    
    default void schedule(Runnable runnable, long timeFirstRun, long period) {
        ScheduledThreadPoolExecutor poolExecutor = AppComponents.threadConfig().getTaskScheduler().getScheduledThreadPoolExecutor();
        BlockingQueue<Runnable> executorQueue = poolExecutor.getQueue();
        for (Runnable runnable1 : executorQueue) {
            if (runnable1.equals(runnable)) {
                executorQueue.remove(runnable1);
            }
        }
        long initialDelay = timeFirstRun - System.currentTimeMillis();
        if (initialDelay < 0) {
            initialDelay = 0;
        }
        
        poolExecutor.scheduleWithFixedDelay(runnable, initialDelay, period, TimeUnit.MILLISECONDS);
    }
}
