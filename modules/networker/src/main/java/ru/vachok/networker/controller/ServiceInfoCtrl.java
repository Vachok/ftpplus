package ru.vachok.networker.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.networker.AppCtx;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.services.VisitorSrv;

import javax.servlet.http.HttpServletRequest;
import java.nio.file.AccessDeniedException;
import java.util.Date;
import java.util.concurrent.TimeUnit;


/**
 * @since 21.09.2018 (11:33)
 */
@Controller
public class ServiceInfoCtrl {

    @GetMapping("/serviceinfo")
    public String infoMapping(Model model, HttpServletRequest request) throws AccessDeniedException {
        VisitorSrv visitorSrv = (VisitorSrv) ConstantsFor.BEAN_FACTORY.getBean("visitorSrv");
        visitorSrv.makeVisit(request);
        if (request.getRemoteAddr().contains("0:0:0:0") ||
            request.getRemoteAddr().contains("10.10.111") ||
            request.getRemoteAddr().contains(ConstantsFor.NO0027)) {
            model.addAttribute("title", "Closed section");
            model.addAttribute("urls", new TForms().fromArray(AppCtx.getClassLoaderURLList()));
            model.addAttribute("request", prepareRequest(request));
            model.addAttribute("genstamp", "Generated: " +
                new Date().getTime() +
                ". Up: " +
                TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - ConstantsFor.START_STAMP));
            return "vir";
        } else throw new AccessDeniedException("Sorry. Denied");
    }

    private String prepareRequest(HttpServletRequest request) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<center><h3>Заголовки</h3></center>");
        stringBuilder.append(new TForms().fromEnum(request.getHeaderNames(), true));
        stringBuilder.append("<center><h3>Атрибуты</h3></center>");
        stringBuilder.append(new TForms().fromEnum(request.getAttributeNames(), true));
        stringBuilder.append("<center><h3>Параметры</h3></center>");
        stringBuilder.append(new TForms().fromEnum(request.getParameterNames(), true));
        return stringBuilder.toString();
    }
}
