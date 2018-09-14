package ru.vachok.networker.controller;


import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.networker.IntoApplication;
import ru.vachok.networker.services.VisitorSrv;

import javax.servlet.http.HttpServletRequest;


/**
 * @since 30.08.2018 (10:10)
 */
@Controller
public class ActiveDirCtr {

    private static AnnotationConfigApplicationContext ctx = IntoApplication.getAppCtx();

    private VisitorSrv visitorSrv = ctx.getBean(VisitorSrv.class);

    @GetMapping("/ad")
    public String initUser(Model model, HttpServletRequest request) {
        visitorSrv.makeVisit(request);
        model.addAttribute("title", visitorSrv.toString());
        return "ad";
    }
}
