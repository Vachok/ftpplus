package ru.vachok.networker.net;


import org.slf4j.Logger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.ad.ADComputer;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.config.ThreadConfig;

import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 Константы пакета
 <p>

 @since 25.01.2019 (10:30) */
public enum ConstantsNet {;

    public static final String DB_FIELD_WHENQUERIED = "whenQueried";

    static final int TDPC = 15;

    /**
     {@link ConstantsFor#DB_PREFIX} + velkom
     */
    static final String DB_NAME = ConstantsFor.U_0466446_VELKOM;

    static final int APC = 350;

    static final int DOPC = 250;

    static final Properties LOC_PROPS = ConstantsFor.getProps();

    /**
     new {@link HashSet}
     */
    static final Set<String> PC_NAMES = new HashSet<>(Integer.parseInt(LOC_PROPS.getOrDefault(ConstantsFor.PR_TOTPC, "318").toString()));

    static final int PPPC = 70;

    /**
     Кол-во ноутов NO
     */
    static final int NOPC = 50;

    /**
     Префиксы имён ПК Велком.
     */
    static final String[] PC_PREFIXES = {"do", "pp", "td", "no", "a"};

    /**
     <i>Boiler Plate</i>
     */
    static final String WRITE_DB = ".writeDB";

    static final String ONLINES_CHECK = ".onLinesCheck";

    static final String GET_INFO_FROM_DB = ".getInfoFromDB";

    static final String ONLINE_NOW = "OnlineNow";

    static final int N_THREADS = 333;

    /**
     {@link AppComponents#getLogger()}
     */
    static final Logger LOGGER = AppComponents.getLogger();

    /**
     {@link AppComponents#adComputers()}
     */
    static final List<ADComputer> AD_COMPUTERS = AppComponents.adComputers();

    /**
     <i>Boiler Plate</i>
     */
    static final String STR_NETSCANNERSVC = "netScannerSvc";

    /**
     {@link ThreadConfig#threadPoolTaskExecutor()}
     */
    static final ThreadPoolTaskExecutor TASK_EXECUTOR = new ThreadConfig().threadPoolTaskExecutor();}
