package ru.vachok.networker.net;


import org.slf4j.Logger;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.*;


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
    private static final String ATT_THEPC = "thePc";

    /**
     {@link NetScannerSvc#getI()}
     */
    private static final NetScannerSvc NET_SCANNER_SVC = NetScannerSvc.getI();

    private static final String STR_REQUEST = "request = [";

    private static final String STR_MODEL = "], model = [";

    private static final String ATT_NET_PINGER = "netPinger";

    /**
     {@link AppComponents#lastNetScanMap()}
     */
    private static ConcurrentMap<String, Boolean> lastScanMAP = AppComponents.lastNetScanMap();

    private NetPinger netPinger = new NetPinger();

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
        model.addAttribute(ATT_THEPC, thePc);
        AppComponents.adSrv().setUserInputRaw(netScannerSvc.getThePc());
        netScannerSvc.setThePc("");
        return "redirect:/ad?" + thePc;
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
     @param request {@link HttpServletRequest} для {@link ConstantsFor#getVis(HttpServletRequest)}
     @param response {@link HttpServletResponse} добавить {@link ConstantsFor#HEAD_REFRESH} 30 сек
     @param model {@link Model}
     @return {@link NetScanCtr#AT_NAME_NETSCAN} (netscan.html)
     */
    @GetMapping (STR_NETSCAN)
    public String netScan(HttpServletRequest request, HttpServletResponse response, Model model) {
        String classMeth = "NetScanCtr.netScan";
        long lastSt = Long.parseLong(properties.getProperty(ConstantsNet.PR_LASTSCAN, "1548919734742"));
        new MessageCons().errorAlert(classMeth);
        new MessageCons().info(
            STR_REQUEST + request + "], response = [" + response + STR_MODEL + model + "]",
            ConstantsFor.STR_INPUT_PARAMETERS_RETURNS,
            ConstantsFor.JAVA_LANG_STRING_NAME);

        Thread.currentThread().setName(classMeth);
        ConstantsFor.getVis(request);
        model.addAttribute("serviceinfo", ( float ) TimeUnit.MILLISECONDS.toSeconds(lastSt - System.currentTimeMillis()) / ConstantsFor.ONE_HOUR_IN_MIN);
        NET_SCANNER_SVC.setThePc("");
        model.addAttribute("pc", FileSystemWorker.readFile("lastnetscan.log"));
        model.addAttribute(ConstantsFor.ATT_TITLE, new Date(lastSt));
        model.addAttribute(ConstantsNet.STR_NETSCANNERSVC, NET_SCANNER_SVC).addAttribute(ATT_THEPC, NET_SCANNER_SVC.getThePc());
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext() + "<br>First Scan: 2018-05-05");
        response.addHeader(ConstantsFor.HEAD_REFRESH, "30");
        checkMapSizeAndDoAction(model, request, lastSt);
        return AT_NAME_NETSCAN;
    }

    /**
     Начало проверок перед сканом.
     <p>
     1. {@link AppComponents#threadConfig()}, 2. {@link ThreadConfig#threadPoolTaskExecutor()}. Получаем сконфигурированный {@link ThreadPoolTaskExecutor}
     <p>
     <b>Runnable:</b>
     3. {@link #mapSizeBigger(Model, HttpServletRequest, long, int)} когда {@link #lastScanMAP} больше 1. <br> или <br> 4.
     {@link #scanIt(HttpServletRequest, Model, Date)}.
     <p>
     Объявим дополнительно lock {@link File} {@code scan.tmp}. Чтобы исключить запуск, если предидущий скан не закончен. <br> Проверяем его наличие.
     Если он существует - запускаем {@link
    #mapSizeBigger(Model, HttpServletRequest, long, int)}, иначе отправляем <b>Runnable</b> в {@link ThreadConfig#threadPoolTaskExecutor()} (1)
     <p>

     @param model   {@link Model}
     @param request {@link HttpServletRequest}
     @param lastSt  timestamp из {@link #properties}
     @see #netScan(HttpServletRequest, HttpServletResponse, Model)
     */
    private void checkMapSizeAndDoAction(Model model, HttpServletRequest request, long lastSt) {
        boolean isMapSizeBigger = lastScanMAP.size() > 1;
        int thisTotpc = Integer.parseInt(properties.getProperty(ConstantsFor.PR_TOTPC, "318"));
        ThreadPoolTaskExecutor taskExecutor = AppComponents.threadConfig().threadPoolTaskExecutor();
        final Runnable runnableScan = () -> {
            if(isMapSizeBigger){
                mapSizeBigger(model, request, lastSt, thisTotpc);
            }
            else{
                scanIt(request, model, new Date(lastSt));
            }
        };
        BlockingQueue<Runnable> queue = taskExecutor.getThreadPoolExecutor().getQueue();
        String methName = "NetScanCtr.checkMapSizeAndDoAction";
        File f = new File("scan.tmp");
        if(f.isFile() && f.exists()){
            mapSizeBigger(model, request, lastSt, thisTotpc);
        }
        else{
            taskExecutor.submit(runnableScan);
            new MessageCons().infoNoTitles("queue = " + queue.remainingCapacity());
        }
        new MessageCons().errorAlert(methName);
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
     @param lastSt    время последнего скана. Берется из {@link #properties}. Default: {@code 1548919734742}.
     @param thisTotpc кол-во ПК для скана. Берется из {@link #properties}. Default: {@code 318}.
     @see #checkMapSizeAndDoAction(Model, HttpServletRequest, long)
     */
    private void mapSizeBigger(Model model, HttpServletRequest request, long lastSt, int thisTotpc) {
        new MessageCons().errorAlert("NetScanCtr.mapSizeBigger");
        long timeLeft = TimeUnit.MILLISECONDS.toSeconds(lastSt - System.currentTimeMillis());
        int pcWas = Integer.parseInt(properties.getProperty(ConstantsNet.ONLINEPC, "0"));
        int remainPC = thisTotpc - lastScanMAP.size();
        boolean newPSs = 0 > remainPC;
        String msg = new StringBuilder()
            .append(timeLeft).append(" seconds (")
            .append(( float ) timeLeft / ConstantsFor.ONE_HOUR_IN_MIN).append(" min) left<br>Delay period is ")
            .append(DURATION).toString();
        LOGGER.warn(msg);
        model
            .addAttribute("left", msg)
            .addAttribute("pc", new TForms().fromArray(lastScanMAP, false))
            .addAttribute(ConstantsFor.ATT_TITLE, new StringBuilder()
                .append(remainPC).append("/")
                .append(thisTotpc).append(" PCs (")
                .append(NET_SCANNER_SVC.getOnLinePCs()).append("/")
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
    private void scanIt(HttpServletRequest request, Model model, Date lastScanDate) {
        String propMsg = "NetScanCtr.scanIt. " + lastScanDate;
        properties.setProperty("propmsg", propMsg);
        if(request!=null && request.getQueryString()!=null){
            lastScanMAP.clear();
            Set<String> pcNames = NET_SCANNER_SVC.getPCNamesPref(request.getQueryString());
            model.addAttribute(ConstantsFor.ATT_TITLE, new Date().toString())
                .addAttribute("pc", new TForms().fromArray(pcNames, true));
        }
        else{
            lastScanMAP.clear();
            Set<String> pCsAsync = NET_SCANNER_SVC.getPcNames();
            model.addAttribute(ConstantsFor.ATT_TITLE, lastScanDate)
                .addAttribute("pc", new TForms().fromArray(pCsAsync, true));
            AppComponents.lastNetScan().setTimeLastScan(new Date());
        }

        new MessageCons().info(ConstantsFor.STR_INPUT_OUTPUT,
            STR_REQUEST + request + STR_MODEL + model + "], lastScanDate = [" + lastScanDate + "]", "void");
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
    private void timeCheck(int remainPC, long lastScanEpoch, HttpServletRequest request, Model model) {
        String classMeth = " NetScanCtr.timeCheck";
        LocalTime lastScanLocalTime = LocalDateTime.ofEpochSecond(lastScanEpoch, 0, ZoneOffset.ofHours(3)).toLocalTime();
        boolean isSystemTimeBigger = (System.currentTimeMillis() > lastScanEpoch * 1000) && remainPC <= 0;
        if(isSystemTimeBigger){
            String valStr = "isSystemTimeBigger = " + true;
            new MessageCons().info(Thread.currentThread().getName(), classMeth, valStr);
            scanIt(request, model, new Date(lastScanEpoch * 1000));
        }
        else{
            String valStr = "lastScanLocalTime = " + lastScanLocalTime;
            new MessageCons().infoNoTitles(Thread.currentThread().getName() + "\n" + classMeth + "\n" + valStr);
        }
    }

    @GetMapping ("/showalldev")
    public String allDevices(Model model, HttpServletRequest request, HttpServletResponse response) {
        LOGGER.warn("NetScanCtr.allDevices");
        model.addAttribute(ConstantsFor.ATT_TITLE, "DiapazonedScan.scanAll");
        model.addAttribute("pcs", ScanOnline.getI().toString());
        if(request.getQueryString()!=null){
            ConditionChecker.qerNotNullScanAllDevices(model, response);
        }
        model.addAttribute("head", new PageFooter().getHeaderUtext() + "<center><p><a href=\"/showalldev?needsopen\"><h2>Show IPs</h2></a></center>");
        model.addAttribute("ok", DiapazonedScan.getInstance().toString());
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext() + ". Left: " + ConstantsFor.ALL_DEVICES.remainingCapacity() + " " +
            "IPs.");
        return "ok";
    }

    @GetMapping ("/ping")
    public String pingAddr(Model model, HttpServletRequest request, HttpServletResponse response) {
        model.addAttribute(ATT_NET_PINGER, netPinger);
        model.addAttribute("pingResult", FileSystemWorker.readFile(ConstantsNet.PINGRESULT_LOG));
        return "ping";
    }

    @PostMapping ("/ping")
    public String pingPost(Model model, HttpServletRequest request, @ModelAttribute NetPinger netPinger) throws ExecutionException, InterruptedException {
        this.netPinger = netPinger;
        netPinger.run();
        model.addAttribute(ATT_NET_PINGER, netPinger);
        model.addAttribute("ok", FileSystemWorker.readFile(ConstantsNet.PINGRESULT_LOG));
        return "ok";
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NetScanCtr{");
        sb.append("AT_NAME_NETSCAN='").append(AT_NAME_NETSCAN).append('\'');
        sb.append(", ATT_THEPC='").append(ATT_THEPC).append('\'');
        sb.append(", DURATION=").append(DURATION);
        sb.append(", lastScanMAP=").append(lastScanMAP.size());
        sb.append(", properties=").append(properties);
        sb.append(", STR_NETSCAN='").append(STR_NETSCAN).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
