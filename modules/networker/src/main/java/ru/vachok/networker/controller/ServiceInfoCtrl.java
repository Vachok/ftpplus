package ru.vachok.networker.controller;


import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.config.AppCtx;
import ru.vachok.networker.services.CookieShower;
import ru.vachok.networker.services.VisitorSrv;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.AccessDeniedException;
import java.time.LocalTime;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * @since 21.09.2018 (11:33)
 */
@Controller
public class ServiceInfoCtrl {

    private VisitorSrv visitorSrv;

    private CookieShower cookieShower;

    private Map<String, Boolean> localMapSB;


    /*Instances*/
    @Autowired
    public ServiceInfoCtrl(VisitorSrv visitorSrv) {
        this.visitorSrv = visitorSrv;
        this.cookieShower = visitorSrv.getCookieShower();
        this.localMapSB = new AppComponents().lastNetScanMap();
    }

    @GetMapping("/serviceinfo")
    public String infoMapping(Model model, HttpServletRequest request) throws AccessDeniedException {
        try{
            visitorSrv.makeVisit(request);
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
        model.addAttribute("title", "srv-git is " + pingBool() + "noF: " +
            ConstantsFor.NO_F_HRS);
        model.addAttribute("ping", pingGit());
        model.addAttribute("urls", new TForms().fromArray(AppCtx.getClassLoaderURLList()));
        model.addAttribute("request", prepareRequest(request));
        model.addAttribute("visit", AppComponents
            .versionInfo().toString() + " (current stamp: " + System.currentTimeMillis() + ")");
        model.addAttribute("genstamp", "Generated: " +
            new Date().getTime() +
            ". Up: " +
            TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - ConstantsFor.START_STAMP));
        model.addAttribute("back", request.getHeader("REFERER".toLowerCase()));
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
            return "<b>" + byName.isReachable(1000) + "</b> srv-git.eatmeat.ru. <i>" + LocalTime.now() + "</i><br>";
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
        stringBuilder.append(cookieShower.showCookie());
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
