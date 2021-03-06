// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.data.enums;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.IntoApplication;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.props.InitProperties;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Year;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;


/**
 Константы, используемые в приложении
 <p>

 @since 12.08.2018 (16:26) */
@SuppressWarnings("SpellCheckingInspection")
public enum ConstantsFor {
    ;

    public static final String HEAD_REFERER = "referer";

    public static final String METHNAME_ACTIONPERFORMED = "actionPerformed";

    public static final String STR_VELKOM = "velkom";

    public static final String DBFIELD_PCNAME = "pcName";

    public static final String DBFIELD_TIMESTAMP = "TimeStamp";

    public static final String DBBASENAME_U0466446_LIFERPG = "u0466446_liferpg";

    public static final String DB_PCUSERAUTO = "pcuserauto";

    public static final String DBFIELD_PCUSER = "pcuser";

    /**
     <i>Boiler Plate</i>
     */
    public static final String BEANNAME_PFLISTS = "pflists";

    /**
     <i>Boiler Plate</i>
     */
    public static final String BEANNAME_CALCULATOR = "simpleCalculator";

    /**
     Диапазон для бинов
     */
    public static final String SINGLETON = "singleton";

    /**
     Название БД в reg.ru
     */
    public static final String DBBASENAME_U0466446_VELKOM = "u0466446_velkom";

    /**
     HTTP-header
     */
    public static final String HEAD_REFRESH = "Refresh";

    /**
     Строка из Live Template soutm
     */
    public static final String STR_SEC_SPEND = " sec spend";

    /**
     <b>1 мегабайт в байтах</b>
     */
    public static final int MBYTE = 1048576;

    /**
     Префикс имени от reg.ru
     */
    public static final String DBPREFIX = "u0466446_";

    /**
     Кол-во байт в килобайте
     */
    public static final int KBYTE = 1024;

    /**
     Timestamp запуска.
     */
    public static final long START_STAMP = System.currentTimeMillis();

    /**
     MSEC таймаут
     */
    public static final int TIMEOUT_650 = 650;

    /**
     Имя для БД с настройками
     */
    public static final String APPNAME_WITHMINUS = "ru_vachok_networker-";

    /**
     Кол-во байт в гигабайте
     */
    public static final long GBYTE = 1073741824;

    public static final String DBFIELD_USERNAME = "userName";

    /**
     Кол-во дней в году
     */
    public static final int ONE_YEAR = 365;

    /**
     Повторения в классах
     */
    public static final String STR_DELETED = " STR_DELETED";

    /**
     Кол-во часов в сутках
     */
    public static final int ONE_DAY_HOURS = 24;

    /**
     Кол-во миллисек. в 1 неделе
     */
    public static final long ONE_WEEK_MILLIS = TimeUnit.HOURS.toMillis(ONE_DAY_HOURS * (long) 7);

    public static final int YEAR_OF_MY_B = 1984;

    /**
     Число, для Secure Random
     */
    public static final long MY_AGE = (long) Year.now().getValue() - YEAR_OF_MY_B;

    public static final String HTML_CENTER_CLOSE = "</center>";

    public static final String STR_INPUT_OUTPUT = "input/output\n";

    public static final String BEANNAME_MATRIX = "matrix";

    /**
     {@link UsefulUtilities#getDelay()}
     */
    public static final long DELAY = UsefulUtilities.getDelay();

    /**
     Домен с точкой
     */
    public static final String DOMAIN_EATMEATRU = ".eatmeat.ru";

    public static final double KM_A107 = 21.6;

    public static final double KM_M9 = 31.2;

    public static final int EXIT_STATUSBAD = 666;

    public static final int INT_ANSWER = 4;

    public static final String PROPS_FILE_JAVA_ID = ConstantsFor.class.getSimpleName() + FileNames.EXT_PROPERTIES;

    public static final String STR_VERSIONINFO = "versioninfo";

    public static final String SQL_SELECTFROM_PCUSERAUTO = "select * from pcuserauto";

    public static final String STR_BYTES = " bytes";

    public static final String DBCOL_RESPONSE = "response";

    public static final String DBFIELD_METHOD = "method";

    public static final String PROGNAME_OSTPST = "ostpst-";

    public static final String FILESUF_SSHACTIONS = "sshactions";

    public static final String HTMLTAG_CENTER = "<center>";

    public static final String STR_PROPERTIES = "properties";

    public static final String DBFIELD_TIMENOW = "TimeNow";

    public static final String HTMLTAG_DETAILSCLOSE = "</details>";

    public static final String COMMAND_CALCTIMES = "calctimes:";

    public static final String COMMAND_CALCTIME = "calctime:";

    public static final String STR_ERROR = "ERROR";

    public static final String STR_WRITTEN = " written";

    public static final String TOSTRING_NAME = "fileName='";

    public static final int FTP_PORT = 21;

    public static final String FILESYSTEM_SEPARATOR = System.getProperty(PropertiesNames.SYS_SEPARATOR);

    public static final String RETURN_ERROR = "error";

    public static final String SQL_SELECTINETSTATS = "SELECT DISTINCT `ip` FROM `inetstats`";

    /**
     Путь к архиву
     */
    public static final Path ARCHIVE_DIR = Paths.get("\\\\192.168.14.10\\IT-Backup\\Srv-Fs\\Archives");

    /**
     Путь к продуктиву
     */
    public static final Path COMMON_DIR = Paths.get("\\\\srv-fs.eatmeat.ru\\common_new");

    @SuppressWarnings("DuplicateStringLiteralInspection") public static final String CP_WINDOWS_1251 = "windows-1251";

    public static final String ROOT_PATH_WITH_SEPARATOR = Paths.get(".").toAbsolutePath().normalize() + System.getProperty(PropertiesNames.SYS_SEPARATOR);

    public static final String DBBASENAME_U0466446_TESTING = "u0466446_testing";

    public static final String DBBASENAME_U0466446_WEBAPP = "u0466446_webapp";

    /**
     <i>Boiler Plate</i>
     */
    public static final String BEANNAME_NETSCANNERSVC = "netScannerSvc";

    public static final String SQL_SELECT_DIST = "SELECT DISTINCT `Date`, `ip`, `inte`, `response`, `method`, `site`, `bytes` FROM `inetstats` WHERE `ip` LIKE ? ORDER BY `inetstats`.`Date` DESC";

    public static final String SRV_MAIL3 = "srv-mail3.eatmeat.ru";

    public static final String USER_SCANNER = "Scanner";

    public static final String STR_FALSE = "false";

    public static final String SITENAME_VELKOMFOODRU = "http://www.velkomfood.ru";

    public static final String SSH_COM_CATALLOWDOMAIN = "sudo cat /etc/pf/allowdomain";

    public static final String BEANNAME_PFLISTSSRV = "PfListsSrv";

    public static final String FOLDERNAME_COMMONNEW = "common_new";

    public static final String DIRNAME_ARCHIVES = "archives";

    public static final String STR_UNKNOWN = "Unknown";

    public static final String STR_OWNEDBY = " owned by: ";

    public static final String PATTERN_POINT = ".";

    public static final String STR_BR = "<br>";

    public static final String STR_N = "\n";

    public static final String STR_P = "<p>";

    public static final String STR_ACTIONPERFORMED = ".actionPerformed";

    public static final String SSH_CAT_PFSQUID = "sudo cat /etc/pf/squid;exit";

    public static final String SSH_SHOW_SQUIDLIMITED = "sudo cat /etc/pf/squidlimited;exit";

    public static final String SSH_CAT_PROXYFULL = "sudo cat /etc/pf/tempfull;exit";

    public static final String STREAMJAR_PROPERTIES = "/static/const.properties";

    public static final String GOOD_NO_LOCKS = "No deadlocks, good!";

    public static final String DBBASENAME_U0466446_PROPERTIES = "u0466446_properties";

    public static final String ATTRIB_HIDDEN = "dos:hidden";

    public static final String TOSTRING_EXECUTOR = "executor = ";

    public static final String PS_IMPORTSYSMODULES = "ImportSystemModules";

    public static final String SSHCOM_24HRS = " >> /etc/pf/24hrs;";

    public static final String NOT_ALLOWED = "NOT Allowed! Try this app: https://appdistribution.firebase.dev/i/s1bgY5MA";

    public static final String ANS_DOMNAMEEXISTS = "Domain is exists!";

    public static final String ANS_DNAMENULL = "allowdomain string is null";

    public static final String SSH_ALLOWDOM_ALLOWDOMTMP = "' /etc/pf/allowdomain > /etc/pf/allowdomain_tmp;";

    public static final String SSH_ALLOWIP_ALLOWIPTMP = "' /etc/pf/allowip > /etc/pf/allowip_tmp;";

    public static final String SSH_ALLOWDOMTMP_ALLOWDOM = "sudo cp /etc/pf/allowdomain_tmp /etc/pf/allowdomain;";

    public static final String SSH_ALLOWIPTMP_ALLOWIP = "sudo cp /etc/pf/allowip_tmp /etc/pf/allowip;";

    public static final String SSH_TAIL_ALLOWIPALLOWDOM = "sudo tail /etc/pf/allowdomain;sudo tail /etc/pf/allowip;";

    public static final String PREF_NODE_NAME = "networker";

    public static final String WRONG_PASS = "WRONG RASS";

    public static final String STR_EATMEAT = ".eatmeat";

    public static final String GREEN = "green";

    public static final String SHOWALLDEV = "/showalldev";

    public static final String DBCOL_BYTES = "bytes";

    public static final String EATMEAT = "eatmeat";

    public static final String YELLOW = "yellow";

    public static final String STR_FINISH = " is finish";

    /**
     Кол-во минут в часе
     */
    public static final float ONE_HOUR_IN_MIN = 60f;

    public static final String DB_VELKOMINETSTATS = "velkom.inetstats";

    public static final int MIN_DELAY = 17;

    public static final String SQL_GET_VELKOMPC_NAMEPP = "select * from velkompc where NamePP like ?";

    public static final String STARTING = "starting";

    public static final String CONNECTING_TO = "Connecting to: ";

    /**
     {@link Pattern} IP-адреса
     */
    public static final Pattern PATTERN_IP = Pattern
        .compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    public static final String DBCOL_IDREC = "idrec";

    public static final String DB_SEARCH = "search";

    public static final String STR_DUPLICATE = "Duplicate";

    public static final String DBCOL_UPSTRING = "upstring";

    public static final String DBCOL_STAMP = "stamp";

    public static final String DBCOL_SQUIDANS = "squidans";

    public static final String DB_INETSTATS = "inetstats.";

    public static final String ERROR_DUPLICATEENTRY = "Duplicate entry";

    public static final String ERROR_NOEXIST = "doesn't exist";

    public static final String ELAPSED = "Elapsed: ";

    public static final String SITE_VELKOMFOOD = "velkomfood.ru";

    public static final String SQL_SELECT = "select * from %s";

    public static final String SQL_DROPTABLE = "drop table %s";

    public static final String SQLTABLE_POINTCOMMON = ".common";

    public static final String DB_TABLESEARCH = "search.";

    public static final String DB_SEARCHS = "search.s";

    public static final String DB_PCUSERAUTO_FULL = DataConnectTo.DBNAME_VELKOM_POINT + DB_PCUSERAUTO;

    public static final String RU_GLAVNAYA = "Главная";

    public static final String TZ_MOSCOW = "Europe/Moscow";

    public static final String CLEANER = "cleaner";

    public static final String DBCOL_PROPERTY = "property";

    public static final String DB_USER = "u0466446_network";

    public static final String LIMIT = "' LIMIT ";

    public static final String DB_MEMPROPERTIES = "mem.properties";

    public static final String DBENGINE_MEMORY = "MEMORY";

    public static final String DB_PERMANENT = "permanent";

    public static final String DB_COMMONRESTORE = "common.restore";

    public static final String FIELDNAME_ADDR = "ipAddr";

    public static final String HTTPS = "https://";

    public static final String ERR_NOFILES = "No files";

    public static final Object TABLE = "Table: ";

    public static final String EXAMINED = "rows examined: ";

    public static final String TIME = "q_time: ";

    public static final String SQL_INSERTINTO = "insert into ";

    public static final String VALUES = ") values (";

    public static final String WHITE = "white";

    public static final String DBFIELD_ONLINE = "online";

    public static final String DBFIELD_ONLINECONROL = "onlineConrol";

    /**
     sshworks.html
     */
    public static final String SSHWORKS_HTML = "sshworks";

    public static final String SSH_UNAMEA = "uname -a";

    /**
     SSH-command
     */
    public static final String SSH_SUDO_ECHO = "sudo echo ";

    /**
     SSH-command
     */
    public static final String SSH_SUDO_GREP_V = "sudo grep -v -w '";

    public static final String SSH_ETCPF = " /etc/pf/";

    public static final String SSH_INITPF = "sudo /etc/initpf.fw;sudo squid -k reconfigure;exit";

    public static final String SSH_CAT_24HRSLIST = "sudo cat /etc/pf/24hrs;exit";

    public static final String DB_FIELD_WHENQUERIED = "whenQueried";

    public static final int LOGLEVEL = 1;

    public static final String RED = "red";

    public static final String DBCOL_ADDRPP = "AddressPP";

    public static final String DBCOL_SEGPP = "SegmentPP";

    public static final String DB_UIDS_FULL = "velkom.restuids";

    public static final String DB_SEARCHPERMANENT = "search.permanent";

    public static final String SQL_TABLE_NAME = "TABLE_NAME";

    public static final String UNKNOWN_USER = "Unknown user";

    public static final String OFFLINE = "offline";

    public static final String NULL_FALSE = "null -> false";

    public static final String DB_VELKOMPCUSER = "velkom.pcuser";

    public static final String ISNTRESOLVED = "Login isn't resolved";

    public static final String USERS = ": Users";

    public static final String DBFIELD_CONTROLIP = "controlIp";

    public static final String DBFIELD_TIMEON = "timeon";

    public static final String DBFIELD_LASTONLINE = "lastOnLine";

    public static final String DBTABLE_LOGTEMPINET = "log.tempinet";

    public static final String SSH_SQUID_RECONFIGURE = "sudo squid && sudo squid -k reconfigure;";

    public static final String LOCAL = "local";

    public static final String GRADLE = "gradle";

    public static final String DB_SLOWLOG = "mysql.slow_log";

    public static final String MARKEDASCRASHED = "is marked as crashed";

    public static final String CHARSET_IBM866 = "IBM866";

    public static final String DB_LANONLINE = "lan.online";

    public static final String DB_LOGNETWORKER = "log.networker";

    public static final String MAIL_SERVERREGRU = "mail.chess.vachok.ru";

    public static final String DB_COMMONOLDFILES = "common.oldfiles";

    public static final String STR_IPREMAIN = " ip remain";

    public static final String REPAIR_TABLE = "REPAIR TABLE ";

    public static final String DBCOL_NAMEPP = "NamePP";

    public static final String TOTALSIZE = "Total size = ";

    public static final String DB_VELKOMVELKOMPC = "velkom.velkompc";

    public static final String DB_ARCHIVEVELKOMPC = "archive.velkompc";

    public static final String PROTOTYPE = "prototype";

    public static final String AUTHORIZATION = "Authorization";

    public static final String OPTION = "option";

    public static final String WHOCALLS = "whocalls";

    public static final String APPLICATION_JSON = "application/json";

    public static final String BEANNAME_LASTNETSCAN = "lastnetscan";

    public static final String SRV_VPN = "srv-vpn.eatmeat.ru";

    public static final String GIT_SERVER = "http://srv-inetstat.eatmeat.ru:1234";

    public static final String ADD = "add";

    public static final String JSON_PARAM_NAME_CODE = "code";

    public static final String SSH_COM_CAT_VIPNET = "sudo cat /etc/pf/vipnet;exit";

    public static final String[] SSH_LIST_COMMANDS = {SSH_CAT_24HRSLIST, SSH_CAT_PFSQUID, SSH_SHOW_SQUIDLIMITED, SSH_CAT_PROXYFULL, SSH_COM_CAT_VIPNET};

    public static final String JSON_LIST_LIMITSQUID = "limitSquid";

    public static final String DB_REGRU_JSON_PROPS_TABLE = "`u0466446_properties`.`dev`";

    public static final String DOMAIN = "domain";

    public static final String DELETE = "delete";

    public static final String RULES = "rules";

    public static final String VACHOK_VACHOK_RU = "vachok@vachok.ru";

    public static final String FIREBASE = InitProperties.FIREBASE;

    public static final String OWNER = "owner";

    public static final String NETWORKER = "ru.vachok.networker";

    public static final String SSHADD = "/sshadd";

    public static final String SSHCOM_GETALLOWDOMAINS = "sudo cat /etc/pf/allowdomain;exit";

    public static final String FILES = "Files: ";

    public static final String EXTENDED = " QUICK EXTENDED;";

    public static final String JSON_PARAM_NAME_SERVER = "server";

    public static final String PARM_NAME_COMMAND = "command";

    public static final String RUNNING = "Walker_running";

    public static final String ISTRANET = "Istranet";

    public static final String FORTEX = "Fortex";

    public static final String VPN_LIST = "OpenVPN CLIENT LIST";

    public static final int SSH_TIMEOUT = 863;

    public static final String JSON_OBJECT_FULL_SQUID = "fullSquid";

    public static final String JSON_OBJECT_RULES = "pfRules";

    public static final String JSON_OBJECT_NAT = "pfNat";

    public static final String JSON_OBJECT_STD_SQUID = "stdSquid";

    public static final String JSON_OBJECT_VIPNET = "vipNet";

    public static final String JSON_OBJECT_SQUID = "squid";

    public static final String JSON_LIST_24HRS = "24hrs";

    public static final String DBCOL_VALUEOFPROPERTY = "valueofproperty";

    public static final String REGRUHOSTING_PC = "regruhosting.ru";

    public static final String APP_ARG_NOSCAN = "noscan";

    public static final String JSON_PARAM_NAME_STARTPATH = "startPath";

    public static final String JSON_PARAM_NAME_USER = "user";

    public static final String JSON_PARAM_NAME_BAD_AUTH = "BAD AUTH";

    public static final String JSON_PARAM_NAME_REQUEST_JSON = "request_json";

    public static final String UTF_8 = "UTF-8";

    private static final String[] EXCLUDED_FOLDERS_FOR_CLEANER = {"01_Дирекция", "_Положения_должностные_инструкции"};

    @NotNull
    public static String[] getExcludedFoldersForCleaner() {
        Set<String> excludeFolders = new TreeSet<>();
        final Set<Path> pathSet = getPathsAlreadyInDB();
        excludeFolders.addAll(Arrays.asList(EXCLUDED_FOLDERS_FOR_CLEANER));
        excludeFolders.addAll(FileSystemWorker.readFileToList(new File(FileNames.CLEANSTOP_TXT).getAbsolutePath()));
        Iterator<Path> pathIterator = pathSet.iterator();
        Object[] objects = pathSet.toArray();
        for (int i = 0; i < objects.length - 1; i++) {
            String toAdd = objects[i].toString();
            excludeFolders.add(toAdd);
        }
        return excludeFolders.toArray(new String[0]);
    }

    private static Set<Path> getPathsAlreadyInDB() {
        SortedSet<Path> retSet = new TreeSet<>();
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(ConstantsFor.DB_COMMONOLDFILES);
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM common.oldfiles");
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                retSet.add(Paths.get(resultSet.getString("AbsolutePath")).subpath(0, 3));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return retSet;
    }

    /**
     @see ConstantsForTest
     */
    public static boolean argNORUNExist(String... runOnlyOn) {
        boolean retBool = false;
        File file = new File(FileNames.ARG_NO_RUN);
        Map<String, String> appArgs = IntoApplication.getAppArgs();
        for (String s : runOnlyOn) {
            if (!s.isEmpty() && UsefulUtilities.thisPC().toLowerCase().contains(s.toLowerCase())) {
                retBool = true;
                FileSystemWorker.writeFile(file.getAbsolutePath(), UsefulUtilities.thisPC() + "\n\n\n" + AbstractForms
                    .fromArray(Thread.currentThread().getStackTrace()));
            }
        }
        if (!appArgs.isEmpty()) {
            if (appArgs.containsKey(APP_ARG_NOSCAN)) {
                retBool = true;
                FileSystemWorker.writeFile(file.getAbsolutePath(), new Date().toString());
            }
        }
        file.deleteOnExit();
        return retBool;
    }
}
