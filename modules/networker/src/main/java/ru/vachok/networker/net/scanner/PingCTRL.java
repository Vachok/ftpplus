// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.scanner;


import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.FileItemFactory;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.componentsrepo.data.enums.*;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.componentsrepo.htmlgen.HTMLGeneration;
import ru.vachok.networker.componentsrepo.htmlgen.PageGenerationHelper;
import ru.vachok.networker.info.NetScanService;
import ru.vachok.networker.net.monitor.PingerFromFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalTime;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;


/**
 @since 18.08.2019 (21:44) */
@Controller
public class PingCTRL {
    
    
    private static final HTMLGeneration PAGE_FOOTER = new PageGenerationHelper();
    
    private static final Properties PROPERTIES = AppComponents.getProps();
    
    private NetScanService netPingerInst;
    
    private MessageToUser messageToUser = ru.vachok.networker.restapi.message.MessageToUser
            .getInstance(ru.vachok.networker.restapi.message.MessageToUser.LOCAL_CONSOLE, this.getClass().getSimpleName());
    
    @Autowired
    public PingCTRL(PingerFromFile netPingerInst) {
        this.netPingerInst = netPingerInst;
    }
    
    @GetMapping("/ping")
    public String pingAddr(@NotNull Model model, HttpServletRequest request, @NotNull HttpServletResponse response) {
        ((PingerFromFile) netPingerInst)
            .setTimeForScanStr(String.valueOf(TimeUnit.SECONDS.toMinutes(Math.abs(LocalTime.now().toSecondOfDay() - LocalTime.parse("08:30").toSecondOfDay()))));
        model.addAttribute(ModelAttributeNames.ATT_NETPINGER, netPingerInst);
        model.addAttribute("pingTest", netPingerInst.getStatistics());
        model.addAttribute("pingResult", FileSystemWorker.readFile(FileNames.PINGRESULT_LOG));
        model.addAttribute(ModelAttributeNames.TITLE, netPingerInst.getExecution() + " pinger hash: " + netPingerInst.hashCode());
        model.addAttribute(ModelAttributeNames.FOOTER, PAGE_FOOTER.getFooter(ModelAttributeNames.FOOTER));
        //noinspection MagicNumber
        response.addHeader(ConstantsFor.HEAD_REFRESH, String.valueOf(ConstantsFor.DELAY * 1.8f));
        messageToUser.info("NetScanCtr.pingAddr", "HEAD_REFRESH", " = " + response.getHeader(ConstantsFor.HEAD_REFRESH));
        return "ping";
    }
    
    @PostMapping("/ping")
    public String pingPost(Model model, HttpServletRequest request, @NotNull @ModelAttribute PingerFromFile netPinger, HttpServletResponse response) {
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
        model.addAttribute(ModelAttributeNames.TITLE, npEq);
        model.addAttribute("ok", FileSystemWorker.readFile(FileNames.PINGRESULT_LOG));
        messageToUser.infoNoTitles("npEq = " + npEq);
        response.addHeader(ConstantsFor.HEAD_REFRESH, PROPERTIES.getProperty(PropertiesNames.PR_PINGSLEEP, "60"));
        return "ok";
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", PingCTRL.class.getSimpleName() + "[\n", "\n]")
            .add("netPingerInst = " + netPingerInst.toString())
            .toString();
    }
}