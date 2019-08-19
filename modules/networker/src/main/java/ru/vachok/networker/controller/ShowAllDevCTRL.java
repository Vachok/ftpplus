// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.controller;


import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.enums.ModelAttributeNames;
import ru.vachok.networker.enums.PropertiesNames;
import ru.vachok.networker.info.HTMLGeneration;
import ru.vachok.networker.info.PageGenerationHelper;
import ru.vachok.networker.net.NetKeeper;
import ru.vachok.networker.net.NetScanService;
import ru.vachok.networker.net.scanner.ScanOnline;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.StringJoiner;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.TimeUnit;

import static ru.vachok.networker.enums.ModelAttributeNames.ATT_PCS;


/**
 Class ru.vachok.networker.controller.ShowAllDevCTRL
 <p>
 
 @see ru.vachok.networker.controller.ShowAllDevCTRLTest
 @since 20.07.2019 (10:13) */
@Controller
public class ShowAllDevCTRL {
    
    
    private final HTMLGeneration pageFooter = new PageGenerationHelper();
    
    private MessageToUser messageToUser;
    
    private NetScanService scanOnline;
    
    @Autowired
    public ShowAllDevCTRL(NetScanService scanOnline) {
        this.scanOnline = scanOnline;
        this.messageToUser = new MessageLocal(getClass().getSimpleName());
    }
    
    @GetMapping(ConstantsFor.SHOWALLDEV)
    public String allDevices(@NotNull Model model, HttpServletRequest request, HttpServletResponse response) {
        model.addAttribute(ModelAttributeNames.ATT_TITLE, NetKeeper.getAllDevices().remainingCapacity() + " ip remain");
        try {
            model.addAttribute(ATT_PCS, scanOnline.toString());
        }
        catch (RuntimeException e) {
            messageToUser.error(e.getMessage());
        }
        if (request.getQueryString() != null) {
            qerNotNullScanAllDevices(model, response);
        }
        model.addAttribute(ModelAttributeNames.ATT_HEAD,
            pageFooter.getInfoAbout(ModelAttributeNames.ATT_HEAD) + "<center><p><a href=\"/showalldev?needsopen\"><h2>Show All IPs in file</h2></a></center>");
//        this.scanOnline= DiapazonScan.getInstance(); todo 12.08.2019 (9:15)
        model.addAttribute("ok", scanOnline.getPingResultStr());
        model.addAttribute(ModelAttributeNames.ATT_FOOTER, pageFooter.getInfoAbout(ModelAttributeNames.ATT_FOOTER) + ". Left: " + NetKeeper.getAllDevices()
            .remainingCapacity() + " " +
            "IPs.");
        return "ok";
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", ShowAllDevCTRL.class.getSimpleName() + "[\n", "\n]")
            .add("scanOnline = " + scanOnline.getPingResultStr())
            .toString();
    }
    
    private void qerNotNullScanAllDevices(Model model, HttpServletResponse response) {
        StringBuilder stringBuilder = new StringBuilder();
        BlockingDeque<String> allDevices = NetKeeper.getAllDevices();
        if (allDevices.remainingCapacity() == 0) {
            allDevices.forEach(x->stringBuilder.append(allDevices.remove()));
            model.addAttribute("pcs", stringBuilder.toString());
        }
        else {
            allDevNotNull(model, response);
        }
    }
    
    private void allDevNotNull(@NotNull Model model, @NotNull HttpServletResponse response) {
        final float scansInMin = Float.parseFloat(AppComponents.getProps().getProperty(PropertiesNames.PR_SCANSINMIN, "200"));
        float minLeft = NetKeeper.getAllDevices().remainingCapacity() / scansInMin;
        
        StringBuilder attTit = new StringBuilder().append(minLeft).append(" ~minLeft. ")
            .append(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis((long) minLeft)));
        model.addAttribute(ModelAttributeNames.ATT_TITLE, attTit.toString());
        model.addAttribute("pcs", new ScanOnline().getPingResultStr());
        response.addHeader(ConstantsFor.HEAD_REFRESH, "75");
    }
}