package ru.vachok.networker.exe.schedule;


import ru.vachok.networker.AppComponents;
import ru.vachok.networker.AppInfoOnLoad;
import ru.vachok.networker.ad.inet.TemporaryFullInternet;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.services.MyCalen;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.exe.ThreadConfig;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.info.NetScanService;
import ru.vachok.networker.info.stats.Stats;
import ru.vachok.networker.mail.testserver.MailPOPTester;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.sysinfo.AppConfigurationLocal;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static java.time.DayOfWeek.SUNDAY;


public class ScheduleDefiner implements AppConfigurationLocal {
    
    
    private static MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, ScheduleDefiner.class.getSimpleName());
    
    @Override
    public void run() {
        startPeriodicTasks();
    }
    
    private void startPeriodicTasks() {
        NetScanService netScanService = NetScanService.getInstance(NetScanService.PTV);
        NetScanService scanOnlineRun = NetScanService.getInstance("ScanOnline");
        NetScanService diapazonScanRun = NetScanService.getInstance(NetScanService.DIAPAZON);
        Runnable popSmtpTest = new MailPOPTester();
        
        ThreadConfig thrConfig = AppComponents.threadConfig();
        
        thrConfig.getTaskScheduler().getScheduledThreadPoolExecutor().scheduleWithFixedDelay(netScanService, 10, 10, TimeUnit.SECONDS);
        thrConfig.getTaskScheduler().getScheduledThreadPoolExecutor().scheduleWithFixedDelay(diapazonScanRun, 2, UsefulUtilities.getScansDelay(), TimeUnit.MINUTES);
        schedule(scanOnlineRun, 3);
        
        schedule(popSmtpTest, (int) (ConstantsFor.DELAY * 2));
        schedule(new TemporaryFullInternet(), (int) ConstantsFor.DELAY);
        schedule((Runnable) InformationFactory.getInstance(InformationFactory.REGULAR_LOGS_SAVER), 4);
        
        AppInfoOnLoad.getMiniLogger().add(thrConfig.toString());
    }
    
    public void startIntervalTasks() {
        Date nextStartDay = MyCalen.getNextDayofWeek(23, 57, SUNDAY);
        scheduleStats(nextStartDay);
        nextStartDay = new Date(nextStartDay.getTime() - TimeUnit.HOURS.toMillis(1));
        scheduleIISLogClean(nextStartDay);
    }
    
    private void scheduleStats(Date nextStartDay) {
        Stats stats = Stats.getInstance(InformationFactory.STATS_WEEKLY_INTERNET);
        Stats instance = Stats.getInstance(InformationFactory.STATS_SUDNAY_PC_SORT);
        AppComponents.threadConfig().getTaskScheduler().scheduleWithFixedDelay((Runnable) instance, nextStartDay, ConstantsFor.ONE_WEEK_MILLIS);
        AppComponents.threadConfig().getTaskScheduler().scheduleWithFixedDelay((Runnable) stats, nextStartDay, ConstantsFor.ONE_WEEK_MILLIS);
        AppInfoOnLoad.getMiniLogger().add(nextStartDay + " WeekPCStats() start\n");
    }
    
    private static void scheduleIISLogClean(Date nextStartDay) {
        Runnable iisCleaner = new MailIISLogsCleaner();
        AppComponents.threadConfig().getTaskScheduler().scheduleWithFixedDelay(iisCleaner, nextStartDay, ConstantsFor.ONE_WEEK_MILLIS);
        AppInfoOnLoad.getMiniLogger().add(nextStartDay + " MailIISLogsCleaner() start\n");
    }
}