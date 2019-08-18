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
import ru.vachok.networker.TForms;
import ru.vachok.networker.UsefulUtilities;
import ru.vachok.networker.enums.ConstantsNet;
import ru.vachok.networker.enums.ModelAttributeNames;
import ru.vachok.networker.enums.PropertiesNames;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.info.HTMLGeneration;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.info.PageGenerationHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


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
    
    private static final TForms T_FORMS = new TForms();
    
    private NetScannerSvc netScannerSvcInstAW;
    
    @Contract(pure = true)
    @Autowired
    public NetScanCtr(NetScannerSvc netScannerSvc) {
        this.netScannerSvcInstAW = netScannerSvc;
    }
    
    @GetMapping(STR_NETSCAN)
    public String netScan(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Model model) {
        final long lastSt = Long.parseLong(PROPERTIES.getProperty(ConstantsNet.PR_LASTSCAN, "1548919734742"));
        UsefulUtilities.getVis(request);
        model.addAttribute("serviceinfo", (float) TimeUnit.MILLISECONDS.toSeconds(lastSt - System.currentTimeMillis()) / UsefulUtilities.ONE_HOUR_IN_MIN);
        netScannerSvcInstAW.setThePc("");
        model.addAttribute("pc", FileSystemWorker.readFile(ConstantsNet.BEANNAME_LASTNETSCAN) + "<p>");
        model.addAttribute(ModelAttributeNames.ATT_TITLE, AppComponents.getUserPref().get(PropertiesNames.PR_ONLINEPC, "0") + " pc at " + new Date(lastSt));
        model.addAttribute(ConstantsNet.BEANNAME_NETSCANNERSVC, netScannerSvcInstAW);
        model.addAttribute(ModelAttributeNames.ATT_THEPC, netScannerSvcInstAW.getThePc());
        model.addAttribute(ModelAttributeNames.ATT_FOOTER, PAGE_FOOTER.getFooter(ModelAttributeNames.ATT_FOOTER) + "<br>First Scan: 2018-05-05");
        response.addHeader(ConstantsFor.HEAD_REFRESH, "30");
    
        try {
            netScannerSvcInstAW.checkMapSizeAndDoAction(model, request, lastSt);
        }
        catch (InterruptedException e) {
            model.addAttribute(ModelAttributeNames.ATT_PCS, e.getMessage());
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        catch (ExecutionException | IOException e) {
            model.addAttribute(ModelAttributeNames.ATT_PCS, T_FORMS.fromArray(e, true));
        }
        catch (TimeoutException e) {
            model.addAttribute(ModelAttributeNames.ATT_PCS, "TIMEOUT!<p>" + e.getMessage());
        }
        return ConstantsNet.ATT_NETSCAN;
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
        InformationFactory pcResolver = InformationFactory.getInstance(InformationFactory.RESOLVER_PC_INFO);
        String thePc = netScannerSvc.getThePc();
    
        if (thePc.toLowerCase().contains("user: ")) {
            model.addAttribute("ok", pcResolver.getInfoAbout(thePc).trim());
            model.addAttribute(ModelAttributeNames.ATT_TITLE, thePc);
            model.addAttribute(ModelAttributeNames.ATT_FOOTER, PAGE_FOOTER.getFooter(ModelAttributeNames.ATT_FOOTER));
            return "ok";
        }
        model.addAttribute(ModelAttributeNames.ATT_THEPC, thePc);
        netScannerSvc.setThePc("");
        return "redirect:/ad?" + thePc;
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", NetScanCtr.class.getSimpleName() + "[\n", "\n]")
            .add("netScannerSvcInstAW = " + netScannerSvcInstAW.toString())
            .toString();
    }
}
