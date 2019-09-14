// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.schedule;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.data.enums.ConstantsFor;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.*;


/**
 @since 07.05.2019 (12:40) */
public class DeadLockMonitor implements Callable<String> {
    
    
    private String message = ConstantsFor.GOOD_NO_LOCKS;
    
    private MessageToUser messageToUser = ru.vachok.networker.restapi.message.MessageToUser
            .getInstance(ru.vachok.networker.restapi.message.MessageToUser.LOCAL_CONSOLE, getClass().getSimpleName());
    
    @Override
    public String call() {
        StringBuilder stringBuilder = new StringBuilder();
        ScheduledFuture<?> scheduledFuture = AppComponents.threadConfig().getTaskScheduler().getScheduledThreadPoolExecutor()
            .scheduleWithFixedDelay(this::monitoringStart, 0, ConstantsFor.DELAY, TimeUnit.SECONDS);
        try {
            stringBuilder.append(scheduledFuture.get(5, TimeUnit.SECONDS));
        }
        catch (InterruptedException e) {
            stringBuilder.append(e.getMessage()).append("\n").append(new TForms().fromArray(e, false));
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        catch (ExecutionException | TimeoutException e) {
            stringBuilder.append(e.getMessage());
        }
        catch (NullPointerException e) {
            stringBuilder.append(message).append(" ").append(true);
        }
        return stringBuilder.toString();
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
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DeadLockMonitor{");
        try {
            sb.append(Arrays.toString(Objects.requireNonNull(monitoringStart(), "No Locks")));
            sb.append('}');
        }
        catch (NullPointerException e) {
            sb.append(e.getMessage());
        }
        return sb.toString();
    }
}
