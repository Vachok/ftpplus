// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.apache.commons.net.ntp.TimeInfo;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageSwing;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.controller.ExCTRL;
import ru.vachok.networker.enums.FileNames;
import ru.vachok.networker.enums.PropertiesNames;
import ru.vachok.networker.exe.runnabletasks.PfListsSrv;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.mailserver.ExSRV;
import ru.vachok.networker.mailserver.MailRule;
import ru.vachok.networker.net.PCUserResolver;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.services.TimeChecker;
import ru.vachok.networker.systray.ActionDefault;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;


/**
 Константы, используемые в приложении
 <p>
 
 @since 12.08.2018 (16:26) */
public enum ConstantsFor {
    ;
    
    public static final String HEAD_REFERER = "referer";
    
    public static final String METHNAME_ACTIONPERFORMED = "actionPerformed";
    
    public static final String STR_VELKOM = "velkom";
    
    public static final String DBFIELD_PCNAME = "pcName";
    
    public static final String DBFIELD_TIMESPEND = "TimeSpend";
    
    public static final String DBFIELD_TIMESTAMP = "TimeStamp";
    
    public static final String DBBASENAME_U0466446_LIFERPG = "u0466446_liferpg";
    
    public static final String DBQUERY_SELECTFROMSPEED = "select * from speed";
    
    public static final String DBFIELD_PCUSERAUTO = "pcuserauto";
    
    public static final String DBFIELD_PCUSER = "pcuser";
    
    /**
     <i>Boiler Plate</i>
     */
    public static final String BEANNAME_PFLISTS = "pflists";
    
    /**
     <i>Boiler Plate</i>
     */
    public static final String BEANNAME_CALCULATOR = "simpleCalculator";
    
    /**
     Диапазон для бинов
     */
    public static final String SINGLETON = "singleton";
    
    /**
     Название БД в reg.ru
     */
    public static final String DBBASENAME_U0466446_VELKOM = "u0466446_velkom";
    
    /**
     HTTP-header
     */
    public static final String HEAD_REFRESH = "Refresh";
    
    /**
     Строка из Live Template soutm
     */
    public static final String STR_SEC_SPEND = " sec spend";
    
    /**
     <b>1 мегабайт в байтах</b>
     */
    public static final int MBYTE = 1048576;
    
    /**
     Префикс имени от reg.ru
     */
    public static final String DBPREFIX = "u0466446_";
    
    public static final int YEAR_OF_MY_B = 1984;
    
    /**
     Число, для Secure Random
     */
    public static final long MY_AGE = (long) Year.now().getValue() - YEAR_OF_MY_B;
    
    public static final String HOSTNAME_DO213 = "do0213";
    
    /**
     Кол-во минут в часе
     */
    public static final float ONE_HOUR_IN_MIN = 60f;
    
    /**
     Кол-во байт в килобайте
     */
    public static final int KBYTE = 1024;
    
    /**
     Timestamp запуска.
     */
    public static final long START_STAMP = System.currentTimeMillis();
    
    /**
     MSEC таймаут
     */
    public static final int TIMEOUT_650 = 650;
    
    /**
     Имя для БД с настройками
     */
    public static final String APPNAME_WITHMINUS = "ru_vachok_networker-";
    
    public static final int HOURS_IN_DAY = 24;
    
    /**
     Кол-во часов в сутках
     */
    public static final int ONE_DAY_HOURS = 24;
    
    /**
     Кол-во байт в гигабайте
     */
    public static final long GBYTE = 1073741824;
    
    public static final String DB_FIELD_USER = "userName";
    
    /**
     Кол-во дней в году
     */
    public static final int ONE_YEAR = 365;
    
    /**
     Повторения в классах
     */
    public static final String STR_DELETED = " STR_DELETED";
    
    /**
     Кол-во миллисек. в 1 неделе
     */
    public static final long ONE_WEEK_MILLIS = TimeUnit.HOURS.toMillis(ONE_DAY_HOURS * (long) 7);
    
    /**
     Первоначальная задержка шедулера.
     */
    public static final long INIT_DELAY = MY_AGE;
    
    public static final String HTML_CENTER_CLOSE = "</center>";
    
    public static final String STR_INPUT_OUTPUT = "input/output\n";
    
    public static final String BEANNAME_MATRIX = "matrix";
    
    public static final String CLASS_NAME_PCUSERRESOLVER = PCUserResolver.class.getSimpleName();
    
    /**
     {@link #getDelay()}
     */
    public static final long DELAY = getDelay();
    
    public static final Runnable INFO_MSG_RUNNABLE = ()->{
        File todoFileHome = new File("G:\\My_Proj\\FtpClientPlus\\modules\\networker\\TODO");
        File todoFileWork = new File("C:\\Users\\ikudryashov\\IdeaProjects\\spring\\modules\\networker\\TODO");
        if (todoFileHome.exists() || todoFileWork.exists()) {
            new MessageSwing(new ActionDefault("https://github.com/Vachok/ftpplus/issues")).warn("CHECK TODO!");
        }
        else {
            new MessageCons(ConstantsFor.class.getSimpleName()).info("ConstantsFor.INFO_MSG_RUNNABLE", "thisPC()", " = " + thisPC());
        }
    };
    
    /**
     Домен с точкой
     */
    public static final String DOMAIN_EATMEATRU = ".eatmeat.ru";
    
    public static final double KM_A107 = 21.6;
    
    public static final double KM_M9 = 31.2;
    
    public static final int EXIT_STATUSBAD = 666;
    
    public static final int INT_ANSWER = 4;
    
    public static final String PROPS_FILE_JAVA_ID = ConstantsFor.class.getSimpleName() + FileNames.FILEEXT_PROPERTIES;
    
    public static final String STR_VERSIONINFO = "versioninfo";
    
    public static final String DBTABLE_GENERALJSCH = "general-jsch";
    
    public static final String SQL_SELECTFROM_PCUSERAUTO = "select * from pcuserauto";
    
    public static final String STR_BYTES = " bytes";
    
    public static final String DBFIELD_RESPONSE = "response";
    
    public static final String DBFIELD_METHOD = "method";
    
    public static final String HTML_PCENTER = "<p><center>";
    
    public static final int EXIT_USEREXIT = 222;
    
    public static final String PROGNAME_OSTPST = "ostpst-";
    
    public static final String FILESUF_SSHACTIONS = "sshactions";
    
    public static final String HTMLTAG_CENTER = "<center>";
    
    public static final String STR_PROPERTIES = "properties";
    
    public static final String DBFIELD_TIMENOW = "TimeNow";
    
    public static final String HTMLTAG_DETAILSCLOSE = "</details>";
    
    public static final String COMMAND_CALCTIMES = "calctimes:";
    
    public static final String COMMAND_CALCTIME = "calctime:";
    
    public static final String STR_ERROR = "ERROR";
    
    public static final String METHNAME_RUNSOCKET = ".runSocket";
    
    public static final String STR_WRITTEN = " written";
    
    public static final String TOSTRING_NAME = "fileName='";
    
    public static final int FTP_PORT = 21;
    
    public static final String FILESYSTEM_SEPARATOR = System.getProperty(PropertiesNames.PRSYS_SEPARATOR);
    
    public static final String RETURN_ERROR = "error";
    
    public static final String SQL_SELECTINETSTATS = "SELECT DISTINCT `ip` FROM `inetstats`";
    
    public static final String STR_INETSTATS = "inetstats";
    
    /**
     Путь к архиву
     */
    public static final Path ARCHIVE_DIR = Paths.get("\\\\192.168.14.10\\IT-Backup\\Srv-Fs\\Archives");
    
    /**
     Путь к продуктиву
     */
    public static final Path COMMON_DIR = Paths.get("\\\\srv-fs.eatmeat.ru\\common_new");
    
    @SuppressWarnings("DuplicateStringLiteralInspection") public static final String CP_WINDOWS_1251 = "windows-1251";
    
    public static final String ROOT_PATH_WITH_SEPARATOR = Paths.get(".").toAbsolutePath().normalize() + System.getProperty(PropertiesNames.PRSYS_SEPARATOR);
    
    public static final String DBBASENAME_U0466446_TESTING = "u0466446_testing";
    
    public static final String DBBASENAME_U0466446_WEBAPP = "u0466446_webapp";
    
    public static final String[] EXCLUDED_FOLDERS_FOR_CLEANER = {"01_Дирекция", "Положения_должностные_инструкции"};
    
    public static final String SRV_MAIL3 = "srv-mail3.eatmeat.ru";
    
    public static final String USER_SCANNER = "Scanner";
    
    public static final String STR_FALSE = "false";
    
    public static final String SITENAME_VELKOMFOODRU = "http://www.velkomfood.ru";
    
    public static final String SSH_COM_CATALLOWDOMAIN = "sudo cat /etc/pf/allowdomain";
    
    public static final String BEANNAME_PFLISTSSRV = "PfListsSrv";
    
    public static final String FOLDERNAME_COMMONNEW = "common_new";
    
    public static final String DIRNAME_ARCHIVES = "archives";
    
    public static final String STR_UNKNOWN = "Unknown";
    
    public static final String STR_OWNEDBY = " owned by: ";
    
    public static final String DBFIELD_SPEED = "Speed";
    
    public static final String DBFIELD_TIMEIN = "Timein";
    
    public static final String DBFIELD_TIMEOUT = "Timeout";
    
    public static final String PATTERN_POINT = ".";
    
    public static final int MINUTES_IN_STD_WORK_DAY = 540;
    
    public static final String STR_BR = "<br>";
    
    public static final String STR_N = "\n";
    
    public static final String STR_P = "<p>";
    
    public static final String STR_ACTIONPERFORMED = ".actionPerformed";
    
    public static final String SSH_SHOW_PFSQUID = "sudo cat /etc/pf/squid && exit";
    
    public static final String SSH_SHOW_SQUIDLIMITED = "sudo cat /etc/pf/squidlimited && exit";
    
    public static final String SSH_SHOW_PROXYFULL = "sudo cat /etc/pf/tempfull && exit";
    
    public static final String TOSTRING_PROPERTIES = "properties = ";
    
    public static final String STREAMJAR_PROPERTIES = "/static/const.properties";
    
    public static final String GOOD_NO_LOCKS = "No deadlocks, good!";
    
    public static final String DBFIELD_TIMESET = "timeset";
    
    public static final String STATIC_INITIALIZER = ".static initializer";
    
    public static final String DBBASENAME_U0466446_PROPERTIES = "u0466446_properties";
    
    public static final String ATTRIB_HIDDEN = "dos:hidden";
    
    public static final String TOSTRING_EXECUTOR = "executor = ";
    
    public static final String PS_IMPORTSYSMODULES = "ImportSystemModules";
    
    public static final String SSHCOM_24HRS = " >> /etc/pf/24hrs;";
    
    public static final String NOT_ALLOWED = "NOT Allowed!";
    
    public static final String ANS_DOMNAMEEXISTS = "Domain is exists!";
    
    public static final String ANS_DNAMENULL = "allowdomain string is null";
    
    public static final String SSH_ALLOWDOM_ALLOWDOMTMP = "' /etc/pf/allowdomain > /etc/pf/allowdomain_tmp;";
    
    public static final String SSH_ALLOWIP_ALLOWIPTMP = "' /etc/pf/allowip > /etc/pf/allowip_tmp;";
    
    public static final String SSH_ALLOWDOMTMP_ALLOWDOM = "sudo cp /etc/pf/allowdomain_tmp /etc/pf/allowdomain;";
    
    public static final String SSH_ALLOWIPTMP_ALLOWIP = "sudo cp /etc/pf/allowip_tmp /etc/pf/allowip;";
    
    public static final String SSH_TAIL_ALLOWIPALLOWDOM = "sudo tail /etc/pf/allowdomain;sudo tail /etc/pf/allowip;";
    
    static final String STR_FINISH = " is finish";
    
    private static final String[] STRINGS_TODELONSTART = {"visit_", ".tv", ".own", ".rgh"};
    
    private static final int MIN_DELAY = 17;
    
    /**
     {@link MessageLocal}
     */
    private static final MessageToUser messageToUser = new MessageLocal(ConstantsFor.class.getSimpleName());
    
    /**
     {@link ExCTRL#uplFile(MultipartFile, Model)}, {@link ExSRV#getOFields()},
     */
    private static final ConcurrentMap<Integer, MailRule> MAIL_RULES = new ConcurrentHashMap<>();
    
    public static final String PREF_NODE_NAME = "networker";
    
    /**
     @return {@link #MAIL_RULES}
     */
    public static ConcurrentMap<Integer, MailRule> getMailRules() {
        return MAIL_RULES;
    }
    
    /**
     Доступность srv-git.eatmeat.ru.
     
     @return 192.168.13.42 online or offline
     */
    public static boolean isPingOK() {
        try {
            return InetAddress.getByName(PfListsSrv.getDefaultConnectSrv()).isReachable((int) (DELAY * 5));
        }
        catch (IOException e) {
            LoggerFactory.getLogger(ConstantsFor.class.getSimpleName()).error(e.getMessage(), e);
            return false;
        }
    }
    
    /**
     @return Время работы в часах.
     */
    public static String getUpTime() {
        String tUnit = " h";
        float hrsOn = (float)
            (System.currentTimeMillis() - START_STAMP) / 1000 / ONE_HOUR_IN_MIN / ONE_HOUR_IN_MIN;
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
    public static String getMemoryInfo() {
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
    
    public static @NotNull String makeURLs(Future<String> filesSizeFuture) throws ExecutionException, InterruptedException, TimeoutException {
        
        return new StringBuilder()
            .append("Запущено - ")
            .append(new Date(START_STAMP))
            .append(getUpTime())
            .append(" (<i>rnd delay is ")
            .append(DELAY)
            .append(" : ")
            .append(String.format("%.02f", (float) (getAtomicTime() - START_STAMP) / TimeUnit.MINUTES.toMillis(DELAY)))
            .append(" delays)</i>")
            .append(".<br> Состояние памяти (МБ): <font color=\"#82caff\">")
            .append(getMemoryInfo())
            .append("<details><summary> disk usage by program: </summary>")
            .append(filesSizeFuture.get(DELAY - 10, TimeUnit.SECONDS)).append("</details></font><br>")
            .toString();
    }
    
    private static String getSeparator() {
        return System.getProperty(PropertiesNames.PRSYS_SEPARATOR);
    }
    
    /**
     Рассчитывает {@link #DELAY}, на всё время запуска приложения.
     
     @return {@link #DELAY}
     */
    private static long getDelay() {
        long delay = new SecureRandom().nextInt((int) MY_AGE);
        if (delay < MIN_DELAY) {
            delay = MIN_DELAY;
        }
        if (thisPC().toLowerCase().contains(HOSTNAME_DO213) || thisPC().toLowerCase().contains(AppInfoOnLoad.HOSTNAME_HOME)) {
            return MIN_DELAY;
        }
        else {
            return delay;
        }
    }
}
