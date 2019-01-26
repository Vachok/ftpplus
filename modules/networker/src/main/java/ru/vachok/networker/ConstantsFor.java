package ru.vachok.networker;


import org.apache.commons.net.ntp.TimeInfo;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import ru.vachok.messenger.MessageSwing;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.FileProps;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.controller.ServiceInfoCtrl;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.mailserver.ExSRV;
import ru.vachok.networker.mailserver.MailRule;
import ru.vachok.networker.net.DiapazonedScan;
import ru.vachok.networker.net.NetScannerSvc;
import ru.vachok.networker.services.TimeChecker;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;

import static java.time.temporal.ChronoUnit.HOURS;


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

    /**
     new {@link Properties}
     */
    private static final Properties PROPS = new Properties();

    public static final String STR_VELKOM = "velkom";

    public static final String AT_NAME_RULESET = "ruleset";

    public static final String ATT_EXSRV = "exsrv";

    public static final String DB_FIELD_PCNAME = "pcName";

    public static final String TIME_SPEND = "TimeSpend";

    public static final String COL_SQL_NAME_TIMESTAMP = "TimeStamp";

    public static final String U_0466446_LIFERPG = "u0466446_liferpg";

    public static final String SELECT_FROM_SPEED = "select * from speed";

    public static final String COMPNAME_USERS_MAP_SIZE = " COMPNAME_USERS_MAP size";

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
    public static final String STR_VISIT = "visit_";

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
     Выгрузка из БД {@link #U_0466446_VELKOM}-pcuserauto
     */
    public static final String VELKOM_PCUSERAUTO_TXT = "velkom_pcuserauto.txt";

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
    public static final String PR_LASTSCAN = "lastscan";

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
     Число, для Secure Random
     */
    public static final long MY_AGE = ( long ) Year.now().getValue() - 1984;

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
     {@link NetScannerSvc#getPCsAsync()}
     */
    public static final ConcurrentMap<String, File> COMPNAME_USERS_MAP = new ConcurrentHashMap<>();

    /**
     <b>1 мегабайт в байтах</b>
     */
    public static final int MBYTE = 1048576;

    /**
     {@link ru.vachok.networker.ad.PCUserResolver#recToDB(String, String)}
     */
    public static final ConcurrentMap<String, String> PC_U_MAP = new ConcurrentHashMap<>();

    /**
     {@link Model} имя атрибута
     */
    public static final String ATT_FOOTER = "footer";

    /**
     Префикс имени от reg.ru
     */
    public static final String DB_PREFIX = "u0466446_";

    /**
     Первоначальная задержка {@link ThreadConfig#threadPoolTaskScheduler()}
     */
    public static final long INIT_DELAY = TimeUnit.MINUTES.toSeconds(MY_AGE);

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
     {@link #getDelay()}
     */
    public static final long DELAY = getDelay();

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
     Кол-во миллисек. в 1 неделе
     */
    public static final long ONE_WEEK_MILLIS = TimeUnit.HOURS.toMillis(ONE_DAY_HOURS * ( long ) 7);

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
     Число IP по кол-ву VLANs
     */
    public static final int IPS_IN_VELKOM_VLAN = getIPs();

    /**
     Все возможные IP из диапазонов {@link DiapazonedScan}
     */
    public static final BlockingQueue<String> ALL_DEVICES = new ArrayBlockingQueue<>(IPS_IN_VELKOM_VLAN);

    /**
     Порт для {@link ru.vachok.networker.net.MyServer}
     */
    public static final int LISTEN_PORT = Integer.parseInt(PROPS.getOrDefault("lport", "9990").toString());

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
     {@link #getAtomicTime()}
     */
    @SuppressWarnings ("NonFinalFieldInEnum")
    private static long atomicTime;

    /**
     @return {@link #MAIL_RULES}
     */
    public static ConcurrentMap<Integer, MailRule> getMailRules() {
        return MAIL_RULES;
    }

    /**
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
     @return {@link #PROPS}
     */
    public static Properties getProps() {
        if(PROPS.size() < 4){
            takePr();
        }
        return PROPS;
    }

    /**
     @return Время работы в часах.
     */
    public static String getUpTime() {

        float hrsOn =
            ( float ) (System.currentTimeMillis() - ConstantsFor.START_STAMP) / 1000 / ConstantsFor.ONE_HOUR_IN_MIN / ConstantsFor.ONE_HOUR_IN_MIN;

        return "(" + String.format("%.03f", hrsOn) +
            "h up)";
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
     Сохраняет {@link Properties} в БД {@link #APP_NAME} с ID {@code ConstantsFor}

     @param propsToSave {@link Properties}
     */
    public static void saveProps(Properties propsToSave) {
        new ThreadConfig().threadPoolTaskExecutor().execute(() -> {
            InitProperties initProperties = new FileProps(ConstantsFor.APP_NAME + ConstantsFor.class.getSimpleName());
            initProperties.setProps(propsToSave);
            initProperties = new DBRegProperties(ConstantsFor.APP_NAME + ConstantsFor.class.getSimpleName());
            initProperties.delProps();
            initProperties.setProps(propsToSave);
        });
    }

    /**
     @return {@link #IPS_IN_VELKOM_VLAN}
     */
    private static int getIPs() {
        int vlansNum = Integer.parseInt(PROPS.getProperty("vlans", "22"));
        int qSize = vlansNum * 255;
        PROPS.setProperty(PR_QSIZE, qSize + "");
        return qSize;
    }

    /**
     @return {@link #DELAY}
     */
    private static long getDelay() {
        long delay = new SecureRandom().nextInt(( int ) MY_AGE);
        if(delay < 16){
            delay = 16;
        }
        return delay;
    }

    /**
     @return кол-во выделенной, используемой и свободной памяти в МБ
     */
    public static String showMem() {
        String msg = ( float ) Runtime.getRuntime().totalMemory() / ConstantsFor.MBYTE + " now totalMemory, " +
            ( float ) Runtime.getRuntime().freeMemory() / ConstantsFor.MBYTE + " now freeMemory, " +
            ( float ) Runtime.getRuntime().maxMemory() / ConstantsFor.MBYTE + " now maxMemory.";
        AppComponents.getLogger().warn(msg);
        return msg;
    }

    /**
     @return время до 17:30 в процентах от 8:30
     */
    public static String percToEnd(Date timeToStart) {
        StringBuilder stringBuilder = new StringBuilder();
        LocalDateTime startDayTime = LocalDateTime.ofEpochSecond(timeToStart.getTime() / 1000, 0, ZoneOffset.ofHours(3));
        LocalTime startDay = startDayTime.toLocalTime();
        LocalTime endDay = startDay.plus(9, HOURS);
        final int secDayEnd = endDay.toSecondOfDay();
        final int startSec = startDay.toSecondOfDay();
        final int allDaySec = secDayEnd - startSec;
        LocalTime localTime = endDay.minusHours(LocalTime.now().getHour());
        localTime = localTime.minusMinutes(LocalTime.now().getMinute());
        localTime = localTime.minusSeconds(LocalTime.now().getSecond());
        boolean workHours = LocalTime.now().isAfter(startDay) && LocalTime.now().isBefore(endDay);
        if(workHours){
            int toEndDaySec = localTime.toSecondOfDay();
            int diffSec = allDaySec - toEndDaySec;
            float percDay = (( float ) toEndDaySec / ((( float ) allDaySec) / 100));
            stringBuilder
                .append("Работаем ")
                .append(TimeUnit.SECONDS.toMinutes(diffSec));
            stringBuilder
                .append("(мин.). Ещё ")
                .append(String.format("%.02f", percDay))
                .append(" % или ");
        }
        else{
            stringBuilder.append("<b> GO HOME! </b><br>");
        }
        stringBuilder.append(localTime.toString());
        return stringBuilder.toString();
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
     Тащит {@link #PROPS} из БД или файла
     */
    static void takePr() {
        InitProperties initProperties;
        try{
            initProperties = new DBRegProperties(ConstantsFor.APP_NAME + ConstantsFor.class.getSimpleName());
            String msg = "Taking DB properties:" + "\n" + initProperties.getClass().getSimpleName();
            AppComponents.getLogger().info(msg);
            PROPS.putAll(initProperties.getProps());
        }
        catch(Exception e){
            initProperties = new FileProps(ConstantsFor.APP_NAME + ConstantsFor.class.getSimpleName());
            String msg = "Taking File properties:" + "\n" + e.getMessage();
            AppComponents.getLogger().warn(msg);
            PROPS.putAll(initProperties.getProps());
            new MessageSwing().infoNoTitlesDIA(e.getMessage() + " " + ConstantsFor.class.getSimpleName());
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

    public static String getUserPC(HttpServletRequest request) {
        return request.getRemoteAddr();
    }}
