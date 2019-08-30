// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.scanner;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import ru.vachok.messenger.MessageSwing;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.ad.user.UserInfo;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.data.Keeper;
import ru.vachok.networker.componentsrepo.data.NetKeeper;
import ru.vachok.networker.componentsrepo.data.enums.*;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.info.InformationFactory;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static ru.vachok.networker.componentsrepo.data.enums.ConstantsFor.STR_P;


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
    
    private static List<String> minimessageToUser = new ArrayList<>();
    
    private NetScanService getScannerUSR = new ScannerUSR(new Date());
    
    private String thePc = "";
    
    private NetScanCtr classOption;
    
    private Model model;
    
    private HttpServletRequest request;
    
    private long lastScanStamp;
    
    public PcNamesScanner() {
        UsefulUtilities.setPreference(PropertiesNames.ONLINEPC, PROPERTIES.getProperty(PropertiesNames.ONLINEPC));
        PROPERTIES.setProperty(PropertiesNames.ONLINEPC, "0");
    }
    
    public void setLastScanStamp(long lastScanStamp) {
        this.lastScanStamp = lastScanStamp;
    }
    
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
    
    public void setClassOption(Object classOption) {
        if (classOption instanceof NetScanCtr) {
            this.classOption = (NetScanCtr) classOption;
        }
        else {
            this.thePc = (String) classOption;
        }
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
    public void run() {
        if (classOption == null) {
            throw new InvokeIllegalException("SET CLASS OPTION: " + this.getClass().getSimpleName());
        }
        else {
            this.lastScanStamp = classOption.getLastScan();
            this.model = classOption.getModel();
            this.request = classOption.getRequest();
            checkScanConditions();
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
    
        PcNamesScanner scanner = (PcNamesScanner) o;
    
        if (lastScanStamp != scanner.lastScanStamp) {
            return false;
        }
        if (!getScannerUSR.equals(scanner.getScannerUSR)) {
            return false;
        }
        if (!thePc.equals(scanner.thePc)) {
            return false;
        }
        if (classOption != null ? !classOption.equals(scanner.classOption) : scanner.classOption != null) {
            return false;
        }
        if (model != null ? !model.equals(scanner.model) : scanner.model != null) {
            return false;
        }
        return request != null ? request.equals(scanner.request) : scanner.request == null;
    }
    
    @Override
    public Runnable getMonitoringRunnable() {
        return new ScannerUSR(new Date(lastScanStamp));
    }
    
    /**
     <b>protected for:</b> {@link PcNamesScannerTest}
     */
    protected void isTime() {
        boolean isSystemTimeBigger = (System.currentTimeMillis() > lastScanStamp);
        if (!(scanTemp.exists())) {
            scheduleScan(isSystemTimeBigger);
        }
        else {
            messageToUser.warn("Last scan: " + new Date(lastScanStamp));
            messageToUser.warn("Next scan: " + new Date(lastScanStamp + TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY)));
            messageToUser.warn("File scan.tmp is " + new File(FileNames.SCAN_TMP).exists());
        }
        
    }
    
    protected @NotNull Set<String> onePrefixSET(String prefixPcName) {
        final long startMethTime = System.currentTimeMillis();
        String pcsString;
        Collection<String> autoPcNames = new ArrayList<>(getCycleNames(prefixPcName));
        for (String pcName : autoPcNames) {
            InformationFactory informationFactory = InformationFactory.getInstance(pcName);
            informationFactory.getInfo();
        }
        prefixToMap(prefixPcName);
        pcsString = UserInfo.writeToDB();
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
        NetKeeper.getUsersScanWebModelMapWithHTMLLinks().put(stringBuilder.toString(), true);
    }
    
    @Override
    public String writeLog() {
        return FileSystemWorker.writeFile(FileNames.LASTNETSCAN_TXT, new TForms().fromArray(NetKeeper.getUsersScanWebModelMapWithHTMLLinks()));
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
    
    @Override
    public String getStatistics() {
        String pcNamesForSendToDatabase = new TForms().fromArray(NetKeeper.getPcNamesForSendToDatabase());
        String lastNetScanMAP = new TForms().fromArray(NetKeeper.getUsersScanWebModelMapWithHTMLLinks());
        Date lastStamp = new Date(lastScanStamp);
        return MessageFormat.format("{0}\nPcNamesForSendToDatabase:\n{1}\n\nLastNetScanMAP:\n{2}", lastStamp, pcNamesForSendToDatabase, lastNetScanMAP);
    }
    
    @Override
    public String getExecution() {
        return new ScanMessagesCreator().fillUserPCForWEBModel();
    }
    
    @Override
    public String getPingResultStr() {
        return getScannerUSR.getPingResultStr();
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
    
    @Override
    public int hashCode() {
        int result = getScannerUSR.hashCode();
        result = 31 * result + thePc.hashCode();
        result = 31 * result + (classOption != null ? classOption.hashCode() : 0);
        result = 31 * result + (model != null ? model.hashCode() : 0);
        result = 31 * result + (request != null ? request.hashCode() : 0);
        result = 31 * result + (int) (lastScanStamp ^ (lastScanStamp >>> 32));
        return result;
    }
    
    private void checkScanConditions() {
        
        int thisTotpc = Integer.parseInt(PROPERTIES.getProperty(PropertiesNames.PR_TOTPC, "269"));
        if ((scanTemp.isFile() && scanTemp.exists())) {
            isMapSizeBigger(thisTotpc);
        }
        else {
            isTime();
        }
    }
    
    private void isMapSizeBigger(int thisTotpc) {
        long timeLeft = TimeUnit.MILLISECONDS.toSeconds(lastScanStamp - System.currentTimeMillis());
        int pcWas = Integer.parseInt(PROPERTIES.getProperty(PropertiesNames.ONLINEPC, "0"));
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
    
    private void scheduleScan(boolean isSystemTimeBigger) {
        LocalDateTime lastScanLocalTime = LocalDateTime.ofEpochSecond(lastScanStamp / 1000, 0, ZoneOffset.ofHours(3));
        model.addAttribute(ModelAttributeNames.NEWPC, lastScanLocalTime);
        if (isSystemTimeBigger) {
            ThreadPoolTaskScheduler taskScheduler = AppComponents.threadConfig().getTaskScheduler();
            ScheduledFuture<?> scheduledFuture = taskScheduler
                .scheduleAtFixedRate(new ScannerUSR(new Date(lastScanStamp)), new Date(), TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY * 2));
            try {
                scheduledFuture.get(ConstantsFor.DELAY - 1, TimeUnit.MINUTES);
                String modelTitle = MessageFormat
                    .format("Scan is Done {0}. Next after {1} minutes", scheduledFuture.isDone(), scheduledFuture.getDelay(TimeUnit.MINUTES));
                model.addAttribute(ModelAttributeNames.TITLE, modelTitle);
                messageToUser.warn(modelTitle);
            }
            catch (InterruptedException e) {
                Thread.currentThread().checkAccess();
                Thread.currentThread().interrupt();
            }
            catch (ExecutionException | TimeoutException e) {
                model.addAttribute(ModelAttributeNames.PCS, getStatistics());
            }
            
        }
    }
    
    private void newPCCheck(String pcValue, double remainPC) {
        FileSystemWorker.writeFile(ConstantsNet.BEANNAME_LASTNETSCAN, pcValue);
        classOption.getModel().addAttribute(ModelAttributeNames.NEWPC, "Добавлены компы! " + Math.abs(remainPC) + " шт.");
        PROPERTIES.setProperty(PropertiesNames.PR_TOTPC, String.valueOf(NetKeeper.getUsersScanWebModelMapWithHTMLLinks().size()));
        PROPERTIES.setProperty(ModelAttributeNames.NEWPC, String.valueOf(remainPC));
    }
    
    private void noNewPCCheck(int remainPC) {
        if (remainPC < ConstantsFor.INT_ANSWER) {
            PROPERTIES.setProperty(PropertiesNames.PR_TOTPC, String.valueOf(NetKeeper.getUsersScanWebModelMapWithHTMLLinks().size()));
        }
    }
    
    private class ScannerUSR implements NetScanService {
        
        
        private Date lastScanDate;
        
        @Contract(pure = true)
        ScannerUSR(Date lastScanDate) {
            this.lastScanDate = lastScanDate;
        }
        
        @Override
        public void run() {
            UsefulUtilities.setPreference(PropertiesNames.ONLINEPC, String.valueOf(0));
            scanIt();
        }
        
        @Override
        public String writeLog() {
            String valueOfPropLastScan = String.valueOf((System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY)));
            PROPERTIES.setProperty(PropertiesNames.PR_LASTSCAN, valueOfPropLastScan);
            minimessageToUser.add(T_FORMS.fromArray(PROPERTIES, false));
            float upTime = (float) (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startClassTime)) / ConstantsFor.ONE_HOUR_IN_MIN;
            try {
                String bodyMsg = MessageFormat
                    .format("Online: {0}.\n{1} min uptime. \n{2} = scan.tmp\n", PROPERTIES.getProperty(PropertiesNames.ONLINEPC, "0"), upTime);
                new MessageSwing().infoTimer((int) ConstantsFor.DELAY, bodyMsg);
                UsefulUtilities.setPreference(PropertiesNames.ONLINEPC, PROPERTIES.getProperty(PropertiesNames.ONLINEPC));
                InitPropertiesAdapter.setProps(PROPERTIES);
            }
            catch (RuntimeException e) {
                messageToUser.error(MessageFormat.format("ScannerUSR.runAfterAllScan: {0}, ({1})", e.getMessage(), e.getClass().getName()));
            }
            
            FileSystemWorker.writeFile(FileNames.LASTNETSCAN_TXT, NetKeeper.getUsersScanWebModelMapWithHTMLLinks().navigableKeySet().stream());
            FileSystemWorker.writeFile(PcNamesScanner.class.getSimpleName() + ".mini", minimessageToUser);
            FileSystemWorker.writeFile(FileNames.UNUSED_IPS, NetKeeper.getUnusedNamesTree().stream());
            fileScanTMPCreate(false);
            setLastScanStamp(System.currentTimeMillis());
            return new File(FileNames.LASTNETSCAN_TXT).toPath().toAbsolutePath().normalize().toString();
        }
        
        @Override
        public String getPingResultStr() {
            return new ScanMessagesCreator().fillUserPCForWEBModel();
        }
        
        @Override
        public Runnable getMonitoringRunnable() {
            return this;
        }
        
        private void scanPCPrefix() {
            for (String pcNamePREFIX : ConstantsNet.getPcPrefixes()) {
                Thread.currentThread().setName(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startClassTime) + "-sec");
                NetKeeper.getPcNamesForSendToDatabase().clear();
                NetKeeper.getPcNamesForSendToDatabase().addAll(onePrefixSET(pcNamePREFIX));
            }
            String elapsedTime = "Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startClassTime) + " sec.";
            NetKeeper.getPcNamesForSendToDatabase().add(elapsedTime);
            AppComponents.threadConfig().execByThreadConfig(this::writeLog);
        }
        
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("ScannerUSR{");
            sb.append("lastScanDate=").append(lastScanDate);
            sb.append('}');
            return sb.toString();
        }
        
        @Override
        public String getStatistics() {
            return new ScanMessagesCreator().fillUserPCForWEBModel();
        }
        
        @Async
        private void scanIt() {
            if (request != null && request.getQueryString() != null) {
                NetKeeper.getUsersScanWebModelMapWithHTMLLinks().clear();
                PROPERTIES.setProperty(PropertiesNames.ONLINEPC, "0");
                UsefulUtilities.setPreference(PropertiesNames.ONLINEPC, String.valueOf(0));
                getExecution();
                Set<String> pcNames = onePrefixSET(classOption.getRequest().getQueryString());
                classOption.getModel()
                    .addAttribute(ModelAttributeNames.TITLE, new Date().toString())
                    .addAttribute(ModelAttributeNames.PC, T_FORMS.fromArray(pcNames, true));
            }
            else {
                NetKeeper.getUsersScanWebModelMapWithHTMLLinks().clear();
                PROPERTIES.setProperty(PropertiesNames.ONLINEPC, "0");
                UsefulUtilities.setPreference(PropertiesNames.ONLINEPC, String.valueOf(0));
                getExecution();
                model.addAttribute(ModelAttributeNames.TITLE, lastScanDate)
                    .addAttribute(ModelAttributeNames.PC, T_FORMS.fromArray(NetKeeper.getPcNamesForSendToDatabase(), true));
                PROPERTIES.setProperty(PropertiesNames.PR_LASTSCAN, String.valueOf(System.currentTimeMillis()));
            }
        }
        
        @Override
        public String getExecution() throws NoClassDefFoundError {
            messageToUser.info("Creating tmp file: ", String.valueOf(fileScanTMPCreate(true)), new File(FileNames.SCAN_TMP).getAbsolutePath());
            try {
                new MessageToTray(this.getClass().getSimpleName())
                    .info("NetScannerSvc started scan", UsefulUtilities.getUpTime(), MessageFormat.format("Last online {0} PCs\n File: {1}",
                        PROPERTIES.getProperty(PropertiesNames.ONLINEPC), new File("scan.tmp").getAbsolutePath()));
            }
            catch (InvokeIllegalException e) {
                messageToUser.error(e.getMessage());
            }
            scanPCPrefix();
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
        
    }
    


    private class ScanMessagesCreator implements Keeper {
        
        
        private @NotNull String getTitle(int remainPC, int thisTotpc, int pcWas) {
            StringBuilder titleBuilder = new StringBuilder();
            titleBuilder.append(remainPC);
            titleBuilder.append("/");
            titleBuilder.append(thisTotpc);
            titleBuilder.append(" PCs (");
            titleBuilder.append(PROPERTIES.getProperty(PropertiesNames.ONLINEPC, "0"));
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
}
