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
        final ThreadPoolExecutor executor = AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor();
        AppComponents.threadConfig().cleanQueue(runnable);
        executor.execute(runnable);
    }

    default void execute(Callable callable) {
        final ThreadPoolExecutor executor = AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor();
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
        final ThreadPoolExecutor executor = AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor();
        Future<?> submit = executor.submit(runnable);
        AppComponents.threadConfig().cleanQueue(runnable);
        AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor().submit(new ThreadTimeout(submit, timeOutSeconds));
    }

    default void schedule(Runnable runnable, int timeInMinPerion) {
        schedule(runnable, TimeUnit.SECONDS.toMillis(timeInMinPerion), TimeUnit.MINUTES.toMillis(timeInMinPerion));
    }

    default void schedule(Runnable runnable, long timeFirstRun, long period) {
        ScheduledThreadPoolExecutor poolExecutor = AppComponents.threadConfig().getTaskScheduler().getScheduledThreadPoolExecutor();
        BlockingQueue<Runnable> executorQueue = poolExecutor.getQueue();
        executorQueue.removeIf(runnable1->runnable1.equals(runnable));
        poolExecutor.scheduleWithFixedDelay(runnable, timeFirstRun, period, TimeUnit.MILLISECONDS);
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
