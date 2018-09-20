package ru.vachok.money.ctrls;


import org.slf4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.money.ConstantsFor;

import ru.vachok.money.services.WhoIsWithSRV;
import ru.vachok.money.services.TimeWorks;
import ru.vachok.money.services.VisitorSrv;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @since 20.08.2018 (17:08)
 */
@Controller
public class Index {

    private static final Logger LOGGER = ConstantsFor.getLogger();

    private static final AnnotationConfigApplicationContext ctx = ConstantsFor.CONTEXT;

    @GetMapping("/")
    public String indexString(HttpServletRequest request, HttpServletResponse response, Model model) {
        TimeWorks timeWorks = ctx.getBean(TimeWorks.class);
        VisitorSrv visitorSrv = ctx.getBean(VisitorSrv.class);
        WhoIsWithSRV whoIsWithSRV = ctx.getBean(WhoIsWithSRV.class);
        visitorSrv.makeVisit(request, response);
        model.addAttribute("title", request.getRemoteAddr() + " " + response.getStatus());
        model.addAttribute("timeleft", timeWorks.timeLeft());
        model.addAttribute("geoloc", whoIsWithSRV.whoIs(request.getRemoteAddr()));
        LOGGER.info(ConstantsFor.localPc());

        if (ConstantsFor.localPc().equalsIgnoreCase("home")) return "index-start";
        else return "home";
    }
}
