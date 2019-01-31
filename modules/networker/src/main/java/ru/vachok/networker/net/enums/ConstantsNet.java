package ru.vachok.networker.net.enums;


import org.slf4j.Logger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.ad.ADComputer;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.config.ThreadConfig;

import java.util.*;

/**
 Константы пакета
 <p>

 @since 25.01.2019 (10:30) */
public enum ConstantsNet {;

    public static final String STR_LASTNETSCAN = "lastnetscan";

    public static final String ONLINEPC = "onlinepc";

    public static final String HTTP_LOCALHOST_8880_NETSCAN = "http://localhost:8880/netscan";

    public static final String DB_FIELD_WHENQUERIED = "whenQueried";

    public static final int TDPC = 15;

    /**
     {@link ConstantsFor#DB_PREFIX} + velkom
     */
    public static final String DB_NAME = ConstantsFor.U_0466446_VELKOM;

    public static final int APC = 350;

    public static final int DOPC = 250;

    public static final Properties LOC_PROPS = ConstantsFor.getProps();

    /**
     new {@link HashSet}
     */
    public static final Set<String> PC_NAMES = new HashSet<>(Integer.parseInt(LOC_PROPS.getOrDefault(ConstantsFor.PR_TOTPC, "318").toString()));

    public static final int PPPC = 70;

    /**
     Кол-во ноутов NO
     */
    public static final int NOPC = 50;

    /**
     Префиксы имён ПК Велком.
     */
    public static final String[] PC_PREFIXES = {"do", "pp", "td", "no", "a"};

    /**
     <i>Boiler Plate</i>
     */
    public static final String WRITE_DB = ".writeDB";

    /**
     Название property
     */
    public static final String PR_LASTSCAN = "lastscan";

    static final String ONLINES_CHECK = ".onLinesCheck";

    static final String GET_INFO_FROM_DB = ".getInfoFromDB";

    public static final String ONLINE_NOW = "OnlineNow";

    public static final int N_THREADS = 333;

    /**
     {@link AppComponents#getLogger()}
     */
    public static final Logger LOGGER = AppComponents.getLogger();

    /**
     {@link AppComponents#adComputers()}
     */
    public static final List<ADComputer> AD_COMPUTERS = AppComponents.adComputers();

    /**
     <i>Boiler Plate</i>
     */
    public static final String STR_NETSCANNERSVC = "netScannerSvc";

    /**
     {@link ThreadConfig#threadPoolTaskExecutor()}
     */
    public static final ThreadPoolTaskExecutor TASK_EXECUTOR = new ThreadConfig().threadPoolTaskExecutor();}
