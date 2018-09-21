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
            model.addAttribute("genstamp", "Generated: " + new Date().getTime());
            return "vir";
        } else throw new AccessDeniedException("Sorry. Denied");
    }

    private String prepareRequest(HttpServletRequest request) {

        return null;
    }
}
