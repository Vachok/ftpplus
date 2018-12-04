package ru.vachok.networker.net;


import org.slf4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.componentsrepo.PageFooter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.*;
import java.util.Date;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;


/**
 Контроллер netscan.html
 <p>

 @since 30.08.2018 (12:55) */
@Controller
public class NetScanCtr {

    /*Fields*/

    /**
     Имя {@link Model} атрибута.
     */
    private static final String AT_NAME_NETSCAN = "netscan";

    /**
     {@link AppComponents#getLogger()}
     */
    private static final Logger LOGGER = AppComponents.getLogger();

    /**
     {@link ConstantsFor#TITLE}
     */
    private static final String TITLE_STR = ConstantsFor.TITLE;

    /**
     {@link ConstantsFor#getPROPS()}
     */
    private static final Properties properties = ConstantsFor.getPROPS();

    /**
     {@link ConstantsFor#NETSCAN_DELAY}
     */
    private static final int DURATION = ConstantsFor.NETSCAN_DELAY;

    /**
     {@link AppComponents#netScannerSvc()}
     */
    private static NetScannerSvc netScannerSvc = AppComponents.netScannerSvc();

    /**
     * {@link AppComponents#lastNetScanMap()}
     */
    private static ConcurrentMap<String, Boolean> lastScan = AppComponents.lastNetScanMap();

    /**
     Отрезок времени для промежутка в сканировании.
     */
    private long propLastScanMinusDuration;

    /**
     GET /netscan
     <p>
     Usages: {@link #scanIt(HttpServletRequest, Model)}, {@link #mapSizeBigger(Model, Map, HttpServletRequest)} <br>
     Uses: {@link PageFooter#getFooterUtext()}, {@link AppComponents#lastNetScan()}.setTimeLastScan(new {@link Date}) <br>

     @param request  {@link HttpServletRequest}
     @param response {@link HttpServletResponse}
     @param model    {@link Model}
     @return {@link #AT_NAME_NETSCAN}.html
     */
    @SuppressWarnings("WeakerAccess")
    @GetMapping("/netscan")
    public String netScan(HttpServletRequest request, HttpServletResponse response, Model model) {
        netScannerSvc.setThePc("");
        Map<String, Boolean> netWork = lastScan;
        boolean isMapSizeBigger = netWork.size() > 2;

        if (isMapSizeBigger) {
            mapSizeBigger(model, netWork, request);
        } else {
            scanIt(request, model);
        }
        model
            .addAttribute("netScannerSvc", netScannerSvc)
            .addAttribute("thePc", netScannerSvc.getThePc());
        model.addAttribute("footer", new PageFooter().getFooterUtext() + "<br>First Scan: 2018-05-05");
        AppComponents.lastNetScan().setTimeLastScan(new Date());
        response.addHeader("Refresh", "30");
        return AT_NAME_NETSCAN;
    }

    /**
     Usage in: {@link #netScan(HttpServletRequest, HttpServletResponse, Model)}
     <p>
     Uses: <br> 1.1 {@link TForms#fromArray(Map, boolean)} 1.2 {@link #scanIt(HttpServletRequest, Model)}

     @param model   {@link Model} для сборки
     @param netWork временная {@link Map} для хранения данных во-время работы метода.
     @param request {@link HttpServletRequest}
     */
    private void mapSizeBigger(Model model, Map<String, Boolean> netWork, HttpServletRequest request) {
        String propertyLastScan = properties.getOrDefault("lastscan", "1515233487000").toString();
        propLastScanMinusDuration = Long.parseLong(propertyLastScan) + TimeUnit.MINUTES.toMillis(DURATION);
        boolean isSystemTimeBigger = (System.currentTimeMillis() > propLastScanMinusDuration);
        long timeLeft = TimeUnit.MILLISECONDS.toSeconds(propLastScanMinusDuration - System.currentTimeMillis());
        String msg = timeLeft + " seconds (" + ( float ) timeLeft / ConstantsFor.ONE_HOUR_IN_MIN + " min) left<br>Delay period is " + DURATION;
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
        else{
            if(isSystemTimeBigger && (5 > i)){
                String msg1 = "isSystemTimeBigger is " + true + " " + netWork.size() + " network map cleared";
                LOGGER.warn(msg1);
                properties.setProperty("totpc", netWork.size() + "");
                scanIt(request, model);
            }
        }
    }

    /**
     Модель для
     {@link #netScan(HttpServletRequest, HttpServletResponse, Model)} <br>
     Делает проверки на {@link HttpServletRequest#getQueryString()}, если != 0: <br>
     {@link #lastScan}.clear() <br>
     {@link NetScannerSvc#getPCNamesPref(String)} <br>
     {@link NetScannerSvc#setQer(String)}
     <p>
     Добавляет в {@link Model}: <br> {@link ConstantsFor#TITLE} = {@link Date}.toString() <br>
     <b>"pc"</b> = {@link TForms#fromArray(Set, boolean)} )} из {@link NetScannerSvc#getPCNamesPref(String)}
     <p>
     Usage in: <br> {@link #mapSizeBigger(Model, Map, HttpServletRequest)} (99)
     <p>
     Uses: <br> 1.1 {@link NetScannerSvc#getPCNamesPref(String)}

     @param request {@link HttpServletRequest} от пользователя через браузер
     @param model   {@link Model} для сборки
     */
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
                .addAttribute(TITLE_STR, new Date(this.propLastScanMinusDuration))
                .addAttribute("pc", new TForms().fromArray(pCsAsync, true));
            AppComponents.lastNetScan().setTimeLastScan(new Date());
            properties.setProperty("lastscan", System.currentTimeMillis() + "");
            lastScan.clear();
        }
    }

    /**
     POST /netscan
     <p>
     @param netScannerSvc {@link NetScannerSvc}
     @param result {@link BindingResult}
     @param model {@link Model}
     @return redirect:/ad? + {@link NetScannerSvc#getThePc()}
     */
    @PostMapping ("/netscan")
    public String pcNameForInfo(@ModelAttribute NetScannerSvc netScannerSvc, BindingResult result, Model model) {
        String thePc = netScannerSvc.getThePc();
        AppComponents.adSrv().setUserInputRaw(thePc);
        if(thePc.toLowerCase().contains("user: ")){
            model.addAttribute("ok", getUserFromDB(thePc));
            model.addAttribute(ConstantsFor.TITLE, thePc);
            model.addAttribute(ConstantsFor.FOOTER, new PageFooter().getFooterUtext());
            return "ok";
        }
        netScannerSvc.getInfoFromDB();
        model.addAttribute("thePc", thePc);
        AppComponents.adSrv().setUserInputRaw(netScannerSvc.getThePc());
        netScannerSvc.setThePc("");
        return "redirect:/ad?" + thePc;
    }

    /**
     Достаёт инфо о пользователе из БД
     <p>
     Usages: {@link #pcNameForInfo(NetScannerSvc, BindingResult, Model)} <br>
     Uses: - <br>

     @param userInputRaw {@link NetScannerSvc#getThePc()}
     @return LAST 20 USER PCs
     */
    private String getUserFromDB(String userInputRaw) {
        try {
            userInputRaw = userInputRaw.split(": ")[1];
        } catch (ArrayIndexOutOfBoundsException e) {
            return e.getMessage();
        }
        try (Connection c = new RegRuMysql().getDefaultConnection(ConstantsFor.DB_PREFIX + "velkom");
             PreparedStatement p = c.prepareStatement("select * from pcuserauto where userName like ? ORDER BY whenQueried DESC LIMIT 0, 20")) {
            p.setString(1, "%" + userInputRaw + "%");
            try (ResultSet r = p.executeQuery()) {
                StringBuilder stringBuilder = new StringBuilder();
                String headER = "<h3><center>LAST 20 USER PCs</center></h3>";
                stringBuilder.append(headER);
                while (r.next()) {
                    String pcName = r.getString("pcName");
                    String returnER = "<br><center><a href=\"/ad?" + pcName.split("\\Q.\\E")[0] + "\">" + pcName + "</a> set: " +
                        r.getString("whenQueried") + "</center>";
                    stringBuilder.append(returnER);
                }
                return stringBuilder.toString();
            }
        } catch (SQLException e) {
            return e.getMessage();
        }
    }
}
