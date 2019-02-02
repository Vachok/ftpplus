package ru.vachok.networker.net;


import org.slf4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.messenger.MessageCons;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.componentsrepo.LastNetScan;
import ru.vachok.networker.componentsrepo.PageFooter;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.enums.ConstantsNet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    private static final int DURATION = ( int ) ConstantsFor.DELAY;

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

    private static final String STR_REQUEST = "request = [";

    private static final String STR_MODEL = "], model = [";

    /**
     {@link AppComponents#lastNetScanMap()}
     */
    private static ConcurrentMap<String, Boolean> lastScanMAP = AppComponents.lastNetScanMap();

    /**
     POST /netscan
     <p>

     @param netScannerSvc {@link NetScannerSvc}
     @param result        {@link BindingResult}
     @param model         {@link Model}
     @return redirect:/ad? + {@link NetScannerSvc#getThePc()}
     */
    @SuppressWarnings ("MethodWithMultipleReturnPoints")
    @PostMapping (STR_NETSCAN)
    public String pcNameForInfo(@ModelAttribute NetScannerSvc netScannerSvc, BindingResult result, Model model) {
        LOGGER.warn("NetScanCtr.pcNameForInfo");
        String thePc = netScannerSvc.getThePc();
        AppComponents.adSrv().setUserInputRaw(thePc);
        if(thePc.toLowerCase().contains("user: ")){
            model.addAttribute("ok", MoreInfoGetter.getUserFromDB(thePc));
            model.addAttribute(ConstantsFor.ATT_TITLE, thePc);
            model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext());
            return "ok";
        }
        NetScannerSvc.getInfoFromDB();
        model.addAttribute(ATT_THE_PC, thePc);
        AppComponents.adSrv().setUserInputRaw(netScannerSvc.getThePc());
        netScannerSvc.setThePc("");
        return "redirect:/ad?" + thePc;
    }

    /**
     GET /{@link #STR_NETSCAN Старт сканера локальных ПК
    <p>
    1. {@link ConstantsFor#getVis(javax.servlet.http.HttpServletRequest)}. Записываем визит. <br> 2. {@link NetScannerSvc#setThePc(java.lang.String)},
    обнулим строку ввода через {@link
    #netScannerSvc}. <br> 3. {@link #mapSizeBigger(org.springframework.ui.Model, java.util.Map, javax.servlet.http.HttpServletRequest, long)}. Если {

    @param request  {@link HttpServletRequest}
    @param response {@link HttpServletResponse}
    @param model    {@link Model}
    @return {@link #AT_NAME_NETSCAN}
    @link #lastScanMAP} больше 2х, запустим это. <br>

    <p>
     */
    @GetMapping (STR_NETSCAN)
    public String netScan(HttpServletRequest request, HttpServletResponse response, Model model) {
        String classMeth = "NetScanCtr.netScan";
        new MessageCons().errorAlert(classMeth);
        new MessageCons().info(
            STR_REQUEST + request + "], response = [" + response + STR_MODEL + model + "]",
            ConstantsFor.STR_INPUT_PARAMETERS_RETURNS,
            ConstantsFor.JAVA_LANG_STRING_NAME);
        Thread.currentThread().setName(classMeth);

        ConstantsFor.getVis(request);
        netScannerSvc.setThePc("");

        checkMapSizeAndDoAction(model, request);

        model.addAttribute(ConstantsNet.STR_NETSCANNERSVC, netScannerSvc)
            .addAttribute("serviceinfo", netScannerSvc.someInfo())
            .addAttribute(ATT_THE_PC, netScannerSvc.getThePc());
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext() + "<br>First Scan: 2018-05-05");
        response.addHeader(ConstantsFor.HEAD_REFRESH, "30");

        AppComponents.lastNetScan().setTimeLastScan(new Date());
        return AT_NAME_NETSCAN;
    }

    private void checkMapSizeAndDoAction(Model model, HttpServletRequest request) {
        long lastSt = Long.parseLong(properties.getProperty(ConstantsNet.PR_LASTSCAN, "1548919734742"));
        boolean isMapSizeBigger = lastScanMAP.size() > 1;
        int thisTotpc = Integer.parseInt(properties.getProperty(ConstantsFor.PR_TOTPC, "318"));
        int remainPC = thisTotpc - lastScanMAP.size();
        final Runnable runnableScan = () -> {
            String s = "isMapSizeBigger = ";
            if(isMapSizeBigger){
                new MessageCons().infoNoTitles(s + true);
                mapSizeBigger(model, request, lastSt, remainPC, thisTotpc);
            }
            else{
                new MessageCons().infoNoTitles(s + false);
                scanIt(request, model, new Date(lastSt));
            }
        };
        new Thread(runnableScan).start();
        new MessageCons().errorAlert("NetScanCtr.checkMapSizeAndDoAction");
        new MessageCons().info(
            "model = [" + model + "], request = [" + request + "]",
            ConstantsFor.STR_INPUT_PARAMETERS_RETURNS,
            "void");
    }

    /**
     Usage in: {@link #netScan(HttpServletRequest, HttpServletResponse, Model)}
     <p>
     Uses: <br> 1.1 {@link TForms#fromArray(Map, boolean)} 1.2 {@link #scanIt(HttpServletRequest, Model)}

     @param model   {@link Model} для сборки
     @param netWork временная {@link Map} для хранения данных во-время работы метода.
     @param request {@link HttpServletRequest}
     */
    private void mapSizeBigger(Model model, HttpServletRequest request, long lastSt, int remainPC, int thisTotpc) {
        new MessageCons().errorAlert("NetScanCtr.mapSizeBigger");
        long timeLeft = TimeUnit.MILLISECONDS.toSeconds(lastSt - System.currentTimeMillis());
        int pcWas = Integer.parseInt(properties.getProperty(ConstantsNet.ONLINEPC, "0"));
        boolean newPSs = 0 > remainPC;

        String msg = new StringBuilder()
            .append(timeLeft).append(" seconds (")
            .append(( float ) timeLeft / ConstantsFor.ONE_HOUR_IN_MIN).append(" min) left<br>Delay period is ")
            .append(DURATION).toString();
        LOGGER.warn(msg);
        LastNetScan.getLastNetScan().setTimeLastScan(new Date(lastSt));
        model
            .addAttribute("left", msg)
            .addAttribute("pc", new TForms().fromArray(lastScanMAP, false))
            .addAttribute(ConstantsFor.ATT_TITLE, new StringBuilder()
                .append(remainPC).append("/")
                .append(thisTotpc).append(" PCs (")
                .append(netScannerSvc.getOnLinePCs()).append("/")
                .append(pcWas).append(" at ")
                .append(LocalDateTime.ofEpochSecond(lastSt / 1000, 0, ZoneOffset.ofHours(3)).toLocalTime().toString()).toString());

        if(newPSs){
            FileSystemWorker.recFile(ConstantsNet.STR_LASTNETSCAN, new TForms().fromArray(lastScanMAP, false));
            model.addAttribute("newpc", "Добавлены компы! " + Math.abs(remainPC) + " шт.");
            properties.setProperty(ConstantsFor.PR_TOTPC, lastScanMAP.size() + "");
        }
        else{
            if(3 > remainPC){
                properties.setProperty(ConstantsFor.PR_TOTPC, lastScanMAP.size() + "");
            }
        }
        timeCheck(remainPC, lastSt / 1000, request, model);
        FileSystemWorker.recFile(FNAME_LASTSCANNET_TXT, lastScanMAP.keySet().stream());
    }

    /**
     Модель для {@link #netScan(HttpServletRequest, HttpServletResponse, Model)} <br> Делает проверки на {@link HttpServletRequest#getQueryString()},
     если != 0: <br> {@link #lastScanMAP}.clear() <br>
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
    private void scanIt(HttpServletRequest request, Model model, Date propLastScanMinusDuration) {
        String propMsg = "NetScanCtr.scanIt. " + propLastScanMinusDuration;
        String valStr = "propMsg = " + propMsg;
        new MessageCons().info(Thread.currentThread().getName(), "NetScanCtr.scanIt", valStr);

        properties.setProperty("propmsg", propMsg);
        if(request!=null && request.getQueryString()!=null){
            lastScanMAP.clear();
            Set<String> pcNames = netScannerSvc.getPCNamesPref(request.getQueryString());
            model.addAttribute(ConstantsFor.ATT_TITLE, new Date().toString())
                .addAttribute("pc", new TForms().fromArray(pcNames, true));
        }
        else{
            lastScanMAP.clear();
            Set<String> pCsAsync = netScannerSvc.getPcNames();
            model.addAttribute(ConstantsFor.ATT_TITLE, propLastScanMinusDuration)
                .addAttribute("pc", new TForms().fromArray(pCsAsync, true));
            AppComponents.lastNetScan().setTimeLastScan(new Date());
        }

        new MessageCons().info(ConstantsFor.STR_INPUT_OUTPUT,
            STR_REQUEST + request + STR_MODEL + model + "], propLastScanMinusDuration = [" + propLastScanMinusDuration + "]", "void");
    }

    private void timeCheck(int remainPC, long lastScanEpoch, HttpServletRequest request, Model model) {
        String classMeth = " NetScanCtr.timeCheck";
        LocalTime nextScanTime = LocalDateTime.ofEpochSecond(lastScanEpoch, 0, ZoneOffset.ofHours(3)).toLocalTime();
        new MessageCons().errorAlert(classMeth);
        boolean isSystemTimeBigger = (System.currentTimeMillis() > lastScanEpoch * 1000) && remainPC <= 0;

        String s = "NetScanCtr.timeCheck";
        if(isSystemTimeBigger){
            String valStr = "isSystemTimeBigger = " + true;
            new MessageCons().info(Thread.currentThread().getName(), s, valStr);
            scanIt(request, model, new Date(lastScanEpoch * 1000));
        }
        else{
            String valStr = "nextScanTime = " + nextScanTime;
            new MessageCons().infoNoTitles(Thread.currentThread().getName() + "\n" + s + "\n" + valStr);
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NetScanCtr{");
        sb.append("AT_NAME_NETSCAN='").append(AT_NAME_NETSCAN).append('\'');
        sb.append(", ATT_THE_PC='").append(ATT_THE_PC).append('\'');
        sb.append(", DURATION=").append(DURATION);
        sb.append(", FNAME_LASTSCANNET_TXT='").append(FNAME_LASTSCANNET_TXT).append('\'');
        sb.append(", lastScanMAP=").append(lastScanMAP.size());
        sb.append(", properties=").append(properties);
        sb.append(", STR_NETSCAN='").append(STR_NETSCAN).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
