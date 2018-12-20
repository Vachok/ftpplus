package ru.vachok.networker.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.*;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.Switches;
import ru.vachok.networker.services.MyCalen;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.AccessDeniedException;
import java.time.LocalTime;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;


/**
 Вывод различной сопутствующей информации

 @since 21.09.2018 (11:33) */
@Controller
public class ServiceInfoCtrl {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceInfoCtrl.class.getSimpleName());

    private boolean authReq;

    private static final Properties PROPS = ConstantsFor.getPROPS();

    private float getLast() {
        return TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() -
            Long.parseLong(ConstantsFor.getPROPS().getProperty("lasts", 1544816520000L + ""))) / 60f / 24f;
    }

    @Autowired
    public ServiceInfoCtrl() {
        new AppComponents();
    }

    @GetMapping("/serviceinfo")
    public String infoMapping(Model model, HttpServletRequest request, HttpServletResponse response) throws AccessDeniedException {
        this.authReq = request.getRemoteAddr().contains("0:0:0:0") ||
            request.getRemoteAddr().contains("10.10.111") ||
            request.getRemoteAddr().contains(ConstantsFor.NO0027) ||
            request.getRemoteAddr().contains("172.16.20");
        Visitor visitor = new Visitor(request);
        try {
            String msg = visitor.toString();
            LOGGER.warn(msg);
        } catch (Exception e) {
            LoggerFactory.getLogger(ServiceInfoCtrl.class.getSimpleName());
        }
        if (authReq) {
            modModMaker(model, request);
            response.addHeader("Refresh", "11");
            return "vir";
        } else {
            throw new AccessDeniedException("Sorry. Denied");
        }
    }

    private void modModMaker(Model model, HttpServletRequest request) {
        model.addAttribute(ConstantsFor.TITLE, getLast() + " (" + getLast() * ConstantsFor.ONE_DAY_HOURS + ")");
        model.addAttribute("mail", ConstantsFor.percToEnd());
        model.addAttribute("ping", pingGit());
        model.addAttribute("urls", new StringBuilder()
            .append("Запущено - ")
            .append(new Date(ConstantsFor.START_STAMP)).append(ConstantsFor.getUpTime())
            .append("<br>Точное время: ")
            .append(ConstantsFor.getAtomicTime())
            .append(". Состояние памяти (МБ): <font color=\"#82caff\">")
            .append(ConstantsFor.showMem()).append("</font><p>")
            .append(Switches.toStringS())
            .toString());
        model.addAttribute("request", prepareRequest(request));
        model.addAttribute("visit", new VersionInfo().toString());
        model.addAttribute("res", MyCalen.toStringS());
        model.addAttribute("back", request.getHeader("REFERER".toLowerCase()));
        model.addAttribute(ConstantsFor.FOOTER, new PageFooter().getFooterUtext() + "<br>" + getJREVers());
    }

    @GetMapping("/stop")
    public String closeApp() throws AccessDeniedException {
        if (authReq) {
            FileSystemWorker.delTemp();
            ConstantsFor.saveProps(PROPS);
            System.exit(ConstantsFor.USER_EXIT);
        } else {
            throw new AccessDeniedException("DENY!");
        }
        return "ok";
    }

    private String getJREVers() {
        return System.getProperty("java.version");
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

        return stringBuilder.toString();
    }

    private String pingGit() {
        boolean reachable = false;
        try {
            InetAddress byName = InetAddress.getByName("srv-git.eatmeat.ru");
            reachable = byName.isReachable(1000);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        String s = "</b> srv-git.eatmeat.ru.</font> Checked at: <i>";
        String s2 = "</i><br>";
        String s1 = "<b><font color=\"#77ff72\">" + true + s + LocalTime.now() + s2;
        if (reachable) {
            return s1;
        } else {
            return "<b><font color=\"#ff2121\">" + true + s + LocalTime.now() + s2;
        }
    }

    @GetMapping ("/pcoff")
    public void offPC(Model model) throws IOException {
        Runtime.getRuntime().exec("shutdown /p /f");
    }
}
