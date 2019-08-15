// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.apache.commons.net.ntp.TimeInfo;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import ru.vachok.networker.accesscontrol.sshactions.PfListsSrv;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.componentsrepo.server.TelnetStarter;
import ru.vachok.networker.controller.ExCTRL;
import ru.vachok.networker.enums.ConstantsNet;
import ru.vachok.networker.enums.OtherKnownDevices;
import ru.vachok.networker.enums.PropertiesNames;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.mailserver.ExSRV;
import ru.vachok.networker.mailserver.MailRule;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.restapi.props.DBPropsCallable;
import ru.vachok.networker.services.TimeChecker;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 @see ru.vachok.networker.UsefulUtilitiesTest
 @since 07.08.2019 (13:28) */
public abstract class UsefulUtilities {
    
    
    public static final int YEAR_OF_MY_B = 1984;
    
    /**
     Число, для Secure Random
     */
    static final long MY_AGE = (long) Year.now().getValue() - YEAR_OF_MY_B;
    
    /**
     Кол-во минут в часе
     */
    public static final float ONE_HOUR_IN_MIN = 60f;
    
    /**
     Кол-во часов в сутках
     */
    public static final int ONE_DAY_HOURS = 24;
    
    private static final String[] STRINGS_TODELONSTART = {"visit_", ".tv", ".own", ".rgh"};
    
    private static final int MIN_DELAY = 17;
    
    private static final Properties APP_PROPS = AppComponents.getProps();
    
    /**
     {@link ExCTRL#uplFile(MultipartFile, Model)}, {@link ExSRV#getOFields()},
     */
    private static final ConcurrentMap<Integer, MailRule> MAIL_RULES = new ConcurrentHashMap<>();
    
    private static final MessageToUser MESSAGE_LOCAL = new MessageLocal(UsefulUtilities.class.getSimpleName());
    
    private static final String[] DELETE_TRASH_PATTERNS = {"DELETE  FROM `inetstats` WHERE `site` LIKE '%clients1.google%'", "DELETE  FROM `inetstats` WHERE `site` LIKE '%g.ceipmsn.com%'"};
    
    /**
     @return {@link #MAIL_RULES}
     */
    @Contract(pure = true)
    public static ConcurrentMap<Integer, MailRule> getMailRules() {
        return MAIL_RULES;
    }
    
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
    
    public static Visitor getVis(HttpServletRequest request) {
        return new AppComponents().visitor(request);
    }
    
    public static long getMyTime() {
        return LocalDateTime.of(YEAR_OF_MY_B, 1, 7, 2, 2).toEpochSecond(ZoneOffset.ofHours(3));
    }
    
    public static long getDelay() {
        long delay = new SecureRandom().nextInt((int) MY_AGE);
        if (delay < MIN_DELAY) {
            delay = MIN_DELAY;
        }
        if (thisPC().toLowerCase().contains(OtherKnownDevices.DO0213_KUDR) || thisPC().toLowerCase().contains(OtherKnownDevices.HOSTNAME_HOME)) {
            return MIN_DELAY;
        }
        else {
            return delay;
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
                stringBuilder.append(runProcess());
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
            (System.currentTimeMillis() - ConstantsFor.START_STAMP) / 1000 / ONE_HOUR_IN_MIN / ONE_HOUR_IN_MIN;
        if (hrsOn > ONE_DAY_HOURS) {
            hrsOn /= ONE_DAY_HOURS;
            tUnit = " d";
        }
        return "(" + String.format("%.03f", hrsOn) + tUnit + " uptime)";
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
        String s = totalSize / ConstantsFor.MBYTE + " MB IIS Logs\n";
        AppInfoOnLoad.MINI_LOGGER.add(s);
        return s;
    }
    
    public static @NotNull String[] getDeleteTrashPatterns() {
        List<String> fromFile = FileSystemWorker.readFileToList(new File("delete.inetaddress.txt").getAbsolutePath());
        fromFile.addAll(Arrays.asList(DELETE_TRASH_PATTERNS));
        return fromFile.toArray(new String[fromFile.size()]);
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
            System.err.println(e.getMessage() + " " + AppInfoOnLoad.class.getSimpleName() + ".getBuildStamp");
        }
        boolean isAppPropsSet = new DBPropsCallable().setProps(appPr);
        return retLong;
    }
    
    static void startTelnet() {
        final Thread telnetThread = new Thread(new TelnetStarter());
        telnetThread.setDaemon(true);
        telnetThread.start();
        MESSAGE_LOCAL.warn(MessageFormat.format("telnetThread.isAlive({0})", telnetThread.isAlive()));
    }
    
    private static @NotNull String runProcess() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        Process processFlushDNS = Runtime.getRuntime().exec("ipconfig /flushdns");
        InputStream flushDNSInputStream = processFlushDNS.getInputStream();
        InputStreamReader reader = new InputStreamReader(flushDNSInputStream);
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            bufferedReader.lines().forEach(stringBuilder::append);
        }
        return stringBuilder.toString();
    }
    
    @SuppressWarnings("MagicNumber")
    static int getScansDelay() {
        int scansInOneMin = Integer.parseInt(AppComponents.getUserPref().get(PropertiesNames.PR_SCANSINMIN, "111"));
        if (scansInOneMin <= 0) {
            scansInOneMin = 85;
        }
        if (scansInOneMin > 800) {
            scansInOneMin = 800;
        }
        return ConstantsNet.IPS_IN_VELKOM_VLAN / scansInOneMin;
    }
}
