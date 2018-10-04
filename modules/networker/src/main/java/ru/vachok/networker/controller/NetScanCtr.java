package ru.vachok.networker.controller;


import org.slf4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.componentsrepo.PageFooter;
import ru.vachok.networker.services.NetScannerSvc;

import javax.servlet.http.HttpServletRequest;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;


/**
 @since 30.08.2018 (12:55) */
@Controller
public class NetScanCtr {

    /*Fields*/
    private static final String SOURCE_CLASS = NetScanCtr.class.getSimpleName();

    private static final String NETSCAN_STR = "netscan";

    private static final Logger LOGGER = AppComponents.getLogger();

    private static final String TITLE_STR = "TITLE_STR";

    private static Properties properties = new Properties();

    private static NetScannerSvc netScannerSvc = AppComponents.netScannerSvc();

    private static ConcurrentMap<String, Boolean> lastScan = AppComponents.lastNetScanMap();

    private long l;

    private final int duration = new SecureRandom().nextInt((int) ConstantsFor.MY_AGE);

    @GetMapping("/netscan")
    public String netScan(HttpServletRequest request, Model model) {
        Map<String, Boolean> netWork = lastScan;
        boolean isMapSizeBigger = netWork.size() > 2;

        if (isMapSizeBigger) {
            mapSizeBigger(model, netWork, request);
        } else {
            scanIt(request, model);
        }
        model
            .addAttribute("netScannerSvc", netScannerSvc)
            .addAttribute("thePc", netScannerSvc.getThePc())
            .addAttribute(TITLE_STR, "First Scan: 2018-05-05");
        model.addAttribute("footer", new PageFooter().getFooterUtext());
        AppComponents.lastNetScan().setTimeLastScan(new Date());
        return NETSCAN_STR;
    }

    private void mapSizeBigger(Model model, Map<String, Boolean> netWork, HttpServletRequest request) {
        String propertyLastScan = properties.getProperty("lastscan", "1515233487000");
        l = Long.parseLong(propertyLastScan) + TimeUnit.MINUTES.toMillis(duration);
        boolean isSystemTimeBigger = (System.currentTimeMillis() > l);
        long timeLeft = TimeUnit.MILLISECONDS.toSeconds(l - System.currentTimeMillis());
        String msg = timeLeft + " seconds (" + (float) timeLeft / ConstantsFor.ONE_HOUR_IN_MIN + " min) left<br>Delay period is " + duration;
        LOGGER.warn(msg);
        int i = ConstantsFor.TOTAL_PC - netWork.size();
        model
            .addAttribute("left", msg)
            .addAttribute("pc", new TForms().fromArray(netWork, true))
            .addAttribute("title", i + "/" + ConstantsFor.TOTAL_PC + " PCs");
        if (0 > i) {
            model.addAttribute("newpc", "Добавлены компы! " + Math.abs(i) + " шт.");
        }
        properties.setProperty("totpc", ConstantsFor.TOTAL_PC + "");
        if (isSystemTimeBigger && !(netWork.size() < ConstantsFor.TOTAL_PC)) {
            String msg1 = "isSystemTimeBigger is " + true + " " + netWork.size() + " network map cleared";
            LOGGER.warn(msg1);
            scanIt(request, model);
        }
    }

    private void scanIt(HttpServletRequest request, Model model) {

        if (request != null && request.getQueryString() != null) {
            lastScan.clear();
            netScannerSvc.setQer(request.getQueryString());
            Set<String> pcNames = netScannerSvc.getPCNamesPref(request.getQueryString());
            model
                .addAttribute(TITLE_STR, new Date().toString())
                .addAttribute("pc", new TForms().fromArray(pcNames, true));
        } else {
            lastScan.clear();
            Set<String> pCsAsync = netScannerSvc.getPcNames();
            model
                .addAttribute(TITLE_STR, (float) TimeUnit.MILLISECONDS
                    .toSeconds(System.currentTimeMillis() - this.l) / ConstantsFor.ONE_HOUR_IN_MIN + " was scan")
                .addAttribute("pc", new TForms().fromArray(pCsAsync, true));
            AppComponents.lastNetScan().setTimeLastScan(new Date());
            properties.setProperty("lastscan", System.currentTimeMillis() + "");
            lastScan.clear();
        }
    }

    @PostMapping("/netscan")
    public String pcNameForInfo(@ModelAttribute NetScannerSvc netScannerSvc, BindingResult result, Model model) {
        netScannerSvc.getInfoFromDB();
        String thePc = netScannerSvc.getThePc();
        model.addAttribute("thePc", thePc);
        AppComponents.adSrv().getAdComputer().setDnsHostName(thePc);
        netScannerSvc.setThePc("");
        return "redirect:/ad?" + thePc;
    }
}
