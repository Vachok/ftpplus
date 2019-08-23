// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo;


import org.apache.commons.net.ntp.TimeInfo;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.componentsrepo.server.TelnetStarter;
import ru.vachok.networker.componentsrepo.services.TimeChecker;
import ru.vachok.networker.data.enums.*;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.restapi.props.DBPropsCallable;
import ru.vachok.networker.ssh.PfListsSrv;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


/**
 @see ru.vachok.networker.UsefulUtilitiesTest
 @since 07.08.2019 (13:28) */
public abstract class UsefulUtilities {
    
    
    private static final String[] STRINGS_TODELONSTART = {"visit_", ".tv", ".own", ".rgh"};
    
    private static final Properties APP_PROPS = AppComponents.getProps();
    
    private static final MessageToUser MESSAGE_LOCAL = new MessageLocal(UsefulUtilities.class.getSimpleName());
    
    private static final String[] DELETE_TRASH_PATTERNS = {"DELETE  FROM `inetstats` WHERE `site` LIKE '%clients1.google%'", "DELETE  FROM `inetstats` WHERE `site` LIKE '%g.ceipmsn.com%'"};
    
    private static long cpuTime = 0;
    
    private static MessageToUser messageToUser = new MessageLocal(UsefulUtilities.class.getSimpleName());
    
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
    public static String[] getStringsVisit() {
        return STRINGS_TODELONSTART;
    }
    
    public static Visitor getVis(HttpServletRequest request) {
        return new AppComponents().visitor(request);
    }
    
    public static long getMyTime() {
        return LocalDateTime.of(ConstantsFor.YEAR_OF_MY_B, 1, 7, 2, 2).toEpochSecond(ZoneOffset.ofHours(3));
    }
    
    public static long getDelay() {
        long delay = new SecureRandom().nextInt((int) ConstantsFor.MY_AGE);
        if (delay < ConstantsFor.MIN_DELAY) {
            delay = ConstantsFor.MIN_DELAY;
        }
        if (thisPC().toLowerCase().contains(OtherKnownDevices.DO0213_KUDR) || thisPC().toLowerCase().contains(OtherKnownDevices.HOSTNAME_HOME)) {
            return ConstantsFor.MIN_DELAY;
        }
        else {
            return delay;
        }
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
            String retStr = new TForms().fromArray((List<?>) e, false);
            FileSystemWorker.writeFile("this_pc.err", Collections.singletonList(retStr));
            return "pc";
        }
    }
    
    /**
     @return ipconfig /flushdns results from console
     
     @throws UnsupportedOperationException if non Windows OS
     @see ru.vachok.networker.AppComponentsTest#testIpFlushDNS
     */
    public static @NotNull String ipFlushDNS() {
        StringBuilder stringBuilder = new StringBuilder();
        if (System.getProperty("os.name").toLowerCase().contains(PropertiesNames.PR_WINDOWSOS)) {
            try {
                stringBuilder.append(runProcess("ipconfig /flushdns"));
            }
            catch (IOException e) {
                stringBuilder.append(e.getMessage());
            }
        }
        else {
            stringBuilder.append(System.getProperty("os.name"));
        }
        return stringBuilder.toString();
    }
    
    /**
     @return Время работы в часах.
     */
    public static @NotNull String getUpTime() {
        String tUnit = " h";
        float hrsOn = (float)
                (System.currentTimeMillis() - ConstantsFor.START_STAMP) / 1000 / ConstantsFor.ONE_HOUR_IN_MIN / ConstantsFor.ONE_HOUR_IN_MIN;
        if (hrsOn > ConstantsFor.ONE_DAY_HOURS) {
            hrsOn /= ConstantsFor.ONE_DAY_HOURS;
            tUnit = " d";
        }
        return "(" + String.format("%.03f", hrsOn) + tUnit + " uptime)";
    }
    
    public static @NotNull String getRunningInformation() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("CPU information:").append("\n").append(InformationFactory.getOS()).append("***\n");
        stringBuilder.append("Memory information:").append("\n").append(InformationFactory.getMemory()).append("***\n");
        stringBuilder.append("Runtime information:").append("\n").append(InformationFactory.getRuntime()).append("***\n");
        return stringBuilder.toString();
        
    }
    
    /**
     @return время билда
     */
    public static long getBuildStamp() {
        long retLong = 1L;
        Properties appPr = AppComponents.getProps();
        try {
            String hostName = InetAddress.getLocalHost().getHostName();
            if (hostName.equalsIgnoreCase(OtherKnownDevices.DO0213_KUDR) || hostName.toLowerCase().contains(OtherKnownDevices.HOSTNAME_HOME)) {
                appPr.setProperty(PropertiesNames.PR_APP_BUILDTIME, String.valueOf(System.currentTimeMillis()));
                retLong = System.currentTimeMillis();
            }
            else {
                retLong = Long.parseLong(appPr.getProperty(PropertiesNames.PR_APP_BUILDTIME, "1"));
            }
        }
        catch (UnknownHostException | NumberFormatException e) {
            messageToUser.error(MessageFormat
                    .format("UsefulUtilities.getBuildStamp {0} - {1}\nStack:\n{2}", e.getClass().getTypeName(), e.getMessage(), new TForms().fromArray(e)));
        }
        boolean isAppPropsSet = new DBPropsCallable().setProps(appPr);
        return retLong;
    }
    
    /**
     @return точное время как {@code long}
     */
    public static long getAtomicTime() {
        TimeChecker t = new TimeChecker();
        TimeInfo call = t.call();
        call.computeDetails();
        return call.getReturnTime();
    }
    
    @Contract(pure = true)
    public static @NotNull String getHTMLCenterColor(String color, String text) {
        String tagOpen = "<center><font color=\"" + color + "\">";
        String tagClose = "</font></center>";
        return tagOpen + text + tagClose;
    }
    
    /**
     Получение размера логов IIS-Exchange.
     <p>
     Путь до папки из {@link #APP_PROPS} iispath. <br> {@code Path iisLogsDir} = {@link Objects#requireNonNull(Object)} -
     {@link Path#toFile()}.{@link File#listFiles()}. <br> Для каждого
     файла из папки, {@link File#length()}. Складываем {@code totalSize}. <br> {@code totalSize/}{@link ConstantsFor#MBYTE}.
     
     @return размер папки логов IIS в мегабайтах
     */
    public static @NotNull String getIISLogSize() {
        Path iisLogsDir = Paths.get(APP_PROPS.getProperty("iispath", "\\\\srv-mail3.eatmeat.ru\\c$\\inetpub\\logs\\LogFiles\\W3SVC1\\"));
        long totalSize = 0L;
        for (File x : Objects.requireNonNull(iisLogsDir.toFile().listFiles())) {
            totalSize += x.length();
        }
        return totalSize / ConstantsFor.MBYTE + " MB IIS Logs\n";
    }
    
    public static @NotNull String[] getDeleteTrashPatterns() {
        List<String> fromFile = FileSystemWorker.readFileToList(new File("delete.inetaddress.txt").getAbsolutePath());
        fromFile.addAll(Arrays.asList(DELETE_TRASH_PATTERNS));
        return fromFile.toArray(new String[fromFile.size()]);
    }
    
    @SuppressWarnings("MagicNumber")
    public static int getScansDelay() {
        int scansInOneMin = Integer.parseInt(AppComponents.getUserPref().get(PropertiesNames.PR_SCANSINMIN, "111"));
        if (scansInOneMin <= 0) {
            scansInOneMin = 85;
        }
        if (scansInOneMin > 800) {
            scansInOneMin = 800;
        }
        return ConstantsNet.IPS_IN_VELKOM_VLAN / scansInOneMin;
    }
    
    public static void setPreference(String prefName, String prefValue) {
        Preferences userPref = AppComponents.getUserPref();
        userPref.put(prefName, prefValue);
        try {
            userPref.sync();
        }
        catch (BackingStoreException e) {
            messageToUser.error(MessageFormat.format("AppComponents.setPreference: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
    }
    
    public static long getCPUTime() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        for (long id : bean.getAllThreadIds()) {
            UsefulUtilities.cpuTime += bean.getThreadCpuTime(id);
        }
        return UsefulUtilities.cpuTime;
    }
    
    public static void startTelnet() {
        final Thread telnetThread = new Thread(new TelnetStarter());
        telnetThread.setDaemon(true);
        telnetThread.start();
        MESSAGE_LOCAL.warn(MessageFormat.format("telnetThread.isAlive({0})", telnetThread.isAlive()));
    }
    
    private static @NotNull String runProcess(String cmdProcess) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        Process processFlushDNS = Runtime.getRuntime().exec(cmdProcess);
        InputStream flushDNSInputStream = processFlushDNS.getInputStream();
        InputStreamReader reader = new InputStreamReader(flushDNSInputStream);
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            bufferedReader.lines().forEach(stringBuilder::append);
        }
        return stringBuilder.toString();
    }
}
