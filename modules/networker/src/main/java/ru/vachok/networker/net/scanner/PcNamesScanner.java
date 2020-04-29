// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.scanner;


import com.eclipsesource.json.JsonObject;
import com.google.firebase.database.FirebaseDatabase;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.AppComponents;
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

    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, PcNamesScanner.class.getSimpleName());

    private static final PcNamesScanner pcNamesScanner = new PcNamesScanner();

    private final File lastNetScan = new File(FileNames.LASTNETSCAN_TXT);

    public static PcNamesScanner getI() {
        return pcNamesScanner;
    }

    private static final List<String> logMini = new ArrayList<>();

    private long lastScanStamp = InitProperties.getUserPref().getLong(PropertiesNames.LASTSCAN, MyCalen.getLongFromDate(7, 1, 1984, 2, 0));

    private String thePc = "";

    public String getThePc() {
        return thePc;
    }

    public void setThePc(String thePc) {
        this.thePc = thePc;
    }

    private PcNamesScanner() {
        if (!lastNetScan.exists()) {
            try {
                lastNetScan.createNewFile();
            }
            catch (IOException e) {
                messageToUser.error(e.getMessage());
            }
        }
    }

    @Override
    public int hashCode() {
        int result = (int) (startClassTime ^ (startClassTime >>> 32));
        result = 31 * result + (int) (lastScanStamp ^ (lastScanStamp >>> 32));
        result = 31 * result + thePc.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PcNamesScanner)) {
            return false;
        }

        PcNamesScanner scanner = (PcNamesScanner) o;

        if (startClassTime != scanner.startClassTime) {
            return false;
        }
        if (lastScanStamp != scanner.lastScanStamp) {
            return false;
        }
        return thePc.equals(scanner.thePc);
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
        return FileSystemWorker.writeFile(lastNetScan.getAbsolutePath(), AbstractForms.fromArray(NetKeeper.getUsersScanWebModelMapWithHTMLLinks()));
    }

    @Override
    public Runnable getMonitoringRunnable() {
        return this;
    }

    @Override
    public String getStatistics() {
        Date lastScanDate = new Date(lastScanStamp);
        return MessageFormat.format("{0} lastScanDate.", lastScanDate);
    }

    /**
     @see PcNamesScannerTest#testRun()
     */
    @Override
    public void run() {
        String fileName = this.getClass().getSimpleName() + "." + hashCode();
        isMapSizeBigger(Integer.parseInt(InitProperties.getInstance(InitProperties.DB_MEMTABLE).getProps().getProperty(PropertiesNames.TOTPC, "269")));
        new File(fileName).deleteOnExit();
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

    @NotNull
    protected Set<String> onePrefixSET(String prefixPcName) {
        final long startMethTime = System.currentTimeMillis();
        Collection<String> autoPcNames = new ArrayList<>(getCycleNames(prefixPcName));

        for (String pcName : autoPcNames) {
            InformationFactory informationFactory = InformationFactory.getInstance(pcName);
            informationFactory.getInfo();
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
    @NotNull
    private List<String> getCycleNames(String namePCPrefix) {
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
        Properties props = InitProperties.getInstance(InitProperties.DB_MEMTABLE).getProps();
        int inDex = 0;
        if (qer.equals("no")) {
            inDex = Integer.parseInt(props.getProperty("nopc", String.valueOf(ConstantsNet.NOPC)));
            props.setProperty("nopc", String.valueOf(inDex));
        }
        if (qer.equals("pp")) {
            inDex = Integer.parseInt(props.getProperty("pppc", String.valueOf(ConstantsNet.PPPC)));
            props.setProperty("pppc", String.valueOf(inDex));
        }
        if (qer.equals("do")) {
            inDex = Integer.parseInt(props.getProperty("dopc", String.valueOf(ConstantsNet.DOPC)));
            props.setProperty("dopc", String.valueOf(inDex));
        }
        if (qer.equals("a")) {
            inDex = Integer.parseInt(props.getProperty("apc", String.valueOf(ConstantsNet.APC)));
            props.setProperty("apc", String.valueOf(inDex));
        }
        if (qer.equals("td")) {
            inDex = Integer.parseInt(props.getProperty("tdpc", String.valueOf(ConstantsNet.TDPC)));
            props.setProperty("tdpc", String.valueOf(inDex));
        }
        if (qer.equals("dotd")) {
            inDex = Integer.parseInt(props.getProperty("dotdpc", String.valueOf(ConstantsNet.DOTDPC)));
            props.setProperty("dotdpc", String.valueOf(inDex));
        }
        if (qer.equals("notd")) {
            inDex = Integer.parseInt(props.getProperty("notdpc", String.valueOf(ConstantsNet.NOTDPC)));
            props.setProperty("notdpc", String.valueOf(inDex));
        }
        InitProperties.getInstance(InitProperties.DB_MEMTABLE).setProps(props);
        InitProperties.getInstance(InitProperties.FILE).setProps(props);
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

    private void newPCCheck(String pcValue, double remainPC) {
        FileSystemWorker.writeFile(ConstantsFor.BEANNAME_LASTNETSCAN, pcValue);
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

    private class ScannerUSR implements NetScanService {


        @NotNull private final Properties props = InitProperties.getTheProps();

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

        /**
         @return absolute path to file {@link FileNames#LASTNETSCAN_TXT}

         @see PcNamesScannerTest#testWriteLog()
         */
        @SuppressWarnings("FeatureEnvy")
        @Override
        public String writeLog() {
            String totPC = String.valueOf(NetKeeper.getUsersScanWebModelMapWithHTMLLinks().size());
            FileSystemWorker.writeFile(FileNames.LASTNETSCAN_TXT, NetKeeper.getUsersScanWebModelMapWithHTMLLinks().navigableKeySet().stream());
            FileSystemWorker.writeFile(PcNamesScanner.class.getSimpleName() + ".mini", logMini);
            FileSystemWorker.writeFile(FileNames.UNUSED_IPS, NetKeeper.getUnusedNamesTree().stream());
            showScreenMessage();
            FirebaseDatabase.getInstance().getReference(ConstantsFor.BEANNAME_LASTNETSCAN)
                .setValue(totPC, (error, ref)->messageToUser
                    .error("ScannerUSR.onComplete", error.toException().getMessage(), AbstractForms.networkerTrace(error.toException().getStackTrace())));
            messageToUser.info(this.getClass().getSimpleName(), "logMini", AbstractForms.fromArray(logMini));
            InitProperties.getInstance(InitProperties.DB_MEMTABLE).getProps().setProperty(PropertiesNames.TOTPC, totPC);
            InitProperties.setPreference(PropertiesNames.TOTPC, totPC);
            InitProperties.getInstance(InitProperties.DB_MEMTABLE).setProps(props);
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
                    props.getProperty(PropertiesNames.ONLINEPC), new File(FileNames.SCAN_TMP).getAbsolutePath()));

            return this.toString();
        }

        @Override
        public String getPingResultStr() {
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

        private void scanIt() {
            ConcurrentNavigableMap<String, Boolean> linksMap = NetKeeper.getUsersScanWebModelMapWithHTMLLinks();
            linksMap.clear();
            InitProperties.setPreference(PropertiesNames.ONLINEPC, String.valueOf(0));
            props.setProperty(PropertiesNames.ONLINEPC, "0");
            getExecution();
        }

        @Override
        public Runnable getMonitoringRunnable() {
            return this;
        }

        @Override
        public String getStatistics() {
            return new ScanMessagesCreator().fillUserPCForWEBModel();
        }

        private void showScreenMessage() {
            float upTime = (float) (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startClassTime)) / ConstantsFor.ONE_HOUR_IN_MIN;
            String bodyMsg = MessageFormat
                .format("Online: {0}.\n{1} min uptime. \n{2} next run\n",
                    props.getProperty(PropertiesNames.ONLINEPC, "0"), upTime, new Date(lastScanStamp));
            try {
                MessageToUser.getInstance(MessageToUser.TRAY, this.getClass().getSimpleName()).info(new AppComponents().getFirebaseApp().getName(), "", bodyMsg);
            }
            finally {
                defineNewTask();
            }

        }
    }
}
