package ru.vachok.money.ctrls;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.money.config.AppComponents;
import ru.vachok.money.config.AppCtx;
import ru.vachok.money.services.AppVerSrv;

import javax.servlet.http.HttpServletRequest;


/**
 @since 27.09.2018 (0:01) */
@Controller
public class SysInfoCtrl {

    private AppVerSrv appVerSrv;

    @GetMapping ("/sysinfo")
    public String gettingInfo(Model model, HttpServletRequest request) {
        this.appVerSrv = new AppComponents().appVerSrv();
        model.addAttribute("title", appVerSrv.getVerID() + " IDver" + "|" + request.getSession().isNew() + " is new session.");
        model.addAttribute("ctxinfo", AppCtx.getCtxInfoFromRequest(request));
        return "sysinfo";
    }
}