package ru.vachok.networker;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.apache.commons.net.ntp.TimeInfo;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.controller.ServiceInfoCtrl;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.mailserver.ExSRV;
import ru.vachok.networker.mailserver.MailRule;
import ru.vachok.networker.net.DiapazonedScan;
import ru.vachok.networker.services.MessageLocal;
import ru.vachok.networker.services.TimeChecker;

import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.time.Year;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 Константы, используемые в приложении
 <p>

 @since 12.08.2018 (16:26) */
public enum ConstantsFor {
    ;

    /**
     {@link ru.vachok.networker.mailserver.ExCTRL#uplFile(MultipartFile, Model)}, {@link ExSRV#getOFields()},
     */
    private static final ConcurrentMap<Integer, MailRule> MAIL_RULES = new ConcurrentHashMap<>();

    private static final int MIN_DELAY = 17;

    /**
     new {@link Properties}
     */
    private static final Properties PROPS = new Properties();

    public static final String METHNAME_STATIC_INITIALIZER = "static initializer";

    public static final String HEAD_REFERER = "referer";

    public static final String METHNAME_ACTIONPERFORMED = "actionPerformed";

    public static final String FILE_RU_VACHOK_NETWORKER_CONSTANTS_FOR = "ru_vachok_networker-ConstantsFor";

    public static final String TOSTRING_SAMACCOUNTNAME = ", samAccountName='";

    public static final String TOSTRING_CLASS_NAME = ", CLASS_NAME='";

    public static final String ATT_ADUSER = "aduser";

    public static final String TOSTRING_MESSAGE_TO_USER = ", messageToUser=";

    public static final String STR_RETURNS = "returns:";

    public static final String STR_INPUT_PARAMETERS_RETURNS = "input parameters] [Returns:";

    public static final String SOUTV = "SOUTV";

    public static final String JAVA_LANG_STRING_NAME = "java.lang.String";

    public static final String HTTP_LOCALHOST_8880_SLASH = "http://localhost:8880/";

    public static final String USERS_TXT = "/static/texts/users.txt";

    public static final String SHOWALLDEV_NEEDSOPEN = "http://localhost:8880/showalldev?needsopen";

    public static final String STR_VELKOM = "velkom";

    public static final String AT_NAME_RULESET = "ruleset";

    public static final String ATT_EXSRV = "exsrv";

    public static final String DB_FIELD_PCNAME = "pcName";

    public static final String TIME_SPEND = "TimeSpend";

    public static final String COL_SQL_NAME_TIMESTAMP = "TimeStamp";

    public static final String U_0466446_LIFERPG = "u0466446_liferpg";

    public static final String SELECT_FROM_SPEED = "select * from speed";

    public static final String STR_PCUSERAUTO = "pcuserauto";

    public static final String STR_PCUSER = "pcuser";

    /**
     <i>Boiler Plate</i>
     */
    public static final String PR_PFSCAN = "pfscan";

    /**
     <i>Boiler Plate</i>
     */
    public static final String ATT_GITSTATS = "gitstats";

    /**
     <i>Boiler Plate</i>
     */
    public static final String PFLISTS = "pflists";

    /**
     <i>Boiler Plate</i>
     */
    public static final String STR_REBOOT = "reboot";

    /**
     Комманда cmd
     */
    public static final String COM_SHUTDOWN_P_F = "shutdown /p /f";

    /**
     <i>Boiler Plate</i>
     */
    public static final String ATT_E_MESSAGE = "eMessage";

    /**
     <i>Boiler Plate</i>
     */
    public static final String ATT_STATCODE = "statcode";

    /**
     <i>Boiler Plate</i>
     */
    public static final String[] STRS_VISIT = {"visit_", ".tmp"};

    /**
     <i>Boiler Plate</i>
     */
    public static final String STR_CALCULATOR = "simpleCalculator";

    /**
     Диапазон для бинов
     */
    public static final String SINGLETON = "singleton";

    /**
     Название БД в reg.ru
     */
    public static final String U_0466446_VELKOM = "u0466446_velkom";

    /**
     Название property
     */
    public static final String PR_APP_VERSION = "appVersion";

    /**
     Название property
     */
    public static final String PR_QSIZE = "qsize";

    /**
     Название аттрибута модели.
     */
    public static final String ATT_RESULT = "result";

    /**
     Название аттрибута модели.
     */
    public static final String ATT_COMMON = "common";

    /**
     Название аттрибута модели.
     */
    public static final String ATT_PHOTO_CONVERTER = "photoConverter";

    /**
     Название аттрибута модели.
     */
    public static final String ATT_SSH_ACTS = "sshActs";

    /**
     Название property
     */
    public static final String PR_TOTPC = "totpc";

    /**
     Личный e-mail
     */
    public static final String GMAIL_COM = "143500@gmail.com";

    /**
     HTTP-header
     */
    public static final String HEAD_REFRESH = "Refresh";

    /**
     Название аттрибута модели.
     */
    public static final String ATT_VISIT = "visit";

    /**
     Название аттрибута модели.
     */
    public static final String ATT_REFERER = "ATT_REFERER";

    /**
     Адрес локального git
     */
    public static final String SRV_GIT_EATMEAT_RU = "srv-git.eatmeat.ru";

    /**
     {@code Files.setAttribute}
     */
    public static final String DOS_ARCHIVE = "dos:archive";

    /**
     Имя ПК no0027
     */
    public static final String NO0027 = "no0027";

    /**
     Название файла старой подсети 192.168.х.х
     */
    public static final String OLD_LAN_TXT = "old_lan.txt";

    /**
     Название файла новой подсети 10.200.х.х
     */
    public static final String AVAILABLE_LAST_TXT = "available_last.txt";

    /**
     Строка из Live Template soutm
     */
    public static final String STR_SEC_SPEND = " sec spend";

    /**
     <b>1 мегабайт в байтах</b>
     */
    public static final int MBYTE = 1048576;

    /**
     {@link Model} имя атрибута
     */
    public static final String ATT_FOOTER = "footer";

    /**
     Префикс имени от reg.ru
     */
    public static final String DB_PREFIX = "u0466446_";

    public static final boolean IS_SYSTRAY_AVAIL = (SystemTray.isSupported() || SystemTray.getSystemTray()!=null);

    /**
     {@link Model} имя атрибута
     */
    public static final String ATT_USERS = "users";

    /**
     {@link Model} имя атрибута
     */
    public static final String ATT_TITLE = "title";

    /**
     {@link ServiceInfoCtrl#closeApp()}
     */
    public static final int USER_EXIT = 222;

    /**
     IP srv-nat.eatmeat.ru
     */
    public static final String SRV_NAT = "192.168.13.30";

    /**
     IP stv-git.eatmeat.ru
     */
    public static final String SRV_GIT = "192.168.13.42";

    /**
     Кол-во минут в часе
     */
    public static final float ONE_HOUR_IN_MIN = 60f;

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
    public static final String APP_NAME = "ru_vachok_networker-";

    /**
     Кол-во часов в сутках
     */
    public static final int ONE_DAY_HOURS = 24;

    /**
     Кол-во байт в гигабайте
     */
    public static final long GBYTE = 1073741824;

    public static final String DB_FIELD_USER = "userName";

    /**
     Кол-во дней в месяце
     */
    public static final int ONE_MONTH_DAYS = 30;

    /**
     Кол-во дней в году
     */
    public static final int ONE_YEAR = 365;

    /**
     Домен с точкой
     */
    public static final String EATMEAT_RU = ".eatmeat.ru";

    /**
     Повторения в классах
     */
    public static final String STR_DELETED = " STR_DELETED";

    public static final String LOG = ".log";

    /**
     Число, для Secure Random
     */
    public static final long MY_AGE = ( long ) Year.now().getValue() - 1984;

    /**
     Первоначальная задержка шедулера.
     */
    public static final long INIT_DELAY = MY_AGE;

    /**
     Кол-во миллисек. в 1 неделе
     */
    public static final long ONE_WEEK_MILLIS = TimeUnit.HOURS.toMillis(ONE_DAY_HOURS * ( long ) 7);

    public static final String HTML_CENTER = "</center>";

    public static final String STR_INPUT_OUTPUT = "input/output\n";

    public static final String MATRIX_STRING_NAME = "matrix";

    public static final String WHOIS_STR = "whois";

    public static final String ICON_FILE_NAME = "icons8-сетевой-менеджер-30.png";

    public static final String PC_USER_RESOLVER_CLASS_NAME = "PCUserResolver";

    public static final int IPS_IN_VELKOM_VLAN = 5610;

    /**
     {@link #getDelay()}
     */
    public static final long DELAY = getDelay();

    /**
     Порт для {@link ru.vachok.networker.net.MyServer}
     */
    public static final int LISTEN_PORT = Integer.parseInt(PROPS.getOrDefault("lport", "9990").toString());

    /**
     Все возможные IP из диапазонов {@link DiapazonedScan}
     */
    public static BlockingDeque<String> ALL_DEVICES = new LinkedBlockingDeque<>(5610);

    /**
     {@link #getAtomicTime()}
     */
    @SuppressWarnings ("NonFinalFieldInEnum")
    private static long atomicTime;

    /**
     {@link MessageLocal}
     */
    private static MessageToUser messageToUser = new MessageLocal();

    /**
     @return {@link #MAIL_RULES}
     */
    public static ConcurrentMap<Integer, MailRule> getMailRules() {
        return MAIL_RULES;
    }

    /**
     Доступность srv-git.eatmeat.ru.

     @return 192.168.13.42 online or offline
     */
    public static boolean isPingOK() {
        try{
            return InetAddress.getByName(SRV_GIT_EATMEAT_RU).isReachable(500);
        }
        catch(IOException e){
            LoggerFactory.getLogger(ConstantsFor.class.getSimpleName()).error(e.getMessage(), e);
            return false;
        }
    }

    /**
     @return {@link AppComponents#getOrSetProps()}
     */
    public static Properties getAppProps() {
        String classMeth = "ConstantsFor.getAppProps";
        if(PROPS.size() < 3){
            takePr(false);
            messageToUser.info(classMeth, "From File", " = " + false);
            messageToUser.info(classMeth, "PROPS", " = " + PROPS.size());
        }
        else{
            takePr(true);
            messageToUser.warn(classMeth, "From File", " = " + true);
            messageToUser.info(classMeth, "PROPS", " = " + PROPS.size());
        }
        return PROPS;
    }

    /**
     Тащит {@link #PROPS} из БД или файла
     */
    static Properties takePr(boolean fromFile) {
        AppComponents.threadConfig().thrNameSet("gProps");
        InitProperties initProperties = new DBRegProperties(ConstantsFor.APP_NAME + ConstantsFor.class.getSimpleName());
        Properties retPr = new Properties();
        try{
            if(fromFile || new File("ff").exists()){
                try(InputStream inputStream = new FileInputStream(ConstantsFor.class.getSimpleName() + ".properties")){
                    retPr.load(inputStream);
                }
            }
            else{
                retPr = initProperties.getProps();
            }
            PROPS.clear();
            PROPS.putAll(retPr);
        }
        catch(IOException e){
            messageToUser.warn(
                "Can't load properties.",
                "Check for file " + new File(ConstantsFor.class.getSimpleName() + ".properties").getAbsolutePath(),
                e.getMessage());
            messageToUser.errorAlert("ConstantsFor.takePr", "PROPS", " = " + PROPS.size());
            FileSystemWorker.error("ConstantsFor.takePr", e);
        }
        return retPr;
    }

    /**
     @return Время работы в часах.
     */
    public static String getUpTime() {
        String tUnit = " h";
        float hrsOn = ( float )
            (System.currentTimeMillis() - ConstantsFor.START_STAMP) / 1000 / ConstantsFor.ONE_HOUR_IN_MIN / ConstantsFor.ONE_HOUR_IN_MIN;
        if(hrsOn > 24){
            hrsOn = hrsOn / ConstantsFor.ONE_DAY_HOURS;
            tUnit = " d";
        }
        return "(" + String.format("%.03f", hrsOn) + tUnit + " up)";
    }

    /**
     @return время билда
     */
    public static long getBuildStamp() {
        try{
            String hostName = InetAddress.getLocalHost().getHostName();
            if(hostName.equalsIgnoreCase("home") || hostName.toLowerCase().contains(NO0027)){
                PROPS.setProperty("build", System.currentTimeMillis() + "");
                return System.currentTimeMillis();
            }
            else{
                return Long.parseLong(PROPS.getProperty("build", "1"));
            }
        }
        catch(UnknownHostException e){
            return 1L;
        }
    }

    /**
     @return точное время как {@code long}
     */
    public static long getAtomicTime() {
        TimeChecker t = new TimeChecker();
        TimeInfo call = t.call();
        call.computeDetails();
        ConstantsFor.atomicTime = call.getReturnTime();
        return atomicTime;
    }

    /**
     @return кол-во выделенной, используемой и свободной памяти в МБ
     */
    public static String getMemoryInfo() {
        String msg = ( float ) Runtime.getRuntime().totalMemory() / ConstantsFor.MBYTE + " now totalMemory, " +
            ( float ) Runtime.getRuntime().freeMemory() / ConstantsFor.MBYTE + " now freeMemory, " +
            ( float ) Runtime.getRuntime().maxMemory() / ConstantsFor.MBYTE + " now maxMemory.";
        AppComponents.getLogger().warn(msg);
        return msg;
    }

    /**
     @return {@link #DELAY}
     */
    private static long getDelay() {
        long delay = new SecureRandom().nextInt(( int ) MY_AGE);
        if(delay < MIN_DELAY){
            delay = MIN_DELAY;
        }
        return delay;
    }

    /**
     Сохраняет {@link Properties} в БД {@link #APP_NAME} с ID {@code ConstantsFor}

     @param propsToSave {@link Properties}
     */
    public static boolean saveAppProps(Properties propsToSave) {
        AppComponents.threadConfig().thrNameSet("sProps");
        propsToSave.setProperty("thispc", thisPC());
        final String javaIDsString = ConstantsFor.APP_NAME + ConstantsFor.class.getSimpleName();
        String classMeth = "ConstantsFor.saveAppProps";
        String methName = "saveAppProps";
        MysqlDataSource mysqlDataSource = new DBRegProperties(javaIDsString).getRegSourceForProperties();
        AtomicBoolean retBool = new AtomicBoolean();
        mysqlDataSource.setLogger("java.util.Logger");

        Callable<Boolean> theProphecy = new SaveDBPropsCallable(mysqlDataSource, propsToSave, classMeth, methName);
        Future<Boolean> booleanFuture = AppComponents.threadConfig().getTaskExecutor().submit(theProphecy);

        try{
            retBool.set(booleanFuture.get());
        }
        catch(InterruptedException | ExecutionException e){
            messageToUser.errorAlert(ConstantsFor.class.getSimpleName(), methName, e.getMessage());
            FileSystemWorker.error(classMeth, e);
            Thread.currentThread().interrupt();
            retBool.set(booleanFuture.isDone());
        }
        return retBool.get();
    }

    /**
     @return имя компьютера, где запущено
     */
    public static String thisPC() {
        try{
            return InetAddress.getLocalHost().getHostName();
        }
        catch(UnknownHostException | ExceptionInInitializerError | NullPointerException e){
            String retStr = new TForms().fromArray(( List<?> ) e, false);
            FileSystemWorker.recFile("this_pc.err", Collections.singletonList(retStr));
            return "pc";
        }
    }

    /**
     Парсинг и проверка уникальности для new {@link Visitor}

     @param request {@link HttpServletRequest}
     @return {@link Visitor}
     */
    public static Visitor getVis(HttpServletRequest request) {
        try{
            return AppComponents.thisVisit(request.getSession().getId());
        }
        catch(Exception e){
            return new AppComponents().visitor(request);
        }
    }

    /**
     @param request для получения IP
     @return boolean авторизован или нет
     */
    public static boolean getPcAuth(HttpServletRequest request) {
        return request.getRemoteAddr().toLowerCase().contains("0:0:0:0") ||
            request.getRemoteAddr().contains("10.200.213") ||
            request.getRemoteAddr().contains("10.10.111") ||
            request.getRemoteAddr().contains("172.16.200");
    }

    public static String getUserPC(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

}
