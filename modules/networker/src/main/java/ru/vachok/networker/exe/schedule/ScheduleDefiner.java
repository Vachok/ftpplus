package ru.vachok.networker.exe.schedule;


import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ad.inet.TemporaryFullInternet;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.services.MyCalen;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.exe.ThreadConfig;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.info.NetScanService;
import ru.vachok.networker.info.stats.Stats;
import ru.vachok.networker.mail.testserver.MailPOPTester;
import ru.vachok.networker.net.ssh.VpnHelper;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.sysinfo.AppConfigurationLocal;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static java.time.DayOfWeek.SUNDAY;


public class ScheduleDefiner implements AppConfigurationLocal {

    @Override
    public void run() {
        startPeriodicTasks();
        startIntervalTasks();
    }

    private void startPeriodicTasks() {
        NetScanService scanOnlineRun = NetScanService.getInstance(NetScanService.SCAN_ONLINE);
        NetScanService diapazonScanRun = NetScanService.getInstance(NetScanService.DIAPAZON);
        Runnable popSmtpTest = new MailPOPTester();
        Runnable openvpnStatusFileMaker = new VpnHelper();
        ThreadConfig thrConfig = AppComponents.threadConfig();
        thrConfig.getTaskScheduler().getScheduledThreadPoolExecutor().scheduleWithFixedDelay(diapazonScanRun, 2, UsefulUtilities.getScansDelay(), TimeUnit.MINUTES);
        schedule(scanOnlineRun, 3);
        schedule(popSmtpTest, (int) (ConstantsFor.DELAY * 2));
        schedule(new TemporaryFullInternet(), (int) ConstantsFor.DELAY);
        schedule((Runnable) InformationFactory.getInstance(InformationFactory.REGULAR_LOGS_SAVER), 5);
        schedule(openvpnStatusFileMaker, 1);
        schedule(this::sendStats, 60);
    }

    private void startIntervalTasks() {
        Date nextStartDay = MyCalen.getNextDayofWeek(23, 50, SUNDAY);
        scheduleStats(nextStartDay);
        nextStartDay = new Date(nextStartDay.getTime() - TimeUnit.HOURS.toMillis(1));
        scheduleIISLogClean(nextStartDay);
    }

    private void scheduleStats(Date nextStartDay) {
        Stats stats = Stats.getInstance(InformationFactory.STATS_WEEKLY_INTERNET);
        Stats instance = Stats.getInstance(InformationFactory.STATS_SUDNAY_PC_SORT);
        AppComponents.threadConfig().getTaskScheduler().scheduleWithFixedDelay((Runnable) instance, nextStartDay, ConstantsFor.ONE_WEEK_MILLIS);
        AppComponents.threadConfig().getTaskScheduler().scheduleWithFixedDelay((Runnable) stats, nextStartDay, ConstantsFor.ONE_WEEK_MILLIS);
    }

    private static void scheduleIISLogClean(Date nextStartDay) {
        Runnable iisCleaner = new MailIISLogsCleaner();
        AppComponents.threadConfig().getTaskScheduler().scheduleWithFixedDelay(iisCleaner, nextStartDay, ConstantsFor.ONE_WEEK_MILLIS);
    }

    private void sendStats() {
        InformationFactory dbInfo = InformationFactory.getInstance(InformationFactory.DATABASE_INFO);
        InformationFactory threadInfo = InformationFactory.getInstance(InformationFactory.MX_BEAN_THREAD);
        MessageToUser.getInstance(MessageToUser.EMAIL, getClass().getSimpleName()).info(UsefulUtilities.thisPC(), dbInfo.getInfo(), threadInfo.getInfo());
    }
}