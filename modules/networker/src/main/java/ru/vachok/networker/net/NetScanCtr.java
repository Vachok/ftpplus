package ru.vachok.networker.net;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
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
import ru.vachok.networker.componentsrepo.PageFooter;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.net.enums.OtherKnownDevices;
import ru.vachok.networker.services.MessageLocal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;


/**
 Контроллер netscan.html
 <p>

 @since 30.08.2018 (12:55) */
@Controller
public class NetScanCtr {


    /**
     {@link LoggerFactory#getLogger(String)}
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(NetScanCtr.class.getSimpleName());

    /**
     {@link AppComponents#getProps()}
     */
    private static final Properties PROPERTIES = AppComponents.getProps();

    /**
     {@link ConstantsFor#DELAY}
     */
    private static final int DURATION_MIN = (int) ConstantsFor.DELAY;

    /**
     <i>Boiler Plate</i>
     */
    private static final String STR_NETSCAN = "/netscan";

    /**
     <i>Boiler Plate</i>
     */
    private static final String ATT_THEPC = "thePc";

    /**
     {@link AppComponents#netScannerSvc()}
     */
    private static final NetScannerSvc NETSCANNERSVC_INST = AppComponents.netScannerSvc();

    private static final String STR_REQUEST = "request = [";

    private static final String STR_MODEL = "], model = [";

    private static final String ATT_NETPINGER = "netPinger";

    /**
     {@link AppComponents#lastNetScanMap()}
     */
    private static ConcurrentMap<String, Boolean> lastScanMAP = AppComponents.lastNetScanMap();

    private NetPinger netPingerInst = AppComponents.netPinger();

    private static Deque<InetAddress> getDeqAddr() {
        Deque<InetAddress> retDeq = new ConcurrentLinkedDeque<>();
        try {
            byte[] inetAddressBytes = InetAddress.getByName(OtherKnownDevices.MOB_KUDR).getAddress();
            retDeq.add(InetAddress.getByAddress(inetAddressBytes));
            inetAddressBytes = InetAddress.getByName("10.10.111.1").getAddress();
            retDeq.add(InetAddress.getByAddress(inetAddressBytes));
        } catch (UnknownHostException e) {
            new MessageCons().errorAlert("NetScanCtr", "getDeqAddr", e.getMessage());
        }
        return retDeq;
    }

    @GetMapping("/ping")
    public String pingAddr(Model model, HttpServletRequest request, HttpServletResponse response) {
        model.addAttribute(ATT_NETPINGER, netPingerInst);
        model.addAttribute("pingResult", FileSystemWorker.readFile(ConstantsNet.PINGRESULT_LOG));
        model.addAttribute(ConstantsFor.ATT_TITLE, netPingerInst.getTimeToEndStr() + " pinger hash: " + netPingerInst.hashCode());
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext());
        model.addAttribute("pingTest", new TForms().fromArray(netPingerInst.pingDev(getDeqAddr()), true) + "<br><b>" +
            netPingerInst.isReach(OtherKnownDevices.MOB_KUDR) + " my mobile...</b>");
        response.addHeader(ConstantsFor.HEAD_REFRESH, "60");
        return "ping";
    }

    @PostMapping("/ping")
    public String pingPost(Model model, HttpServletRequest request, @ModelAttribute NetPinger netPinger, HttpServletResponse response) {
        this.netPingerInst = netPinger;
        netPinger.run();
        model.addAttribute(ATT_NETPINGER, netPinger);
        String npEq = "Netpinger equals is " + netPinger.equals(this.netPingerInst);
        model.addAttribute(ConstantsFor.ATT_TITLE, npEq);
        model.addAttribute("ok", FileSystemWorker.readFile(ConstantsNet.PINGRESULT_LOG));
        new MessageLocal().infoNoTitles("npEq = " + npEq);
        response.addHeader(ConstantsFor.HEAD_REFRESH, PROPERTIES.getProperty(ConstantsNet.PROP_PINGSLEEP, "60"));
        return "ok";
    }

    /**
     POST /netscan
     <p>

     @param netScannerSvc {@link NetScannerSvc}
     @param result        {@link BindingResult}
     @param model         {@link Model}
     @return redirect:/ad? + {@link NetScannerSvc#getThePc()}
     */
    @PostMapping(STR_NETSCAN)
    public static String pcNameForInfo(@ModelAttribute NetScannerSvc netScannerSvc, BindingResult result, Model model) {
        LOGGER.warn("NetScanCtr.pcNameForInfo");
        String thePc = netScannerSvc.getThePc();
        AppComponents.adSrv().setUserInputRaw(thePc);
        if (thePc.toLowerCase().contains("user: ")) {
            model.addAttribute("ok", MoreInfoGetter.getUserFromDB(thePc));
            model.addAttribute(ConstantsFor.ATT_TITLE, thePc);
            model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext());
            return "ok";
        }
        NetScannerSvc.getInfoFromDB();
        model.addAttribute(ATT_THEPC, thePc);
        AppComponents.adSrv().setUserInputRaw(netScannerSvc.getThePc());
        netScannerSvc.setThePc("");
        return "redirect:/ad?" + thePc;
    }

    @GetMapping("/showalldev")
    public static String allDevices(Model model, HttpServletRequest request, HttpServletResponse response) {
        LOGGER.warn("NetScanCtr.allDevices");
        model.addAttribute(ConstantsFor.ATT_TITLE, "DiapazonedScan.scanAll");
        model.addAttribute("pcs", ScanOnline.getI().toString());
        if (request.getQueryString() != null) {
            ConditionChecker.qerNotNullScanAllDevices(model, response);
        }
        model.addAttribute("head", new PageFooter().getHeaderUtext() + "<center><p><a href=\"/showalldev?needsopen\"><h2>Show IPs</h2></a></center>");
        model.addAttribute("ok", DiapazonedScan.getInstance().toString());
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext() + ". Left: " + ConstantsFor.ALL_DEVICES.remainingCapacity() + " " +
            "IPs.");
        return "ok";
    }

    /**
     GET /{@link #STR_NETSCAN} Старт сканера локальных ПК
     <p>
     1. {@link ConstantsFor#getVis(javax.servlet.http.HttpServletRequest)}. Запись {@link Visitor } <br>
     2.{@link NetScannerSvc#setThePc(java.lang.String)} обнуляем строку в форме. <br>
     3. {@link FileSystemWorker#readFile(java.lang.String)} добавляем файл {@code lastnetscan.log} в качестве аттрибута {@code pc} в {@link Model} <br>
     4. {@link NetScannerSvc#getThePc()} аттрибут {@link Model} - {@link #ATT_THEPC}. <br>
     5. {@link PageFooter#getFooterUtext()} footer web-страницы. 6. {@link AppComponents#lastNetScan()} <br>
     7. {@link #checkMapSizeAndDoAction(Model, HttpServletRequest, long)} - начинаем проверку.
     <p>

     @param request  {@link HttpServletRequest} для {@link ConstantsFor#getVis(HttpServletRequest)}
     @param response {@link HttpServletResponse} добавить {@link ConstantsFor#HEAD_REFRESH} 30 сек
     @param model    {@link Model}
     @return {@link ConstantsNet#ATT_NETSCAN} (netscan.html)
     */
    @GetMapping(STR_NETSCAN)
    public static String netScan(HttpServletRequest request, HttpServletResponse response, Model model) {
        String classMeth = "NetScanCtr.netScan";
        final long lastSt = Long.parseLong(PROPERTIES.getProperty(ConstantsNet.PR_LASTSCAN, "1548919734742"));
        new MessageCons().errorAlert(classMeth);
        new MessageCons().info(
            STR_REQUEST + request + "], response = [" + response + STR_MODEL + model + "]",
            ConstantsFor.STR_INPUT_PARAMETERS_RETURNS,
            ConstantsFor.JAVA_LANG_STRING_NAME);

        Thread.currentThread().setName(classMeth);
        ConstantsFor.getVis(request);
        model.addAttribute("serviceinfo", (float) TimeUnit.MILLISECONDS.toSeconds(lastSt - System.currentTimeMillis()) / ConstantsFor.ONE_HOUR_IN_MIN);
        NETSCANNERSVC_INST.setThePc("");
        model.addAttribute("pc", FileSystemWorker.readFile(ConstantsNet.STR_LASTNETSCAN));
        model.addAttribute(ConstantsFor.ATT_TITLE, new Date(lastSt));
        model.addAttribute(ConstantsNet.STR_NETSCANNERSVC, NETSCANNERSVC_INST).addAttribute(ATT_THEPC, NETSCANNERSVC_INST.getThePc());
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext() + "<br>First Scan: 2018-05-05");
        response.addHeader(ConstantsFor.HEAD_REFRESH, "30");
        checkMapSizeAndDoAction(model, request, lastSt);
        return ConstantsNet.ATT_NETSCAN;
    }

    /**
     Если {@link #lastScanMAP} более 1
     <p>
     1. {@link TForms#fromArray(java.util.Map, boolean)} добавим в {@link Model} содержимое {@link #lastScanMAP} <br> 2.
     {@link NetScannerSvc#getOnLinePCs()} - в заголовке страницы, при обновлении,
     отображение остатка ПК. <br> 3. {@link TForms#fromArray(java.util.Map, boolean)} запишем файл {@link ConstantsNet#STR_LASTNETSCAN}, 4.
     {@link FileSystemWorker#recFile(java.lang.String,
         java.lang.String)} <br> 5. {@link #timeCheck(int, long, HttpServletRequest, Model)} переходим в проверке времени.
     <p>

     @param model     {@link Model}
     @param request   {@link HttpServletRequest}
     @param lastSt    время последнего скана. Берется из {@link #PROPERTIES}. Default: {@code 1548919734742}.
     @param thisTotpc кол-во ПК для скана. Берется из {@link #PROPERTIES}. Default: {@code 318}.
     @see #checkMapSizeAndDoAction(Model, HttpServletRequest, long)
     */
    private static void mapSizeBigger(Model model, HttpServletRequest request, long lastSt, int thisTotpc) {
        new MessageCons().errorAlert("NetScanCtr.mapSizeBigger");
        long timeLeft = TimeUnit.MILLISECONDS.toSeconds(lastSt - System.currentTimeMillis());
        final int pcWas = Integer.parseInt(PROPERTIES.getProperty(ConstantsNet.ONLINEPC, "0"));
        int remainPC = thisTotpc - lastScanMAP.size();
        boolean newPSs = 0 > remainPC;
        String msg = new StringBuilder()
            .append(timeLeft).append(" seconds (")
            .append((float) timeLeft / ConstantsFor.ONE_HOUR_IN_MIN).append(" min) left<br>Delay period is ")
            .append(DURATION_MIN).toString();
        LOGGER.warn(msg);
        model
            .addAttribute("left", msg)
            .addAttribute("pc", new TForms().fromArray(lastScanMAP, false))
            .addAttribute(ConstantsFor.ATT_TITLE, new StringBuilder()
                .append(remainPC).append("/")
                .append(thisTotpc).append(" PCs (")
                .append(NetScannerSvc.getOnLinePCs()).append("/")
                .append(pcWas).append(" at ")
                .append(LocalDateTime.ofEpochSecond(lastSt / 1000, 0, ZoneOffset.ofHours(3)).toLocalTime().toString()).toString());
        if (newPSs) {
            FileSystemWorker.recFile(ConstantsNet.STR_LASTNETSCAN, new TForms().fromArray(lastScanMAP, false));
            model.addAttribute("newpc", "Добавлены компы! " + Math.abs(remainPC) + " шт.");
            PROPERTIES.setProperty(ConstantsFor.PR_TOTPC, lastScanMAP.size() + "");
        } else {
            if (3 > remainPC) {
                PROPERTIES.setProperty(ConstantsFor.PR_TOTPC, lastScanMAP.size() + "");
            }
        }
        timeCheck(remainPC, lastSt / 1000, request, model);
    }

    /**
     Проверки времени
     <p>
     Если {@link System#currentTimeMillis()} больше {@code lastScanEpoch}*1000 ии {@code remainPC} меньше либо равно 0, запустить
     {@link #scanIt(HttpServletRequest, Model, Date)}. <br> Иначе выдать
     сообщение в консоль, с временем след. запуска.
     <p>

     @param remainPC      осталось ПК
     @param lastScanEpoch последнее сканирование Timestamp как <b>EPOCH Seconds</b>
     @param request       {@link HttpServletRequest}
     @param model         {@link Model}
     @see #mapSizeBigger(Model, HttpServletRequest, long, int)
     */
    private static void timeCheck(int remainPC, long lastScanEpoch, HttpServletRequest request, Model model) {
        String classMeth = " NetScanCtr.timeCheck";
        LOGGER.warn(classMeth);
        LocalTime lastScanLocalTime = LocalDateTime.ofEpochSecond(lastScanEpoch, 0, ZoneOffset.ofHours(3)).toLocalTime();
        boolean isSystemTimeBigger = (System.currentTimeMillis() > lastScanEpoch * 1000) && remainPC <= 0;
        if (isSystemTimeBigger) {
            String valStr = "isSystemTimeBigger = " + true;
            new MessageCons().info(Thread.currentThread().getName(), classMeth, valStr);
            scanIt(request, model, new Date(lastScanEpoch * 1000));
        } else {
            String valStr = "lastScanLocalTime = " + lastScanLocalTime;
            new MessageCons().infoNoTitles(Thread.currentThread().getName() + "\n" + classMeth + "\n" + valStr);
        }
    }

    /**
     Запуск скана.
     <p>
     Проверяем {@link HttpServletRequest} на наличие {@link HttpServletRequest#getQueryString()}. Если есть, сбрасываем {@link #lastScanMAP}, и
     запускаем {@link
    NetScannerSvc#getPCNamesPref(java.lang.String)}, где параметр это наша {@link HttpServletRequest#getQueryString()}. <br> В {@link Model}, добавим
     аттрибуты {@code title, pc}. new {@link Date} и
     {@link Set} pcNames, полученный из {@link NetScannerSvc#getPCNamesPref(java.lang.String)}
     <p>
     Иначе: <br> Очищаем {@link #lastScanMAP} <br> Запускаем {@link NetScannerSvc#getPcNames()} <br> В {@link Model} добавим {@code lastScanDate} как
     {@code title}, и {@link Set} {@link
    NetScannerSvc#getPcNames()}.

     @param request      {@link HttpServletRequest}
     @param model        {@link Model}
     @param lastScanDate дата последнего скана
     */
    private static void scanIt(HttpServletRequest request, Model model, Date lastScanDate) {
        String propMsg = "NetScanCtr.scanIt. " + lastScanDate;
        if (request != null && request.getQueryString() != null) {
            lastScanMAP.clear();
            Set<String> pcNames = NETSCANNERSVC_INST.getPCNamesPref(request.getQueryString());
            model.addAttribute(ConstantsFor.ATT_TITLE, new Date().toString())
                .addAttribute("pc", new TForms().fromArray(pcNames, true));
        } else {
            lastScanMAP.clear();
            Set<String> pCsAsync = NETSCANNERSVC_INST.getPcNames();
            model.addAttribute(ConstantsFor.ATT_TITLE, lastScanDate)
                .addAttribute("pc", new TForms().fromArray(pCsAsync, true));
            AppComponents.lastNetScan().setTimeLastScan(new Date());
        }
        String msg = propMsg + "\n" + ConstantsFor.STR_INPUT_OUTPUT + STR_REQUEST + request + STR_MODEL + model + "]+ lastScanDate = [" + lastScanDate + "]";
        LOGGER.info(msg);
    }

    /**
     Начало проверок перед сканом.
     <p>
     1. {@link AppComponents#threadConfig()}, 2. {@link ThreadConfig#getTaskExecutor()}. Получаем сконфигурированный {@link ThreadPoolTaskExecutor}
     <p>
     <b>Runnable:</b>
     3. {@link #mapSizeBigger(Model, HttpServletRequest, long, int)} когда {@link #lastScanMAP} больше 1. <br> или <br> 4.
     {@link #scanIt(HttpServletRequest, Model, Date)}.
     <p>
     Объявим дополнительно lock {@link File} {@code scan.tmp}. Чтобы исключить запуск, если предидущий скан не закончен. <br> Проверяем его наличие.
     Если он существует - запускаем {@link
    #mapSizeBigger(Model, HttpServletRequest, long, int)}, иначе отправляем <b>Runnable</b> в {@link ThreadConfig#getTaskExecutor()} (1)
     <p>

     @param model   {@link Model}
     @param request {@link HttpServletRequest}
     @param lastSt  timestamp из {@link #PROPERTIES}
     @see #netScan(HttpServletRequest, HttpServletResponse, Model)
     */
    private static void checkMapSizeAndDoAction(Model model, HttpServletRequest request, long lastSt) {
        boolean isMapSizeBigger = lastScanMAP.size() > 1;
        final int thisTotpc = Integer.parseInt(PROPERTIES.getProperty(ConstantsFor.PR_TOTPC, "318"));
        File f = new File("scan.tmp");
        if (f.isFile() && f.exists()) {
            mapSizeBigger(model, request, lastSt, thisTotpc);
        } else if (isMapSizeBigger) {
            mapSizeBigger(model, request, lastSt, thisTotpc);
        } else {
            ThreadConfig.executeAsThread(() -> scanIt(request, model, new Date(lastSt)));
        }
    }

    @Override
    public int hashCode() {
        return netPingerInst.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NetScanCtr)) return false;

        NetScanCtr that = (NetScanCtr) o;

        return netPingerInst.equals(that.netPingerInst);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NetScanCtr{");
        sb.append("ATT_NETSCAN='").append(ConstantsNet.ATT_NETSCAN).append('\'');
        sb.append(", PROPERTIES=").append(PROPERTIES.size());
        sb.append(", DURATION_MIN=").append(DURATION_MIN);
        sb.append(", STR_NETSCAN='").append(STR_NETSCAN).append('\'');
        sb.append(", ATT_THEPC='").append(ATT_THEPC).append('\'');
        sb.append(", NETSCANNERSVC_INST=").append(NETSCANNERSVC_INST.hashCode());
        sb.append(", STR_REQUEST='").append(STR_REQUEST).append('\'');
        sb.append(", STR_MODEL='").append(STR_MODEL).append('\'');
        sb.append(", ATT_NETPINGER='").append(ATT_NETPINGER).append('\'');
        sb.append(", lastScanMAP=").append(lastScanMAP.size());
        sb.append(", netPingerInst=").append(netPingerInst.hashCode());
        sb.append('}');
        return sb.toString();
    }

}
