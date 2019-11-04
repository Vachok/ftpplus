// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.scanner;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.componentsrepo.htmlgen.PageGenerationHelper;
import ru.vachok.networker.componentsrepo.services.MyCalen;
import ru.vachok.networker.data.Keeper;
import ru.vachok.networker.data.NetKeeper;
import ru.vachok.networker.data.enums.*;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.info.NetScanService;
import ru.vachok.networker.info.stats.Stats;
import ru.vachok.networker.restapi.message.MessageToTray;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.restapi.props.InitProperties;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.*;

import static ru.vachok.networker.data.enums.ConstantsFor.STR_P;


/**
 @see ru.vachok.networker.net.scanner.PcNamesScannerTest
 @since 21.08.2018 (14:40) */
@SuppressWarnings("WeakerAccess")
@Service(ConstantsFor.BEANNAME_NETSCANNERSVC)
@Scope(ConstantsFor.SINGLETON)
public class PcNamesScanner implements NetScanService {
    
    
    public static final String REMOVE = " remove ";
    
    /**
     {@link ConstantsFor#DELAY}
     */
    static final int DURATION_MIN = (int) ConstantsFor.DELAY;
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, PcNamesScanner.class.getSimpleName());
    
    /**
     Время инициализации
     */
    private static final long startClassTime = System.currentTimeMillis();
    
    /**
     {@link AppComponents#getProps()}
     */
    private static final Properties PROPS = AppComponents.getProps();
    
    private static final File fileTmp = new File(FileNames.SCAN_TMP);
    
    protected static final String SCANNER = "PcNamesScanner{";
    
    private static List<String> logMini = new ArrayList<>();
    
    private String thePc = "";
    
    @SuppressWarnings("InstanceVariableOfConcreteClass") private NetScanCtr classOption;
    
    private Model model;
    
    private HttpServletRequest request;
    
    private long lastScanStamp = AppComponents.getUserPref().getLong(PropertiesNames.LASTSCAN, MyCalen.getLongFromDate(7, 1, 1984, 2, 0));
    
    private long nextScanStamp = AppComponents.getUserPref().getLong(PropertiesNames.LASTSCAN, 0) + TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY * 2);
    
    @SuppressWarnings("InstanceVariableOfConcreteClass") private PcNamesScanner.ScannerUSR scanTask;
    
    private ScheduledFuture<?> scheduledFuture;
    
    /**
     @return атрибут модели.
     */
    public String getThePc() {
        return thePc;
    }
    
    public void setThePc(String thePc) {
        this.thePc = thePc;
    }
    
    public void setModel(Model model) {
        this.model = model;
    }
    
    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }
    
    public void setClassOption(Object classOption) {
        if (classOption instanceof NetScanCtr) {
            this.classOption = (NetScanCtr) classOption;
        }
        else {
            this.thePc = (String) classOption;
        }
    }
    
    @Override
    public String getExecution() {
        return new ScanMessagesCreator().fillUserPCForWEBModel();
    }
    
    @Override
    public String getPingResultStr() {
        return new ScanMessagesCreator().fillUserPCForWEBModel();
    }
    
    @Override
    public String writeLog() {
        return FileSystemWorker.writeFile(FileNames.LASTNETSCAN_TXT, AbstractForms.fromArray(NetKeeper.getUsersScanWebModelMapWithHTMLLinks()));
    }
    
    @Override
    public Runnable getMonitoringRunnable() {
        return this;
    }
    
    @Override
    public String getStatistics() {
        String lastNetScanMAP = AbstractForms.fromArray(NetKeeper.getUsersScanWebModelMapWithHTMLLinks().descendingMap())
            .replace(": true", "").replace(": false", "");
        Date lastStamp = new Date(lastScanStamp);
        Date netxStampDate = new Date(nextScanStamp);
        return MessageFormat.format("{0}-{1}<br>PcNamesForSendToDatabase:<br>{2}", lastStamp, netxStampDate, lastNetScanMAP);
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(SCANNER);
        try {
            sb.append("thePc='").append(thePc).append('\'');
            sb.append(", lastScanStamp=").append(new Date(Long.parseLong(AppComponents.getUserPref().get(PropertiesNames.LASTSCAN, String
                .valueOf(MyCalen.getLongFromDate(7, 1, 1984, 2, 0))))));
            sb.append('}');
        }
        catch (NumberFormatException e) {
            messageToUser.error(PcNamesScanner.class.getSimpleName(), e.getMessage(), " see line: 174 ***");
        }
        return sb.toString();
    }
    
    /**
     @see PcNamesScannerTest#testRun()
     */
    @Override
    public void run() {
        if (classOption == null) {
            throw new InvokeIllegalException("SET CLASS OPTION: " + this.getClass().getSimpleName());
        }
        else if (!fileTmp.exists()) {
            this.model = classOption.getModel();
            this.request = classOption.getRequest();
            isMapSizeBigger(Integer.parseInt(AppComponents.getUserPref().get(PropertiesNames.TOTPC, "269")));
        }
        else {
            messageToUser.warn(this.getClass().getSimpleName(), FileNames.SCAN_TMP, toString());
        }
    }
    
    protected void checkTime() {
        boolean isSystemTimeBigger = System.currentTimeMillis() > AppComponents.getUserPref().getLong(PropertiesNames.NEXTSCAN, 0);
        model.addAttribute(ModelAttributeNames.SERVICEINFO, MessageFormat.format("{0} last<br>{1}", new Date(lastScanStamp), new Date(nextScanStamp)));
        if (isSystemTimeBigger) {
            sysTimeBigger();
        }
        else {
            String minLeftToModel = TimeUnit.MILLISECONDS.toMinutes(nextScanStamp - System.currentTimeMillis()) + " minutes left";
            minLeftToModel = new PageGenerationHelper().setColor(ConstantsFor.YELLOW, minLeftToModel);
            model.addAttribute(ModelAttributeNames.PCS, minLeftToModel);
        }
    }
    
    protected @NotNull Set<String> onePrefixSET(String prefixPcName) {
        final long startMethTime = System.currentTimeMillis();
        Collection<String> autoPcNames = new ArrayList<>(getCycleNames(prefixPcName));
    
        for (String pcName : autoPcNames) {
            InformationFactory informationFactory = InformationFactory.getInstance(pcName);
            messageToUser
                .info(pcName, NetKeeper.getPcNamesForSendToDatabase().size() + " NetKeeper.getPcNamesForSendToDatabase() size", informationFactory.getInfo());
        }
        Set<String> copySet = new HashSet<>(NetKeeper.getPcNamesForSendToDatabase());
        String elapsedTime = MessageFormat
            .format("<b>Elapsed: {0} sec.</b> {1}", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startMethTime), LocalTime.now());
        copySet.add(elapsedTime);
        dbSend(prefixPcName);
        return copySet;
    }
    
    /**
     1. {@link #getNamesCount(String)}
     
     @param namePCPrefix префикс имени ПК
     @return обработанные имена ПК, для пинга
     
     @see #onePrefixSET(String)
     */
    private @NotNull List<String> getCycleNames(String namePCPrefix) {
        if (namePCPrefix == null) {
            namePCPrefix = "pp";
        }
        int inDex = getNamesCount(namePCPrefix);
        String nameCount;
        List<String> list = new ArrayList<>();
        int pcNum = 0;
        for (int i = 1; i < inDex; i++) {
            //noinspection IfMayBeConditional
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
    
    private void dbSend(String prefixPcName) {
        prefixToMap(prefixPcName);
        try {
            closePrefix();
        }
        catch (InvokeIllegalException e) {
            AppComponents.threadConfig().execByThreadConfig(NetScanService::writeUsersToDBFromSET);
            String title = MessageFormat.format("{0}, exception: ", e.getMessage(), e.getClass().getSimpleName());
            MessageToUser.getInstance(MessageToUser.DB, "PcNamesScanner").error("PcNamesScanner", title, AbstractForms.exceptionNetworker(e.getStackTrace()));
        }
        
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
    
    private void prefixToMap(String prefixPcName) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<h4>");
        stringBuilder.append(prefixPcName);
        stringBuilder.append("     ");
        stringBuilder.append(NetKeeper.getPcNamesForSendToDatabase().size());
        stringBuilder.append("</h4>");
        NetKeeper.getUsersScanWebModelMapWithHTMLLinks().put(stringBuilder.toString(), true);
    }
    
    /**
     @throws InvokeIllegalException если БД не записана
     */
    private void closePrefix() {
        boolean bigDBWritten = NetScanService.writeUsersToDBFromSET();
        if (bigDBWritten) {
            NetKeeper.getPcNamesForSendToDatabase().clear();
            messageToUser.info(this.getClass().getSimpleName(), "NetKeeper.getPcNamesForSendToDatabase()", "CLEARED");
        }
        else {
            throw new InvokeIllegalException(this.getClass().getSimpleName() + " ERR to write: NetKeeper.getPcNamesForSendToDatabase");
        }
    }
    
    private void isMapSizeBigger(int thisTotalPC) {
        long timeLeft = TimeUnit.MILLISECONDS.toSeconds(nextScanStamp - System.currentTimeMillis());
        int pcWas = Integer.parseInt(PROPS.getProperty(PropertiesNames.ONLINEPC, "0"));
        int remainPC = thisTotalPC - NetKeeper.getUsersScanWebModelMapWithHTMLLinks().size();
        boolean newPSs = remainPC < 0;
        
        String msg = new ScanMessagesCreator().getMsg(timeLeft);
        String title = new ScanMessagesCreator().getTitle(remainPC, thisTotalPC, pcWas);
        String pcValue = new ScanMessagesCreator().fillUserPCForWEBModel();
        
        messageToUser.info(msg);
        classOption.getModel().addAttribute("left", msg).addAttribute("pc", pcValue).addAttribute(ModelAttributeNames.TITLE, title);
        if (newPSs) {
            newPCCheck(pcValue, remainPC);
        }
        else {
            noNewPCCheck(remainPC);
        }
        try {
            checkTime();
        }
        catch (InvokeIllegalException e) {
            messageToUser.error(PcNamesScanner.class.getSimpleName(), e.getMessage(), " see line: 334 ***");
        }
    }
    
    private void sysTimeBigger() {
        ThreadPoolTaskScheduler taskScheduler = AppComponents.threadConfig().getTaskScheduler();
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(1);
        this.scanTask = new PcNamesScanner.ScannerUSR(startSignal, doneSignal);
        this.scheduledFuture = taskScheduler.scheduleAtFixedRate(scanTask,
            new Date(AppComponents.getUserPref().getLong(PropertiesNames.NEXTSCAN, 0)), TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY));
        if (!fileTmp.exists()) {
            messageToUser.warn(this.getClass().getSimpleName(), FileNames.SCAN_TMP, String.valueOf(scanTask.fileScanTMPCreate(true)));
            try {
                tryScan(startSignal, doneSignal);
            }
            catch (ConcurrentModificationException e) {
                String title = MessageFormat.format("{0}, exception: ", e.getMessage(), e.getClass().getSimpleName());
                MessageToUser.getInstance(MessageToUser.DB, "PcNamesScanner").error("PcNamesScanner", title, AbstractForms.exceptionNetworker(e.getStackTrace()));
            }
        }
        else {
            throw new InvokeIllegalException(this.getClass().getName() + REMOVE + FileNames.SCAN_TMP);
        }
    }
    
    private void tryScan(@NotNull CountDownLatch startSignal, @NotNull CountDownLatch doneSignal) {
        startSignal.countDown();
        noFileExists(doneSignal);
        String modelTitle = MessageFormat.format("Scan is Done {0}. Next after {1} minutes", scheduledFuture.isDone(), scheduledFuture.getDelay(TimeUnit.MINUTES));
        messageToUser.warn(this.getClass().getSimpleName(), "tryScan", modelTitle);
        model.addAttribute(ModelAttributeNames.TITLE, modelTitle);
    }
    
    private void noFileExists(@NotNull CountDownLatch doneSignal) {
        try {
            PROPS.setProperty(PropertiesNames.LASTSCAN, String.valueOf(System.currentTimeMillis()));
            InitProperties.getInstance(InitProperties.FILE).setProps(PROPS);
            UsefulUtilities.setPreference(PropertiesNames.LASTSCAN, String.valueOf(System.currentTimeMillis()));
            scheduledFuture.get();
            doneSignal.await();
        }
        catch (InterruptedException | ExecutionException e) {
            String title = MessageFormat.format("{0}, exception: ", e.getMessage(), e.getClass().getSimpleName());
            MessageToUser.getInstance(MessageToUser.DB, "PcNamesScanner").error("PcNamesScanner", title, AbstractForms.exceptionNetworker(e.getStackTrace()));
            messageToUser.error(PcNamesScanner.class.getSimpleName(), e.getMessage(), " see line: 376 ***");
            this.scheduledFuture = AppComponents.threadConfig().getTaskScheduler().scheduleAtFixedRate(scanTask,
                new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY)), TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY));
            isMapSizeBigger(Integer.parseInt(AppComponents.getUserPref().get(PropertiesNames.TOTPC, "269")));
        }
        setPrefProps();
    }
    
    private void setPrefProps() {
        String lastScan = String.valueOf(System.currentTimeMillis());
        UsefulUtilities.setPreference(PropertiesNames.LASTSCAN, lastScan);
        AppComponents.getProps().setProperty(PropertiesNames.LASTSCAN, lastScan);
        String nextScan = String.valueOf(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY));
        UsefulUtilities.setPreference(PropertiesNames.NEXTSCAN, nextScan);
        AppComponents.getProps().setProperty(PropertiesNames.NEXTSCAN, nextScan);
        InitProperties.getInstance(InitProperties.FILE).setProps(AppComponents.getProps());
        InitProperties.getInstance(InitProperties.DB_MEMTABLE).setProps(AppComponents.getProps());
    }
    
    private void newPCCheck(String pcValue, double remainPC) {
        FileSystemWorker.writeFile(ConstantsNet.BEANNAME_LASTNETSCAN, pcValue);
        classOption.getModel().addAttribute(ModelAttributeNames.NEWPC, "Добавлены компы! " + Math.abs(remainPC) + " шт.");
        PROPS.setProperty(PropertiesNames.TOTPC, String.valueOf(NetKeeper.getUsersScanWebModelMapWithHTMLLinks().size()));
        UsefulUtilities.setPreference(PropertiesNames.TOTPC, String.valueOf(NetKeeper.getUsersScanWebModelMapWithHTMLLinks().size()));
        PROPS.setProperty(ModelAttributeNames.NEWPC, String.valueOf(remainPC));
    }
    
    private void noNewPCCheck(int remainPC) {
        if (remainPC < ConstantsFor.INT_ANSWER) {
            PROPS.setProperty(PropertiesNames.TOTPC, String.valueOf(NetKeeper.getUsersScanWebModelMapWithHTMLLinks().size()));
            UsefulUtilities.setPreference(PropertiesNames.TOTPC, String.valueOf(NetKeeper.getUsersScanWebModelMapWithHTMLLinks().size()));
        }
    }
    
    private class ScannerUSR implements NetScanService {
        
        
        private final CountDownLatch startSignal;
        
        private final CountDownLatch doneSignal;
        
        private Date nextScanDate;
        
        public ScannerUSR(CountDownLatch startSignal, CountDownLatch doneSignal) {
            this.startSignal = startSignal;
            this.doneSignal = doneSignal;
            String startLongString = AppComponents.getUserPref().get(PropertiesNames.NEXTSCAN, String.valueOf(MyCalen.getLongFromDate(7, 1, 1984, 2, 0)));
            this.nextScanDate = new Date(Long.parseLong(startLongString));
        }
        
        @Override
        public int hashCode() {
            return nextScanDate != null ? nextScanDate.hashCode() : 0;
        }
        
        @Contract(value = ConstantsFor.NULL_FALSE, pure = true)
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            
            PcNamesScanner.ScannerUSR usr = (PcNamesScanner.ScannerUSR) o;
            
            return nextScanDate != null ? nextScanDate.equals(usr.nextScanDate) : usr.nextScanDate == null;
        }
        
        @Override
        public void run() {
            try {
                messageToUser.warn(this.getClass().getSimpleName(), FileNames.INETSTATSIP_CSV, Stats.getIpsInet() + " kb");
                scanIt();
            }
            catch (RuntimeException e) {
                String title = MessageFormat.format("{0}, exception: ", e.getMessage(), e.getClass().getSimpleName());
                MessageToUser.getInstance(MessageToUser.DB, "ScannerUSR").error("ScannerUSR", title, AbstractForms.exceptionNetworker(e.getStackTrace()));
            }
        }
        
        @Async
        private void scanIt() {
            TForms tForms = new TForms();
            ConcurrentNavigableMap<String, Boolean> linksMap = NetKeeper.getUsersScanWebModelMapWithHTMLLinks();
            if (request != null && request.getQueryString() != null) {
                linksMap.clear();
                UsefulUtilities.setPreference(PropertiesNames.ONLINEPC, String.valueOf(0));
                PROPS.setProperty(PropertiesNames.ONLINEPC, "0");
                Set<String> pcNames = onePrefixSET(classOption.getRequest().getQueryString());
                classOption.getModel()
                    .addAttribute(ModelAttributeNames.TITLE, MessageFormat.format("Last: {0}, next: {1}", new Date(lastScanStamp), nextScanDate))
                    .addAttribute(ModelAttributeNames.PC, tForms.fromArray(pcNames, true));
            }
            else {
                linksMap.clear();
                UsefulUtilities.setPreference(PropertiesNames.ONLINEPC, String.valueOf(0));
                PROPS.setProperty(PropertiesNames.ONLINEPC, "0");
                getExecution();
                model.addAttribute(ModelAttributeNames.TITLE, MessageFormat.format("Last: {0}, next: {1}", new Date(lastScanStamp), nextScanDate))
                    .addAttribute(ModelAttributeNames.PC, tForms.fromArray(NetKeeper.getPcNamesForSendToDatabase(), true));
            }
        }
        
        /**
         @return {@link #toString()}
         
         @throws InvokeIllegalException {@link #scanPCPrefix()} , set not written to DB
         */
        @Override
        public String getExecution() {
            try {
                startSignal.await();
                new MessageToTray(this.getClass().getSimpleName())
                    .info("NetScannerSvc started scan", UsefulUtilities.getUpTime(), MessageFormat.format("Last online {0} PCs\n File: {1}",
                        PROPS.getProperty(PropertiesNames.ONLINEPC), new File("scan.tmp").getAbsolutePath()));
                scanPCPrefix();
                doneSignal.countDown();
            }
            catch (InterruptedException e) {
                String title = MessageFormat.format("{0}, exception: ", e.getMessage(), e.getClass().getSimpleName());
                MessageToUser.getInstance(MessageToUser.DB, "ScannerUSR").error("ScannerUSR", title, AbstractForms.exceptionNetworker(e.getStackTrace()));
                Thread.currentThread().checkAccess();
                Thread.currentThread().interrupt();
            }
            return this.toString();
        }
        
        @Override
        public String getPingResultStr() {
            return new ScanMessagesCreator().fillUserPCForWEBModel();
        }
        
        /**
         @throws InvokeIllegalException {@link #onePrefixSET(String)}, not written
         */
        private void scanPCPrefix() {
            Set<String> setToDB = NetKeeper.getPcNamesForSendToDatabase();
            for (String pcNamePREFIX : ConstantsNet.getPcPrefixes()) {
                Thread.currentThread().setName(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startClassTime) + "-sec");
                setToDB.clear();
                UsefulUtilities.ipFlushDNS();
                NetKeeper.getPcNamesForSendToDatabase().addAll(onePrefixSET(pcNamePREFIX));
            }
            String elapsedTime = ConstantsFor.ELAPSED + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startClassTime) + " sec.";
            setToDB.add(elapsedTime);
            AppComponents.threadConfig().execByThreadConfig(this::writeLog);
        }
        
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("ScannerUSR{");
            sb.append("lastScanDate=").append(nextScanDate);
            sb.append('}');
            return sb.toString();
        }
        
        /**
         @return absolute path to file {@link FileNames#LASTNETSCAN_TXT}
 
         @see PcNamesScannerTest#testWriteLog()
         */
        @SuppressWarnings("FeatureEnvy")
        @Override
        public String writeLog() {
            FileSystemWorker.writeFile(FileNames.LASTNETSCAN_TXT, NetKeeper.getUsersScanWebModelMapWithHTMLLinks().navigableKeySet().stream());
            FileSystemWorker.writeFile(PcNamesScanner.class.getSimpleName() + ".mini", logMini);
            FileSystemWorker.writeFile(FileNames.UNUSED_IPS, NetKeeper.getUnusedNamesTree().stream());
            messageToUser.warn(this.getClass().getSimpleName(), FileNames.SCAN_TMP, String.valueOf(scanTask.fileScanTMPCreate(false)));
            setTimesLastNext();
            showScreenMessage();
            MessageToUser.getInstance(MessageToUser.DB, this.getClass().getSimpleName())
                .info(this.getClass().getSimpleName(), FileNames.LASTNETSCAN_TXT, FileSystemWorker.readRawFile(FileNames.LASTNETSCAN_TXT));
            MessageToUser.getInstance(MessageToUser.DB, this.getClass().getSimpleName())
                .info(this.getClass().getSimpleName(), "logMini", AbstractForms.fromArray(logMini));
            return new File(FileNames.LASTNETSCAN_TXT).toPath().toAbsolutePath().normalize().toString();
        }
        
        @Override
        public Runnable getMonitoringRunnable() {
            return this;
        }
        
        @Override
        public String getStatistics() {
            return new ScanMessagesCreator().fillUserPCForWEBModel();
        }
        
        private boolean fileScanTMPCreate(boolean create) {
            File file = new File("scan.tmp");
            try {
                String startDoneMsg = MessageFormat
                    .format("{0} lastScanStamp, {1} nextScanDate. {2}/{3} startSignal/doneSignal", lastScanStamp, nextScanDate, this.startSignal
                        .toString(), this.doneSignal.toString());
                if (create) {
                    file = Files.createFile(file.toPath()).toFile();
                    FileSystemWorker.appendObjectToFile(file, startDoneMsg);
                }
                else {
                    Files.deleteIfExists(Paths.get("scan.tmp"));
                    messageToUser.warn(this.getClass().getSimpleName(), "deleting " + file.getAbsolutePath(), startDoneMsg);
                }
            }
            catch (IOException e) {
                String title = MessageFormat.format("{0}, exception: ", e.getMessage(), e.getClass().getSimpleName());
                MessageToUser.getInstance(MessageToUser.DB, "ScannerUSR").error("ScannerUSR", title, AbstractForms.exceptionNetworker(e.getStackTrace()));
            }
            boolean exists = file.exists() & file.lastModified() > (System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(10));
            
            file.deleteOnExit();
            
            return exists;
        }
        
        private void setTimesLastNext() {
            InitProperties initProperties = InitProperties.getInstance(InitProperties.DB_MEMTABLE);
            long nextStart = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY);
            PROPS.setProperty(PropertiesNames.NEXTSCAN, String.valueOf(nextStart));
            UsefulUtilities.setPreference(PropertiesNames.NEXTSCAN, String.valueOf(nextStart));
            initProperties.setProps(PROPS);
            initProperties = InitProperties.getInstance(InitProperties.FILE);
            initProperties.setProps(PROPS);
            String prefLastNext = MessageFormat.format("{0} last, {1} next", new Date(System.currentTimeMillis()), new Date(nextStart));
            FileSystemWorker.appendObjectToFile(new File(FileNames.LASTNETSCAN_TXT), prefLastNext);
        }
        
        private void showScreenMessage() {
            float upTime = (float) (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startClassTime)) / ConstantsFor.ONE_HOUR_IN_MIN;
            String bodyMsg = MessageFormat.format("Online: {0}.\n{1} min uptime. \n{2} = scan.tmp\n", PROPS.getProperty(PropertiesNames.ONLINEPC, "0"), upTime);
            AppComponents.getMessageSwing(this.getClass().getSimpleName()).infoTimer((int) ConstantsFor.DELAY, bodyMsg);
            FileSystemWorker.appendObjectToFile(new File(FileNames.LASTNETSCAN_TXT), bodyMsg);
            new PcNamesScanner().run();
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        
    }
    
    
    
    private class ScanMessagesCreator implements Keeper {
    
    
        private @NotNull String getTitle(int remainPC, int thisTotalPC, int pcWas) {
            StringBuilder titleBuilder = new StringBuilder();
            titleBuilder.append(remainPC);
            titleBuilder.append("/");
            titleBuilder.append(thisTotalPC);
            titleBuilder.append(" PCs (");
            titleBuilder.append(PROPS.getProperty(PropertiesNames.ONLINEPC, "0"));
            titleBuilder.append("/");
            titleBuilder.append(pcWas);
            titleBuilder.append(") Next run ");
            titleBuilder.append(new Date(lastScanStamp));
            return titleBuilder.toString();
        }
        
        private @NotNull String getMsg(long timeLeft) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(timeLeft);
            stringBuilder.append(" seconds (");
            stringBuilder.append((float) timeLeft / ConstantsFor.ONE_HOUR_IN_MIN);
            stringBuilder.append(" min) left<br>Delay period is ");
            stringBuilder.append(DURATION_MIN);
            return stringBuilder.toString();
        }
        
        private @NotNull String fillUserPCForWEBModel() {
            StringBuilder brStringBuilder = new StringBuilder();
            brStringBuilder.append(STR_P);
            Set<String> keySet = NetKeeper.getUsersScanWebModelMapWithHTMLLinks().keySet();
            List<String> list = new ArrayList<>(keySet.size());
            list.addAll(keySet);
            
            Collections.sort(list);
            Collections.reverse(list);
            for (String keyMap : list) {
                String valueMap = String.valueOf(NetKeeper.getUsersScanWebModelMapWithHTMLLinks().get(keyMap));
                brStringBuilder.append(keyMap).append(" ").append(valueMap).append("<br>");
            }
            return brStringBuilder.toString();
            
        }
    }
}
