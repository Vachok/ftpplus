package ru.vachok.networker;


import org.slf4j.LoggerFactory;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.FileProps;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.logic.PassGenerator;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.time.Year;
import java.util.Properties;
import java.util.concurrent.TimeUnit;


/**
 <h2>Константы</h2>

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
    public static final long INIT_DELAY = new SecureRandom().nextInt((int) MY_AGE);

    /**
     Кол-во локальных ПК {@link ru.vachok.networker.services.NetScannerSvc}
     */
    public static final int TOTAL_PC = Integer.parseInt(PROPS.getProperty("totpc", "315"));

    /**
     <b>1 мегабайт в байтах</b>
     */
    public static final int MBYTE = 1024 * 1024;

    public static final Float NO_F_DAYS = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() -
        Long.parseLong(getTheProps().getProperty("lasts", 1515233487000L + ""))) / 60f / 24f;

    /**
     {@link InitProperties}
     */
    private static InitProperties initProperties;

    /**
     {@link Properties} приложения
     */
    public static final Properties PROPS = takePr();

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

    private static boolean pingOK = true;

    public static boolean isPingOK() {
        try {
            pingOK = InetAddress.getByName("srv-git.eatmeat.ru").isReachable(500);
        } catch (IOException e) {
            LoggerFactory.getLogger(ConstantsFor.class.getSimpleName()).error(e.getMessage(), e);
        }
        return pingOK;
    }

    public static long getBuildStamp() {
        Properties props = PROPS;
        try {
            String hostName = InetAddress.getLocalHost().getHostName();
            if (hostName.equalsIgnoreCase("home") || hostName.toLowerCase().contains("no0027")) {
                props.setProperty("build", System.currentTimeMillis() + "");
                initProperties.delProps();
                initProperties.setProps(props);
                return System.currentTimeMillis();
            } else {
                return Long.parseLong(props.getProperty("build", "1"));
            }
        } catch (UnknownHostException e) {
            return 1L;
        }
    }

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

    public static final long DELAY = new SecureRandom().nextInt(1600);

    public static final int DOPC = 250;

    public static final int APC = 350;

    public static final int TDPC = 15;

    public static final int TIMEOUT_650 = 650;

    public static void saveProps() {
        initProperties.delProps();
        initProperties.setProps(PROPS);
        initProperties = new FileProps(ConstantsFor.APP_NAME + ConstantsFor.class.getSimpleName());
        initProperties.setProps(PROPS);
    }

    public static final Long CACHE_TIME_MS = TimeUnit.MINUTES.toMillis(10);

    public static final float ONE_HOUR_IN_MIN = 60f;

    public static final int KBYTE = 1024;

    public static final long START_STAMP = System.currentTimeMillis();

    public static final String APP_NAME = "ru_vachok_networker-";

    public static final String THIS_PC_NAME = thisPC();

    private static Properties takePr() {
        try {
            initProperties = new DBRegProperties(ConstantsFor.APP_NAME + ConstantsFor.class.getSimpleName());
            return initProperties.getProps();
        } catch (Exception e) {

            initProperties = new FileProps(ConstantsFor.APP_NAME + ConstantsFor.class.getSimpleName());
            return initProperties.getProps();
        }
    }

    public static final PassGenerator passGenerator = new PassGenerator();

    public static String consString() {
        return "ConstantsFor{" +
            "APC=" + APC +
            ", APP_NAME='" + APP_NAME + '\'' +
            ", passGenerator=" + passGenerator +
            ", DB_PREFIX='" + DB_PREFIX + '\'' +
            ", DELAY=" + DELAY +
            ", DOPC=" + DOPC +
            ", INIT_DELAY=" + INIT_DELAY +
            ", KBYTE=" + KBYTE +
            ", MBYTE=" + MBYTE +
            ", NO0027='" + NO0027 + '\'' +
            ", NOPC=" + NOPC +
            ", PPPC=" + PPPC +
            ", SRV_NAT='" + SRV_NAT + '\'' +
            ", SSH_ERR=" + SSH_ERR +
            ", SSH_OUT=" + SSH_OUT +
            ", START_STAMP=" + START_STAMP +
            ", TDPC=" + TDPC +
            ", THIS_PC_NAME='" + THIS_PC_NAME + '\'' +
            ", TIMEOUT_2=" + TIMEOUT_2 +
            ", TIMEOUT_650=" + TIMEOUT_650 +
            '}';
    }

    public static String getUserPC(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

    private static Properties getTheProps() {
        InitProperties initProperties = new DBRegProperties("u0466446_properties-general");
        return initProperties.getProps();
    }

    public static String thisPC() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "No hostname!";
        }
    }
}
