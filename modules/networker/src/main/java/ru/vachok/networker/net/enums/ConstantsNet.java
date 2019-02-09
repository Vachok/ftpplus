package ru.vachok.networker.net.enums;


import org.slf4j.Logger;
import org.springframework.ui.Model;
import ru.vachok.messenger.MessageCons;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.ad.ADComputer;
import ru.vachok.networker.ad.ADSrv;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.NetScannerSvc;
import ru.vachok.networker.net.TraceRoute;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.*;
import java.util.concurrent.*;

/**
 Константы пакета
 <p>

 @since 25.01.2019 (10:30) */
public enum ConstantsNet {;

    public static final String PTV2_EATMEAT_RU = "ptv2.eatmeat.ru";

    public static final String PTV1_EATMEAT_RU = "ptv1.eatmeat.ru";

    public static final String RECONNECT_TO_DB = "reconnectToDB";

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
     <p>

     @see ru.vachok.networker.net.NetScannerSvc#getPCNamesPref(String)
     @see ru.vachok.networker.net.NetScanCtr#scanIt(HttpServletRequest, Model, Date)
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
     Название property
     */
    public static final String PR_LASTSCAN = "lastscan";

    /**
     {@link NetScannerSvc#getPCsAsync()}

     @see ADSrv#getDetails(java.lang.String)
     */
    public static final ConcurrentMap<String, File> COMPNAME_USERS_MAP = new ConcurrentHashMap<>();

    /**
     {@link ru.vachok.networker.ad.PCUserResolver#recToDB(String, String)}
     */
    public static final ConcurrentMap<String, String> PC_U_MAP = new ConcurrentHashMap<>();

    public static final String STR_COMPNAME_USERS_MAP_SIZE = " COMPNAME_USERS_MAP size";

    /**
     Выгрузка из БД {@link ConstantsFor#DB_PREFIX} {@code velkom} - pcuserauto
     */
    public static final String VELKOM_PCUSERAUTO_TXT = "velkom_pcuserauto.txt";

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

    public static String getProvider() {
        Future<String> submit = ThreadConfig.getI().threadPoolTaskExecutor().submit(new TraceRoute());
        try {
            String s = submit.get();
            FileSystemWorker.recFile("trace", s);
            return s;
        } catch (InterruptedException | ExecutionException e) {
            new MessageCons().errorAlert("ConstantsNet", "getProvider", TForms.from(e));
            Thread.currentThread().interrupt();
            return e.getMessage();
        }
    }}
