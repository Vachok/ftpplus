// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.scanner;


import com.eclipsesource.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import ru.vachok.networker.*;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.componentsrepo.services.MyCalen;
import ru.vachok.networker.data.NetKeeper;
import ru.vachok.networker.data.enums.*;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.info.NetScanService;
import ru.vachok.networker.info.stats.Stats;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.restapi.props.InitProperties;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.TimeUnit;


/**
 @see ru.vachok.networker.net.scanner.PcNamesScannerTest
 @since 21.08.2018 (14:40) */
@Service(ConstantsFor.BEANNAME_NETSCANNERSVC)
@Scope(ConstantsFor.SINGLETON)
@EnableAsync(proxyTargetClass = true)
public class PcNamesScanner implements NetScanService {
    
    
    private final long startClassTime = System.currentTimeMillis();
    
    private static final File scanFile = new File(FileNames.SCAN_TMP);
    
    static final int DURATION_MIN = (int) ConstantsFor.DELAY;
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, PcNamesScanner.class.getSimpleName());
    
    private long lastScanStamp = InitProperties.getUserPref().getLong(PropertiesNames.LASTSCAN, MyCalen.getLongFromDate(7, 1, 1984, 2, 0));
    
    private static List<String> logMini = new ArrayList<>();
    
    private String thePc = "";
    
    public String getThePc() {
        return thePc;
    }
    
    public void setThePc(String thePc) {
        this.thePc = thePc;
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
                .replace(": true", "").replace(OtherConstants.SEMIFALSE, "");
        Date lastScanDate = new Date(lastScanStamp);
        return MessageFormat.format("{0} lastScanDate.", lastScanDate);
    }
    
    @SuppressWarnings("DuplicateStringLiteralInspection")
    @Override
    public String toString() {
        JsonObject jsonObject = new JsonObject();
        try {
            jsonObject.add("startClassTime", startClassTime)
                .add("lastScanStamp", new Date(lastScanStamp).toString())
                    .add(ModelAttributeNames.THEPC, thePc);
        }
        catch (RuntimeException e) {
            messageToUser.error(PcNamesScanner.class.getSimpleName(), e.getMessage(), " see line: 195 ***");
        }
        return jsonObject.toString();
    }
    
    /**
     @see PcNamesScannerTest#testRun()
     */
    @Override
    public void run() {
        FileSystemWorker.writeFile(this.getClass().getSimpleName() + "." + hashCode(), AbstractForms.fromArray(Thread.currentThread().getStackTrace()));
        isMapSizeBigger(Integer.parseInt(InitProperties.getUserPref().get(PropertiesNames.TOTPC, "269")));
    }
    
    private void isMapSizeBigger(int thisTotalPC) {
        try {
            checkPC(thisTotalPC);
        }
        catch (RuntimeException e) {
            messageToUser.error(MessageFormat.format("PcNamesScanner.isMapSizeBigger {0}\n{1}", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace())));
        }
        finally {
            noFileExists();
        }
    }
    
    private void checkPC(int thisTotalPC) {
        int remainPC = thisTotalPC - NetKeeper.getUsersScanWebModelMapWithHTMLLinks().size();
        boolean newPSs = remainPC < 0;
        if (newPSs) {
            newPCCheck(thePc, remainPC);
        }
        else {
            noNewPCCheck(remainPC);
        }
    }
    
    private void newPCCheck(String pcValue, double remainPC) {
        FileSystemWorker.writeFile(ConstantsNet.BEANNAME_LASTNETSCAN, pcValue);
        InitProperties.getTheProps().setProperty(PropertiesNames.TOTPC, String.valueOf(NetKeeper.getUsersScanWebModelMapWithHTMLLinks().size()));
        InitProperties.setPreference(PropertiesNames.TOTPC, String.valueOf(NetKeeper.getUsersScanWebModelMapWithHTMLLinks().size()));
        InitProperties.getTheProps().setProperty(ModelAttributeNames.NEWPC, String.valueOf(remainPC));
    }
    
    private void noNewPCCheck(int remainPC) {
        if (remainPC < ConstantsFor.INT_ANSWER) {
            InitProperties.getTheProps().setProperty(PropertiesNames.TOTPC, String.valueOf(NetKeeper.getUsersScanWebModelMapWithHTMLLinks().size()));
            InitProperties.setPreference(PropertiesNames.TOTPC, String.valueOf(NetKeeper.getUsersScanWebModelMapWithHTMLLinks().size()));
        }
    }
    
    private void noFileExists() {
        InitProperties initProperties = InitProperties.getInstance(InitProperties.FILE);
        Thread.currentThread().setName(thePc);
        initProperties.getProps().setProperty(PropertiesNames.LASTSCAN, String.valueOf(System.currentTimeMillis()));
        InitProperties.setPreference(PropertiesNames.LASTSCAN, String.valueOf(System.currentTimeMillis()));
        File file = new File(FileNames.SCAN_TMP);
        new ScannerUSR().run();
        messageToUser.info(this.getClass().getSimpleName(), file.getAbsolutePath(), String.valueOf(fileScanTMPCreate(false)));
        defineNewTask();
    }
    
    private void defineNewTask() {
        setPrefProps();
        if (!new File(FileNames.SCAN_TMP).exists()) {
            run();
        }
        else {
            messageToUser.info(this.getClass().getSimpleName(), FileNames.SCAN_TMP, String.valueOf(new File(FileNames.SCAN_TMP).exists()));
        }
    }
    
    private void setPrefProps() {
        String lastScan = String.valueOf(System.currentTimeMillis());
        this.lastScanStamp = System.currentTimeMillis();
        
        InitProperties.setPreference(PropertiesNames.LASTSCAN, lastScan);
        InitProperties.getTheProps().setProperty(PropertiesNames.LASTSCAN, lastScan);
        InitProperties.getInstance(InitProperties.FILE).setProps(InitProperties.getTheProps());
        InitProperties.getInstance(InitProperties.DB_MEMTABLE).setProps(InitProperties.getTheProps());
    }
    
    /**
     @param createNewFile true, if need new file
     @return true, if create<br>or<br>true if delete, with parameter.
     */
    static boolean fileScanTMPCreate(boolean createNewFile) {
        boolean retBool = scanFile.exists();
        try {
            if (createNewFile) {
                Files.createFile(scanFile.toPath()).toFile();
                retBool = scanFile.lastModified() > (System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(10));
            }
            else {
                retBool = Files.deleteIfExists(Paths.get(FileNames.SCAN_TMP));
            }
        }
        catch (IOException e) {
            messageToUser.error(PcNamesScanner.class.getSimpleName(), e.getMessage(), " see line: 345 ***");
        }
        finally {
            scanFile.deleteOnExit();
            messageToUser.warn(PcNamesScanner.class.getSimpleName(), FileNames.SCAN_TMP, String.valueOf(retBool));
        }
        return retBool;
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
            AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor().execute((NetScanService::writeUsersToDBFromSET));
            String title = MessageFormat.format("{0}, exception: ", e.getMessage(), e.getClass().getSimpleName());
            messageToUser.error(PcNamesScanner.class.getSimpleName(), e.getMessage(), " see line: 408 ***");
        }
        finally {
            if (NetKeeper.getPcNamesForSendToDatabase().size() > 0) {
                NetScanService.writeUsersToDBFromSET();
            }
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
        boolean isSmallDBWritten = NetScanService.writeUsersToDBFromSET();
        NetKeeper.getPcNamesForSendToDatabase().clear();
        messageToUser.info(this.getClass().getSimpleName(), "NetKeeper.getPcNamesForSendToDatabase cleared", String.valueOf(isSmallDBWritten));
    }
    
    private class ScannerUSR implements NetScanService {
        
        
        @Override
        public String getPingResultStr() {
            return new ScanMessagesCreator().fillUserPCForWEBModel();
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
            setTimesLastNext();
            showScreenMessage();
            messageToUser.info(this.getClass().getSimpleName(), "logMini", AbstractForms.fromArray(logMini));
            InitProperties.getTheProps().setProperty(PropertiesNames.TOTPC, String.valueOf(NetKeeper.getUsersScanWebModelMapWithHTMLLinks().size()));
            NetKeeper.getUsersScanWebModelMapWithHTMLLinks().clear();
            return new File(FileNames.LASTNETSCAN_TXT).toPath().toAbsolutePath().normalize().toString();
        }
        
        
        /**
         @return {@link #toString()}
         
         @throws InvokeIllegalException {@link #scanPCPrefixes()} , set not written to DB
         */
        @Override
        public String getExecution() {
            scanPCPrefixes();
    
            MessageToUser.getInstance(MessageToUser.TRAY, this.getClass().getSimpleName())
                    .info("NetScannerSvc started scan", UsefulUtilities.getUpTime(), MessageFormat.format("Last online {0} PCs\n File: {1}",
                        InitProperties.getTheProps().getProperty(PropertiesNames.ONLINEPC), new File(FileNames.SCAN_TMP).getAbsolutePath()));
    
            return this.toString();
        }
        
        @Override
        public String getStatistics() {
            return new ScanMessagesCreator().fillUserPCForWEBModel();
        }
    
        /**
         @throws InvokeIllegalException {@link #onePrefixSET(String)}, not written
         */
        private void scanPCPrefixes() {
            Set<String> setToDB = NetKeeper.getPcNamesForSendToDatabase();
            for (String pcNamePREFIX : ConstantsNet.getPcPrefixes()) {
                Thread.currentThread().setName(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startClassTime) + "-sec");
                setToDB.clear();
                NetKeeper.getPcNamesForSendToDatabase().addAll(onePrefixSET(pcNamePREFIX));
            }
            String elapsedTime = ConstantsFor.ELAPSED + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startClassTime) + " sec.";
            setToDB.add(elapsedTime);
            writeLog();
        }
        
        @Override
        public String toString() {
            return new StringJoiner(",\n", PcNamesScanner.ScannerUSR.class.getSimpleName() + "[\n", "\n]")
                .add("Map With HTML Links size " + NetKeeper.getUsersScanWebModelMapWithHTMLLinks().size())
                .add("Pc Names For Send To Database size " + NetKeeper.getPcNamesForSendToDatabase().size())
                .toString();
        }
        
        @Override
        public void run() {
            try {
                messageToUser.warn(this.getClass().getSimpleName(), FileNames.INETSTATSIP_CSV, Stats.getIpsInet() + " kb");
                scanIt();
            }
            catch (RuntimeException e) {
                messageToUser.error("ScannerUSR.run", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
            }
            finally {
                messageToUser.info("ScannerUSR.run", new ScanMessagesCreator().fillUserPCForWEBModel(), "From Finally");
            }
        }
    
        @Override
        public Runnable getMonitoringRunnable() {
            return this;
        }
        
        private void scanIt() {
            TForms tForms = new TForms();
            ConcurrentNavigableMap<String, Boolean> linksMap = NetKeeper.getUsersScanWebModelMapWithHTMLLinks();
            linksMap.clear();
            InitProperties.setPreference(PropertiesNames.ONLINEPC, String.valueOf(0));
            InitProperties.getTheProps().setProperty(PropertiesNames.ONLINEPC, "0");
            getExecution();
        }
    
        private void setTimesLastNext() {
            Properties props = InitProperties.getTheProps();
    
            InitProperties initProperties = InitProperties.getInstance(InitProperties.DB_MEMTABLE);
            long nextStart = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY / 10);
    
            props.setProperty(PropertiesNames.NEXTSCAN, String.valueOf(nextStart));
            InitProperties.setPreference(PropertiesNames.NEXTSCAN, String.valueOf(nextStart));
    
            initProperties.setProps(props);
            initProperties = InitProperties.getInstance(InitProperties.FILE);
            initProperties.setProps(props);
        }
        
        private void showScreenMessage() {
            float upTime = (float) (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startClassTime)) / ConstantsFor.ONE_HOUR_IN_MIN;
            String bodyMsg = MessageFormat
                .format("Online: {0}.\n{1} min uptime. \n{2} next run\n",
                        InitProperties.getTheProps().getProperty(PropertiesNames.ONLINEPC, "0"), upTime, new Date(lastScanStamp));
            try {
                AppComponents.getMessageSwing(this.getClass().getSimpleName()).infoTimer((int) ConstantsFor.DELAY, bodyMsg);
            }
            finally {
                defineNewTask();
            }
    
        }
    }
    
    
}
