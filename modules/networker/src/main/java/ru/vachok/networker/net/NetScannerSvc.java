package ru.vachok.networker.net;


import org.slf4j.Logger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.ad.ActDirectoryCTRL;
import ru.vachok.networker.ad.PCUserResolver;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.services.TimeChecker;
import ru.vachok.networker.systray.ActionDefault;
import ru.vachok.networker.systray.MessageToTray;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;


/**
 Управление сервисами LAN-разведки.
 <p>

 @since 21.08.2018 (14:40) */
@SuppressWarnings("MethodWithMultipleReturnPoints")
@Service(ConstantsNet.STR_NETSCANNERSVC)
public class NetScannerSvc {


    private static final String CLASS_NAME = "NetScannerSvc";

    private static final Logger LOGGER = AppComponents.getLogger();

    private static final Properties p = ConstantsFor.getProps();

    /**
     {@link RegRuMysql#getDefaultConnection(String)}
     */
    static Connection c;

    /**
     Компьютеры онлайн
     */
    static int onLinePCs = 0;

    private static Collection<String> unusedIPs = new TreeSet<>();

    /**
     new {@link NetScannerSvc}
     */
    private static NetScannerSvc netScannerSvc = null;

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
     {@link #thePc}

     @param thePc имя ПК
     */
    public void setThePc(String thePc) {
        this.thePc = thePc;
    }

    /**
     @return {@link #netScannerSvc}
     */
    public static NetScannerSvc getI() {
        if (netScannerSvc == null) {
            synchronized (NetScannerSvc.class) {
                if (netScannerSvc == null) {
                    netScannerSvc = new NetScannerSvc();
                    netScannerSvc.countStat();
                }
            }
        }
        return netScannerSvc;
    }

    /**
     @return атрибут модели.
     */
    @SuppressWarnings("WeakerAccess")
    public String getThePc() {
        return thePc;
    }

    /**
     Выполняет запрос в БД по-пользовательскому вводу <br> Устанавливает {@link ActDirectoryCTRL#queryStringExists(java.lang.String, org.springframework.ui.Model)}

     @return web-страница с результатом
     */
    public String getInfoFromDB() {
        new MessageCons().errorAlert("NetScannerSvc.getInfoFromDB");
        StringBuilder sqlQBuilder = new StringBuilder();

        if (thePc.isEmpty()) {
            IllegalArgumentException argumentException = new IllegalArgumentException("Must be NOT NULL!");
            sqlQBuilder.append(argumentException.getMessage());
        } else {
            sqlQBuilder.append("select * from velkompc where NamePP like '%").append(thePc).append("%'");
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
                    setThePc(stringBuilder.toString());
                }
                Collections.sort(timeNow);
                String str = timeNow.get(timeNow.size() - 1);
                String thePcWithDBInfo = new StringBuilder()
                    .append(getThePc())
                    .append("Last online: ")
                    .append(str)
                    .append(" (")
                    .append(")<br>Actual on: ").toString();
                thePcWithDBInfo = thePcWithDBInfo + AppComponents.lastNetScan().getTimeLastScan() + "</center></font>";
                setThePc(thePcWithDBInfo);
                ActDirectoryCTRL.setInputWithInfoFromDB(thePcWithDBInfo);
            }
        } catch (SQLException e) {
            reconnectToDB();
        } catch (IndexOutOfBoundsException e) {
            setThePc(e.getMessage() + " " + new TForms().fromArray(e, false));
        }
        return "ok";
    }

    /**
     @return {@link #onLinePCs}
     */
    int getOnLinePCs() {
        return onLinePCs;
    }

    /**
     1 {@link ThreadConfig#threadPoolTaskExecutor()}

     @return {@link ConstantsNet#PC_NAMES}
     @see NetScanCtr#scanIt(HttpServletRequest, Model)
     */
    Set<String> getPcNames() {
        ThreadPoolTaskExecutor executor = ConstantsNet.TASK_EXECUTOR;
        Runnable getPCs = this::getPCsAsync;
        executor.execute(getPCs);
        return ConstantsNet.PC_NAMES;
    }

    static {
        try {
            c = new RegRuMysql().getDefaultConnection(ConstantsNet.DB_NAME);
        } catch (Exception e) {
            c = new RegRuMysql().getDefaultConnection(ConstantsNet.DB_NAME);
        }
    }

    /**
     @see AppComponents#lastNetScanMap()
     */
    private NetScannerSvc() {
        this.netWork = AppComponents.lastNetScanMap();
    }

    /**
     Реконнект к БД
     */
    public static void reconnectToDB() {
        Connection connection = new RegRuMysql().getDefaultConnection(ConstantsFor.U_0466446_VELKOM);
        try {
            connection.clearWarnings();
        } catch (SQLException e) {
            new MessageCons().errorAlert(CLASS_NAME, "reconnectToDB", e.getMessage());
        }
        c = connection;
    }

    private static void setOnLinePCsToZero() {
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
                    reconnectToDB();
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
     Сборщик для {@link ConstantsNet#PC_NAMES} <br> 1. {@link #getCycleNames(String)} 1.1 {@link #getNamesCount(String)} <br> 2. {@link MoreInfoGetter#getSomeMore(String, boolean)} 2.1 {@link
    MoreInfoGetter#onLinesCheck(String, String)} 2 .1.1 {@link ThreadConfig#threadPoolTaskExecutor()} 2.1.2 {@link PCUserResolver#namesToFile(String)} <br> 2.2 {@link
    MoreInfoGetter#offLinesCheckUser(String, String)} <br> 3. {@link MoreInfoGetter#getSomeMore(String, boolean)} 3.1 {@link MoreInfoGetter#onLinesCheck(String, String)} 3.1.1 {@link
    ThreadConfig#threadPoolTaskExecutor()} 3.1.2 {@link PCUserResolver#namesToFile(String)} <br> 4. {@link MoreInfoGetter#getSomeMore(String, boolean)} 4.1 {@link
    ThreadConfig#threadPoolTaskExecutor()} 4.1.2 {@link PCUserResolver#namesToFile(String)} 4.2 {@link MoreInfoGetter#offLinesCheckUser(String, String)} <br> 5. {@link #writeDB()} 5.1 {@link
    TForms#fromArray(List, boolean)}

     @param prefixPcName префикс имени ПК
     @return {@link ConstantsNet#PC_NAMES}
     @see NetScanCtr#scanIt(HttpServletRequest, Model)
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
                    String someMore = new StringBuilder().append("<i><font color=\"yellow\">last name is ")
                        .append(MoreInfoGetter.getSomeMore(pcName, false)).append("</i></font> ")
                        .append(MoreInfoGetter.getSomeMore(pcName, true))
                        .toString();
                    String onLines = new StringBuilder()
                        .append(" online ")
                        .append(true)
                        .append("<br><br>").toString();
                    String format = MessageFormat.format("{0} {1} | {2}", pcName, onLines, someMore);
                    ConstantsNet.PC_NAMES.add(pcName + ":" + byName.getHostAddress() + onLines);
                    String printStr = new StringBuilder().append("<br><b><a href=\"/ad?")
                        .append(pcName.split(".eatm")[0]).append("\" >")
                        .append(pcName).append("</b></a>     ")
                        .append(someMore).append(". ")
                        .toString();
                    netWork.putIfAbsent(printStr, true);
                    ConstantsNet.LOGGER.info(format);
                }
            } catch (IOException e) {
                unusedIPs.add(e.getMessage());
            }
        }
        netWork.put("<h4>" + prefixPcName + "     " + ConstantsNet.PC_NAMES.size() + "</h4>", true);
        String pcsString = writeDB();
        ConstantsNet.LOGGER.info(pcsString);
        String e = "<b>Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startMethTime) + " sec.</b> " + LocalTime.now();
        ConstantsNet.PC_NAMES.add(e);
        return ConstantsNet.PC_NAMES;
    }

    String someInfo() {
        StringBuilder stringBuilder = new StringBuilder();
        String str = new TimeChecker().call().getMessage().toString();
        stringBuilder
            .append("</font><p> new TimeChecker().call():<br> <font color=\"yellow\">")
            .append(str)
            .append("</font>");
        tryKillSleepTHR(str);
        return stringBuilder.toString();
    }

    /**
     1. {@link #getNamesCount(String)}

     @param namePCPrefix префикс имени ПК
     @return обработанные имена ПК, для пинга
     @see #getPCNamesPref(String)
     */
    private Collection<String> getCycleNames(String namePCPrefix) {
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
        return list;
    }

    private void countStat() {
        List<String> readFileAsList = new ArrayList<>();
        try (InputStream inputStream = new FileInputStream(ConstantsFor.VELKOM_PCUSERAUTO_TXT);
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            while (inputStreamReader.ready()) {
                readFileAsList.add(bufferedReader.readLine().split("\\Q0) \\E")[1]);
            }
        } catch (IOException e) {
            ConstantsNet.LOGGER.error(e.getMessage(), e);
        }
        List<String> disStringsToWrite = readFileAsList.parallelStream().distinct().collect(Collectors.toList());
        FileSystemWorker.recFile("pcautodis.txt", disStringsToWrite);
        String valStr = "disStringsToWrite = " + new TForms().fromArray(disStringsToWrite, false) + " NetScannerSvc.countStat";
        new MessageCons().info("SOUTV", "NetScannerSvc.countStat", valStr);
    }

    private void tryKillSleepTHR(String str) {
        Thread.currentThread().checkAccess();
        for (Map.Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
            Thread x = entry.getKey();
            if (x.getState().equals(Thread.State.WAITING) && x.getName().contains("eatmeat.ru")) {
                x.checkAccess();
                x.interrupt();
                ConstantsNet.TASK_EXECUTOR.destroy();
                String s = new StringBuilder()
                    .append(str)
                    .append("\n\n\n")
                    .append(new TForms().fromArray(x.getStackTrace(), false))
                    .append("\n").append(x.isAlive()).append(" isAlive. Total active = ")
                    .append(Thread.activeCount()).toString();
                String name = x.getName() + "log.txt";
                try (OutputStream outputStream = new FileOutputStream(name)) {
                    outputStream.write(s.getBytes());
                } catch (IOException e) {
                    ConstantsNet.LOGGER.info(e.getMessage());
                }
            }
        }
    }

    /**
     Сканирующий метод. Запускает отдельный {@link Thread}, который блокируется с помощью {@link ReentrantLock} <br> 1 {@link #getPCNamesPref(String)} 1 .1 {@link #getCycleNames(String)} 1.1.1 {@link
    #getNamesCount(String)} 1.2 {@link MoreInfoGetter#getSomeMore(String, boolean)} 1.2.1 {@link MoreInfoGetter#onLinesCheck(String, String)} 1.2.1.1 {@link ThreadConfig#threadPoolTaskExecutor()}
     1.2.1.2 {@link PCUserResolver#namesToFile(String)} 1.2.2 {@link MoreInfoGetter#offLinesCheckUser(String, String)} 1.3 {@link MoreInfoGetter#getSomeMore(String, boolean)} 1.3.1 {@link
    MoreInfoGetter#onLinesCheck(String, String)} 1.3.1.1 {@link ThreadConfig#threadPoolTaskExecutor()} 1.3.1.2 {@link PCUserResolver#namesToFile(String)} 1.4 {@link MoreInfoGetter#getSomeMore(String,
        boolean)} 1.4.1 {@link MoreInfoGetter#onLinesCheck(String, String)} 1.4.1.1 {@link ThreadConfig#threadPoolTaskExecutor()} 1.4.1.2 {@link PCUserResolver#namesToFile(String)} 1.4.2 {@link
    MoreInfoGetter#offLinesCheckUser(String, String)} 1.5 {@link #writeDB()} 1.5.1 {@link TForms#fromArray(List, boolean)} <br>
     <p>
     2 {@link TForms#fromArray(Map)} <br>
     <p>
     3 {@link TForms#fromArray(java.util.concurrent.ConcurrentMap, boolean)} <br>
     <p>
     4 {@link TForms#fromArrayUsers(ConcurrentMap, boolean)}

     @see #getPcNames()
     */
    @SuppressWarnings({"OverlyLongLambda", "OverlyLongMethod"})
    private void getPCsAsync() {
        ExecutorService eServ = Executors.unconfigurableExecutorService(Executors.newFixedThreadPool(ConstantsNet.N_THREADS));
        AtomicReference<String> msg = new AtomicReference<>("");
        final long stArt = System.currentTimeMillis();
        new MessageCons().errorAlert("NetScannerSvc.getPCsAsync");

        List<String> toFileList = new ArrayList<>();
        eServ.submit(() -> {
            msg.set(new StringBuilder()
                .append("Thread ")
                .append(Thread.currentThread().getId())
                .append(" with name ")
                .append(Thread.currentThread().getName())
                .append(" is locked = ").toString());
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
            toFileList.add(msg.get());

            Runnable runAfterAll = () -> {
                Thread.currentThread().setName("runAfterAll");
                MessageToUser mailMSG = new MessageCons();
                float upTime = (float) (TimeUnit.MILLISECONDS
                    .toSeconds(System.currentTimeMillis() - ConstantsFor.START_STAMP)) / ConstantsFor.ONE_HOUR_IN_MIN;
                String compNameUsers = new TForms().fromArray(ConstantsFor.COMPNAME_USERS_MAP, false);
                String psUser = new TForms().fromArrayUsers(ConstantsFor.PC_U_MAP, false);
                String s1 = new StringBuilder().append(ConstantsFor.getMemoryInfo()).append("\n\n").append(psUser).append("\n").append(compNameUsers).toString();
                String s2 = " min uptime. ";
                String s3 = " Online: ";
                String activeThreadsNow = Thread.activeCount() + " active threads now.";
                String valStr = "activeThreadsNow = " + activeThreadsNow;
                new MessageCons().info(Thread.currentThread().getName(), "NetScannerSvc.getPCsAsync", valStr);
                String msgTimeSp = "NetScannerSvc.getPCsAsync method. " + (float) (System.currentTimeMillis() - stArt) / 1000 + ConstantsFor.STR_SEC_SPEND;

                toFileList.add(s1);
                toFileList.add(msgTimeSp);
                toFileList.add(new TForms().fromArray(ConstantsNet.LOC_PROPS, false));
                toFileList.add(ConstantsFor.getMemoryInfo());

                ConstantsFor.saveProps(ConstantsNet.LOC_PROPS);
                new MessageToTray(new ActionDefault(ConstantsNet.HTTP_LOCALHOST_8880_NETSCAN))
                    .info(
                        "Netscan complete!",
                        s3 + onLinePCs,
                        (float) (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - stArt)) / ConstantsFor.ONE_HOUR_IN_MIN + s2);

                NetScannerSvc.setOnLinePCsToZero();
                ConstantsFor.getProps().setProperty(
                    ConstantsNet.PR_LASTSCAN,
                    System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY) + "");

                FileSystemWorker.recFile(this.getClass().getSimpleName() + ".getPCsAsync", toFileList);
                FileSystemWorker.recFile("unused.ips", unusedIPs.stream());
                mailMSG.info(
                    this.getClass().getSimpleName(),
                    "getPCsAsync " + upTime + " " + ConstantsFor.thisPC(),
                    new TForms().fromArray(toFileList, false));

                eServ.shutdown();
            };
            ThreadConfig.executeAsThread(runAfterAll);
        });
    }

    /**
     Если ПК не пингуется
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
        netWork.putIfAbsent(pcName + " last name is " + someMore, false);
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
        new MessageToTray().info("NetScannerSvc.getNamesCount", qer + inDex, "\n" + thrName);
        return inDex;
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
        if (this == o) return true;
        if (!(o instanceof NetScannerSvc)) return false;

        NetScannerSvc that = (NetScannerSvc) o;

        if (!getThePc().equals(that.getThePc())) return false;
        if (!thrName.equals(that.thrName)) return false;
        return netWork != null ? netWork.equals(that.netWork) : that.netWork == null;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NetScannerSvc{");
        sb.append("c=").append(c);
        sb.append(", CLASS_NAME='").append(CLASS_NAME).append('\'');
        sb.append(", infoFromDB='").append(getInfoFromDB()).append('\'');
        sb.append(", netScannerSvc=").append(netScannerSvc);
        sb.append(", netWork=").append(netWork);
        sb.append(", onLinePCs=").append(onLinePCs);
        sb.append(", p=").append(p);
        sb.append(", thePc='").append(thePc).append('\'');
        sb.append(", thrName='").append(thrName).append('\'');
        sb.append(", unusedIPs=").append(unusedIPs);
        sb.append('}');
        return sb.toString();
    }
}
