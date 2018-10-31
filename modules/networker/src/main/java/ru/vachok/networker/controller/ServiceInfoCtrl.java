package ru.vachok.networker.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.componentsrepo.PageFooter;
import ru.vachok.networker.componentsrepo.ServiceInform;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.config.AppCtx;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.AccessDeniedException;
import java.time.LocalTime;
import java.util.Map;


/**
 * @since 21.09.2018 (11:33)
 */
@Controller
public class ServiceInfoCtrl {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceInfoCtrl.class.getSimpleName());

    private ServiceInform serviceInform;

    private Visitor visitor;

    private Map<String, Boolean> localMapSB;


    /*Instances*/
    @Autowired
    public ServiceInfoCtrl(ServiceInform serviceInform) {
        this.localMapSB = new AppComponents().lastNetScanMap();
        this.serviceInform = serviceInform;
    }

    @GetMapping("/serviceinfo")
    public String infoMapping(Model model, HttpServletRequest request) throws AccessDeniedException {
        this.visitor = new Visitor(request);
        try{
            LOGGER.warn(visitor.toString());
        }
        catch(Exception e){
            LoggerFactory.getLogger(ServiceInfoCtrl.class.getSimpleName());
        }
        if (request.getRemoteAddr().contains("0:0:0:0") ||
            request.getRemoteAddr().contains("10.10.111") ||
            request.getRemoteAddr().contains(ConstantsFor.NO0027)) {
            modModMaker(model, request);
            return "vir";
        } else throw new AccessDeniedException("Sorry. Denied");
    }

    private void modModMaker(Model model, HttpServletRequest request) {
        this.serviceInform = new ServiceInform();
        model.addAttribute("title", "srv-git is " + pingBool() + "noF: " +
            ConstantsFor.NO_F_DAYS);
        model.addAttribute("ping", pingGit());
        model.addAttribute("urls", new AppCtx().toString().replaceAll("\n", "<br>"));
        model.addAttribute("request", prepareRequest(request));
        model.addAttribute("visit", AppComponents.versionInfo().toString());
        model.addAttribute("back", request.getHeader("REFERER".toLowerCase()));
        model.addAttribute("footer", new PageFooter().getFooterUtext());
        //        model.addAttribute("res", serviceInform.getResourcesTXT()); fixme 02.10.2018 (20:40)
    }
    private boolean pingBool() {
        try {
            return InetAddress.getByName("srv-git.eatmeat.ru").isReachable(1000);
        } catch (IOException e) {
            return false;
        }
    }
    private String pingGit() {
        try {
            InetAddress byName = InetAddress.getByName("srv-git.eatmeat.ru");
            boolean reachable = byName.isReachable(1000);
            if (reachable) {
                return "<b><font color=\"#77ff72\">" + true + "</b> srv-git.eatmeat.ru.</font> Checked at: <i>" + LocalTime.now() + "</i><br>";
            } else return "<b><font color=\"#ff2121\">" + true + "</b> srv-git.eatmeat.ru.</font> Checked at: <i>" + LocalTime.now() + "</i><br>";
        } catch (IOException e) {
            return e.getMessage();
        }
    }
    private String prepareRequest(HttpServletRequest request) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<center><h3>Заголовки</h3></center>");
        String bBr = "</b><br>";
        stringBuilder
            .append("HOST: ")
            .append("<b>").append(request.getHeader("host")).append(bBr);
        stringBuilder
            .append("CONNECTION: ")
            .append("<b>").append(request.getHeader("connection")).append(bBr);
        stringBuilder
            .append("upgrade-insecure-requests: ".toUpperCase())
            .append("<b>").append(request.getHeader("upgrade-insecure-requests")).append(bBr);
        stringBuilder
            .append("user-agent: ".toUpperCase())
            .append("<b>").append(request.getHeader("user-agent")).append(bBr);
        stringBuilder
            .append("ACCEPT: ")
            .append("<b>").append(request.getHeader("accept")).append(bBr);
        stringBuilder
            .append("referer: ".toUpperCase())
            .append("<b>").append(request.getHeader("referer")).append(bBr);
        stringBuilder
            .append("accept-encoding: ".toUpperCase())
            .append("<b>").append(request.getHeader("accept-encoding")).append(bBr);
        stringBuilder
            .append("accept-language: ".toUpperCase())
            .append("<b>").append(request.getHeader("accept-language")).append(bBr);
        stringBuilder
            .append("cookie: ".toUpperCase())
            .append("<b>").append(request.getHeader("cookie")).append(bBr);

        stringBuilder.append("<center><h3>Атрибуты</h3></center>");
        stringBuilder.append(new TForms().fromEnum(request.getAttributeNames(), true));

        stringBuilder.append("<center><h3>Параметры</h3></center>");
        stringBuilder.append(new TForms().mapStrStrArr(request.getParameterMap(), true));

        stringBuilder.append("<center><h3>Cookies</h3></center>");
        stringBuilder.append(new TForms().fromArray(ConstantsFor.VISITS_MAP.get(visitor.getUserID()).getCookies(), true));
        return stringBuilder.toString();
    }

    @GetMapping("/clsmail")
    public String mailBox(Model model, HttpServletRequest request) {
        model.addAttribute("title", "You have another app");
        model.addAttribute("mbox", "See another APP");
        model.addAttribute("locator", new TForms().mapStringBoolean(localMapSB));
        return "clsmail";
    }
}
