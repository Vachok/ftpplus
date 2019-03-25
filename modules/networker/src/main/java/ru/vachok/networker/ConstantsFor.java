// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.apache.commons.net.ntp.TimeInfo;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageSwing;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.networker.ad.user.PCUserResolver;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.controller.ServiceInfoCtrl;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.mailserver.ExSRV;
import ru.vachok.networker.mailserver.MailRule;
import ru.vachok.networker.services.MessageLocal;
import ru.vachok.networker.services.TimeChecker;
import ru.vachok.networker.systray.ActionDefault;

import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.time.Year;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import static ru.vachok.networker.IntoApplication.getConfigurableApplicationContext;


/**
 Константы, используемые в приложении
 <p>

 @since 12.08.2018 (16:26) */
public enum ConstantsFor {
    ;

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

    public static final String HTTP_LOCALHOST8880SLASH = "http://localhost:8880/";

    public static final String FILEPATHSTR_USERSTXT = "/static/texts/users.txt";

    public static final String STR_VELKOM = "velkom";

    public static final String AT_NAME_RULESET = "ruleset";

    public static final String ATT_EXSRV = "exsrv";

    public static final String DBFIELD_PCNAME = "pcName";

    public static final String DBFIELD_TIMESPEND = "TimeSpend";

    public static final String DBFIELD_TIMESTAMP = "TimeStamp";

    public static final String DBBASENAME_U0466446_LIFERPG = "u0466446_liferpg";

    public static final String DBQUERY_SELECTFROMSPEED = "select * from speed";

    public static final String DBFIELD_PCUSERAUTO = "pcuserauto";

    public static final String DBFIELD_PCUSER = "pcuser";

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
    public static final String BEANNAME_PFLISTS = "pflists";

    /**
     <i>Boiler Plate</i>

     @see ru.vachok.networker.accesscontrol.MatrixCtr
     @see ru.vachok.networker.net.MyServer
     */
    public static final String COM_REBOOT = "reboot";

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

    public static final boolean IS_SYSTRAY_AVAIL = (SystemTray.isSupported() || SystemTray.getSystemTray() != null);

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
    public static final String DBDASENAME_U0466446_VELKOM = "u0466446_velkom";

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
    public static final String MAILADDR_143500GMAILCOM = "143500@gmail.com";

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
    public static final String HOSTNAME_SRVGITEATMEATRU = "srv-git.eatmeat.ru";

    /**
     {@code Files.setAttribute}
     */
    public static final String DOS_ARCHIVE = "dos:archive";

    /**
     Имя ПК HOME
     */
    public static final String HOSTNAME_HOME = "HOME";

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
    public static final String DBPREFIX = "u0466446_";

    /**
     Число, для Secure Random
     */
    public static final long MY_AGE = (long) Year.now().getValue() - 1984;

    /**
     {@link Model} имя атрибута
     */
    public static final String ATT_USERS = "users";

    /**
     {@link Model} имя атрибута
     */
    public static final String ATT_TITLE = "title";

    /**
     {@link ServiceInfoCtrl#closeApp(HttpServletRequest)}
     */
    public static final int CODE_USEREXIT = 222;

    public static final String ATT_HEAD = "head";

    public static final String HOSTNAME_DO213 = "do0213";

    /**
     IP srv-nat.eatmeat.ru
     */
    public static final String IPADDR_SRVNAT = "192.168.13.30";

    /**
     IP stv-git.eatmeat.ru
     */
    public static final String IPADDR_SRVGIT = "192.168.13.42";

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
    public static final String APPNAME_WITHMINUS = "ru_vachok_networker-";

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
     Кол-во дней в году
     */
    public static final int ONE_YEAR = 365;

    /**
     Повторения в классах
     */
    public static final String STR_DELETED = " STR_DELETED";

    public static final String FILEEXT_LOG = ".log";

    /**
     Кол-во миллисек. в 1 неделе
     */
    public static final long ONE_WEEK_MILLIS = TimeUnit.HOURS.toMillis(ONE_DAY_HOURS * (long) 7);

    /**
     Первоначальная задержка шедулера.
     */
    public static final long INIT_DELAY = MY_AGE;

    /**
     Имя аттрибута
     */
    public static final String ATT_SSHDETAIL = "sshdetail";

    public static final String HTML_CENTER = "</center>";

    public static final String STR_INPUT_OUTPUT = "input/output\n";

    public static final String BEANNAME_MATRIX = "matrix";

    public static final String ATT_WHOIS = "whois";

    public static final String FILENAME_ICON = "icons8-сетевой-менеджер-30.png";

    public static final String CLASS_NAME_PCUSERRESOLVER = PCUserResolver.class.getSimpleName();

    /**
     {@link #getDelay()}
     */
    public static final long DELAY = getDelay();

    public static final Runnable INFO_MSG_RUNNABLE = ()->{
        File todoFileHome = new File("G:\\My_Proj\\FtpClientPlus\\modules\\networker\\TODO");
        File todoFileWork = new File("C:\\Users\\ikudryashov\\IdeaProjects\\spring\\modules\\networker\\TODO");
        if (todoFileHome.exists() || todoFileWork.exists()) {
            new MessageSwing(new ActionDefault("https://github.com/Vachok/ftpplus/issues")).warn("CHECK TODO!");
        } else {
            new MessageCons().info("ConstantsFor.INFO_MSG_RUNNABLE", "thisPC()", " = " + thisPC());
        }
    };
    public static final String PR_AND_ATT_NEWPC = "newpc";

    static final String STR_FINISH = " is finish";

    /**
     new {@link Properties}
     */
    private static final Properties PROPS = new Properties();

    private static final String[] STRINGS_TODELONSTART = {"visit_", ".tmp"};

    /**
     {@link ru.vachok.networker.mailserver.ExCTRL#uplFile(MultipartFile, Model)}, {@link ExSRV#getOFields()},
     */
    private static final ConcurrentMap<Integer, MailRule> MAIL_RULES = new ConcurrentHashMap<>();

    private static final int MIN_DELAY = 17;

    public static final String FILENAME_PTV = "ping.tv";
    public static final int EXIT_STATUSBAD = 666;
    public static final int INT_ANSWER = 42;
    public static final String FILENAME_ALLDEVMAP = "alldev.map";
    /**
     * {@link MessageLocal}
     */
    private static final MessageToUser messageToUser = new MessageLocal(ConstantsFor.class.getSimpleName());

    public static final String FILEEXT_PROPERTIES = ".properties";


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
        try {
            return InetAddress.getByName(HOSTNAME_SRVGITEATMEATRU).isReachable(500);
        } catch (IOException e) {
            LoggerFactory.getLogger(ConstantsFor.class.getSimpleName()).error(e.getMessage(), e);
            return false;
        }
    }

    public static Visitor thisVisit(String sessionID) throws NullPointerException, NoSuchBeanDefinitionException {
        return (Visitor) configurableApplicationContext().getBean(sessionID);
    }

    /**
     @return {@link #takePr(boolean)} or {@link #PROPS}
     */
    public static Properties getAppProps() {
        if (PROPS.size() < 3) {
            messageToUser.info("PROPS", "return takePr(false)", " = " + PROPS.size());
            return takePr(false);
        } else {
            messageToUser.info("PROPS", "return PROPS", " = " + PROPS.size());
            return PROPS;
        }
    }

    /**
     @return Время работы в часах.
     */
    public static String getUpTime() {
        String tUnit = " h";
        float hrsOn = (float)
            (System.currentTimeMillis() - START_STAMP) / 1000 / ONE_HOUR_IN_MIN / ONE_HOUR_IN_MIN;
        if (hrsOn > 24) {
            hrsOn /= ONE_DAY_HOURS;
            tUnit = " d";
        }
        return "(" + String.format("%.03f", hrsOn) + tUnit + " up)";
    }

    /**
     @return время билда
     */
    public static long getBuildStamp() {
        long retLong = 1L;
        try {
            String hostName = InetAddress.getLocalHost().getHostName(); if (hostName.equalsIgnoreCase(HOSTNAME_DO213) || hostName.toLowerCase().contains(HOSTNAME_HOME)) {
                PROPS.setProperty("build", System.currentTimeMillis() + "");
                retLong = System.currentTimeMillis();
            } else {
                retLong = Long.parseLong(PROPS.getProperty("build", "1"));
            }
        } catch (UnknownHostException e) {
            messageToUser.errorAlert("ConstantsFor", "getBuildStamp", e.getMessage());
            FileSystemWorker.error("ConstantsFor.getBuildStamp", e);
        }
        return retLong;
    }

    /**
     @return точное время как {@code long}
     */
    public static long getAtomicTime() {
        TimeChecker t = new TimeChecker();
        TimeInfo call = t.call();
        call.computeDetails();
        return call.getReturnTime();
    }

    /**
     @return кол-во выделенной, используемой и свободной памяти в МБ
     */
    public static String getMemoryInfo() {
        String msg = (float) Runtime.getRuntime().totalMemory() / MBYTE + " now totalMemory, " +
            (float) Runtime.getRuntime().freeMemory() / MBYTE + " now freeMemory, " +
            (float) Runtime.getRuntime().maxMemory() / MBYTE + " now maxMemory.";
        messageToUser.info(msg);
        return msg;
    }

    /**
     @return имена-паттерны временных файлов, которые надо удалить при запуске.
     */
    public static String[] getStringsVisit() {
        return STRINGS_TODELONSTART;
    }

    /**
     Этот ПК
     <p>

     @return имя компьютера, где запущено
     */
    public static String thisPC() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException | ExceptionInInitializerError | NullPointerException e) {
            String retStr = new TForms().fromArray((List<?>) e, false);
            FileSystemWorker.writeFile("this_pc.err", Collections.singletonList(retStr));
            return "pc";
        }
    }

    /**
     Парсинг и проверка уникальности для new {@link Visitor}

     @param request {@link HttpServletRequest}
     @return {@link Visitor}
     */
    public static Visitor getVis(HttpServletRequest request) {
        try {
            return thisVisit(request.getSession().getId());
        } catch (Exception e) {
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

    @Bean
    @Scope(SINGLETON)
    static ConfigurableApplicationContext configurableApplicationContext() {
        return getConfigurableApplicationContext();
    }

    /**
     Тащит {@link #PROPS} из БД или файла
     <p>
     {@link ThreadConfig#thrNameSet(java.lang.String)} <br>
     */
    static Properties takePr(boolean fromFile) {
        AppComponents.threadConfig().thrNameSet("gProps");

        InitProperties initProperties = new DBRegProperties(APPNAME_WITHMINUS + ConstantsFor.class.getSimpleName());
        Properties retPr = new Properties();
        File prFile = new File(ConstantsFor.class.getSimpleName() + FILEEXT_PROPERTIES);
        StringBuilder stringBuilder = new StringBuilder();
        String classMeth = "ConstantsFor.takePr";

        if (fromFile || new File("ff").exists()) {
            try (InputStream inputStream = new FileInputStream(prFile)) {
                retPr.load(inputStream);
                stringBuilder.append("fromFile: ").append(fromFile).append("\n");
                stringBuilder.append("File \"ff\": ").append(new File("ff").exists()).append("\n");
            } catch (IOException e) {
                stringBuilder.append(e.getMessage()).append("\n").append(new TForms().fromArray(e, false)).append("\n");
                FileSystemWorker.error(classMeth, e);
            }
        } else {
            retPr = initProperties.getProps();

            try (OutputStream outputStream = new FileOutputStream(prFile)) {
                long millisOf24HRS = TimeUnit.DAYS.toMillis(1);

                if (!prFile.exists() || (System.currentTimeMillis() - millisOf24HRS) > prFile.lastModified()) {
                    retPr.store(outputStream, ConstantsFor.class.getSimpleName() + ".takePr");
                    stringBuilder.append(prFile.getName()).append(" is exist: ").append(!prFile.exists()).append("\n");
                    if (prFile.exists()) {
                        stringBuilder.append(prFile.getName()).append("last modified: ").append(new Date(prFile.lastModified())).append("\n");
                    }
                }
            } catch (IOException e) {
                stringBuilder.append(e.getMessage()).append(new TForms().fromArray(e, false)).append("\n");
                FileSystemWorker.error(classMeth, e);
            }
        }

        PROPS.clear();
        PROPS.putAll(retPr);
        stringBuilder.append(PROPS.size()).append(" is PROPS size, PROPS equals retPr: ").append(PROPS.equals(retPr));
        messageToUser.warn(classMeth, "results", " = " + stringBuilder);
        return retPr;
    }

    /**
     Рассчитывает {@link #DELAY}, на всё время запуска приложения.
     @return {@link #DELAY}
     */
    private static long getDelay() {
        long delay = new SecureRandom().nextInt((int) MY_AGE);
        if (delay < MIN_DELAY) {
            delay = MIN_DELAY;
        } if (thisPC().toLowerCase().contains(HOSTNAME_DO213) || thisPC().toLowerCase().contains(HOSTNAME_HOME)) {
            return MIN_DELAY;
        } else {
            return delay;
        }
    }
}
