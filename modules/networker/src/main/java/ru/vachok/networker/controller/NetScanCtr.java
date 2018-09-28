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
import java.util.concurrent.TimeUnit;


/**
 @since 30.08.2018 (12:55) */
@Controller
public class NetScanCtr {

    /*Fields*/
    private static final String SOURCE_CLASS = NetScanCtr.class.getSimpleName();

    private static final String NETSCAN_STR = "netscan";

    private static InitProperties initProperties = new DBRegProperties(ConstantsFor.APP_NAME + SOURCE_CLASS);

    private static final Logger LOGGER = AppComponents.getLogger();

    private static Properties properties = initProperties.getProps();

    private NetScannerSvc netScannerSvc;

    private static final String TITLE_STR = "TITLE_STR";

    private LastNetScan lastScan;

    private long l;

    /*Instances*/
    @Autowired
    public NetScanCtr(NetScannerSvc netScannerSvc, Map<String, Boolean> lastScanMap) {
        this.netScannerSvc = netScannerSvc;
        this.lastScan = netScannerSvc.getLastNetScan();
    }

    @GetMapping ("/netscan")
    public String netScan(HttpServletRequest request, Model model) {
        String propertyLastScan = properties.getProperty("lastscan", "1");
        l = Long.parseLong(propertyLastScan) + TimeUnit.MINUTES.toMillis(ConstantsFor.MY_AGE);
        Map<String, Boolean> netWork = lastScan.getNetWork();
        boolean b1 = netWork.size() > 10;
        if(b1){
            long l1 = TimeUnit.MILLISECONDS.toSeconds(l - System.currentTimeMillis());
            String msg = l1 + " seconds (" + ( float ) l1 / ConstantsFor.ONE_HOUR_IN_MIN + " min) left";
            LOGGER.warn(msg);
            model
                .addAttribute("left", msg)
                .addAttribute("pc", new TForms().fromArray(netWork, true))
                .addAttribute("title", netWork.size() + " PCs");
        }
        else{
            scanIt(request, model);
        }
        model
            .addAttribute("netScannerSvc", netScannerSvc)
            .addAttribute("thePc", netScannerSvc.getThePc())
            .addAttribute(TITLE_STR, "First Scan: 2018-05-05");
        return NETSCAN_STR;
    }

    private void scanIt(HttpServletRequest request, Model model) {

        boolean b = (l > System.currentTimeMillis());

        if(request.getQueryString()!=null){
            netScannerSvc.setQer(request.getQueryString());
            List<String> pcNames = netScannerSvc.getPCNamesPref(request.getQueryString());
            model
                .addAttribute(TITLE_STR, new Date().toString())
                .addAttribute("pc", new TForms().fromArray(pcNames));
        }
        else{
            if(b){
                List<String> pCsAsync = netScannerSvc.getPcNames();
                model
                    .addAttribute(TITLE_STR, ( float ) TimeUnit.MILLISECONDS
                        .toSeconds(System.currentTimeMillis() - l) / ConstantsFor.ONE_HOUR_IN_MIN + " was scan")
                    .addAttribute("pc", new TForms().fromArray(pCsAsync));
                properties.setProperty("lastscan", System.currentTimeMillis() + "");
                initProperties.delProps();
                initProperties.setProps(properties);
            }
        }
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
