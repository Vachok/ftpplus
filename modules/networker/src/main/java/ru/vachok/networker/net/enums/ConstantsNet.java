package ru.vachok.networker.net.enums;


import org.springframework.ui.Model;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.ad.ADSrv;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.net.DiapazonedScan;
import ru.vachok.networker.net.NetScannerSvc;
import ru.vachok.networker.services.MessageLocal;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingDeque;

/**
 Константы пакета
 <p>

 @since 25.01.2019 (10:30) */
public enum ConstantsNet {;

    public static final boolean IS_RUPS = ConstantsFor.thisPC().toLowerCase().contains("rups");

    public static final String STR_CONNECTION = "connection";

    /**
     Имя {@link Model} атрибута.
     */
    public static final String ATT_NETSCAN = "netscan";

    public static final String PINGRESULT_LOG = "pingresult";

    public static final String BEANNAME_LASTNETSCAN = "lastnetscan";

    public static final String ONLINEPC = "onlinepc";

    public static final String HTTP_LOCALHOST_8880_NETSCAN = "http://localhost:8880/netscan";

    public static final String DB_FIELD_WHENQUERIED = "whenQueried";

    public static final int TDPC = 15;

    /**
     {@link ConstantsFor#DBPREFIX} + velkom
     */
    public static final String DB_NAME = ConstantsFor.DBDASENAME_U0466446_VELKOM;

    public static final int APC = 350;

    public static final int DOPC = 250;

    /**
     Название настройки.
     <p>
     pingsleep. Сколько делать перерыв в пингах. В <b>миллисекундах</b>.

     @see AppComponents#getOrSetProps()
     */
    public static final String PROP_PINGSLEEP = "pingsleep";

    public static final int PPPC = 70;

    /**
     Кол-во ноутов NO
     */
    public static final int NOPC = 50;

    /**
     Название property
     */
    public static final String PR_LASTSCAN = "lastscan";

    public static final String STR_COMPNAME_USERS_MAP_SIZE = " COMPNAME_USERS_MAP size";

    /**
     Выгрузка из БД {@link ConstantsFor#DBPREFIX} {@code velkom} - pcuserauto
     */
    public static final String VELKOM_PCUSERAUTO_TXT = "velkom_pcuserauto.txt";

    public static final String ONLINE_NOW = "OnlineNow";

    /**
     <i>Boiler Plate</i>
     */
    public static final String BEANNAME_NETSCANNERSVC = "netScannerSvc";

    /**
     Файл уникальных записей из БД velkom-pcuserauto
     */
    public static final String FILENAME_PCAUTODISTXT = "pcautodis.txt";

    /**
     Название файла новой подсети 10.200.х.х
     */
    public static final String FILENAME_AVAILABLELASTTXT = "available_last.txt";

    /**
     Название файла старой подсети 192.168.х.х
     */
    public static final String FILENAME_OLDLANTXT = "old_lan.txt";

    /**
     Домен с точкой
     */
    public static final String DOMAIN_EATMEATRU = ".eatmeat.ru";

    public static final int MAX_IN_ONE_VLAN = 255;

    public static final int IPS_IN_VELKOM_VLAN = 63 * MAX_IN_ONE_VLAN;

    public static final int TIMEOUT240 = 240;

    private static final String[] PC_PREFIXES = {"do", "pp", "td", "no", "a"};

    private static final ConcurrentMap<String, File> COMPNAME_USERS_MAP = new ConcurrentHashMap<>();

    private static final ConcurrentMap<String, String> PC_U_MAP = new ConcurrentHashMap<>();

    private static final Properties LOC_PROPS = AppComponents.getOrSetProps();

    private static final ConcurrentMap<String, Long> SSH_CHECKER_MAP = new ConcurrentHashMap<>();

    private static final BlockingDeque<String> ALL_DEVICES = new LinkedBlockingDeque<>(IPS_IN_VELKOM_VLAN);

    public static final String FILENAME_SERVTXT = "srv.txt";

    /**
     new {@link HashSet}

     @see ru.vachok.networker.net.NetScannerSvc#getPCNamesPref(String)
     @see ru.vachok.networker.net.NetScanCtr#scanIt(HttpServletRequest, Model, Date)
     */
    private static Set<String> pcNames = new HashSet<>(Integer.parseInt(LOC_PROPS.getOrDefault(ConstantsFor.PR_TOTPC, "242").toString()));

    private static MessageToUser messageToUser = new MessageLocal();

    private static String sshMapStr = "SSH Temp list is empty";

    public static Set<String> getPcNames() {
        return pcNames;
    }

    public static void setPcNames(Set<String> pcNames) {
        ConstantsNet.pcNames = pcNames;
    }

// --Commented out by Inspection START (01.03.2019 16:38):
//    public static String getProvider() {
//        Future<String> submit = AppComponents.threadConfig().getTaskExecutor().submit(new TraceRoute());
//        try {
//            String s = submit.get();
//            FileSystemWorker.recFile("trace", s);
//            return s;
//        } catch (InterruptedException | ExecutionException e) {
//            messageToUser.errorAlert("ConstantsNet", "getProvider", new TForms().fromArray(e, false));
//            Thread.currentThread().interrupt();
//            return e.getMessage();
//        }
//    }
// --Commented out by Inspection STOP (01.03.2019 16:38)

    /**
     Префиксы имён ПК Велком.
     */
    public static String[] getPcPrefixes() {
        return PC_PREFIXES;
    }

    /**
     {@link NetScannerSvc#getPCsAsync()}

     @see ADSrv#getDetails(String)
     */
    public static ConcurrentMap<String, File> getCompnameUsersMap() {
        return COMPNAME_USERS_MAP;
    }

    /**
     {@link ADSrv#recToDB(String, String)}
     */
    public static ConcurrentMap<String, String> getPcUMap() {
        return PC_U_MAP;
    }

    /**
     Все возможные IP из диапазонов {@link DiapazonedScan}
     */
    public static BlockingDeque<String> getAllDevices() {
        return ALL_DEVICES;
    }

    public static void setSSHMapStr(String sshMapStr) {
        ConstantsNet.sshMapStr = sshMapStr;
    }

    public static Map<String, Long> getSshCheckerMap() {
        return SSH_CHECKER_MAP;
    }

    public static String getSshMapStr() {
        return sshMapStr;
    }}
