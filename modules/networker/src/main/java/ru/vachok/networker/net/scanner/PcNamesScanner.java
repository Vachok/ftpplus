// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.scanner;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import ru.vachok.messenger.MessageSwing;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.ad.pc.PCInfo;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.data.Keeper;
import ru.vachok.networker.componentsrepo.data.NetKeeper;
import ru.vachok.networker.componentsrepo.data.enums.*;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.componentsrepo.htmlgen.HTMLInfo;
import ru.vachok.networker.net.NetScanService;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageToTray;
import ru.vachok.networker.restapi.props.InitPropertiesAdapter;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.*;

import static ru.vachok.networker.componentsrepo.data.enums.ConstantsFor.STR_P;


/**
 @see ru.vachok.networker.net.scanner.PcNamesScannerTest
 @since 21.08.2018 (14:40) */
@Service(ConstantsFor.BEANNAME_NETSCANNERSVC)
@Scope(ConstantsFor.SINGLETON)
public class PcNamesScanner implements HTMLInfo {
    
    
    /**
     {@link ConstantsFor#DELAY}
     */
    static final int DURATION_MIN = (int) ConstantsFor.DELAY;
    
    /**
     {@link AppComponents#getProps()}
     */
    private static final Properties PROPERTIES = AppComponents.getProps();
    
    private static final String METH_GETPCSASYNC = ".getPCsAsync";
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, PcNamesScanner.class.getSimpleName());
    
    private static final File scanTemp = new File("scan.tmp");
    
    private static final TForms T_FORMS = new TForms();
    
    /**
     Время инициализации
     */
    private static final long startClassTime = System.currentTimeMillis();
    
    private NetScanService getScannerUSR = new ScannerUSR(new Date());
    
    private static List<String> minimessageToUser = new ArrayList<>();
    
    private String thePc = "";
    
    private NetScanCtr classOption;
    
    private Model model;
    
    private HttpServletRequest request;
    
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
    
    private long lastSt;
    
    public PcNamesScanner() {
        UsefulUtilities.setPreference(PropertiesNames.PR_ONLINEPC, PROPERTIES.getProperty(PropertiesNames.PR_ONLINEPC));
        PROPERTIES.setProperty(PropertiesNames.PR_ONLINEPC, "0");
    }
    
    /**
     @return атрибут модели.
     */
    public String getThePc() {
        return thePc;
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
    public String fillAttribute(String attributeName) {
        PCInfo pcInfo = PCInfo.getInstance(attributeName);
        return pcInfo.getInfo();
    }
    
    @Override
    public String fillWebModel() {
        if (classOption == null) {
            throw new InvokeIllegalException("SET CLASS OPTION: " + this.getClass().getSimpleName());
        }
        else {
            try {
                this.lastSt = classOption.getLastScan();
                this.model = classOption.getModel();
                this.request = classOption.getRequest();
                checkScanConditions();
            }
            catch (ExecutionException | InterruptedException | TimeoutException e) {
                return MessageFormat.format("NetScannerSvc.getInfo: {0}, ({1})", e.getMessage(), e.getClass().getName());
            }
        }
        return new ScanMessagesCreator().fillUserPCForWEBModel();
    }
    
    private void checkScanConditions() throws ExecutionException, InterruptedException, TimeoutException {
        
        int thisTotpc = Integer.parseInt(PROPERTIES.getProperty(PropertiesNames.PR_TOTPC, "269"));
        if ((scanTemp.isFile() && scanTemp.exists())) {
            isMapSizeBigger(thisTotpc);
        }
        else {
            isTime(thisTotpc - NetKeeper.getScannedUsersPC().size(), lastSt / 1000);
        }
    }
    
    @Override
    public String toString() {
        try {
            String s = new StringJoiner(",\n", PcNamesScanner.class.getSimpleName() + "[\n", "\n]")
                .add("getScannerUSR = " + getScannerUSR)
                .add("thePc = '" + thePc + "'")
                .add("model = " + !(classOption.getModel() == null))
                .add("request = " + classOption.getRequest())
                .add("lastSt = " + lastSt)
                .toString();
            return s;
        }
        catch (RuntimeException e) {
            return e.getMessage();
        }
        
    }
    
    void setThePc(String thePc) {
        this.thePc = thePc;
    }
    
    private void isMapSizeBigger(int thisTotpc) throws ExecutionException, InterruptedException, TimeoutException {
        long timeLeft = TimeUnit.MILLISECONDS.toSeconds(lastSt - System.currentTimeMillis());
        int pcWas = Integer.parseInt(PROPERTIES.getProperty(PropertiesNames.PR_ONLINEPC, "0"));
        int remainPC = thisTotpc - NetKeeper.getScannedUsersPC().size();
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
        
        isTime(remainPC, lastSt / 1000);
    }
    
    private void newPCCheck(String pcValue, double remainPC) {
        FileSystemWorker.writeFile(ConstantsNet.BEANNAME_LASTNETSCAN, pcValue);
        classOption.getModel().addAttribute(ModelAttributeNames.NEWPC, "Добавлены компы! " + Math.abs(remainPC) + " шт.");
        PROPERTIES.setProperty(PropertiesNames.PR_TOTPC, String.valueOf(NetKeeper.getScannedUsersPC().size()));
        PROPERTIES.setProperty(ModelAttributeNames.NEWPC, String.valueOf(remainPC));
    }
    
    private void noNewPCCheck(int remainPC) {
        if (remainPC < ConstantsFor.INT_ANSWER) {
            PROPERTIES.setProperty(PropertiesNames.PR_TOTPC, String.valueOf(NetKeeper.getScannedUsersPC().size()));
        }
    }
    
    private void isTime(int remainPC, long lastScanEpoch) throws ExecutionException, InterruptedException, TimeoutException {
        long lastScanStamp = lastScanEpoch * 1000;
        Runnable scanRun = new ScannerUSR(new Date(lastScanStamp));
        LocalTime lastScanLocalTime = LocalDateTime.ofEpochSecond(lastScanEpoch, 0, ZoneOffset.ofHours(3)).toLocalTime();
        boolean isSystemTimeBigger = (System.currentTimeMillis() > lastScanStamp);
        if (!(scanTemp.exists())) {
            model.addAttribute(ModelAttributeNames.NEWPC, lastScanLocalTime);
            if (isSystemTimeBigger) {
                Future<?> submitScan = Executors.newSingleThreadExecutor().submit(scanRun);
                submitScan.get(ConstantsFor.DELAY - 1, TimeUnit.MINUTES);
                messageToUser.warn(MessageFormat.format("Scan is Done {0}", submitScan.isDone()));
            }
        }
        else {
            messageToUser.warn(this.getClass().getSimpleName() + ".isTime(last)", " = " + lastScanLocalTime, new Date().toString());
            messageToUser
                .warn(this.getClass().getSimpleName() + ".isTime(next)", "", " = " + new Date(lastScanStamp + TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY)));
            messageToUser.warning(this.getClass().getSimpleName(), "scan.tmp", "is " + new File(FileNames.SCAN_TMP).exists());
        }
        
    }
    
    private @NotNull Set<String> onePrefixSET(String prefixPcName) {
        final long startMethTime = System.currentTimeMillis();
        String pcsString;
        Collection<String> autoPcNames = new ArrayList<>(getCycleNames(prefixPcName));
        for (String pcName : autoPcNames) {
            PCInfo userInfo=PCInfo.getInstance(pcName);
            userInfo.getInfo();
        }
        prefixToMap(prefixPcName);
        pcsString = PCInfo.writeToDB();
        messageToUser.info(pcsString);
        String elapsedTime = "<b>Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startMethTime) + " sec.</b> " + LocalTime.now();
        NetKeeper.getPcNamesForSendToDatabase().add(elapsedTime);
        return NetKeeper.getPcNamesForSendToDatabase();
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
        NetKeeper.getScannedUsersPC().put(stringBuilder.toString(), true);
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
            titleBuilder.append(new Date(lastSt));
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
            Set<String> keySet = NetKeeper.getScannedUsersPC().keySet();
            List<String> list = new ArrayList<>(keySet.size());
            list.addAll(keySet);
            
            Collections.sort(list);
            
            for (String keyMap : list) {
                String valueMap = String.valueOf(NetKeeper.getScannedUsersPC().get(keyMap));
                brStringBuilder.append(keyMap).append(" ").append(valueMap).append("<br>");
            }
            return brStringBuilder.toString();
            
        }
    }
    
    
    
    private class ScannerUSR implements NetScanService {
        
        
        private Date lastScanDate;
        
        @Contract(pure = true)
        ScannerUSR(Date lastScanDate) {
            this.lastScanDate = lastScanDate;
        }
        
        @Override
        public String getPingResultStr() {
            return new ScanMessagesCreator().fillUserPCForWEBModel();
        }
        
        @Override
        public String writeLog() {
            runAfterAllScan();
            FileSystemWorker.writeFile(FileNames.LASTNETSCAN_TXT, NetKeeper.getScannedUsersPC().navigableKeySet().stream());
            FileSystemWorker.writeFile(PcNamesScanner.class.getSimpleName() + ".mini", minimessageToUser);
            FileSystemWorker.writeFile(FileNames.UNUSED_IPS, NetKeeper.getUnusedNamesTree().stream());
            return scanTemp.exists() + " scan.tmp";
        }
        
        @Override
        public String getStatistics() {
            return new ScanMessagesCreator().fillUserPCForWEBModel();
        }
        
        @Override
        public void run() {
            scanIt();
        }
        
        @Override
        public Runnable getMonitoringRunnable() {
            return this;
        }
    
        @Async
        private void scanIt() {
            if (request != null && request.getQueryString() != null) {
                NetKeeper.getScannedUsersPC().clear();
                PROPERTIES.setProperty(PropertiesNames.PR_ONLINEPC, "0");
                UsefulUtilities.setPreference(PropertiesNames.PR_ONLINEPC, String.valueOf(0));
                getExecution();
                Set<String> pcNames = onePrefixSET(classOption.getRequest().getQueryString());
                classOption.getModel()
                    .addAttribute(ModelAttributeNames.TITLE, new Date().toString())
                    .addAttribute(ModelAttributeNames.PC, T_FORMS.fromArray(pcNames, true));
            }
            else {
                NetKeeper.getScannedUsersPC().clear();
                PROPERTIES.setProperty(PropertiesNames.PR_ONLINEPC, "0");
                UsefulUtilities.setPreference(PropertiesNames.PR_ONLINEPC, String.valueOf(0));
                getExecution();
                model.addAttribute(ModelAttributeNames.TITLE, lastScanDate)
                    .addAttribute(ModelAttributeNames.PC, T_FORMS.fromArray(NetKeeper.getPcNamesForSendToDatabase(), true));
                PROPERTIES.setProperty(PropertiesNames.PR_LASTSCAN, String.valueOf(System.currentTimeMillis()));
            }
        }
        
        @Override
        public String getExecution() {
            messageToUser.info("Creating tmp file: ", String.valueOf(fileScanTMPCreate(true)), new File(FileNames.SCAN_TMP).getAbsolutePath());
            try {
                new MessageToTray(this.getClass().getSimpleName())
                    .info("NetScannerSvc started scan", UsefulUtilities.getUpTime(), MessageFormat.format("Last online {0} PCs\n File: {1}",
                        PROPERTIES.getProperty(PropertiesNames.PR_ONLINEPC), new File("scan.tmp").getAbsolutePath()));
            }
            catch (NoClassDefFoundError e) {
                {
                messageToUser.error(getClass().getSimpleName(), METH_GETPCSASYNC, T_FORMS.fromArray(e.getStackTrace(), false));
                }
            }
            catch (InvokeIllegalException e) {
                messageToUser.error(e.getMessage());
            }
            Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).execute(this::scanPCPrefix);
            return this.toString();
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
        
        private void scanPCPrefix() {
            for (String pcNamePREFIX : ConstantsNet.getPcPrefixes()) {
                Thread.currentThread().setName(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startClassTime) + "-sec");
                NetKeeper.getPcNamesForSendToDatabase().clear();
                NetKeeper.getPcNamesForSendToDatabase().addAll(onePrefixSET(pcNamePREFIX));
            }
            String elapsedTime = "Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startClassTime) + " sec.";
            NetKeeper.getPcNamesForSendToDatabase().add(elapsedTime);
            AppComponents.threadConfig().execByThreadConfig(this::runAfterAllScan);
        }
    
        @SuppressWarnings("MagicNumber")
        private void runAfterAllScan() {
            String valueOfPropLastScan = String.valueOf((System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY)));
            PROPERTIES.setProperty(PropertiesNames.PR_LASTSCAN, valueOfPropLastScan);
            minimessageToUser.add(T_FORMS.fromArray(PROPERTIES, false));
            float upTime = (float) (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startClassTime)) / ConstantsFor.ONE_HOUR_IN_MIN;
            try {
                String bodyMsg = MessageFormat
                    .format("Online: {0}.\n{1} min uptime. \n{2} = scan.tmp\n", PROPERTIES.getProperty(PropertiesNames.PR_ONLINEPC, "0"), upTime);
                new MessageSwing().infoTimer((int) ConstantsFor.DELAY, bodyMsg);
                UsefulUtilities.setPreference(PropertiesNames.PR_ONLINEPC, PROPERTIES.getProperty(PropertiesNames.PR_ONLINEPC));
                InitPropertiesAdapter.setProps(PROPERTIES);
            }
            catch (RuntimeException e) {
                messageToUser.error(MessageFormat.format("ScannerUSR.runAfterAllScan: {0}, ({1})", e.getMessage(), e.getClass().getName()));
            }
        }
        
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Scanner{");
            sb.append("lastScanDate=").append(lastScanDate);
            sb.append('}');
            return sb.toString();
        }
    }
}
