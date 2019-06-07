package ru.vachok.networker.exe.schedule;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.services.MessageLocal;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 @since 07.05.2019 (12:40) */
public class DeadLockMonitor implements Runnable {
    
    
    private String message = "No deadlocks, good!";
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    @Override public void run() {
        ScheduledFuture<?> scheduledFuture = AppComponents.threadConfig().getTaskScheduler().getScheduledThreadPoolExecutor()
            .scheduleWithFixedDelay(this::monitoringStart, 0, ConstantsFor.DELAY, TimeUnit.SECONDS);
        try {
            scheduledFuture.get(ConstantsFor.DELAY, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        catch (ExecutionException | TimeoutException e) {
            messageToUser.error(e.getMessage());
        }
        catch (NullPointerException e) {
            messageToUser.info(message + " " + true);
        }
    }
    
    @Override public String toString() {
        final StringBuilder sb = new StringBuilder("DeadLockMonitor{");
        try {
            sb.append(Objects.requireNonNull(monitoringStart(), "No Locks"));
            sb.append('}');
        }
        catch (NullPointerException e) {
            sb.append(e.getMessage());
        }
        return sb.toString();
    }
    
    private long[] monitoringStart() {
        ThreadMXBean monitorBean = ManagementFactory.getThreadMXBean();
        long[] deadlockedThreads = monitorBean.findDeadlockedThreads();
        Objects.requireNonNull(deadlockedThreads, message);
        if (deadlockedThreads.length > 0) {
            messageToUser.warn(getClass().getSimpleName() + ".monitoringStart", "deadlockedThreads", " = " + new TForms().fromArray(deadlockedThreads));
        }
        return deadlockedThreads;
    }
}
