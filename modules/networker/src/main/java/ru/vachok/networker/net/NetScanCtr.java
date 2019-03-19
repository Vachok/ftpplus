package ru.vachok.networker.net;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
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
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.*;


/**
 Контроллер netscan.html
 <p>
 
 @since 30.08.2018 (12:55) */
@SuppressWarnings({"ClassWithMultipleLoggers", "SameReturnValue", "DuplicateStringLiteralInspection", "ClassUnconnectedToPackage"})
@Controller
public class NetScanCtr {
    
    
    /**
     {@link LoggerFactory#getLogger(String)}
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(NetScanCtr.class.getSimpleName());
    
    /**
     {@link AppComponents#getOrSetProps()}
     */
    private static final Properties PROPERTIES = AppComponents.getOrSetProps();
    
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
    
    private static final String STR_REQUEST = "request = [";
    
    private static final String STR_MODEL = "], model = [";
    
    private static final String ATT_NETPINGER = "netPinger";
    
    private static final String ATT_PCS = "pcs";
    
    private static final MessageToUser messageToUser = new MessageLocal(NetScanCtr.class.getSimpleName());
    
    private static ThreadPoolTaskExecutor locExecutor = AppComponents.threadConfig().getTaskExecutor();
    
    /**
     {@link AppComponents#lastNetScanMap()}
     */
    private static ConcurrentMap<String, Boolean> lastScanMAP = AppComponents.lastNetScanMap();
    
    private ScanOnline scanOnline;
    
    /**
     {@link AppComponents#netScannerSvc()}
     */
    private NetScannerSvc netScannerSvcInstAW;
    
    private NetPinger netPingerInst;
    
    @SuppressWarnings("WeakerAccess")
    @Autowired
    public NetScanCtr(NetScannerSvc netScannerSvc, NetPinger netPingerInst, ScanOnline scanOnline) {
        this.netScannerSvcInstAW = netScannerSvc;
        this.netPingerInst = netPingerInst;
        this.scanOnline = scanOnline;
    }
    
    @GetMapping("/ping")
    public String pingAddr(Model model, HttpServletRequest request, HttpServletResponse response) {
        model.addAttribute(ATT_NETPINGER, netPingerInst);
        model.addAttribute("pingResult", FileSystemWorker.readFile(ConstantsNet.PINGRESULT_LOG));
        model.addAttribute(ConstantsFor.ATT_TITLE, netPingerInst.getTimeToEndStr() + " pinger hash: " + netPingerInst.hashCode());
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext());
        model.addAttribute("pingTest", new TForms().fromArray(netPingerInst.pingDev(getDeqAddr()), true));
        //noinspection MagicNumber
        response.addHeader(ConstantsFor.HEAD_REFRESH, String.valueOf(ConstantsFor.DELAY * 1.8f));
        messageToUser.info("NetScanCtr.pingAddr", "HEAD_REFRESH", " = " + response.getHeader(ConstantsFor.HEAD_REFRESH));
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
    public String netScan(HttpServletRequest request, HttpServletResponse response, Model model) {
        String classMeth = "NetScanCtr.netScan";
        final long lastSt = Long.parseLong(PROPERTIES.getProperty(ConstantsNet.PR_LASTSCAN, "1548919734742"));
        messageToUser.info(
            STR_REQUEST + request + "], response = [" + response + STR_MODEL + model + "]",
            ConstantsFor.STR_INPUT_PARAMETERS_RETURNS,
            ConstantsFor.JAVA_LANG_STRING_NAME);
    
        AppComponents.threadConfig().thrNameSet("scan");
        ConstantsFor.getVis(request);
        model.addAttribute("serviceinfo", (float) TimeUnit.MILLISECONDS.toSeconds(lastSt - System.currentTimeMillis()) / ConstantsFor.ONE_HOUR_IN_MIN);
        netScannerSvcInstAW.setThePc("");
        model.addAttribute("pc", FileSystemWorker.readFile(ConstantsNet.BEANNAME_LASTNETSCAN));
        model.addAttribute(ConstantsFor.ATT_TITLE, new Date(lastSt));
        model.addAttribute(ConstantsNet.BEANNAME_NETSCANNERSVC, netScannerSvcInstAW);
        model.addAttribute(ATT_THEPC, netScannerSvcInstAW.getThePc());
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext() + "<br>First Scan: 2018-05-05");
        response.addHeader(ConstantsFor.HEAD_REFRESH, "30");
        try {
            checkMapSizeAndDoAction(model, request, lastSt);
        } catch (InterruptedException e) {
            model.addAttribute(ATT_PCS, e.getMessage());
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            model.addAttribute(ATT_PCS, new TForms().fromArray(e, true));
        } catch (TimeoutException e) {
            model.addAttribute(ATT_PCS, "TIMEOUT!<p>" + e.getMessage());
        }
        return ConstantsNet.ATT_NETSCAN;
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
        String thePc = netScannerSvc.getThePc();
        AppComponents.adSrv().setUserInputRaw(thePc);
        if (thePc.toLowerCase().contains("user: ")) {
            model.addAttribute("ok", MoreInfoGetter.getUserFromDB(thePc));
            model.addAttribute(ConstantsFor.ATT_TITLE, thePc);
            model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext());
            return "ok";
        }
        model.addAttribute(ATT_THEPC, thePc);
        AppComponents.adSrv().setUserInputRaw(netScannerSvc.getThePc());
        netScannerSvc.setThePc("");
        return "redirect:/ad?" + thePc;
    }
    
    @GetMapping("/showalldev")
    public String allDevices(Model model, HttpServletRequest request, HttpServletResponse response) {
        model.addAttribute(ConstantsFor.ATT_TITLE, ConstantsNet.getAllDevices().remainingCapacity() + " ip remain");
        try {
            model.addAttribute(ATT_PCS, scanOnline.toString());
        }
        catch (Exception e) {
            FileSystemWorker.error("NetScanCtr.allDevices", e);
        }
        if (request.getQueryString() != null) {
            ConditionChecker.qerNotNullScanAllDevices(model, response);
        }
        model.addAttribute("head", new PageFooter().getHeaderUtext() + "<center><p><a href=\"/showalldev?needsopen\"><h2>Show All IPs in file</h2></a></center>");
        model.addAttribute("ok", DiapazonedScan.getInstance().toString());
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext() + ". Left: " + ConstantsNet.getAllDevices().remainingCapacity() + " " +
            "IPs.");
        return "ok";
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NetScanCtr ctr = (NetScanCtr) o;
        return Objects.equals(scanOnline, ctr.scanOnline) && Objects.equals(netScannerSvcInstAW, ctr.netScannerSvcInstAW) && Objects.equals(netPingerInst, ctr.netPingerInst);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(scanOnline, netScannerSvcInstAW, netPingerInst);
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NetScanCtr{");
        sb.append("ATT_NETSCAN='").append(ConstantsNet.ATT_NETSCAN).append('\'');
        sb.append(", PROPERTIES=").append(PROPERTIES.size());
        sb.append(", DURATION_MIN=").append(DURATION_MIN);
        sb.append(", STR_NETSCAN='").append(STR_NETSCAN).append('\'');
        sb.append(", ATT_THEPC='").append(ATT_THEPC).append('\'');
        sb.append(", NETSCANNERSVC_INST=").append(netScannerSvcInstAW.hashCode());
        sb.append(", STR_REQUEST='").append(STR_REQUEST).append('\'');
        sb.append(", STR_MODEL='").append(STR_MODEL).append('\'');
        sb.append(", ATT_NETPINGER='").append(ATT_NETPINGER).append('\'');
        sb.append(", lastScanMAP=").append(lastScanMAP.size());
        sb.append(", netPingerInst=").append(netPingerInst.hashCode());
        sb.append('}');
        return sb.toString();
    }
    
    /**
     ИП-адреса, которые проверяются в момент входа на <a href="http://rups00.eatmeat.ru:8880/ping" target=_blank>http://rups00.eatmeat.ru:8880/ping</a>
     
     @return {@link Deque} {@link InetAddress}
     */
    private static Deque<InetAddress> getDeqAddr() {
        Deque<InetAddress> retDeq = new ConcurrentLinkedDeque<>();
        Field[] fields = OtherKnownDevices.class.getFields();
        try {
            for (Field field : fields) {
                if (field.getName().contains("IP")) {
                    byte[] inetAddressBytes = InetAddress.getByName(field.get(field).toString()).getAddress();
                    retDeq.add(InetAddress.getByAddress(inetAddressBytes));
                } else {
                    retDeq.add(InetAddress.getByName(field.get(field).toString()));
                }
            }
        } catch (IOException | IllegalAccessException e) {
            messageToUser.errorAlert("NetScanCtr", "getDeqAddr", e.getMessage());
            FileSystemWorker.error("NetScanCtr.getDeqAddr", e);
        }
        return retDeq;
    }
    
    /**
     Если {@link #lastScanMAP} более 1
     <p>
     1. {@link TForms#fromArray(java.util.Map, boolean)} добавим в {@link Model} содержимое {@link #lastScanMAP} <br> 2.
     {@link NetScannerSvc#getOnLinePCs()} - в заголовке страницы, при обновлении,
     отображение остатка ПК. <br> 3. {@link TForms#fromArray(java.util.Map, boolean)} запишем файл {@link ConstantsNet#BEANNAME_LASTNETSCAN}, 4.
     {@link FileSystemWorker#writeFile(java.lang.String,
         java.lang.String)} <br> 5. {@link #timeCheck(int, long, HttpServletRequest, Model)} переходим в проверке времени.
     <p>
 
     @param model     {@link Model}
     @param request   {@link HttpServletRequest}
     @param lastSt    время последнего скана. Берется из {@link #PROPERTIES}. Default: {@code 1548919734742}.
     @param thisTotpc кол-во ПК для скана. Берется из {@link #PROPERTIES}. Default: {@code 243}.
     @throws ExecutionException    timeCheck
     @throws InterruptedException timeCheck
     @throws TimeoutException     timeCheck
     @see #checkMapSizeAndDoAction(Model, HttpServletRequest, long)
     */
    private void mapSizeBigger(Model model, HttpServletRequest request, long lastSt, int thisTotpc) throws ExecutionException, InterruptedException, TimeoutException {
        long timeLeft = TimeUnit.MILLISECONDS.toSeconds(lastSt - System.currentTimeMillis());
        final int pcWas = Integer.parseInt(PROPERTIES.getProperty(ConstantsNet.ONLINEPC, "0"));
        int remainPC = thisTotpc - lastScanMAP.size();
        boolean newPSs = 0 > remainPC;
    
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(timeLeft);
        stringBuilder.append(" seconds (");
        stringBuilder.append((float) timeLeft / ConstantsFor.ONE_HOUR_IN_MIN);
        stringBuilder.append(" min) left<br>Delay period is ");
        stringBuilder.append(DURATION_MIN);
    
        String msg = stringBuilder.toString();
        LOGGER.info(msg);
    
        StringBuilder titleBuilder = new StringBuilder();
        titleBuilder.append(remainPC);
        titleBuilder.append("/");
        titleBuilder.append(thisTotpc);
        titleBuilder.append(" PCs (");
        titleBuilder.append(NetScannerSvc.getOnLinePCs());
        titleBuilder.append("/");
        titleBuilder.append(pcWas);
        titleBuilder.append(" at ");
        titleBuilder.append(LocalDateTime.ofEpochSecond(lastSt / 1000, 0, ZoneOffset.ofHours(3)).toLocalTime());
    
        model
            .addAttribute("left", msg)
            .addAttribute("pc", new TForms().fromArray(lastScanMAP, false))
            .addAttribute(ConstantsFor.ATT_TITLE, titleBuilder.toString());
        if (newPSs) {
            FileSystemWorker.writeFile(ConstantsNet.BEANNAME_LASTNETSCAN, new TForms().fromArray(lastScanMAP, false));
            model.addAttribute("newpc", "Добавлены компы! " + Math.abs(remainPC) + " шт.");
            PROPERTIES.setProperty(ConstantsFor.PR_TOTPC, String.valueOf(lastScanMAP.size()));
        } else {
            if (3 > remainPC) {
                PROPERTIES.setProperty(ConstantsFor.PR_TOTPC, String.valueOf(lastScanMAP.size()));
                messageToUser.info("NetScanCtr.mapSizeBigger", "setProps", " = " + PROPERTIES.size());
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
     @throws ExecutionException   submitScan
     @throws InterruptedException submitScan
     @throws TimeoutException     submitScan
     @see #mapSizeBigger(Model, HttpServletRequest, long, int)
     */
    private void timeCheck(int remainPC, long lastScanEpoch, HttpServletRequest request, Model model) throws ExecutionException, InterruptedException, TimeoutException {
        final Runnable scanRun = () -> scanIt(request, model, new Date(lastScanEpoch * 1000));
        LocalTime lastScanLocalTime = LocalDateTime.ofEpochSecond(lastScanEpoch, 0, ZoneOffset.ofHours(3)).toLocalTime();
        String classMeth = "NetScanCtr.timeCheck";
        boolean isSystemTimeBigger = (System.currentTimeMillis() > lastScanEpoch * 1000) && remainPC <= 0;
    
        if (isSystemTimeBigger) {
            String valStr = "isSystemTimeBigger = " + true;
            messageToUser.info(valStr);
            Future<?> submitScan = locExecutor.submit(scanRun);
            submitScan.get(ConstantsFor.DELAY - 1, TimeUnit.MINUTES);
            messageToUser.info("NetScanCtr.checkMapSizeAndDoAction", "submitScan.isDone()", " = " + submitScan.isDone());
        } else {
            String valStr = "lastScanLocalTime = " + lastScanLocalTime;
            messageToUser.infoNoTitles(Thread.currentThread().getName() + "\n" + classMeth + "\n" + valStr);
        }
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
     @throws ExecutionException   mapSizeBigger, submitScan
     @throws InterruptedException mapSizeBigger, submitScan
     @throws TimeoutException     mapSizeBigger, submitScan
     @see #netScan(HttpServletRequest, HttpServletResponse, Model)
     */
    private void checkMapSizeAndDoAction(Model model, HttpServletRequest request, long lastSt) throws ExecutionException, InterruptedException, TimeoutException {
        final Runnable scanRun = () -> scanIt(request, model, new Date(lastSt));
        boolean isMapSizeBigger = lastScanMAP.size() > 0;
        final int thisTotpc = Integer.parseInt(PROPERTIES.getProperty(ConstantsFor.PR_TOTPC, "243"));
        File scanTemp = new File("scan.tmp");
        if (scanTemp.isFile() && scanTemp.exists()) {
            mapSizeBigger(model, request, lastSt, thisTotpc);
        } else {
            if (isMapSizeBigger) {
                mapSizeBigger(model, request, lastSt, thisTotpc);
            } else {
                Future<?> submitScan = locExecutor.submit(scanRun);
                submitScan.get(ConstantsFor.DELAY - 1, TimeUnit.MINUTES);
                messageToUser.info("NetScanCtr.checkMapSizeAndDoAction", "submitScan.isDone()", " = " + submitScan.isDone());
            }
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
    @Async
    private void scanIt(HttpServletRequest request, Model model, Date lastScanDate) {
        if (request != null && request.getQueryString() != null) {
            lastScanMAP.clear();
            Set<String> pcNames = netScannerSvcInstAW.getPCNamesPref(request.getQueryString());
            model.addAttribute(ConstantsFor.ATT_TITLE, new Date().toString())
                .addAttribute("pc", new TForms().fromArray(pcNames, true));
        } else {
            lastScanMAP.clear();
            Set<String> pCsAsync = netScannerSvcInstAW.getPcNames();
            model.addAttribute(ConstantsFor.ATT_TITLE, lastScanDate)
                .addAttribute("pc", new TForms().fromArray(pCsAsync, true));
            AppComponents.lastNetScan().setTimeLastScan(new Date());
        }
    }
    
}
