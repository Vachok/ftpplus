package ru.vachok.networker;


import org.slf4j.LoggerFactory;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.FileProps;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.mailserver.MailRule;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Year;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;


/**
 <b>Константы</b>

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

    public static final long GBYTE = 1073741824;

    public static final ConcurrentMap<String, String> PC_U_MAP = new ConcurrentHashMap<>();

    public static final String FOOTER = "footer";

    public static final String USERS = "users";

    public static final String TITLE = "title";

    public static final int USER_EXIT = 222;

    /**
     {@link Visitor#getVisitsMap()}
     */
    public static final Map<Long, HttpServletRequest> VISITS_MAP = new ConcurrentHashMap<>();

    public static final ConcurrentMap<String, File> COMPNAME_USERS_MAP = new ConcurrentHashMap<>();

    public static final ConcurrentMap<Integer, MailRule> MAIL_RULES = new ConcurrentHashMap<>();

    public static final String ALERT_AD_FOTO =
        "<p>Для корректной работы, вам нужно положить фото юзеров <a href=\"file://srv-mail3.eatmeat.ru/c$/newmailboxes/fotoraw/\" target=\"_blank\">\\\\srv-mail3.eatmeat" +
            ".ru\\c$\\newmailboxes\\fotoraw\\</a>\n";

    public static final String NO0027 = "10.200.213.85";

    public static final String DB_PREFIX = "u0466446_";

    public static final File SSH_ERR = new File("ssh_err.txt");

    public static final File SSH_OUT = new File("ssh_out.txt");

    public static final int TIMEOUT_2 = 2000;

    public static final String SRV_NAT = "192.168.13.30";

    public static final int NOPC = 50;

    public static final int PPPC = 70;

    public static final String SRV_GIT = "192.168.13.42";

    public static final int TIMEOUT_5 = 5000;

    public static final long DELAY = new SecureRandom().nextInt((int) MY_AGE);

    public static final int DOPC = 250;

    public static final int APC = 350;

    public static final int TDPC = 15;

    public static final int TIMEOUT_650 = 650;

    public static final Long CACHE_TIME_MS = TimeUnit.MINUTES.toMillis(10);

    public static final float ONE_HOUR_IN_MIN = 60f;

    public static final int KBYTE = 1024;

    public static final long START_STAMP = System.currentTimeMillis();

    public static final String APP_NAME = "ru_vachok_networker-";

    /*Fields*/
    private static final Properties PROPS = takePr();

    public static Properties getPROPS() {
        return PROPS;
    }

    public static final int TEST_EXIT = 333;

    public static final int BAD_STATS = 666;

    public static final int ONE_DAY = 24;

    public static final int TOTAL_PC = Integer.parseInt(PROPS.getOrDefault("totpc", "316").toString());

    public static final PassGenerator passGenerator = new PassGenerator();

    public static final int LISTEN_PORT = Integer.parseInt(PROPS.getOrDefault("lport", "9990").toString());

    public static final int ONE_YEAR = 365;

    public static final int NETSCAN_DELAY = (int) ConstantsFor.DELAY;

    public static final String COMMON_FOLDER = "\\\\srv-fs.eatmeat.ru\\common_new";

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
        }
    }

    private static long atomicTime;

    public static long getAtomicTime() {
        ConstantsFor.atomicTime = new TimeChecker().call().getReturnTime();
        return atomicTime;
    }

    static void setAtomicTime(long returnTime) {
        ConstantsFor.atomicTime = returnTime;
    }

    static boolean checkDay() {
        String msg = LocalDate.now().getDayOfWeek().getValue() + " - day of week\n" +
            LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault());
        if (msg.toLowerCase().contains("понедель") && LocalTime.now().isBefore(LocalTime.of(13, 0))) {
            try (Connection c = new RegRuMysql().getDefaultConnection(DB_PREFIX + "velkom");
                 PreparedStatement preparedStatement = c.prepareStatement("TRUNCATE TABLE pcuserauto")) {
                preparedStatement.executeUpdate();
                return true;
            } catch (SQLException e) {
                return false;
            }
        }
        return false;
    }

    private static Properties takePr() {
        InitProperties initProperties = new DBRegProperties(ConstantsFor.APP_NAME + ConstantsFor.class.getSimpleName());
        try {
            AppComponents.getLogger().info("ConstantsFor.takePr");
            return initProperties.getProps();
        } catch (Exception e) {
            initProperties = new FileProps(ConstantsFor.APP_NAME + ConstantsFor.class.getSimpleName());
            String msg = "Taking File properties:" + "\n" + e.getMessage();
            AppComponents.getLogger().warn(msg);
            return initProperties.getProps();
        }
    }
}
