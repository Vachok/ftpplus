// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.scanner;


import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import ru.vachok.messenger.MessageSwing;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.*;
import ru.vachok.networker.abstr.NetKeeper;
import ru.vachok.networker.enums.ConstantsNet;
import ru.vachok.networker.enums.FileNames;
import ru.vachok.networker.enums.ModelAttributeNames;
import ru.vachok.networker.enums.PropertiesNames;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.info.TvPcInformation;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.restapi.message.MessageToTray;
import ru.vachok.networker.restapi.props.InitPropertiesAdapter;
import ru.vachok.networker.systray.actions.ActionCloseMsg;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.*;
import java.util.prefs.Preferences;

import static ru.vachok.networker.ConstantsFor.STR_P;


/**
 @since 21.08.2018 (14:40) */
@Service(ConstantsNet.BEANNAME_NETSCANNERSVC)
@Scope(ConstantsFor.SINGLETON)
public class NetScannerSvc {
    
    /**
     {@link ConstantsFor#DELAY}
     */
    static final int DURATION_MIN = (int) ConstantsFor.DELAY;
    
    /**
     {@link AppComponents#getProps()}
     */
    private static final Properties PROPERTIES = AppComponents.getProps();
    
    /**
     Имя метода, как строка.
     <p>
     {@link NetScannerSvc#getPCsAsync()}
     */
    private static final String METH_NAME_GET_PCS_ASYNC = "NetScannerSvc.getPCsAsync";
    
    private static final Set<String> PC_NAMES_SET = new TreeSet<>();
    
    private static final String METH_GETPCSASYNC = ".getPCsAsync";
    
    private static final MessageToUser messageToUser = new MessageLocal(NetScannerSvc.class.getSimpleName());
    
    private static final File scanTemp = new File("scan.tmp");
    
    private static final TForms T_FORMS = new TForms();
    
    /**
     Время инициализации
     */
    private final long startClassTime = System.currentTimeMillis();
    
    /**
     Неиспользуемые имена ПК
 
     @see #theSETOfPCNamesPref(String)
     */
    private static Collection<String> unusedNamesTree = new TreeSet<>();
    
    private List<String> minimessageToUser = new ArrayList<>();
    
    @SuppressWarnings("CanBeFinal")
    private Connection connection;
    
    private String thePc = "PC";
    
    /**
     Название {@link Thread}
     <p>
     {@link Thread#getName()}
     */
    private String thrName = Thread.currentThread().getName();
    
    private Map<String, Boolean> netWorkMap;
    
    private Model model;
    
    private HttpServletRequest request;
    
    private long lastSt;
    
    private static final Preferences PREFERENCES = AppComponents.getUserPref();
    
    public NetScannerSvc() {
        this.netWorkMap = NetKeeper.getNetworkPCs();
        try {
            this.connection = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_VELKOM);
        }
        catch (SQLException e) {
            messageToUser.error(MessageFormat.format("NetScannerSvc.static initializer: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
        PREFERENCES.put(PropertiesNames.PR_ONLINEPC, PROPERTIES.getProperty(PropertiesNames.PR_ONLINEPC));
        PROPERTIES.setProperty(PropertiesNames.PR_ONLINEPC, "0");
    }
    
    /**
     @return атрибут модели.
     */
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
    
    private Set<String> theSETOfPcNames() {
        fileScanTMPCreate(true);
        getPCsAsync();
        return PC_NAMES_SET;
    }
    
    private Set<String> theSETOfPCNamesPref(String prefixPcName) {
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
    
    private static boolean fileScanTMPCreate(boolean create) {
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
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NetScannerSvc{");
        sb.append(", METH_NAME_GET_PCS_ASYNC='").append(METH_NAME_GET_PCS_ASYNC).append('\'');
        sb.append(", FILENAME_PCAUTOUSERSUNIQ='").append(FileNames.FILENAME_PCAUTOUSERSUNIQ).append('\'');
        sb.append(", PC_NAMES_SET=").append(PC_NAMES_SET.size());
        sb.append(", onLinePCsNum=").append(PROPERTIES.getProperty(PropertiesNames.PR_ONLINEPC, "0"));
        sb.append(", unusedNamesTree=").append(unusedNamesTree.size());
        sb.append(", startClassTime=").append(new Date(startClassTime));
        sb.append(", thePc='").append(thePc).append('\'');
        sb.append(", thrName='").append(thrName).append('\'');
        sb.append(", netWorkMap=").append(netWorkMap.size());
        sb.append('}');
        return sb.toString();
    }
    
    void checkMapSizeAndDoAction(Model model, HttpServletRequest request, long lastSt) throws ExecutionException, InterruptedException, TimeoutException, IOException {
        this.model = model;
        this.request = request;
        this.lastSt = lastSt;
        Runnable scanRun = ()->scanIt(new Date(lastSt));
        int thisTotpc = Integer.parseInt(PROPERTIES.getProperty(PropertiesNames.PR_TOTPC, "259"));
        
        if ((scanTemp.isFile() && scanTemp.exists())) {
            mapSizeBigger(thisTotpc);
        }
        else {
            timeCheck(thisTotpc - NetKeeper.getNetworkPCs().size(), lastSt / 1000);
        }
        
    }
    
    private void pcNameInfo(String pcName) {
        InformationFactory informationFactory = new TvPcInformation();
        boolean reachable;
        InetAddress byName;
        try {
            byName = InetAddress.getByName(pcName);
            reachable = byName.isReachable(ConstantsFor.TIMEOUT_650);
            //noinspection CastCanBeRemovedNarrowingVariableType
            informationFactory.setInfo(reachable);
            
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
                int onlinePC = Integer.parseInt((PROPERTIES.getProperty(PropertiesNames.PR_ONLINEPC, "0")));
                onlinePC += 1;
                PROPERTIES.setProperty(PropertiesNames.PR_ONLINEPC, String.valueOf(onlinePC));
            }
        }
        catch (IOException e) {
            unusedNamesTree.add(e.getMessage());
        }
    }
    
    /**
     Основной скан-метод.
     <p>
     1. {@link NetScannerSvc#fileScanTMPCreate(boolean)}. Убедимся, что файл создан. <br>
     2. {@link ActionCloseMsg} , 3. {@link MessageToTray}. Создаём взаимодействие с юзером. <br>
     3. {@link UsefulUtilities#getUpTime()} - uptime приложения в 4. {@link MessageToTray#info(java.lang.String, java.lang.String, java.lang.String)}. <br>
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
                .info("NetScannerSvc started scan", UsefulUtilities.getUpTime(), MessageFormat.format("Last online {0} PCs\n File: {1}",
                    PROPERTIES.getProperty(PropertiesNames.PR_ONLINEPC), new File("scan.tmp").getAbsolutePath()));
        }
        catch (NoClassDefFoundError e) {
            messageToUser.error(getClass().getSimpleName(), METH_GETPCSASYNC, T_FORMS.fromArray(e.getStackTrace(), false));
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
        float upTime = (float) (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startClassTime)) / UsefulUtilities.ONE_HOUR_IN_MIN;
        String compNameUsers = T_FORMS.fromArray(ConstantsNet.getPCnameUsersMap(), false);
        String psUser = T_FORMS.fromArrayUsers(ConstantsNet.getPcUMap(), false);
        String msgTimeSp = MessageFormat.format("NetScannerSvc.getPCsAsync method spend {0} seconds.", (float) (System.currentTimeMillis() - startClassTime) / 1000);
        String valueOfPropLastScan = String.valueOf((System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY)));
    
        PROPERTIES.setProperty(ConstantsNet.PR_LASTSCAN, valueOfPropLastScan);
        minimessageToUser.add(compNameUsers);
        minimessageToUser.add(psUser);
        minimessageToUser.add(msgTimeSp);
        minimessageToUser.add(T_FORMS.fromArray(PROPERTIES, false));
    
        ConcurrentNavigableMap<String, Boolean> lastStateOfPCs = NetKeeper.getNetworkPCs();
        
        FileSystemWorker.writeFile(ConstantsNet.BEANNAME_LASTNETSCAN, lastStateOfPCs.navigableKeySet().stream());
        FileSystemWorker.writeFile(this.getClass().getSimpleName() + ".mini", minimessageToUser);
        FileSystemWorker.writeFile("unused.ips", unusedNamesTree.stream());
    
        new ExitApp(FileNames.FILENAME_ALLDEVMAP, NetKeeper.getAllDevices()).isWriteOwnObject();
        boolean isFile = fileScanTMPCreate(false);
        String bodyMsg = "Online: " + PROPERTIES.getProperty(PropertiesNames.PR_ONLINEPC, "0") + ".\n"
            + upTime + " min uptime. \n" + isFile + " = scan.tmp\n";
        try {
            new MessageSwing().infoTimer((int) ConstantsFor.DELAY, bodyMsg);
            PREFERENCES.put(PropertiesNames.PR_ONLINEPC, PROPERTIES.getProperty(PropertiesNames.PR_ONLINEPC));
            InitPropertiesAdapter.setProps(PROPERTIES);
        }
        catch (RuntimeException e) {
            messageToUser.warn(bodyMsg);
        }
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
        return T_FORMS.fromArray(list, true);
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
    
    private void mapSizeBigger(int thisTotpc) throws ExecutionException, InterruptedException, TimeoutException, IOException {
        long timeLeft = TimeUnit.MILLISECONDS.toSeconds(lastSt - System.currentTimeMillis());
        int pcWas = Integer.parseInt(PROPERTIES.getProperty(PropertiesNames.PR_ONLINEPC, "0"));
        int remainPC = thisTotpc - NetKeeper.getNetworkPCs().size();
        boolean newPSs = remainPC < 0;
        
        String msg = getMsg(timeLeft);
        String title = getTitle(remainPC, thisTotpc, pcWas);
        String pcValue = fromArray();
        
        messageToUser.info(msg);
        model.addAttribute("left", msg).addAttribute("pc", pcValue).addAttribute(ModelAttributeNames.ATT_TITLE, title);
        
        if (newPSs) {
            newPCCheck(pcValue, remainPC);
        }
        else {
            noNewewPCCheck(remainPC);
        }
        
        timeCheck(remainPC, lastSt / 1000);
    }
    
    private @NotNull String getMsg(long timeLeft) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(timeLeft);
        stringBuilder.append(" seconds (");
        stringBuilder.append((float) timeLeft / UsefulUtilities.ONE_HOUR_IN_MIN);
        stringBuilder.append(" min) left<br>Delay period is ");
        stringBuilder.append(DURATION_MIN);
        return stringBuilder.toString();
    }
    
    private @NotNull String getTitle(int remainPC, int thisTotpc, int pcWas) {
        StringBuilder titleBuilder = new StringBuilder();
        titleBuilder.append(remainPC);
        titleBuilder.append("/");
        titleBuilder.append(thisTotpc);
        titleBuilder.append(" PCs (");
        titleBuilder.append(PROPERTIES.getProperty(PropertiesNames.PR_ONLINEPC, "0"));
        titleBuilder.append("/");
        titleBuilder.append(pcWas);
        titleBuilder.append(") Next run ");
        titleBuilder.append(LocalDateTime.ofEpochSecond(lastSt / 1000, 0, ZoneOffset.ofHours(3)).toLocalTime());
        return titleBuilder.toString();
    }
    
    private void noNewewPCCheck(int remainPC) {
        if (remainPC < ConstantsFor.INT_ANSWER) {
            PROPERTIES.setProperty(PropertiesNames.PR_TOTPC, String.valueOf(NetKeeper.getNetworkPCs().size()));
        }
    }
    
    private void newPCCheck(String pcValue, double remainPC) {
        FileSystemWorker.writeFile(ConstantsNet.BEANNAME_LASTNETSCAN, pcValue);
        model.addAttribute(PropertiesNames.PR_AND_ATT_NEWPC, "Добавлены компы! " + Math.abs(remainPC) + " шт.");
        PROPERTIES.setProperty(PropertiesNames.PR_TOTPC, String.valueOf(NetKeeper.getNetworkPCs().size()));
        PROPERTIES.setProperty(PropertiesNames.PR_AND_ATT_NEWPC, String.valueOf(remainPC));
    }
    
    private void timeCheck(int remainPC, long lastScanEpoch) throws ExecutionException, InterruptedException, TimeoutException {
        Runnable scanRun = ()->scanIt(new Date(lastScanEpoch * 1000));
        LocalTime lastScanLocalTime = LocalDateTime.ofEpochSecond(lastScanEpoch, 0, ZoneOffset.ofHours(3)).toLocalTime();
        boolean isSystemTimeBigger = (System.currentTimeMillis() > lastScanEpoch * 1000);
        if (!(scanTemp.exists())) {
            model.addAttribute(PropertiesNames.PR_AND_ATT_NEWPC, lastScanLocalTime);
            if (isSystemTimeBigger) {
                Future<?> submitScan = Executors.newSingleThreadExecutor().submit(scanRun);
                submitScan.get(ConstantsFor.DELAY - 1, TimeUnit.MINUTES);
                messageToUser.warn(MessageFormat.format("Scan is Done {0}", submitScan.isDone()));
            }
        }
        else {
            messageToUser.warn(this.getClass().getSimpleName() + ".timeCheck", "lastScanLocalTime", " = " + lastScanLocalTime);
        }
        
    }
    
    @Async
    private void scanIt(Date lastScanDate) {
        if (request != null && request.getQueryString() != null) {
            NetKeeper.getNetworkPCs().clear();
            PROPERTIES.setProperty(PropertiesNames.PR_ONLINEPC, "0");
            PREFERENCES.putInt(PropertiesNames.PR_ONLINEPC, 0);
            Set<String> pcNames = theSETOfPCNamesPref(request.getQueryString());
            model
                .addAttribute(ModelAttributeNames.ATT_TITLE, new Date().toString())
                .addAttribute("pc", T_FORMS.fromArray(pcNames, true));
        }
        else {
            NetKeeper.getNetworkPCs().clear();
            PROPERTIES.setProperty(PropertiesNames.PR_ONLINEPC, "0");
            PREFERENCES.putInt(PropertiesNames.PR_ONLINEPC, 0);
            Set<String> pCsAsync = theSETOfPcNames();
            model.addAttribute(ModelAttributeNames.ATT_TITLE, lastScanDate).addAttribute("pc", T_FORMS.fromArray(pCsAsync, true));
            PROPERTIES.setProperty(ConstantsNet.PR_LASTSCAN, String.valueOf(System.currentTimeMillis()));
        }
    }
    
    private @NotNull String fromArray() {
        StringBuilder brStringBuilder = new StringBuilder();
        brStringBuilder.append(STR_P);
        Set<?> keySet = NetKeeper.getNetworkPCs().keySet();
        List<String> list = new ArrayList<>(keySet.size());
        keySet.forEach(x->list.add(x.toString()));
        Collections.sort(list);
        for (String keyMap : list) {
            String valueMap = NetKeeper.getNetworkPCs().get(keyMap).toString();
            brStringBuilder.append(keyMap).append(" ").append(valueMap).append("<br>");
        }
        return brStringBuilder.toString();
        
    }
    
}
