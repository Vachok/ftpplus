package ru.vachok.networker.sysinfo;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.data.enums.ConstantsFor;
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

    BlockingQueue<Callable> workQ = new LinkedBlockingQueue<>();

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

    static Object executeInWorkStealingPool(Callable<?> o, long timeOut) {
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

    static Object executeInWorkStealingPool(ForkJoinTask<?> fjTask, long timeOutSec) {
        ForkJoinPool service = ThreadConfig.getForkJoin();
        ForkJoinTask<?> fork = service.submit(fjTask);
        Object o = null;
        try {
            o = fork.get(timeOutSec, TimeUnit.SECONDS);
        }
        catch (InterruptedException | TimeoutException | ExecutionException e) {
            e.printStackTrace();
        }
        if (o != null) {
            return o;
        }
        else {
            return "executeInWorkStealingPool : o is null";
        }
    }

    default void execute(Runnable runnable) {
        AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor().execute(runnable);
    }

    default void execute(Callable<?> callable) {
        ThreadConfig.cleanQueue(callable);
        ThreadPoolExecutor executor = AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor();
        executor.submit(callable);
    }

    default void execute(Runnable runnable, long timeOutSeconds) {
        ThreadConfig.cleanQueue(runnable);
        ThreadPoolExecutor poolExecutor = AppComponents.threadConfig().getTaskExecutor((int) timeOutSeconds).getThreadPoolExecutor();
        Future<?> submit = poolExecutor.submit(runnable);
        AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor().submit(new ThreadTimeout(submit, timeOutSeconds));
    }

    default void schedule(Runnable runnable, int timeInMinPerion) {
        ThreadConfig.cleanQueue(runnable);
        schedule(runnable, 0, TimeUnit.MINUTES.toMillis(timeInMinPerion));
    }

    default void schedule(Runnable runnable, long timeFirstRun, long period) {
        ThreadConfig.cleanQueue(runnable);
        ScheduledThreadPoolExecutor poolExecutor = AppComponents.threadConfig().getTaskScheduler().getScheduledThreadPoolExecutor();
        BlockingQueue<Runnable> executorQueue = poolExecutor.getQueue();
        executorQueue.removeIf(runnable1->runnable1.equals(runnable));
        long initialDelay = timeFirstRun - System.currentTimeMillis();
        if (initialDelay < 0) {
            initialDelay = 10;
        }
        poolExecutor.scheduleWithFixedDelay(runnable, initialDelay, period, TimeUnit.MILLISECONDS);
    }

    default Object executeGet(Callable<?> callable, int timeOutSeconds) {
        ThreadConfig.cleanQueue(callable);
        ThreadPoolExecutor executor = AppComponents.threadConfig().getTaskExecutor(timeOutSeconds).getThreadPoolExecutor();
        Future<?> submit = executor.submit(callable);
        Object o;
        try {
            o = submit.get(timeOutSeconds, TimeUnit.SECONDS);
        }
        catch (InterruptedException | ExecutionException | TimeoutException e) {
            o = e.getMessage();
        }
        return o;
    }

    default void executeBlock(Runnable runnable, int timeWait) throws TimeoutException {
        ThreadConfig.cleanQueue(runnable);
        try {
            AppComponents.threadConfig().getTaskExecutor(timeWait).submit(runnable).get(timeWait, TimeUnit.SECONDS);
        }
        catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    default String submitAsString(Callable<String> callable, int timeOutInSec) {
        String result = MessageFormat.format("{0} size of threads queue", workQ.size());
        ExecutorService serviceOneLaunch = Executors.newSingleThreadExecutor();
        Future<String> submit = serviceOneLaunch.submit(callable);
        try {
            String s = submit.get(timeOutInSec, TimeUnit.SECONDS);
            if (submit.isDone()) {
                result = s;
            }
            else {
                result = submit.getClass().getSimpleName() + ": " + submit.cancel(true);
            }
        }
        catch (InterruptedException | ExecutionException | TimeoutException | RuntimeException e) {
            result = MessageFormat
                .format("{0} error {1}: {2}", callable.getClass().getSimpleName(), e.getClass().getSimpleName(), e.getMessage());
        }
        finally {
            String submitDone;
            if (submit.isDone()) {
                submitDone = "DONE";
            }
            else {
                submitDone = ConstantsFor.STR_ERROR;
            }
            System.out.println(MessageFormat.format("{0}: {1}\nresult = {2}", submit.getClass().getSimpleName(), submitDone, result));
            serviceOneLaunch.shutdown();
        }
        return result;
    }
}
