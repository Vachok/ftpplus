package ru.vachok.networker.enums;


import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.net.scanner.ScanOnline;


/**
 @since 06.08.2019 (16:27) */
public enum FileNames {
    ;
    
    public static final String FILENAME_INETSTATSCSV = "inetstats.csv";
    
    public static final String FILENAME_OLDCOMMON = "files.old";
    
    public static final String FILENAME_ICON = "icons8-сетевой-менеджер-30.png";
    
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
    
    public static final String FILENAME_MAXONLINE = ConstantsFor.ROOT_PATH_WITH_SEPARATOR + "lan" + ConstantsFor.FILESYSTEM_SEPARATOR + "onlines.max";
    
    public static final String FILENAME_OWNER = "owner_users.txt";
    
    public static final String FILENAME_FOLDERACLTXT = "folder_acl.txt";
    
    public static final String FILENAME_OLDCOMMONCSV = "files_2.5_years_old_25mb.csv";
    
    public static final String PINGRESULT_LOG = "pingresult";
    
    public static final String PR_OSTFILENAME = "ostfilename";
    
    public static final String FILEEXT_LOG = ".log";
}
