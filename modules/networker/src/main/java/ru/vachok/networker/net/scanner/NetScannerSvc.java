// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.scanner;


import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import ru.vachok.messenger.MessageSwing;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.ExitApp;
import ru.vachok.networker.TForms;
import ru.vachok.networker.abstr.NetKeeper;
import ru.vachok.networker.ad.user.InformationFactoryImpl;
import ru.vachok.networker.componentsrepo.report.InformationFactory;
import ru.vachok.networker.enums.ConstantsNet;
import ru.vachok.networker.enums.FileNames;
import ru.vachok.networker.enums.PropertiesNames;
import ru.vachok.networker.enums.UsefulUtilites;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.restapi.message.MessageToTray;
import ru.vachok.networker.restapi.props.InitPropertiesAdapter;
import ru.vachok.networker.systray.actions.ActionCloseMsg;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.TimeUnit;


/**
 Управление сервисами LAN-разведки.
 <p>
 
 @see ru.vachok.networker.net.scanner.NetScannerSvcTest
 @since 21.08.2018 (14:40) */
@Service(ConstantsNet.BEANNAME_NETSCANNERSVC)
@Scope(ConstantsFor.SINGLETON)
public class NetScannerSvc implements InformationFactory {
    
    
    /**
     {@link AppComponents#getProps()}
     */
    private static final Properties LOCAL_PROPS = AppComponents.getProps();
    
    /**
     Имя метода, как строка.
     <p>
     {@link NetScannerSvc#getPCsAsync()}
     */
    private static final String METH_NAME_GET_PCS_ASYNC = "NetScannerSvc.getPCsAsync";
    
    private static final Set<String> PC_NAMES_SET = new TreeSet<>();
    
    private static final String METH_GETPCSASYNC = ".getPCsAsync";
    
    private static final MessageToUser messageToUser = new MessageLocal(NetScannerSvc.class.getSimpleName());
    
    /**
     Время инициализации
     */
    private final long startClassTime = System.currentTimeMillis();
    
    /**
     Неиспользуемые имена ПК
 
     @see #theSETOfPCNamesPref(String)
     */
    private static Collection<String> unusedNamesTree = new TreeSet<>();
    
    /**
     new {@link NetScannerSvc}
     */
    private static NetScannerSvc netScannerSvcInst = new NetScannerSvc();
    
    private static String inputWithInfoFromDB = NetScannerSvc.class.getSimpleName();
    
    private List<String> minimessageToUser = new ArrayList<>();
    
    @SuppressWarnings("CanBeFinal")
    private Connection connection;
    
    /**
     Компьютеры онлайн
     */
    private int onLinePCsNum;
    
    private String thePc = "PC";
    
    /**
     Название {@link Thread}
     <p>
     {@link Thread#getName()}
     */
    private String thrName = Thread.currentThread().getName();
    
    private Map<String, Boolean> netWorkMap;
    
    private NetScannerSvc() {
        this.netWorkMap = NetKeeper.getNetworkPCs();
        try {
            this.connection = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_VELKOM);
        }
        catch (SQLException e) {
            messageToUser.error(MessageFormat.format("NetScannerSvc.static initializer: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
    }
    
    /**
     @return {@link #netScannerSvcInst}
     */
    public static NetScannerSvc getInst() {
        netScannerSvcInst.setOnLinePCsNum(0);
        return netScannerSvcInst;
    }
    
    /**
     Доступность пк. online|offline сколько раз.
 
     @see NetScannerSvc#theInfoFromDBGetter()
     */
    public String getInputWithInfoFromDB() {
        return inputWithInfoFromDB;
    }
    
    /**
     @param inputWithInfoFromDB {@link NetScannerSvc#theInfoFromDBGetter()}
     */
    public static void setInputWithInfoFromDB(String inputWithInfoFromDB) {
        NetScannerSvc.inputWithInfoFromDB = inputWithInfoFromDB;
    }
    
    @SuppressWarnings("SameReturnValue")
    public String theInfoFromDBGetter() {
        StringBuilder sqlQBuilder = new StringBuilder();
        String thePcLoc = AppComponents.netScannerSvc().getThePc();
        if (thePcLoc.isEmpty()) {
            IllegalArgumentException argumentException = new IllegalArgumentException("Must be NOT NULL!");
            sqlQBuilder.append(argumentException.getMessage());
        }
        else {
            sqlQBuilder.append("select * from velkompc where NamePP like '%").append(thePcLoc).append("%'");
        }
        try (
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQBuilder.toString())
        ) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                List<String> timeNow = new ArrayList<>();
                List<Integer> integersOff = new ArrayList<>();
                while (resultSet.next()) {
                    int onlineNow = resultSet.getInt(ConstantsNet.ONLINE_NOW);
                    if (onlineNow == 1) {
                        timeNow.add(resultSet.getString(ConstantsFor.DBFIELD_TIMENOW));
                    }
                    else {
                        integersOff.add(onlineNow);
                    }
                    StringBuilder stringBuilder = new StringBuilder();
                    String namePP = new StringBuilder()
                        .append("<center><h2>").append(InetAddress.getByName(thePcLoc + ConstantsFor.DOMAIN_EATMEATRU)).append(" information.<br></h2>")
                        .append("<font color = \"silver\">OnLines = ").append(timeNow.size())
                        .append(". Offline = ").append(integersOff.size()).append(". TOTAL: ")
                        .append(integersOff.size() + timeNow.size()).toString();
    
                    stringBuilder
                        .append(namePP)
                        .append(". <br>");
                    AppComponents.netScannerSvc().setThePc(stringBuilder.toString());
                }
                sortList(timeNow);
            }
        }
        catch (SQLException e) {
            FileSystemWorker.error("NetScannerSvc.theInfoFromDBGetter", e);
        }
        catch (IndexOutOfBoundsException | UnknownHostException e) {
            AppComponents.netScannerSvc().setThePc(e.getMessage() + " " + new TForms().fromArray(e, false));
        }
        return "ok";
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
    
    public int getOnLinePCsNum() {
        return onLinePCsNum;
    }
    
    public void setOnLinePCsNum(int onLinePCsNum) {
        this.onLinePCsNum = onLinePCsNum;
    }
    
    public Set<String> theSETOfPcNames() {
        fileScanTMPCreate(true);
        getPCsAsync();
        return PC_NAMES_SET;
    }
    
    public Set<String> theSETOfPCNamesPref(String prefixPcName) {
        final long startMethTime = System.currentTimeMillis();
        String pcsString;
        for (String pcName : getCycleNames(prefixPcName)) {
            pcNameInfo(pcName);
        }
        netWorkMap.put("<h4>" + prefixPcName + "     " + PC_NAMES_SET.size() + "</h4>", true);
        try {
            pcsString = writeDB();
            messageToUser.info(pcsString);
        }
        catch (SQLException e) {
            messageToUser.error(e.getMessage());
        }
        String elapsedTime = "<b>Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startMethTime) + " sec.</b> " + LocalTime.now();
        PC_NAMES_SET.add(elapsedTime);
        return PC_NAMES_SET;
    }
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        return null;
    }
    
    @Override
    public void setInfo() {
    
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NetScannerSvc{");
        sb.append(", LOCAL_PROPS=").append(LOCAL_PROPS.equals(AppComponents.getProps()));
        sb.append(", METH_NAME_GET_PCS_ASYNC='").append(METH_NAME_GET_PCS_ASYNC).append('\'');
        sb.append(", FILENAME_PCAUTOUSERSUNIQ='").append(FileNames.FILENAME_PCAUTOUSERSUNIQ).append('\'');
        sb.append(", PC_NAMES_SET=").append(PC_NAMES_SET.size());
        sb.append(", onLinePCsNum=").append(onLinePCsNum);
        sb.append(", unusedNamesTree=").append(unusedNamesTree.size());
        sb.append(", netScannerSvcInst=").append(netScannerSvcInst.hashCode());
        sb.append(", startClassTime=").append(new Date(startClassTime));
        sb.append(", thePc='").append(thePc).append('\'');
        sb.append(", thrName='").append(thrName).append('\'');
        sb.append(", netWorkMap=").append(netWorkMap.size());
        sb.append('}');
        return sb.toString();
    }
    
    /**
     Возвращает последнее время когда видели онлайн.
     
     @param timeNow колонка из БД {@code velkompc} TimeNow (время записи)
     @see NetScannerSvc#theInfoFromDBGetter()
     */
    private static void sortList(List<String> timeNow) {
        Collections.sort(timeNow);
        
        String str = timeNow.get(timeNow.size() - 1);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(AppComponents.netScannerSvc().getThePc());
        
        stringBuilder.append("Last online: ");
        stringBuilder.append(str);
        stringBuilder.append(" (");
        stringBuilder.append(")<br>Actual on: ");
        stringBuilder.append(new Date(Long.parseLong(LOCAL_PROPS.getProperty(ConstantsNet.PR_LASTSCAN))));
        stringBuilder.append("</center></font>");
        
        String thePcWithDBInfo = stringBuilder.toString();
        AppComponents.netScannerSvc().setThePc(thePcWithDBInfo);
        setInputWithInfoFromDB(thePcWithDBInfo);
        
    }
    
    private void pcNameInfo(String pcName) {
        InformationFactory informationFactory = new InformationFactoryImpl();
        boolean reachable;
        InetAddress byName;
        try {
            byName = InetAddress.getByName(pcName);
            reachable = byName.isReachable(ConstantsFor.TIMEOUT_650);
            //noinspection CastCanBeRemovedNarrowingVariableType
            informationFactory.setInfo();
    
            String someMore = informationFactory.getInfoAbout(pcName);
            if (!reachable) {
                pcNameUnreachable(someMore, byName);
            }
            else {
                StringBuilder builder = new StringBuilder();
                builder.append("<br><b><a href=\"/ad?");
                builder.append(pcName.split(".eatm")[0]);
                builder.append("\" >");
                builder.append(InetAddress.getByName(pcName));
                builder.append("</b></a>     ");
                builder.append(someMore);
                builder.append(". ");
    
                String printStr = builder.toString();
                String pcOnline = "online is true<br>";
    
                netWorkMap.put(printStr, true);
                PC_NAMES_SET.add(pcName + ":" + byName.getHostAddress() + pcOnline);
                messageToUser.info(pcName, pcOnline, someMore);
                this.onLinePCsNum += 1;
            }
        }
        catch (IOException e) {
            unusedNamesTree.add(e.getMessage());
        }
    }
    
    /**
     Основной скан-метод.
     <p>
     1. {@link #fileScanTMPCreate(boolean)}. Убедимся, что файл создан. <br>
     2. {@link ActionCloseMsg} , 3. {@link MessageToTray}. Создаём взаимодействие с юзером. <br>
     3. {@link UsefulUtilites#getUpTime()} - uptime приложения в 4. {@link MessageToTray#info(java.lang.String, java.lang.String, java.lang.String)}. <br>
     5. {@link NetScannerSvc#theSETOfPCNamesPref(java.lang.String)} - скан сегмента. <br>
 
     @see #theSETOfPcNames()
     */
    private void getPCsAsync() {
        ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
        mxBean.setThreadContentionMonitoringEnabled(true);
        mxBean.resetPeakThreadCount();
        mxBean.setThreadCpuTimeEnabled(true);
        try {
            new MessageToTray(this.getClass().getSimpleName())
                .info("NetScannerSvc started scan", UsefulUtilites.getUpTime(), MessageFormat.format("Last online {0} PCs\n File: {1}",
                    onLinePCsNum, new File("scan.tmp").getAbsolutePath()));
        }
        catch (NoClassDefFoundError e) {
            messageToUser.error(getClass().getSimpleName(), METH_GETPCSASYNC, new TForms().fromArray(e.getStackTrace(), false));
        }
        catch (Exception e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + METH_GETPCSASYNC, e));
        }
        AppComponents.threadConfig().execByThreadConfig(this::scanPCPrefix);
        long[] deadlockedThreads = mxBean.findDeadlockedThreads();
        if (deadlockedThreads != null) {
            System.err.println("You have a deadLock(s): " + Arrays.toString(deadlockedThreads));
        }
        else {
            long cpuTimeTotal = 0;
            for (long threadId : mxBean.getAllThreadIds()) {
                cpuTimeTotal += mxBean.getThreadCpuTime(threadId);
            }
            cpuTimeTotal = TimeUnit.NANOSECONDS.toSeconds(cpuTimeTotal);
            minimessageToUser
                .add(MessageFormat.format("Peak was {0} threads, now: {1}. Time: {2} millis.", mxBean.getPeakThreadCount(), mxBean.getThreadCount(), cpuTimeTotal));
        }
    }
    
    @SuppressWarnings("MagicNumber")
    private void runAfterAllScan() {
        float upTime = (float) (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startClassTime)) / UsefulUtilites.ONE_HOUR_IN_MIN;
        String compNameUsers = new TForms().fromArray(ConstantsNet.getPCnameUsersMap(), false);
        String psUser = new TForms().fromArrayUsers(ConstantsNet.getPcUMap(), false);
        String msgTimeSp = MessageFormat.format("NetScannerSvc.getPCsAsync method spend {0} seconds.", (float) (System.currentTimeMillis() - startClassTime) / 1000);
        String valueOfPropLastScan = String.valueOf((System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY)));
    
        LOCAL_PROPS.setProperty(ConstantsNet.PR_LASTSCAN, valueOfPropLastScan);
        minimessageToUser.add(compNameUsers);
        minimessageToUser.add(psUser);
        minimessageToUser.add(msgTimeSp);
        minimessageToUser.add(new TForms().fromArray(LOCAL_PROPS, false));
        LOCAL_PROPS.setProperty(PropertiesNames.PR_ONLINEPC, String.valueOf(onLinePCsNum));
    
        boolean isLastModSet = new File(ConstantsFor.class.getSimpleName() + FileNames.FILEEXT_PROPERTIES).setLastModified(ConstantsFor.DELAY);
    
        ConcurrentNavigableMap<String, Boolean> lastStateOfPCs = NetKeeper.getNetworkPCs();
        
        FileSystemWorker.writeFile(ConstantsNet.BEANNAME_LASTNETSCAN, lastStateOfPCs.navigableKeySet().stream());
        FileSystemWorker.writeFile(this.getClass().getSimpleName() + ".mini", minimessageToUser);
        FileSystemWorker.writeFile("unused.ips", unusedNamesTree.stream());
    
        boolean ownObject = new ExitApp(FileNames.FILENAME_ALLDEVMAP, NetKeeper.getAllDevices()).isWriteOwnObject();
        boolean isFile = fileScanTMPCreate(false);
        File file = new File(FileNames.FILENAME_ALLDEVMAP);
        String bodyMsg = "Online: " + onLinePCsNum + ".\n"
            + upTime + " min uptime. \n" + isFile + " = scan.tmp\n";
        try {
            new MessageSwing().infoTimer((int) ConstantsFor.DELAY, bodyMsg);
            InitPropertiesAdapter.setProps(LOCAL_PROPS);
        }
        catch (Exception e) {
            messageToUser.warn(bodyMsg);
        }
        this.onLinePCsNum = 0;
    }
    
    /**
     Если ПК не пингуется
     <p>
     Добавить в {@link #netWorkMap} , {@code online = false}.
     <p>
     
     @param byName {@link InetAddress}
     @see #theSETOfPCNamesPref(String)
     */
    private void pcNameUnreachable(String someMore, @NotNull InetAddress byName) {
        String onLines = new StringBuilder()
            .append("online ")
            .append(false)
            .append("<br>").toString();
        PC_NAMES_SET.add(byName.getHostName() + ":" + byName.getHostAddress() + " " + onLines);
        netWorkMap.put("<br>" + byName + " last name is " + someMore, false);
        messageToUser.warn(byName.toString(), onLines, someMore);
    }
    
    /**
     Создание lock-файла
     <p>
     
     @param create создать или удалить файл.
     @return scan.tmp exist
     
     @see #getPCsAsync()
     */
    private boolean fileScanTMPCreate(boolean create) {
        File file = new File("scan.tmp");
        try {
            if (create) {
                file = Files.createFile(file.toPath()).toFile();
            }
            else {
                Files.deleteIfExists(Paths.get("scan.tmp"));
            }
        }
        catch (IOException e) {
            FileSystemWorker.error("NetScannerSvc.fileScanTMPCreate", e);
        }
        boolean exists = file.exists();
        if (exists) {
            file.deleteOnExit();
        }
        return exists;
    }
    
    /**
     1. {@link #getNamesCount(String)}
     
     @param namePCPrefix префикс имени ПК
     @return обработанные имена ПК, для пинга
 
     @see #theSETOfPCNamesPref(String)
     */
    private @NotNull Collection<String> getCycleNames(String namePCPrefix) {
        if (namePCPrefix == null) {
            namePCPrefix = "pp";
        }
        int inDex = getNamesCount(namePCPrefix);
        String nameCount;
        Collection<String> list = new ArrayList<>();
        int pcNum = 0;
        for (int i = 1; i < inDex; i++) {
            if (namePCPrefix.equals("no") || namePCPrefix.equals("pp") || namePCPrefix.equals("do") || namePCPrefix.equals("notd") || namePCPrefix.equals("dotd")) {
                nameCount = String.format("%04d", ++pcNum);
            }
            else {
                nameCount = String.format("%03d", ++pcNum);
            }
            list.add(namePCPrefix + nameCount + ConstantsFor.DOMAIN_EATMEATRU);
        }
        return list;
    }
    
    /**
     @param qer префикс имени ПК
     @return кол-во ПК, для пересичления
     
     @see #getCycleNames(String)
     */
    private int getNamesCount(@NotNull String qer) {
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
        if (qer.equals("dotd")) {
            inDex = ConstantsNet.DOTDPC;
        }
        if (qer.equals("notd")) {
            inDex = ConstantsNet.NOTDPC;
        }
        return inDex;
    }
    
    /**
     Запись в таблицу <b>velkompc</b> текущего состояния. <br>
     <p>
     1 {@link TForms#fromArray(List, boolean)}
     
     @return строка в html-формате
     
     @throws SQLException insert into  velkompc (NamePP, AddressPP, SegmentPP , OnlineNow) values (?,?,?,?)
     @see #theSETOfPCNamesPref(String)
     */
    @SuppressWarnings({"OverlyComplexMethod", "OverlyLongMethod"})
    private String writeDB() throws SQLException {
        int exUpInt = 0;
        List<String> list = new ArrayList<>();
        try (PreparedStatement p = connection.prepareStatement("insert into  velkompc (NamePP, AddressPP, SegmentPP , OnlineNow) values (?,?,?,?)")) {
            List<String> toSort = new ArrayList<>(PC_NAMES_SET);
            toSort.sort(null);
            for (String x : toSort) {
                String pcSegment = "Я не знаю...";
                if (x.contains("200.200")) {
                    pcSegment = "Торговый дом";
                }
                if (x.contains("200.201")) {
                    pcSegment = "IP телефоны";
                }
                if (x.contains("200.202")) {
                    pcSegment = "Техслужба";
                }
                if (x.contains("200.203")) {
                    pcSegment = "СКУД";
                }
                if (x.contains("200.204")) {
                    pcSegment = "Упаковка";
                }
                if (x.contains("200.205")) {
                    pcSegment = "МХВ";
                }
                if (x.contains("200.206")) {
                    pcSegment = "Здание склада 5";
                }
                if (x.contains("200.207")) {
                    pcSegment = "Сырокопоть";
                }
                if (x.contains("200.208")) {
                    pcSegment = "Участок убоя";
                }
                if (x.contains("200.209")) {
                    pcSegment = "Да ладно?";
                }
                if (x.contains("200.210")) {
                    pcSegment = "Мастера колб";
                }
                if (x.contains("200.212")) {
                    pcSegment = "Мастера деликатесов";
                }
                if (x.contains("200.213")) {
                    pcSegment = "2й этаж. АДМ.";
                }
                if (x.contains("200.214")) {
                    pcSegment = "WiFiCorp";
                }
                if (x.contains("200.215")) {
                    pcSegment = "WiFiFree";
                }
                if (x.contains("200.217")) {
                    pcSegment = "1й этаж АДМ";
                }
                if (x.contains("200.218")) {
                    pcSegment = "ОКК";
                }
                if (x.contains("192.168")) {
                    pcSegment = "Может быть в разных местах...";
                }
                if (x.contains("172.16.200")) {
                    pcSegment = "Open VPN авторизация - сертификат";
                }
                boolean onLine = false;
                if (x.contains("true")) {
                    onLine = true;
                }
                String x1 = x.split(":")[0];
                p.setString(1, x1);
                String x2 = x.split(":")[1];
                p.setString(2, x2.split("<")[0]);
                p.setString(3, pcSegment);
                p.setBoolean(4, onLine);
                exUpInt += p.executeUpdate();
                list.add(x1 + " " + x2 + " " + pcSegment + " " + onLine);
            }
        }
        messageToUser.warn(getClass().getSimpleName() + ".writeDB", "executeUpdate: ", " = " + exUpInt);
        return new TForms().fromArray(list, true);
    }
    
    private void scanPCPrefix() {
        for (String s : ConstantsNet.getPcPrefixes()) {
            this.thrName = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startClassTime) + "-sec";
            PC_NAMES_SET.clear();
            PC_NAMES_SET.addAll(theSETOfPCNamesPref(s));
            AppComponents.threadConfig().thrNameSet("pcGET");
        }
        String elapsedTime = "Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startClassTime) + " sec.";
        PC_NAMES_SET.add(elapsedTime);
        AppComponents.threadConfig().execByThreadConfig(this::runAfterAllScan);
    }
}