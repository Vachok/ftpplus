// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.scanner;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.componentsrepo.htmlgen.HTMLGeneration;
import ru.vachok.networker.componentsrepo.htmlgen.PageGenerationHelper;
import ru.vachok.networker.data.enums.*;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.restapi.props.InitProperties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.TimeUnit;


/**
 @see ru.vachok.networker.net.scanner.NetScanCtrTest
 @since 30.08.2018 (12:55) */
@SuppressWarnings({"SameReturnValue", "ClassUnconnectedToPackage"})
@Controller
public class NetScanCtr {
    
    
    /**
     <i>Boiler Plate</i>
     */
    private static final String STR_NETSCAN = "/netscan";
    
    private static final Properties PROPERTIES = AppComponents.getProps();
    
    private static final HTMLGeneration PAGE_FOOTER = new PageGenerationHelper();
    
    private static MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, NetScanCtr.class.getSimpleName());
    
    private final ThreadPoolTaskScheduler scheduler = AppComponents.threadConfig().getTaskScheduler();
    
    @SuppressWarnings("InstanceVariableOfConcreteClass") private PcNamesScanner pcNamesScanner;
    
    private HttpServletRequest request;
    
    private HttpServletResponse response;
    
    private Model model = new ExtendedModelMap();
    
    public HttpServletResponse getResponse() {
        return response;
    }
    
    HttpServletRequest getRequest() {
        return request;
    }
    
    Model getModel() {
        return model;
    }
    
    public void setModel(Model model) {
        this.model = model;
    }
    
    @Contract(pure = true)
    public NetScanCtr() {
        this.pcNamesScanner = new PcNamesScanner();
    }
    
    /**
     @see NetScanCtrTest#testNetScan()
     */
    @GetMapping(STR_NETSCAN)
    public String netScan(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Model model) {
        this.request = request;
        this.response = response;
        this.model = model;
        long lastScan = Long.parseLong(InitProperties.getUserPref().get(PropertiesNames.LASTSCAN, "1548919734742"));
        pcNamesScanner.setClassOption(this);
        UsefulUtilities.getVis(request);
    
        float serviceInfoVal = (float) TimeUnit.MILLISECONDS
                .toSeconds(InitProperties.getUserPref().getLong(PropertiesNames.NEXTSCAN, UsefulUtilities.getAtomicTime()) - System
                .currentTimeMillis()) / ConstantsFor.ONE_HOUR_IN_MIN;
        String pcVal = pcNamesScanner.getStatistics() + "<p>";
        String titleVal = InitProperties.getUserPref().get(PropertiesNames.ONLINEPC, "0") + " pc at " + new Date(lastScan);
        String footerVal = PAGE_FOOTER.getFooter(ModelAttributeNames.FOOTER) + "<br>First Scan: 2018-05-05";
        String thePCVal = pcNamesScanner.getThePc();
        
        model.addAttribute(ModelAttributeNames.SERVICEINFO, serviceInfoVal);
        model.addAttribute(ModelAttributeNames.PC, pcVal);
        model.addAttribute(ModelAttributeNames.TITLE, titleVal);
        model.addAttribute(ConstantsFor.BEANNAME_NETSCANNERSVC, pcNamesScanner);
        model.addAttribute(ModelAttributeNames.THEPC, thePCVal);
        model.addAttribute(ModelAttributeNames.FOOTER, MessageFormat.format("{0}<br>{1}", scheduler.getScheduledThreadPoolExecutor().toString(), footerVal));
        
        response.addHeader(ConstantsFor.HEAD_REFRESH, "30");
        
        starterNetScan();
        return ModelAttributeNames.NETSCAN;
    }
    
    private void starterNetScan() {
        RejectedExecutionHandler handlerReject = scheduler.getScheduledThreadPoolExecutor().getRejectedExecutionHandler();
        if (!new File(FileNames.SCAN_TMP).exists()) {
            messageToUser.info(this.getClass().getSimpleName(), "No tmp file. Starting", new Date().toString());
            scheduler.scheduleAtFixedRate(pcNamesScanner, new Date(InitProperties.getUserPref()
                .getLong(PropertiesNames.LASTSCAN, System.currentTimeMillis())), TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY + 5));
        }
        else {
            String schedExec = scheduler.getScheduledThreadPoolExecutor().toString();
            FileSystemWorker.writeFile(FileNames.SCHEDULER_TXT, scheduler.getScheduledThreadPoolExecutor().getQueue().stream());
            FileSystemWorker.appendObjectToFile(new File(FileNames.SCHEDULER_TXT), MessageFormat.format("Current class hash : {0}", pcNamesScanner.hashCode()));
            messageToUser.warn(this.getClass().getSimpleName(), new File(FileNames.SCHEDULER_TXT).getAbsolutePath(), schedExec);
        }
    }
    
    /**
     POST /netscan
     <p>
 
     @param pcNamesScanner {@link PcNamesScanner}
     @param model {@link Model}
     @return redirect:/ad? + {@link PcNamesScanner#getThePc()}
     */
    @PostMapping(STR_NETSCAN)
    public @NotNull String pcNameForInfo(@NotNull @ModelAttribute PcNamesScanner pcNamesScanner, Model model) {
    
        this.pcNamesScanner = pcNamesScanner;
        this.model = model;
        this.pcNamesScanner.setClassOption(this);
        String thePc = pcNamesScanner.getThePc();
        
        if (thePc.toLowerCase().contains("user: ")) {
            model.addAttribute("ok", this.pcNamesScanner.getExecution().trim());
            model.addAttribute(ModelAttributeNames.TITLE, thePc);
            model.addAttribute(ModelAttributeNames.FOOTER, PAGE_FOOTER.getFooter(ModelAttributeNames.FOOTER));
            return "ok";
        }
        model.addAttribute(ModelAttributeNames.THEPC, thePc);
        pcNamesScanner.setThePc("");
        return "redirect:/ad?" + thePc;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NetScanCtr{");
        try {
            sb.append(", request=").append(request);
        }
        catch (RuntimeException e) {
            sb.append(MessageFormat.format("Exception: {0} in {1}.toString()", e.getMessage(), this.getClass().getSimpleName()));
        }
        sb.append('}');
        return sb.toString();
    }
}
