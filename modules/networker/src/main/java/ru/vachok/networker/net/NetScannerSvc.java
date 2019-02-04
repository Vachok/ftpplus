package ru.vachok.networker.net;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.messenger.email.ESender;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.ad.ActDirectoryCTRL;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.systray.ActionCloseMsg;
import ru.vachok.networker.systray.ActionDefault;
import ru.vachok.networker.systray.MessageToTray;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;


/**
 Управление сервисами LAN-разведки.
 <p>

 @since 21.08.2018 (14:40) */
@SuppressWarnings("MethodWithMultipleReturnPoints")
@Service(ConstantsNet.STR_NETSCANNERSVC)
public class NetScannerSvc {

    private static final String CLASS_NAME = "NetScannerSvc";

    /**
     {@link LoggerFactory#getLogger(String)}
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CLASS_NAME);

    private static final Properties p = ConstantsFor.getProps();

    /**
     Имя метода, как строка.
     <p>
     {@link NetScannerSvc#getPCsAsync()}
     */
    private static final String METH_GET_PCS_ASYNC = "NetScannerSvc.getPCsAsync";

    /**
     {@link RegRuMysql#getDefaultConnection(String)}
     */
    static Connection c;

    /**
     Компьютеры онлайн
     */
    static int onLinePCs = 0;

    /**
     Неиспользуемые имена ПК

     @see #getPCNamesPref(String)
     */
    private static Collection<String> unusedNames = new TreeSet<>();

    /**
     new {@link NetScannerSvc}
     */
    private static NetScannerSvc netScannerSvc = new NetScannerSvc();

    private long stArt;

    /**
     Статистика скана.
     <p>
     */
    private Runnable runAfterAll = this::runAfterAllScan;

    /**
     /netscan POST форма
     <p>

     @see NetScanCtr {@link }
     */
    private String thePc = "PC";

    private String thrName = Thread.currentThread().getName();

    /**
     {@link AppComponents#lastNetScan()}
     */
    private Map<String, Boolean> netWork;

    /**
     @return {@link #onLinePCs}
     */
    int getOnLinePCs() {
        return onLinePCs;
    }

    /**
     Выполняет запрос в БД по-пользовательскому вводу
     <p>
     Устанавливает {@link ActDirectoryCTRL#queryStringExists(java.lang.String, org.springframework.ui.Model)}

     @return web-страница с результатом
     */
    public static String getInfoFromDB() {
        StringBuilder sqlQBuilder = new StringBuilder();

        String thePcLoc = NetScannerSvc.getI().getThePc();
        if (thePcLoc.isEmpty()) {
            IllegalArgumentException argumentException = new IllegalArgumentException("Must be NOT NULL!");
            sqlQBuilder.append(argumentException.getMessage());
        } else {
            sqlQBuilder.append("select * from velkompc where NamePP like '%").append(thePcLoc).append("%'");
        }
        try (PreparedStatement preparedStatement = c.prepareStatement(sqlQBuilder.toString())) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                List<String> timeNow = new ArrayList<>();
                List<Integer> integersOff = new ArrayList<>();
                while (resultSet.next()) {
                    int onlineNow = resultSet.getInt(ConstantsNet.ONLINE_NOW);
                    if (onlineNow == 1) {
                        timeNow.add(resultSet.getString("TimeNow"));
                    } else {
                        integersOff.add(onlineNow);
                    }
                    StringBuilder stringBuilder = new StringBuilder();
                    String namePP = "<center><h2>" + resultSet.getString("NamePP") +
                        " information.<br></h2>" +
                        "<font color = \"silver\">OnLines = " +
                        timeNow.size() +
                        ". Offlines = " +
                        integersOff.size() +
                        ". TOTAL: " + (integersOff.size() + timeNow.size());
                    stringBuilder
                        .append(namePP)
                        .append(". <br>");
                    NetScannerSvc.getI().setThePc(stringBuilder.toString());
                }
                sortList(timeNow);
            }
        } catch (SQLException e) {
            c = reconnectToDB();
        } catch (IndexOutOfBoundsException e) {
            NetScannerSvc.getI().setThePc(e.getMessage() + " " + new TForms().fromArray(e, false));
        }
        return "ok";
    }

    /**
     @return {@link #netScannerSvc}
     */
    public static synchronized NetScannerSvc getI() {
        return netScannerSvc;
    }

    /**
     Выполняет {@link #getPCsAsync()}.
     <p>

     @return {@link ConstantsNet#PC_NAMES}
     @see #getPCNamesPref(String)
     @see NetScanCtr#scanIt(HttpServletRequest, Model, Date)
     */
    Set<String> getPcNames() {
        ThreadPoolTaskExecutor executor = AppComponents.threadConfig().threadPoolTaskExecutor();
        Runnable getPCs = this::getPCsAsync;
        executor.execute(getPCs);
        return ConstantsNet.PC_NAMES;
    }

    /**
     @return атрибут модели.
     */
    @SuppressWarnings("WeakerAccess")
    public String getThePc() {
        return thePc;
    }

    /**
     {@link #thePc}

     @param thePc имя ПК
     */
    public void setThePc(String thePc) {
        this.thePc = thePc;
    }

    static {
        try {
            c = new RegRuMysql().getDefaultConnection(ConstantsNet.DB_NAME);
        } catch (Exception e) {
            c = reconnectToDB();
        }
    }

    /**
     @see AppComponents#lastNetScanMap()
     */
    private NetScannerSvc() {
        this.netWork = AppComponents.lastNetScanMap();
    }

    private static void sortList(List<String> timeNow) {
        Collections.sort(timeNow);
        String str = timeNow.get(timeNow.size() - 1);
        String thePcWithDBInfo = new StringBuilder().append(NetScannerSvc.getI().getThePc()).append("Last online: ")
            .append(str).append(" (").append(")<br>Actual on: ").toString();
        thePcWithDBInfo = thePcWithDBInfo + AppComponents.lastNetScan().getTimeLastScan() + "</center></font>";
        NetScannerSvc.getI().setThePc(thePcWithDBInfo);
        ActDirectoryCTRL.setInputWithInfoFromDB(thePcWithDBInfo);

    }

    /**
     Обнуление счётчика онлайн ПК.
     <p>
     Устанавливает {@link #p} {@link ConstantsNet#ONLINEPC} в "". <br> Устававливает {@link NetScannerSvc#onLinePCs} = 0.

     @see #runAfterAll
     */
    private static void setOnLinePCsToZero() {
        new MessageCons().errorAlert("NetScannerSvc.setOnLinePCsToZero");
        p.setProperty(ConstantsNet.ONLINEPC, onLinePCs + "");
        NetScannerSvc.onLinePCs = 0;
    }

    /**
     Запись в таблицу <b>velkompc</b> текущего состояния. <br>
     <p>
     1 {@link TForms#fromArray(List, boolean)}

     @return строка в html-формате
     @see #getPCNamesPref(String)
     */
    @SuppressWarnings({"OverlyComplexMethod", "OverlyLongLambda", "OverlyLongMethod"})
    private static String writeDB() {
        List<String> list = new ArrayList<>();
        try (PreparedStatement p = c.prepareStatement("insert into  velkompc (NamePP, AddressPP, SegmentPP , OnlineNow) values (?,?,?,?)")) {
            ConstantsNet.PC_NAMES.stream().sorted().forEach(x -> {
                String pcSerment = "Я не знаю...";
                ConstantsNet.LOGGER.info(x);
                if (x.contains("200.200")) {
                    pcSerment = "Торговый дом";
                }
                if (x.contains("200.201")) {
                    pcSerment = "IP телефоны";
                }
                if (x.contains("200.202")) {
                    pcSerment = "Техслужба";
                }
                if (x.contains("200.203")) {
                    pcSerment = "СКУД";
                }
                if (x.contains("200.204")) {
                    pcSerment = "Упаковка";
                }
                if (x.contains("200.205")) {
                    pcSerment = "МХВ";
                }
                if (x.contains("200.206")) {
                    pcSerment = "Здание склада 5";
                }
                if (x.contains("200.207")) {
                    pcSerment = "Сырокопоть";
                }
                if (x.contains("200.208")) {
                    pcSerment = "Участок убоя";
                }
                if (x.contains("200.209")) {
                    pcSerment = "Да ладно?";
                }
                if (x.contains("200.210")) {
                    pcSerment = "Мастера колб";
                }
                if (x.contains("200.212")) {
                    pcSerment = "Мастера деликатесов";
                }
                if (x.contains("200.213")) {
                    pcSerment = "2й этаж. АДМ.";
                }
                if (x.contains("200.214")) {
                    pcSerment = "WiFiCorp";
                }
                if (x.contains("200.215")) {
                    pcSerment = "WiFiFree";
                }
                if (x.contains("200.217")) {
                    pcSerment = "1й этаж АДМ";
                }
                if (x.contains("192.168")) {
                    pcSerment = "Может быть в разных местах...";
                }
                if (x.contains("172.16.200")) {
                    pcSerment = "Open VPN авторизация - сертификат";
                }
                boolean onLine = false;
                try {
                    if (x.contains("true")) {
                        onLine = true;
                    }
                    String x1 = x.split(":")[0];
                    p.setString(1, x1);
                    String x2 = x.split(":")[1];
                    p.setString(2, x2.split("<")[0]);
                    p.setString(3, pcSerment);
                    p.setBoolean(4, onLine);
                    p.executeUpdate();
                    list.add(x1 + " " + x2 + " " + pcSerment + " " + onLine);
                } catch (SQLException e) {
                    c = reconnectToDB();
                    String writeDB = "writeDB";
                    new MessageCons().errorAlert(CLASS_NAME, writeDB, e.getMessage());
                }
            });
            return new TForms().fromArray(list, true);
        } catch (SQLException e) {
            return e.getMessage();
        }
    }

    /**
     Реконнект к БД
     */
    private static Connection reconnectToDB() {
        c = null;
        Connection connection = new RegRuMysql().getDefaultConnection(ConstantsFor.U_0466446_VELKOM);
        c = connection;
        return connection;
    }

    /**
     Сканирование с определённым префиксом.
     <p>
     1. {@link #getCycleNames(String)} создаёт имена, для конкретного префикса. <br>
     <i>ПК офлайн:</i> <br>
     2. {@link #pcNameUnreach(String, InetAddress)}. Если комп не пингуется. Добавить в {@link #netWork}. <br>
     <i>ПК он-лайн:</i> <br>
     3. {@link MoreInfoGetter#getSomeMore(String, boolean)}. Когда копм онлайн. Получает последний известный username. 4. {@link MoreInfoGetter#getSomeMore(String, boolean)} получает статистику
     (сколько online, сколько offline) <br> Создаётся ссылка {@code a href=\"/ad?"<b>имя</b>/a}. Добавляет в {@link #netWork} put форматированную строку {@code printStr, true} <br> Выводит в консоль
     через {@link #LOGGER} строку {@code printStr}. <br> Добавляет в {@link ConstantsNet#PC_NAMES}, имя, ip и {@code online true}. <br> При возникновении {@link IOException}, например если имя ПК не
     существует, добавляет {@code getMessage} в {@link #unusedNames}
     <p>
     <i>По завершении цикла:</i> <br>
     {@link #netWork} put префикс, кол-во 5. {@link #writeDB()}. записывает в базу.
     <p>

     @param prefixPcName префикс имени ПК. {@link ConstantsNet#PC_PREFIXES}
     @return состояние запрошенного сегмента
     @see NetScanCtr#scanIt(HttpServletRequest, Model, Date)
     @see #getPCsAsync()
     */
    Set<String> getPCNamesPref(String prefixPcName) {
        final long startMethTime = System.currentTimeMillis();
        boolean reachable;
        InetAddress byName;
        Thread.currentThread().setPriority(8);
        for (String pcName : getCycleNames(prefixPcName)) {
            try {
                byName = InetAddress.getByName(pcName);
                reachable = byName.isReachable(ConstantsFor.TIMEOUT_650);
                if (!reachable) {
                    pcNameUnreach(pcName, byName);
                } else {
                    String onOffCounterAndLastUser = new StringBuilder().append("<i><font color=\"yellow\">last name is ")
                        .append(MoreInfoGetter.getSomeMore(pcName, false)).append("</i></font> ")
                        .append(MoreInfoGetter.getSomeMore(pcName, true))
                        .toString();
                    String pcOnline = new StringBuilder()
                        .append(" online ")
                        .append(true)
                        .append("<br>").toString();
                    String strToConsole = MessageFormat.format("{0} {1} | {2}", pcName, pcOnline, onOffCounterAndLastUser);
                    String printStr = new StringBuilder().append("<br><b><a href=\"/ad?")
                        .append(pcName.split(".eatm")[0]).append("\" >")
                        .append(pcName).append("</b></a>     ")
                        .append(onOffCounterAndLastUser).append(". ")
                        .toString();

                    netWork.put(printStr, true);
                    ConstantsNet.PC_NAMES.add(pcName + ":" + byName.getHostAddress() + pcOnline);
                    LOGGER.info(strToConsole);
                }
            } catch (IOException e) {
                unusedNames.add(e.getMessage());
            }
        }
        netWork.put("<h4>" + prefixPcName + "     " + ConstantsNet.PC_NAMES.size() + "</h4>", true);
        String pcsString = writeDB();
        ConstantsNet.LOGGER.info(pcsString);
        String elapsedTime = "<b>Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startMethTime) + " sec.</b> " + LocalTime.now();
        ConstantsNet.PC_NAMES.add(elapsedTime);
        return ConstantsNet.PC_NAMES;
    }

    /**
     Основной скан-метод.
     <p>

     @see #getPcNames()
     */
    @SuppressWarnings({"OverlyLongLambda"})
    private void getPCsAsync() {
        ExecutorService eServ = Executors.unconfigurableExecutorService(Executors.newFixedThreadPool(ConstantsNet.N_THREADS * 5));
        AtomicReference<String> msg = new AtomicReference<>("");
        this.stArt = System.currentTimeMillis();
        new MessageCons().errorAlert(METH_GET_PCS_ASYNC);
        boolean fileCreate = fileCreate(true);
        new MessageToTray(new ActionCloseMsg(AppComponents.getLogger()))
            .info("NetScannerSvc started scan", ConstantsFor.getUpTime(), " File: " + fileCreate);
        eServ.submit(() -> {
            msg.set(new StringBuilder()
                .append("Thread id ")
                .append(Thread.currentThread().getId())
                .append(" name ")
                .append(Thread.currentThread().getName())
                .toString());

            ConstantsNet.LOGGER.warn(msg.get());

            for (String s : ConstantsNet.PC_PREFIXES) {
                this.thrName = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - stArt) + "-sec";
                ConstantsNet.PC_NAMES.clear();
                ConstantsNet.PC_NAMES.addAll(getPCNamesPref(s));
                Thread.currentThread().setName(thrName);
            }
            String elapsedTime = "Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - stArt) + " sec.";
            ConstantsNet.PC_NAMES.add(elapsedTime);
            ConstantsNet.LOGGER.warn(msg.get());
            eServ.submit(runAfterAll);
        });
    }

    /**
     Создание lock-файла
     <p>

     @param create создать или удалить файл.
     @return scan.tmp exist
     @see #getPCsAsync()
     */
    private boolean fileCreate(boolean create) {
        File file = new File("scan.tmp");
        try {
            if (create) {
                file = Files.createFile(file.toPath()).toFile();
            } else Files.deleteIfExists(Paths.get("scan.tmp"));
        } catch (IOException e) {
            FileSystemWorker.error("NetScannerSvc.fileCreate", e);
            return false;
        }
        return file.exists();
    }

    /**
     Подсчёт статистики по {@link ConstantsNet#VELKOM_PCUSERAUTO_TXT}
     <p>
     {@link List} readFileAsList - читает по-строкам {@link ConstantsNet#VELKOM_PCUSERAUTO_TXT}. <br>
     */
    private void countStat() {
        List<String> readFileAsList = new ArrayList<>();
        try (InputStream inputStream = new FileInputStream(ConstantsNet.VELKOM_PCUSERAUTO_TXT);
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            while (inputStreamReader.ready()) {
                readFileAsList.add(bufferedReader.readLine().split("\\Q0) \\E")[1]);
            }
        } catch (IOException e) {
            new MessageCons().errorAlert(CLASS_NAME, "countStat", e.getMessage());
        }
        FileSystemWorker.recFile("pcautodis.txt", readFileAsList.parallelStream().distinct());
        String valStr = FileSystemWorker.readFile("pcautodis.txt");
        new MessageCons().info(ConstantsFor.SOUTV, "NetScannerSvc.countStat", valStr);
    }

    /**
     1. {@link #getNamesCount(String)}

     @param namePCPrefix префикс имени ПК
     @return обработанные имена ПК, для пинга
     @see #getPCNamesPref(String)
     */
    private Collection<String> getCycleNames(String namePCPrefix) {
        new MessageCons().errorAlert("NetScannerSvc.getCycleNames");
        if (namePCPrefix == null) {
            namePCPrefix = "pp";
        }
        int inDex = getNamesCount(namePCPrefix);
        String nameCount;
        Collection<String> list = new ArrayList<>();
        int pcNum = 0;
        for (int i = 1; i < inDex; i++) {
            if (namePCPrefix.equals("no") || namePCPrefix.equals("pp") || namePCPrefix.equals("do")) {
                nameCount = String.format("%04d", ++pcNum);
            } else {
                nameCount = String.format("%03d", ++pcNum);
            }
            list.add(namePCPrefix + nameCount + ConstantsFor.EATMEAT_RU);
        }
        new MessageCons().info(
            ConstantsFor.STR_INPUT_OUTPUT,
            "namePCPrefix = [" + namePCPrefix + "]",
            "java.util.Collection<java.lang.String>");
        return list;
    }

    /**
     Если ПК не пингуется
     <p>
     Добавить в {@link #netWork} , {@code online = false}.
     <p>
     {@link MoreInfoGetter#getSomeMore(String, boolean)}. Получить более подробную информацию о ПК.
     <p>

     @param pcName имя ПК
     @param byName {@link InetAddress}
     @see #getPCNamesPref(String)
     */
    private void pcNameUnreach(String pcName, InetAddress byName) {
        String someMore = MoreInfoGetter.getSomeMore(pcName, false);
        String onLines = new StringBuilder()
            .append("online ")
            .append(false)
            .append("<br>").toString();
        ConstantsNet.PC_NAMES.add(pcName + ":" + byName.getHostAddress() + " " + onLines);
        String format = MessageFormat.format("{0} {1} | {2}", pcName, onLines, someMore);
        netWork.put(pcName + " last name is " + someMore, false);
        ConstantsNet.LOGGER.warn(format);
    }

    /**
     @param qer префикс имени ПК
     @return кол-во ПК, для пересичления
     @see #getCycleNames(String)
     */
    private int getNamesCount(String qer) {
        int inDex = 0;
        if (qer.equals("no")) {
            inDex = ConstantsNet.NOPC;
        }
        if (qer.equals("pp")) {
            inDex = ConstantsNet.PPPC;
        }
        if (qer.equals("do")) {
            inDex = ConstantsNet.DOPC;
        }
        if (qer.equals("a")) {
            inDex = ConstantsNet.APC;
        }
        if (qer.equals("td")) {
            inDex = ConstantsNet.TDPC;
        }
        return inDex;
    }

    /**
     Статистика по-сканированию.
     <p>
     <i>Переменные:</i> <br>
     1. float upTime. Время в минутах, после начала сканирования. {@link #getPCsAsync()} <br> 2.
     */
    private void runAfterAllScan() {
        float upTime = (float) (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - stArt)) / ConstantsFor.ONE_HOUR_IN_MIN;
        List<String> toFileList = new ArrayList<>();
        MessageToUser mailMSG = new ESender(ConstantsFor.GMAIL_COM);

        String compNameUsers = new TForms().fromArray(ConstantsNet.COMPNAME_USERS_MAP, false);
        String psUser = new TForms().fromArrayUsers(ConstantsNet.PC_U_MAP, false);
        String msgTimeSp = "NetScannerSvc.getPCsAsync method. " + (float) (System.currentTimeMillis() - stArt) / 1000 + ConstantsFor.STR_SEC_SPEND;
        String valueOfPropLastScan = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY) + "";

        toFileList.add(compNameUsers);
        toFileList.add(psUser);
        toFileList.add(msgTimeSp);
        toFileList.add(new TForms().fromArray(p, false));

        p.setProperty(ConstantsNet.PR_LASTSCAN, valueOfPropLastScan);
        ConstantsFor.saveProps(p);

        new MessageToTray(new ActionDefault(ConstantsNet.HTTP_LOCALHOST_8880_NETSCAN))
            .info(
                "Netscan complete!",
                "Online: " + onLinePCs,
                upTime + " min uptime.");

        NetScannerSvc.setOnLinePCsToZero();
        AppComponents.lastNetScan().setTimeLastScan(new Date());
        countStat();
        mailMSG
            .info(
                this.getClass().getSimpleName(),
                "getPCsAsync " + ConstantsFor.getUpTime() + " " + ConstantsFor.thisPC(),
                " scan.tmp exist." + fileCreate(false) + "\n" + new TForms().fromArray(toFileList, false));

        FileSystemWorker.recFile(this.getClass().getSimpleName() + ".getPCsAsync", toFileList);
        FileSystemWorker.recFile("unused.ips", unusedNames.stream());
        FileSystemWorker.recFile("lastnetscan", new TForms().fromArray(AppComponents.lastNetScanMap(), false));
    }

    @Override
    public int hashCode() {
        int result = getThePc().hashCode();
        result = 31 * result + thrName.hashCode();
        result = 31 * result + (netWork != null ? netWork.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NetScannerSvc)) {
            return false;
        }

        NetScannerSvc that = (NetScannerSvc) o;

        if (!getThePc().equals(that.getThePc())) {
            return false;
        }
        if (!thrName.equals(that.thrName)) {
            return false;
        }
        return netWork != null ? netWork.equals(that.netWork) : that.netWork == null;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NetScannerSvc{");
        sb.append("c=").append(c);
        sb.append(", CLASS_NAME='").append(CLASS_NAME).append('\'');
        sb.append(", infoFromDB='").append(getInfoFromDB()).append('\'');
        sb.append(", netWork=").append(netWork);
        sb.append(", onLinePCs=").append(onLinePCs);
        sb.append(", p=").append(p);
        sb.append(", thePc='").append(thePc).append('\'');
        sb.append(", thrName='").append(thrName).append('\'');
        sb.append(", unusedNames=").append(unusedNames);
        sb.append('}');
        return sb.toString();
    }
}
