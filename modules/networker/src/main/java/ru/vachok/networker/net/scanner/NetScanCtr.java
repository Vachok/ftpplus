// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.scanner;


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
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.abstr.NetKeeper;
import ru.vachok.networker.ad.user.InformationFactoryImpl;
import ru.vachok.networker.componentsrepo.PageFooter;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.componentsrepo.report.InformationFactory;
import ru.vachok.networker.enums.*;
import ru.vachok.networker.exe.ThreadConfig;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.LongNetScanServiceFactory;
import ru.vachok.networker.net.NetScanService;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.*;
import java.util.concurrent.*;

import static ru.vachok.networker.ConstantsFor.STR_P;


/**
 @see ru.vachok.networker.controller.NetScanCtrTest
 @since 30.08.2018 (12:55) */
@SuppressWarnings({"SameReturnValue", "DuplicateStringLiteralInspection", "ClassUnconnectedToPackage"})
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
    
    private static final ThreadPoolTaskExecutor THREAD_POOL_TASK_EXECUTOR_LOCAL = AppComponents.threadConfig();
    
    private static final PageFooter PAGE_FOOTER = new PageFooter();
    
    private final MessageToUser messageToUser = new MessageLocal(NetScanCtr.class.getSimpleName());
    
    private final File scanTemp = new File("scan.tmp");
    
    private final ConcurrentNavigableMap<String, Boolean> lastScanMAP = NetKeeper.getNetworkPCs();
    
    private final TForms forms = new TForms();
    
    /**
     {@link AppComponents#netScannerSvc()}
     */
    private InformationFactory netScannerSvcInstAW;
    
    private NetScanService netPingerInst;
    
    @Autowired
    public NetScanCtr(NetScannerSvc netScannerSvc, LongNetScanServiceFactory netPingerInst) {
        this.netScannerSvcInstAW = netScannerSvc;
        this.netPingerInst = netPingerInst;
    }
    
    /**
     GET /{@link #STR_NETSCAN} Старт сканера локальных ПК
     <p>
     1. {@link UsefulUtilites#getVis(HttpServletRequest)}. Запись {@link Visitor } <br>
     2.{@link NetScannerSvc#setThePc(java.lang.String)} обнуляем строку в форме. <br>
     3. {@link FileSystemWorker#readFile(java.lang.String)} добавляем файл {@code lastnetscan.log} в качестве аттрибута {@code pc} в {@link Model} <br>
     4. {@link NetScannerSvc#getThePc()} аттрибут {@link Model} - {@link #ATT_THEPC}. <br>
     7. {@link #checkMapSizeAndDoAction(Model, HttpServletRequest, long)} - начинаем проверку.
     <p>
 
     @param request {@link HttpServletRequest} для {@link UsefulUtilites#getVis(HttpServletRequest)}
     @param response {@link HttpServletResponse} добавить {@link ConstantsFor#HEAD_REFRESH} 30 сек
     @param model {@link Model}
     @return {@link ConstantsNet#ATT_NETSCAN} (netscan.html)
     */
    @GetMapping(STR_NETSCAN)
    public String netScan(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Model model) {
        final long lastSt = Long.parseLong(PROPERTIES.getProperty(ConstantsNet.PR_LASTSCAN, "1548919734742"));
        UsefulUtilites.getVis(request);
        model.addAttribute("serviceinfo", (float) TimeUnit.MILLISECONDS.toSeconds(lastSt - System.currentTimeMillis()) / UsefulUtilites.ONE_HOUR_IN_MIN);
        ((NetScannerSvc) netScannerSvcInstAW).setThePc("");
        model.addAttribute("pc", FileSystemWorker.readFile(ConstantsNet.BEANNAME_LASTNETSCAN) + "<p>");
        model.addAttribute(ModelAttributeNames.ATT_TITLE, ((NetScannerSvc) netScannerSvcInstAW).getOnLinePCsNum() + " pc at " + new Date(lastSt));
        model.addAttribute(ConstantsNet.BEANNAME_NETSCANNERSVC, netScannerSvcInstAW);
        model.addAttribute(ATT_THEPC, ((NetScannerSvc) netScannerSvcInstAW).getThePc());
        model.addAttribute(ModelAttributeNames.ATT_FOOTER, PAGE_FOOTER.getFooterUtext() + "<br>First Scan: 2018-05-05");
        response.addHeader(ConstantsFor.HEAD_REFRESH, "30");
    
        try {
            checkMapSizeAndDoAction(model, request, lastSt);
        }
        catch (InterruptedException e) {
            model.addAttribute(ModelAttributeNames.ATT_PCS, e.getMessage());
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        catch (ExecutionException | IOException e) {
            model.addAttribute(ModelAttributeNames.ATT_PCS, forms.fromArray(e, true));
        }
        catch (TimeoutException e) {
            model.addAttribute(ModelAttributeNames.ATT_PCS, "TIMEOUT!<p>" + e.getMessage());
        }
        return ConstantsNet.ATT_NETSCAN;
    }
    
    @GetMapping("/ping")
    public String pingAddr(@NotNull Model model, HttpServletRequest request, @NotNull HttpServletResponse response) {
        ((LongNetScanServiceFactory) netPingerInst)
            .setTimeForScanStr(String.valueOf(TimeUnit.SECONDS.toMinutes(Math.abs(LocalTime.now().toSecondOfDay() - LocalTime.parse("08:30").toSecondOfDay()))));
        model.addAttribute(ModelAttributeNames.ATT_NETPINGER, netPingerInst);
        model.addAttribute("pingTest", netPingerInst.getStatistics());
        model.addAttribute("pingResult", FileSystemWorker.readFile(FileNames.PINGRESULT_LOG));
        model.addAttribute(ModelAttributeNames.ATT_TITLE, netPingerInst.getExecution() + " pinger hash: " + netPingerInst.hashCode());
        model.addAttribute(ModelAttributeNames.ATT_FOOTER, PAGE_FOOTER.getFooterUtext());
        //noinspection MagicNumber
        response.addHeader(ConstantsFor.HEAD_REFRESH, String.valueOf(ConstantsFor.DELAY * 1.8f));
        messageToUser.info("NetScanCtr.pingAddr", "HEAD_REFRESH", " = " + response.getHeader(ConstantsFor.HEAD_REFRESH));
        return "ping";
    }
    
    @PostMapping("/ping")
    public String pingPost(Model model, HttpServletRequest request, @NotNull @ModelAttribute LongNetScanServiceFactory netPinger, HttpServletResponse response) {
        this.netPingerInst = netPinger;
        try {
            netPinger.run();
        }
        catch (InvokeIllegalException e) {
            String multipartFileResource = getClass().getResource("/static/ping2ping.txt").getFile();
            FileItemFactory factory = new DiskFileItemFactory();
            FileItem fileItem = factory.createItem("multipartFile", "text/plain", true, multipartFileResource);
        }
        model.addAttribute(ModelAttributeNames.ATT_NETPINGER, netPinger);
        String npEq = "Netpinger equals is " + netPinger.equals(this.netPingerInst);
        model.addAttribute(ModelAttributeNames.ATT_TITLE, npEq);
        model.addAttribute("ok", FileSystemWorker.readFile(FileNames.PINGRESULT_LOG));
        messageToUser.infoNoTitles("npEq = " + npEq);
        response.addHeader(ConstantsFor.HEAD_REFRESH, PROPERTIES.getProperty(PropertiesNames.PR_PINGSLEEP, "60"));
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
    public @NotNull String pcNameForInfo(@NotNull @ModelAttribute NetScannerSvc netScannerSvc, Model model) {
        this.netScannerSvcInstAW = netScannerSvc;
        String thePc = netScannerSvc.getThePc();
        if (thePc.toLowerCase().contains("user: ")) {
            model.addAttribute("ok", InformationFactoryImpl.getUserFromDB(thePc).trim());
            model.addAttribute(ModelAttributeNames.ATT_TITLE, thePc);
            model.addAttribute(ModelAttributeNames.ATT_FOOTER, PAGE_FOOTER.getFooterUtext());
            return "ok";
        }
        model.addAttribute(ATT_THEPC, thePc);
        netScannerSvc.setThePc("");
        return "redirect:/ad?" + thePc;
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
        sb.append(", ATT_NETPINGER='").append(ModelAttributeNames.ATT_NETPINGER).append('\'');
        sb.append(", lastScanMAP=").append(lastScanMAP.size());
        sb.append(", Bean information=").append(getInformationForThreads(getTHRBeanMX()));
        sb.append('}');
        return sb.toString();
    }
    
    private void mapSizeBigger(@NotNull Model model, HttpServletRequest request, long lastSt, int thisTotpc) throws ExecutionException, InterruptedException, TimeoutException, IOException {
        long timeLeft = TimeUnit.MILLISECONDS.toSeconds(lastSt - System.currentTimeMillis());
        int pcWas = Integer.parseInt(PROPERTIES.getProperty(PropertiesNames.PR_ONLINEPC, "0"));
        int remainPC = thisTotpc - lastScanMAP.size();
        boolean newPSs = 0 > remainPC;
        
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(timeLeft);
        stringBuilder.append(" seconds (");
        stringBuilder.append((float) timeLeft / UsefulUtilites.ONE_HOUR_IN_MIN);
        stringBuilder.append(" min) left<br>Delay period is ");
        stringBuilder.append(DURATION_MIN);
        
        String msg = stringBuilder.toString();
        LOGGER.info(msg);
        
        StringBuilder titleBuilder = new StringBuilder();
        titleBuilder.append(remainPC);
        titleBuilder.append("/");
        titleBuilder.append(thisTotpc);
        titleBuilder.append(" PCs (");
        titleBuilder.append(((NetScannerSvc) netScannerSvcInstAW).getOnLinePCsNum());
        titleBuilder.append("/");
        titleBuilder.append(pcWas);
        titleBuilder.append(") Next run ");
        titleBuilder.append(LocalDateTime.ofEpochSecond(lastSt / 1000, 0, ZoneOffset.ofHours(3)).toLocalTime());
    
        String pcValue = fromArray(lastScanMAP);
        
        model
            .addAttribute("left", msg)
            .addAttribute("pc", pcValue)
            .addAttribute(ModelAttributeNames.ATT_TITLE, titleBuilder.toString());
        if (newPSs) {
            FileSystemWorker.writeFile(ConstantsNet.BEANNAME_LASTNETSCAN, pcValue);
            model.addAttribute(PropertiesNames.PR_AND_ATT_NEWPC, "Добавлены компы! " + Math.abs(remainPC) + " шт.");
            PROPERTIES.setProperty(PropertiesNames.PR_TOTPC, String.valueOf(lastScanMAP.size()));
            PROPERTIES.setProperty(PropertiesNames.PR_AND_ATT_NEWPC, String.valueOf(remainPC));
        }
        else {
            if (ConstantsFor.INT_ANSWER > remainPC) {
                PROPERTIES.setProperty(PropertiesNames.PR_TOTPC, String.valueOf(lastScanMAP.size()));
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
        Runnable scanRun = ()->NetScanCtr.this.scanIt(request, model, new Date(lastScanEpoch * 1000));
        LocalTime lastScanLocalTime = LocalDateTime.ofEpochSecond(lastScanEpoch, 0, ZoneOffset.ofHours(3)).toLocalTime();
        boolean isSystemTimeBigger = (System.currentTimeMillis() > lastScanEpoch * 1000);
        if (!(scanTemp.exists())) {
            model.addAttribute(PropertiesNames.PR_AND_ATT_NEWPC, lastScanLocalTime);
            if (isSystemTimeBigger) {
                Future<?> submitScan = Executors.newSingleThreadExecutor().submit(scanRun);
                submitScan.get(ConstantsFor.DELAY - 1, TimeUnit.MINUTES);
                messageToUser.warn(MessageFormat.format("Scan is Done {0}", submitScan.isDone()));
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
        };
        int thisTotpc = Integer.parseInt(PROPERTIES.getProperty(PropertiesNames.PR_TOTPC, "259"));
        
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
            ((NetScannerSvc) netScannerSvcInstAW).setOnLinePCsNum(0);
            Set<String> pcNames = ((NetScannerSvc) netScannerSvcInstAW).theSETOfPCNamesPref(request.getQueryString());
            model
                .addAttribute(ModelAttributeNames.ATT_TITLE, new Date().toString())
                .addAttribute("pc", forms.fromArray(pcNames, true));
        }
        else {
            lastScanMAP.clear();
            ((NetScannerSvc) netScannerSvcInstAW).setOnLinePCsNum(0);
            Set<String> pCsAsync = ((NetScannerSvc) netScannerSvcInstAW).theSETOfPcNames();
            model.addAttribute(ModelAttributeNames.ATT_TITLE, lastScanDate).addAttribute("pc", forms.fromArray(pCsAsync, true));
            PROPERTIES.setProperty(ConstantsNet.PR_LASTSCAN, String.valueOf(System.currentTimeMillis()));
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
    
}
