package ru.vachok.networker.net;


import org.slf4j.Logger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.CustomizableThreadCreator;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.ad.ADComputer;
import ru.vachok.networker.ad.ActDirectoryCTRL;
import ru.vachok.networker.ad.PCUserResolver;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageToTray;
import ru.vachok.networker.services.TimeChecker;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.InetAddress;
import java.sql.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;


/**
 Управление сервисами LAN-разведки.
 <p>

 @since 21.08.2018 (14:40) */
@SuppressWarnings("MethodWithMultipleReturnPoints")
@Service (NetScannerSvc.STR_NETSCANNERSVC)
public class NetScannerSvc {

    private static final int TDPC = 15;

    /**
     {@link ConstantsFor#DB_PREFIX} + velkom
     */
    private static final String DB_NAME = ConstantsFor.U_0466446_VELKOM;

    private static final int APC = 350;

    private static final int DOPC = 250;

    private static final Properties LOC_PROPS = ConstantsFor.getProps();
    /**
     new {@link HashSet}
     */
    private static final Set<String> PC_NAMES = new HashSet<>(Integer.parseInt(LOC_PROPS.getOrDefault(ConstantsFor.PR_TOTPC, "318").toString()));
    private static final int PPPC = 70;

    /**
     Кол-во ноутов NO
     */
    private static final int NOPC = 50;

    /**
     Префиксы имён ПК Велком.
     */
    private static final String[] PC_PREFIXES = {"do", "pp", "td", "no", "a"};

    /**
     <i>Boiler Plate</i>
     */
    private static final String WRITE_DB = ".writeDB";

    private static final String ONLINES_CHECK = ".onLinesCheck";

    private static final String GET_INFO_FROM_DB = ".getInfoFromDB";

    private static final String ONLINE_NOW = "OnlineNow";

    public static final int N_THREADS = 333;


    /**
     /netscan POST форма
     <p>

     @see NetScanCtr {@link }
     */
    private String thePc = "PC";

    /**
     {@link AppComponents#getLogger()}
     */
    private static final Logger LOGGER = AppComponents.getLogger();

    public static final String DB_FIELD_WHENQUERIED = "whenQueried";

    /**
     {@link RegRuMysql#getDefaultConnection(String)}
     */
    private static Connection c;

    /**
     {@link AppComponents#adComputers()}
     */
    private static final List<ADComputer> AD_COMPUTERS = AppComponents.adComputers();


    /**
     new {@link NetScannerSvc}
     */
    private static volatile NetScannerSvc netScannerSvc = null;

    /**
     Компьютеры онлайн
     */
    private int onLinePCs = 0;

    /**
     {@link AppComponents#lastNetScan()}
     */
    private Map<String, Boolean> netWork;

    /**
     * <i>Boiler Plate</i>
     */
    static final String STR_NETSCANNERSVC = "netScannerSvc";

    /**
     {@link ThreadConfig#threadPoolTaskExecutor()}
     */
    private static final ThreadPoolTaskExecutor TASK_EXECUTOR = new ThreadConfig().threadPoolTaskExecutor();

    static {
        try {
            c = new RegRuMysql().getDefaultConnection(DB_NAME);
        } catch (Exception e) {
            c = new RegRuMysql().getDefaultConnection(DB_NAME);
        }
    }

    /**
     @return {@link #netScannerSvc}
     */
    public static NetScannerSvc getI() {
        ConstantsFor.showMem();
        //noinspection DoubleCheckedLocking
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
     Выполняет запрос в БД по-пользовательскому вводу <br> Устанавливает {@link ActDirectoryCTRL#queryStringExists(java.lang.String, org.springframework.ui.Model)}

     @return web-страница с результатом
     */
    public String getInfoFromDB() {
        StringBuilder sql = new StringBuilder();
        if (thePc.isEmpty()) {
            IllegalArgumentException argumentException = new IllegalArgumentException("Must be NOT NULL!");
            sql.append(argumentException.getMessage());
        }
        sql
            .append("select * from velkompc where NamePP like '%")
            .append(thePc)
            .append("%'");
        try (PreparedStatement preparedStatement = c.prepareStatement(sql.toString())) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                List<String> timeNow = new ArrayList<>();
                List<Integer> integersOff = new ArrayList<>();
                while (resultSet.next()) {
                    int onlineNow = resultSet.getInt(ONLINE_NOW);
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
        }
        catch(SQLException e){
            reconnectToDB();
            FileSystemWorker.recFile(
                this.getClass().getSimpleName() +
                    GET_INFO_FROM_DB + ConstantsFor.LOG, Collections.singletonList(new TForms().fromArray(e, false)));
            setThePc(e.getMessage());
        }
        catch(IndexOutOfBoundsException e){
            FileSystemWorker.recFile(
                this.getClass().getSimpleName() +
                    GET_INFO_FROM_DB + ConstantsFor.LOG, Collections.singletonList(new TForms().fromArray(e, false)));
            setThePc(e.getMessage());
        }
        return "ok";
    }

    /**
     Реконнект к БД
     */
    public static void reconnectToDB() {
        final long stArt = System.currentTimeMillis();
        Connection connection = new RegRuMysql().getDefaultConnection(ConstantsFor.U_0466446_VELKOM);
        try{
            connection.clearWarnings();
        }
        catch(SQLException e){
            FileSystemWorker.recFile(
                NetScannerSvc.class.getSimpleName() + ".reconnectToDB" + ConstantsFor.LOG,
                Collections.singletonList(new TForms().fromArray(e, false)));
            LOGGER.error(e.getMessage(), e);
        }
        c = connection;
        String msgTimeSp = new StringBuilder()
            .append("NetScannerSvc.reconnectToDB: ")
            .append(( float ) (System.currentTimeMillis() - stArt) / 1000)
            .append(ConstantsFor.STR_SEC_SPEND)
            .toString();
        LOGGER.info(msgTimeSp);
    }

    /**
     @return {@link #onLinePCs}
     */
    int getOnLinePCs() {
        return onLinePCs;
    }

    /**
     {@link #thePc}

     @param thePc имя ПК
     */
    public void setThePc(String thePc) {
        this.thePc = thePc;
    }

    /**
     1 {@link ThreadConfig#threadPoolTaskExecutor()}

     @return {@link #PC_NAMES}
     @see NetScanCtr#scanIt(HttpServletRequest, Model)
     */
    Set<String> getPcNames() {
        ThreadPoolTaskExecutor executor = TASK_EXECUTOR;
        Runnable getPCs = this::getPCsAsync;
        executor.execute(getPCs);
        return PC_NAMES;
    }

    /**
     @return атрибут модели.
     */
    @SuppressWarnings("WeakerAccess")
    public String getThePc() {
        return thePc;
    }

    /**
     Сканирующий метод. Запускает отдельный {@link Thread}, который блокируется с помощью {@link ReentrantLock} <br> 1 {@link #getPCNamesPref(String)} 1.1 {@link #getCycleNames(String)} 1.1.1 {@link
    #getNamesCount(String)} 1.2 {@link #getSomeMore(String, boolean)} 1.2.1 {@link #onLinesCheck(String, String)} 1.2.1.1 {@link ThreadConfig#threadPoolTaskExecutor()} 1.2.1.2 {@link
    PCUserResolver#namesToFile(String)} 1.2.2 {@link #offLinesCheckUser(String, String)} 1.3 {@link #getSomeMore(String, boolean)} 1.3.1 {@link #onLinesCheck(String, String)} 1.3.1.1 {@link
    ThreadConfig#threadPoolTaskExecutor()} 1.3.1.2 {@link PCUserResolver#namesToFile(String)} 1.4 {@link #getSomeMore(String, boolean)} 1.4.1 {@link #onLinesCheck(String, String)} 1.4.1.1 {@link
    ThreadConfig#threadPoolTaskExecutor()} 1.4.1.2 {@link PCUserResolver#namesToFile(String)} 1.4.2 {@link #offLinesCheckUser(String, String)} 1.5 {@link #writeDB()} 1.5.1 {@link
    TForms#fromArray(List, boolean)} <br>
     <p>
     2 {@link TForms#fromArray(Map)} <br>
     <p>
     3 {@link TForms#fromArray(java.util.concurrent.ConcurrentMap, boolean)} <br>
     <p>
     4 {@link TForms#fromArrayUsers(ConcurrentMap, boolean)}

     @see PCUserResolver#getResolvedName()
     @see #getPcNames()
     */
    @SuppressWarnings("OverlyLongLambda")
    public void getPCsAsync() {
        ExecutorService eServ = Executors.
            unconfigurableExecutorService(Executors.
                newFixedThreadPool(N_THREADS));
        final long stArt = System.currentTimeMillis();
        List<String> toFileList = new ArrayList<>();
        AtomicReference<String> msg = new AtomicReference<>("");
        eServ.submit(() -> {
            msg.set(new StringBuilder()
                .append("Thread ")
                .append(Thread.currentThread().getId())
                .append(" with name ")
                .append(Thread.currentThread().getName())
                .append(" is locked = ").toString());
            final long startMethod = System.currentTimeMillis();
            LOGGER.warn(msg.get());
            for (String s : PC_PREFIXES) {
                PC_NAMES.clear();
                PC_NAMES.addAll(getPCNamesPref(s));
                Thread.currentThread().setName(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - stArt) + "-sec");
            }
            String elapsedTime = "Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startMethod) + " sec.";
            PC_NAMES.add(elapsedTime);
            LOGGER.warn(msg.get());
            toFileList.add(msg.get());
            new Thread(() -> {
                Thread.currentThread().setName("mailMSG");
                MessageToUser mailMSG = new MessageCons();
                float upTime = (float) (TimeUnit.MILLISECONDS
                                            .toSeconds(System.currentTimeMillis() - ConstantsFor.START_STAMP)) / ConstantsFor.ONE_HOUR_IN_MIN;
                Map<String, String> lastLogs = new AppComponents().getLastLogs();
                String retLogs = new TForms().fromArray(lastLogs);
                String fromArray = new TForms().fromArray(ConstantsFor.COMPNAME_USERS_MAP, false);
                String psUser = new TForms().fromArrayUsers(ConstantsFor.PC_U_MAP, false);
                String thisPCStr;
                thisPCStr = ConstantsFor.thisPC();
                //noinspection SpellCheckingInspection
                String s1 = new StringBuilder()
                    .append(ConstantsFor.showMem())
                    .append("\n\n")
                    .append(retLogs).append(" \n")
                    .append(psUser).append("\n").append(fromArray).toString();
                String s2 = " min uptime. ";
                String s3 = " Online: ";
                mailMSG.info(
                    this.getClass().getSimpleName() + s3 + onLinePCs,
                    upTime + s2 + thisPCStr + ConstantsFor.COMPNAME_USERS_MAP_SIZE, s1);
                //noinspection SpellCheckingInspection
                toFileList.add(s1);
                String s = Thread.activeCount() + " active threads now.";
                LOGGER.warn(s);
                ConstantsFor.saveProps(LOC_PROPS);
                toFileList.add(new TForms().fromArray(LOC_PROPS, false));
                toFileList.add(ConstantsFor.showMem());
                String msgTimeSp = "NetScannerSvc.getPCsAsync method. " + ( float ) (System.currentTimeMillis() - stArt) / 1000 + ConstantsFor.STR_SEC_SPEND;
                toFileList.add(msgTimeSp);
                FileSystemWorker.recFile(this.getClass().getSimpleName() + ".getPCsAsync" + ConstantsFor.LOG, toFileList);
                eServ.shutdown();
                new MessageToTray().info("Netscan complete!",
                    s3 + onLinePCs,
                    ( float ) (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - stArt)) / ConstantsFor.ONE_HOUR_IN_MIN + s2);
                this.onLinePCs = 0;
            }).start();
        });
    }

    /**
     Сборщик для {@link #PC_NAMES} <br> 1. {@link #getCycleNames(String)} 1.1 {@link #getNamesCount(String)} <br> 2. {@link #getSomeMore(String, boolean)} 2.1 {@link #onLinesCheck(String, String)} 2
     .1.1 {@link ThreadConfig#threadPoolTaskExecutor()} 2.1.2 {@link PCUserResolver#namesToFile(String)} <br> 2.2 {@link #offLinesCheckUser(String, String)} <br> 3. {@link #getSomeMore(String,
        boolean)} 3.1 {@link #onLinesCheck(String, String)} 3.1.1 {@link ThreadConfig#threadPoolTaskExecutor()} 3.1.2 {@link PCUserResolver#namesToFile(String)} <br> 4. {@link #getSomeMore(String,
        boolean)} 4.1 {@link ThreadConfig#threadPoolTaskExecutor()} 4.1.2 {@link PCUserResolver#namesToFile(String)} 4.2 {@link #offLinesCheckUser(String, String)} <br> 5. {@link #writeDB()} 5.1 {@link
    TForms#fromArray(List, boolean)}

     @param prefixPcName префикс имени ПК
     @return {@link #PC_NAMES}
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
                    String someMore = getSomeMore(pcName, false);
                    String onLines = new StringBuilder()
                        .append("online ")
                        .append(false)
                        .append("<br>").toString();

                    PC_NAMES.add(pcName + ":" + byName.getHostAddress() + " " + onLines);
                    String format = MessageFormat.format("{0} {1} | {2}", pcName, onLines, someMore);
                    netWork.putIfAbsent(pcName + " last name is " + someMore, false);
                    LOGGER.warn(format);
                } else {
                    String someMore = new StringBuilder().append("<i><font color=\"yellow\">last name is ")
                        .append(getSomeMore(pcName, false)).append("</i></font> ")
                        .append(getSomeMore(pcName, true))
                        .toString();
                    String onLines = new StringBuilder()
                        .append(" online ")
                        .append(true)
                        .append("<br><br>").toString();
                    String format = MessageFormat.format("{0} {1} | {2}", pcName, onLines, someMore);
                    PC_NAMES.add(pcName + ":" + byName.getHostAddress() + onLines);
                    String printStr = new StringBuilder().append("<br><b><a href=\"/ad?")
                        .append(pcName.split(".eatm")[0]).append("\" >")
                        .append(pcName).append("</b></a>     ")
                        .append(someMore).append(". ")
                        .toString();
                    netWork.putIfAbsent(printStr, true);
                    LOGGER.info(format);
                }
            } catch (IOException e) {
                Thread.currentThread().interrupt();
            }
        }
        netWork.put("<h4>" + prefixPcName + "     " + PC_NAMES.size() + "</h4>", true);
        String pcsString = writeDB();
        LOGGER.info(pcsString);
        String e = "<b>Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startMethTime) + " sec.</b>";
        PC_NAMES.add(e);
        new MessageToTray().infoNoTitles(e);
        return PC_NAMES;
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

    /**
     Поиск имён пользователей компьютера <br> Обращения: <br> 1 {@link #onLinesCheck(String, String)} 1.1 {@link ThreadConfig#threadPoolTaskExecutor()} 1.2 {@link PCUserResolver#namesToFile(String)}
     <br> 2. {@link #offLinesCheckUser(String, String)}

     @param pcName   имя компьютера
     @param isOnline онлайн = true
     @return выдержка из БД (когда последний раз был онлайн + кол-во проверок) либо хранимый в БД юзернейм (для offlines)
     @see #getPCNamesPref(String)
     */
    private String getSomeMore(String pcName, boolean isOnline) {
        String sql;
        if (isOnline) {
            sql = "select * from velkompc where NamePP like ?";
            this.onLinePCs = this.onLinePCs + 1;
            return onLinesCheck(sql, pcName) + " | " + onLinePCs;
        } else {
            sql = "select * from pcuser where pcName like ?";
            return offLinesCheckUser(sql, pcName);
        }
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
            NetScannerSvc.PC_NAMES.stream().sorted().forEach(x -> {
                String pcSerment = "Я не знаю...";
                LOGGER.info(x);
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
                    FileSystemWorker.recFile(
                        NetScannerSvc.class.getSimpleName() + WRITE_DB + ConstantsFor.LOG,
                        Collections.singletonList(new TForms().fromArray(e, false)));
                }
            });
            return new TForms().fromArray(list, true);
        } catch (SQLException e) {
            FileSystemWorker.recFile(
                NetScannerSvc.class.getSimpleName() +
                    WRITE_DB + ConstantsFor.LOG, Collections.singletonList(new TForms().fromArray(e, false)));
            return e.getMessage();
        }
    }

    /**
     @param qer префикс имени ПК
     @return кол-во ПК, для пересичления
     @see #getCycleNames(String)
     */
    private int getNamesCount(String qer) {
        int inDex = 0;
        if (qer.equals("no")) {
            inDex = NOPC;
        }
        if (qer.equals("pp")) {
            inDex = PPPC;
        }
        if (qer.equals("do")) {
            inDex = DOPC;
        }
        if (qer.equals("a")) {
            inDex = APC;
        }
        if (qer.equals("td")) {
            inDex = TDPC;
        }
        return inDex;
    }

    /**
     <b>Проверяет есть ли в БД имя пользователя</b>

     @param sql    запрос
     @param pcName имя ПК
     @return имя юзера, если есть.
     */
    @SuppressWarnings ("MethodWithMultipleLoops")
    private String offLinesCheckUser(String sql, String pcName) {
        StringBuilder stringBuilder = new StringBuilder();
        try(PreparedStatement p = c.prepareStatement(sql);
            PreparedStatement p1 = c.prepareStatement(sql.replaceAll(ConstantsFor.STR_PCUSER, ConstantsFor.STR_PCUSERAUTO))){
            p.setString(1, pcName);
            p1.setString(1, pcName);
            try(ResultSet resultSet = p.executeQuery();
                ResultSet resultSet1 = p1.executeQuery()){
                while(resultSet.next()){
                    stringBuilder.append("<b>")
                        .append(resultSet.getString(ConstantsFor.DB_FIELD_USER).trim()).append("</b> (time: ")
                        .append(resultSet.getString(DB_FIELD_WHENQUERIED)).append(")");
                }
                while(resultSet1.next()){
                    if(resultSet1.last()){
                        return stringBuilder
                            .append("    (AutoResolved name: ")
                            .append(resultSet1.getString(ConstantsFor.DB_FIELD_USER).trim()).append(" (time: ")
                            .append(resultSet1.getString(DB_FIELD_WHENQUERIED)).append("))").toString();
                    }
                }
            }
        }
        catch(SQLException e){
            FileSystemWorker.recFile(
                NetScannerSvc.class.getSimpleName() + "offLinesCheckUser" + ConstantsFor.LOG,
                Collections.singletonList(new TForms().fromArray(e, false)));
            stringBuilder.append(e.getMessage());
            reconnectToDB();
        }
        return "<font color=\"orange\">EXCEPTION in SQL dropped. <br>" + stringBuilder.toString() + "</font>";
    }

    /**
     Проверяет имя пользователя на ПК онлайн

     @param sql    запрос
     @param pcName имя ПК
     @return кол-во проверок и сколько был вкл/выкл
     @see #getSomeMore(String, boolean)
     */
    private String onLinesCheck(String sql, String pcName) {
        PCUserResolver pcUserResolver = PCUserResolver.getPcUserResolver(c);
        List<Integer> onLine = new ArrayList<>();
        List<Integer> offLine = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        try (PreparedStatement statement = c.prepareStatement(sql)) {
            Runnable r = () -> pcUserResolver.namesToFile(pcName);
            execSet(r);
            statement.setString(1, pcName);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ADComputer adComputer = new ADComputer();
                    int onlineNow = resultSet.getInt(ONLINE_NOW);
                    if (onlineNow == 1) {
                        onLine.add(onlineNow);
                        adComputer.setDnsHostName(pcName);
                    }
                    if (onlineNow == 0) {
                        offLine.add(onlineNow);
                    }
                    AD_COMPUTERS.add(adComputer);
                }
            }
        }
        catch(SQLException e){
            reconnectToDB();
            FileSystemWorker.recFile(
                NetScannerSvc.class.getSimpleName() + ONLINES_CHECK + ConstantsFor.LOG,
                Collections.singletonList(new TForms().fromArray(e, false)));
            return e.getMessage();
        }
        catch(NullPointerException e){
            FileSystemWorker.recFile(
                NetScannerSvc.class.getSimpleName() + ONLINES_CHECK + ConstantsFor.LOG,
                Collections.singletonList(new TForms().fromArray(e, false)));
            return e.getMessage();
        }
        return stringBuilder
            .append(offLine.size())
            .append(" offline times and ")
            .append(onLine.size())
            .append(" online times.").toString();
    }

    /**
     @see AppComponents#lastNetScanMap()
     */
    private NetScannerSvc() {
        this.netWork = AppComponents.lastNetScanMap();
    }

    /**
     Сетает {@link org.springframework.core.task.TaskExecutor} для запуска сканирования отдельного ПК.@param executor {@link ThreadConfig}
     <p>
     Usages: {@link #onLinesCheck(String, String)} <br> Uses: -

     @param r {@link Runnable}, для пуска.
     */
    private void execSet(Runnable r) {
        CustomizableThreadCreator customizableThreadCreator = new CustomizableThreadCreator("OnChk: ");
        customizableThreadCreator.setThreadGroup(TASK_EXECUTOR.getThreadGroup());
        Thread thread = customizableThreadCreator.createThread(r);
        thread.start();
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

    private void tryKillSleepTHR(String str) {
        Thread.currentThread().checkAccess();
        for (Map.Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
            Thread x = entry.getKey();
            if (x.getState().equals(Thread.State.WAITING) && x.getName().contains("eatmeat.ru")) {
                x.checkAccess();
                x.interrupt();
                TASK_EXECUTOR.destroy();
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
                    LOGGER.info(e.getMessage());
                }
            }
        }
    }

    private void countStat() {
        List<String> readFileAsList = new ArrayList<>();
        try(InputStream inputStream = new FileInputStream(ConstantsFor.VELKOM_PCUSERAUTO_TXT);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader)){
            while(inputStreamReader.ready()){
                readFileAsList.add(bufferedReader.readLine().split("\\Q0) \\E")[1]);
            }
        }
        catch(IOException e){
            LOGGER.error(e.getMessage(), e);
        }
        FileSystemWorker.recFile("pcautodis.txt", readFileAsList.parallelStream().distinct());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NetScannerSvc{");
        sb.append("DB_NAME='").append(DB_NAME).append('\'');
        sb.append(", GET_INFO_FROM_DB='").append(GET_INFO_FROM_DB).append('\'');
        sb.append(", ONLINE_NOW='").append(ONLINE_NOW).append('\'');
        sb.append(", onLinePCs=").append(onLinePCs);
        sb.append(", ONLINES_CHECK='").append(ONLINES_CHECK).append('\'');
        sb.append(", STR_NETSCANNERSVC='").append(STR_NETSCANNERSVC).append('\'');
        sb.append(", thePc='").append(thePc).append('\'');
        sb.append(", WRITE_DB='").append(WRITE_DB).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
