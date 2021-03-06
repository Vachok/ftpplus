// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo;


import org.apache.commons.net.ntp.TimeInfo;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.IntoApplication;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.server.TelnetStarter;
import ru.vachok.networker.componentsrepo.services.MyCalen;
import ru.vachok.networker.componentsrepo.services.TimeChecker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.ConstantsNet;
import ru.vachok.networker.data.enums.OtherKnownDevices;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.net.ssh.PfListsSrv;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.restapi.props.InitProperties;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.lang.management.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.*;


/**
 @see ru.vachok.networker.componentsrepo.UsefulUtilitiesTest
 @since 07.08.2019 (13:28) */
@SuppressWarnings("ClassWithTooManyMethods")
public abstract class UsefulUtilities {


    private static final String[] STRINGS_TODELONSTART = {"visit_", ".tv", ".own", ".rgh", ".lck", "IntoApplication."};

    private static final Properties APP_PROPS = InitProperties.getTheProps();

    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, UsefulUtilities.class.getSimpleName());

    /**
     Доступность srv-git.eatmeat.ru.

     @return 192.168.13.42 online or offline
     */
    public static boolean isPingOK() {
        try {
            return InetAddress.getByName(PfListsSrv.getDefaultConnectSrv()).isReachable((int) (ConstantsFor.DELAY * 5));
        }
        catch (IOException e) {
            LoggerFactory.getLogger(ConstantsFor.class.getSimpleName()).error(e.getMessage(), e);
            return false;
        }
    }

    /**
     @return имена-паттерны временных файлов, которые надо удалить при запуске.
     */
    @Contract(pure = true)
    @NotNull
    public static List<String> getPatternsToDeleteFilesOnStart() {
        return Arrays.asList(STRINGS_TODELONSTART);
    }

    public static long getMyTime() {
        return LocalDateTime.of(ConstantsFor.YEAR_OF_MY_B, 1, 7, 2, 2).toEpochSecond(ZoneOffset.ofHours(3));
    }

    public static long getDelay() {
        long delay = new SecureRandom().nextInt((int) ConstantsFor.MY_AGE);
        if (delay < ConstantsFor.MIN_DELAY) {
            delay = ConstantsFor.MIN_DELAY;
        }
        if (thisPC().toLowerCase().contains(OtherKnownDevices.DO0213_KUDR.replace(ConstantsFor.DOMAIN_EATMEATRU, "")) | thisPC().toLowerCase()
            .contains(OtherKnownDevices.HOSTNAME_HOME) | thisPC().toLowerCase().contains("-h")) {
            delay = Long.parseLong(InitProperties.getTheProps().getProperty(PropertiesNames.MINDELAY, String.valueOf(5)));
        }
        return delay;
    }

    private static String getStorageInfo() {
        StringBuilder stringBuilder = new StringBuilder();
        try (FileSystem fileSystem = FileSystems.getDefault()) {
            for (FileStore storeSys : fileSystem.getFileStores()) {
                stringBuilder.append(storeSys.getUsableSpace() / ConstantsFor.MBYTE).append("/").append(storeSys.getTotalSpace() / ConstantsFor.MBYTE)
                    .append(" on ").append(storeSys.name()).append(" type: ").append(storeSys.type()).append("\n");
            }
        }
        catch (IOException | RuntimeException e) {
            stringBuilder.append(e.getMessage()).append("\n").append(UsefulUtilities.class.getSimpleName()).append(".getStorageInfo");
        }
        return stringBuilder.toString();
    }

    @NotNull
    public static String getRunningInformation() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Version: ").append(MessageFormat
            .format("{0} on {1}\n", InitProperties.getTheProps().getProperty(PropertiesNames.APPVERSION), System.getProperty(PropertiesNames.JAVA_VERSION)));
        stringBuilder.append("CPU information:").append("\n").append(getOS()).append("***\n\n");
        stringBuilder.append("Memory information:").append("\n").append(getMemory()).append("***\n\n");
        stringBuilder.append("Runtime information:").append("\n").append(getRuntime()).append("***\n\n");
        stringBuilder.append("Disks: ").append("\n").append(getStorageInfo()).append("***\n\n");
        return stringBuilder.toString();

    }

    /**
     @return OS info

     @see UsefulUtilitiesTest#testGetOS()
     */
    @NotNull
    public static String getOS() {
        StringBuilder stringBuilder = new StringBuilder();
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        stringBuilder.append(operatingSystemMXBean.getClass().getTypeName()).append("\n");
        stringBuilder.append(operatingSystemMXBean.getAvailableProcessors()).append(" Available Processors\n");
        stringBuilder.append(operatingSystemMXBean.getName()).append(" Name\n");
        stringBuilder.append(operatingSystemMXBean.getVersion()).append(" Version\n");
        stringBuilder.append(operatingSystemMXBean.getArch()).append(" Arch\n");
        if (!System.getProperty("os.name").toLowerCase().contains("win")) {
            stringBuilder.append(operatingSystemMXBean.getSystemLoadAverage()).append(" System Load Average\n");
        }
        stringBuilder.append(operatingSystemMXBean.getObjectName()).append(" Object Name\n");
        stringBuilder.append(getTotalCPUTimeInformation()).append("\n");

        return stringBuilder.toString();
    }

    @NotNull
    public static String getMemory() {
        StringBuilder stringBuilder = new StringBuilder();

        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        memoryMXBean.setVerbose(true);
        stringBuilder.append(memoryMXBean.getHeapMemoryUsage()).append(" Heap Memory Usage; \n");
        stringBuilder.append(memoryMXBean.getNonHeapMemoryUsage()).append(" NON Heap Memory Usage; \n");
        stringBuilder.append(memoryMXBean.getObjectPendingFinalizationCount()).append(" Object Pending Finalization Count; \n");

        ClassLoadingMXBean classLoading = ManagementFactory.getClassLoadingMXBean();
        stringBuilder.append(classLoading.getLoadedClassCount()).append(" Loaded Class Count; \n");
        stringBuilder.append(classLoading.getUnloadedClassCount()).append(" Unloaded Class Count; \n");
        stringBuilder.append(classLoading.getTotalLoadedClassCount()).append(" Total Loaded Class Count; \n");

        CompilationMXBean compileBean = ManagementFactory.getCompilationMXBean();
        stringBuilder.append(compileBean.getName()).append(" Name; \n");
        stringBuilder.append(compileBean.getTotalCompilationTime()).append(" Total Compilation Time; \n");

        return stringBuilder.toString();
    }

    @NotNull
    public static String getRuntime() {
        StringBuilder stringBuilder = new StringBuilder();
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        stringBuilder.append(runtimeMXBean.getClass().getSimpleName()).append("\n");
        stringBuilder.append(new Date(runtimeMXBean.getStartTime())).append(" StartTime, Running: ")
            .append(countRun(runtimeMXBean)).append(" \n");
        stringBuilder.append(InformationFactory.getInstance(InformationFactory.MX_BEAN_THREAD).getInfo());
        return stringBuilder.toString();
    }

    private static String countRun(@NotNull RuntimeMXBean runtimeMXBean) {
        float l = TimeUnit.MILLISECONDS.toMinutes(runtimeMXBean.getUptime());
        String runTimer = l + " minutes";

        if (l > 60) {
            l = l / 60f;
            runTimer = l + " hours";
            if (l > 24) {
                l = l / 24f;
                runTimer = l + " days";
            }
        }
        return runTimer;
    }

    /**
     @return точное время как {@code long}

     @see UsefulUtilitiesTest#testGetAtomicTime()
     */
    public static long getAtomicTime() {
        long result;
        TimeChecker t = new TimeChecker();
        Future<TimeInfo> infoFuture = AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor().submit(t);
        try {
            TimeInfo call = infoFuture.get(20, TimeUnit.SECONDS);
            call.computeDetails();
            result = call.getReturnTime();
        }
        catch (InterruptedException | RuntimeException e) {
            messageToUser.warn(UsefulUtilities.class.getSimpleName(), e.getMessage(), " see line: 180 ***");
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
            result = System.currentTimeMillis();
        }
        catch (ExecutionException | TimeoutException e) {
            messageToUser.warn(UsefulUtilities.class.getSimpleName(), e.getMessage(), " see line: 185 ***");
            result = System.currentTimeMillis();
        }
        return result;
    }

    @NotNull
    public static String getTotalCPUTimeInformation() {
        String cpuTime = getTotCPUTime();
        return MessageFormat.format("Total CPU time for all threads = {0}. Max time: {1}", cpuTime, maxCPUThread());
    }

    @NotNull
    public static String getTotCPUTime() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        String cpuTimeStr;
        long cpuTime = 0;
        long userTime = 0;
        for (long id : bean.getAllThreadIds()) {
            cpuTime += bean.getThreadCpuTime(id);
            userTime += bean.getThreadUserTime(id);
        }
        cpuTimeStr = MessageFormat.format("{0} sec. (user - {1} sec)", TimeUnit.NANOSECONDS.toSeconds(cpuTime), TimeUnit.NANOSECONDS.toSeconds(userTime));
        return cpuTimeStr;
    }

    @NotNull
    private static String maxCPUThread() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        Map<String, Long> allThreadsCPU = new ConcurrentHashMap<>();
        StringBuilder stringBuilder = new StringBuilder();

        try {
            for (long threadId : bean.getAllThreadIds()) {
                ThreadInfo info = bean.getThreadInfo(threadId);
                allThreadsCPU
                    .put(MessageFormat.format("{0}, INFO: {1}\n{2}", info.getThreadName(), info.toString(), new TForms().fromArray(info.getStackTrace(), true)), bean
                        .getThreadCpuTime(threadId));
            }
        }
        catch (RuntimeException e) {
            messageToUser.error(e.getMessage() + " see line: 361 ***");
        }

        Optional<Long> maxOpt = allThreadsCPU.values().stream().max(Comparator.naturalOrder());
        maxOpt.ifPresent(stringBuilder::append);
        for (Map.Entry<String, Long> stringLongEntry : allThreadsCPU.entrySet()) {
            maxOpt.ifPresent(aLong->{
                if (stringLongEntry.getValue().equals(aLong)) {
                    stringBuilder.append(" ").append(stringLongEntry.getKey());
                }
            });
        }
        return stringBuilder.toString();
    }

    /**
     Этот ПК
     <p>

     @return имя компьютера, где запущено
     */
    public static String thisPC() {
        try {
            return InetAddress.getLocalHost().getHostName();
        }
        catch (UnknownHostException | ExceptionInInitializerError | NullPointerException e) {
            return IntoApplication.getCTXEnvironment().getProperty(PropertiesNames.COMPUTERNAME);
        }
    }

    /**
     @return время билда
     */
    public static long getBuildStamp() {
        long retLong = 1L;
        InitProperties initProperties = InitProperties.getInstance(InitProperties.DB_MEMTABLE);
        Properties appPr = InitProperties.getTheProps();
        try {
            String hostName = InetAddress.getLocalHost().getHostName();
            if (hostName.equalsIgnoreCase(OtherKnownDevices.DO0213_KUDR) || hostName.toLowerCase().contains(OtherKnownDevices.HOSTNAME_HOME)) {
                appPr.setProperty(PropertiesNames.BUILDTIME, String.valueOf(System.currentTimeMillis()));
                SimpleDateFormat weekNumFormat = new SimpleDateFormat("w");
                appPr.setProperty(PropertiesNames.APPVERSION, "8.0.19" + weekNumFormat.format(new Date()));
                retLong = System.currentTimeMillis();
            }
            else {
                retLong = Long.parseLong(appPr.getProperty(PropertiesNames.BUILDTIME, "1"));
            }
        }
        catch (UnknownHostException | NumberFormatException e) {
            messageToUser.error(MessageFormat
                .format("UsefulUtilities.getBuildStamp {0} - {1}\nStack:\n{2}", e.getClass().getTypeName(), e.getMessage(), new TForms().fromArray(e)));
        }
        initProperties.setProps(appPr);
        return retLong;
    }

    /**
     @return Время работы в часах.
     */
    @NotNull
    public static String getUpTime() {
        String tUnit = " h";
        float hrsOn = (float)
            (System.currentTimeMillis() - ConstantsFor.START_STAMP) / 1000 / ConstantsFor.ONE_HOUR_IN_MIN / ConstantsFor.ONE_HOUR_IN_MIN;
        if (hrsOn > ConstantsFor.ONE_DAY_HOURS) {
            hrsOn /= ConstantsFor.ONE_DAY_HOURS;
            tUnit = " d";
        }
        return MessageFormat.format("({0} {1} up)", String.format("%.03f", hrsOn), tUnit);
    }

    /**
     Получение размера логов IIS-Exchange.
     <p>
     Путь до папки из {@link #APP_PROPS} iispath. <br> {@code Path iisLogsDir} = {@link Objects#requireNonNull(Object)} -
     {@link Path#toFile()}.{@link File#listFiles()}. <br> Для каждого
     файла из папки, {@link File#length()}. Складываем {@code totalSize}. <br> {@code totalSize/}{@link ConstantsFor#MBYTE}.

     @return размер папки логов IIS в мегабайтах
     */
    @NotNull
    public static String getIISLogSize() {
        Path iisLogsDir = Paths.get(APP_PROPS.getProperty("iispath", "\\\\srv-mail3.eatmeat.ru\\c$\\inetpub\\logs\\LogFiles\\W3SVC1\\"));
        long totalSize = 0L;
        for (File x : Objects.requireNonNull(iisLogsDir.toFile().listFiles())) {
            totalSize += x.length();
        }
        return totalSize / ConstantsFor.MBYTE + " MB IIS Logs\n";
    }

    @SuppressWarnings("MagicNumber")
    public static int getScansDelay() {
        int scansInOneMin = Integer.parseInt(InitProperties.getTheProps().getProperty(PropertiesNames.SCANSINMIN, "111"));
        if (scansInOneMin <= 0) {
            scansInOneMin = 85;
        }
        if (scansInOneMin > 800) {
            scansInOneMin = 800;
        }
        return ConstantsNet.IPS_IN_VELKOM_VLAN / scansInOneMin;
    }

    public static int getLogLevel() {
        return InitProperties.getUserPref().getInt("loglevel", ConstantsFor.LOGLEVEL);
    }

    @NotNull
    public static String scheduleTrunkPcUserAuto() {
        Runnable trunkTableUsers = UsefulUtilities::trunkTableUsers;
        ScheduledThreadPoolExecutor schedExecutor = AppComponents.threadConfig().getTaskScheduler().getScheduledThreadPoolExecutor();
        schedExecutor.scheduleWithFixedDelay(trunkTableUsers, getDelayMs(), ConstantsFor.ONE_WEEK_MILLIS, TimeUnit.MILLISECONDS);
        return AppComponents.threadConfig().getTaskScheduler().toString();
    }

    /**
     Очистка pcuserauto
     */
    private static void trunkTableUsers() {
        try (Connection c = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(ConstantsFor.STR_VELKOM + "." + ConstantsFor.DB_PCUSERAUTO);
             PreparedStatement preparedStatement = c.prepareStatement("TRUNCATE TABLE pcuserauto")) {
            preparedStatement.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected static long getDelayMs() {
        Date dateStart = MyCalen.getNextDayofWeek(8, 30, DayOfWeek.MONDAY);
        DateFormat dateFormat = new SimpleDateFormat("MM.dd, hh:mm", Locale.getDefault());
        return dateStart.getTime() - System.currentTimeMillis();
    }

    public static Visitor getVis(HttpServletRequest request) {
        return new AppComponents().visitor(request);
    }

    /**
     @return ipconfig /flushdns results from console

     @throws UnsupportedOperationException if non Windows OS
     @see ru.vachok.networker.AppComponentsTest#testIpFlushDNS
     */
    @NotNull
    public static String ipFlushDNS() {
        StringBuilder stringBuilder = new StringBuilder();
        if (System.getProperty("os.name").toLowerCase().contains(PropertiesNames.WINDOWSOS)) {
            stringBuilder.append(runProcess("ipconfig /flushdns"));
        }
        else {
            stringBuilder.append(runProcess(ConstantsFor.SSH_UNAMEA));
        }
        return stringBuilder.toString();
    }

    @NotNull
    private static String runProcess(String cmdProcess) {
        StringBuilder stringBuilder = new StringBuilder();
        Process processFlushDNS = null;
        String name = "UsefulUtilities.runProcess";
        try {
            processFlushDNS = Runtime.getRuntime().exec(cmdProcess);
        }
        catch (IOException e) {
            System.err.println(MessageFormat.format(name, e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace())));
        }

        try (InputStream flushDNSInputStream = processFlushDNS.getInputStream()) {
            try (InputStreamReader reader = new InputStreamReader(flushDNSInputStream)) {
                try (BufferedReader bufferedReader = new BufferedReader(reader)) {
                    bufferedReader.lines().forEach(stringBuilder::append);
                }
            }
        }
        catch (IOException e) {
            System.err.println(MessageFormat.format(name, e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace())));
        }
        return stringBuilder.toString();
    }

    public static void startTelnet() {
        final Thread telnetThread = new Thread(new TelnetStarter());
        telnetThread.setDaemon(true);
        telnetThread.start();
        messageToUser.warn(MessageFormat.format("telnetThread.isAlive({0})", telnetThread.isAlive()));
    }

    @Contract(pure = true)
    @NotNull
    public static String getHTMLCenterColor(String color, String text) {
        String tagOpen = "<center><font color=\"" + color + "\">";
        String tagClose = "</font></center>";
        return tagOpen + text + tagClose;
    }

}
