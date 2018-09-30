package ru.vachok.money.ctrls;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.money.ConstantsFor;
import ru.vachok.money.components.Visitor;
import ru.vachok.money.config.AppCtx;
import ru.vachok.money.services.TimeWorms;
import ru.vachok.money.services.WhoIsWithSRV;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 @since 20.08.2018 (17:08) */
@Controller
public class Index {

    /*Fields*/
    private static final Logger LOGGER = LoggerFactory.getLogger(Index.class.getSimpleName());

    private WhoIsWithSRV whoIsWithSRV;

    @Value ("version")
    private String appVersion;

    private Visitor visitor;

    private TimeWorms timeWorms;

    /*Instances*/
    @Autowired
    public Index(WhoIsWithSRV whoIsWithSRV, Visitor visitor, TimeWorms timeWorms) {
        this.whoIsWithSRV = whoIsWithSRV;
        this.visitor = visitor;
        this.timeWorms = timeWorms;
        ConfigurableEnvironment environment = AppCtx.getCtx().getEnvironment();
        for(String profile : environment.getActiveProfiles()){
            String msg = profile + " profile (WTF?)";
            LOGGER.info(msg);
        }
    }

    @GetMapping ("/")
    public String indexString(HttpServletRequest request, HttpServletResponse response, Model model) {
        try{
            visitor.getVisitorSrv().makeVisit(request, response);
        }
        catch(Exception e){
            LOGGER.warn(e.getMessage());
        }
        model.addAttribute("title", request.getRemoteAddr() + " " + response.getStatus());
        model.addAttribute("timeleft", timeWorms.timeLeft());
        model.addAttribute("geoloc", whoIsWithSRV.whoIs(request.getRemoteAddr()));

        if(ConstantsFor.localPc().equalsIgnoreCase("home")){
            return "index-start";
        }
        return "home";
    }
}
