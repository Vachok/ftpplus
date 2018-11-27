package ru.vachok.networker.net;


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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;


/**
 @since 30.08.2018 (12:55) */
@Controller
public class NetScanCtr {

    /*Fields*/
    private static final String NETSCAN_STR = "netscan";

    private static final Logger LOGGER = AppComponents.getLogger();

    private static final String TITLE_STR = "TITLE_STR";

    private static Properties properties = ConstantsFor.PROPS;

    private static NetScannerSvc netScannerSvc = AppComponents.netScannerSvc();

    private static ConcurrentMap<String, Boolean> lastScan = AppComponents.lastNetScanMap();

    private long l;

    private final int duration = ConstantsFor.NETSCAN_DELAY;

    @SuppressWarnings ("WeakerAccess")
    @GetMapping ("/netscan")
    public String netScan(HttpServletRequest request, HttpServletResponse response, Model model) {
        netScannerSvc.setThePc("");
        Map<String, Boolean> netWork = lastScan;
        boolean isMapSizeBigger = netWork.size() > 2;

        if(isMapSizeBigger){
            mapSizeBigger(model, netWork, request);
        }
        else{
            scanIt(request, model);
        }
        model
            .addAttribute("netScannerSvc", netScannerSvc)
            .addAttribute("thePc", netScannerSvc.getThePc())
            .addAttribute(TITLE_STR, "First Scan: 2018-05-05");
        model.addAttribute("footer", new PageFooter().getFooterUtext() + "<br>First Scan: 2018-05-05");
        AppComponents.lastNetScan().setTimeLastScan(new Date());
        response.addHeader("Refresh", "30");
        return NETSCAN_STR;
    }

    /**
     Usage in: {@link #netScan(HttpServletRequest, HttpServletResponse, Model)}
     <p>
     Uses: <br>
     1.1 {@link TForms#fromArray(Map, boolean)}
     1.2 {@link #scanIt(HttpServletRequest, Model)}

     @param model   {@link Model} для сборки
     @param netWork временная {@link Map} для хранения данных во-время работы метода.
     @param request {@link HttpServletRequest}
     */
    private void mapSizeBigger(Model model, Map<String, Boolean> netWork, HttpServletRequest request) {
        String propertyLastScan = properties.getProperty("lastscan", "1515233487000");
        l = Long.parseLong(propertyLastScan) + TimeUnit.MINUTES.toMillis(duration);
        boolean isSystemTimeBigger = (System.currentTimeMillis() > l);
        long timeLeft = TimeUnit.MILLISECONDS.toSeconds(l - System.currentTimeMillis());
        String msg = timeLeft + " seconds (" + ( float ) timeLeft / ConstantsFor.ONE_HOUR_IN_MIN + " min) left<br>Delay period is " + duration;
        LOGGER.warn(msg);
        int i = ConstantsFor.TOTAL_PC - netWork.size();
        model
            .addAttribute("left", msg)
            .addAttribute("pc", new TForms().fromArray(netWork, true))
            .addAttribute("title", i + "/" + ConstantsFor.TOTAL_PC + " PCs");
        if(0 > i){
            model.addAttribute("newpc", "Добавлены компы! " + Math.abs(i) + " шт.");
            properties.setProperty("totpc", netWork.size() + "");
        }
        if(isSystemTimeBigger && !(netWork.size() < ConstantsFor.TOTAL_PC)){
            String msg1 = "isSystemTimeBigger is " + true + " " + netWork.size() + " network map cleared";
            LOGGER.warn(msg1);
            properties.setProperty("totpc", netWork.size() + "");
            scanIt(request, model);
        }
    }

    /**
     Модель для {@link #netScan(HttpServletRequest, HttpServletResponse, Model)} <br>
     Делает проверки на {@link HttpServletRequest#getQueryString()}, если != 0: <br>
     {@link #lastScan}.clear() <br>
     {@link NetScannerSvc#getPCNamesPref(String)} <br>
     {@link NetScannerSvc#setQer(String)}
     <p>
     Добавляет в {@link Model}: <br>
     {@link ConstantsFor#TITLE} = {@link Date}.toString() <br>
     <b>"pc"</b> = {@link TForms#fromArray(Set, boolean)} )} из {@link NetScannerSvc#getPCNamesPref(String)}
     <p>
     Usage in: <br>
     {@link #mapSizeBigger(Model, Map, HttpServletRequest)} (99)
     <p>
     Uses: <br>
     1.1 {@link NetScannerSvc#getPCNamesPref(String)}
     @param request {@link HttpServletRequest} от пользователя через браузер
     @param model {@link Model} для сборки
     */
    private void scanIt(HttpServletRequest request, Model model) {
        if(request!=null && request.getQueryString()!=null){
            lastScan.clear();
            netScannerSvc.setQer(request.getQueryString());
            Set<String> pcNames = netScannerSvc.getPCNamesPref(request.getQueryString());
            model
                .addAttribute(TITLE_STR, new Date().toString())
                .addAttribute("pc", new TForms().fromArray(pcNames, true));
        }
        else{
            lastScan.clear();
            Set<String> pCsAsync = netScannerSvc.getPcNames();
            model
                .addAttribute(TITLE_STR, ( float ) TimeUnit.MILLISECONDS
                    .toSeconds(System.currentTimeMillis() - this.l) / ConstantsFor.ONE_HOUR_IN_MIN + " was scan")
                .addAttribute("pc", new TForms().fromArray(pCsAsync, true));
            AppComponents.lastNetScan().setTimeLastScan(new Date());
            properties.setProperty("lastscan", System.currentTimeMillis() + "");
            lastScan.clear();
        }
    }

    @PostMapping ("/netscan")
    public String pcNameForInfo(@ModelAttribute NetScannerSvc netScannerSvc, BindingResult result, Model model) {
        String thePc = netScannerSvc.getThePc();
        AppComponents.adSrv().setUserInputRaw(thePc);
        netScannerSvc.getInfoFromDB();
        model.addAttribute("thePc", thePc);
        AppComponents.adSrv().setUserInputRaw(netScannerSvc.getThePc());
        netScannerSvc.setThePc("");
        return "redirect:/ad?" + thePc;
    }
}
