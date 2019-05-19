// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.enums;


import org.springframework.ui.Model;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.net.DiapazonedScan;
import ru.vachok.networker.net.NetScannerSvc;
import ru.vachok.networker.services.MessageLocal;

import java.io.File;
import java.util.Properties;
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
 
     @see AppComponents#getProps()
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

    public static final String ONLINE_NOW = "OnlineNow";

    /**
     <i>Boiler Plate</i>
     */
    public static final String BEANNAME_NETSCANNERSVC = "netScannerSvc";
    
    /**
     Название файла новой подсети 10.200.х.х
     */
    public static final String FILENAME_NEWLAN210 = "lan_200210.txt";
    
    public static final String FILENAME_NEWLAN220 = "lan_213220.txt";
    
    public static final String FILENAME_NEWLAN213 = "lan_210213.txt";
    /**
     Название файла старой подсети 192.168.х.х
     */
    public static final String FILENAME_OLDLANTXT0 = "lan_old0.txt";

    public static final String FILENAME_OLDLANTXT1 = "lan_old1.txt";

    public static final int MAX_IN_ONE_VLAN = 255;

    public static final int IPS_IN_VELKOM_VLAN = Integer
        .parseInt(AppComponents.getProps().getProperty(ConstantsFor.PR_VLANNUM, "59")) * MAX_IN_ONE_VLAN;

    public static final int TIMEOUT240 = 240;

    public static final int DOTDPC = 50;

    public static final int NOTDPC = 50;

    public static final String FILENAME_SERVTXT = "srv.txt";
    
    public static final String FILENAME_SERVTXT_10SRVTXT = "lan_11v" + FILENAME_SERVTXT;

    public static final String FILENAME_SERVTXT_21SRVTXT = "lan_21v" + FILENAME_SERVTXT;

    public static final String FILENAME_SERVTXT_31SRVTXT = "lan_31v" + FILENAME_SERVTXT;

    public static final String FILENAME_SERVTXT_41SRVTXT = "lan_41v" + FILENAME_SERVTXT;

    public static final String FILENAME_PINGTV = "ping.tv";

    private static final String[] PC_PREFIXES = {"do", "pp", "td", "no", "a", "dotd", "notd"};

    private static final ConcurrentMap<String, File> COMPNAME_USERS_MAP = new ConcurrentHashMap<>();

    private static final ConcurrentMap<String, String> PC_U_MAP = new ConcurrentHashMap<>();
    
    private static final Properties LOC_PROPS = AppComponents.getProps();

    private static final BlockingDeque<String> ALL_DEVICES = new LinkedBlockingDeque<>(IPS_IN_VELKOM_VLAN);

    public static final String COM_INITPF = "sudo /etc/initpf.fw;sudo squid -k reconfigure && exit";

    public static final String COM_CAT24HRSLIST = "cat /etc/pf/24hrs && exit";
    
    private static MessageToUser messageToUser = new MessageLocal(ConstantsNet.class.getSimpleName());
    
    public static void setSshMapStr(String sshMapStr) {
        ConstantsNet.sshMapStr = sshMapStr;
    }
    
    private static String sshMapStr = "SSH Temp list is empty";
    
    
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

    public static ConcurrentMap<String, String> getPcUMap() {
        return PC_U_MAP;
    }

    /**
     Все возможные IP из диапазонов {@link DiapazonedScan}

     @return {@link #ALL_DEVICES}
     */
    public static BlockingDeque<String> getAllDevices() {
        AppComponents.getProps().setProperty(ConstantsFor.PR_VLANNUM, String.valueOf((IPS_IN_VELKOM_VLAN / MAX_IN_ONE_VLAN)));
        return ALL_DEVICES;
    }

    public static String getSshMapStr() {
        return sshMapStr;
    } }
