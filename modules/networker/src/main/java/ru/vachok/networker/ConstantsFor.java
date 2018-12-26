package ru.vachok.networker;


import org.apache.commons.net.ntp.TimeInfo;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.messenger.email.ESender;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.FileProps;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.networker.ad.PCUserResolver;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.controller.ServiceInfoCtrl;
import ru.vachok.networker.mailserver.ExSRV;
import ru.vachok.networker.mailserver.MailRule;
import ru.vachok.networker.net.DiapazonedScan;
import ru.vachok.networker.net.NetScannerSvc;
import ru.vachok.networker.services.MyCalen;
import ru.vachok.networker.services.TimeChecker;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.*;
import java.time.format.TextStyle;
import java.util.*;
import java.util.concurrent.*;


/**
 Константы, используемые в приложении
 <p>

 @since 12.08.2018 (16:26) */
public enum ConstantsFor {
    ;

    public static final String DOS_ARCHIVE = "dos:archive";

    /**
     Имя ПК no0027
     */
    public static final String NO0027 = "no0027";

    /**
     Число, для Secure Random
     */
    public static final long MY_AGE = (long) Year.now().getValue() - 1984;

    public static final String EXIT_APP_RUN = "ExitApp.run";

    public static final String OLD_LAN_TXT = "old_lan.txt";

    public static final String AVAILABLE_LAST_TXT = "available_last.txt";

    /**
     Строка из Live Template soutm
     */
    public static final String STR_SEC_SPEND = " sec spend";

    /**
     Список девайсов и адресов.

     @see DiapazonedScan
     */


    /**
     {@link ru.vachok.networker.ad.ADSrv#getDetails(String)}, {@link PCUserResolver#getResolvedName()}, {@link AppComponents#getCompUsersMap()}, {@link NetScannerSvc#getPCsAsync()}
     */
    public static final ConcurrentMap<String, File> COMPNAME_USERS_MAP = new ConcurrentHashMap<>();

    /**
     <b>1 мегабайт в байтах</b>
     */
    public static final int MBYTE = 1048576;

    /**
     {@link ru.vachok.networker.ad.PCUserResolver#recToDB(String, String)}
     */
    public static final ConcurrentMap<String, String> PC_U_MAP = new ConcurrentHashMap<>();

    /**
     {@link Model} имя атрибута
     */
    public static final String FOOTER = "footer";

    /**
     Префикс имени от reg.ru
     */
    public static final String DB_PREFIX = "u0466446_";

    /**
     Первоначальная задержка {@link ThreadConfig#threadPoolTaskScheduler()}
     */
    public static final long INIT_DELAY = TimeUnit.MINUTES.toSeconds(MY_AGE);

    /**
     {@link Model} имя атрибута
     */
    public static final String USERS = "users";

    /**
     {@link Model} имя атрибута
     */
    public static final String TITLE = "title";

    /**
     {@link Visitor#getVisitsMap()}
     */
    public static final Map<Long, HttpServletRequest> VISITS_MAP = new ConcurrentHashMap<>();

    /**
     {@link ServiceInfoCtrl#closeApp()}
     */
    public static final int USER_EXIT = 222;

    /**
     IP srv-nat.eatmeat.ru
     */
    public static final String SRV_NAT = "192.168.13.30";

    /**
     IP stv-git.eatmeat.ru
     */
    public static final String SRV_GIT = "192.168.13.42";

    /**
     * {@link #getDelay()}
     */
    public static final long DELAY = getDelay();

    /**
     * Кол-во минут в часе
     */
    public static final float ONE_HOUR_IN_MIN = 60f;

    /**
     * Кол-во байт в килобайте
     */
    public static final int KBYTE = 1024;

    /**
     * Timestamp запуска.
     */
    public static final long START_STAMP = System.currentTimeMillis();

    public static final int TIMEOUT_650 = 650;

    /**
     Имя для БД с настройками
     */
    public static final String APP_NAME = "ru_vachok_networker-";

    /**
     * Кол-во часов в сутках
     */
    public static final int ONE_DAY_HOURS = 24;

    /**
     * Кол-во миллисек. в 1 неделе
     */
    public static final long ONE_WEEK_MILLIS = TimeUnit.HOURS.toMillis(ONE_DAY_HOURS * 7);

    /**
     * Кол-во байт в гигабайте
     */
    public static final long GBYTE = 1073741824;

    /**
     {@link ru.vachok.networker.mailserver.ExCTRL#uplFile(MultipartFile, Model)}, {@link ExSRV#getOFields()},
     */
    public static final ConcurrentMap<Integer, MailRule> MAIL_RULES = new ConcurrentHashMap<>();

    /**
     Кол-во дней в месяце
     */
    public static final int ONE_MONTH_DAYS = 30;

    /**
     * Кол-во дней в году
     */
    public static final int ONE_YEAR = 365;

    /**
     * Папка it$$
     */
    public static final String IT_FOLDER = "\\\\srv-fs.eatmeat.ru\\it$$";

    /**
     new {@link Properties}
     */
    private static final Properties PROPS = new Properties();

    /**
     Число IP по кол-ву VLANs
     */
    public static final int IPS_IN_VELKOM_VLAN = getIPs();

    public static final BlockingQueue<String> ALL_DEVICES = new ArrayBlockingQueue<>(IPS_IN_VELKOM_VLAN);
    /**
     Порт для {@link ru.vachok.networker.net.MyServer}
     */
    public static final int LISTEN_PORT = Integer.parseInt(PROPS.getOrDefault("lport", "9990").toString());

    public static final String EATMEAT_RU = ".eatmeat.ru";

    public static final String DELETED = " DELETED";

    /**
     {@link #getAtomicTime()}
     */
    @SuppressWarnings("NonFinalFieldInEnum")
    private static long atomicTime;

    /**
     @return 192.168.13.42 online or offline
     */
    public static boolean isPingOK() {
        try {
            return InetAddress.getByName("srv-git.eatmeat.ru").isReachable(500);
        } catch (IOException e) {
            LoggerFactory.getLogger(ConstantsFor.class.getSimpleName()).error(e.getMessage(), e);
            return false;
        }
    }

    public static Properties getProps() {
        return PROPS;
    }

    /**
     @return
     */
    public static long getBuildStamp() {
        try {
            String hostName = InetAddress.getLocalHost().getHostName();
            if (hostName.equalsIgnoreCase("home") || hostName.toLowerCase().contains(NO0027)) {
                PROPS.setProperty("build", System.currentTimeMillis() + "");
                return System.currentTimeMillis();
            } else {
                return Long.parseLong(PROPS.getProperty("build", "1"));
            }
        } catch (UnknownHostException e) {
            return 1L;
        }
    }

    public static long getAtomicTime() {
        TimeChecker t = new TimeChecker();
        TimeInfo call = t.call();
        call.computeDetails();
        ConstantsFor.atomicTime = call.getReturnTime();
        return atomicTime;
    }

    public static String showMem() {
        String msg = (float) Runtime.getRuntime().totalMemory() / ConstantsFor.MBYTE + " now totalMemory, " +
            (float) Runtime.getRuntime().freeMemory() / ConstantsFor.MBYTE + " now freeMemory, " +
            (float) Runtime.getRuntime().maxMemory() / ConstantsFor.MBYTE + " now maxMemory.";
        AppComponents.getLogger().warn(msg);
        return msg;
    }

    public static String percToEnd() {
        StringBuilder stringBuilder = new StringBuilder();
        LocalTime endDay = LocalTime.parse("17:30");
        final int secDayEnd = endDay.toSecondOfDay();
        LocalTime startDay = LocalTime.parse("08:30");
        final int startSec = startDay.toSecondOfDay();
        final int allDaySec = secDayEnd - startSec;
        LocalTime localTime = endDay.minusHours(LocalTime.now().getHour());
        localTime = localTime.minusMinutes(LocalTime.now().getMinute());
        localTime = localTime.minusSeconds(LocalTime.now().getSecond());
        boolean workHours = LocalTime.now().isAfter(startDay) && LocalTime.now().isBefore(endDay);
        if (workHours) {
            int toEndDaySec = localTime.toSecondOfDay();
            int diffSec = allDaySec - toEndDaySec;
            float percDay = ((float) toEndDaySec / (((float) allDaySec) / 100));
            stringBuilder
                .append("Работаем ")
                .append(TimeUnit.SECONDS.toMinutes(diffSec));
            stringBuilder
                .append("(мин.). Ещё ")
                .append(percDay)
                .append(" % или ");
        } else {
            stringBuilder.append("<b> GO HOME! </b><br>");
        }
        stringBuilder.append(localTime.toString());
        return stringBuilder.toString();
    }

    public static void saveProps(Properties propsToSave) {
        InitProperties initProperties;
        try {
            initProperties = new DBRegProperties(ConstantsFor.APP_NAME + ConstantsFor.class.getSimpleName());
        } catch (Exception e) {
            initProperties = new FileProps(ConstantsFor.APP_NAME + ConstantsFor.class.getSimpleName());
        }
        initProperties.delProps();
        initProperties.setProps(propsToSave);
        initProperties = new FileProps(ConstantsFor.APP_NAME + ConstantsFor.class.getSimpleName());
        initProperties.setProps(propsToSave);
    }

    public static String thisPC() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException | ExceptionInInitializerError e) {
            return e.getMessage();
        } catch (NullPointerException n) {
            return "pc";
        }
    }

    static void takePr() {
        InitProperties initProperties;
        try {
            initProperties = new DBRegProperties(ConstantsFor.APP_NAME + ConstantsFor.class.getSimpleName());
            String msg = "Taking DB properties:" + "\n" + initProperties.getClass().getSimpleName();
            AppComponents.getLogger().info(msg);
            PROPS.putAll(initProperties.getProps());
        } catch (Exception e) {
            initProperties = new FileProps(ConstantsFor.APP_NAME + ConstantsFor.class.getSimpleName());
            String msg = "Taking File properties:" + "\n" + e.getMessage();
            AppComponents.getLogger().warn(msg);
            PROPS.putAll(initProperties.getProps());
            AppComponents.getLogger().warn(msg);
        }
    }

    /**
     @param request для получения IP
     @return boolean авторизован или нет
     */
    public static boolean getPcAuth(HttpServletRequest request) {
        return request.getRemoteAddr().toLowerCase().contains("0:0:0:0") ||
            request.getRemoteAddr().contains("10.200.213") ||
            request.getRemoteAddr().contains("10.10.111") ||
            request.getRemoteAddr().contains("172.16.200");
    }

    public static String getUserPC(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

    /**
     @return {@link #IPS_IN_VELKOM_VLAN}
     */
    private static int getIPs() {
        int vlansNum = Integer.parseInt(PROPS.getProperty("vlans", "22"));
        int qSize = vlansNum * 255;
        PROPS.setProperty("qsize", qSize + "");
        return qSize;
    }

    static String checkDay() {
        Date dateStart = MyCalen.getNextDayofWeek(10, 0, DayOfWeek.MONDAY);
        String msg = dateStart + " - date to TRUNCATE , " +
            LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault()) + "\n" +
            ONE_WEEK_MILLIS + " ms delay.\n";
        ThreadConfig t = new ThreadConfig();
        ThreadPoolTaskScheduler threadPoolTaskScheduler = t.threadPoolTaskScheduler();
        threadPoolTaskScheduler.scheduleWithFixedDelay(ConstantsFor::trunkTableUsers, dateStart, ONE_WEEK_MILLIS);
        return msg;
    }

    private static long getDelay() {
        long delay = new SecureRandom().nextInt((int) MY_AGE);
        if (delay < 14) {
            delay = 14;
        }
        return delay;
    }

    private static void trunkTableUsers() {
        MessageToUser messageToUser = new ESender("143500@gmail.com");
        try (Connection c = new RegRuMysql().getDefaultConnection(DB_PREFIX + "velkom");
             PreparedStatement preparedStatement = c.prepareStatement("TRUNCATE TABLE pcuserauto")) {
            preparedStatement.executeUpdate();
            messageToUser.infoNoTitles("TRUNCATE true\n" + ConstantsFor.getUpTime() + " uptime.");
        } catch (SQLException e) {
            messageToUser.infoNoTitles("TRUNCATE false\n" + ConstantsFor.getUpTime() + " uptime.");
        }
    }

    public static String getUpTime() {
        return "(" + (+(float) (System.currentTimeMillis() - ConstantsFor.START_STAMP) / 1000 / 60 / 60) + " hrs ago)";
    }}
