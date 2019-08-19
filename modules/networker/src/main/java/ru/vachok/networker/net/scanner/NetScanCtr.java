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
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.UsefulUtilities;
import ru.vachok.networker.enums.ConstantsNet;
import ru.vachok.networker.enums.ModelAttributeNames;
import ru.vachok.networker.enums.PropertiesNames;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.info.HTMLGeneration;
import ru.vachok.networker.info.PageGenerationHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Properties;
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
    
    private NetScannerSvc netScannerSvcInstAW;
    
    private HttpServletRequest request;
    
    private Model model;
    
    private long lastScan;
    
    @GetMapping(STR_NETSCAN)
    public String netScan(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Model model) {
        this.request = request;
        this.model = model;
        this.lastScan = Long.parseLong(PROPERTIES.getProperty(PropertiesNames.PR_LASTSCAN, "1548919734742"));
        final long lastSt = lastScan;
        UsefulUtilities.getVis(request);
        model.addAttribute("serviceinfo", (float) TimeUnit.MILLISECONDS.toSeconds(lastSt - System.currentTimeMillis()) / UsefulUtilities.ONE_HOUR_IN_MIN);
    
        netScannerSvcInstAW.setClassOption(this);
        
        model.addAttribute("pc", FileSystemWorker.readFile(ConstantsNet.BEANNAME_LASTNETSCAN) + "<p>");
        model.addAttribute(ModelAttributeNames.ATT_TITLE, AppComponents.getUserPref().get(PropertiesNames.PR_ONLINEPC, "0") + " pc at " + new Date(lastSt));
        model.addAttribute(ConstantsNet.BEANNAME_NETSCANNERSVC, netScannerSvcInstAW);
        model.addAttribute(ModelAttributeNames.ATT_THEPC, netScannerSvcInstAW.getThePc());
        model.addAttribute(ModelAttributeNames.ATT_FOOTER, PAGE_FOOTER.getFooter(ModelAttributeNames.ATT_FOOTER) + "<br>First Scan: 2018-05-05");
        response.addHeader(ConstantsFor.HEAD_REFRESH, "30");
    
        System.out.println(netScannerSvcInstAW.fillWebModel());
        
        return ConstantsNet.ATT_NETSCAN;
    }
    
    public HttpServletRequest getRequest() {
        return request;
    }
    
    public Model getModel() {
        return model;
    }
    
    @Contract(pure = true)
    @Autowired
    public NetScanCtr(NetScannerSvc netScannerSvc) {
        this.netScannerSvcInstAW = netScannerSvc;
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
        this.netScannerSvcInstAW = netScannerSvc;
        String thePc = netScannerSvc.getThePc();
        if (thePc.toLowerCase().contains("user: ")) {
            model.addAttribute("ok", netScannerSvcInstAW.fillAttribute(thePc).trim());
            model.addAttribute(ModelAttributeNames.ATT_TITLE, thePc);
            model.addAttribute(ModelAttributeNames.ATT_FOOTER, PAGE_FOOTER.getFooter(ModelAttributeNames.ATT_FOOTER));
            return "ok";
        }
        model.addAttribute(ModelAttributeNames.ATT_THEPC, thePc);
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
