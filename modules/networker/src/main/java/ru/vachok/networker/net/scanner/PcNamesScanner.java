// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.scanner;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import ru.vachok.networker.*;
import ru.vachok.networker.ad.user.UserInfo;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.componentsrepo.htmlgen.PageGenerationHelper;
import ru.vachok.networker.data.Keeper;
import ru.vachok.networker.data.NetKeeper;
import ru.vachok.networker.data.enums.*;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.info.NetScanService;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToTray;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.restapi.props.InitProperties;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.text.MessageFormat;
import java.time.*;
import java.util.Date;
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
    
    
    /**
     {@link ConstantsFor#DELAY}
     */
    static final int DURATION_MIN = (int) ConstantsFor.DELAY;
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, PcNamesScanner.class.getSimpleName());
    
    private static final File scanTemp = new File("scan.tmp");
    
    /**
     Время инициализации
     */
    private static final long startClassTime = System.currentTimeMillis();
    
    /**
     {@link AppComponents#getProps()}
     */
    private static final Properties PROPS = AppComponents.getProps();
    
    private static List<String> minimessageToUser = new ArrayList<>();
    
    private String thePc = "";
    
    private NetScanCtr classOption;
    
    private Model model;
    
    private HttpServletRequest request;
    
    private long lastScanStamp;
    
    private long nextScanStamp;
    
    private NetScanService scanTask;
    
    public Model getModel() {
        return model;
    }
    
    public void setModel(Model model) {
        this.model = model;
    }
    
    public HttpServletRequest getRequest() {
        return request;
    }
    
    public void setRequest(HttpServletRequest request) {
        this.request = request;
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
        String lastNetScanMAP = AbstractForms.fromArray(NetKeeper.getUsersScanWebModelMapWithHTMLLinks());
        Date lastStamp = new Date(lastScanStamp);
        return MessageFormat.format("{0}\nPcNamesForSendToDatabase:\n{1}\n\nLastNetScanMAP:\n{2}", lastStamp, lastNetScanMAP);
    }
    
    public void setClassOption(Object classOption) {
        if (classOption instanceof NetScanCtr) {
            this.classOption = (NetScanCtr) classOption;
        }
        else {
            this.thePc = (String) classOption;
        }
    }
    
    /**
     Очистка pcuserauto
     */
    public static void trunkTableUsers() {
        try (Connection c = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(ConstantsFor.STR_VELKOM + "." + ConstantsFor.DB_PCUSERAUTO);
             PreparedStatement preparedStatement = c.prepareStatement("TRUNCATE TABLE pcuserauto")) {
            preparedStatement.executeUpdate();
        }
        catch (SQLException e) {
            messageToUser.error(e.getMessage() + " see line: 170");
        }
    }
    
    @Override
    public void run() {
        this.nextScanStamp = Long.parseLong(PROPS.getProperty(PropertiesNames.NEXTSCAN, String.valueOf(System.currentTimeMillis())));
        if (classOption == null) {
            throw new InvokeIllegalException("SET CLASS OPTION: " + this.getClass().getSimpleName());
        }
        else {
            this.model = classOption.getModel();
            this.request = classOption.getRequest();
            this.lastScanStamp = Long.parseLong(PROPS.getProperty(PropertiesNames.LASTSCAN, String.valueOf(System.currentTimeMillis())));
            checkScanConditions();
        }
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PcNamesScanner{");
        try {
            sb.append("thePc='").append(thePc).append('\'');
            sb.append(", lastScanStamp=").append(lastScanStamp);
            sb.append('}');
        }
        catch (RuntimeException e) {
            sb.append(e.getMessage());
        }
        return sb.toString();
    }
    
    private void checkScanConditions() {
        int thisTotpc = Integer.parseInt(PROPS.getProperty(PropertiesNames.PR_TOTPC, "269"));
        if ((scanTemp.isFile() && scanTemp.exists())) {
            isMapSizeBigger(thisTotpc);
        }
        else {
            isTime();
        }
    }
    
    private void isMapSizeBigger(int thisTotpc) {
        long timeLeft = TimeUnit.MILLISECONDS.toSeconds(nextScanStamp - System.currentTimeMillis());
        int pcWas = Integer.parseInt(PROPS.getProperty(PropertiesNames.ONLINEPC, "0"));
        int remainPC = thisTotpc - NetKeeper.getUsersScanWebModelMapWithHTMLLinks().size();
        boolean newPSs = remainPC < 0;
        
        String msg = new ScanMessagesCreator().getMsg(timeLeft);
        String title = new ScanMessagesCreator().getTitle(remainPC, thisTotpc, pcWas);
        String pcValue = new ScanMessagesCreator().fillUserPCForWEBModel();
        
        messageToUser.info(msg);
        classOption.getModel().addAttribute("left", msg).addAttribute("pc", pcValue).addAttribute(ModelAttributeNames.TITLE, title);
        
        if (newPSs) {
            newPCCheck(pcValue, remainPC);
        }
        else {
            noNewPCCheck(remainPC);
        }
        isTime();
    }
    
    private void newPCCheck(String pcValue, double remainPC) {
        FileSystemWorker.writeFile(ConstantsNet.BEANNAME_LASTNETSCAN, pcValue);
        classOption.getModel().addAttribute(ModelAttributeNames.NEWPC, "Добавлены компы! " + Math.abs(remainPC) + " шт.");
        PROPS.setProperty(PropertiesNames.PR_TOTPC, String.valueOf(NetKeeper.getUsersScanWebModelMapWithHTMLLinks().size()));
        PROPS.setProperty(ModelAttributeNames.NEWPC, String.valueOf(remainPC));
    }
    
    private void noNewPCCheck(int remainPC) {
        if (remainPC < ConstantsFor.INT_ANSWER) {
            PROPS.setProperty(PropertiesNames.PR_TOTPC, String.valueOf(NetKeeper.getUsersScanWebModelMapWithHTMLLinks().size()));
        }
    }
    
    private void scheduleScan() {
        boolean isSystemTimeBigger = (System.currentTimeMillis() > nextScanStamp);
        LocalDateTime lastScanLocalTime = LocalDateTime.ofEpochSecond(lastScanStamp / 1000, 0, ZoneOffset.ofHours(3));
        LocalDateTime nextScan = LocalDateTime.ofEpochSecond(nextScanStamp / 1000, 0, ZoneOffset.ofHours(3));
        model.addAttribute(ModelAttributeNames.SERVICEINFO, MessageFormat.format("{0} last<br>{1}", lastScanLocalTime, nextScan));
        if (isSystemTimeBigger) {
            sysTimeBigger();
        }
        else {
            String minLeftToModel = TimeUnit.MILLISECONDS.toMinutes(nextScanStamp - System.currentTimeMillis()) + " minutes left";
            minLeftToModel = new PageGenerationHelper().setColor(ConstantsFor.YELLOW, minLeftToModel);
            model.addAttribute(ModelAttributeNames.PCS, minLeftToModel);
        }
    }
    
    private void sysTimeBigger() {
        ThreadPoolTaskScheduler taskScheduler = AppComponents.threadConfig().getTaskScheduler();
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(1);
        this.scanTask = new ScannerUSR(new Date(nextScanStamp), startSignal, doneSignal);
        ScheduledFuture<?> scheduledFuture = taskScheduler.scheduleAtFixedRate(scanTask, new Date(nextScanStamp), TimeUnit.MINUTES
                .toMillis(ConstantsFor.DELAY * 2));
        try {
            startSignal.countDown();
            messageToUser.info(this.getClass().getSimpleName(), "Start signal: ", startSignal.toString());
            messageToUser.info(this.getClass().getSimpleName(), "Done signal: ", doneSignal.toString());
            scheduledFuture.get(ConstantsFor.DELAY - 1, TimeUnit.MINUTES);
            doneSignal.await();
            String modelTitle = MessageFormat
                    .format("Scan is Done {0}. Next after {1} minutes", scheduledFuture.isDone(), scheduledFuture.getDelay(TimeUnit.MINUTES));
            model.addAttribute(ModelAttributeNames.TITLE, modelTitle);
            messageToUser.warn(modelTitle);
            messageToUser.warn(this.getClass().getSimpleName(), "Start signal: ", startSignal.toString());
            messageToUser.warn(this.getClass().getSimpleName(), "Done signal: ", doneSignal.toString());
        }
        catch (InterruptedException e) {
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        catch (ExecutionException | TimeoutException e) {
            model.addAttribute(ModelAttributeNames.PCS, getStatistics());
        }
        catch (ConcurrentModificationException e) {
            messageToUser.error(e.getMessage() + " see line: 386 ***");
        }
        
    }
    


    private class ScannerUSR implements NetScanService {
        
        
        private final CountDownLatch startSignal;
        
        private final CountDownLatch doneSignal;
        
        private Date nextScanDate;
        
        @Contract(pure = true)
        ScannerUSR(Date nextScanDate, CountDownLatch startSignal, CountDownLatch doneSignal) {
            this.nextScanDate = nextScanDate;
            this.doneSignal = doneSignal;
            this.startSignal = startSignal;
        }
        
        @Override
        public int hashCode() {
            return nextScanDate != null ? nextScanDate.hashCode() : 0;
        }
        
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
    
        /**
         @return {@link #toString()}
     
         @throws InvokeIllegalException {@link #scanPCPrefix()} , set not written to DB
         */
        @Override
        public String getExecution() {
            messageToUser.info("Creating tmp file: ", String.valueOf(fileScanTMPCreate(true)), new File(FileNames.SCAN_TMP).getAbsolutePath());
            try {
                new MessageToTray(this.getClass().getSimpleName())
                        .info("NetScannerSvc started scan", UsefulUtilities.getUpTime(), MessageFormat.format("Last online {0} PCs\n File: {1}",
                                PROPS.getProperty(PropertiesNames.ONLINEPC), new File("scan.tmp").getAbsolutePath()));
                startSignal.await();
                scanPCPrefix();
                doneSignal.countDown();
            }
            catch (InterruptedException e) {
                messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".getExecution", e));
                Thread.currentThread().checkAccess();
                Thread.currentThread().interrupt();
            }
            return this.toString();
        }
    
        /**
         @throws InvokeIllegalException {@link #onePrefixSET(String)}, not written
         */
        private void scanPCPrefix() {
            for (String pcNamePREFIX : ConstantsNet.getPcPrefixes()) {
                Thread.currentThread().setName(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startClassTime) + "-sec");
                NetKeeper.getPcNamesForSendToDatabase().clear();
                NetKeeper.getPcNamesForSendToDatabase().addAll(onePrefixSET(pcNamePREFIX));
            }
            String elapsedTime = ConstantsFor.ELAPSED + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startClassTime) + " sec.";
            NetKeeper.getPcNamesForSendToDatabase().add(elapsedTime);
            AppComponents.threadConfig().execByThreadConfig(this::writeLog);
        }
        
        @Async
        private void scanIt() {
            TForms tForms = new TForms();
            if (request != null && request.getQueryString() != null) {
                NetKeeper.getUsersScanWebModelMapWithHTMLLinks().clear();
                getExecution();
                Set<String> pcNames = onePrefixSET(classOption.getRequest().getQueryString());
                classOption.getModel()
                        .addAttribute(ModelAttributeNames.TITLE, new Date().toString())
                        .addAttribute(ModelAttributeNames.PC, tForms.fromArray(pcNames, true));
            }
            else {
                NetKeeper.getUsersScanWebModelMapWithHTMLLinks().clear();
                getExecution();
                model.addAttribute(ModelAttributeNames.TITLE, nextScanDate)
                        .addAttribute(ModelAttributeNames.PC, tForms.fromArray(NetKeeper.getPcNamesForSendToDatabase(), true));
            }
        }
        
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("ScannerUSR{");
            sb.append("lastScanDate=").append(nextScanDate);
            sb.append('}');
            return sb.toString();
        }
        
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
                messageToUser.error(file.getAbsolutePath() + " see line: 482");
            }
            boolean exists = file.exists() & file.lastModified() > (System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(10));
            
            file.deleteOnExit();
            
            return exists;
        }
        
        @Override
        public void run() {
            UsefulUtilities.setPreference(PropertiesNames.ONLINEPC, String.valueOf(0));
            PROPS.setProperty(PropertiesNames.ONLINEPC, "0");
            scanIt();
        }
    
        private void planNextStart() {
            InitProperties initProperties = InitProperties.getInstance(InitProperties.DB_MEMTABLE);
            fileScanTMPCreate(false);
            long nextStart = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY * 2);
            
            PROPS.setProperty(PropertiesNames.LASTSCAN, String.valueOf(System.currentTimeMillis()));
            PROPS.setProperty(PropertiesNames.NEXTSCAN, String.valueOf(nextStart));
            
            initProperties.setProps(PROPS);
            initProperties = InitProperties.getInstance(InitProperties.FILE);
            initProperties.setProps(PROPS);
            String prefLastNext = MessageFormat.format("{0} last, {1} next", new Date(System.currentTimeMillis()), new Date(nextStart));
            FileSystemWorker.appendObjectToFile(new File(FileNames.LASTNETSCAN_TXT), prefLastNext);
        }
        
        private void showScreenMessage() {
            
            float upTime = (float) (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startClassTime)) / ConstantsFor.ONE_HOUR_IN_MIN;
            try {
                String bodyMsg = MessageFormat
                        .format("Online: {0}.\n{1} min uptime. \n{2} = scan.tmp\n", PROPS.getProperty(PropertiesNames.ONLINEPC, "0"), upTime);
                AppComponents.getMessageSwing(this.getClass().getSimpleName()).infoTimer((int) ConstantsFor.DELAY, bodyMsg);
                
            }
            catch (RuntimeException e) {
                messageToUser.error(MessageFormat.format("ScannerUSR.runAfterAllScan: {0}, ({1})", e.getMessage(), e.getClass().getName()));
            }
        }
        
        @Override
        public String getPingResultStr() {
            return new ScanMessagesCreator().fillUserPCForWEBModel();
        }
        
        /**
         @return absolute path to file {@link FileNames#LASTNETSCAN_TXT}
         
         @see PcNamesScannerTest#testWriteLog()
         */
        @Override
        public String writeLog() {
            FileSystemWorker.writeFile(FileNames.LASTNETSCAN_TXT, NetKeeper.getUsersScanWebModelMapWithHTMLLinks().navigableKeySet().stream());
            FileSystemWorker.writeFile(PcNamesScanner.class.getSimpleName() + ".mini", minimessageToUser);
            FileSystemWorker.writeFile(FileNames.UNUSED_IPS, NetKeeper.getUnusedNamesTree().stream());
            
            planNextStart();
            showScreenMessage();
            minimessageToUser.add(AbstractForms.fromArray(PROPS));
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
        
    }
    


    private class ScanMessagesCreator implements Keeper {
        
        
        private @NotNull String getTitle(int remainPC, int thisTotpc, int pcWas) {
            StringBuilder titleBuilder = new StringBuilder();
            titleBuilder.append(remainPC);
            titleBuilder.append("/");
            titleBuilder.append(thisTotpc);
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
            
            for (String keyMap : list) {
                String valueMap = String.valueOf(NetKeeper.getUsersScanWebModelMapWithHTMLLinks().get(keyMap));
                brStringBuilder.append(keyMap).append(" ").append(valueMap).append("<br>");
            }
            return brStringBuilder.toString();
            
        }
    }
    
    /**
     <b>protected for:</b> {@link PcNamesScannerTest}
     */
    protected void isTime() {
        if (!(scanTemp.exists())) {
            scheduleScan();
        }
        else {
            messageToUser.warn("Last scan: " + new Date(lastScanStamp));
            messageToUser.warn("Next scan: " + new Date(lastScanStamp + TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY * 2)));
            messageToUser.warn("File scan.tmp is " + new File(FileNames.SCAN_TMP).exists());
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
        try {
            closePrefix(prefixPcName);
        }
        catch (InvokeIllegalException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".onePrefixSET", e));
            UserInfo.writeUsersToDBFromSET();
        }
        return copySet;
    }
    
    private void closePrefix(String prefixPcName) throws InvokeIllegalException {
        prefixToMap(prefixPcName);
        if (UserInfo.writeUsersToDBFromSET()) {
            NetKeeper.getPcNamesForSendToDatabase().clear();
            messageToUser.info(this.getClass().getSimpleName(), "NetKeeper.getPcNamesForSendToDatabase()", "CLEARED");
        }
        else {
            throw new InvokeIllegalException(this.getClass().getSimpleName() + " ERR to write: NetKeeper.getPcNamesForSendToDatabase");
        }
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
}
