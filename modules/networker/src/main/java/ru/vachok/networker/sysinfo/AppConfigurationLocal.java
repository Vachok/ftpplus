package ru.vachok.networker.sysinfo;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.exe.ThreadConfig;
import ru.vachok.networker.exe.ThreadTimeout;
import ru.vachok.networker.exe.runnabletasks.OnStartTasksLoader;
import ru.vachok.networker.exe.schedule.ScheduleDefiner;

import java.text.MessageFormat;
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

    default void execute(Callable<?> callable) {
        ThreadPoolExecutor executor = AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor();
        AppComponents.threadConfig().cleanQueue(callable);
        executor.submit(callable);
    }

    default Object executeGet(Callable<?> callable, int timeOutSeconds) {
        ThreadConfig.cleanQueue(callable);
        ThreadPoolExecutor executor = AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor();
        Future<?> submit = executor.submit(callable);
        Object o;
        try {
            o = submit.get(timeOutSeconds, TimeUnit.SECONDS);
            System.out.println(MessageFormat.format("submit.get() = {0}", o));
        }
        catch (InterruptedException e) {
            o = e.getMessage();
            System.err.println(e.getMessage());
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        catch (ExecutionException | TimeoutException e) {
            o = e.getMessage();
            System.err.println(e.getMessage());
        }
        return o;
    }

    default void execute(Runnable runnable, long timeOutSeconds) {
        ThreadPoolExecutor poolExecutor = AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor();
        ThreadConfig.cleanQueue(runnable);
        Future<?> submit = poolExecutor.submit(runnable);
        AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor().submit(new ThreadTimeout(submit, timeOutSeconds));
    }

    default void schedule(Runnable runnable, int timeInMinPerion) {
        schedule(runnable, 0, TimeUnit.MINUTES.toMillis(timeInMinPerion));
    }

    default void schedule(Runnable runnable, long timeFirstRun, long period) {
        ScheduledThreadPoolExecutor poolExecutor = AppComponents.threadConfig().getTaskScheduler().getScheduledThreadPoolExecutor();
        ThreadConfig.cleanQueue(runnable);
        BlockingQueue<Runnable> executorQueue = poolExecutor.getQueue();
        executorQueue.removeIf(runnable1->runnable1.equals(runnable));
        long initialDelay = timeFirstRun - System.currentTimeMillis();
        if (initialDelay < 0) {
            initialDelay = 0;
        }
        poolExecutor.scheduleWithFixedDelay(runnable, initialDelay, period, TimeUnit.MILLISECONDS);
    }

    default String submitAsString(Callable<String> callableQuestion, int timeOutInSec) {
        String result = "null";
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> submit = executor.submit(callableQuestion);
        try {
            String s = submit.get(timeOutInSec, TimeUnit.SECONDS);
            if (submit.isDone()) {
                result = s;
            }
            else {
                result = MessageFormat.format("{0} submit is {1}", getClass().getSimpleName(), false);
            }
        }
        catch (InterruptedException | ExecutionException | TimeoutException | RuntimeException e) {
            result = MessageFormat
                .format("{0} try to run: {1} ({2})", AppConfigurationLocal.class.getSimpleName(), e.getMessage(), callableQuestion.getClass().getSimpleName());
        }
        finally {
            System.out.println(MessageFormat.format("{0} = {1} is done: {2}", result, callableQuestion.getClass().getName(), submit.isDone()));
        }
        return result;
    }
}
