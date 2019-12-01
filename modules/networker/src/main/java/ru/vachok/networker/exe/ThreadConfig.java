// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe;


import com.eclipsesource.json.JsonObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.support.ExecutorServiceAdapter;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.TaskUtils;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.componentsrepo.htmlgen.PageGenerationHelper;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.restapi.message.DBMessenger;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.restapi.props.InitProperties;
import ru.vachok.networker.sysinfo.AppConfigurationLocal;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.text.MessageFormat;
import java.util.Date;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;


/**
 @see ru.vachok.networker.exe.ThreadConfigTest
 @since 11.09.2018 (11:41) */
@SuppressWarnings("MagicNumber")
@EnableAsync
public class ThreadConfig implements AppConfigurationLocal {


    /**
     {@link ThreadPoolTaskScheduler}
     */
    private static final ThreadPoolTaskScheduler TASK_SCHEDULER;

    /**
     {@link ThreadPoolTaskExecutor}
     */
    private static final ThreadPoolTaskExecutor TASK_EXECUTOR;

    /**
     Instance
     */
    private static final AppConfigurationLocal THREAD_CONFIG_INST = new ThreadConfig();

    private static final int PROCESSORS = ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();

    /**
     {@link MessageLocal}
     */
    private static MessageToUser messageToUser = ru.vachok.networker.restapi.message.MessageToUser
            .getInstance(ru.vachok.networker.restapi.message.MessageToUser.LOCAL_CONSOLE, ThreadConfig.class.getSimpleName());

    private Runnable r = new Thread();

    /**
     @return {@link #TASK_EXECUTOR}
     */
    public ThreadPoolTaskExecutor getTaskExecutor() {
        setExecutor();
        return TASK_EXECUTOR;
    }

    public static @NotNull String thrNameSet(String className) {

        float localUptime = (System.currentTimeMillis() - ConstantsFor.START_STAMP) / 1000 / ConstantsFor.ONE_HOUR_IN_MIN;
        String delaysCount = String.format("%.01f", (localUptime / ConstantsFor.DELAY));
        String upStr = String.format("%.01f", localUptime);

        upStr += "m";
        if (localUptime > ConstantsFor.ONE_HOUR_IN_MIN) {
            localUptime /= ConstantsFor.ONE_HOUR_IN_MIN;
            upStr = String.format("%.02f", localUptime);
            upStr += "h";
        }
        String thrName = className + ";" + upStr + ";" + delaysCount;
        Thread.currentThread().setName(thrName);
        return thrName;
    }

    @Override
    public void run() {
        throw new TODOException("just do it!");
    }

    private void setExecutor() {
        TASK_EXECUTOR.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        TASK_EXECUTOR.getThreadPoolExecutor().setCorePoolSize(35);
        TASK_EXECUTOR.setQueueCapacity(500);
        TASK_EXECUTOR.setWaitForTasksToCompleteOnShutdown(true);
        TASK_EXECUTOR.setAwaitTerminationSeconds(6);
        TASK_EXECUTOR.setThreadPriority(7);
        TASK_EXECUTOR.setThreadNamePrefix("E-");
    }

    public ThreadPoolTaskScheduler getTaskScheduler() {
        setScheduler();
        return TASK_SCHEDULER;
    }

    private void setScheduler() {
        ScheduledThreadPoolExecutor scThreadPoolExecutor = TASK_SCHEDULER.getScheduledThreadPoolExecutor();
        scThreadPoolExecutor.setCorePoolSize(PROCESSORS);
        scThreadPoolExecutor.setMaximumPoolSize(20);
        scThreadPoolExecutor.setRemoveOnCancelPolicy(true);
        scThreadPoolExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        TASK_SCHEDULER.setErrorHandler(TaskUtils.LOG_AND_SUPPRESS_ERROR_HANDLER);
        TASK_SCHEDULER.prefersShortLivedTasks();
        TASK_SCHEDULER.setThreadNamePrefix("S");
        TASK_SCHEDULER.setThreadPriority(2);
        TASK_SCHEDULER.setWaitForTasksToCompleteOnShutdown(false);
        TASK_SCHEDULER.setDaemon(true);
    }

    @Contract(pure = true)
    public static ThreadConfig getI() {
        return (ThreadConfig) THREAD_CONFIG_INST;
    }

    public @NotNull String getAllThreads() {
        StringBuilder stringBuilder = new StringBuilder();
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        bean.setThreadCpuTimeEnabled(true);
        bean.setThreadContentionMonitoringEnabled(true);
        bean.resetPeakThreadCount();
        try {
            for (long id : bean.getAllThreadIds()) {
                ThreadInfo info = bean.getThreadInfo(id);
                String timeThr = new PageGenerationHelper()
                        .setColor(ConstantsFor.YELLOW, " time: " + TimeUnit.NANOSECONDS.toMillis(bean.getThreadCpuTime(id)) + " millis.\n<br>");
                stringBuilder.append(info.toString()).append(timeThr);
            }
        }
        catch (RuntimeException e) {
            messageToUser.error(e.getMessage() + " see line: 387 ***");
        }
        FileSystemWorker
                .appendObjectToFile(new File(this.getClass().getSimpleName() + ".time"), UsefulUtilities.getRunningInformation() + "\n" + stringBuilder.toString());
        return stringBuilder.toString();
    }

    private ThreadConfig() {
        dumpToFile("ThreadConfig");
    }

    public static @NotNull String dumpToFile(String fileName) {
        String fromArray = new TForms().fromArray(Thread.currentThread().getStackTrace());
        ReentrantLock reentrantLock = new ReentrantLock();
        fileName = "thr_" + fileName + "-stack.txt";
        reentrantLock.lock();
        try (OutputStream outputStream = new FileOutputStream(fileName, true);
             PrintStream printStream = new PrintStream(outputStream, true)) {
            printStream.println();
            printStream.println(new Date());
            printStream.println(fromArray);
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format("ThreadConfig.dumpToFile: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
        finally {
            reentrantLock.unlock();
        }
        return "DUMPED: " + fileName;
    }

    static {
        TASK_SCHEDULER = new ThreadPoolTaskScheduler();
        TASK_EXECUTOR = new ThreadPoolTaskExecutor();
        TASK_SCHEDULER.initialize();
        TASK_EXECUTOR.initialize();
    }


    public void cleanQueue(@NotNull ThreadPoolExecutor poolExecutor, Runnable runnable) {
        BlockingQueue<Runnable> executorQueue = poolExecutor.getQueue();
        for (Runnable r : executorQueue) {
            if (r.equals(runnable) || r instanceof DBMessenger) {
                MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, this.getClass().getSimpleName())
                    .warn(this.getClass().getSimpleName(), "execute", r.toString());
                executorQueue.remove(r);
            }
        }
    }

    /**
     Killer
     */
    @SuppressWarnings("MethodWithMultipleLoops")
    public void killAll() {
        long minRun = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - ConstantsFor.START_STAMP);
        InitProperties.getUserPref();
        BlockingQueue<Runnable> runnableBlockingQueueSched = TASK_SCHEDULER.getScheduledThreadPoolExecutor().getQueue();
        BlockingQueue<Runnable> runnableBlockingQueue = TASK_EXECUTOR.getThreadPoolExecutor().getQueue();
        runnableBlockingQueue.clear();
        runnableBlockingQueueSched.clear();
        TASK_SCHEDULER.getScheduledThreadPoolExecutor().shutdown();
        TASK_EXECUTOR.getThreadPoolExecutor().shutdown();

        try {
            if (!TASK_EXECUTOR.getThreadPoolExecutor().awaitTermination(10, TimeUnit.SECONDS) && !TASK_SCHEDULER.getScheduledThreadPoolExecutor()
                    .awaitTermination(10, TimeUnit.SECONDS)) {
                TASK_SCHEDULER.getScheduledThreadPoolExecutor().shutdownNow();
                TASK_EXECUTOR.getThreadPoolExecutor().shutdownNow();
            }
            if (!TASK_EXECUTOR.getThreadPoolExecutor().awaitTermination(10, TimeUnit.SECONDS) && !TASK_SCHEDULER.getScheduledThreadPoolExecutor()
                    .awaitTermination(10, TimeUnit.SECONDS)) {
                System.exit(Math.toIntExact(minRun));
            }
        }
        catch (InterruptedException | IllegalStateException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".killAll", e));
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        finally {
            Runtime.getRuntime().halt(666);
        }
    }

    public boolean execByThreadConfig(Runnable runnable) {
        return execByThreadConfig(runnable, "test");
    }

    private boolean execByThreadConfig(Runnable runnable, @SuppressWarnings("SameParameterValue") String threadName) {
        this.r = runnable;
        try {
            return execByThreadConfig(threadName);
        }
        catch (RuntimeException e) {
            messageToUser.error("ThreadConfig.execByThreadConfig", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
            return false;
        }
    }

    /**
     @return executed or not

     @see ru.vachok.networker.exe.ThreadConfigTest#testExecByThreadConfig()
     */
    private boolean execByThreadConfig(String thName) {
        SimpleAsyncTaskExecutor simpleAsyncExecutor = new ASExec().getSimpleAsyncExecutor();
        Thread newThread = new Thread();
        boolean retBool = false;
        try {
            newThread = new Thread(r);
            newThread.setName(thName);
            Future<?> submit = simpleAsyncExecutor.submit(newThread);
            Object oGet = submit.get(ConstantsFor.DELAY, TimeUnit.MINUTES);
            retBool = (oGet == null);
        }
        catch (InterruptedException e) {
            newThread.checkAccess();
            newThread.interrupt();
            messageToUser.error(ThreadConfig.class.getSimpleName(), e.getMessage(), " see line: 256 ***");
        }
        catch (ExecutionException | TimeoutException e) {
            messageToUser.error(ThreadConfig.class.getSimpleName(), e.getMessage(), " see line: 259 ***");
        }
        finally {
            FileSystemWorker.appendObjectToFile(new File(FileNames.APP_JSON), getTrheadInfo(newThread));
        }
        return retBool;

    }

    @Contract(pure = true)
    private @NotNull JsonObject getTrheadInfo(@NotNull Thread thread) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.set(PropertiesNames.TIMESTAMP, System.currentTimeMillis());
        jsonObject.set("threadName", thread.getName());
        jsonObject.set("id", thread.getId());
        jsonObject.set("prio", thread.getPriority());
        jsonObject.set("state", thread.getState().name());
        jsonObject.set("tostring", thread.toString());
        return jsonObject;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ThreadConfig{");
        sb.append(TASK_EXECUTOR.getThreadPoolExecutor()).append(" TASK EXECUTOR, ");
        sb.append(TASK_SCHEDULER.getScheduledExecutor()).append(" TASK SCHEDULER.\n <p>");
        sb.append('}');
        return sb.toString();
    }

    /**
     Асинхронный {@link ThreadPoolTaskExecutor}
     <p>

     @see AsyncConfigurerSupport
     @since <a href="https://github.com/Vachok/ftpplus/commit/f40030246ec6f28cc9c484b9c56a3879da1162af" target=_blank>21.02.2019 (22:49)</a>
     */
    private class ASExec extends AsyncConfigurerSupport {


        private SimpleAsyncTaskExecutor simpleAsyncExecutor = new SimpleAsyncTaskExecutor("A");

        @Contract(pure = true)
        private SimpleAsyncTaskExecutor getSimpleAsyncExecutor() {
            return simpleAsyncExecutor;
        }

        @Override
        public Executor getAsyncExecutor() {
            simpleAsyncExecutor.setConcurrencyLimit(Runtime.getRuntime().availableProcessors() - 2);
            simpleAsyncExecutor.setThreadPriority(1);
            return new ExecutorServiceAdapter(simpleAsyncExecutor);
        }

        ASExec() {
        }

        @Override
        public String toString() {
            boolean throttleActive = simpleAsyncExecutor.isThrottleActive();
            return throttleActive + " throttleActive. Concurrency limit : " + simpleAsyncExecutor.getConcurrencyLimit();
        }
    }
}
