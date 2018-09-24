package ru.vachok.networker;


import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import ru.vachok.networker.config.AppCtx;
import ru.vachok.networker.logic.PassGenerator;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;


/**
 <h1>Константы</h1>
 * @since 12.08.2018 (16:26)
 */
public enum ConstantsFor {
    ;

    /**
     * <b>1 мегабайт в байтах</b>
     */
    public static final int MBYTE = 1024 * 1024;

    private static boolean pingOK = true;

    public static boolean isPingOK() {
        try{
            pingOK = InetAddress.getByName("srv-git.eatmeat.ru").isReachable(500);
        }
        catch(IOException e){
            LoggerFactory.getLogger(ConstantsFor.class.getSimpleName()).error(e.getMessage(), e);
        }
        return pingOK;
    }

    public static final AutowireCapableBeanFactory BEAN_FACTORY = new AppCtx().getAutowireCapableBeanFactory();

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

    public static final long DELAY = new SecureRandom().nextInt(600);

    public static final int DOPC = 250;

    public static final int APC = 350;

    public static final int TDPC = 15;

    public static final int TIMEOUT_650 = 650;

    public static final long INIT_DELAY = 35;

    public static final Long CACHE_TIME_MS = TimeUnit.MINUTES.toMillis(10);

    public static final float ONE_HOUR_IN_MIN = 60f;

    public static int KBYTE = 1024;

    public static final long START_STAMP = System.currentTimeMillis();

    public static final String APP_NAME = "ru_vachok_networker-";

    public static final String THIS_PC_NAME = thisPC();

    public static final String SCREEN_PATH = "C:\\Users\\ikudryashov\\Documents\\ShareX\\Screenshots\\";

    public static PassGenerator passGenerator = new PassGenerator();

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

    /*PS Methods*/
    public static String getUserPC(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

    private static String thisPC() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "No hostname!";
        }
    }
}
