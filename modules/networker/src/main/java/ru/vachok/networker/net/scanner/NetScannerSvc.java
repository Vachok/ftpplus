// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.scanner;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import ru.vachok.messenger.MessageSwing;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.*;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.enums.ConstantsNet;
import ru.vachok.networker.enums.ModelAttributeNames;
import ru.vachok.networker.enums.PropertiesNames;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.info.HTMLInfo;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.net.NetKeeper;
import ru.vachok.networker.net.NetScanService;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.restapi.message.MessageToTray;
import ru.vachok.networker.restapi.props.InitPropertiesAdapter;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
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
 @see ru.vachok.networker.net.scanner.NetScannerSvcTest
 @since 21.08.2018 (14:40) */
@Service(ConstantsNet.BEANNAME_NETSCANNERSVC)
@Scope(ConstantsFor.SINGLETON)
public class NetScannerSvc implements HTMLInfo {
    
    
    /**
     {@link ConstantsFor#DELAY}
     */
    static final int DURATION_MIN = (int) ConstantsFor.DELAY;
    
    /**
     {@link AppComponents#getProps()}
     */
    private static final Properties PROPERTIES = AppComponents.getProps();
    
    private static final String METH_GETPCSASYNC = ".getPCsAsync";
    
    private static final MessageToUser messageToUser = new MessageLocal(NetScannerSvc.class.getSimpleName());
    
    private static final File scanTemp = new File("scan.tmp");
    
    private static final TForms T_FORMS = new TForms();
    
    /**
     Время инициализации
     */
    private static final long startClassTime = System.currentTimeMillis();
    
    private static final Preferences PREFERENCES = AppComponents.getUserPref();
    
    private static List<String> minimessageToUser = new ArrayList<>();
    
    @SuppressWarnings("CanBeFinal")
    private Connection connection;
    
    private String thePc = "";
    
    private NetScanCtr classOption;
    
    private Model model;
    
    private HttpServletRequest request;
    
    private long lastSt;
    
    public NetScannerSvc() {
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
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NetScannerSvc{");
        try {
            sb.append("connection=").append(connection.getWarnings());
            sb.append(", thePc='").append(thePc).append('\'');
            
            sb.append(", model=").append(model.asMap().size());
            sb.append(", request=").append(request.getRequestURI());
            sb.append(", lastSt=").append(new Date(lastSt));
        }
        catch (RuntimeException | SQLException e) {
            sb.append(MessageFormat.format("Exception: {0} in {1}.toString()", e.getMessage(), this.getClass().getSimpleName()));
        }
        sb.append('}');
        return sb.toString();
    }
    
    public String fillAttribute(String attributeName) {
        InformationFactory informationFactory = InformationFactory.getInstance(InformationFactory.LOCAL);
        return informationFactory.getInfoAbout(attributeName);
    }
    
    @Override
    public void setClassOption(Object classOption) {
        if (classOption instanceof NetScanCtr) {
            this.classOption = (NetScanCtr) classOption;
        }
        else {
            this.thePc = (String) classOption;
        }
    }
    
    @Override
    public String fillWebModel() {
        Set<String> pcNamesSet = NetKeeper.getPcNamesSet();
        if (classOption == null) {
            throw new InvokeIllegalException("SET CLASS OPTION: " + this.getClass().getSimpleName());
        }
        else {
            this.model = classOption.getModel();
            this.request = classOption.getRequest();
            try {
                messageToUser.info(checkMapSizeAndDoAction(model, request, lastSt));
                return new TForms().fromArray(pcNamesSet, true);
            }
            catch (ExecutionException | InterruptedException | TimeoutException e) {
                return MessageFormat.format("NetScannerSvc.getInfo: {0}, ({1})", e.getMessage(), e.getClass().getName());
            }
        }
    }
    
    private @NotNull String checkMapSizeAndDoAction(Model model, HttpServletRequest request, long lastSt) throws ExecutionException, InterruptedException, TimeoutException {
        this.model = classOption.getModel();
        this.request = classOption.getRequest();
        this.lastSt = classOption.getLastScan();
        
        int thisTotpc = Integer.parseInt(PROPERTIES.getProperty(PropertiesNames.PR_TOTPC, "259"));
        
        if ((scanTemp.isFile() && scanTemp.exists())) {
            mapSizeBigger(thisTotpc);
        }
        else {
            timeCheck(thisTotpc - NetKeeper.getNetworkPCs().size(), lastSt / 1000);
        }
        return MessageFormat.format("File scan.tmp - {0}", scanTemp.exists());
    }
    
    private void mapSizeBigger(int thisTotpc) throws ExecutionException, InterruptedException, TimeoutException {
        long timeLeft = TimeUnit.MILLISECONDS.toSeconds(lastSt - System.currentTimeMillis());
        int pcWas = Integer.parseInt(PROPERTIES.getProperty(PropertiesNames.PR_ONLINEPC, "0"));
        int remainPC = thisTotpc - NetKeeper.getNetworkPCs().size();
        boolean newPSs = remainPC < 0;
    
        String msg = new ScanMessagesCreator().getMsg(timeLeft);
        String title = new ScanMessagesCreator().getTitle(remainPC, thisTotpc, pcWas);
        String pcValue = new ScanMessagesCreator().fromArray();
        
        messageToUser.info(msg);
        model.addAttribute("left", msg).addAttribute("pc", pcValue).addAttribute(ModelAttributeNames.ATT_TITLE, title);
        
        if (newPSs) {
            newPCCheck(pcValue, remainPC);
        }
        else {
            noNewPCCheck(remainPC);
        }
        
        timeCheck(remainPC, lastSt / 1000);
    }
    
    private void timeCheck(int remainPC, long lastScanEpoch) throws ExecutionException, InterruptedException, TimeoutException {
        Runnable scanRun = new Scanner(new Date(lastScanEpoch * 1000));
        LocalTime lastScanLocalTime = LocalDateTime.ofEpochSecond(lastScanEpoch, 0, ZoneOffset.ofHours(3)).toLocalTime();
        boolean isSystemTimeBigger = (System.currentTimeMillis() > lastScanEpoch * 1000);
        if (!(scanTemp.exists())) {
            classOption.getModel().addAttribute(PropertiesNames.PR_AND_ATT_NEWPC, lastScanLocalTime);
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
    
    private void newPCCheck(String pcValue, double remainPC) {
        FileSystemWorker.writeFile(ConstantsNet.BEANNAME_LASTNETSCAN, pcValue);
        model.addAttribute(PropertiesNames.PR_AND_ATT_NEWPC, "Добавлены компы! " + Math.abs(remainPC) + " шт.");
        PROPERTIES.setProperty(PropertiesNames.PR_TOTPC, String.valueOf(NetKeeper.getNetworkPCs().size()));
        PROPERTIES.setProperty(PropertiesNames.PR_AND_ATT_NEWPC, String.valueOf(remainPC));
    }
    
    private void noNewPCCheck(int remainPC) {
        if (remainPC < ConstantsFor.INT_ANSWER) {
            PROPERTIES.setProperty(PropertiesNames.PR_TOTPC, String.valueOf(NetKeeper.getNetworkPCs().size()));
        }
    }
    
    void setThePc(String thePc) {
        this.thePc = thePc;
    }
    
    private Set<String> theSETOfPcNames() {
        messageToUser.info(new Scanner(new Date(Long.parseLong(PROPERTIES.getProperty(PropertiesNames.PR_LASTSCAN, "0")))).getPingResultStr());
        return NetKeeper.getPcNamesSet();
    }
    
    private Set<String> theSETOfPCNamesPref(String prefixPcName) {
        InformationFactory databaseInfo = InformationFactory.getInstance(InformationFactory.LOCAL);
        final long startMethTime = System.currentTimeMillis();
        String pcsString;
        for (String pcName : getCycleNames(prefixPcName)) {
            databaseInfo.getInfoAbout(pcName);
        }
        NetKeeper.getNetworkPCs().put("<h4>" + prefixPcName + "     " + NetKeeper.getPcNamesSet().size() + "</h4>", true);
        try {
            pcsString = new ScanMessagesCreator().writeDB();
            messageToUser.info(pcsString);
        }
        catch (SQLException e) {
            messageToUser.error(e.getMessage());
        }
        String elapsedTime = "<b>Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startMethTime) + " sec.</b> " + LocalTime.now();
        NetKeeper.getPcNamesSet().add(elapsedTime);
        return NetKeeper.getPcNamesSet();
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
    
    @SuppressWarnings("MagicNumber")
    private static void runAfterAllScan() {
        float upTime = (float) (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startClassTime)) / UsefulUtilities.ONE_HOUR_IN_MIN;
        String compNameUsers = T_FORMS.fromArray(ConstantsNet.getPCnameUsersMap(), false);
        String psUser = T_FORMS.fromArrayUsers(ConstantsNet.getPcUMap(), false);
        String msgTimeSp = MessageFormat.format("NetScannerSvc.getPCsAsync method spend {0} seconds.", (float) (System.currentTimeMillis() - startClassTime) / 1000);
        String valueOfPropLastScan = String.valueOf((System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY)));
        
        PROPERTIES.setProperty(PropertiesNames.PR_LASTSCAN, valueOfPropLastScan);
        minimessageToUser.add(compNameUsers);
        minimessageToUser.add(psUser);
        minimessageToUser.add(msgTimeSp);
        minimessageToUser.add(T_FORMS.fromArray(PROPERTIES, false));
        
        ConcurrentNavigableMap<String, Boolean> lastStateOfPCs = NetKeeper.getNetworkPCs();
        
        FileSystemWorker.writeFile(ConstantsNet.BEANNAME_LASTNETSCAN, lastStateOfPCs.navigableKeySet().stream());
        FileSystemWorker.writeFile(NetScannerSvc.class.getSimpleName() + ".mini", minimessageToUser);
        FileSystemWorker.writeFile("unused.ips", NetKeeper.getUnusedNamesTree().stream());
        
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
    
    private class Scanner implements NetScanService {
        
        
        private Date lastScanDate;
        
        @Contract(pure = true)
        Scanner(Date lastScanDate) {
            this.lastScanDate = lastScanDate;
        }
        
        @Override
        public String getExecution() {
            throw new TODOException("18.08.2019 (22:17)");
        }
        
        @Override
        public String getPingResultStr() {
            ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
            String retStr;
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
            catch (InvokeIllegalException e) {
                messageToUser.error(MessageFormat
                        .format("Scanner.getPingResultStr {0} - {1}\nStack:\n{2}", e.getClass().getTypeName(), e.getMessage(), new TForms().fromArray(e)));
            }
            AppComponents.threadConfig().execByThreadConfig(this::scanPCPrefix);
            long[] deadlockedThreads = mxBean.findDeadlockedThreads();
            if (deadlockedThreads != null) {
                retStr = "You have a deadLock(s): " + Arrays.toString(deadlockedThreads);
                System.err.println(retStr);
            }
            else {
                long cpuTimeTotal = 0;
                for (long threadId : mxBean.getAllThreadIds()) {
                    cpuTimeTotal += mxBean.getThreadCpuTime(threadId);
                }
                cpuTimeTotal = TimeUnit.NANOSECONDS.toSeconds(cpuTimeTotal);
                retStr = MessageFormat
                        .format("Peak was {0} threads, now: {1}. Time: {2} millis.", mxBean.getPeakThreadCount(), mxBean.getThreadCount(), cpuTimeTotal);
                minimessageToUser.add(retStr);
            }
            return retStr;
        }
        
        private void scanPCPrefix() {
            for (String s : ConstantsNet.getPcPrefixes()) {
                Thread.currentThread().setName(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startClassTime) + "-sec");
                NetKeeper.getPcNamesSet().clear();
                NetKeeper.getPcNamesSet().addAll(theSETOfPCNamesPref(s));
                AppComponents.threadConfig().thrNameSet("pcGET");
            }
            String elapsedTime = "Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startClassTime) + " sec.";
            NetKeeper.getPcNamesSet().add(elapsedTime);
            AppComponents.threadConfig().execByThreadConfig(NetScannerSvc::runAfterAllScan);
        }
        
        @Override
        public void run() {
            if (fileScanTMPCreate(true)) {
                scanIt();
            }
            else {
                throw new InvokeIllegalException(MessageFormat.format("{0} can't create scan.tmp file!", this.getClass().getSimpleName()));
            }
        }
        
        @Async
        private void scanIt() {
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
                PROPERTIES.setProperty(PropertiesNames.PR_LASTSCAN, String.valueOf(System.currentTimeMillis()));
            }
        }
        
        @Override
        public String writeLog() {
            throw new TODOException("18.08.2019 (22:17)");
        }
        
        @Override
        public Runnable getMonitoringRunnable() {
            throw new TODOException("18.08.2019 (22:17)");
        }
        
        @Override
        public String getStatistics() {
            throw new TODOException("18.08.2019 (22:17)");
        }
    
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Scanner{");
            sb.append("lastScanDate=").append(lastScanDate);
            sb.append('}');
            return sb.toString();
        }
    }
    
    
    
    private class ScanMessagesCreator implements Keeper {
        
        
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
        
        private @NotNull String getMsg(long timeLeft) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(timeLeft);
            stringBuilder.append(" seconds (");
            stringBuilder.append((float) timeLeft / UsefulUtilities.ONE_HOUR_IN_MIN);
            stringBuilder.append(" min) left<br>Delay period is ");
            stringBuilder.append(DURATION_MIN);
            return stringBuilder.toString();
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
        
        private String writeDB() throws SQLException {
            int exUpInt = 0;
            List<String> list = new ArrayList<>();
            try (PreparedStatement p = connection.prepareStatement("insert into  velkompc (NamePP, AddressPP, SegmentPP , OnlineNow) values (?,?,?,?)")) {
                List<String> toSort = new ArrayList<>(NetKeeper.getPcNamesSet());
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
        
        
    }
    
}
