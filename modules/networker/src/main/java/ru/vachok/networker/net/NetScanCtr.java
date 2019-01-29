package ru.vachok.networker.net;


import org.apache.commons.net.ntp.TimeInfo;
import org.slf4j.Logger;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.messenger.MessageSwing;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.componentsrepo.PageFooter;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MyCalen;
import ru.vachok.networker.services.TimeChecker;
import ru.vachok.networker.systray.ActionDefault;
import ru.vachok.networker.systray.MessageToTray;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;


/**
 Контроллер netscan.html
 <p>

 @since 30.08.2018 (12:55) */
@Controller
public class NetScanCtr {


    /**
     Имя {@link Model} атрибута.
     */
    private static final String AT_NAME_NETSCAN = "netscan";

    /**
     {@link AppComponents#getLogger()}
     */
    private static final Logger LOGGER = AppComponents.getLogger();

    /**
     {@link ConstantsFor#getProps()}
     */
    private static final Properties properties = ConstantsFor.getProps();

    /**
     {@link ConstantsFor#DELAY}
     */
    private static final int DURATION = (int) ConstantsFor.DELAY;

    /**
     <i>Boiler Plate</i>
     */
    private static final String STR_NETSCAN = "/netscan";

    /**
     <i>Boiler Plate</i>
     */
    private static final String ATT_THE_PC = "thePc";

    /**
     <i>Boiler Plate</i>
     */
    private static final String FNAME_LASTSCANNET_TXT = "lastscannet.txt";

    /**
     {@link NetScannerSvc#getI()}
     */
    private static final NetScannerSvc netScannerSvc = NetScannerSvc.getI();

    /**
     {@link AppComponents#lastNetScanMap()}
     */
    private static final ConcurrentMap<String, Boolean> lastScan = AppComponents.lastNetScanMap();

    /**
     Отрезок времени для промежутка в сканировании.
     */
    private long propLastScanMinusDuration = 0L;

    /**
     POST /netscan
     <p>

     @param netScannerSvc {@link NetScannerSvc}
     @param result        {@link BindingResult}
     @param model         {@link Model}
     @return redirect:/ad? + {@link NetScannerSvc#getThePc()}
     */
    @SuppressWarnings("MethodWithMultipleReturnPoints")
    @PostMapping(STR_NETSCAN)
    public String pcNameForInfo(@ModelAttribute NetScannerSvc netScannerSvc, BindingResult result, Model model) {
        String thePc = netScannerSvc.getThePc();
        AppComponents.adSrv().setUserInputRaw(thePc);
        if (thePc.toLowerCase().contains("user: ")) {
            model.addAttribute("ok", getUserFromDB(thePc));
            model.addAttribute(ConstantsFor.ATT_TITLE, thePc);
            model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext());
            return "ok";
        }
        netScannerSvc.getInfoFromDB();
        model.addAttribute(ATT_THE_PC, thePc);
        AppComponents.adSrv().setUserInputRaw(netScannerSvc.getThePc());
        netScannerSvc.setThePc("");
        return "redirect:/ad?" + thePc;
    }

    /**
     Достаёт инфо о пользователе из БД
     <p>
     Usages: {@link #pcNameForInfo(NetScannerSvc, BindingResult, Model)} <br> Uses: - <br>

     @param userInputRaw {@link NetScannerSvc#getThePc()}
     @return LAST 20 USER PCs
     */
    @SuppressWarnings("MethodWithMultipleReturnPoints")
    private String getUserFromDB(String userInputRaw) {
        StringBuilder retBuilder = new StringBuilder();
        try {
            userInputRaw = userInputRaw.split(": ")[1];
        } catch (ArrayIndexOutOfBoundsException e) {
            retBuilder.append(e.getMessage()).append("\n").append(new TForms().fromArray(e, false));
        }
        try (Connection c = new RegRuMysql().getDefaultConnection(ConstantsFor.DB_PREFIX + ConstantsFor.STR_VELKOM);
             PreparedStatement p = c.prepareStatement("select * from pcuserauto where userName like ? ORDER BY whenQueried DESC LIMIT 0, 20")) {
            p.setString(1, "%" + userInputRaw + "%");
            try (ResultSet r = p.executeQuery()) {
                StringBuilder stringBuilder = new StringBuilder();
                String headER = "<h3><center>LAST 20 USER PCs</center></h3>";
                stringBuilder.append(headER);
                while (r.next()) {
                    String pcName = r.getString(ConstantsFor.DB_FIELD_PCNAME);
                    String returnER = "<br><center><a href=\"/ad?" + pcName.split("\\Q.\\E")[0] + "\">" + pcName + "</a> set: " +
                        r.getString(ConstantsNet.DB_FIELD_WHENQUERIED) + ConstantsFor.HTML_CENTER;
                    stringBuilder.append(returnER);
                    if (r.last()) {
                        MessageToUser messageToUser = new MessageToTray(new ActionDefault("http://localhost:8880/netscan"));
                        messageToUser.info(
                            r.getString(ConstantsFor.DB_FIELD_PCNAME),
                            r.getTimestamp("stamp").toLocalDateTime().toString(),
                            r.getString(ConstantsFor.DB_FIELD_USER));
                    }
                }
                return stringBuilder.toString();
            }
        } catch (SQLException e) {
            retBuilder.append(e.getMessage()).append("\n").append(new TForms().fromArray(e, false));
        }
        return retBuilder.toString();
    }

    /**
     GET /netscan
     <p>
     Usages: {@link #scanIt(HttpServletRequest, Model)}, {@link #mapSizeBigger(Model, Map, HttpServletRequest)} <br> Uses: {@link PageFooter#getFooterUtext()}, {@link
    AppComponents#lastNetScan()}.setTimeLastScan(new {@link Date}) <br>

     @return {@link #AT_NAME_NETSCAN}.html
     @param request  {@link HttpServletRequest}
     @param response {@link HttpServletResponse}
     @param model    {@link Model}
     */
    @GetMapping(STR_NETSCAN)
    public String netScan(HttpServletRequest request, HttpServletResponse response, Model model) {
        Thread.currentThread().setName("NetScanCtr.netScan");
        Visitor visitor = getVis(request);
        netScannerSvc.setThePc("");
        LOGGER.warn("{}", visitor);
        Map<String, Boolean> netWork = lastScan;
        boolean isMapSizeBigger = netWork.size() > 2;

        if (isMapSizeBigger) {
            mapSizeBigger(model, netWork, request);
        } else {
            scanIt(request, model);
        }
        model
            .addAttribute(ConstantsNet.STR_NETSCANNERSVC, netScannerSvc)
            .addAttribute("serviceinfo", netScannerSvc.someInfo())
            .addAttribute(ATT_THE_PC, netScannerSvc.getThePc());
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext() + "<br>First Scan: 2018-05-05");
        AppComponents.lastNetScan().setTimeLastScan(new Date());
        response.addHeader(ConstantsFor.HEAD_REFRESH, "30");
        return AT_NAME_NETSCAN;
    }

    @SuppressWarnings("MethodWithMultipleReturnPoints")
    private Visitor getVis(HttpServletRequest request) {
        try {
            return AppComponents.thisVisit(request.getSession().getId());
        } catch (InvocationTargetException | NullPointerException | NoSuchBeanDefinitionException e) {
            return new AppComponents().visitor(request);
        }
    }

    /**
     Модель для {@link #netScan(HttpServletRequest, HttpServletResponse, Model)} <br> Делает проверки на {@link HttpServletRequest#getQueryString()}, если != 0: <br> {@link #lastScan}.clear() <br>
     {@link NetScannerSvc#getPCNamesPref(String)}
     <p>
     Добавляет в {@link Model}: <br> {@link ConstantsFor#ATT_TITLE} = {@link Date}.toString() <br>
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
            Set<String> pcNames = netScannerSvc.getPCNamesPref(request.getQueryString());
            model
                .addAttribute(ConstantsFor.ATT_TITLE, new Date().toString())
                .addAttribute("pc", new TForms().fromArray(pcNames, true));
        } else {
            lastScan.clear();
            Set<String> pCsAsync = netScannerSvc.getPcNames();
            model
                .addAttribute(ConstantsFor.ATT_TITLE, new Date(this.propLastScanMinusDuration))
                .addAttribute("pc", new TForms().fromArray(pCsAsync, true));
            AppComponents.lastNetScan().setTimeLastScan(new Date());
            properties.setProperty(ConstantsFor.PR_LASTSCAN, System.currentTimeMillis() + "");
            lastScan.clear();
        }
    }

    @GetMapping("/showalldev")
    public String allDevices(Model model, HttpServletRequest request, HttpServletResponse response) {
        NetScanFileWorker netScanFileWorker = NetScanFileWorker.getI();
        model.addAttribute(ConstantsFor.ATT_TITLE, "DiapazonedScan.scanAll");
        if (request.getQueryString() != null) {
            StringBuilder stringBuilder = new StringBuilder();
            if (ConstantsFor.ALL_DEVICES.remainingCapacity() == 0) {
                ConstantsFor.ALL_DEVICES.forEach(x -> stringBuilder.append(ConstantsFor.ALL_DEVICES.remove()));
                model.addAttribute("pcs", stringBuilder.toString());
            } else {
                allDevNotNull(model, netScanFileWorker, response);
            }
        }
        model.addAttribute("head", new PageFooter().getHeaderUtext() + "<center><p><a href=\"/showalldev?needsopen\"><h2>Show IPs</h2></a></center>");
        model.addAttribute("ok", DiapazonedScan.getInstance().toString());
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext() + ". Left: " + ConstantsFor.ALL_DEVICES.remainingCapacity() + " IPs.");
        return "ok";
    }

    private void allDevNotNull(Model model, NetScanFileWorker netScanFileWorker, HttpServletResponse response) {
        final float scansInMin = 45.9f;
        float minLeft = ConstantsFor.ALL_DEVICES.remainingCapacity() / scansInMin;
        String attributeValue = new StringBuilder()
            .append(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis((long) minLeft)))
            .append(" ~minLeft: ").append(minLeft)
            .toString();
        model.addAttribute(ConstantsFor.ATT_TITLE, attributeValue);
        if (netScanFileWorker.equals(DiapazonedScan.getNetScanFileWorker())) {
            model.addAttribute("pcs",
                "OFF: " + ScanOnline.getI().toString() + "<p>" + netScanFileWorker.getNewLanLastScanAsStr() + "<p>" + netScanFileWorker.getOldLanLastScanAsStr());
        } else {
            model.addAttribute("pcs", FileSystemWorker.readFile(ConstantsFor.AVAILABLE_LAST_TXT) + "<p>" + FileSystemWorker.readFile(ConstantsFor.OLD_LAN_TXT));
        }
        response.addHeader(ConstantsFor.HEAD_REFRESH, "60");
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
        int thisTotpc;
        try {
            thisTotpc = Integer.parseInt(properties.getProperty(ConstantsFor.PR_TOTPC));
        } catch (NumberFormatException | NullPointerException e) {
            thisTotpc = 318;
            new MessageSwing().infoNoTitles(thisTotpc + " PC Exception...\n" + e.getMessage() + " in " + new Date(new TimeChecker().call().getReturnTime()));
        }
        String propertyLastScan = properties.getProperty(ConstantsFor.PR_LASTSCAN, "1515233487000");
        propLastScanMinusDuration = Long.parseLong(propertyLastScan) + TimeUnit.MINUTES.toMillis(DURATION);
        boolean isSystemTimeBigger = (System.currentTimeMillis() > propLastScanMinusDuration);
        long timeLeft = TimeUnit.MILLISECONDS.toSeconds(propLastScanMinusDuration - System.currentTimeMillis());
        String msg = timeLeft + " seconds (" + (float) timeLeft / ConstantsFor.ONE_HOUR_IN_MIN + " min) left<br>Delay period is " + DURATION;
        LOGGER.warn(msg);
        int i = thisTotpc - netWork.size();
        int pcWas = Integer.parseInt(properties.getProperty("onlinepc", "0"));
        model
            .addAttribute("left", msg)
            .addAttribute("pc", new TForms().fromArray(netWork, true))
            .addAttribute(ConstantsFor.ATT_TITLE, i + "/" + thisTotpc + " PCs (" + netScannerSvc.getOnLinePCs() + "/" + pcWas + " at last)");
        if (0 > i) {
            writeToFile(netWork);
            model.addAttribute("newpc", "Добавлены компы! " + Math.abs(i) + " шт.");
            properties.setProperty(ConstantsFor.PR_TOTPC, netWork.size() + "");
            ConstantsFor.saveProps(properties);
        } else {
            if (3 > i) {
                properties.setProperty(ConstantsFor.PR_TOTPC, netWork.size() + "");
                ConstantsFor.saveProps(properties);
                writeToFile(netWork);
                if (isSystemTimeBigger) {
                    String msg1 = "isSystemTimeBigger is " + true + " " + netWork.size() + " network map cleared";
                    LOGGER.warn(msg1);
                    scanIt(request, model);
                }
            }
        }
        FileSystemWorker.recFile(FNAME_LASTSCANNET_TXT, netWork.keySet().stream());
    }

    /**
     @param netWork {@link #lastScan}
     */
    private void writeToFile(Map<String, Boolean> netWork) {
        try (OutputStream outputStream = new FileOutputStream(FNAME_LASTSCANNET_TXT);
             PrintWriter printWriter = new PrintWriter(outputStream, true)) {
            printWriter.println("3 > Network Size!");
            TimeInfo timeInfo = MyCalen.getTimeInfo();
            timeInfo.computeDetails();
            printWriter.println(new Date(timeInfo.getReturnTime()));
            netWork.forEach((x, y) -> printWriter.println(x + " " + y));
        } catch (IOException e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }
}
