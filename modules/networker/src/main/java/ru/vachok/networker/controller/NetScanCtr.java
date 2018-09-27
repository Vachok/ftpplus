package ru.vachok.networker.controller;


import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.componentsrepo.LastNetScan;
import ru.vachok.networker.services.NetScannerSvc;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


/**
 @since 30.08.2018 (12:55) */
@Controller
public class NetScanCtr {

    /*Fields*/
    private static final String SOURCE_CLASS = NetScanCtr.class.getSimpleName();

    private static InitProperties initProperties = new DBRegProperties(ConstantsFor.APP_NAME + SOURCE_CLASS);

    private static final Logger LOGGER = AppComponents.getLogger();

    private static Properties properties = initProperties.getProps();

    private NetScannerSvc netScannerSvc;

    private Map<String, Boolean> lastScanMap = new ConcurrentHashMap<>();

    private LastNetScan lastNetScan = AppComponents.lastNetScan();

    /*Instances*/
    @Autowired
    public NetScanCtr(NetScannerSvc netScannerSvc) {
        this.netScannerSvc = netScannerSvc;
    }

    @GetMapping ("/netscan")
    public String netScan(HttpServletRequest request, Model model) {
        Map<String, Boolean> netScanNetWork = lastNetScan.getNetWork();
        String propertyLastScan = properties.getProperty("lastscan", "1");
        long l = Long.parseLong(propertyLastScan) + TimeUnit.MINUTES.toMillis(25);
        boolean b = (l > System.currentTimeMillis());
        boolean b1 = lastScanMap.size() > 10;
        String titleStr = "title";
        if(b1 && b){
            long l1 = TimeUnit.MILLISECONDS.toSeconds(l - System.currentTimeMillis());
            String msg = l1 + " seconds (" + ( float ) l1 / ConstantsFor.ONE_HOUR_IN_MIN + " min) left";
            LOGGER.warn(msg);
            model
                .addAttribute("left", msg)
                .addAttribute("pc", new TForms().fromArray(lastScanMap, true));
        }
        else{
            if(request.getQueryString()!=null){
                netScannerSvc.setQer(request.getQueryString());
                List<String> pcNames = netScannerSvc.getPCNames(request.getQueryString());
                model
                    .addAttribute(titleStr, new Date().toString())
                    .addAttribute("pc", new TForms().fromArray(pcNames));
            }
            else{
                List<String> pCsAsync;
                pCsAsync = netScannerSvc.getPCsAsync();
                model
                    .addAttribute(titleStr,
                        ( float ) TimeUnit.MILLISECONDS
                            .toSeconds(System.currentTimeMillis() - l) / ConstantsFor.ONE_HOUR_IN_MIN +
                            " ago was last scan")
                    .addAttribute("pc", new TForms().fromArray(pCsAsync));
                properties.setProperty("lastscan", System.currentTimeMillis() + "");
                initProperties.delProps();
                initProperties.setProps(properties);
            }
        }
        model
            .addAttribute("netScannerSvc", netScannerSvc)
            .addAttribute("thePc", netScannerSvc.getThePc())
            .addAttribute("title", "First Scan: 2018-05-05");
        return "netscan";
    }

    @PostMapping ("/netscan")
    public void pcNameForInfo(@ModelAttribute NetScannerSvc netScannerSvc, BindingResult result, Model model) {
        this.netScannerSvc = netScannerSvc;
        netScannerSvc.getInfoFromDB();
        String thePc = netScannerSvc.getThePc();
        model.addAttribute("thePc", thePc);
        netScannerSvc.setThePc("");
    }
}
