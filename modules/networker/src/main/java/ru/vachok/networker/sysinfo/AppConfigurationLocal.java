package ru.vachok.networker.sysinfo;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
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

    static Object executeInWorkStealingPool(ForkJoinTask<?> o, long timeOut) {
        ForkJoinPool service = AppComponents.threadConfig().getForkJoin();
        Object ret;
        try {
            ret = service.submit(o).get(timeOut, TimeUnit.SECONDS);
        }
        catch (InterruptedException | ExecutionException | TimeoutException e) {
            ret = e;
        }
        finally {
            System.out.println(getConditions(service));
        }
        return ret;
    }

    static String getConditions(ForkJoinPool service) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(service.getClass().getSimpleName()).append("\n");
        stringBuilder.append(service.getActiveThreadCount()).append(" getActiveThreadCount\n");
        stringBuilder.append(service.getAsyncMode()).append(" getAsyncMode\n");
        stringBuilder.append(service.getParallelism()).append(" getParallelism\n");
        stringBuilder.append(service.getQueuedSubmissionCount()).append(" getQueuedSubmissionCount\n");
        stringBuilder.append(service.getQueuedTaskCount()).append(" getQueuedTaskCount\n");
        stringBuilder.append(service.getRunningThreadCount()).append(" getRunningThreadCount\n");
        stringBuilder.append(service.getStealCount()).append(" getStealCount\n");
        stringBuilder.append(service.getUncaughtExceptionHandler()).append(" getUncaughtExceptionHandler\n");
        return stringBuilder.toString();
    }

    default void execute(Runnable runnable) {
        ThreadConfig.cleanQueue(runnable);
        AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor().execute(runnable);
    }

    default void execute(Callable<?> callable) {
        ThreadPoolExecutor executor = AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor();
        AppComponents.threadConfig().cleanQueue(callable);
        executor.submit(callable);
    }

    default Object executeGet(Callable<?> callable, int timeOutSeconds) {
        ThreadConfig.cleanQueue(callable);
        ThreadPoolExecutor executor = AppComponents.threadConfig().getTaskExecutor(timeOutSeconds).getThreadPoolExecutor();
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
        ThreadPoolExecutor poolExecutor = AppComponents.threadConfig().getTaskExecutor((int) timeOutSeconds).getThreadPoolExecutor();
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
        String result;
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
            result = MessageFormat.format("AppConfigurationLocal.submitAsString\n{0}\n{1}", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
        }
        return result;
    }

    default void executeBlock(Runnable actualizer, int timeWait) throws TimeoutException {
        try {
            AppComponents.threadConfig().getTaskExecutor(timeWait).submit(actualizer).get(timeWait, TimeUnit.SECONDS);
        }
        catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
