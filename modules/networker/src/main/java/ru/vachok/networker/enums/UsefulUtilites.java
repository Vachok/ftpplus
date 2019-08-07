package ru.vachok.networker.enums;


import org.apache.commons.net.ntp.TimeInfo;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.AppInfoOnLoad;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.controller.ExCTRL;
import ru.vachok.networker.exe.runnabletasks.PfListsSrv;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.mailserver.ExSRV;
import ru.vachok.networker.mailserver.MailRule;
import ru.vachok.networker.services.TimeChecker;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;


/**
 @since 07.08.2019 (13:28) */
public enum UsefulUtilites {
    ;
    
    public static final int YEAR_OF_MY_B = 1984;
    
    /**
     Число, для Secure Random
     */
    public static final long MY_AGE = (long) Year.now().getValue() - YEAR_OF_MY_B;
    
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
    
    /**
     {@link ExCTRL#uplFile(MultipartFile, Model)}, {@link ExSRV#getOFields()},
     */
    private static final ConcurrentMap<Integer, MailRule> MAIL_RULES = new ConcurrentHashMap<>();
    
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
    
    /**
     @return кол-во выделенной, используемой и свободной памяти в МБ
     */
    public static @NotNull String getMemoryInfo() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<br>\n");
        stringBuilder.append(memoryMXBean.getHeapMemoryUsage()).append(" HeapMemoryUsage <br>\n ");
        stringBuilder.append(memoryMXBean.getNonHeapMemoryUsage()).append(" NonHeapMemoryUsage <br>\n ");
        stringBuilder.append(memoryMXBean.getObjectPendingFinalizationCount()).append(" ObjectPendingFinalizationCount.");
        
        return stringBuilder.toString();
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
    
    public static @NotNull String makeURLs(@NotNull Future<String> filesSizeFuture) throws ExecutionException, InterruptedException, TimeoutException {
        
        return new StringBuilder()
            .append("Запущено - ")
            .append(new Date(ConstantsFor.START_STAMP))
            .append(getUpTime())
            .append(" (<i>rnd delay is ")
            .append(ConstantsFor.DELAY)
            .append(" : ")
            .append(String.format("%.02f", (float) (getAtomicTime() - ConstantsFor.START_STAMP) / TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY)))
            .append(" delays)</i>")
            .append(".<br> Состояние памяти (МБ): <font color=\"#82caff\">")
            .append(getMemoryInfo())
            .append("<details><summary> disk usage by program: </summary>")
            .append(filesSizeFuture.get(ConstantsFor.DELAY - 10, TimeUnit.SECONDS)).append("</details></font><br>")
            .toString();
    }
    
    public static long getDelay() {
        long delay = new SecureRandom().nextInt((int) MY_AGE);
        if (delay < MIN_DELAY) {
            delay = MIN_DELAY;
        }
        if (thisPC().toLowerCase().contains(OtherKnownDevices.DO0213_KUDR) || thisPC().toLowerCase().contains(AppInfoOnLoad.HOSTNAME_HOME)) {
            return MIN_DELAY;
        }
        else {
            return delay;
        }
    }
    
    private static String getSeparator() {
        return System.getProperty(PropertiesNames.PRSYS_SEPARATOR);
    }
}
