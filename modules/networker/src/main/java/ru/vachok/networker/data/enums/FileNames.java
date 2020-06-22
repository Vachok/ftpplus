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
     Выгрузка из БД {@link ConstantsFor#U0466446_DBPREFIX} {@code velkom} - pcuserauto
     */
    public static final String VELKOM_PCUSERAUTO_TXT = "velkom_pcuserauto.txt";

    public static final String EXT_ONLIST = ".onList";

    public static final String ONSCAN = ScanOnline.class.getSimpleName() + EXT_ONLIST;

    public static final String BUILD_GRADLE = "build.gradle";

    public static final String COMMON_RGH = "common.rgh";

    public static final String CLEANER_LOG_TXT = "cleaner_log.txt";

    public static final String EXT_PROPERTIES = ".properties";

    public static final String COMMON_OWN = "common.own";

    public static final String INETSTATSIP_CSV = "inetstatsIP.csv";

    public static final String ONLINES_MAX = ConstantsFor.ROOT_PATH_WITH_SEPARATOR + "lan" + ConstantsFor.FILESYSTEM_SEPARATOR + "onlines.max";

    public static final String FILENAME_OWNER = "owner_users.txt";

    public static final String PINGRESULT = "ping.result";

    public static final String UNUSED_IPS = "unused.ips";

    public static final String LASTNETSCAN_TXT = ConstantsFor.BEANNAME_LASTNETSCAN + ".txt";

    public static final String SCAN_TMP = "scan.tmp";

    public static final String SYSTEM = "system";

    /**
     Название файла новой подсети 10.200.х.х
     */
    public static final String LAN_200205_TXT = "lan_200205.txt";

    public static final String LAN_205210_TXT = "lan_205210.txt";

    public static final String LAN_213220_TXT = "lan_213220.txt";

    public static final String LAN_210215_TXT = "lan_210215.txt";

    /**
     Название файла старой подсети 192.168.х.х
     */
    public static final String LAN_OLD0_TXT = "lan_old0.txt";

    public static final String LAN_OLD1_TXT = "lan_old1.txt";

    public static final String SERVTXT = "srv.txt";

    public static final String LAN_31V_SERV_TXT = "lan_31v" + SERVTXT;

    public static final String LAN_21V_SERV_TXT = "lan_21v" + SERVTXT;

    public static final String LAN_11V_SERV_TXT = "lan_11v" + SERVTXT;

    public static final String EXT_TABLE = ".table";

    public static final String SEARCH_LAST = "search.last";

    public static final String DIR_INETSTATS = "inetstats";

    public static final String INETIPS_SET = "inetips.set";

    public static final String APP_JSON = "app.json";

    public static final String CONSTANTSFOR_PROPERTIES = "ConstantsFor.properties";

    public static final String USER_LOGIN_COUNTER_TXT = "user_login_counter.txt";

    public static final String CLEANSTOP_TXT = "cleanstop.txt";

    public static final String WALKER_LCK = "walker.lck";

    public static final String WEEKLY_LCK = "weekly.lck";

    public static final String OLDFILES_LOG = "old_files.log";

    public static final String SSH_ERR_LOG = "ssh_err.log";

    public static final String SSH_LISTS_LOG = "ssh_lists.log";

    public static final String DFINETSTAT = "dfinetstat";

    public static final String OPENVPN_STATUS = "openvpn-status";

    public static final String APP_START = "app.start";

    public static final String SSH_LOG = "ssh.log";

    public static final String ARG_NO_RUN = "app.arg";

    public static final String AVAILABLE_CHARSETS_TXT = "availableCharsets.txt";

    public static final String PEM = "a161.pem";
}
