// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.scanner;


import com.eclipsesource.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.EnableAsync;
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
@Scope(ConstantsFor.SINGLETON)
@EnableAsync(proxyTargetClass = true)
public class PcNamesScanner extends TimerTask implements NetScanService {
    
    
    /**
     Время инициализации
     */
    private final long startClassTime = System.currentTimeMillis();
    
    private static final File scanFile = new File(FileNames.SCAN_TMP);
    
    /**
     {@link ConstantsFor#DELAY}
     */
    static final int DURATION_MIN = (int) ConstantsFor.DELAY;
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, PcNamesScanner.class.getSimpleName());
    
    private long lastScanStamp = InitProperties.getUserPref().getLong(PropertiesNames.LASTSCAN, MyCalen.getLongFromDate(7, 1, 1984, 2, 0));
    
    private long nextScanStamp = InitProperties.getUserPref().getLong(PropertiesNames.LASTSCAN, 0) + TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY * 2);
    
    private static List<String> logMini = new ArrayList<>();
    
    private static final ExecutorService RUN_SERVICE = Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor());
    
    private String thePc = "";
    
    @SuppressWarnings("InstanceVariableOfConcreteClass") private NetScanCtr classOption;
    
    private Model model;
    
    private HttpServletRequest request;
    
    private Thread scanThread;
    
    public Thread getScanThread() {
        return scanThread;
    }
    
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
            this.model = ((NetScanCtr) classOption).getModel();
            this.request = ((NetScanCtr) classOption).getRequest();
        }
        else {
            this.thePc = (String) classOption;
        }
    }
    
    @Override
    public String getExecution() {
        return new PcNamesScanner.ScanMessagesCreator().fillUserPCForWEBModel();
    }
    
    @Override
    public String getPingResultStr() {
        return new PcNamesScanner.ScanMessagesCreator().fillUserPCForWEBModel();
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
    
    @SuppressWarnings("DuplicateStringLiteralInspection")
    @Override
    public String toString() {
        JsonObject jsonObject = new JsonObject();
        try {
            jsonObject.add("startClassTime", startClassTime)
                .add("lastScanStamp", new Date(lastScanStamp).toString())
                .add("nextScanStamp", new Date(nextScanStamp).toString())
                .add("minutes", TimeUnit.MILLISECONDS.toMinutes(scheduledExecutionTime()))
                .add(ModelAttributeNames.THEPC, thePc)
                .add("classOption", classOption.toString());
        }
        catch (RuntimeException e) {
            messageToUser.error(PcNamesScanner.class.getSimpleName(), e.getMessage(), " see line: 195 ***");
        }
        return jsonObject.toString();
    }
    
    @Override
    public long scheduledExecutionTime() {
        return System.currentTimeMillis() - startClassTime;
    }
    
    /**
     @see PcNamesScannerTest#testRun()
     */
    @Override
    public void run() {
        this.scanThread = new Thread(new PcNamesScanner.ScannerUSR());
        if (classOption == null) {
            throw new InvokeIllegalException(MessageFormat.format("SET CLASS OPTION: {0} in {1}", NetScanCtr.class.getSimpleName(), this.getClass().getSimpleName()));
        }
        this.model = classOption.getModel();
        this.request = classOption.getRequest();
        synchronized(scanFile) {
            isMapSizeBigger(Integer.parseInt(InitProperties.getUserPref().get(PropertiesNames.TOTPC, "269")));
        }
    }
    
    private void isMapSizeBigger(int thisTotalPC) {
        int pcWas = Integer.parseInt(InitProperties.getTheProps().getProperty(PropertiesNames.ONLINEPC, "0"));
        int remainPC = thisTotalPC - NetKeeper.getUsersScanWebModelMapWithHTMLLinks().size();
        boolean newPSs = remainPC < 0;
    
        PcNamesScanner.ScanMessagesCreator creator = new PcNamesScanner.ScanMessagesCreator();
        String msg = creator.getMsg();
        String title = creator.getTitle(remainPC, thisTotalPC, pcWas);
        String pcValue = creator.fillUserPCForWEBModel();
        model.addAttribute("left", msg).addAttribute("pc", pcValue).addAttribute(ModelAttributeNames.TITLE, title);
        if (newPSs) {
            newPCCheck(pcValue, remainPC);
        }
        else {
            noNewPCCheck(remainPC);
        }
        checkTime();
    }
    
    private void newPCCheck(String pcValue, double remainPC) {
        FileSystemWorker.writeFile(ConstantsNet.BEANNAME_LASTNETSCAN, pcValue);
        classOption.getModel().addAttribute(ModelAttributeNames.NEWPC, "Добавлены компы! " + Math.abs(remainPC) + " шт.");
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
    
    /**
     Step 3
     
     @return true, if system time > plan time
     
     @see PcNamesScannerTest#testIsTime()
     */
    protected boolean checkTime() {
        long nextScanLong = InitProperties.getUserPref().getLong(PropertiesNames.NEXTSCAN, 1);
        this.nextScanStamp = nextScanLong;
        boolean isSystemTimeBigger = System.currentTimeMillis() > nextScanLong;
        model.addAttribute(ModelAttributeNames.SERVICEINFO, MessageFormat.format("{0} last<br>{1}", new Date(lastScanStamp), new Date(nextScanStamp)));
        if (isSystemTimeBigger) {
            catchingConcurencyException();
        }
        else {
            String minLeftToModel = TimeUnit.MILLISECONDS.toMinutes(nextScanStamp - System.currentTimeMillis()) + " minutes left";
            minLeftToModel = new PageGenerationHelper().setColor(ConstantsFor.YELLOW, minLeftToModel);
            model.addAttribute(ModelAttributeNames.PCS, minLeftToModel);
            messageToUser.warning(this.getClass().getSimpleName(), minLeftToModel, String.valueOf(isSystemTimeBigger));
        }
        return isSystemTimeBigger;
    }
    
    private void catchingConcurencyException() {
        try {
            noFileExists();
        }
        catch (ConcurrentModificationException | ExecutionException | TimeoutException e) {
            messageToUser.error("PcNamesScanner.catchingConcurencyException", e.getMessage(), AbstractForms.exceptionNetworker(e.getStackTrace()));
        }
        finally {
            model.addAttribute(ModelAttributeNames.TITLE, new Date(nextScanStamp));
        }
    }
    
    private void noFileExists() throws ExecutionException, TimeoutException {
        Thread.currentThread().setName(thePc);
        this.scanThread = new Thread(new PcNamesScanner.ScannerUSR());
        InitProperties.getTheProps().setProperty(PropertiesNames.LASTSCAN, String.valueOf(System.currentTimeMillis()));
        InitProperties.getInstance(InitProperties.FILE).setProps(InitProperties.getTheProps());
        InitProperties.setPreference(PropertiesNames.LASTSCAN, String.valueOf(System.currentTimeMillis()));
        scanThread.start();
        messageToUser.info(this.getClass().getSimpleName(), scanThread.getName(), scanThread.getState().name());
        defineNewTask();
    }
    
    private void defineNewTask() {
        setPrefProps();
        this.scanThread = new Thread(new PcNamesScanner.ScannerUSR());
        boolean timeAndFileOk = (System.currentTimeMillis() > nextScanStamp & fileScanTMPCreate(false));
        if (!scanThread.isAlive() && timeAndFileOk) {
            classOption.starterNetScan();
        }
        else {
            messageToUser.info(this.getClass().getSimpleName(), "defineNewTask", String.valueOf(new File(FileNames.SCAN_TMP).exists()));
            messageToUser.warning(this.getClass().getSimpleName(), MessageFormat.format("{0} state: {1}", scanThread.getName(), scanThread.getState()), AbstractForms
                .exceptionNetworker(scanThread.getStackTrace()));
        }
    }
    
    private void setPrefProps() {
        String lastScan = String.valueOf(System.currentTimeMillis());
        InitProperties.setPreference(PropertiesNames.LASTSCAN, lastScan);
        InitProperties.getTheProps().setProperty(PropertiesNames.LASTSCAN, lastScan);
        this.lastScanStamp = System.currentTimeMillis();
        
        long nextStart = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY);
        String nextScan = String.valueOf(nextStart);
        InitProperties.setPreference(PropertiesNames.NEXTSCAN, nextScan);
        InitProperties.getTheProps().setProperty(PropertiesNames.NEXTSCAN, nextScan);
        this.nextScanStamp = nextStart;
    
        InitProperties.getInstance(InitProperties.FILE).setProps(InitProperties.getTheProps());
        InitProperties.getInstance(InitProperties.DB_MEMTABLE).setProps(InitProperties.getTheProps());
    }
    
    /**
     @param createNewFile true, if need new file
     @return true, if create<br>or<br>true if delete, with parameter.
     
     @see PcNamesScannerTest#testFileScanTMPCreate()
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
        boolean bigDBWritten = NetScanService.writeUsersToDBFromSET();
        if (bigDBWritten) {
            NetKeeper.getPcNamesForSendToDatabase().clear();
            messageToUser.info(this.getClass().getSimpleName(), "NetKeeper.getPcNamesForSendToDatabase()", "CLEARED");
        }
        else {
            throw new InvokeIllegalException(this.getClass().getSimpleName() + " ERR to write: NetKeeper.getPcNamesForSendToDatabase");
        }
    }
    
    private class ScannerUSR implements NetScanService {
        
        
        @Override
        public void run() {
            try {
                messageToUser.warn(this.getClass().getSimpleName(), FileNames.INETSTATSIP_CSV, Stats.getIpsInet() + " kb");
                scanIt();
            }
            catch (RuntimeException e) {
                String title = MessageFormat.format("{0}, exception: ", e.getMessage(), e.getClass().getSimpleName());
                messageToUser.error("ScannerUSR.run", e.getMessage(), AbstractForms.exceptionNetworker(e.getStackTrace()));
            }
            finally {
                messageToUser.info("ScannerUSR.run", new ScanMessagesCreator().fillUserPCForWEBModel(), "From Finally");
            }
        }
        
        private void scanIt() {
            TForms tForms = new TForms();
            ConcurrentNavigableMap<String, Boolean> linksMap = NetKeeper.getUsersScanWebModelMapWithHTMLLinks();
            if (request != null && request.getQueryString() != null) {
                linksMap.clear();
                InitProperties.setPreference(PropertiesNames.ONLINEPC, String.valueOf(0));
                InitProperties.getTheProps().setProperty(PropertiesNames.ONLINEPC, "0");
                Set<String> pcNames = onePrefixSET(classOption.getRequest().getQueryString());
                classOption.getModel()
                    .addAttribute(ModelAttributeNames.TITLE, MessageFormat.format("Last: {0}, next: {1}", new Date(lastScanStamp), new Date(nextScanStamp)))
                    .addAttribute(ModelAttributeNames.PC, tForms.fromArray(pcNames, true));
            }
            else {
                linksMap.clear();
                InitProperties.setPreference(PropertiesNames.ONLINEPC, String.valueOf(0));
                InitProperties.getTheProps().setProperty(PropertiesNames.ONLINEPC, "0");
                getExecution();
                model.addAttribute(ModelAttributeNames.TITLE, MessageFormat.format("Last: {0}, next: {1}", new Date(lastScanStamp), new Date(nextScanStamp)))
                    .addAttribute(ModelAttributeNames.PC, tForms.fromArray(NetKeeper.getPcNamesForSendToDatabase(), true));
            }
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
        public String getPingResultStr() {
            return new PcNamesScanner.ScanMessagesCreator().fillUserPCForWEBModel();
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
            NetKeeper.getUsersScanWebModelMapWithHTMLLinks().clear();
            return new File(FileNames.LASTNETSCAN_TXT).toPath().toAbsolutePath().normalize().toString();
        }
        
        @Override
        public Runnable getMonitoringRunnable() {
            return this;
        }
        
        @Override
        public String getStatistics() {
            return new PcNamesScanner.ScanMessagesCreator().fillUserPCForWEBModel();
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
                    InitProperties.getTheProps().getProperty(PropertiesNames.ONLINEPC, "0"), upTime, new Date(nextScanStamp));
            try {
                AppComponents.getMessageSwing(this.getClass().getSimpleName()).infoTimer((int) ConstantsFor.DELAY, bodyMsg);
            }
            finally {
                defineNewTask();
            }
    
        }
    }
    
    
    
    private class ScanMessagesCreator implements Keeper {
    
    
        private @NotNull String getMsg() {
            long timeLeft = InitProperties.getUserPref().getLong(PropertiesNames.NEXTSCAN, MyCalen.getLongFromDate(7, 1, 1984, 2, 0));
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(timeLeft);
            stringBuilder.append(" seconds (");
            stringBuilder.append((float) timeLeft / ConstantsFor.ONE_HOUR_IN_MIN);
            stringBuilder.append(" min) left<br>Delay period is ");
            stringBuilder.append(DURATION_MIN);
            return stringBuilder.toString();
        }
    
        private @NotNull String getTitle(int remainPC, int thisTotalPC, int pcWas) {
            StringBuilder titleBuilder = new StringBuilder();
            titleBuilder.append(remainPC);
            titleBuilder.append("/");
            titleBuilder.append(thisTotalPC);
            titleBuilder.append(" PCs (");
            titleBuilder.append(InitProperties.getTheProps().getProperty(PropertiesNames.ONLINEPC, "0"));
            titleBuilder.append("/");
            titleBuilder.append(pcWas);
            titleBuilder.append(") Next run ");
            titleBuilder.append(new Date(lastScanStamp));
            return titleBuilder.toString();
        }
        
        private @NotNull String fillUserPCForWEBModel() {
            StringBuilder brStringBuilder = new StringBuilder();
            brStringBuilder.append(STR_P);
            ConcurrentNavigableMap<String, Boolean> linksMap = NetKeeper.getUsersScanWebModelMapWithHTMLLinks();
            if (linksMap.size() == 0) {
                brStringBuilder.append(FileSystemWorker.readRawFile(new File(FileNames.LASTNETSCAN_TXT).getAbsolutePath()));
            }
            else {
                Set<String> keySet = linksMap.keySet();
                List<String> list = new ArrayList<>(keySet.size());
                list.addAll(keySet);
    
                Collections.sort(list);
                Collections.reverse(list);
                for (String keyMap : list) {
                    String valueMap = String.valueOf(NetKeeper.getUsersScanWebModelMapWithHTMLLinks().get(keyMap));
                    brStringBuilder.append(keyMap).append(" ").append(valueMap).append("<br>");
                }
            }
            return brStringBuilder.toString();
            
        }
    }
}
