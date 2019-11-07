// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.scanner;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.htmlgen.HTMLGeneration;
import ru.vachok.networker.componentsrepo.htmlgen.PageGenerationHelper;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.data.enums.ModelAttributeNames;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.restapi.props.InitProperties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Timer;
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
    
    private final Timer scheduler = new Timer();
    
    private static MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, NetScanCtr.class.getSimpleName());
    
    @SuppressWarnings("InstanceVariableOfConcreteClass") private PcNamesScanner pcNamesScanner;
    
    private HttpServletRequest request;
    
    private HttpServletResponse response;
    
    private Model model;
    
    public HttpServletResponse getResponse() {
        return response;
    }
    
    HttpServletRequest getRequest() {
        return request;
    }
    
    Model getModel() {
        return model;
    }
    
    public void setModel(Model model) {
        this.model = model;
    }
    
    @Contract(pure = true)
    public NetScanCtr() {
    
    }
    
    /**
     @see NetScanCtrTest#testNetScan()
     */
    @GetMapping(STR_NETSCAN)
    public String netScan(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Model model) {
        this.pcNamesScanner = new PcNamesScanner();
        this.request = request;
        this.response = response;
        this.model = model;
    
        long lastScan = Long.parseLong(InitProperties.getUserPref().get(PropertiesNames.LASTSCAN, "1548919734742"));
        pcNamesScanner.setClassOption(this);
        UsefulUtilities.getVis(request);
    
        float serviceInfoVal = (float) TimeUnit.MILLISECONDS
            .toSeconds(InitProperties.getUserPref().getLong(PropertiesNames.NEXTSCAN, UsefulUtilities.getAtomicTime()) - System
                .currentTimeMillis()) / ConstantsFor.ONE_HOUR_IN_MIN;
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
    
    private void starterNetScan() {
        File file = new File(FileNames.SCAN_TMP);
        if (!file.exists()) {
            scheduler.purge();
            scheduler.schedule(pcNamesScanner, new Date(InitProperties.getUserPref().getLong(PropertiesNames.NEXTSCAN, 0)), ConstantsFor.DELAY);
        }
        else {
            MessageToUser.getInstance(MessageToUser.DB, this.getClass().getSimpleName())
                .info(this.getClass().getSimpleName(), new Date(this.pcNamesScanner.scheduledExecutionTime()).toString(), this.pcNamesScanner.toString());
            file.deleteOnExit();
        }
    }
    
    /**
     POST /netscan
     <p>
 
     @param pcNamesScanner {@link PcNamesScanner}
     @param model {@link Model}
     @return redirect:/ad? + {@link PcNamesScanner#getThePc()}
     */
    @PostMapping(STR_NETSCAN)
    public @NotNull String pcNameForInfo(@NotNull @ModelAttribute PcNamesScanner pcNamesScanner, Model model) {
    
        this.pcNamesScanner = pcNamesScanner;
        this.model = model;
        this.pcNamesScanner.setClassOption(this);
        String thePc = pcNamesScanner.getThePc();
        
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
}
