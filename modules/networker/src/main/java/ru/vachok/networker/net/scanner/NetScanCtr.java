// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.scanner;


import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.FileItemFactory;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.abstr.NetKeeper;
import ru.vachok.networker.ad.user.TvPcInformation;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.enums.*;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.info.PageFooter;
import ru.vachok.networker.net.LongNetScanServiceFactory;
import ru.vachok.networker.net.NetScanService;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalTime;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 @see ru.vachok.networker.net.scanner.NetScanCtrTest
 @since 30.08.2018 (12:55) */
@SuppressWarnings({"SameReturnValue", "DuplicateStringLiteralInspection", "ClassUnconnectedToPackage"})
@Controller
public class NetScanCtr {
    
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
    
    private static final Properties PROPERTIES = AppComponents.getProps();
    
    private static final InformationFactory PAGE_FOOTER = new PageFooter();
    
    private static final MessageToUser messageToUser = new MessageLocal(NetScanCtr.class.getSimpleName());
    
    private static final TForms T_FORMS = new TForms();
    
    private NetScannerSvc netScannerSvcInstAW;
    
    private NetScanService netPingerInst;
    
    @Contract(pure = true)
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
     7. {@link NetScannerSvc#checkMapSizeAndDoAction(Model, HttpServletRequest, long)} - начинаем проверку.
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
        netScannerSvcInstAW.setThePc("");
        model.addAttribute("pc", FileSystemWorker.readFile(ConstantsNet.BEANNAME_LASTNETSCAN) + "<p>");
        model.addAttribute(ModelAttributeNames.ATT_TITLE, AppComponents.getUserPref().get(PropertiesNames.PR_ONLINEPC, "0") + " pc at " + new Date(lastSt));
        model.addAttribute(ConstantsNet.BEANNAME_NETSCANNERSVC, netScannerSvcInstAW);
        model.addAttribute(ATT_THEPC, netScannerSvcInstAW.getThePc());
        model.addAttribute(ModelAttributeNames.ATT_FOOTER, PAGE_FOOTER.getInfoAbout(ModelAttributeNames.ATT_FOOTER) + "<br>First Scan: 2018-05-05");
        response.addHeader(ConstantsFor.HEAD_REFRESH, "30");
    
        try {
            netScannerSvcInstAW.checkMapSizeAndDoAction(model, request, lastSt);
        }
        catch (InterruptedException e) {
            model.addAttribute(ModelAttributeNames.ATT_PCS, e.getMessage());
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        catch (ExecutionException | IOException e) {
            model.addAttribute(ModelAttributeNames.ATT_PCS, T_FORMS.fromArray(e, true));
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
        model.addAttribute(ModelAttributeNames.ATT_FOOTER, PAGE_FOOTER.getInfoAbout(ModelAttributeNames.ATT_FOOTER));
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
            model.addAttribute("ok", TvPcInformation.getUserFromDB(thePc).trim());
            model.addAttribute(ModelAttributeNames.ATT_TITLE, thePc);
            model.addAttribute(ModelAttributeNames.ATT_FOOTER, PAGE_FOOTER.getInfoAbout(ModelAttributeNames.ATT_FOOTER));
            return "ok";
        }
        model.addAttribute(ATT_THEPC, thePc);
        netScannerSvc.setThePc("");
        return "redirect:/ad?" + thePc;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NetScanCtr{");
        sb.append("ATT_NETSCAN='").append(ConstantsNet.ATT_NETSCAN).append('\'');
        sb.append(", PROPERTIES=").append(PROPERTIES.size());
        sb.append(", DURATION_MIN=").append(NetScannerSvc.DURATION_MIN);
        sb.append(", STR_NETSCAN='").append(STR_NETSCAN).append('\'');
        sb.append(", ATT_THEPC='").append(ATT_THEPC).append('\'');
        sb.append(", NETSCANNERSVC_INST=").append(netScannerSvcInstAW.hashCode());
        sb.append(", STR_REQUEST='").append(STR_REQUEST).append('\'');
        sb.append(", STR_MODEL='").append(STR_MODEL).append('\'');
        sb.append(", ATT_NETPINGER='").append(ModelAttributeNames.ATT_NETPINGER).append('\'');
        sb.append(", lastScanMAP=").append(NetKeeper.getNetworkPCs().size());
        sb.append('}');
        return sb.toString();
    }
}
