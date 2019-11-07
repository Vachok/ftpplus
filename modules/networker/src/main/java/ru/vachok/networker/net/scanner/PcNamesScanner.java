// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.scanner;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import ru.vachok.networker.*;
import ru.vachok.networker.componentsrepo.FakeRequest;
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
@EnableAsync(proxyTargetClass = true)
public class PcNamesScanner implements NetScanService {
    
    
    protected static final String SCANNER = "PcNamesScanner{";
    
    /**
     Время инициализации
     */
    private final long startClassTime = System.currentTimeMillis();
    
    /**
     {@link ConstantsFor#DELAY}
     */
    static final int DURATION_MIN = (int) ConstantsFor.DELAY;
    
    private long lastScanStamp = InitProperties.getUserPref().getLong(PropertiesNames.LASTSCAN, MyCalen.getLongFromDate(7, 1, 1984, 2, 0));
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, PcNamesScanner.class.getSimpleName());
    
    private long nextScanStamp = InitProperties.getUserPref().getLong(PropertiesNames.LASTSCAN, 0) + TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY * 2);
    
    private static List<String> logMini = new ArrayList<>();
    
    @Override
    public String getExecution() {
        return new PcNamesScanner.ScanMessagesCreator().fillUserPCForWEBModel();
    }
    
    private String thePc = "";
    
    @SuppressWarnings("InstanceVariableOfConcreteClass") private NetScanCtr classOption;
    
    private Model model;
    
    private HttpServletRequest request;
    
    @Override
    public String getPingResultStr() {
        return new PcNamesScanner.ScanMessagesCreator().fillUserPCForWEBModel();
    }
    
    @Override
    public String getStatistics() {
        String lastNetScanMAP = AbstractForms.fromArray(NetKeeper.getUsersScanWebModelMapWithHTMLLinks().descendingMap())
                .replace(": true", "").replace(": false", "");
        Date lastStamp = new Date(lastScanStamp);
        Date netxStampDate = new Date(nextScanStamp);
        return MessageFormat.format("{0}-{1}<br>PcNamesForSendToDatabase:<br>{2}", lastStamp, netxStampDate, lastNetScanMAP);
    }
    
    @SuppressWarnings("InstanceVariableOfConcreteClass") private PcNamesScanner.ScannerUSR scanTask;
    
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
    
    @Override
    public String writeLog() {
        return FileSystemWorker.writeFile(FileNames.LASTNETSCAN_TXT, AbstractForms.fromArray(NetKeeper.getUsersScanWebModelMapWithHTMLLinks()));
    }
    
    @Override
    public Runnable getMonitoringRunnable() {
        return this;
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
    
    @Autowired
    public PcNamesScanner() {
    }
    
    @Override
    public int hashCode() {
        int result = (int) (startClassTime ^ (startClassTime >>> 32));
        result = 31 * result + thePc.hashCode();
        result = 31 * result + classOption.hashCode();
        result = 31 * result + (int) (lastScanStamp ^ (lastScanStamp >>> 32));
        result = 31 * result + (int) (nextScanStamp ^ (nextScanStamp >>> 32));
        return result;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        
        PcNamesScanner that = (PcNamesScanner) o;
        
        if (startClassTime != that.startClassTime) {
            return false;
        }
        if (lastScanStamp != that.lastScanStamp) {
            return false;
        }
        if (nextScanStamp != that.nextScanStamp) {
            return false;
        }
        if (!thePc.equals(that.thePc)) {
            return false;
        }
        return classOption.equals(that.classOption);
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(SCANNER);
        try {
            sb.append("thePc='").append(thePc).append('\'');
            sb.append(", lastScanStamp=").append(new Date(Long.parseLong(InitProperties.getUserPref().get(PropertiesNames.LASTSCAN, String
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
            throw new InvokeIllegalException(MessageFormat.format("SET CLASS OPTION: {0} in {1}", NetScanCtr.class.getSimpleName(), this.getClass().getSimpleName()));
        }
        isMapSizeBigger(Integer.parseInt(InitProperties.getUserPref().get(PropertiesNames.TOTPC, "269")));
    }
    
    private void isMapSizeBigger(int thisTotalPC) {
        long timeLeft = TimeUnit.MILLISECONDS.toSeconds(InitProperties.getUserPref().getLong(PropertiesNames.NEXTSCAN, 0) - System.currentTimeMillis());
        int pcWas = Integer.parseInt(AppComponents.getProps().getProperty(PropertiesNames.ONLINEPC, "0"));
        int remainPC = thisTotalPC - NetKeeper.getUsersScanWebModelMapWithHTMLLinks().size();
        boolean newPSs = remainPC < 0;
    
        PcNamesScanner.ScanMessagesCreator creator = new PcNamesScanner.ScanMessagesCreator();
        String msg = creator.getMsg(timeLeft);
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
        AppComponents.getProps().setProperty(PropertiesNames.TOTPC, String.valueOf(NetKeeper.getUsersScanWebModelMapWithHTMLLinks().size()));
        InitProperties.setPreference(PropertiesNames.TOTPC, String.valueOf(NetKeeper.getUsersScanWebModelMapWithHTMLLinks().size()));
        AppComponents.getProps().setProperty(ModelAttributeNames.NEWPC, String.valueOf(remainPC));
    }
    
    private void noNewPCCheck(int remainPC) {
        if (remainPC < ConstantsFor.INT_ANSWER) {
            AppComponents.getProps().setProperty(PropertiesNames.TOTPC, String.valueOf(NetKeeper.getUsersScanWebModelMapWithHTMLLinks().size()));
            InitProperties.setPreference(PropertiesNames.TOTPC, String.valueOf(NetKeeper.getUsersScanWebModelMapWithHTMLLinks().size()));
        }
    }
    
    protected boolean checkTime() {
        boolean isSystemTimeBigger = System.currentTimeMillis() > InitProperties.getUserPref().getLong(PropertiesNames.NEXTSCAN, 0);
        model.addAttribute(ModelAttributeNames.SERVICEINFO, MessageFormat.format("{0} last<br>{1}", new Date(lastScanStamp), new Date(nextScanStamp)));
        if (isSystemTimeBigger && fileScanTMPCreate(true)) {
            sysTimeBigger();
        }
        else {
            String minLeftToModel = TimeUnit.MILLISECONDS.toMinutes(nextScanStamp - System.currentTimeMillis()) + " minutes left";
            minLeftToModel = new PageGenerationHelper().setColor(ConstantsFor.YELLOW, minLeftToModel);
            model.addAttribute(ModelAttributeNames.PCS, minLeftToModel);
        }
        return isSystemTimeBigger;
    }
    
    private static boolean fileScanTMPCreate(boolean createNewFile) {
        File file = new File(FileNames.SCAN_TMP);
        boolean retBool = file.exists();
        try {
            if (createNewFile) {
                file = Files.createFile(file.toPath()).toFile();
                retBool = file.lastModified() > (System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(10));
                messageToUser.info(FileSystemWorker.appendObjectToFile(file, UsefulUtilities.getRunningInformation()));
            }
            else {
                retBool = Files.deleteIfExists(Paths.get("scan.tmp"));
            }
        }
        catch (IOException e) {
            String title = MessageFormat.format("{0}, exception: ", e.getMessage(), e.getClass().getSimpleName());
            MessageToUser.getInstance(MessageToUser.DB, "ScannerUSR").error("ScannerUSR", title, AbstractForms.exceptionNetworker(e.getStackTrace()));
        }
        finally {
            file.deleteOnExit();
        }
        return retBool;
    }
    
    private void sysTimeBigger() {
        try {
            tryScan();
        }
        catch (ConcurrentModificationException e) {
            String title = MessageFormat.format("{0}, exception: ", e.getMessage(), e.getClass().getSimpleName());
            MessageToUser.getInstance(MessageToUser.DB, "PcNamesScanner").error("PcNamesScanner", title, AbstractForms.exceptionNetworker(e.getStackTrace()));
        }
    }
    
    private void tryScan() {
        try {
            noFileExists();
        }
        catch (ExecutionException e) {
            MessageToUser.getInstance(MessageToUser.DB, PcNamesScanner.class.getSimpleName())
                    .error("PcNamesScanner.tryScan", e.getMessage(), AbstractForms.exceptionNetworker(e.getStackTrace()));
            ;
        }
        catch (InterruptedException e) {
            MessageToUser.getInstance(MessageToUser.DB, PcNamesScanner.class.getSimpleName())
                    .error("PcNamesScanner.tryScan", e.getMessage(), AbstractForms.exceptionNetworker(e.getStackTrace()));
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        finally {
            model.addAttribute(ModelAttributeNames.TITLE, new Date(nextScanStamp));
        }
    }
    
    private void noFileExists() throws ExecutionException, InterruptedException {
        this.scanTask = new ScannerUSR();
        try {
            AppComponents.getProps().setProperty(PropertiesNames.LASTSCAN, String.valueOf(System.currentTimeMillis()));
            InitProperties.getInstance(InitProperties.FILE).setProps(AppComponents.getProps());
            InitProperties.setPreference(PropertiesNames.LASTSCAN, String.valueOf(System.currentTimeMillis()));
            Future<?> submit = AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor().submit(this.scanTask);
            submit.get(ConstantsFor.DELAY, TimeUnit.MINUTES);
        }
        catch (TimeoutException e) {
            messageToUser.error(MessageFormat.format("PcNamesScanner.noFileExists", e.getMessage(), AbstractForms.exceptionNetworker(e.getStackTrace())));
        }
        finally {
            defineNewTask();
        }
    }
    
    private void defineNewTask() {
        setPrefProps();
        this.scanTask = new PcNamesScanner.ScannerUSR();
        if (fileScanTMPCreate(false)) {
            classOption.netScan(new FakeRequest(), classOption.getResponse(), this.model);
        }
        else {
            MessageToUser.getInstance(MessageToUser.DB, this.getClass().getSimpleName())
                    .warn("PcNamesScanner.noFileExists", AppComponents.threadConfig().toString(), "Finally block");
        }
    }
    
    private void setPrefProps() {
        String lastScan = String.valueOf(System.currentTimeMillis());
        InitProperties.setPreference(PropertiesNames.LASTSCAN, lastScan);
        AppComponents.getProps().setProperty(PropertiesNames.LASTSCAN, lastScan);
        this.lastScanStamp = System.currentTimeMillis();
        
        long nextStart = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY);
        String nextScan = String.valueOf(nextStart);
        InitProperties.setPreference(PropertiesNames.NEXTSCAN, nextScan);
        AppComponents.getProps().setProperty(PropertiesNames.NEXTSCAN, nextScan);
        this.nextScanStamp = nextStart;
        
        InitProperties.getInstance(InitProperties.FILE).setProps(AppComponents.getProps());
        InitProperties.getInstance(InitProperties.DB_MEMTABLE).setProps(AppComponents.getProps());
    }
    
    private class ScannerUSR implements NetScanService {
        
        
        private Date nextScanDate;
        
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
            finally {
                classOption.netScan(request, classOption.getResponse(), model);
                MessageToUser.getInstance(MessageToUser.DB, PcNamesScanner.ScannerUSR.class.getSimpleName())
                        .warn("ScannerUSR.run", classOption.toString(), "From Finally");
            }
        }
        
        private void scanIt() {
            TForms tForms = new TForms();
            ConcurrentNavigableMap<String, Boolean> linksMap = NetKeeper.getUsersScanWebModelMapWithHTMLLinks();
            if (request != null && request.getQueryString() != null) {
                linksMap.clear();
                InitProperties.setPreference(PropertiesNames.ONLINEPC, String.valueOf(0));
                AppComponents.getProps().setProperty(PropertiesNames.ONLINEPC, "0");
                Set<String> pcNames = onePrefixSET(classOption.getRequest().getQueryString());
                classOption.getModel()
                        .addAttribute(ModelAttributeNames.TITLE, MessageFormat.format("Last: {0}, next: {1}", new Date(lastScanStamp), nextScanDate))
                        .addAttribute(ModelAttributeNames.PC, tForms.fromArray(pcNames, true));
            }
            else {
                linksMap.clear();
                InitProperties.setPreference(PropertiesNames.ONLINEPC, String.valueOf(0));
                AppComponents.getProps().setProperty(PropertiesNames.ONLINEPC, "0");
                getExecution();
                model.addAttribute(ModelAttributeNames.TITLE, MessageFormat.format("Last: {0}, next: {1}", new Date(lastScanStamp), nextScanDate))
                        .addAttribute(ModelAttributeNames.PC, tForms.fromArray(NetKeeper.getPcNamesForSendToDatabase(), true));
            }
        }
        
        /**
         @return {@link #toString()}
         
         @throws InvokeIllegalException {@link #scanPCPrefixes()} , set not written to DB
         */
        @Override
        public String getExecution() {
            try {
                new MessageToTray(this.getClass().getSimpleName())
                        .info("NetScannerSvc started scan", UsefulUtilities.getUpTime(), MessageFormat.format("Last online {0} PCs\n File: {1}",
                                AppComponents.getProps().getProperty(PropertiesNames.ONLINEPC), new File(FileNames.SCAN_TMP).getAbsolutePath()));
                scanPCPrefixes();
            }
            finally {
                model.addAttribute(ModelAttributeNames.NEWPC, toString());
            }
            return this.toString();
        }
        
        @Override
        public String getPingResultStr() {
            return new PcNamesScanner.ScanMessagesCreator().fillUserPCForWEBModel();
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
            MessageToUser.getInstance(MessageToUser.DB, this.getClass().getSimpleName())
                    .info(this.getClass().getSimpleName(), FileNames.LASTNETSCAN_TXT, FileSystemWorker.readRawFile(FileNames.LASTNETSCAN_TXT));
            MessageToUser.getInstance(MessageToUser.DB, this.getClass().getSimpleName())
                    .info(this.getClass().getSimpleName(), "logMini", AbstractForms.fromArray(logMini));
            return new File(FileNames.LASTNETSCAN_TXT).toPath().toAbsolutePath().normalize().toString();
        }
        
        @Override
        public String getStatistics() {
            return new PcNamesScanner.ScanMessagesCreator().fillUserPCForWEBModel();
        }
        
        @Override
        public Runnable getMonitoringRunnable() {
            return this;
        }
        
        /**
         @throws InvokeIllegalException {@link #onePrefixSET(String)}, not written
         */
        private void scanPCPrefixes() {
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
            sb.append("lastScanDate=").append(nextScanDate).append("<p>").append("\n\n");
            sb.append(FileSystemWorker.readRawFile(FileNames.LASTNETSCAN_TXT));
            sb.append('}');
            return sb.toString();
        }
        
        private void setTimesLastNext() {
            Properties props = AppComponents.getProps();
            InitProperties initProperties = InitProperties.getInstance(InitProperties.DB_MEMTABLE);
            long nextStart = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY);
            props.setProperty(PropertiesNames.NEXTSCAN, String.valueOf(nextStart));
            InitProperties.setPreference(PropertiesNames.NEXTSCAN, String.valueOf(nextStart));
            initProperties.setProps(props);
            initProperties = InitProperties.getInstance(InitProperties.FILE);
            initProperties.setProps(props);
            String prefLastNext = MessageFormat.format("{0} last, {1} next", new Date(System.currentTimeMillis()), new Date(nextStart));
            FileSystemWorker.appendObjectToFile(new File(FileNames.LASTNETSCAN_TXT), prefLastNext);
        }
        
        private void showScreenMessage() {
            float upTime = (float) (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startClassTime)) / ConstantsFor.ONE_HOUR_IN_MIN;
            String bodyMsg = MessageFormat.format("Online: {0}.\n{1} min uptime. \n{2} = scan.tmp\n", AppComponents.getProps().getProperty(PropertiesNames.ONLINEPC, "0"), upTime);
            AppComponents.getMessageSwing(this.getClass().getSimpleName()).infoTimer((int) ConstantsFor.DELAY, bodyMsg);
            FileSystemWorker.appendObjectToFile(new File(FileNames.LASTNETSCAN_TXT), bodyMsg);
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
            titleBuilder.append(AppComponents.getProps().getProperty(PropertiesNames.ONLINEPC, "0"));
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
        finally {
            if (NetKeeper.getPcNamesForSendToDatabase().size() > 0) {
                NetScanService.writeUsersToDBFromSET();
            }
            classOption.netScan(new FakeRequest(), classOption.getResponse(), model);
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
}
