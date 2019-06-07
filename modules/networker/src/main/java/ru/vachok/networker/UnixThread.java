package ru.vachok.networker;


import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.exe.runnabletasks.NetMonitorPTV;
import ru.vachok.networker.exe.runnabletasks.ScanOnline;
import ru.vachok.networker.exe.runnabletasks.TemporaryFullInternet;
import ru.vachok.networker.exe.schedule.DiapazonScan;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.concurrent.*;

import static ru.vachok.networker.AppInfoOnLoad.dateSchedulers;


/**
 @since 06.06.2019 (16:58) */
class UnixThread extends Thread {
    
    
    private MessageToUser messageToUser = new MessageCons(getClass().getSimpleName());
    
    UnixThread(AppInfoOnLoad load) {
    }
    
    public void run() {
        try {
            unixTrySched();
        }
        catch (Exception e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".run", e));
        }
    }
    
    private String unixTrySched() throws RuntimeException {
        StringBuilder stringBuilder = new StringBuilder();
        
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        threadMXBean.setThreadContentionMonitoringEnabled(true);
        threadMXBean.setThreadCpuTimeEnabled(true);
        ScheduledExecutorService executorService = Executors.unconfigurableScheduledExecutorService(Executors.newScheduledThreadPool(ConstantsFor.ONE_DAY_HOURS));
        stringBuilder.append(executorService);
        ScheduledFuture<?> ptvPing = executorService.scheduleWithFixedDelay(new NetMonitorPTV(), 0, ConstantsFor.ONE_DAY_HOURS, TimeUnit.SECONDS);
        ScheduledFuture<?> tmpInet = executorService.scheduleWithFixedDelay(new TemporaryFullInternet(), 0, ConstantsFor.ONE_DAY_HOURS, TimeUnit.SECONDS);
        ScheduledFuture<?> diapScan = executorService.scheduleWithFixedDelay(DiapazonScan.getInstance(), 2, ConstantsFor.DELAY, TimeUnit.MINUTES);
        ScheduledFuture<?> scanOnline = executorService.scheduleWithFixedDelay(new ScanOnline(), 3, 3, TimeUnit.MINUTES);
        
        try {
            if (ptvPing.get() != null) {
                stringBuilder.append("ptvPing");
            }
            if (tmpInet.get() != null) {
                stringBuilder.append("tmpInet");
            }
            if (diapScan.get() != null) {
                stringBuilder.append("diapScan");
            }
            if (scanOnline.get() != null) {
                stringBuilder.append("scanOnline");
            }
        }
        catch (InterruptedException | ExecutionException e) {
            stringBuilder.append(FileSystemWorker.error(getClass().getSimpleName() + ".unixTrySched", e));
        }
        for (long id : threadMXBean.getAllThreadIds()) {
            FileSystemWorker.writeFile("scheduler.stack", Arrays.toString(threadMXBean.getThreadInfo(id).getStackTrace()));
            stringBuilder.append(threadMXBean.getThreadInfo(Thread.currentThread().getId()));
        }
        
        dateSchedulers(executorService);
        return stringBuilder.toString();
    }
    
}
