// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.data.enums;


import ru.vachok.networker.net.scanner.ScanOnline;


/**
 @since 06.08.2019 (16:27) */
public enum FileNames {
    ;

    public static final String FILES_OLD = "files.old";

    public static final String PING_TV = "ping.tv";

    public static final String INET_UNIQ = "inet.uniq";

    /**
     Выгрузка из БД {@link ConstantsFor#DBPREFIX} {@code velkom} - pcuserauto
     */
    public static final String VELKOMPCUSERAUTO_TXT = "velkom_pcuserauto.txt";

    public static final String EXT_ONLIST = ".onList";

    public static final String ONSCAN = ScanOnline.class.getSimpleName() + EXT_ONLIST;

    public static final String BUILD_GRADLE = "build.gradle";

    public static final String COMMON_RGH = "common.rgh";

    public static final String CLEANERLOG_TXT = "cleaner.log.txt";

    public static final String EXT_PROPERTIES = ".properties";

    public static final String COMMON_OWN = "common.own";

    public static final String INETSTATSIP_CSV = "inetstatsIP.csv";

    public static final String ONLINES_MAX = ConstantsFor.ROOT_PATH_WITH_SEPARATOR + "lan" + ConstantsFor.FILESYSTEM_SEPARATOR + "onlines.max";

    public static final String FILENAME_OWNER = "owner_users.txt";

    public static final String PINGRESULT = "pingresult";

    public static final String UNUSED_IPS = "unused.ips";

    public static final String LASTNETSCAN_TXT = ConstantsFor.BEANNAME_LASTNETSCAN + ".txt";

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

    public static final String SERVTXT_31SRVTXT = "lan_31v" + SERVTXT;

    public static final String SERVTXT_21SRVTXT = "lan_21v" + SERVTXT;

    public static final String SERVTXT_10SRVTXT = "lan_11v" + SERVTXT;

    public static final String EXT_TABLE = ".table";

    public static final String SEARCH_LAST = "search.last";

    public static final String DIR_INETSTATS = "inetstats";

    public static final String INETIPS_SET = "inetips.set";

    public static final String APP_JSON = "app.json";

    public static final String CONSTANTSFOR_PROPERTIES = "ConstantsFor.properties";

    public static final String USERLOGINCOUNTER_TXT = "user_login_counter.txt";

    public static final String CLEANSTOP_TXT = "cleanstop.txt";

    public static final String WALKER_LCK = "walker.lck";

    public static final String WEEKLY_LCK = "weekly.lck";

    public static final String _STACK_TXT = "-stack.txt";

    public static final String OLDFILES_LOG = "old_files.log";

    public static final String SSH_ERR = "ssh.err.log";

    public static final String SSH_LISTS_LOG = "getsshlists.log";

    public static final String DFINETSTAT = "dfinetstat";

    public static final String OPENVPN_STATUS = "openvpn-status";
}
