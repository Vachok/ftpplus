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
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageSwing;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.*;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.services.MyCalen;
import ru.vachok.networker.services.TimeChecker;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.*;
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
    private static ConcurrentMap<String, Boolean> lastScan = AppComponents.lastNetScanMap();

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
        LOGGER.warn("NetScanCtr.pcNameForInfo");
        String thePc = netScannerSvc.getThePc();
        AppComponents.adSrv().setUserInputRaw(thePc);
        if (thePc.toLowerCase().contains("user: ")) {
            model.addAttribute("ok", MoreInfoGetter.getUserFromDB(thePc));
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
        String classMeth = "NetScanCtr.netScan";
        LOGGER.warn(classMeth);
        Thread.currentThread().setName(classMeth);
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
        LOGGER.warn("NetScanCtr.getVis");
        try {
            return AppComponents.thisVisit(request.getSession().getId());
        } catch (InvocationTargetException | NullPointerException | NoSuchBeanDefinitionException e) {
            return new AppComponents().visitor(request);
        }
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
        LOGGER.warn("NetScanCtr.mapSizeBigger");
        int thisTotpc;
        try{
            thisTotpc = Integer.parseInt(properties.getProperty(ConstantsFor.PR_TOTPC));
        }
        catch(NumberFormatException | NullPointerException e){
            thisTotpc = 318;
            new MessageSwing().infoNoTitles(thisTotpc + " PC Exception...\n" + e.getMessage() + " in " + new Date(new TimeChecker().call().getReturnTime()));
        }
        String propertyLastScan = properties.getProperty(ConstantsNet.PR_LASTSCAN, "1515233487000");
        long timeLeft = TimeUnit.MILLISECONDS.toSeconds(Long.parseLong(propertyLastScan) - System.currentTimeMillis());

        String msg = new StringBuilder()
            .append(timeLeft).append(" seconds (")
            .append(( float ) timeLeft / ConstantsFor.ONE_HOUR_IN_MIN).append(" min) left<br>Delay period is ")
            .append(DURATION).toString();
        LOGGER.warn(msg);

        int remainPC = thisTotpc - netWork.size();
        int pcWas = Integer.parseInt(properties.getProperty(ConstantsNet.ONLINEPC, "0"));
        long lastScanEpoch = Long.parseLong(propertyLastScan);
        LastNetScan.getLastNetScan().setTimeLastScan(new Date(lastScanEpoch));
        lastScanEpoch = lastScanEpoch / 1000;
        model
            .addAttribute("left", msg)
            .addAttribute("pc", new TForms().fromArray(netWork, true))
            .addAttribute(ConstantsFor.ATT_TITLE, new StringBuilder()
                .append(remainPC).append("/")
                .append(thisTotpc).append(" PCs (")
                .append(netScannerSvc.getOnLinePCs()).append("/")
                .append(pcWas).append(" at ")
                .append(LocalDateTime.ofEpochSecond(lastScanEpoch, 0, ZoneOffset.ofHours(3)).toLocalTime().toString()).toString());
        boolean newPSs = 0 > remainPC;
        if(newPSs){
            writeToFile(netWork);
            model.addAttribute("newpc", "Добавлены компы! " + Math.abs(remainPC) + " шт.");
            properties.setProperty(ConstantsFor.PR_TOTPC, netWork.size() + "");
        }
        else{
            if(3 > remainPC){
                properties.setProperty(ConstantsFor.PR_TOTPC, netWork.size() + "");
                writeToFile(netWork);
            }
        }
        timeCheck(remainPC, netWork, lastScanEpoch, request, model);
        FileSystemWorker.recFile(FNAME_LASTSCANNET_TXT, netWork.keySet().stream());
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
        LOGGER.warn("NetScanCtr.scanIt");
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
            lastScan.clear();
        }
    }

    /**
     @param netWork {@link #lastScan}
     */
    private void writeToFile(Map<String, Boolean> netWork) {
        LOGGER.warn("NetScanCtr.writeToFile");
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

    private void timeCheck(int remainPC, Map<String, Boolean> netWork, long lastScanEpoch, HttpServletRequest request, Model model) {
        LOGGER.warn("NetScanCtr.timeCheck");
        LocalTime localTime = LocalDateTime.ofEpochSecond(lastScanEpoch, 0, ZoneOffset.ofHours(3)).toLocalTime();
        boolean isSystemTimeBigger = (System.currentTimeMillis() > lastScanEpoch * 1000) && remainPC <= 0;
        long eSecNow = System.currentTimeMillis() / 1000;
        String pamStr = eSecNow + "(" + (eSecNow - lastScanEpoch) / ConstantsFor.ONE_HOUR_IN_MIN + " min diff, current-last)\n" +
            (eSecNow > lastScanEpoch) + " currentTimeMillis() >lastScan(" + lastScanEpoch + "\n" + (netWork.size() <= 0) +
            " netWork.size()<=0\nremainPC = [" + remainPC + "], netWork = [" + netWork.size() + "], lastScanEpoch = [" +
            lastScanEpoch + "], min left = " +
            "[" +
            TimeUnit.SECONDS.toMinutes(LocalTime.parse("07:00").toSecondOfDay() - LocalTime.now().toSecondOfDay()) +
            "], uptime = [" + ConstantsFor.getUpTime() + "]";
        if(isSystemTimeBigger){
            String msg1 = new StringBuilder().append("isSystemTimeBigger is ")
                .append(true).append(" ")
                .append(netWork.size()).append(" network map cleared\n")
                .append(localTime.toString()).toString();
            LOGGER.warn(msg1);
            scanIt(request, model);
        }
        else{
            new MessageCons().infoNoTitles(pamStr);
        }
    }

    @GetMapping ("/showalldev")
    public String allDevices(Model model, HttpServletRequest request, HttpServletResponse response) {
        LOGGER.warn("NetScanCtr.allDevices");
        NetScanFileWorker netScanFileWorker = NetScanFileWorker.getI();
        model.addAttribute(ConstantsFor.ATT_TITLE, "DiapazonedScan.scanAll");
        if(request.getQueryString()!=null){
            ConditionChecker.qerNotNullScanAllDevices(model, netScanFileWorker, response);
        }
        model.addAttribute("head", new PageFooter().getHeaderUtext() + "<center><p><a href=\"/showalldev?needsopen\"><h2>Show IPs</h2></a></center>");
        model.addAttribute("ok", DiapazonedScan.getInstance().toString());
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext() + ". Left: " + ConstantsFor.ALL_DEVICES.remainingCapacity() + " " +
            "IPs.");
        return "ok";
    }
}
