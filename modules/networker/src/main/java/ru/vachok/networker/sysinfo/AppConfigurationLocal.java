package ru.vachok.networker.sysinfo;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.exe.ThreadTimeout;
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
        AppComponents.threadConfig().cleanQueue(runnable);
        AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor().execute(runnable);
    }

    default void execute(Callable callable) {
        ThreadPoolExecutor executor = AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor();
        AppComponents.threadConfig().cleanQueue(callable);
        executor.submit(callable);
    }

    default void executeGet(Callable<?> callable, int timeOutSeconds) {
        ThreadPoolExecutor executor = AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor();
        Future<?> submit = executor.submit(callable);
        try {
            System.out.println("submit.get() = " + submit.get(timeOutSeconds, TimeUnit.SECONDS));
        }
        catch (InterruptedException e) {
            System.err.println(e.getMessage());
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        catch (ExecutionException | TimeoutException e) {
            System.err.println(e.getMessage());
        }
    }

    default void execute(Runnable runnable, long timeOutSeconds) {
        ThreadPoolExecutor poolExecutor = AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor();
        Future<?> submit = poolExecutor.submit(runnable);
        AppComponents.threadConfig().cleanQueue(runnable);
        AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor().submit(new ThreadTimeout(submit, timeOutSeconds));
    }

    default void schedule(Runnable runnable, int timeInMinPerion) {
        schedule(runnable, 0, (int) TimeUnit.MINUTES.toMillis(timeInMinPerion));
    }

    default void schedule(Runnable runnable, long timeFirstRun, long period) {
        ScheduledThreadPoolExecutor poolExecutor = AppComponents.threadConfig().getTaskScheduler().getScheduledThreadPoolExecutor();
        BlockingQueue<Runnable> executorQueue = poolExecutor.getQueue();
        executorQueue.removeIf(runnable1->runnable1.equals(runnable));
        long initialDelay = timeFirstRun - System.currentTimeMillis();
        if (initialDelay < 0) {
            initialDelay = 0;
        }
        poolExecutor.scheduleWithFixedDelay(runnable, initialDelay, period, TimeUnit.MILLISECONDS);
    }

    default String submitAsString(Callable<?> callableQuestion, int timeOutInSec) {
        ThreadPoolExecutor executor = AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor();
        try {
            Future<?> submit = executor.submit(callableQuestion);
            return (String) submit.get(timeOutInSec, TimeUnit.SECONDS);
        }
        catch (InterruptedException | ExecutionException | TimeoutException e) {
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
            return AbstractForms.networkerTrace(e);
        }
    }
}
