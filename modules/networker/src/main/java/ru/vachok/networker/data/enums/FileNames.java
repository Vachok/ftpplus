// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.data.enums;


import ru.vachok.networker.net.scanner.ScanOnline;


/**
 @since 06.08.2019 (16:27) */
public enum FileNames {
    ;
    
    public static final String FILENAME_INETSTATSCSV = "inetstats.csv";
    
    public static final String FILENAME_OLDCOMMON = "files.old";
    
    public static final String ICON_DEFAULT = "icons8-сетевой-менеджер-30.png";
    
    public static final String FILENAME_PTV = "ping.tv";
    
    public static final String FILENAME_ALLDEVMAP = "alldev.map";
    
    public static final String FILENAME_INETUNIQ = "inet.uniq";
    
    /**
     Выгрузка из БД {@link ConstantsFor#DBPREFIX} {@code velkom} - pcuserauto
     */
    public static final String FILENAME_VELKOMPCUSERAUTOTXT = "velkom_pcuserauto.txt";
    
    /**
     Файл уникальных записей из БД velkom-pcuserauto
     */
    public static final String FILENAME_PCAUTOUSERSUNIQ = "pcusersauto.uniq";
    
    public static final String FILEEXT_ONLIST = ".onList";
    
    public static final String FILENAME_ONSCAN = ScanOnline.class.getSimpleName() + FILEEXT_ONLIST;
    
    public static final String FILENAME_BUILDGRADLE = "build.gradle";
    
    public static final String FILENAME_STATSZIP = "stats.zip";
    
    public static final String FILENAME_COMMONRGH = "common.rgh";
    
    public static final String FILENAME_CLEANERLOGTXT = "cleaner.log.txt";
    
    public static final String FILEEXT_PROPERTIES = ".properties";
    
    public static final String FILEEXT_TEST = ConstantsFor.class.getSimpleName() + FILEEXT_PROPERTIES + ".t";
    
    public static final String FILENAME_COMMONOWN = "common.own";
    
    public static final String FILENAME_INETSTATSIPCSV = "inetstatsIP.csv";
    
    public static final String FILENALE_ONLINERES = "onLinesResolve.map";
    
    public static final String MAXONLINE = ConstantsFor.ROOT_PATH_WITH_SEPARATOR + "lan" + ConstantsFor.FILESYSTEM_SEPARATOR + "onlines.max";
    
    public static final String FILENAME_OWNER = "owner_users.txt";
    
    public static final String FILENAME_FOLDERACLTXT = "folder_acl.txt";
    
    public static final String FILENAME_OLDCOMMONCSV = "files_2.5_years_old_25mb.csv";
    
    public static final String PINGRESULT_LOG = "pingresult";
    
    public static final String FILEEXT_LOG = ".log";
    
    public static final String USERS_CSV = "/users.csv";
    
    public static final String SPEED_MAIL = "Speed.chechMail";
    
    public static final String UNUSED_IPS = "unused.ips";
    
    public static final String LASTNETSCAN_TXT = ConstantsNet.BEANNAME_LASTNETSCAN + ".txt";
    
    public static final String SCAN_TMP = "scan.tmp";
    
    public static final String SYSTEM = "system";
    
    /**
     Название файла новой подсети 10.200.х.х
     */
    public static final String NEWLAN205 = "lan_200205.txt";
    
    public static final String NEWLAN210 = "lan_205210.txt";
    
    public static final String NEWLAN220 = "lan_213220.txt";
    
    public static final String NEWLAN215 = "lan_210215.txt";
    
    /**
     Название файла старой подсети 192.168.х.х
     */
    public static final String OLDLANTXT0 = "lan_old0.txt";
    
    public static final String OLDLANTXT1 = "lan_old1.txt";
    
    public static final String SERVTXT = "srv.txt";
    
    public static final String SERVTXT_41SRVTXT = "lan_41v" + SERVTXT;
    
    public static final String SERVTXT_31SRVTXT = "lan_31v" + SERVTXT;
    
    public static final String SERVTXT_21SRVTXT = "lan_21v" + SERVTXT;
    
    public static final String SERVTXT_10SRVTXT = "lan_11v" + SERVTXT;
    
    public static final String AVAILABLECHARSETS_TXT = "availableCharsets.txt";
    
    public static final String EXT_TABLE = ".table";
    
    public static final String FILE_PREFIX_SEARCH_ = "search_";
}
