// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.scanner;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.htmlgen.HTMLGeneration;
import ru.vachok.networker.componentsrepo.htmlgen.PageGenerationHelper;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.data.enums.ModelAttributeNames;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.info.NetScanService;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.restapi.props.InitProperties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.text.MessageFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;


/**
 @see ru.vachok.networker.net.scanner.NetScanCtrTest
 @since 30.08.2018 (12:55) */
@SuppressWarnings({"SameReturnValue", "ClassUnconnectedToPackage"})
@Controller
public class NetScanCtr {
    
    
    /**
     <i>Boiler Plate</i>
     */
    private static final String STR_NETSCAN = "/netscan";
    
    private static final HTMLGeneration PAGE_FOOTER = new PageGenerationHelper();
    
    private final File file = new File(FileNames.SCAN_TMP);
    
    private static MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, NetScanCtr.class.getSimpleName());
    
    @SuppressWarnings("InstanceVariableOfConcreteClass") private PcNamesScanner pcNamesScanner;
    
    private NetScanService pcNamesScannerOld = new PcNamesScannerWorks();
    
    private HttpServletRequest request;
    
    private HttpServletResponse response;
    
    private Model model;
    
    public HttpServletResponse getResponse() {
        return response;
    }
    
    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }
    
    HttpServletRequest getRequest() {
        return request;
    }
    
    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }
    
    Model getModel() {
        return model;
    }
    
    public void setModel(Model model) {
        this.model = model;
    }
    
    @Contract(pure = true)
    @Autowired
    public NetScanCtr(PcNamesScanner pcNamesScanner) {
        this.pcNamesScanner = pcNamesScanner;
    }
    
    /**
     @see NetScanCtrTest#testNetScan()
     */
    @GetMapping(STR_NETSCAN)
    public String netScan(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Model model, @NotNull @ModelAttribute
        PcNamesScanner pcNamesScanner) {
        this.pcNamesScanner = pcNamesScanner;
        this.request = request;
        this.response = response;
        this.model = model;
        this.pcNamesScanner.setClassOption(this);
        long lastScan = Long.parseLong(InitProperties.getUserPref().get(PropertiesNames.LASTSCAN, "1548919734742"));
        UsefulUtilities.getVis(request);
    
        long nextScan = InitProperties.getUserPref().getLong(PropertiesNames.NEXTSCAN, UsefulUtilities.getAtomicTime());
        float serviceInfoVal = (float) TimeUnit.MILLISECONDS.toSeconds(nextScan - System.currentTimeMillis()) / ConstantsFor.ONE_HOUR_IN_MIN;
        String pcVal = pcNamesScanner.getStatistics() + "<p>";
        String titleVal = InitProperties.getUserPref().get(PropertiesNames.ONLINEPC, "0") + " pc at " + new Date(lastScan);
        String footerVal = PAGE_FOOTER.getFooter(ModelAttributeNames.FOOTER) + "<br>First Scan: 2018-05-05";
        String thePCVal = pcNamesScanner.getThePc();
        
        model.addAttribute(ModelAttributeNames.SERVICEINFO, serviceInfoVal);
        model.addAttribute(ModelAttributeNames.PC, pcVal);
        model.addAttribute(ModelAttributeNames.TITLE, titleVal);
        model.addAttribute(ConstantsFor.BEANNAME_NETSCANNERSVC, pcNamesScanner);
        model.addAttribute(ModelAttributeNames.THEPC, thePCVal);
        model.addAttribute(ModelAttributeNames.FOOTER, MessageFormat.format("{0}<br>{1}", this.toString(), footerVal));
        response.addHeader(ConstantsFor.HEAD_REFRESH, "30");
        starterNetScan();
        return ModelAttributeNames.NETSCAN;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NetScanCtr{");
        try {
            sb.append(", request=").append(request);
        }
        catch (RuntimeException e) {
            sb.append(MessageFormat.format("Exception: {0} in {1}.toString()", e.getMessage(), this.getClass().getSimpleName()));
        }
        sb.append('}');
        return sb.toString();
    }
    
    /**
     @see NetScanCtrTest#testStarterNetScan()
     */
    void starterNetScan() {
        Date nextStart = new Date(InitProperties.getUserPref().getLong(PropertiesNames.NEXTSCAN, System.currentTimeMillis()));
        if (!file.exists()) {
            PcNamesScanner.fileScanTMPCreate(true);
            messageToUser.warn(this.getClass().getSimpleName(), file.getAbsolutePath(), MessageFormat
                .format("{0} nextStart. pcNamesScanner with hash {1} next run.", nextStart.toString(), this.pcNamesScanner.hashCode()));
            AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor().execute(pcNamesScanner);
        }
        else {
            String bodyMsg = MessageFormat
                .format("{0} scan.tmp\npcNamesScanner with hash {1} running... {2} next start, InitProperties.getUserPref().getLong(PropertiesNames.NEXTSCAN, System.currentTimeMillis()): {3}",
                    file.exists(), this.pcNamesScanner.hashCode(), nextStart.toString());
            messageToUser.info(this.getClass().getSimpleName(), new Date(pcNamesScanner.scheduledExecutionTime()).toString(), bodyMsg);
            file.deleteOnExit();
        }
    }
    
    /**
     POST /netscan
     <p>
 
     @param model {@link Model}
     @return redirect:/ad? + {@link PcNamesScanner#getThePc()}
     */
    @PostMapping(STR_NETSCAN)
    public @NotNull String pcNameForInfo(Model model, @NotNull @ModelAttribute PcNamesScannerWorks pcNamesScanner) {
        this.pcNamesScanner = pcNamesScanner;
        this.model = model;
        this.pcNamesScanner.setClassOption(this);
        String thePc = pcNamesScanner.getThePc();
        this.pcNamesScanner.setModel(model);
        if (thePc.toLowerCase().contains("user: ")) {
            model.addAttribute("ok", this.pcNamesScanner.getExecution().trim());
            model.addAttribute(ModelAttributeNames.TITLE, thePc);
            model.addAttribute(ModelAttributeNames.FOOTER, PAGE_FOOTER.getFooter(ModelAttributeNames.FOOTER));
            return "ok";
        }
        model.addAttribute(ModelAttributeNames.THEPC, thePc);
        pcNamesScanner.setThePc("");
        return "redirect:/ad?" + thePc;
    }
}
