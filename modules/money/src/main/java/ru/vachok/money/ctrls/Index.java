package ru.vachok.money.ctrls;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.money.config.AppCtx;
import ru.vachok.money.config.ConstantsFor;
import ru.vachok.money.services.TimeWorks;
import ru.vachok.money.services.VisitorSrv;
import ru.vachok.money.services.WhoIsWithSRV;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 @since 20.08.2018 (17:08) */
@Controller
public class Index {

    /*Fields*/
    private static final Logger LOGGER = LoggerFactory.getLogger(Index.class.getSimpleName());

    private TimeWorks timeWorks;

    private VisitorSrv visitorSrv;

    private WhoIsWithSRV whoIsWithSRV;

    @Value ("version")
    private String appVersion;

    /*Instances*/
    @Autowired
    public Index(TimeWorks timeWorks, VisitorSrv visitorSrv, WhoIsWithSRV whoIsWithSRV) {
        this.visitorSrv = visitorSrv;
        this.whoIsWithSRV = whoIsWithSRV;
        this.timeWorks = timeWorks;
        ConfigurableEnvironment environment = AppCtx.getCtx().getEnvironment();
        for(String profile : environment.getActiveProfiles()){
            String msg = profile + " profile (WTF?)";
            LOGGER.info(msg);
        }
        ;
    }

    @GetMapping ("/")
    public String indexString(HttpServletRequest request, HttpServletResponse response, Model model) {
        visitorSrv.makeVisit(request, response);
        model.addAttribute("title", request.getRemoteAddr() + " " + response.getStatus());
        model.addAttribute("timeleft", timeWorks.timeLeft());
        model.addAttribute("geoloc", whoIsWithSRV.whoIs(request.getRemoteAddr()));

        if(ConstantsFor.localPc().equalsIgnoreCase("home")){
            return "index-start";
        }
        return "home";
    }
}
