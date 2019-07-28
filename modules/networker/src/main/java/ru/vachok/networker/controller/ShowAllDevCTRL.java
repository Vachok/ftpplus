// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.controller;


import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.abstr.NetKeeper;
import ru.vachok.networker.abstr.monitors.NetScanService;
import ru.vachok.networker.componentsrepo.PageFooter;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.net.scanner.ScanOnline;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

import static ru.vachok.networker.ConstantsFor.ATT_PCS;


/**
 Class ru.vachok.networker.controller.ShowAllDevCTRL
 <p>
 
 @see ru.vachok.networker.controller.ShowAllDevCTRLTest
 @since 20.07.2019 (10:13) */
@Controller
public class ShowAllDevCTRL {
    
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    private NetScanService scanOnline;
    
    @Autowired
    public ShowAllDevCTRL(NetScanService scanOnline) {
        this.scanOnline = scanOnline;
    }
    
    @GetMapping("/showalldev")
    public String allDevices(@NotNull Model model, HttpServletRequest request, HttpServletResponse response) {
        model.addAttribute(ConstantsFor.ATT_TITLE, NetKeeper.getAllDevices().remainingCapacity() + " ip remain");
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
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext() + ". Left: " + NetKeeper.getAllDevices().remainingCapacity() + " " +
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
        if (NetKeeper.getAllDevices().remainingCapacity() == 0) {
            NetKeeper.getAllDevices().forEach(x->stringBuilder.append(NetKeeper.getAllDevices().remove()));
            model.addAttribute("pcs", stringBuilder.toString());
        }
        else {
            allDevNotNull(model, response);
        }
    }
    
    /**
     Если размер {@link NetKeeper#getAllDevices()} более 0
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
        float minLeft = NetKeeper.getAllDevices().remainingCapacity() / scansInMin;
        
        StringBuilder attTit = new StringBuilder().append(minLeft).append(" ~minLeft. ")
            .append(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis((long) minLeft)));
        model.addAttribute(ConstantsFor.ATT_TITLE, attTit.toString());
        model.addAttribute("pcs", new ScanOnline().getPingResultStr());
        response.addHeader(ConstantsFor.HEAD_REFRESH, "75");
    }
}