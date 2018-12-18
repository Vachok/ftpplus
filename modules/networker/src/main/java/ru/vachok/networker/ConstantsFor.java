package ru.vachok.networker;


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
import ru.vachok.networker.net.NetScannerSvc;
import ru.vachok.networker.services.MyCalen;
import ru.vachok.networker.services.PassGenerator;
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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.TextStyle;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;


/**
 Константы, используемые в приложении
 <p>
 @since 12.08.2018 (16:26) */
public enum ConstantsFor {
    ;

    /**
     Число, для Secure Random
     */
    public static final long MY_AGE = (long) Year.now().getValue() - 1984;

    /**
     Первоначальная задержка {@link ThreadConfig#threadPoolTaskScheduler()}
     */
    public static final long INIT_DELAY = TimeUnit.MINUTES.toSeconds(MY_AGE);

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
     * {@link Model} имя атрибута
     */
    public static final String USERS = "users";

    /**
     * {@link Model} имя атрибута
     */
    public static final String TITLE = "title";

    /**
     * {@link ServiceInfoCtrl#closeApp()}
     */
    public static final int USER_EXIT = 222;

    /**
     {@link Visitor#getVisitsMap()}
     */
    public static final Map<Long, HttpServletRequest> VISITS_MAP = new ConcurrentHashMap<>();

    /**
     * {@link ru.vachok.networker.ad.ADSrv#getDetails(String)}, {@link PCUserResolver#getResolvedName()},
     * {@link AppComponents#getCompUsersMap()}, {@link NetScannerSvc#getPCsAsync()}
     */
    public static final ConcurrentMap<String, File> COMPNAME_USERS_MAP = new ConcurrentHashMap<>();

    /**
     * {@link ru.vachok.networker.mailserver.ExCTRL#uplFile(MultipartFile, Model)}, {@link ExSRV#getOFields()},
     */
    public static final ConcurrentMap<Integer, MailRule> MAIL_RULES = new ConcurrentHashMap<>();

    public static final String ALERT_AD_FOTO =
        "<p>Для корректной работы, вам нужно положить фото юзеров <a href=\"file://srv-mail3.eatmeat.ru/c$/newmailboxes/fotoraw/\" target=\"_blank\">\\\\srv-mail3.eatmeat" +
            ".ru\\c$\\newmailboxes\\fotoraw\\</a>\n";

    public static final String NO0027 = "10.200.213.85";

    public static final String DB_PREFIX = "u0466446_";

    public static final File SSH_ERR = new File("ssh_err.txt");

    public static final String SRV_NAT = "192.168.13.30";

    public static final int NOPC = 50;

    public static final int PPPC = 70;

    public static final String SRV_GIT = "192.168.13.42";

    public static final int TIMEOUT_5 = 5000;

    public static final long DELAY = getDelay();

    public static final int DOPC = 250;

    public static final int APC = 350;

    public static final int TDPC = 15;

    public static final int TIMEOUT_650 = 650;

    public static final Long CACHE_TIME_MS = TimeUnit.MINUTES.toMillis(10);

    public static final float ONE_HOUR_IN_MIN = 60f;

    public static final int KBYTE = 1024;

    public static final long START_STAMP = System.currentTimeMillis();

    public static final String APP_NAME = "ru_vachok_networker-";

    public static final int ONE_DAY_HOURS = 24;

    public static final long ONE_WEEK_MILLIS = TimeUnit.HOURS.toMillis(ONE_DAY_HOURS * 7);

    public static final long GBYTE = 1073741824;

    private static final Properties PROPS = takePr();

    public static final int ONE_MONTH_DAYS = 30;

    public static Properties getPROPS() {
        return PROPS;
    }

    private static long getDelay() {
        long delay = new SecureRandom().nextInt((int) MY_AGE);
        if (delay < 14) {
            delay = 14;
        }
        return delay;
    }

    public static final int TOTAL_PC = Integer.parseInt(PROPS.getOrDefault("totpc", "316").toString());

    public static final PassGenerator passGenerator = new PassGenerator();

    public static final int LISTEN_PORT = Integer.parseInt(PROPS.getOrDefault("lport", "9990").toString());

    public static final int ONE_YEAR = 365;

    public static final int NETSCAN_DELAY = (int) ConstantsFor.DELAY;

    public static final String IT_FOLDER = "\\\\srv-fs.eatmeat.ru\\it$$";

    public static boolean isPingOK() {
        try {
            return InetAddress.getByName("srv-git.eatmeat.ru").isReachable(500);
        } catch (IOException e) {
            LoggerFactory.getLogger(ConstantsFor.class.getSimpleName()).error(e.getMessage(), e);
            return false;
        }
    }

    public static long getBuildStamp() {
        try {
            String hostName = InetAddress.getLocalHost().getHostName();
            if (hostName.equalsIgnoreCase("home") || hostName.toLowerCase().contains("no0027")) {
                PROPS.setProperty("build", System.currentTimeMillis() + "");
                return System.currentTimeMillis();
            } else {
                return Long.parseLong(PROPS.getProperty("build", "1"));
            }
        } catch (UnknownHostException e) {
            return 1L;
        }
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

    public static String getUpTime() {
        return "(" + (+(float) (System.currentTimeMillis() - ConstantsFor.START_STAMP) / 1000 / 60 / 60) + " hrs ago)";
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

    public static String thisPC() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException | ExceptionInInitializerError e) {
            return e.getMessage();
        } catch (NullPointerException n) {
            return "pc";
        }
    }

    private static long atomicTime;

    public static long getAtomicTime() {
        ConstantsFor.atomicTime = new TimeChecker().call().getReturnTime();
        return atomicTime;
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

    private static Properties takePr() {
        InitProperties initProperties;
        try {
            initProperties = new DBRegProperties(ConstantsFor.APP_NAME + ConstantsFor.class.getSimpleName());
            AppComponents.getLogger().info("ConstantsFor.takePr");
            return initProperties.getProps();
        } catch (Exception e) {
            initProperties = new FileProps(ConstantsFor.APP_NAME + ConstantsFor.class.getSimpleName());
            String msg = "Taking File properties:" + "\n" + e.getMessage();
            AppComponents.getLogger().warn(msg);
            return initProperties.getProps();
        }
    }

    public static String toStringS() {
        final StringBuilder sb = new StringBuilder("ConstantsFor{");
        sb.append("ALERT_AD_FOTO='").append(ALERT_AD_FOTO).append('\n');
        sb.append(", APC=").append(APC);
        sb.append(", APP_NAME='").append(APP_NAME).append('\n');
        sb.append(", atomicTime=").append(atomicTime);
        sb.append(", CACHE_TIME_MS=").append(CACHE_TIME_MS);
        sb.append(", COMPNAME_USERS_MAP=").append(COMPNAME_USERS_MAP);
        sb.append(", DB_PREFIX='").append(DB_PREFIX).append('\n');
        sb.append(", DELAY=").append(DELAY);
        sb.append(", DOPC=").append(DOPC);
        sb.append(", FOOTER='").append(FOOTER).append('\n');
        sb.append(", GBYTE=").append(GBYTE);
        sb.append(", INIT_DELAY=").append(INIT_DELAY);
        sb.append(", IT_FOLDER='").append(IT_FOLDER).append('\n');
        sb.append(", KBYTE=").append(KBYTE);
        sb.append(", LISTEN_PORT=").append(LISTEN_PORT);
        sb.append(", MAIL_RULES=").append(MAIL_RULES);
        sb.append(", MBYTE=").append(MBYTE);
        sb.append(", MY_AGE=").append(MY_AGE);
        sb.append(", NETSCAN_DELAY=").append(NETSCAN_DELAY);
        sb.append(", NO0027='").append(NO0027).append('\n');
        sb.append(", NOPC=").append(NOPC);
        sb.append(", ONE_DAY_HOURS=").append(ONE_DAY_HOURS);
        sb.append(", ONE_HOUR_IN_MIN=").append(ONE_HOUR_IN_MIN);
        sb.append(", ONE_MONTH_DAYS=").append(ONE_MONTH_DAYS);
        sb.append(", ONE_WEEK_MILLIS=").append(ONE_WEEK_MILLIS);
        sb.append(", ONE_YEAR=").append(ONE_YEAR);
        sb.append(", passGenerator=").append(passGenerator);
        sb.append(", PC_U_MAP=").append(PC_U_MAP);
        sb.append(", PPPC=").append(PPPC);
        sb.append(", PROPS=").append(PROPS);
        sb.append(", SRV_GIT='").append(SRV_GIT).append('\n');
        sb.append(", SRV_NAT='").append(SRV_NAT).append('\n');
        sb.append(", SSH_ERR=").append(SSH_ERR);
        sb.append(", START_STAMP=").append(START_STAMP);
        sb.append(", TDPC=").append(TDPC);
        sb.append(", TIMEOUT_5=").append(TIMEOUT_5);
        sb.append(", TIMEOUT_650=").append(TIMEOUT_650);
        sb.append(", TITLE='").append(TITLE).append('\n');
        sb.append(", TOTAL_PC=").append(TOTAL_PC);
        sb.append(", USER_EXIT=").append(USER_EXIT);
        sb.append(", USERS='").append(USERS).append('\n');
        sb.append(", VISITS_MAP=").append(VISITS_MAP);
        sb.append('}');
        return sb.toString();
    }}
