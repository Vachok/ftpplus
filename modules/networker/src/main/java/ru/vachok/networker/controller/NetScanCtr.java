// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.controller;


import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.FileItemFactory;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.abstr.monitors.NetMonitor;
import ru.vachok.networker.abstr.monitors.PingerService;
import ru.vachok.networker.ad.user.MoreInfoWorker;
import ru.vachok.networker.componentsrepo.LastNetScan;
import ru.vachok.networker.componentsrepo.PageFooter;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.componentsrepo.exceptions.ScanFilesException;
import ru.vachok.networker.exe.ThreadConfig;
import ru.vachok.networker.exe.runnabletasks.NetScannerSvc;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.NetPingerService;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.net.scanner.NetListKeeper;
import ru.vachok.networker.net.scanner.ScanOnline;
import ru.vachok.networker.restapi.message.MessageLocal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.*;
import java.util.concurrent.*;

import static ru.vachok.networker.ConstantsFor.STR_P;


/**
 Контроллер netscan.html
 <p>
 
 @see ru.vachok.networker.controller.NetScanCtrTest
 @since 30.08.2018 (12:55) */
@SuppressWarnings({"ClassWithMultipleLoggers", "SameReturnValue", "DuplicateStringLiteralInspection", "ClassUnconnectedToPackage"})
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
    
    private static final String STR_REQUEST = "request = [";
    
    private static final String STR_MODEL = "], model = [";
    
    private static final String ATT_PCS = "pcs";
    
    private static final ThreadPoolTaskExecutor THREAD_POOL_TASK_EXECUTOR_LOCAL = AppComponents.threadConfig().getTaskExecutor();
    
    private final MessageToUser messageToUser = new MessageLocal(NetScanCtr.class.getSimpleName());
    
    private final File scanTemp = new File("scan.tmp");
    
    private final ConcurrentNavigableMap<String, Boolean> lastScanMAP = LastNetScan.getLastNetScan().getNetWork();
    
    private NetMonitor scanOnline;
    
    /**
     {@link AppComponents#netScannerSvc()}
     */
    private NetScannerSvc netScannerSvcInstAW;
    
    private PingerService netPingerInst;
    
    @Autowired
    public NetScanCtr(NetScannerSvc netScannerSvc, NetPingerService netPingerInst, ScanOnline scanOnline) {
        this.netScannerSvcInstAW = netScannerSvc;
        this.netPingerInst = netPingerInst;
        this.scanOnline = new ScanOnline();
        messageToUser.info(getClass().getSimpleName() + ".pingDevices", "AppComponents.ipFlushDNS()", " = " + AppComponents.ipFlushDNS());
    }
    
    /**
     GET /{@link #STR_NETSCAN} Старт сканера локальных ПК
     <p>
     1. {@link ConstantsFor#getVis(javax.servlet.http.HttpServletRequest)}. Запись {@link Visitor } <br>
     2.{@link NetScannerSvc#setThePc(java.lang.String)} обнуляем строку в форме. <br>
     3. {@link FileSystemWorker#readFile(java.lang.String)} добавляем файл {@code lastnetscan.log} в качестве аттрибута {@code pc} в {@link Model} <br>
     4. {@link NetScannerSvc#getThePc()} аттрибут {@link Model} - {@link #ATT_THEPC}. <br>
     7. {@link #checkMapSizeAndDoAction(Model, HttpServletRequest, long)} - начинаем проверку.
     <p>
     
     @param request {@link HttpServletRequest} для {@link ConstantsFor#getVis(HttpServletRequest)}
     @param response {@link HttpServletResponse} добавить {@link ConstantsFor#HEAD_REFRESH} 30 сек
     @param model {@link Model}
     @return {@link ConstantsNet#ATT_NETSCAN} (netscan.html)
     */
    @GetMapping(STR_NETSCAN)
    public String netScan(HttpServletRequest request, HttpServletResponse response, Model model) {
        final long lastSt = Long.parseLong(PROPERTIES.getProperty(ConstantsNet.PR_LASTSCAN, "1548919734742"));
        AppComponents.threadConfig().thrNameSet("scan");
        
        ConstantsFor.getVis(request);
        model.addAttribute("serviceinfo", (float) TimeUnit.MILLISECONDS.toSeconds(lastSt - System.currentTimeMillis()) / ConstantsFor.ONE_HOUR_IN_MIN);
        netScannerSvcInstAW.setThePc("");
        model.addAttribute("pc", FileSystemWorker.readFile(ConstantsNet.BEANNAME_LASTNETSCAN) + "<p>");
        model.addAttribute(ConstantsFor.ATT_TITLE, netScannerSvcInstAW.getOnLinePCsNum() + " pc at " + new Date(lastSt));
        model.addAttribute(ConstantsNet.BEANNAME_NETSCANNERSVC, netScannerSvcInstAW);
        model.addAttribute(ATT_THEPC, netScannerSvcInstAW.getThePc());
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext() + "<br>First Scan: 2018-05-05");
        response.addHeader(ConstantsFor.HEAD_REFRESH, "30");
    
        try {
            checkMapSizeAndDoAction(model, request, lastSt);
        }
        catch (InterruptedException e) {
            model.addAttribute(ATT_PCS, e.getMessage());
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        catch (ExecutionException | IOException e) {
            model.addAttribute(ATT_PCS, new TForms().fromArray(e, true));
        }
        catch (TimeoutException e) {
            model.addAttribute(ATT_PCS, "TIMEOUT!<p>" + e.getMessage());
        }
        return ConstantsNet.ATT_NETSCAN;
    }
    
    @GetMapping("/ping")
    public String pingAddr(@NotNull Model model, HttpServletRequest request, @NotNull HttpServletResponse response) {
        ((NetPingerService) netPingerInst)
            .setTimeForScanStr(String.valueOf(TimeUnit.SECONDS.toMinutes(Math.abs(LocalTime.now().toSecondOfDay() - LocalTime.parse("08:30").toSecondOfDay()))));
        model.addAttribute(ConstantsFor.ATT_NETPINGER, netPingerInst);
        model.addAttribute("pingResult", FileSystemWorker.readFile(ConstantsNet.PINGRESULT_LOG));
        model.addAttribute(ConstantsFor.ATT_TITLE, netPingerInst.getExecution() + " pinger hash: " + netPingerInst.hashCode());
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext());
        List<String> pingedDevices = netPingerInst.pingDevices(NetListKeeper.getMapAddr());
        String valuePingTest = new TForms().fromArray(pingedDevices, true);
        model.addAttribute("pingTest", valuePingTest);
        //noinspection MagicNumber
        response.addHeader(ConstantsFor.HEAD_REFRESH, String.valueOf(ConstantsFor.DELAY * 1.8f));
        messageToUser.info("NetScanCtr.pingAddr", "HEAD_REFRESH", " = " + response.getHeader(ConstantsFor.HEAD_REFRESH));
        return "ping";
    }
    
    @PostMapping("/ping")
    public String pingPost(Model model, HttpServletRequest request, @NotNull @ModelAttribute NetPingerService netPinger, HttpServletResponse response) {
        this.netPingerInst = netPinger;
        try {
            netPinger.run();
        }
        catch (IllegalComponentStateException e) {
            System.err.println(e.getMessage() + " " + getClass().getSimpleName() + ".pingPost");
        }
        catch (ScanFilesException e) {
            String multipartFileResource = getClass().getResource("/static/ping2ping.txt").getFile();
            FileItemFactory factory = new DiskFileItemFactory();
            FileItem fileItem = factory
                .createItem("multipartFile", "text/plain", true, multipartFileResource);
            messageToUser.warn(e.getMessage(), multipartFileResource, new TForms().fromArray(e));
        }
        model.addAttribute(ConstantsFor.ATT_NETPINGER, netPinger);
        String npEq = "Netpinger equals is " + netPinger.equals(this.netPingerInst);
        model.addAttribute(ConstantsFor.ATT_TITLE, npEq);
        model.addAttribute("ok", FileSystemWorker.readFile(ConstantsNet.PINGRESULT_LOG));
        messageToUser.infoNoTitles("npEq = " + npEq);
        response.addHeader(ConstantsFor.HEAD_REFRESH, PROPERTIES.getProperty(ConstantsFor.PR_PINGSLEEP, "60"));
        return "ok";
    }
    
    /**
     POST /netscan
     <p>
     
     @param netScannerSvc {@link NetScannerSvc}
     @param model {@link Model}
     @return redirect:/ad? + {@link NetScannerSvc#getThePc()}
     */
    @PostMapping(STR_NETSCAN)
    public static @NotNull String pcNameForInfo(@NotNull @ModelAttribute NetScannerSvc netScannerSvc, Model model) {
        String thePc = netScannerSvc.getThePc();
        if (thePc.toLowerCase().contains("user: ")) {
            model.addAttribute("ok", MoreInfoWorker.getUserFromDB(thePc).trim());
            model.addAttribute(ConstantsFor.ATT_TITLE, thePc);
            model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext());
            return "ok";
        }
        model.addAttribute(ATT_THEPC, thePc);
        netScannerSvc.setThePc("");
        return "redirect:/ad?" + thePc;
    }
    
    @GetMapping("/showalldev")
    public String allDevices(@NotNull Model model, HttpServletRequest request, HttpServletResponse response) {
        model.addAttribute(ConstantsFor.ATT_TITLE, ConstantsNet.getAllDevices().remainingCapacity() + " ip remain");
        try {
            model.addAttribute(ATT_PCS, scanOnline.toString());
        }
        catch (Exception e) {
            messageToUser.error(e.getMessage());
        }
        if (request.getQueryString() != null) {
            qerNotNullScanAllDevices(model, response);
        }
        model.addAttribute("head", new PageFooter().getHeaderUtext() + "<center><p><a href=\"/showalldev?needsopen\"><h2>Show All IPs in file</h2></a></center>");
        model.addAttribute("ok", AppComponents.diapazonedScanInfo());
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext() + ". Left: " + ConstantsNet.getAllDevices().remainingCapacity() + " " +
            "IPs.");
        return "ok";
    }
    
    public void scanIt() {
        throw new IllegalComponentStateException("14.05.2019 (17:45)\n" + getClass().getSimpleName() + ".scanIT()");
        
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
        sb.append(", ATT_NETPINGER='").append(ConstantsFor.ATT_NETPINGER).append('\'');
        sb.append(", lastScanMAP=").append(lastScanMAP.size());
        sb.append(", Bean information=").append(getInformationForThreads(getTHRBeanMX()));
        sb.append('}');
        return sb.toString();
    }
    
    private void mapSizeBigger(@NotNull Model model, HttpServletRequest request, long lastSt, int thisTotpc) throws ExecutionException, InterruptedException, TimeoutException, IOException {
        long timeLeft = TimeUnit.MILLISECONDS.toSeconds(lastSt - System.currentTimeMillis());
        int pcWas = Integer.parseInt(PROPERTIES.getProperty(ConstantsFor.PR_ONLINEPC, "0"));
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
        titleBuilder.append(netScannerSvcInstAW.getOnLinePCsNum());
        titleBuilder.append("/");
        titleBuilder.append(pcWas);
        titleBuilder.append(") Next run ");
        titleBuilder.append(LocalDateTime.ofEpochSecond(lastSt / 1000, 0, ZoneOffset.ofHours(3)).toLocalTime());
    
        String pcValue = fromArray(lastScanMAP);
        
        model
            .addAttribute("left", msg)
            .addAttribute("pc", pcValue)
            .addAttribute(ConstantsFor.ATT_TITLE, titleBuilder.toString());
        if (newPSs) {
            FileSystemWorker.writeFile(ConstantsNet.BEANNAME_LASTNETSCAN, pcValue);
            model.addAttribute(ConstantsFor.PR_AND_ATT_NEWPC, "Добавлены компы! " + Math.abs(remainPC) + " шт.");
            PROPERTIES.setProperty(ConstantsFor.PR_TOTPC, String.valueOf(lastScanMAP.size()));
            PROPERTIES.setProperty(ConstantsFor.PR_AND_ATT_NEWPC, String.valueOf(remainPC));
        }
        else {
            if (ConstantsFor.INT_ANSWER > remainPC) {
                PROPERTIES.setProperty(ConstantsFor.PR_TOTPC, String.valueOf(lastScanMAP.size()));
            }
        }
        timeCheck(remainPC, lastSt / 1000, request, model);
    }
    
    private @NotNull String fromArray(@NotNull ConcurrentMap<String, Boolean> map) {
        StringBuilder brStringBuilder = new StringBuilder();
        brStringBuilder.append(STR_P);
        Set<?> keySet = map.keySet();
        List<String> list = new ArrayList<>(keySet.size());
        keySet.forEach(x->list.add(x.toString()));
        Collections.sort(list);
        for (String keyMap : list) {
            String valueMap = map.get(keyMap).toString();
            brStringBuilder.append(keyMap).append(" ").append(valueMap).append("<br>");
        }
        return brStringBuilder.toString();
        
    }
    
    /**
     Проверки времени
     <p>
     Если {@link System#currentTimeMillis()} больше {@code lastScanEpoch}*1000 ии {@code remainPC} меньше либо равно 0, запустить
     {@link #scanIt(HttpServletRequest, Model, Date)}. <br> Иначе выдать
     сообщение в консоль, с временем след. запуска.
     <p>
     
     @param remainPC осталось ПК
     @param lastScanEpoch последнее сканирование Timestamp как <b>EPOCH Seconds</b>
     @param request {@link HttpServletRequest}
     @param model {@link Model}
     @throws ExecutionException submitScan
     @throws InterruptedException submitScan
     @throws TimeoutException submitScan
     @see #mapSizeBigger(Model, HttpServletRequest, long, int)
     */
    private void timeCheck(int remainPC, long lastScanEpoch, HttpServletRequest request, Model model) throws ExecutionException, InterruptedException, TimeoutException {
        Runnable scanRun = ()->{
            ThreadMXBean threadMXBean = getTHRBeanMX();
            String threadsInfoInit = getInformationForThreads(threadMXBean);
            messageToUser.warn(getClass().getSimpleName(), ".scanIt", " = " + threadsInfoInit);
            scanIt(request, model, new Date(lastScanEpoch * 1000));
            netScannerSvcInstAW.setMemoryInfo(getInformationForThreads(threadMXBean));
        };
        LocalTime lastScanLocalTime = LocalDateTime.ofEpochSecond(lastScanEpoch, 0, ZoneOffset.ofHours(3)).toLocalTime();
        boolean isSystemTimeBigger = (System.currentTimeMillis() > lastScanEpoch * 1000);
    
        if (!(scanTemp.exists())) {
            model.addAttribute(ConstantsFor.PR_AND_ATT_NEWPC, lastScanLocalTime);
            if (isSystemTimeBigger) {
                Future<?> submitScan = THREAD_POOL_TASK_EXECUTOR_LOCAL.submit(scanRun);
                submitScan.get(ConstantsFor.DELAY - 1, TimeUnit.MINUTES);
                messageToUser.info("NetScanCtr.checkMapSizeAndDoAction", "submitScan.isDone()", " = " + submitScan.isDone());
            }
        }
        else {
            messageToUser.warn(getClass().getSimpleName() + ".timeCheck", "lastScanLocalTime", " = " + lastScanLocalTime);
        }
        
    }
    
    private ThreadMXBean getTHRBeanMX() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        threadMXBean.setThreadContentionMonitoringEnabled(true);
        threadMXBean.setThreadCpuTimeEnabled(true);
        messageToUser.warn("PEAK THREADS: " + threadMXBean.getPeakThreadCount());
        threadMXBean.resetPeakThreadCount();
        return threadMXBean;
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
     
     @param model {@link Model}
     @param request {@link HttpServletRequest}
     @param lastSt timestamp из {@link #PROPERTIES}
     @throws ExecutionException mapSizeBigger, submitScan
     @throws InterruptedException mapSizeBigger, submitScan
     @throws TimeoutException mapSizeBigger, submitScan
     @see #netScan(HttpServletRequest, HttpServletResponse, Model)
     */
    private void checkMapSizeAndDoAction(Model model, HttpServletRequest request, long lastSt) throws ExecutionException, InterruptedException, TimeoutException, IOException {
        Runnable scanRun = ()->{
            ThreadMXBean threadMXBean = getTHRBeanMX();
            String threadsInfoInit = getInformationForThreads(threadMXBean);
            messageToUser.warn(getClass().getSimpleName(), ".scanIt", " = " + threadsInfoInit);
            scanIt(request, model, new Date(lastSt));
            netScannerSvcInstAW.setMemoryInfo(getInformationForThreads(threadMXBean));
        };
        int thisTotpc = Integer.parseInt(PROPERTIES.getProperty(ConstantsFor.PR_TOTPC, "259"));
        
        if ((scanTemp.isFile() && scanTemp.exists())) {
            mapSizeBigger(model, request, lastSt, thisTotpc);
        }
        else {
            timeCheck(thisTotpc - lastScanMAP.size(), lastSt / 1000, request, model);
        }
        
    }
    
    /**
     Запуск скана.
     <p>
     Проверяем {@link HttpServletRequest} на наличие {@link HttpServletRequest#getQueryString()}. Если есть, сбрасываем {@link #lastScanMAP}, и
     запускаем {@link
    NetScannerSvc#theSETOfPCNamesPref(java.lang.String)}, где параметр это наша {@link HttpServletRequest#getQueryString()}. <br> В {@link Model}, добавим
     аттрибуты {@code title, pc}. new {@link Date} и
     {@link Set} pcNames, полученный из {@link NetScannerSvc#theSETOfPCNamesPref(java.lang.String)}
     <p>
     Иначе: <br> Очищаем {@link #lastScanMAP} <br> Запускаем {@link NetScannerSvc#theSETOfPcNames()} <br> В {@link Model} добавим {@code lastScanDate} как
     {@code title}, и {@link Set} {@link
    NetScannerSvc#theSETOfPcNames()}.@param request      {@link HttpServletRequest}
     
     @param model {@link Model}
     @param lastScanDate дата последнего скана
     */
    @Async
    private void scanIt(HttpServletRequest request, Model model, Date lastScanDate) {
        if (request != null && request.getQueryString() != null) {
            lastScanMAP.clear();
            netScannerSvcInstAW.setOnLinePCsNum(0);
            Set<String> pcNames = netScannerSvcInstAW.theSETOfPCNamesPref(request.getQueryString());
            model
                .addAttribute(ConstantsFor.ATT_TITLE, new Date().toString())
                .addAttribute("pc", new TForms().fromArray(pcNames, true));
        }
        else {
            lastScanMAP.clear();
            netScannerSvcInstAW.setOnLinePCsNum(0);
            Set<String> pCsAsync = netScannerSvcInstAW.theSETOfPcNames();
            model.addAttribute(ConstantsFor.ATT_TITLE, lastScanDate)
                .addAttribute("pc", new TForms().fromArray(pCsAsync, true));
            
            LastNetScan.getLastNetScan().setTimeLastScan(new Date());
        }
    }
    
    private @NotNull String getInformationForThreads(@NotNull ThreadMXBean threadMXBean) {
        long cpuTimeMS = TimeUnit.NANOSECONDS.toMillis(threadMXBean.getCurrentThreadCpuTime());
        long userTimeMS = TimeUnit.NANOSECONDS.toMillis(threadMXBean.getCurrentThreadUserTime());
        return cpuTimeMS + " ms cpu time. " +
            userTimeMS + " user ms time. " +
            threadMXBean.getThreadCount() + " thr running, " +
            threadMXBean.getPeakThreadCount() + " peak threads." +
            threadMXBean.getTotalStartedThreadCount() + " total threads started. (" +
            threadMXBean.getDaemonThreadCount() + " daemons)";
    }
    
    private void qerNotNullScanAllDevices(Model model, HttpServletResponse response) {
        StringBuilder stringBuilder = new StringBuilder();
        if (ConstantsNet.getAllDevices().remainingCapacity() == 0) {
            ConstantsNet.getAllDevices().forEach(x->stringBuilder.append(ConstantsNet.getAllDevices().remove()));
            model.addAttribute("pcs", stringBuilder.toString());
        }
        else {
            allDevNotNull(model, response);
        }
    }
    
    /**
     Если размер {@link ConstantsNet#getAllDevices()} более 0
     <p>
     {@code scansInMin} - кол-во сканирований в минуту для рассчёта времени. {@code minLeft} - примерное кол-во оставшихся минут.
     {@code attributeValue} - то, что видим на страничке.
     <p>
     <b>{@link Model#addAttribute(Object)}:</b> <br>
     {@link ConstantsFor#ATT_TITLE} = {@code attributeValue} <br>
     {@code pcs} = {@link ConstantsNet#FILENAME_NEWLAN205} + {@link ConstantsNet#FILENAME_OLDLANTXT0} и {@link ConstantsNet#FILENAME_OLDLANTXT1} + {@link ConstantsNet#FILENAME_SERVTXT}
     <p>
     <b>{@link HttpServletResponse#addHeader(String, String)}:</b><br>
     {@link ConstantsFor#HEAD_REFRESH} = 45
     
     @param model {@link Model}
     @param response {@link HttpServletResponse}
     */
    private void allDevNotNull(@NotNull Model model, @NotNull HttpServletResponse response) {
        final float scansInMin = Float.parseFloat(AppComponents.getProps().getProperty(ConstantsFor.PR_SCANSINMIN, "200"));
        float minLeft = ConstantsNet.getAllDevices().remainingCapacity() / scansInMin;
    
        StringBuilder attTit = new StringBuilder().append(minLeft).append(" ~minLeft. ")
            .append(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis((long) minLeft)));
        model.addAttribute(ConstantsFor.ATT_TITLE, attTit.toString());
        model.addAttribute("pcs", new ScanOnline().getPingResultStr());
        response.addHeader(ConstantsFor.HEAD_REFRESH, "75");
    }
}
