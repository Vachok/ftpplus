package ru.vachok.networker.net.enums;


import org.springframework.ui.Model;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
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
@SuppressWarnings("NonFinalFieldInEnum")
public enum ConstantsNet { ;

    public static final boolean IS_RUPS = ConstantsFor.thisPC().toLowerCase().contains("rups");

    public static final String STR_CONNECTION = "connection";

    /**
     Имя {@link Model} атрибута.
     */
    public static final String ATT_NETSCAN = "netscan";

    public static final String PINGRESULT_LOG = "pingresult";

    public static final String BEANNAME_LASTNETSCAN = "lastnetscan";

    public static final String HTTP_LOCALHOST_8880_NETSCAN = "http://localhost:8880/netscan";

    public static final String DB_FIELD_WHENQUERIED = "whenQueried";

    public static final int TDPC = 15;

    /**
     {@link ConstantsFor#DBPREFIX} + velkom
     */
    public static final String DB_NAME = ConstantsFor.DBBASENAME_U0466446_VELKOM;

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
    public static final String FILENAME_NEWLAN210 = "lan_200210.txt";

    public static final String FILENAME_NEWLAN200210 = "lan_210220.txt";
    /**
     Название файла старой подсети 192.168.х.х
     */
    public static final String FILENAME_OLDLANTXT0 = "lan_old0.txt";

    public static final String FILENAME_OLDLANTXT1 = "lan_old1.txt";
    
    public static final int MAX_IN_ONE_VLAN = 255;

    public static final int IPS_IN_VELKOM_VLAN = Integer
        .parseInt(AppComponents.getOrSetProps().getProperty(ConstantsFor.PR_VLANNUM , "69")) * MAX_IN_ONE_VLAN;

    public static final int TIMEOUT240 = 240;

    public static final int DOTDPC = 50;

    public static final int NOTDPC = 50;

    public static final String FILENAME_SERVTXT = "srv.txt";

    public static final String FILENAME_SERVTXT_11SRVTXT = "lan_11v" + FILENAME_SERVTXT;

    public static final String FILENAME_SERVTXT_21SRVTXT = "lan_21v" + FILENAME_SERVTXT;

    public static final String FILENAME_SERVTXT_31SRVTXT = "lan_31v" + FILENAME_SERVTXT;

    public static final String FILENAME_SERVTXT_41SRVTXT = "lan_41v" + FILENAME_SERVTXT;

    public static final String FILENAME_PINGTV = "ping.tv";

    private static final String[] PC_PREFIXES = {"do", "pp", "td", "no", "a", "dotd", "notd"};

    private static final ConcurrentMap<String, File> COMPNAME_USERS_MAP = new ConcurrentHashMap<>();

    private static final ConcurrentMap<String, String> PC_U_MAP = new ConcurrentHashMap<>();

    private static final Properties LOC_PROPS = AppComponents.getOrSetProps();
    
    private static final Map<String, Long> SSH_CHECKER_MAP = new ConcurrentHashMap<>();

    private static final BlockingDeque<String> ALL_DEVICES = new LinkedBlockingDeque<>(IPS_IN_VELKOM_VLAN);

    public static final String HOSTNAMEPATT_HOME = "home";
    
    public static final String COM_INITPF = "sudo /etc/initpf.fw;sudo squid -k reconfigure && exit";
    
    public static final String COM_CAT24HRSLIST = "cat /etc/pf/24hrs && exit";

    /**
     new {@link HashSet}

     @see ru.vachok.networker.net.NetScannerSvc#getPCNamesPref(String)
     @see ru.vachok.networker.net.NetScanCtr#scanIt(HttpServletRequest, Model, Date)
     */
    private static Set<String> pcNames = new HashSet<>(Integer.parseInt(LOC_PROPS.getOrDefault(ConstantsFor.PR_TOTPC , "42").toString()));

    private static MessageToUser messageToUser = new MessageLocal(ConstantsNet.class.getSimpleName());

    private static String sshMapStr = "SSH Temp list is empty";


    public static Set<String> getPcNames() {
        return pcNames;
    }


    public static void setPcNames(Set<String> pcNames) {
        ConstantsNet.pcNames = pcNames;
    }

    /**
     Префиксы имён ПК Велком.

     @return {@link #PC_PREFIXES}
     */
    public static String[] getPcPrefixes() {
        return PC_PREFIXES;
    }


    /**
     {@link NetScannerSvc#getPCsAsync()}

     @return {@link #COMPNAME_USERS_MAP}
     */
    public static ConcurrentMap<String, File> getPCnameUsersMap() {
        return COMPNAME_USERS_MAP;
    }


    /**
     {@link ADSrv#recToDB(String, String)}

     @return {@link #PC_U_MAP}
     */
    public static ConcurrentMap<String, String> getPcUMap() {
        return PC_U_MAP;
    }


    /**
     Все возможные IP из диапазонов {@link DiapazonedScan}

     @return {@link #ALL_DEVICES}
     */
    public static BlockingDeque<String> getAllDevices() {
        AppComponents.getOrSetProps().setProperty(ConstantsFor.PR_VLANNUM , String.valueOf((IPS_IN_VELKOM_VLAN / MAX_IN_ONE_VLAN)));
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
    } }
