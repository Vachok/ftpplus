// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.scanner;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.vachok.networker.*;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.componentsrepo.htmlgen.HTMLGeneration;
import ru.vachok.networker.componentsrepo.htmlgen.PageGenerationHelper;
import ru.vachok.networker.enums.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.Executors;
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
    
    private static final Properties PROPERTIES = AppComponents.getProps();
    
    private static final HTMLGeneration PAGE_FOOTER = new PageGenerationHelper();
    
    private NetScannerSvc netScan;
    
    private HttpServletRequest request;
    
    private Model model;
    
    @Contract(pure = true)
    @Autowired
    public NetScanCtr(NetScannerSvc netScannerSvc) {
        this.netScan = netScannerSvc;
    }
    
    private long lastScan;
    
    @GetMapping(STR_NETSCAN)
    public String netScan(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Model model) {
        this.request = request;
        this.model = model;
        this.lastScan = Long.parseLong(PROPERTIES.getProperty(PropertiesNames.PR_LASTSCAN, "1548919734742"));
        final long lastSt = lastScan;
        netScan.setClassOption(this);
        UsefulUtilities.getVis(request);
    
        float serviceInfoVal = (float) TimeUnit.MILLISECONDS.toSeconds(lastSt - System.currentTimeMillis()) / UsefulUtilities.ONE_HOUR_IN_MIN;
        String pcVal = FileSystemWorker.readFile(FileNames.LASTNETSCAN_TXT) + "<p>";
        String titleVal = AppComponents.getUserPref().get(PropertiesNames.PR_ONLINEPC, "0") + " pc at " + new Date(lastSt);
        String footerVal = PAGE_FOOTER.getFooter(ModelAttributeNames.FOOTER) + "<br>First Scan: 2018-05-05";
        String thePCVal = netScan.getThePc();
    
        model.addAttribute(ModelAttributeNames.SERVICEINFO, serviceInfoVal);
        model.addAttribute(ModelAttributeNames.PC, pcVal);
        model.addAttribute(ModelAttributeNames.TITLE, titleVal);
        model.addAttribute(ConstantsFor.BEANNAME_NETSCANNERSVC, netScan);
        model.addAttribute(ModelAttributeNames.THEPC, thePCVal);
        model.addAttribute(ModelAttributeNames.FOOTER, footerVal);
        
        response.addHeader(ConstantsFor.HEAD_REFRESH, "30");
        Executors.newSingleThreadExecutor().execute(()->netScan.fillWebModel());
    
        return ModelAttributeNames.NETSCAN;
    }
    
    public HttpServletRequest getRequest() {
        return request;
    }
    
    public Model getModel() {
        return model;
    }
    
    /**
     POST /netscan
     <p>
     
     @param netScannerSvc {@link NetScannerSvc}
     @param model {@link Model}
     @return redirect:/ad? + {@link NetScannerSvc#getThePc()}
     */
    @PostMapping(STR_NETSCAN)
    public @NotNull String pcNameForInfo(@NotNull @ModelAttribute NetScannerSvc netScannerSvc, Model model) {
        this.netScan = netScannerSvc;
        this.model = model;
        netScan.setClassOption(this);
        String thePc = netScannerSvc.getThePc();
        if (thePc.toLowerCase().contains("user: ")) {
            model.addAttribute("ok", netScan.fillAttribute(thePc).trim());
            model.addAttribute(ModelAttributeNames.TITLE, thePc);
            model.addAttribute(ModelAttributeNames.FOOTER, PAGE_FOOTER.getFooter(ModelAttributeNames.FOOTER));
            return "ok";
        }
        model.addAttribute(ModelAttributeNames.THEPC, thePc);
        netScannerSvc.setThePc("");
        return "redirect:/ad?" + thePc;
    }
    
    long getLastScan() {
        return lastScan;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NetScanCtr{");
        try {
            sb.append(", request=").append(request);
            sb.append(", model=").append(model);
        }
        catch (RuntimeException e) {
            sb.append(MessageFormat.format("Exception: {0} in {1}.toString()", e.getMessage(), this.getClass().getSimpleName()));
        }
        sb.append('}');
        return sb.toString();
    }
}
